"""
Aplicación Consumidora de Ejemplo.

Implementa un worker que procesa mensajes mediante el sistema de push 
y confirma la recepción mediante ACKs automáticos.
"""

import sys
from client import MOMClient

def procesar_mensaje(mensaje):
    """
    Manejador de lógica de negocio.
    Si esta función termina sin errores, el MOMClient envía el ACK al Broker.
    """
    print(f"[x] Procesando tarea -> {mensaje}")

def main():
    """
    Suscribe al consumidor a una cola y entra en bucle de escucha.
    """
    broker_ip = sys.argv[1] if len(sys.argv) > 1 else "127.0.0.1"
    broker = MOMClient(host=broker_ip)
    cola_nombre = "mi_cola_pruebas"
    
    print(f"[*] Conectando al Broker en {broker_ip}...")
    broker.declarar_cola(cola_nombre)
    
    print(f"[*] Esperando mensajes en '{cola_nombre}'...")
    
    try:
        # Llamada bloqueante con callback
        broker.consumir(cola_nombre, callback=procesar_mensaje)
    except KeyboardInterrupt:
        print("\n[*] Consumidor finalizado.")
        sys.exit(0)

if __name__ == "__main__":
    main()
