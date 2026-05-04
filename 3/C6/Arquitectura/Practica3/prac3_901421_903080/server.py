"""
Módulo de Servidor para el Broker de Mensajes (MOM).

Implementa el núcleo del sistema de mensajería asíncrona. Gestiona:
- Persistencia robusta con Bootstrap completo (Evita pérdida de datos/Data Wipe).
- Distribución 'Fair Dispatch' utilizando 'Prefetch=1'.
- Resiliencia mediante expiración de mensajes (TTL), ACK Timeout (contra starvation)
  y rescate instantáneo por desconexión de red de consumidores.
- Concurrencia segura mediante candados reentrantes (RLock) para aislar colas.
- Buffer de red de bytes puro contra ataques o fallos de fragmentación TCP.
"""

import socket # Proveedor de la comunicación de red TCP/IP
import threading # Proveedor de hilos y mecanismos de exclusión mutua (Locks) para concurrencia
import json # Proveedor del parseo y generación de tramas JSON
import time # Proveedor de métricas de tiempo para TTL y Timeouts
import uuid # Generador de identificadores únicos universales (ID de mensajes)
import logging # Sistema de trazas unificado
from storage import StorageManager # Nuestro propio gestor de persistencia atómica

# Configuración del sistema de Logging centralizado para el servidor
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(threadName)s: %(message)s'
)
logger = logging.getLogger(__name__)

# Parámetros Globales y de Configuración del sistema
HOST = '0.0.0.0' # 0.0.0.0 significa que el servidor aceptará conexiones de cualquier interfaz de red (Local o Externa)
PORT = 5555 # Puerto TCP de escucha estandarizado para el broker
MESSAGE_TTL = 10  # Tiempo en segundos que un mensaje puede existir en cola antes de ser descartado automáticamente. Ajustado a 10 para agilizar tests.
ACK_TIMEOUT = 5   # Tiempo máximo que se le otorga a un consumidor para que confirme (ACK) un mensaje. Ajustado a 5 para tests.
storage = StorageManager() # Instancia de la clase que interactúa con el disco

# --- ESTRUCTURAS DE DATOS EN MEMORIA ---
# 'queues' es el Diccionario Principal del Broker. Estructura:
# { "nombre_cola": { "messages": [...], "consumers": [...], "unacked": {...}, "next_idx": int, "lock": RLock } }
queues = {} 

# 'queues_master_lock' es el Candado Global (exclusión mutua) del servidor.
# Únicamente se utiliza para proteger modificaciones a la ESTRUCTURA del diccionario 'queues' 
# (ej. crear o borrar una cola completa), garantizando la integridad estructural en concurrencia.
queues_master_lock = threading.RLock()

def save_broker_state():
    """
    Vuelca el estado completo de las colas desde la RAM al disco.
    Para garantizar que no haya corrupciones estructurales, retiene el Master Lock
    durante la petición de guardado.
    """
    with queues_master_lock:
        storage.save_state(queues)

def get_queue(q_name, create=False):
    """
    Recupera un diccionario de datos (una cola) desde la memoria principal. 
    Si la cola no existe y create=True, la inicializa de forma segura.
    
    Args:
        q_name (str): Nombre de la cola a buscar.
        create (bool): Bandera indicando si se debe instanciar la cola si esta no existe.
    """
    # Adquirimos el candado maestro para evitar que dos hilos creen la misma cola simultáneamente
    with queues_master_lock:
        if q_name not in queues:
            if create:
                # Inicialización de la estructura interna de una nueva cola
                queues[q_name] = {
                    "messages": [], # Array FIFO para los mensajes listos para procesar
                    "consumers": [], # Array de sockets de los consumidores activos conectados
                    "unacked": {}, # Diccionario de seguimiento: Mensajes despachados que esperan ACK
                    "next_idx": 0, # Puntero interno para calcular la lógica de reparto Round-Robin
                    "lock": threading.RLock() # Candado AISLADO para esta cola. Permite el multi-tenancy.
                }
                logger.info(f"Cola '{q_name}' inicializada.")
        # Retorna el diccionario de datos de la cola (o None si no existe y create=False)
        return queues.get(q_name)

