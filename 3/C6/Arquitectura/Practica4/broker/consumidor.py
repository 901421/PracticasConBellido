"""
Aplicación Consumidora de Ejemplo (Script principal).

Implementa un nodo trabajador o 'worker' que se suscribe a una cola específica 
dentro del Broker. Queda en modo de escucha pasiva, siendo despertado únicamente 
cuando el Broker realiza un 'Push' con un mensaje disponible.
"""

import sys
from client import MOMClient

def procesar_mensaje(mensaje):
    """
    Función Callback (manejador) invocada internamente por la librería de cliente.
    
    Esta función encapsula la lógica de negocio real de la aplicación.
    Ejemplos en el mundo real: Inserciones en Base de Datos, envío de emails, 
    procesamiento de imágenes o cálculos complejos.
    
    IMPORTANTE: Si esta función finaliza correctamente, el cliente (MOMClient) 
    enviará un ACK (reconocimiento) al servidor. Si lanza una excepción, el 
    ACK se omite, reteniendo el mensaje en el Broker.
    
    Args:
        mensaje (dict/string): El contenido del mensaje deserializado.
    """
    print(f"[x] Nodo Consumidor ejecutando tarea -> {mensaje}")

def main():
    """
    Configura los parámetros de suscripción e invoca el ciclo de vida del consumidor.
    """
    # Soporte para IP dinámica vía argumentos del sistema
    broker_ip = sys.argv[1] if len(sys.argv) > 1 else "127.0.0.1"
    
    # Inicialización del cliente con la configuración de red
    broker = MOMClient(host=broker_ip)
    
    # Identificador de la cola a la que estamos vinculando (Binding) el trabajador
    cola_nombre = "mi_cola_pruebas"
    
    print(f"[*] Consumidor arrancando... Conectando al nodo Broker en {broker_ip}...")
    
    # Declaración idempotente (Si ya existe no hace nada, si no, la crea en el servidor)
    broker.declarar_cola(cola_nombre)
    
    print(f"[*] Suscripción exitosa a la cola '{cola_nombre}'.")
    print(f"[*] El hilo actual entra en bloqueo esperando push de datos (Presiona CTRL+C para forzar salida)")
    
    try:
        # Llamada bloqueante. Toma control del hilo y delega los eventos al 'callback' asignado
        broker.consumir(cola_nombre, callback=procesar_mensaje)
    except KeyboardInterrupt:
        # Salida controlada desde el terminal
        print("\n[*] Nodo consumidor apagado y desconectado del broker.")
        sys.exit(0)

if __name__ == "__main__":
    main()
