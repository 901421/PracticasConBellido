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
from anyio import to_thread
import os
import mysql.connector
import httpx
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
@app.get("/orders/health")
def health():
    """Endpoint de diagnóstico de salud (Liveness Check)."""
    return {"status": "ok", "service": "orders"}

@app.post("/orders")
async def create_order(order: OrderRequest):
    """Flujo Transaccional de Creación de Pedido con Patrón Saga (Compensación)."""
    logger.info(f"Transacción: Iniciando procesamiento para el cliente {order.customer_name}...")

    # --- PASO 1: Reserva Atómica de Stock (Comunicación Síncrona REST - httpx) ---
    # Eliminamos el GET previo (Anti-patrón de Validación) y realizamos directamente el PUT.
    # El inventory-service ya es atómico y valida existencia/stock.
    try:
        async with httpx.AsyncClient() as client:
            inv_update = await client.put(
                f"{INVENTORY_URL}/inventory/{order.product_id}", 
                json={"cantidad": -order.quantity},
                timeout=5
            )
            
            if inv_update.status_code != 200:
                logger.warning(f"Negocio: No se pudo reservar stock. Status: {inv_update.status_code}")
                # Si el error es 404 o 400, lo propagamos (Producto no encontrado o Stock insuficiente)
                detail = inv_update.json().get("detail", "Error en inventario")
                raise HTTPException(status_code=inv_update.status_code, detail=detail)
            
    except httpx.RequestError as e:
        logger.error(f"Falla Sistémica: El servicio de Inventario es inalcanzable: {e}")
        raise HTTPException(status_code=503, detail="Servicio de Inventario temporalmente fuera de servicio")

    # --- PASO 2: Persistencia de la Venta (MariaDB) con Compensación ---
    # Usamos to_thread.run_sync para no bloquear el Event Loop con la DB síncrona.
    try:
        def persist_order_sync():
            conn = get_db_connection()
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO orders (product_id, quantity, customer_name) VALUES (%s, %s, %s)",
                (order.product_id, order.quantity, order.customer_name)
            )
            order_id = cur.lastrowid
            conn.commit()
            cur.close()
            conn.close()
            return order_id

        order_id = await to_thread.run_sync(persist_order_sync)
        logger.info(f"Persistencia: Pedido #{order_id} consolidado en MariaDB.")
    except Exception as e:
        # --- COMPENSACIÓN (SAGA) ---
        # Si la DB local falla, debemos "deshacer" el descuento de stock en el Inventario.
        logger.error(f"Fallo de Persistencia: Error en MariaDB: {e}. Iniciando compensación...")
        try:
            async with httpx.AsyncClient() as client:
                # Sumamos la cantidad que restamos previamente
                await client.put(
                    f"{INVENTORY_URL}/inventory/{order.product_id}", 
                    json={"cantidad": order.quantity},
                    timeout=5
                )
                logger.info("Compensación: Stock restaurado correctamente.")
        except Exception as comp_e:
            logger.error(f"CRÍTICO: Error en compensación de stock: {comp_e}. El sistema ha quedado inconsistente.")
        
        raise HTTPException(status_code=500, detail="Error crítico al persistir la transacción. Stock restaurado.")

    # --- PASO 3: Notificación Asíncrona (MOM) ---
    # Usamos to_thread.run_sync para el Broker MOM que también es síncrono.
    try:
        mensaje_notif = {
            "order_id": order_id,
            "customer": order.customer_name,
            "message": f"Su pedido #{order_id} ha sido procesado con éxito."
        }
        await to_thread.run_sync(broker.declarar_cola, COLA_NOTIFICACIONES)
        await to_thread.run_sync(broker.publicar, COLA_NOTIFICACIONES, mensaje_notif)
        logger.debug(f"Asíncrono: Evento publicado en cola '{COLA_NOTIFICACIONES}'.")
    except Exception as e:
        logger.error(f"Aviso: El pedido se guardó pero la notificación falló: {e}")

    # Retornamos éxito al cliente final a través del Gateway
    return {"order_id": order_id, "status": "Transacción finalizada satisfactoriamente"}
