"""
Suite de Pruebas Automatizadas (Tests de Integración y Regresión).

Módulo crítico para validar continuamente la salud y correcto cumplimiento 
de los requisitos no funcionales (Arquitectura) solicitados en la Práctica 3.
Incluye tests End-to-End que involucran redes reales mediante threads.
"""

import threading
import time
import sys
import os
import json

# Ajuste temporal del PATH para permitir importaciones directas de los módulos
# aunque el script se ejecute desde otro directorio raíz distinto.
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from server import main as start_server
from client import MOMClient

def test_workflow_persistencia():
    """
    Prueba Funcional 1: Valida el flujo normal TCP y el consumo cruzado.
    Garantiza que:
    a) Una conexión puede publicar varias veces.
    b) Un consumidor puede suscribirse y recibir en un hilo separado.
    """
    print("\n--- TEST 1: FLUJO TCP Y CONSUMO ---")
    client = MOMClient()
    cola = "test_tcp"
    client.declarar_cola(cola)
    
    recibidos = []
    # Arranca un suscriptor en un hilo daemonizado para no bloquear el test principal
    threading.Thread(target=client.consumir, args=(cola, lambda m: recibidos.append(m)), daemon=True).start()
    
    # Breve pausa para dar tiempo a que el socket del consumidor asiente en el broker
    time.sleep(0.5)

    # Inyecta mensajes secuencialmente
    for i in range(3):
        client.publicar(cola, f"MSG_{i}")
    
    # Da tiempo de red para que los mensajes sean enrutados y descargados
    time.sleep(0.5)
    client.close()
    
    # Verifica que el callback procesó todos los mensajes inyectados
    return len(recibidos) == 3

def test_resiliencia_ack():
    """
    Prueba de Resiliencia 2: Valida el mecanismo Fair Dispatch y Recuperación (Rescue).
    Simula una caída de red severa (Crash Crash-stop) en el cliente antes de que este
    pueda enviar el reconocimiento (ACK).
    Asegura que el broker devuelve el mensaje a la cola.
    """
    print("\n--- TEST 2: RESILIENCIA, ACK Y RECUPERACIÓN ---")
    client = MOMClient()
    cola = "test_res"
    client.declarar_cola(cola)

    import socket
    def fail_consumer():
        """Worker simulado maligno que corta la conexión de golpe TCP."""
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect(('127.0.0.1', 5555))
        # Solicita la suscripción raw sin usar nuestra librería client.py segura
        s.sendall(json.dumps({"action": "consume", "queue": cola}).encode() + b"\n")
        # Lee la primera trama y aborta bruscamente la conexión (Simula proceso muerto)
        s.recv(1024) 
        s.close()    

    # Lanza el consumidor que va a fallar
    threading.Thread(target=fail_consumer).start()
    time.sleep(0.5)
    
    # Publica el mensaje de prueba. Se asignará al consumidor maligno.
    client.publicar(cola, "RESCUE_ME")
    # Pausa intencional para permitir que el broker reciba el EOF o TCP FIN y ejecute rescate
    time.sleep(1) 

    # Ahora un consumidor legítimo se conecta a la misma cola esperando el rescate
    recuperado = []
    threading.Thread(target=client.consumir, args=(cola, lambda m: recuperado.append(m)), daemon=True).start()
    time.sleep(1)
    
    # Si el mensaje rescatado está en su poder, el rescate automático funcionó
    return "RESCUE_ME" in recuperado

def test_persistencia_disco():
    """
    Prueba de Durabilidad 3: Verifica la serialización JSON del estado en el archivo físico.
    """
    print("\n--- TEST 3: DURABILIDAD FISICA EN DISCO ---")
    file_name = "broker_storage.json"
    
    # Limpia estado pre-existente para tener un entorno aislado
    if os.path.exists(file_name): os.remove(file_name)
    
    client = MOMClient()
    # Una declaración debería forzar un save_broker_state()
    client.declarar_cola("disco_cola")
    time.sleep(0.5)
    
    # Verifica que el archivo de base de datos se ha creado realmente en disco
    return os.path.exists(file_name)

if __name__ == "__main__":
    # Arranca el servidor Broker en el mismo proceso (background) para probarlo todo
    threading.Thread(target=start_server, daemon=True).start()
    
    # Espera inicialización completa del socket server.bind()
    time.sleep(1)

    # Batería de pruebas (Suite)
    results = [
        test_workflow_persistencia(),
        test_resiliencia_ack(),
        test_persistencia_disco()
    ]

    # Evaluación y Reporte Final
    if all(results):
        print("\n" + "="*50)
        print("  TODAS LAS PRUEBAS (3/3) SUPERADAS CON ÉXITO")
        print("="*50)
        sys.exit(0) # Código de salida 0: Pipeline de CI/CD Verde
    else:
        print("\n[!] Error crítico. Una o más pruebas funcionales fallaron.")
        sys.exit(1) # Código de salida 1: Detiene el pipeline si esto fuera Integración Continua
