"""
Microservicio de Notificaciones (Notifications Service).

Microservicio asíncrono puro que actúa como un trabajador (Worker) dentro de 
la arquitectura orientada a eventos. Su función es consumir mensajes del 
Broker MOM y procesar las confirmaciones de pedido en segundo plano.

Responsabilidades:
    - Suscripción continua a colas de mensajería asíncrona.
    - Procesamiento de eventos de negocio (Confirmación de ventas).
    - Simulación de envío de notificaciones (Mocking) vía logs de sistema.
    - Gestión de la tolerancia a fallos y reintentos en la conexión con el Middleware.
"""

import os # Acceso a configuración del entorno
import time # Manejo de retardos y simulaciones
import logging # Sistema de registro de actividad
from client import MOMClient # Cliente unificado para comunicación con el Broker (P3)

# Configuración de logging profesional optimizado para la monitorización vía 'kubectl logs'
logging.basicConfig(
    level=logging.INFO, 
    format='%(asctime)s [%(levelname)s] Notificaciones: %(message)s'
)
logger = logging.getLogger(__name__)

# Configuración de red inyectada por el orquestador Kubernetes
BROKER_HOST = os.getenv("BROKER_HOST", "broker-service") # Host DNS interno del broker
COLA_NOTIFICACIONES = "notificaciones_pedidos" # Nombre de la cola de trabajo

def procesar_evento_pedido(mensaje):
    """
    Callback de procesamiento de eventos (Lógica de Negocio).
    
    Esta función es invocada automáticamente por el cliente MOM cada vez que
    llega un mensaje desde la cola de suscripción.
    """
    logger.info(f">>> EVENTO RECIBIDO: Iniciando notificación para Pedido #{mensaje.get('order_id')}")
    logger.info(f"    - Cliente: {mensaje.get('customer')}")
    logger.info(f"    - Detalle: {mensaje.get('message')}")

    # SIMULACIÓN DE TRABAJO PESADO:
    # Simulamos que el sistema tarda un segundo en procesar el envío de un email real.
    # Gracias al modelo del Broker (Prefetch=1), otros trabajadores podrían procesar 
    # otros emails en paralelo mientras este duerme.
    time.sleep(1) 

    logger.info(">>> Estado: Notificación emitida exitosamente mediante simulador.")
    # Línea visual para separar eventos en los archivos de log
    print("-" * 60)

def main():
    """
    Punto de entrada principal del Worker de Notificaciones.
    
    Implementa un patrón de arranque resiliente para soportar entornos donde 
    el Middleware puede tardar en estar disponible (Startup Latency).
    """
    logger.info(f"[*] Lanzando Servicio de Notificaciones (Target Broker: {BROKER_HOST})")
    
    # Instanciamos el cliente MOM utilizando la dirección inyectada por el clúster
    broker = MOMClient(host=BROKER_HOST)
    
    intentos = 0 # Contador de intentos de conexión
    max_intentos = 20 # Límite de reintentos antes de desistir
    
    # BUCLE DE RESILIENCIA:
    # Como los contenedores arrancan en paralelo, este servicio podría intentar conectar
    # antes de que el Broker esté listo. Este bucle evita que el microservicio muera al inicio.
    while intentos < max_intentos:
        try:
            logger.info(f"Protocolo: Solicitando suscripción en cola '{COLA_NOTIFICACIONES}'...")
            
            # 1. Aseguramos la existencia de la infraestructura en el Broker (Idempotente)
            broker.declarar_cola(COLA_NOTIFICACIONES)
            
            # 2. ACTIVACIÓN DEL MODO ESCUCHA (BLOQUEANTE):
            # El programa se queda "colgado" aquí de forma eficiente esperando mensajes.
            # Por cada mensaje recibido, el cliente invocará a 'procesar_evento_pedido'
            # y enviará automáticamente el ACK al broker al finalizar la función.
            broker.consumir(COLA_NOTIFICACIONES, callback=procesar_evento_pedido)
            
            # Si la conexión se cierra ordenadamente por el servidor, salimos del bucle.
            break
            
        except Exception as e:
            # En caso de fallo de red o broker inalcanzable, aplicamos una pausa de espera
            intentos += 1
            logger.warning(f"Falla de Red: El broker no responde ({intentos}/{max_intentos}). Reintento en 5s...")
            time.sleep(5) # Espera activa antes del siguiente reintento de conexión

if __name__ == "__main__":
    # Arrancamos la lógica principal del servicio
    main()
