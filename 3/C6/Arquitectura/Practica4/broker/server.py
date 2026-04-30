"""
Módulo de Servidor para el Broker de Mensajes (MOM).

Este componente actúa como el nodo central de una arquitectura orientada a mensajes.
Gestiona el ciclo de vida de las colas, garantiza la entrega mediante protocolos de 
confirmación (ACK), implementa políticas de distribución justa (Fair Dispatch) y 
mantiene la persistencia del estado en disco.

Atributos Globales:
    HOST (str): Interfaz de red de escucha (0.0.0.0 para todas las interfaces).
    PORT (int): Puerto TCP dedicado para el servicio del broker.
    MESSAGE_TTL (int): Tiempo de vida (Time To Live) de mensajes no entregados en segundos.
    storage (StorageManager): Instancia encargada de la persistencia física en disco.
"""

import os
import socket
import threading
import json
import time
import uuid
import logging
from storage import StorageManager

# Configuración de Logging profesional para el seguimiento y depuración
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(threadName)s: %(message)s'
)
logger = logging.getLogger(__name__)

# Configuración de red del servidor
HOST = '0.0.0.0'  # Escucha en todas las interfaces de red disponibles
PORT = 5555       # Puerto por defecto para las conexiones de los clientes
MESSAGE_TTL = 300 # Los mensajes expiran tras 5 minutos (300 segundos) en la cola sin ser consumidos
STORAGE_PATH = os.getenv('BROKER_STORAGE_PATH', 'broker_storage.json')
storage = StorageManager(STORAGE_PATH) # Gestor de persistencia

# Diccionario principal que almacena el estado de todas las colas en memoria
# Estructura: { "nombre_cola": { "messages": [...], "consumers": [...], "unacked": {...}, "next_idx": 0, "lock": Lock() } }
queues = {}

# Candado (Lock) global para proteger modificaciones estructurales sobre el diccionario 'queues'
queues_master_lock = threading.Lock()

def save_broker_state():
    """
    Sincroniza el estado actual de las colas con el almacenamiento físico.
    Utiliza el master_lock para garantizar una captura consistente del estado de todas las colas,
    evitando que se modifique la estructura mientras se serializa.
    """
    with queues_master_lock:
        # Se delega la serialización y guardado al gestor de almacenamiento
        storage.save_state(queues)

def get_queue(q_name, create=False):
    """
    Recupera una cola existente por su nombre o inicializa una nueva si se solicita.
    
    Args:
        q_name (str): Nombre identificador único de la cola.
        create (bool): Si es True, crea la cola en memoria si esta no existe previamente.
        
    Returns:
        dict: Estructura de datos completa de la cola solicitada, o None si no existe y create=False.
    """
    # Bloqueamos el acceso global para evitar condiciones de carrera al crear colas
    with queues_master_lock:
        if q_name not in queues:
            # Si la cola no está en memoria, intentamos cargarla del estado persistente
            saved_data = storage.load_state().get(q_name)
            
            # Si existía en disco o se pide creación explícita
            if saved_data or create:
                queues[q_name] = {
                    "messages": saved_data["messages"] if saved_data else [], # Carga mensajes previos si los hay
                    "consumers": [],                                        # Lista de sockets de consumidores activos
                    "unacked": {},                                          # Diccionario de mensajes enviados pendientes de ACK
                    "next_idx": 0,                                          # Índice para el reparto Round-Robin
                    "lock": threading.Lock()                                # Candado específico para esta cola (granularidad fina)
                }
                logger.info(f"Cola '{q_name}' activada. Mensajes cargados: {len(queues[q_name]['messages'])}")
        
        # Retorna la referencia a la cola
        return queues.get(q_name)

def cleanup_task():
    """
    Hilo de mantenimiento en segundo plano para la purga de mensajes expirados (TTL).
    Se ejecuta cíclicamente para liberar memoria de mensajes que superaron su tiempo de vida.
    """
    while True:
        # Pausa de 30 segundos entre cada ciclo de limpieza para no saturar la CPU
        time.sleep(30)
        current_time = time.time()
        
        # Obtenemos una copia segura de los nombres de las colas actuales
        with queues_master_lock:
            q_names = list(queues.keys())
        
        # Iteramos sobre cada cola para evaluar la expiración de sus mensajes
        for q_name in q_names:
            q_data = get_queue(q_name)
            if not q_data: continue # Si la cola desapareció en el proceso, la ignoramos
            
            # Bloqueamos exclusivamente esta cola durante la purga
            with q_data["lock"]:
                original_len = len(q_data["messages"])
                
                # Filtramos la lista conservando solo los mensajes cuyo tiempo de vida no exceda el TTL
                q_data["messages"] = [
                    m for m in q_data["messages"]
                    if (current_time - m["timestamp"]) <= MESSAGE_TTL
                ]
                
                diff = original_len - len(q_data["messages"])
                if diff > 0:
                    # Registramos si se han eliminado mensajes por expiración
                    logger.info(f"TTL: Purgados {diff} mensajes en '{q_name}'")

