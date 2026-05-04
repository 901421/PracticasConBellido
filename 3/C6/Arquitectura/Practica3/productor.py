"""
Aplicación Productora de Ejemplo.

Simula un sistema o microservicio que genera eventos (ej. telemetría) 
y los inyecta en el Broker de Mensajes para su procesamiento asíncrono.
"""

import time # Usado para generar pausas entre el envío de mensajes (ráfagas)
import sys # Para capturar argumentos de la línea de comandos (IP del broker) y salir limpiamente
from client import MOMClient # Importamos la fachada de nuestra librería cliente

def main():
    """
    Punto de entrada principal para la aplicación productora.
    Se conecta al broker, asegura que la cola existe y entra en un bucle infinito
    de generación y publicación de eventos.
    """
    # Capturamos la IP del broker pasada como primer argumento (ej: python productor.py 192.168.1.10). 
    # Si no se pasa, asume que el broker está en la misma máquina (localhost).
    broker_ip = sys.argv[1] if len(sys.argv) > 1 else "127.0.0.1"
    
    # Instanciamos el cliente conectándolo a la IP resuelta
    broker = MOMClient(host=broker_ip)
    # Definimos la llave (nombre) de la cola donde inyectaremos los datos
    cola_nombre = "mi_cola_pruebas"
    
    print(f"[*] Conectando al Broker en {broker_ip}...")
    
    # --- DECLARACIÓN IDEMPOTENTE ---
    # Es una buena práctica que los productores declaren las colas antes de usarlas.
    # Como la operación es idempotente, si el consumidor la creó primero, esto no causará error.
    broker.declarar_cola(cola_nombre)
    
    contador = 1 # Inicializamos un ID secuencial para simular métricas
    try:
        # Bucle principal de ejecución
        while True:
            # Construimos el payload de datos del mensaje (puede ser cualquier diccionario anidado)
            mensaje = {
                "id_mensaje": contador, 
                "texto": f"Evento de telemetría #{contador}",
                "timestamp": time.time()
            }
            
            print(f"[*] Publicando -> {mensaje}")
            
            # --- PUBLICACIÓN ASÍNCRONA ---
            # El productor no se bloquea esperando a que nadie lea el mensaje.
            # Simplemente lo suelta en la red y continúa su trabajo.
            broker.publicar(cola_nombre, mensaje)
            
            contador += 1 # Preparamos el siguiente evento
            time.sleep(2) # Pausamos 2 segundos para simular una "ráfaga espaciada" y no saturar la CPU
            
    except KeyboardInterrupt:
        # Captura limpia de Control+C en la terminal
        print("\n[*] Productor finalizado por el usuario.")
        broker.close() # Cerramos el socket subyacente de forma segura
        sys.exit(0) # Salida limpia del sistema operativo

if __name__ == "__main__":
    main()
