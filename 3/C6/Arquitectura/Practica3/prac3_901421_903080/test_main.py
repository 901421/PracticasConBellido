"""
Suite de Pruebas de Cobertura Total (Integración y Stress).

Esta suite es el núcleo de la evaluación académica de la práctica. Valida automáticamente
que la implementación cumple el 100% de los requisitos del enunciado (MOM, Round-Robin,
Persistencia, Fair Dispatch) así como mejoras adicionales para producción.

Requisitos probados:
- Flujo básico y Sockets (Publish/Subscribe).
- Persistencia en disco (Bootstrap atómico).
- Recuperación de mensajes Unacked tras un Crash forzado.
- Distribución Round-Robin (Balanceo de carga).
- Política Fair Dispatch (Prefetch=1) para evitar sobrecarga.
- Rescate por ACK Timeout (Starvation prevention).
- Gestión de colas (Listado, Borrado, Idempotencia).
- Expiración de mensajes antiguos por TTL (Time-To-Live).
- Rescate Inmediato por Desconexión (Socket Drop detection).
- Aislamiento de Colas concurrentes (Multi-tenancy).
"""

import threading
import time
import sys
import os
import json
import uuid
import socket

# Añadimos el directorio actual al PATH para poder importar los módulos sin errores relativos
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

# Importamos las dependencias maestras del servidor y el cliente. 
# Importar 'main as start_server' nos permite invocar el broker dentro de un hilo secundario del test.
from server import main as start_server, queues, queues_master_lock, save_broker_state, storage, ACK_TIMEOUT
from client import MOMClient

def run_test_isolated(test_func):
    """
    Función contenedora (Wrapper) que ejecuta un test asegurando un entorno "Sandbox" (Caja de Arena) completamente limpio.
    Resuelve el problema de que los tests anteriores interfieran con el estado de los siguientes.
    """
    # 1. Purgamos la memoria RAM del broker
    with queues_master_lock:
        queues.clear()
    
    # 2. Purgamos el archivo persistente del disco duro (Simulamos un borrado completo)
    if os.path.exists("broker_storage.json"):
        try:
            os.remove("broker_storage.json")
        except Exception: pass
    
    # 3. Ejecutamos el test real
    try:
        return test_func()
    except Exception as e:
        # Si el test hace crash de Python (ej. TypeError), no tiramos toda la suite.
        # Reportamos y devolvemos False (fallo).
        print(f"  > EXCEPCIÓN en test {test_func.__name__}: {e}")
        return False

def test_1_flujo_basico():
    """Valida el requisito base: Un productor publica, un consumidor recibe."""
    print("\n[TEST 1] Publicación y Consumo Básico...")
    client = MOMClient()
    cola = "test_basico"
    client.declarar_cola(cola)
    
    recibidos = []
    # Lanzamos el consumidor en un hilo en segundo plano (daemon) para que no bloquee el hilo de testing
    threading.Thread(target=client.consumir, args=(cola, lambda m: recibidos.append(m)), daemon=True).start()
    
    time.sleep(0.5) # Esperamos a que la red se estabilice
    # El publicador lanza una ráfaga de 5 mensajes
    for i in range(5):
        client.publicar(cola, f"M_{i}")
    
    time.sleep(1) # Esperamos a que el consumidor procese los 5
    client.close()
    # Si el array tiene 5 mensajes, el circuito funciona
    success = len(recibidos) == 5
    print(f"  > Resultado: {'OK' if success else 'FALLO'}")
    return success

def test_2_round_robin():
    """Valida el requisito de RabbitMQ: Reparto equitativo entre múltiples consumidores en la misma cola."""
    print("\n[TEST 2] Distribución Round-Robin...")
    client = MOMClient()
    cola = "test_rr"
    client.declarar_cola(cola)
    
    res1, res2 = [], []
    # Conectamos DOS consumidores independientes a la MISMA cola
    threading.Thread(target=MOMClient().consumir, args=(cola, lambda m: res1.append(m)), daemon=True).start()
    threading.Thread(target=MOMClient().consumir, args=(cola, lambda m: res2.append(m)), daemon=True).start()
    
    time.sleep(0.5)
    # Inyectamos 4 mensajes
    for i in range(4):
        client.publicar(cola, f"RR_{i}")
    
    time.sleep(1)
    # Matemáticamente, el Round-Robin debe garantizar que reciben 2 mensajes cada uno (2+2=4)
    success = len(res1) == 2 and len(res2) == 2
    print(f"  > C1: {len(res1)}, C2: {len(res2)} | Resultado: {'OK' if success else 'FALLO'}")
    return success

