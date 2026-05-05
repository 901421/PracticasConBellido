"""
Microservicio de Pedidos (Orders Service).

Actúa como el orquestador principal del proceso de compra. Este servicio integra
comunicación síncrona HTTP para la validación de inventario y comunicación
asíncrona mediante un Broker MOM para notificaciones de post-venta. Implementa
el patrón Saga para garantizar consistencia eventual.

Responsabilidades:
    - Gestión del ciclo de vida de los pedidos (Creación y Persistencia).
    - Orquestación de llamadas síncronas a Inventario (Reserva de stock).
    - Persistencia de transacciones en base de datos MariaDB (Decentralized Data).
    - Ejecución de lógica de compensación ante fallos (Patrón Saga).
    - Emisión de eventos asíncronos para el sistema de notificaciones.
"""

from fastapi import FastAPI, HTTPException # Framework para la API REST
from pydantic import BaseModel # Validación de esquemas de datos
from anyio import to_thread # Permite ejecutar tareas síncronas en hilos sin bloquear el event loop
import os # Gestión de variables de entorno
import mysql.connector # Driver de conexión para MariaDB/MySQL
import httpx # Cliente HTTP asíncrono moderno
import logging # Registro de eventos
from client import MOMClient # Cliente unificado para el Middleware de Mensajería (P3)

# Configuración de logging profesional para trazabilidad completa de pedidos
logging.basicConfig(
    level=logging.INFO, 
    format='%(asctime)s [%(levelname)s] Pedidos: %(message)s'
)
logger = logging.getLogger(__name__)

# Instanciación de la aplicación
app = FastAPI(
    title="Orders Service - P4 Arquitectura Software",
    description="Orquestador de compras con persistencia en MariaDB e integración MOM.",
    version="1.0.0"
)

# Configuración de infraestructura inyectada por Kubernetes (ConfigMaps/Secrets)
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_USER = os.getenv("DB_USER", "user")
DB_PASS = os.getenv("DB_PASS", "password")
DB_NAME = os.getenv("DB_NAME", "orders_db")
INVENTORY_URL = os.getenv("INVENTORY_URL", "http://inventory-service:8000")
BROKER_HOST = os.getenv("BROKER_HOST", "broker-service")
COLA_NOTIFICACIONES = "notificaciones_pedidos"

# Inicialización del cliente del Broker MOM.
# Se utiliza el archivo unificado compartido desde la carpeta /broker.
broker = MOMClient(host=BROKER_HOST)

def get_db_connection():
    """
    Factoría de conexiones para MariaDB.
    Se encarga de conectar con la instancia de base de datos dedicada a pedidos.
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
    Esquema de datos para la creación de un nuevo pedido.
    """
    product_id: int
    quantity: int
    customer_name: str

@app.get("/health")
@app.get("/orders/health")
def health():
    """Endpoint de diagnóstico (Liveness Check)."""
    return {"status": "ok", "service": "orders"}