def cleanup_task():
    """
    Hilo daemon (en segundo plano) encargado de las tareas de mantenimiento de datos (Garbage Collector).
    Ejecuta ciclos periódicos para:
    1. Expirar mensajes antiguos superando el límite de tiempo (TTL).
    2. Detectar y rescatar mensajes retenidos por consumidores colgados o zombies (ACK Timeout / Starvation fix).
    """
    while True:
        # El hilo duerme 2 segundos entre cada ciclo de mantenimiento para no saturar la CPU
        time.sleep(2) 
        current_time = time.time() # Tomamos una "foto" del momento actual
        
        # Clonamos la lista de nombres de colas mientras retenemos el candado maestro para iterar seguros
        with queues_master_lock:
            q_names = list(queues.keys())
        
        # Iteramos sobre cada cola del sistema de forma independiente
        for q_name in q_names:
            q_data = get_queue(q_name)
            if not q_data: continue # Por seguridad, si la cola fue borrada entre medias, la saltamos
            
            # Retenemos el candado EXCLUSIVO de esta cola para no interferir con las operaciones en curso
            with q_data["lock"]:
                # --- Gestión de TTL (Time-To-Live) ---
                orig = len(q_data["messages"])
                # Filtramos la lista, quedándonos SOLO con los mensajes cuya edad no supera MESSAGE_TTL
                q_data["messages"] = [
                    m for m in q_data["messages"]
                    if (current_time - m["timestamp"]) <= MESSAGE_TTL
                ]
                # Si el tamaño original era mayor que el resultante, significa que hemos borrado mensajes
                if orig != len(q_data["messages"]):
                    logger.info(f"TTL: Limpieza en '{q_name}'.")
                    save_broker_state() # Guardamos en disco el estado actualizado tras la limpieza

                # --- Gestión de ACK Timeout (Control de Starvation) ---
                requeue_ids = []
                # Iteramos sobre los mensajes que están "en vuelo" (unacked).
                # Usamos list(items()) para congelar el estado del diccionario en una lista,
                # evitando el RuntimeError si otro hilo borra un elemento porque acaba de recibir un ACK.
                for mid, (msg, conn, t_envio) in list(q_data["unacked"].items()):
                    # Si el tiempo actual menos el tiempo de envío es mayor que el límite permitido
                    if (current_time - t_envio) > ACK_TIMEOUT:
                        # Marcamos este ID de mensaje para ser rescatado
                        requeue_ids.append(mid)
                
                # Por cada mensaje marcado como caducado
                for mid in requeue_ids:
                    # Lo extraemos (borramos) definitivamente de la lista de unacked
                    msg, _, _ = q_data["unacked"].pop(mid)
                    # Lo inyectamos DE VUELTA al PRINCIPIO de la cola principal (posición 0) 
                    # para que sea el siguiente en procesarse
                    q_data["messages"].insert(0, msg)
                    logger.warning(f"Rescate (Timeout): Mensaje {mid} en '{q_name}'.")
                
                # Si hemos rescatado al menos 1 mensaje, persistimos el nuevo estado en disco
                if requeue_ids:
                    save_broker_state()