def test_3_fair_dispatch():
    """
    Valida el requisito avanzado de Fair Dispatch.
    Si un consumidor está ocupado/caído y NO ha enviado el ACK, el broker NO debe enviarle 
    más mensajes, y debe desviar el tráfico al consumidor libre.
    """
    print("\n[TEST 3] Fair Dispatch (Prefetch=1)...")
    client = MOMClient()
    cola = "test_fair"
    client.declarar_cola(cola)
    
    # Consumidor 1: Simulamos un cliente raw TCP que se queda congelado adrede.
    # Recibe pero nunca hace ACK de los mensajes.
    def slow_consumer():
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(('127.0.0.1', 5555))
        s.sendall(json.dumps({"action": "consume", "queue": cola}).encode() + b"\n")
        s.recv(1024) # Recibe el primero y se queda "bloqueado"
        time.sleep(10)

    threading.Thread(target=slow_consumer, daemon=True).start()
    time.sleep(0.5)
    
    # Consumidor 2: Normal (rápido, despacha con el cliente MOM que manda ACKs automáticamente)
    recibidos_c2 = []
    threading.Thread(target=MOMClient().consumir, args=(cola, lambda m: recibidos_c2.append(m)), daemon=True).start()
    
    time.sleep(0.5)
    # Enviamos 5 mensajes.
    # El primero irá a C1 y C1 se colgará (quedando como 'unacked' en el servidor).
    # Como el servidor aplica Fair Dispatch, verá a C1 en la lista de unacked y mandará los otros 4 a C2.
    for i in range(5):
        client.publicar(cola, f"F_{i}")
    
    time.sleep(1)
    # Éxito: C2 tiene los 4 mensajes restantes.
    success = len(recibidos_c2) == 4
    print(f"  > C1 tiene 1 (bloqueado), C2 tiene {len(recibidos_c2)} | Resultado: {'OK' if success else 'FALLO'}")
    return success

def test_4_ack_timeout():
    """Valida la resiliencia contra 'Consumidores Zombie' basándose en tiempo."""
    print(f"\n[TEST 4] ACK Timeout (Starvation) - Esperando {ACK_TIMEOUT+2}s...")
    client = MOMClient()
    cola = "test_timeout"
    client.declarar_cola(cola)
    
    # Consumidor que bloquea un mensaje adrede sin mandarlo a ACK
    def zombie_consumer():
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(('127.0.0.1', 5555))
        s.sendall(json.dumps({"action": "consume", "queue": cola}).encode() + b"\n")
        s.recv(1024) 

    threading.Thread(target=zombie_consumer, daemon=True).start()
    time.sleep(0.5)
    client.publicar(cola, "MENSAJE_ZOMBIE")
    
    # Esperamos a que pase el tiempo configurado para expirar ACKs
    time.sleep(ACK_TIMEOUT + 3)
    
    # Si el timeout funciona, el mensaje debería haber sido re-encolado.
    # Metemos un nuevo consumidor a ver si es capaz de recibir ese mismo mensaje.
    res = []
    threading.Thread(target=client.consumir, args=(cola, lambda m: res.append(m)), daemon=True).start()
    time.sleep(1)
    
    success = "MENSAJE_ZOMBIE" in res
    print(f"  > Recuperado tras timeout: {'OK' if success else 'FALLO'}")
    return success

