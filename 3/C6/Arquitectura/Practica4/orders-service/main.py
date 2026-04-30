"""
Microservicio de Pedidos (Orders Service).

Actúa como el orquestador principal del proceso de compra. Este servicio integra
comunicación síncrona HTTP para la validación de inventario y comunicación
asíncrona mediante un Broker MOM (del Sistema de Mensajería de la P3) para 
notificaciones de post-venta.

Responsabilidades:
    - Gestión del ciclo de vida de los pedidos.
    - Orquestación de llamadas síncronas a Inventario.
    - Persistencia de transacciones en base de datos MariaDB.
    - Emisión de eventos asíncronos para el sistema de notificaciones.
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import os
import mysql.connector
import requests
import logging
from mom_client import MOMClient

# Configuración de logging profesional para trazabilidad de pedidos
logging.basicConfig(
    level=logging.INFO, 
    format='%(asctime)s [%(levelname)s] Pedidos: %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Orders Service - P4 Arquitectura Software",
    description="Orquestador de compras con persistencia en MariaDB e integración MOM.",
    version="1.0.0"
)

# Configuración de infraestructura inyectada por el orquestador Kubernetes
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_USER = os.getenv("DB_USER", "user")
DB_PASS = os.getenv("DB_PASS", "password")
DB_NAME = os.getenv("DB_NAME", "orders_db")
INVENTORY_URL = os.getenv("INVENTORY_URL", "http://inventory-service:8000")
BROKER_HOST = os.getenv("BROKER_HOST", "broker-service")
COLA_NOTIFICACIONES = "notificaciones_pedidos"

# Inicialización del cliente del Broker de Mensajes desarrollado en la Práctica 3
# Se asegura la compatibilidad con el protocolo de ACKs original.
broker = MOMClient(host=BROKER_HOST)

def get_db_connection():
    """
    Factoría de conexiones para MariaDB.
    Gestiona la conexión hacia la base de datos de pedidos persistida vía PVC.
    """
    return mysql.connector.connect(
        host=DB_HOST, 
        user=DB_USER, 
        password=DB_PASS, 
        database=DB_NAME, 
        connect_timeout=5
    )

class OrderRequest(BaseModel):
    """
    Esquema de validación para la creación de un nuevo pedido.
    """
    product_id: int
    quantity: int
    customer_name: str

@app.get("/health")
def health():
    """Endpoint de diagnóstico de salud (Liveness Check)."""
    return {"status": "ok", "service": "orders"}

@app.post("/orders")
def create_order(order: OrderRequest):
    """Flujo Transaccional de Creación de Pedido."""
    logger.info(f"Transacción: Iniciando procesamiento para el cliente {order.customer_name}...")

    # --- PASO 1: Validación y Reserva de Stock (Comunicación Síncrona REST) ---
    # Realizamos una llamada bloqueante al microservicio de Inventario antes de confirmar la venta.
    try:
        # 1.1 Verificamos si el producto existe
        inv_check = requests.get(f"{INVENTORY_URL}/inventory/{order.product_id}", timeout=5)
        if inv_check.status_code != 200:
            logger.warning(f"Negocio: Producto {order.product_id} no localizado en catálogo.")
            raise HTTPException(status_code=404, detail="Producto no encontrado")
        
        # 1.2 Intentamos descontar el stock
        # Si el inventario no tiene existencias, devolverá un error 400 y abortaremos.
        inv_update = requests.put(
            f"{INVENTORY_URL}/inventory/{order.product_id}", 
            json={"cantidad": -order.quantity},
            timeout=5
        )
        if inv_update.status_code != 200:
            raise HTTPException(
                status_code=inv_update.status_code, 
                detail="Operación cancelada: Stock insuficiente o error en inventario"
            )
            
    except requests.exceptions.RequestException as e:
        # Si el microservicio de inventario está caído, no podemos garantizar la venta.
        logger.error(f"Falla Sistémica: El servicio de Inventario es inalcanzable: {e}")
        raise HTTPException(status_code=503, detail="Servicio de Inventario temporalmente fuera de servicio")

    # --- PASO 2: Persistencia de la Venta (Base de Datos Local MariaDB) ---
    # Una vez reservado el stock, registramos el pedido en nuestra propia base de datos.
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        # Insertamos los datos del pedido para auditoría y facturación
        cur.execute(
            "INSERT INTO orders (product_id, quantity, customer_name) VALUES (%s, %s, %s)",
            (order.product_id, order.quantity, order.customer_name)
        )
        # Obtenemos el ID autogenerado para el mensaje de confirmación
        order_id = cur.lastrowid
        conn.commit() # Consolidamos la grabación en el volumen persistente (PVC)
        cur.close()
        conn.close()
        logger.info(f"Persistencia: Pedido #{order_id} consolidado en MariaDB.")
    except Exception as e:
        # Error crítico: hemos descontado stock pero no hemos grabado el pedido.
        logger.error(f"Fallo de Persistencia: Error al registrar pedido en MariaDB: {e}")
        raise HTTPException(status_code=500, detail="Error crítico al persistir la transacción de venta")

    # --- PASO 3: Notificación Asíncrona (Comunicación MOM) ---
    # Enviamos un evento al Broker de la P3 para que el servicio de Notificaciones actúe.
    # Usamos un bloque try-except independiente para no arruinar la venta si el broker falla.
    try:
        mensaje_notif = {
            "order_id": order_id,
            "customer": order.customer_name,
            "message": f"Su pedido #{order_id} ha sido procesado con éxito."
        }
        # Aseguramos la existencia de la cola (idempotente)
        broker.declarar_cola(COLA_NOTIFICACIONES)
        # Publicamos el mensaje JSON en el broker MOM
        broker.publicar(COLA_NOTIFICACIONES, mensaje_notif)
        logger.debug(f"Asíncrono: Evento publicado en cola '{COLA_NOTIFICACIONES}'.")
    except Exception as e:
        # Registramos el fallo del broker pero la transacción principal se considera éxito.
        logger.error(f"Aviso: El pedido se guardó pero la notificación falló: {e}")

    # Retornamos éxito al cliente final a través del Gateway
    return {"order_id": order_id, "status": "Transacción finalizada satisfactoriamente"}