def dispatch_messages(q_name):
    """
    Algoritmo central de distribución de mensajes a los consumidores (Fair Dispatch).
    
    Selecciona consumidores que no tengan confirmaciones (ACKs) pendientes, aplicando 
    Round-Robin sobre el subconjunto de disponibles para balancear equitativamente la carga.
    
    Args:
        q_name (str): Nombre de la cola sobre la que se intentará despachar mensajes.
    """
    q_data = get_queue(q_name)
    if not q_data: return
    
    # Bloqueamos la cola para garantizar que el despacho y extracción de mensajes sea atómico
    with q_data["lock"]:
        # Condición de parada rápida: si no hay mensajes o no hay consumidores, no hay nada que hacer
        if not q_data["messages"] or not q_data["consumers"]:
            return

        # Implementación de Fair Dispatch (Prefetch count = 1)
        # 1. Identificamos qué consumidores están actualmente ocupados procesando un mensaje
        busy_consumers = {c for m, c in q_data["unacked"].values()}
        
        # 2. Filtramos la lista total de consumidores para obtener solo los que están libres
        available = [c for c in q_data["consumers"] if c not in busy_consumers]

        # Mientras haya mensajes por enviar y consumidores libres para recibirlos
        while q_data["messages"] and available:
            # Extraemos el mensaje más antiguo de la cola (FIFO)
            msg = q_data["messages"].pop(0)
            
            # Seleccionamos un consumidor usando aritmética modular (Round-Robin)
            idx = q_data["next_idx"] % len(available)
            conn = available.pop(idx) # Lo quitamos de available para esta iteración
            
            # Incrementamos el índice para que en la próxima vuelta se seleccione el siguiente
            q_data["next_idx"] += 1
            
            # Registramos el mensaje como "enviado pero no confirmado" (Unacked)
            # Guardamos la tupla (mensaje, conexion_consumidor)
            q_data["unacked"][msg["id"]] = (msg, conn)
            
            # Preparamos la trama JSON a enviar al cliente
            payload = json.dumps({
                "action": "message",
                "queue": q_name,
                "msg_id": msg["id"],
                "message": msg["data"]
            }) + "\n" # Delimitador de trama
            
            try:
                # Intentamos enviar el mensaje por el socket del consumidor
                conn.sendall(payload.encode('utf-8'))
                # Guardamos el estado del broker (el mensaje ya no está en la cola principal)
                save_broker_state()
            except Exception:
                # Recuperación ante fallos: si el envío falla (ej. red caída), revertimos
                del q_data["unacked"][msg["id"]]
                # Devolvemos el mensaje al principio de la cola para que sea el primero en reintentarse
                q_data["messages"].insert(0, msg)
                # Si la conexión falló, asumimos que el consumidor murió y lo eliminamos de la lista
                if conn in q_data["consumers"]:
                    q_data["consumers"].remove(conn)