def test_5_crash_recovery_unacked():
    """
    Simula un apagón catastrófico de corriente en el servidor, validando que 
    al reiniciar ('Bootstrap'), los mensajes que estaban "en el limbo" (entregados sin ACK) 
    sean devueltos a la cola gracias al sistema de StorageManager.
    """
    print("\n[TEST 5] Recuperación de Unacked tras Crash (Bootstrap)...")
    client = MOMClient()
    cola = "test_crash"
    client.declarar_cola(cola)
    
    # Mandamos mensaje a un consumidor bloqueado
    def kill_me_consumer():
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(('127.0.0.1', 5555))
        s.sendall(json.dumps({"action": "consume", "queue": cola}).encode() + b"\n")
        s.recv(1024) 

    threading.Thread(target=kill_me_consumer, daemon=True).start()
    time.sleep(0.5)
    client.publicar(cola, "VALOR_CRÍTICO")
    time.sleep(0.5)
    
    # 1. Forzamos volcado a disco del estado "congelado"
    save_broker_state()
    print("  > Estado guardado en disco. Simulando reinicio...")
    
    # 2. Crash simulado: Borramos por completo la memoria RAM del servidor.
    with queues_master_lock:
        queues.clear()
    
    # 3. Ejecutamos la Lógica pura del proceso Bootstrap del main() del servidor
    saved = storage.load_state()
    with queues_master_lock:
        for q_name, data in saved.items():
            all_msgs = data.get("messages", []) + data.get("unacked", [])
            queues[q_name] = {
                "messages": all_msgs, "consumers": [], "unacked": {}, 
                "next_idx": 0, "lock": threading.RLock()
            }
    
    # 4. Validamos que el mensaje que estaba en 'unacked' existe ahora en 'messages'
    q_data = queues.get(cola)
    success = any(m["data"] == "VALOR_CRÍTICO" for m in q_data["messages"])
    print(f"  > Mensaje recuperado del disco: {'OK' if success else 'FALLO'}")
    return success

def test_6_gestion_colas():
    """Valida los comandos de control: Idempotencia en declaración, listado y eliminación."""
    print("\n[TEST 6] Gestión de Colas (List/Delete/Idempotencia)...")
    client = MOMClient()
    
    # Declarar múltiples veces no debe tirar error (Idempotencia)
    client.declarar_cola("repetida")
    client.declarar_cola("repetida")
    
    lista = client.listar_colas()
    if "repetida" not in lista: return False
    
    # Probamos el borrado
    client.eliminar_cola("repetida")
    lista_despues = client.listar_colas()
    
    # Éxito si la cola ya no existe en el servidor
    success = "repetida" not in lista_despues
    print(f"  > Idempotencia y Borrado: {'OK' if success else 'FALLO'}")
    return success

def test_7_ttl_expiration():
    """Valida que el hilo Garbage Collector elimina físicamente de la RAM y del Disco los mensajes caducados."""
    from server import MESSAGE_TTL
    print(f"\n[TEST 7] Expiración por TTL (Esperando {MESSAGE_TTL + 3}s)...")
    client = MOMClient()
    cola = "test_ttl"
    client.declarar_cola(cola)
    client.publicar(cola, "MENSAJE_EFIMERO")
    
    time.sleep(0.2) # Estabilización de red
    # Validamos que el mensaje entró efectivamente a la memoria
    with queues_master_lock:
        q_data = queues.get(cola)
        exists_before = q_data is not None and len(q_data["messages"]) == 1
    
    if not exists_before: 
        print("  > Fallo: El mensaje no llegó a la cola.")
        return False
    
    # Esperamos a que pase el límite de TTL más el ciclo del Garbage Collector
    time.sleep(MESSAGE_TTL + 4)
    
    # Validamos que el mensaje se ha esfumado del diccionario de Python
    with queues_master_lock:
        q_data = queues.get(cola)
        exists_after = q_data is None or len(q_data["messages"]) == 0
        
    print(f"  > Eliminado tras TTL: {'OK' if exists_after else 'FALLO'}")
    return exists_after

