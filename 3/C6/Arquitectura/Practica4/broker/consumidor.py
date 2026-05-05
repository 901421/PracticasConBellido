"""
Aplicación Consumidora de Ejemplo.

Implementa un worker (trabajador) que procesa mensajes de manera asíncrona
mediante un modelo 'Push'.
Confirma la recepción al servidor automáticamente mediante ACKs.
"""

import sys # Usado para capturar la IP por argumentos y salir
from client import MOMClient # Importamos la fachada de nuestra librería cliente

def procesar_mensaje(mensaje):
    """
    Función Callback (Manejador de Lógica de Negocio).
    Esta función es invocada automáticamente por la librería MOMClient cada vez 
    que un mensaje nuevo llega por la red.
    
    Args:
        mensaje (dict): El diccionario exacto que envió el productor.
    """
    # En un entorno real, aquí iría la escritura a base de datos, el envío de email, el renderizado de vídeo, etc.
    print(f"[x] Tarea recibida y completada -> {mensaje}")
    
    # IMPORTANTE: Si esta función llega al final (return implícito) sin lanzar excepciones, 
    # la librería cliente asume "Éxito" e inyecta un paquete ACK (Reconocimiento) hacia el broker
    # para que borre el mensaje.

def main():
    """
    Punto de entrada principal para el consumidor.
    Se suscribe a una cola específica y delega el control del hilo principal a la librería.
    """
    # Capturamos la IP y Puerto del broker desde la línea de comandos, si existen.
    # Uso: python consumidor.py <ip> [puerto]
    broker_ip = sys.argv[1] if len(sys.argv) > 1 else "127.0.0.1"
    broker_port = int(sys.argv[2]) if len(sys.argv) > 2 else 5555
    
    # Instanciamos el cliente con los parámetros dinámicos
    broker = MOMClient(host=broker_ip, port=broker_port)
    cola_nombre = "mi_cola_pruebas"
    
    print(f"[*] Conectando al Broker en {broker_ip}:{broker_port}...")
    
    # Al igual que el productor, el consumidor intenta declarar la cola para asegurar que existe
    # antes de intentar suscribirse a ella.
    broker.declarar_cola(cola_nombre)
    
    print(f"[*] Esperando mensajes en la cola '{cola_nombre}'... (Pulsa CTRL+C para salir)")
    
    try:
        # --- SUSCRIPCIÓN BLOQUEANTE ---
        # La llamada broker.consumir() inicia un bucle infinito de red interno.
        # El hilo se quedará en esta línea para siempre esperando paquetes TCP.
        # Pasamos por referencia la función 'procesar_mensaje' (Inyección de Dependencias).
        broker.consumir(cola_nombre, callback=procesar_mensaje)
    except KeyboardInterrupt:
        # Salida controlada al pulsar Control+C
        print("\n[*] Desconectando Consumidor de forma segura.")
        sys.exit(0)

if __name__ == "__main__":
    main()