@app.post("/orders")
async def create_order(order: OrderRequest):
    """
    Flujo Transaccional de Creación de Pedido con Patrón Saga (Orquestación).
    
    Sigue una secuencia coordinada de pasos síncronos y asíncronos con lógica
    de marcha atrás (compensación) en caso de fallo intermedio.
    """
    logger.info(f"Transacción: Iniciando procesamiento para el cliente {order.customer_name}...")

    # --- PASO 1: RESERVA ATÓMICA DE STOCK (Comunicación Síncrona - REST) ---
    # Realizamos directamente un PUT al servicio de Inventario.
    # El inventario gestiona internamente la atomicidad y la regla de no-stock-negativo.
    try:
        async with httpx.AsyncClient() as client:
            inv_update = await client.put(
                f"{INVENTORY_URL}/inventory/{order.product_id}", 
                json={"cantidad": -order.quantity}, # Restamos la cantidad solicitada
                timeout=5
            )
            
            # Si el inventario responde con error (ej. 400 Stock Insuficiente o 404 No existe)
            if inv_update.status_code != 200:
                logger.warning(f"Negocio: Reserva de stock rechazada. Status: {inv_update.status_code}")
                detail = inv_update.json().get("detail", "Error en inventario")
                # Abortamos el flujo. Como no hemos guardado nada todavía, no hace falta compensación.
                raise HTTPException(status_code=inv_update.status_code, detail=detail)
            
    except httpx.RequestError as e:
        # Si el servicio de inventario está caído, devolvemos un 503 (Servicio no disponible)
        logger.error(f"Falla Sistémica: El servicio de Inventario es inalcanzable: {e}")
        raise HTTPException(status_code=503, detail="Servicio de Inventario temporalmente fuera de servicio")

    # --- PASO 2: PERSISTENCIA LOCAL (MariaDB) ---
    # Una vez reservado el stock, procedemos a guardar la venta en nuestra propia BBDD.
    # Se utiliza to_thread.run_sync porque el driver de MariaDB es síncrono y bloquearía la API.
    try:
        def persist_order_sync():
            """Lógica síncrona de inserción en base de datos."""
            conn = get_db_connection()
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO orders (product_id, quantity, customer_name) VALUES (%s, %s, %s)",
                (order.product_id, order.quantity, order.customer_name)
            )
            order_id = cur.lastrowid # Recuperamos el ID autoincremental generado
            conn.commit() # Confirmamos la transacción SQL
            cur.close()
            conn.close()
            return order_id

        # Ejecutamos la persistencia en un hilo separado
        order_id = await to_thread.run_sync(persist_order_sync)
        logger.info(f"Persistencia: Pedido #{order_id} consolidado en MariaDB.")

    except Exception as e:
        # --- LÓGICA DE COMPENSACIÓN (PATRÓN SAGA) ---
        # ¡IMPORTANTE! Si llegamos aquí, el Inventario YA ha restado el stock, pero
        # nosotros no hemos podido guardar el pedido. Debemos "deshacer" la resta de stock
        # para que el sistema vuelva a un estado consistente.
        logger.error(f"Fallo de Persistencia: Error en MariaDB: {e}. Iniciando compensación...")
        try:
            async with httpx.AsyncClient() as client:
                # Realizamos un PUT inverso sumando la cantidad que restamos previamente
                await client.put(
                    f"{INVENTORY_URL}/inventory/{order.product_id}", 
                    json={"cantidad": order.quantity}, # Sumamos de vuelta
                    timeout=5
                )
                logger.info("Compensación: Stock restaurado correctamente en Inventario.")
        except Exception as comp_e:
            # Si falla la compensación, estamos ante un error crítico de inconsistencia (Data Corruption)
            logger.error(f"CRÍTICO: Error en compensación: {comp_e}. Sistema inconsistente.")
        
        # Devolvemos error al cliente informando que la transacción falló pero se intentó restaurar.
        raise HTTPException(status_code=500, detail="Error crítico al persistir la transacción. Stock restaurado.")

    # --- PASO 3: NOTIFICACIÓN ASÍNCRONA (MOM - Event Driven) ---
    # El pedido es firme. Avisamos al sistema de notificaciones mediante el Broker.
    # Se hace de forma asíncrona para no hacer esperar al cliente por el envío del email.
    try:
        mensaje_notif = {
            "order_id": order_id,
            "customer": order.customer_name,
            "message": f"Su pedido #{order_id} ha sido procesado con éxito."
        }
        # Invocamos al broker mediante hilos para no bloquear la respuesta HTTP
        await to_thread.run_sync(broker.declarar_cola, COLA_NOTIFICACIONES)
        await to_thread.run_sync(broker.publicar, COLA_NOTIFICACIONES, mensaje_notif)
        logger.debug(f"Asíncrono: Evento publicado en cola '{COLA_NOTIFICACIONES}'.")
    except Exception as e:
        # Si falla el broker, el pedido SIGUE SIENDO VÁLIDO.
        # Es mejor que el cliente no reciba el email a decirle que su compra falló cuando ya le hemos cobrado.
        logger.error(f"Aviso: El pedido se guardó pero la notificación falló: {e}")

    # Retornamos el éxito definitivo al cliente a través del API Gateway
    return {"order_id": order_id, "status": "Transacción finalizada satisfactoriamente"}