def handle_client(conn, addr):
    """
    Gestiona el ciclo de vida de una conexión TCP individual con un cliente.
    
    Implementa el protocolo de tramas JSON delimitadas por saltos de línea (\n).
    Asegura la recuperación de mensajes no confirmados si el cliente se desconecta abruptamente.
    
    Args:
        conn (socket.socket): El socket de la conexión establecida.
        addr (tuple): Tupla con la IP y puerto del cliente remoto.
    """
    logger.info(f"Nueva conexión establecida desde {addr}")
    buffer = ""       # Buffer de recepción para recomponer tramas parciales
    my_subs = []      # Registro de las colas a las que se suscribe este cliente
    
    try:
        # Bucle de recepción continua de datos
        while True:
            try:
                data = conn.recv(4096) # Lectura de fragmentos de hasta 4KB
            except ConnectionResetError: 
                # El cliente forzó el cierre de la conexión RST
                break
                
            # Si recv retorna vacío, el cliente cerró la conexión ordenadamente FIN
            if not data: break
                
            # Decodificamos bytes a string y acumulamos en el buffer
            buffer += data.decode('utf-8')
            
            # Procesamos todas las tramas completas (delimitadas por \n) presentes en el buffer
            while "\n" in buffer:
                # Separamos la primera línea del resto del buffer
                line, buffer = buffer.split("\n", 1)
                
                # Ignoramos líneas en blanco
                if not line.strip(): continue
                    
                try:
                    # Parseamos la trama JSON
                    req = json.loads(line)
                    action = req.get("action") # Comando solicitado
                    q_name = req.get("queue")  # Cola objetivo
                    
                    if action == "declare":
                        # Garantiza la existencia de la cola (Idempotencia)
                        get_queue(q_name, create=True)
                        save_broker_state()
                        # Responde al cliente confirmando la creación
                        conn.sendall(b'{"status": "ok"}\n')
                                
                    elif action == "list":
                        # Solicita el listado de colas existentes
                        with queues_master_lock:
                            response = json.dumps({"status": "ok", "queues": list(queues.keys())})
                            conn.sendall(response.encode() + b"\n")

                    elif action == "delete":
                        # Elimina una cola y todo su contenido
                        with queues_master_lock:
                            if q_name in queues:
                                del queues[q_name]
                                save_broker_state()
                                conn.sendall(b'{"status": "ok"}\n')
                            else:
                                # La cola no existe, se notifica error
                                conn.sendall(b'{"status": "error", "message": "Cola no encontrada"}\n')
                                
                    elif action == "publish":
                        # El cliente envía un mensaje para publicar en la cola
                        q_data = get_queue(q_name)
                        if q_data:
                            # Bloqueamos la cola para insertar el mensaje de forma segura
                            with q_data["lock"]:
                                q_data["messages"].append({
                                    "id": str(uuid.uuid4()),      # Identificador único del mensaje
                                    "data": req.get("message"),   # El contenido o payload real
                                    "timestamp": time.time()      # Marca de tiempo para controlar el TTL
                                })
                            # Guardamos en disco el nuevo mensaje
                            save_broker_state()
                            # Intentamos despachar el mensaje a un consumidor inmediatamente
                            dispatch_messages(q_name)
                        
                    elif action == "consume":
                        # El cliente se registra como consumidor de esta cola
                        q_data = get_queue(q_name)
                        if q_data:
                            with q_data["lock"]:
                                # Evitamos duplicidades de registro del mismo socket
                                if conn not in q_data["consumers"]:
                                    q_data["consumers"].append(conn)
                                    my_subs.append(q_name) # Recordamos a qué está suscrito
                            # Revisamos si hay mensajes acumulados esperando ser despachados
                            dispatch_messages(q_name)
                    
                    elif action == "ack":
                        # El cliente confirma que procesó exitosamente un mensaje
                        msg_id = req.get("msg_id")
                        q_data = get_queue(q_name)
                        if q_data and msg_id:
                            with q_data["lock"]:
                                # Si el mensaje está en pendientes, se elimina definitivamente (Acknowledgement)
                                if msg_id in q_data["unacked"]:
                                    del q_data["unacked"][msg_id]
                            # Tras confirmar, el consumidor está libre, así que intentamos despachar otro
                            dispatch_messages(q_name)

                except json.JSONDecodeError:
                    logger.error(f"Error de protocolo JSON desde {addr}. Trama mal formada.")
                    
    finally:
        # Lógica ejecutada SIEMPRE al cerrar la conexión (por error o voluntariamente)
        logger.info(f"Cierre de sesión finalizado para: {addr}")
        
        # Recuperación de mensajes pendientes de confirmación (Message Durability / Rescue)
        for q_name in my_subs:
            q_data = get_queue(q_name)
            if q_data:
                lost_msgs = []
                with q_data["lock"]:
                    # Retiramos al socket muerto de la lista de consumidores
                    if conn in q_data["consumers"]:
                        q_data["consumers"].remove(conn)
                    
                    # Buscamos todos los mensajes "unacked" asignados a esta conexión muerta
                    lost_msgs = [mid for mid, (m, c) in q_data["unacked"].items() if c == conn]
                    
                    # Para cada mensaje perdido, lo rescatamos y lo devolvemos a la cola
                    for mid in lost_msgs:
                        msg, _ = q_data["unacked"].pop(mid)
                        q_data["messages"].insert(0, msg) # Se inserta al principio para prioridad
                        logger.info(f"Rescate: Mensaje {mid} reencolado debido a desconexión del consumidor.")
                
                # Si hubo mensajes rescatados, hay que actualizar el disco e intentar redistribuir
                if lost_msgs:
                    save_broker_state()
                    dispatch_messages(q_name)
        
        # Cerramos físicamente el socket para liberar recursos del SO
        conn.close()

def main():
    """
    Punto de entrada principal. Inicia el servidor TCP y levanta los hilos de soporte.
    """
    logger.info(f"Iniciando servicio de Broker en la dirección {HOST}:{PORT}")
    
    # Hilo en segundo plano (daemon) para limpieza de TTL. Muere al morir el hilo principal.
    threading.Thread(target=cleanup_task, daemon=True).start()
    
    # Creación del socket servidor TCP
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # Permite reusar el puerto inmediatamente después de reiniciar el script (evita errores 'Address in use')
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    
    try:
        # Enlazamos el socket a la IP y puerto definidos
        server.bind((HOST, PORT))
        # Escuchamos conexiones entrantes. El backlog es 100 conexiones en espera.
        server.listen(100)
        
        # Bucle infinito para aceptar clientes
        while True:
            # Bloquea hasta que un nuevo cliente se conecte
            conn, addr = server.accept()
            # Por cada cliente, se crea un hilo dedicado para gestionar su comunicación
            threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()
            
    except KeyboardInterrupt:
        # Captura interrupción manual (Ctrl+C) para apagar elegantemente
        logger.info("Señal de apagado recibida. Cerrando servidor de forma ordenada.")
    finally:
        # Cierra el socket de escucha general
        server.close()

if __name__ == "__main__":
    main()