def dispatch_messages(q_name):
    """
    Motor central de distribución de mensajes de una cola específica.
    Implementa 'Fair Dispatch' limitando los envíos concurrentes (Prefetch=1) 
    y garantizando un reparto equitativo mediante lógica Round-Robin sobre la lista de disponibles.
    
    Esta función es invocada de forma reactiva cada vez que: 
    - Entra un nuevo mensaje a la cola.
    - Se conecta un nuevo consumidor.
    - Un consumidor existente termina un mensaje enviando un ACK (liberándose).
    """
    q_data = get_queue(q_name)
    if not q_data: return
    
    # Lista temporal que acumulará los paquetes listos para enviar por la red.
    # Es fundamental para poder realizar los "sendall" (E/S de red) FUERA de los candados.
    to_send = []
    
    # Abrimos zona crítica bloqueando exclusivamente esta cola
    with q_data["lock"]:
        # Si no hay mensajes que procesar o no hay consumidores que los puedan procesar, abortamos
        if not q_data["messages"] or not q_data["consumers"]:
            return

        # --- Identificación de Estado (Prefetch = 1) ---
        # Buscamos qué sockets (consumidores) tienen un mensaje en vuelo (presentes en unacked)
        busy = {c for m, c, t in q_data["unacked"].values()}
        # Filtramos la lista de todos los consumidores de la cola, quedándonos solo con los "desocupados"
        available = [c for c in q_data["consumers"] if c not in busy]

        # Mientras tengamos mensajes en la bandeja Y tengamos consumidores libres...
        while q_data["messages"] and available:
            # Sacamos el mensaje más antiguo (FIFO) de la cola principal
            msg = q_data["messages"].pop(0)
            
            # --- Lógica de Balanceo Round-Robin ---
            # Aplicamos módulo a nuestro puntero interno sobre la cantidad de disponibles actuales.
            # Esto asegura que el índice resultante siempre es válido y reparte equitativamente.
            idx = q_data["next_idx"] % len(available)
            # Extraemos el socket afortunado de la lista de disponibles
            conn = available.pop(idx)
            # Avanzamos el puntero general para la siguiente iteración
            q_data["next_idx"] += 1
            
            # --- Transición de Estado a Unacked ---
            # Guardamos el mensaje en seguimiento, junto al socket responsable y el timestamp actual de despacho
            q_data["unacked"][msg["id"]] = (msg, conn, time.time())
            
            # Encolamos la pareja (socket, mensaje) para despacharlo cuando liberemos el candado
            to_send.append((conn, msg))

    # --- ZONA LIBRE DE CANDADOS (Operaciones de Red / I/O) ---
    # Realizamos el envío físico de bytes por TCP sin retener el Lock.
    # Esto previene que un consumidor con "red lenta" paralice a todos los demás hilos del sistema.
    for conn, msg in to_send:
        # Construimos el payload JSON estándar del protocolo
        payload = json.dumps({
            "action": "message", "queue": q_name, 
            "msg_id": msg["id"], "message": msg["data"]
        }) + "\n"
        
        try:
            # Enviamos el payload convertido a bytes
            conn.sendall(payload.encode('utf-8'))
            # Guardamos en disco que hemos entregado este mensaje (está en unacked)
            save_broker_state()
        except Exception:
            # Plan de Reversión: Si 'sendall' falla (por ej. la conexión TCP cayó milisegundos antes),
            # recuperamos el candado un instante para deshacer los cambios internos de la memoria.
            with q_data["lock"]:
                # Si todavía está en unacked, lo borramos de allí
                if msg["id"] in q_data["unacked"]:
                    del q_data["unacked"][msg["id"]]
                    # Y devolvemos el mensaje a la primera posición de la cola para no perderlo
                    q_data["messages"].insert(0, msg)
                # Si el socket responsable de la caída sigue en la lista de consumidores, lo expulsamos
                if conn in q_data["consumers"]:
                    q_data["consumers"].remove(conn)

