"""
Módulo de Servidor para el Broker de Mensajes (MOM).

Implementa el núcleo del sistema de mensajería asíncrona. Gestiona:
- Persistencia robusta con Bootstrap completo (Evita Data Wipe).
- Distribución Fair Dispatch (Prefetch=1).
- Resiliencia mediante ACK Timeout y Rescate de mensajes por desconexión.
- Concurrencia segura mediante RLock.
"""

import socket
import threading
import json
import time
import uuid
import logging
from storage import StorageManager

# Configuración de Logging centralizado
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(threadName)s: %(message)s'
)
logger = logging.getLogger(__name__)

# Parámetros del sistema
HOST = '0.0.0.0'
PORT = 5555
MESSAGE_TTL = 300 
ACK_TIMEOUT = 60  # Tiempo para detectar consumidores colgados (Valor de producción)
storage = StorageManager()

# Punto 4: Uso de Candados Reentrantes para mayor seguridad en hilos
queues = {}
queues_master_lock = threading.RLock()

def save_broker_state():
    """Vuelca el estado completo de las colas a disco."""
    with queues_master_lock:
        storage.save_state(queues)

def get_queue(q_name, create=False):
    """
    Recupera una cola de la memoria. 
    
    Nota: Tras el fix del Punto 1, ya no lee de disco aquí para evitar borrar
    colas no cargadas durante el guardado. El Bootstrap lo hace todo al inicio.
    """
    with queues_master_lock:
        if q_name not in queues:
            if create:
                queues[q_name] = {
                    "messages": [],
                    "consumers": [],
                    "unacked": {}, # Diccionario de mensajes en proceso
                    "next_idx": 0,
                    "lock": threading.RLock()
                }
                logger.info(f"Cola '{q_name}' inicializada.")
        return queues.get(q_name)

def cleanup_task():
    """
    Hilo daemon encargado de las tareas de mantenimiento de datos:
    1. Expira mensajes antiguos (TTL).
    2. Detecta y rescata mensajes estancados por ACK Timeout (Starvation fix).
    """
    while True:
        time.sleep(30)
        current_time = time.time()
        
        with queues_master_lock:
            q_names = list(queues.keys())
        
        for q_name in q_names:
            q_data = get_queue(q_name)
            if not q_data: continue
            
            with q_data["lock"]:
                # --- Gestión de TTL ---
                orig = len(q_data["messages"])
                q_data["messages"] = [
                    m for m in q_data["messages"]
                    if (current_time - m["timestamp"]) <= MESSAGE_TTL
                ]
                if orig != len(q_data["messages"]):
                    logger.info(f"TTL: Limpieza en '{q_name}'.")

                # --- Gestión de ACK Timeout (Punto 3) ---
                requeue_ids = []
                for mid, (msg, conn, t_envio) in q_data["unacked"].items():
                    if (current_time - t_envio) > ACK_TIMEOUT:
                        requeue_ids.append(mid)
                
                for mid in requeue_ids:
                    msg, _, _ = q_data["unacked"].pop(mid)
                    q_data["messages"].insert(0, msg)
                    logger.warning(f"Rescate (Timeout): Mensaje {mid} en '{q_name}'.")
                
                if requeue_ids:
                    save_broker_state()

def dispatch_messages(q_name):
    """
    Algoritmo de distribución Fair Dispatch con reparto Round-Robin.
    
    Garantiza que ningún consumidor reciba más de un mensaje simultáneamente 
    (Prefetch count = 1), optimizando el balanceo de carga en tareas pesadas.
    """
    q_data = get_queue(q_name)
    if not q_data: return
    
    with q_data["lock"]:
        if not q_data["messages"] or not q_data["consumers"]:
            return

        # Identificación de consumidores ocupados
        busy = {c for m, c, t in q_data["unacked"].values()}
        available = [c for c in q_data["consumers"] if c not in busy]

        while q_data["messages"] and available:
            msg = q_data["messages"].pop(0)
            
            # Reparto equitativo (Round-Robin)
            idx = q_data["next_idx"] % len(available)
            conn = available.pop(idx)
            q_data["next_idx"] += 1
            
            # Registro en unacked con timestamp para timeout
            q_data["unacked"][msg["id"]] = (msg, conn, time.time())
            
            payload = json.dumps({
                "action": "message", "queue": q_name, 
                "msg_id": msg["id"], "message": msg["data"]
            }) + "\n"
            
            try:
                conn.sendall(payload.encode('utf-8'))
                save_broker_state()
            except Exception:
                # Reversión inmediata si el socket falla durante el envío
                del q_data["unacked"][msg["id"]]
                q_data["messages"].insert(0, msg)
                if conn in q_data["consumers"]:
                    q_data["consumers"].remove(conn)

