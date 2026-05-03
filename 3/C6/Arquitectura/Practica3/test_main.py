"""
Suite de Pruebas de Cobertura Total (Integración y Stress).

Esta suite valida el 100% de los requisitos del Broker, incluyendo:
- Flujo básico y Sockets.
- Persistencia en disco (Bootstrap).
- Recuperación de mensajes Unacked tras un Crash.
- Distribución Round-Robin.
- Política Fair Dispatch (Prefetch=1).
- Rescate por ACK Timeout (Starvation).
- Gestión de colas (Listado/Borrado).
"""

import threading
import time
import sys
import os
import json
import uuid
import socket

# Ajuste de PATH
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from server import main as start_server, queues, queues_master_lock, save_broker_state, storage, ACK_TIMEOUT
from client import MOMClient

def test_1_flujo_basico():
    print("\n[TEST 1] Publicación y Consumo Básico...")
    client = MOMClient()
    cola = "test_basico"
    client.declarar_cola(cola)
    
    recibidos = []
    threading.Thread(target=client.consumir, args=(cola, lambda m: recibidos.append(m)), daemon=True).start()
    
    time.sleep(0.5)
    for i in range(5):
        client.publicar(cola, f"M_{i}")
    
    time.sleep(1)
    client.close()
    success = len(recibidos) == 5
    print(f"  > Resultado: {'OK' if success else 'FALLO'}")
    return success

def test_2_round_robin():
    print("\n[TEST 2] Distribución Round-Robin...")
    client = MOMClient()
    cola = "test_rr"
    client.declarar_cola(cola)
    
    res1, res2 = [], []
    threading.Thread(target=MOMClient().consumir, args=(cola, lambda m: res1.append(m)), daemon=True).start()
    threading.Thread(target=MOMClient().consumir, args=(cola, lambda m: res2.append(m)), daemon=True).start()
    
    time.sleep(0.5)
    for i in range(4):
        client.publicar(cola, f"RR_{i}")
    
    time.sleep(1)
    # Deberían recibir 2 cada uno (2+2=4)
    success = len(res1) == 2 and len(res2) == 2
    print(f"  > C1: {len(res1)}, C2: {len(res2)} | Resultado: {'OK' if success else 'FALLO'}")
    return success

def test_3_fair_dispatch():
    print("\n[TEST 3] Fair Dispatch (Prefetch=1)...")
    client = MOMClient()
    cola = "test_fair"
    client.declarar_cola(cola)
    
    # Consumidor 1: Muy lento y NO envía ACK (simulado con socket raw)
    def slow_consumer():
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(('127.0.0.1', 5555))
        s.sendall(json.dumps({"action": "consume", "queue": cola}).encode() + b"\n")
        s.recv(1024) # Recibe el primero y se queda "bloqueado" sin hacer ACK
        # No cerramos para que el broker crea que sigue ahí procesando
        time.sleep(10)

    threading.Thread(target=slow_consumer, daemon=True).start()
    time.sleep(0.5)
    
    # Consumidor 2: Normal (rápido con ACK)
    recibidos_c2 = []
    threading.Thread(target=MOMClient().consumir, args=(cola, lambda m: recibidos_c2.append(m)), daemon=True).start()
    
    time.sleep(0.5)
    # Enviamos 5 mensajes. 
    # El 1º va a C1 (y ahí se queda bloqueado). 
    # Los otros 4 DEBEN ir a C2 porque C1 está ocupado (Fair Dispatch).
    for i in range(5):
        client.publicar(cola, f"F_{i}")
    
    time.sleep(1)
    success = len(recibidos_c2) == 4
    print(f"  > C1 tiene 1 (bloqueado), C2 tiene {len(recibidos_c2)} | Resultado: {'OK' if success else 'FALLO'}")
    return success