def handle_client(conn, addr):
    """
    Manejador del ciclo de vida individual de la sesión TCP de un cliente (productor o consumidor).
    Opera en su propio hilo. Implementa el protocolo JSON con protección contra tramas TCP fragmentadas
    y rescate inteligente ('Socket Drop') en caso de desconexión anómala.
    
    Args:
        conn (socket): Objeto socket conectado representando al cliente.
        addr (tuple): Tupla con la IP y Puerto del cliente para propósitos de logging.
    """
    logger.info(f"Nuevo cliente conectado desde: {addr}")
    
    # Buffer de bytes crudos. Vital para la integridad del flujo TCP que no garantiza entregar paquetes enteros.
    raw_buffer = b"" 
    
    # Lista local al hilo para recordar a qué colas se ha suscrito este socket en concreto.
    my_subs = [] 
    
    try:
        # Bucle de escucha infinita durante la vida de la conexión
        while True:
            try:
                # Intentamos leer 4096 bytes del socket. Esta operación es bloqueante.
                data = conn.recv(4096)
            except (ConnectionResetError, ConnectionAbortedError): 
                # Si el cliente cierra forzosamente (cierra terminal de golpe), rompemos el bucle
                break
                
            # Si `data` está vacío (longitud 0), es el estándar TCP indicando cierre ordenado de la otra parte
            if not data: break
                
            # Añadimos los nuevos bytes recién leídos al final del buffer acumulativo
            raw_buffer += data
            
            # Buscamos si tenemos en el buffer un delimitador de mensaje '\n' (codificado a byte)
            while b"\n" in raw_buffer:
                # Extraemos la línea entera, separándola del resto de bytes en el buffer
                line_bytes, raw_buffer = raw_buffer.split(b"\n", 1)
                
                # Descartamos líneas vacías, para evitar procesar saltos de línea extra
                if not line_bytes.strip(): continue
                    
                try:
                    # Decodificamos solo la trama completa y asegurada a string UTF-8
                    line = line_bytes.decode('utf-8')
                    # Intentamos convertir la trama JSON en un objeto o estructura Python
                    req = json.loads(line)
                    
                    # --- VALIDACIÓN DE TIPO CRÍTICA ---
                    # Si el cliente envía '["hola"]' o '"string_puro"', json.loads funciona,
                    # pero req.get("...") fallará explotando el servidor. Comprobamos que sea diccionario.
                    if not isinstance(req, dict):
                        logger.warning(f"Protocolo violado por {addr}: La trama no es un objeto JSON.")
                        continue # Ignoramos este comando erróneo y seguimos escuchando

                    # Extraemos los valores clave que dictan qué desea hacer el cliente
                    action = req.get("action")
                    q_name = req.get("queue")
                    
                    # --- Lógica RPC: Declarar ---
                    if action == "declare":
                        get_queue(q_name, create=True) # Crea la cola (idempotente si ya existe)
                        save_broker_state() # Persiste la nueva cola en disco
                        conn.sendall(b'{"status": "ok"}\n') # Confirmamos éxito
                    
                    # --- Lógica RPC: Listar ---            
                    elif action == "list":
                        with queues_master_lock:
                            # Montamos una respuesta con todas las llaves (nombres de colas) maestras
                            res = json.dumps({"status": "ok", "queues": list(queues.keys())})
                            # Enviamos la lista al cliente
                            conn.sendall(res.encode() + b"\n")
                            
                    # --- Lógica RPC: Eliminar ---
                    elif action == "delete":
                        with queues_master_lock:
                            if q_name in queues:
                                # Eliminamos por completo la entrada de la memoria
                                del queues[q_name]
                                save_broker_state() # Consolidamos la destrucción en disco
                                conn.sendall(b'{"status": "ok"}\n')
                            else:
                                conn.sendall(b'{"status": "error", "message": "No existe"}\n')
                                
                    # --- Lógica Asíncrona: Publicar ---            
                    elif action == "publish":
                        q_data = get_queue(q_name)
                        # Requisito: "Si la cola no existe, se descarta silenciosamente"
                        if q_data:
                            # Bloqueamos la cola para mutar su lista de mensajes
                            with q_data["lock"]:
                                q_data["messages"].append({
                                    "id": str(uuid.uuid4()), # UUID único para garantizar seguimiento de ACKs
                                    "data": req.get("message"), # El payload de datos que envía el productor
                                    "timestamp": time.time() # Marca temporal base para chequear el TTL luego
                                })
                            save_broker_state() # Guardamos en disco que hemos recibido mensaje nuevo
                            # Intentamos notificar/enviar a cualquier consumidor ocioso
                            dispatch_messages(q_name)
                    
                    # --- Lógica Asíncrona: Suscripción ---    
                    elif action == "consume":
                        q_data = get_queue(q_name)
                        if q_data:
                            with q_data["lock"]:
                                # Evitamos duplicidades de registro del mismo socket en la lista
                                if conn not in q_data["consumers"]:
                                    q_data["consumers"].append(conn)
                                    # Anotamos a nivel local del hilo en qué cola estamos para limpieza final
                                    my_subs.append(q_name)
                            # Al haber un nuevo consumidor, forzamos un intento de despachar la cola
                            dispatch_messages(q_name)
                    
                    # --- Lógica Asíncrona: Reconocimiento (ACK) ---
                    elif action == "ack":
                        msg_id = req.get("msg_id")
                        q_data = get_queue(q_name)
                        if q_data and msg_id:
                            with q_data["lock"]:
                                # Si el mensaje existe en la lista de seguimiento, es que fue entregado
                                if msg_id in q_data["unacked"]:
                                    # Lo borramos: Significa que la tarea se procesó con éxito.
                                    del q_data["unacked"][msg_id]
                            # Como el consumidor ha terminado su tarea y nos mandó el ACK, 
                            # está libre. Invocamos al despachador para darle el siguiente mensaje.
                            dispatch_messages(q_name)

                except json.JSONDecodeError:
                    # Se captura únicamente si la cadena decodificada no forma un JSON válido (basura sintáctica)
                    logger.error(f"Error parseando estructura JSON desde {addr}")
                    
    finally:
        # --- PROCEDIMIENTO DE LIMPIEZA FINAL DE SESIÓN ('Socket Drop' Rescue) ---
        # Este bloque se ejecuta SIEMPRE que el cliente se vaya (por cierre normal, timeout, Exception o crash red)
        logger.info(f"Cliente desconectado: {addr}")
        
        # Iteramos únicamente sobre las colas en las que este cliente específico estaba inscrito
        for q_name in my_subs:
            q_data = get_queue(q_name)
            if q_data:
                # Bloqueamos la cola para mutar su estado
                with q_data["lock"]:
                    # Borramos al cliente de la lista de candidatos disponibles
                    if conn in q_data["consumers"]:
                        q_data["consumers"].remove(conn)
                    
                    # --- RESCATE INMEDIATO DE MENSAJES (Bypass de ACK_TIMEOUT) ---
                    # Comprobamos el diccionario de unacked. Si algún mensaje estaba asignado al socket `conn` 
                    # que acaba de desconectarse, significa que se quedó huérfano antes de poder procesarse y hacer ACK.
                    lost = [mid for mid, (m, c, t) in q_data["unacked"].items() if c == conn]
                    
                    for mid in lost:
                        # Lo extraemos y lo devolvemos inmediatamente a la cabecera de la cola maestra
                        msg, _, _ = q_data["unacked"].pop(mid)
                        q_data["messages"].insert(0, msg)
                
                # Si recuperamos al menos un mensaje huérfano
                if lost:
                    # Guardamos el disco para afianzar el retorno a la cola principal
                    save_broker_state()
                    # Avisamos de inmediato al despachador para que se lo asigne rápidamente a otro consumidor
                    dispatch_messages(q_name)
                    
        # Finalmente, cerramos el descriptor del socket
        conn.close()

