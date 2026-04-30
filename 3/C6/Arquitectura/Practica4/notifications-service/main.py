"""
Microservicio de Notificaciones (Notifications Service).

Microservicio asíncrono puro que actúa como un trabajador (Worker) dentro de 
la arquitectura orientada a eventos. Su función es consumir mensajes del 
Broker MOM (implementado en la P3) y procesar las confirmaciones de pedido.

Responsabilidades:
    - Suscripción continua a colas de mensajería asíncrona.
    - Procesamiento de eventos de negocio tras la consolidación de pedidos.
    - Simulación de envío de notificaciones (Mocking) vía logs.
    - Gestión de la tolerancia a fallos en la conexión con el Middleware.
"""

import os
import time
import logging
from mom_client import MOMClient

# Configuración de logging profesional optimizado para visualización en kubectl logs
logging.basicConfig(
    level=logging.INFO, 
    format='%(asctime)s [%(levelname)s] Notificaciones: %(message)s'
)
logger = logging.getLogger(__name__)

# Configuración del entorno inyectada por Kubernetes
BROKER_HOST = os.getenv("BROKER_HOST", "broker-service")
COLA_NOTIFICACIONES = "notificaciones_pedidos"

def procesar_evento_pedido(mensaje):
    """Callback de procesamiento de eventos."""
    logger.info(f">>> EVENTO RECIBIDO: Iniciando notificación para Pedido #{mensaje.get('order_id')}")
    logger.info(f"    - Cliente: {mensaje.get('customer')}")
    logger.info(f"    - Detalle: {mensaje.get('message')}")

    # Simulamos que el sistema tarda un segundo en enviar el "email" real
    time.sleep(1) 

    logger.info(">>> Estado: Notificación emitida exitosamente mediante simulador.")
    print("-" * 60)

def main():
    """Punto de entrada del Microservicio."""
    logger.info(f"[*] Lanzando Servicio de Notificaciones (Target Broker: {BROKER_HOST})")
    
    # Creamos la instancia del cliente MOM (Práctica 3)
    broker = MOMClient(host=BROKER_HOST)
    
    intentos = 0
    max_intentos = 20 
    
    # Bucle de reintento para aguantar mientras el clúster arranca
    while intentos < max_intentos:
        try:
            logger.info(f"Protocolo: Solicitando suscripción en cola '{COLA_NOTIFICACIONES}'...")
            
            # 1. Aseguramos que la cola existe en el servidor
            broker.declarar_cola(COLA_NOTIFICACIONES)
            
            # 2. Entramos en modo escucha (bloqueante).
            # Por cada mensaje que llegue, el cliente llamará a 'procesar_evento_pedido'
            # y automáticamente enviará el ACK al broker tras terminar.
            broker.consumir(COLA_NOTIFICACIONES, callback=procesar_evento_pedido)
            
            # Si por algún motivo salimos del bucle de consumo sin error
            break
            
        except Exception as e:
            # Si el broker no está listo o hay error de red, esperamos 5s y reintentamos
            intentos += 1
            logger.warning(f"Falla de Red: El broker no responde o ha caído ({intentos}/{max_intentos}). Reintento en 5s...")
            time.sleep(5)

if __name__ == "__main__":
    # Importante para que el contenedor no muera si falla la conexión inicial
    main()
