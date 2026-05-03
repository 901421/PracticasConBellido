"""
Aplicación Productora de Ejemplo.

Simula un sistema que genera eventos y los inyecta en el Broker 
de Mensajes para su procesamiento asíncrono.
"""

import time
import sys
from client import MOMClient

def main():
    """
    Punto de entrada para el productor.
    Obtiene la IP del broker por argumentos e inyecta ráfagas de mensajes.
    """
    broker_ip = sys.argv[1] if len(sys.argv) > 1 else "127.0.0.1"
    broker = MOMClient(host=broker_ip)
    cola_nombre = "mi_cola_pruebas"
    
    print(f"[*] Conectando al Broker en {broker_ip}...")
    
    # Declaración idempotente de la cola
    broker.declarar_cola(cola_nombre)
    
    contador = 1
    try:
        while True:
            mensaje = {
                "id_mensaje": contador, 
                "texto": f"Evento de telemetría #{contador}",
                "timestamp": time.time()
            }
            
            print(f"[*] Publicando -> {mensaje}")
            broker.publicar(cola_nombre, mensaje)
            
            contador += 1
            time.sleep(2) # Simulación de ráfaga espaciada
            
    except KeyboardInterrupt:
        print("\n[*] Productor finalizado.")
        broker.close()
        sys.exit(0)

if __name__ == "__main__":
    main()