def main():
    """
    Función de inicialización principal del Broker de Mensajes.
    Contiene la lógica de 'Bootstrap' (Arranque en Frío) para cargar datos de disco
    y revivir colas y mensajes que quedaron inconclusos tras la última parada del servidor.
    """
    logger.info(f"Servicio Broker iniciando operaciones en {HOST}:{PORT}")
    
    # --- FASE DE BOOTSTRAP ---
    # Leemos la estructura JSON almacenada en el archivo físico
    saved = storage.load_state()
    
    with queues_master_lock:
        # Por cada cola que existiera previamente en el fichero:
        for q_name, data in saved.items():
            # Recuperación crítica: Unimos en una sola lista los mensajes que estaban "esperando" (messages)
            # y los que se enviaron pero jamás fueron confirmados por un apagón (unacked).
            all_msgs = data.get("unacked", []) + data.get("messages", [])
            
            # Recreamos la estructura de memoria RAM, inyectando la lista de mensajes resultante
            queues[q_name] = {
                "messages": all_msgs,
                "consumers": [], # Obviamente, al arrancar, no hay clientes conectados aún
                "unacked": {}, # Vaciamos los unacked ya que ahora son mensajes "nuevos"
                "next_idx": 0,
                "lock": threading.RLock() # Asignamos un candado nuevo a la cola renacida
            }
            logger.info(f"Bootstrap: Cola '{q_name}' cargada exitosamente ({len(all_msgs)} msgs retenidos).")
    # ----------------------------

    # Arrancamos el Garbage Collector en un hilo que correrá paralelo al servidor.
    # Al ser 'daemon=True', se destruirá sin bloquear cuando paremos el programa principal.
    threading.Thread(target=cleanup_task, daemon=True).start()
    
    # Preparamos el Listener TCP (El zócalo maestro donde golpearán las conexiones nuevas)
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    # Opciones Avanzadas: SO_REUSEADDR permite reiniciar el servidor sin el error "Address already in use"
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    
    try:
        # Atamos el zócalo a la IP y Puerto
        server.bind((HOST, PORT))
        # Arrancamos el modo escucha, permitiendo encolar hasta 100 peticiones simultáneas de conexión
        server.listen(100)
        logger.info("Broker a la espera de conexiones...")
        
        # Bucle de vida infinito del servidor (El Main Loop)
        while True:
            # La instrucción accept() bloquea hasta que un cliente emisor/receptor solicita entrar
            conn, addr = server.accept()
            # Derivamos inmediatamente a este nuevo cliente a un hilo secundario y volvemos a escuchar
            threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()
    except KeyboardInterrupt:
        # Manejo limpio cuando el administrador pulsa Ctrl+C en consola
        logger.info("Cerrando servidor manualmente.")
    finally:
        # Limpieza final del socket maestro
        server.close()

if __name__ == "__main__":
    main()