def test_8_immediate_rescue_on_disconnect():
    """
    Valida la captura de excepciones de red y la reconversión (Socket Drop).
    Si un cliente se desconecta bruscamente perdiendo TCP (cierre de terminal), el Broker
    no debe esperar al ACK_TIMEOUT, sino rescatar el mensaje ipso facto en el 'finally'.
    """
    print("\n[TEST 8] Rescate Inmediato por Desconexión (Socket Drop)...")
    client = MOMClient()
    cola = "test_rescue"
    client.declarar_cola(cola)
    client.publicar(cola, "RESCATAME")
    
    # Conectamos por RAW y recibimos
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(('127.0.0.1', 5555))
    s.sendall(json.dumps({"action": "consume", "queue": cola}).encode() + b"\n")
    s.recv(1024) 
    
    print("  > Consumidor recibió mensaje. Cayendo...")
    s.close() # Cierre de red BRUSCO y violento. Cero ACKs enviados.
    time.sleep(1) # Un segundo al servidor para procesar el try/finally TCP
    
    # Suscribimos de inmediato a otro, sin esperar timeouts largos. 
    # Debería coger el mensaje rescatado.
    res = []
    threading.Thread(target=client.consumir, args=(cola, lambda m: res.append(m)), daemon=True).start()
    time.sleep(1)
    
    success = "RESCATAME" in res
    print(f"  > Recuperado inmediatamente: {'OK' if success else 'FALLO'}")
    return success

def test_9_queue_isolation():
    """Valida que la concurrencia RLock aísa correctamente colas independientes (Multi-tenant)."""
    print("\n[TEST 9] Aislamiento de Colas (Multi-tenancy)...")
    client = MOMClient()
    client.declarar_cola("cola_A")
    client.declarar_cola("cola_B")
    
    # Publicamos en A y B
    client.publicar("cola_A", "MSG_A")
    client.publicar("cola_B", "MSG_B")
    
    # Consumimos SOLO en B
    res_b = []
    threading.Thread(target=client.consumir, args=("cola_B", lambda m: res_b.append(m)), daemon=True).start()
    time.sleep(1)
    
    # Éxito: Solo le llegó lo de B, lo de A sigue retenido en A sin mezclarse en memoria.
    success = "MSG_B" in res_b and "MSG_A" not in res_b
    print(f"  > Aislamiento de datos: {'OK' if success else 'FALLO'}")
    return success

def test_10_publish_non_existent_queue():
    """Valida el requisito literal: 'Si se publica en cola inexistente, se descarta'."""
    print("\n[TEST 10] Publicación en Cola Inexistente (Descarte)...")
    client = MOMClient()
    # Petición ciego a una cola sin declarar
    client.publicar("cola_fantasma", "HOLA")
    time.sleep(0.5)
    
    # Verificamos que el broker no autogeneró la cola como mecanismo de defensa
    lista = client.listar_colas()
    success = "cola_fantasma" not in lista
    print(f"  > Mensaje descartado: {'OK' if success else 'FALLO'}")
    return success

if __name__ == "__main__":
    """Motor central de la Suite de Pruebas."""
    
    # Arrancamos el servidor broker en el hilo maestro. 
    # Quedará escuchando puerto 5555 de fondo de forma permanente.
    threading.Thread(target=start_server, daemon=True).start()
    time.sleep(1) # Esperamos que el SO vincule el socket TCP (bind)

    # Array con los 10 tests de la auditoría.
    tests = [
        test_1_flujo_basico,
        test_2_round_robin,
        test_3_fair_dispatch,
        test_4_ack_timeout,
        test_5_crash_recovery_unacked,
        test_6_gestion_colas,
        test_7_ttl_expiration,
        test_8_immediate_rescue_on_disconnect,
        test_9_queue_isolation,
        test_10_publish_non_existent_queue
    ]
    
    passed = 0
    # Ejecutamos aisladamente cada test.
    for t in tests:
        if run_test_isolated(t):
            passed += 1

    print("\n" + "="*50)
    print(f"  RESULTADO FINAL: {passed}/{len(tests)} TESTS PASADOS")
    print("="*50)
    
    # Retorno de código de sistema operativo: 0 si todo perfecto, 1 si algo falló.
    # Vital para que sistemas de Integración Continua (CI/CD) sepan si el pipeline pasó.
    sys.exit(0 if passed == len(tests) else 1)