def handle_client(conn, addr):
    """
    Manejador de sesión para un cliente individual.
    Implementa el protocolo JSON sobre TCP con soporte para reencolado por desconexión.
    """
    logger.info(f"Nuevo cliente: {addr}")
    buffer = ""
    my_subs = []
    
    try:
        while True:
            try:
                data = conn.recv(4096)
            except ConnectionResetError: break
            if not data: break
                
            buffer += data.decode('utf-8')
            while "\n" in buffer:
                line, buffer = buffer.split("\n", 1)
                if not line.strip(): continue
                    
                try:
                    req = json.loads(line)
                    action = req.get("action")
                    q_name = req.get("queue")
                    
                    if action == "declare":
                        get_queue(q_name, create=True)
                        save_broker_state()
                        conn.sendall(b'{"status": "ok"}\n')
                                
                    elif action == "list":
                        with queues_master_lock:
                            res = json.dumps({"status": "ok", "queues": list(queues.keys())})
                            conn.sendall(res.encode() + b"\n")

                    elif action == "delete":
                        with queues_master_lock:
                            if q_name in queues:
                                del queues[q_name]
                                save_broker_state()
                                conn.sendall(b'{"status": "ok"}\n')
                            else:
                                conn.sendall(b'{"status": "error", "message": "No existe"}\n')
                                
                    elif action == "publish":
                        q_data = get_queue(q_name)
                        if q_data:
                            with q_data["lock"]:
                                q_data["messages"].append({
                                    "id": str(uuid.uuid4()),
                                    "data": req.get("message"),
                                    "timestamp": time.time()
                                })
                            save_broker_state()
                            dispatch_messages(q_name)
                        
                    elif action == "consume":
                        q_data = get_queue(q_name)
                        if q_data:
                            with q_data["lock"]:
                                if conn not in q_data["consumers"]:
                                    q_data["consumers"].append(conn)
                                    my_subs.append(q_name)
                            dispatch_messages(q_name)
                    
                    elif action == "ack":
                        msg_id = req.get("msg_id")
                        q_data = get_queue(q_name)
                        if q_data and msg_id:
                            with q_data["lock"]:
                                if msg_id in q_data["unacked"]:
                                    del q_data["unacked"][msg_id]
                            dispatch_messages(q_name)

                except json.JSONDecodeError:
                    logger.error(f"Error JSON desde {addr}")
                    
    finally:
        # Lógica de limpieza y rescate de mensajes tras desconexión del cliente
        logger.info(f"Cliente desconectado: {addr}")
        for q_name in my_subs:
            q_data = get_queue(q_name)
            if q_data:
                with q_data["lock"]:
                    if conn in q_data["consumers"]:
                        q_data["consumers"].remove(conn)
                    
                    # Identificar mensajes que quedaron sin ACK para este socket
                    lost = [mid for mid, (m, c, t) in q_data["unacked"].items() if c == conn]
                    for mid in lost:
                        msg, _, _ = q_data["unacked"].pop(mid)
                        q_data["messages"].insert(0, msg)
                
                if lost:
                    save_broker_state()
                    dispatch_messages(q_name)
        conn.close()

def main():
    """
    Inicia el Broker de Mensajes.
    Punto 1 y 2: Implementa Bootstrap Completo para garantizar la durabilidad
    de todas las colas y mensajes (unacked incluidos) desde el arranque.
    """
    logger.info(f"Servicio Broker activo en {HOST}:{PORT}")
    
    # --- PROCESO DE BOOTSTRAP ---
    saved = storage.load_state()
    with queues_master_lock:
        for q_name, data in saved.items():
            # Punto 2: Rescate de mensajes que estaban 'en vuelo' en la sesión anterior
            all_msgs = data.get("messages", []) + data.get("unacked", [])
            queues[q_name] = {
                "messages": all_msgs,
                "consumers": [],
                "unacked": {},
                "next_idx": 0,
                "lock": threading.RLock()
            }
            logger.info(f"Bootstrap: Cola '{q_name}' cargada ({len(all_msgs)} msgs).")
    # ----------------------------

    # Hilo de mantenimiento en background
    threading.Thread(target=cleanup_task, daemon=True).start()
    
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    
    try:
        server.bind((HOST, PORT))
        server.listen(100)
        while True:
            conn, addr = server.accept()
            threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()
    except KeyboardInterrupt:
        logger.info("Cerrando servidor.")
    finally:
        server.close()

if __name__ == "__main__":
    main()