def test_4_ack_timeout():
    # Nota: Este test depende de ACK_TIMEOUT en server.py. 
    # Si es 60s, el test tardará 60s. Si lo bajaste a 5s, será rápido.
    print(f"\n[TEST 4] ACK Timeout (Starvation) - Esperando {ACK_TIMEOUT+1}s...")
    client = MOMClient()
    cola = "test_timeout"
    client.declarar_cola(cola)
    
    # Simulamos consumidor que recibe pero nunca hace ACK y luego muere
    def zombie_consumer():
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(('127.0.0.1', 5555))
        s.sendall(json.dumps({"action": "consume", "queue": cola}).encode() + b"\n")
        s.recv(1024) 
        # Se queda zombie...

    threading.Thread(target=zombie_consumer, daemon=True).start()
    time.sleep(0.5)
    client.publicar(cola, "MENSAJE_ZOMBIE")
    
    # Esperamos a que pase el timeout del servidor
    time.sleep(ACK_TIMEOUT + 2)
    
    # Ahora un consumidor nuevo debería poder rescatarlo
    res = []
    threading.Thread(target=client.consumir, args=(cola, lambda m: res.append(m)), daemon=True).start()
    time.sleep(1)
    
    success = "MENSAJE_ZOMBIE" in res
    print(f"  > Recuperado tras timeout: {'OK' if success else 'FALLO'}")
    return success

def test_5_crash_recovery_unacked():
    print("\n[TEST 5] Recuperación de Unacked tras Crash (Bootstrap)...")
    client = MOMClient()
    cola = "test_crash"
    client.declarar_cola(cola)
    
    # 1. Mensaje que se queda en 'unacked' (enviado pero no confirmado)
    def kill_me_consumer():
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(('127.0.0.1', 5555))
        s.sendall(json.dumps({"action": "consume", "queue": cola}).encode() + b"\n")
        s.recv(1024) 
        # No cerramos ordenadamente para que el mensaje se quede en unacked

    threading.Thread(target=kill_me_consumer, daemon=True).start()
    time.sleep(0.5)
    client.publicar(cola, "VALOR_CRÍTICO")
    time.sleep(0.5)
    
    # 2. Forzamos guardado y simulamos el CRASH borrando la memoria
    save_broker_state()
    print("  > Estado guardado en disco (incluyendo unacked). Simulando reinicio...")
    
    with queues_master_lock:
        queues.clear() # Borramos memoria
    
    # 3. Ejecutamos lógica de Bootstrap (la misma de server.py main)
    saved = storage.load_state()
    with queues_master_lock:
        for q_name, data in saved.items():
            all_msgs = data.get("messages", []) + data.get("unacked", [])
            queues[q_name] = {
                "messages": all_msgs, "consumers": [], "unacked": {}, 
                "next_idx": 0, "lock": threading.RLock()
            }
    
    # 4. Comprobamos si el mensaje que estaba en unacked ha vuelto a la cola
    q_data = queues.get(cola)
    success = any(m["data"] == "VALOR_CRÍTICO" for m in q_data["messages"])
    print(f"  > Mensaje recuperado del disco tras reinicio: {'OK' if success else 'FALLO'}")
    return success

def test_6_gestion_colas():
    print("\n[TEST 6] Gestión de Colas (List/Delete/Idempotencia)...")
    client = MOMClient()
    
    # Idempotencia
    client.declarar_cola("repetida")
    client.declarar_cola("repetida")
    
    # Listado
    lista = client.listar_colas()
    if "repetida" not in lista: return False
    
    # Borrado
    client.eliminar_cola("repetida")
    lista_despues = client.listar_colas()
    
    success = "repetida" not in lista_despues
    print(f"  > Idempotencia y Borrado: {'OK' if success else 'FALLO'}")
    return success

if __name__ == "__main__":
    # Limpiamos storage previo
    if os.path.exists("broker_storage.json"): os.remove("broker_storage.json")
    
    # Arrancamos servidor
    threading.Thread(target=start_server, daemon=True).start()
    time.sleep(1)

    # Suite completa
    tests = [
        test_1_flujo_basico,
        test_2_round_robin,
        test_3_fair_dispatch,
        test_4_ack_timeout,
        test_5_crash_recovery_unacked,
        test_6_gestion_colas
    ]
    
    passed = 0
    for t in tests:
        try:
            if t(): passed += 1
        except Exception as e:
            print(f"  > EXCEPCIÓN en test: {e}")

    print("\n" + "="*50)
    print(f"  RESULTADO FINAL: {passed}/{len(tests)} TESTS PASADOS")
    print("="*50)
    
    if passed == len(tests):
        sys.exit(0)
    else:
        sys.exit(1)
