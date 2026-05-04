"""
Microservicio de Inventario (Inventory Service).

Gestiona el catálogo de productos y su disponibilidad física. Este servicio
mantiene la consistencia del stock mediante el uso de una base de datos 
relacional PostgreSQL y garantiza la integridad transaccional en operaciones concurrentes.

Responsabilidades:
    - Consulta síncrona de niveles de existencias.
    - Actualización atómica de stock (sumas y restas).
    - Persistencia políglota independiente (Data Decentralization).
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import os
import psycopg2
from psycopg2.extras import RealDictCursor
import logging

# Configuración de logging para trazabilidad de operaciones de almacén
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] Inventario: %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Inventory Service - P4 Arquitectura Software",
    description="Gestión de existencias con persistencia en PostgreSQL.",
    version="1.0.0"
)

# Parámetros de infraestructura inyectados dinámicamente por Kubernetes
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_NAME = os.getenv("DB_NAME", "inventory_db")
DB_USER = os.getenv("DB_USER", "user")
DB_PASS = os.getenv("DB_PASS", "password")

def get_db_connection():
    """
    Factoría de conexiones para PostgreSQL.
    
    Configura un cursor de tipo RealDictCursor para facilitar el mapeo 
    de filas de la base de datos a objetos JSON/Diccionarios.
    """
    return psycopg2.connect(
        host=DB_HOST, 
        database=DB_NAME, 
        user=DB_USER, 
        password=DB_PASS,
        cursor_factory=RealDictCursor, 
        connect_timeout=5
    )

class StockUpdate(BaseModel):
    """
    Esquema de datos para la modificación de existencias.
    La 'cantidad' puede ser positiva (reposición) o negativa (venta).
    """
    cantidad: int

@app.get("/health")
@app.get("/inventory/health")
def health():
    """Endpoint de diagnóstico para monitorización de salud del servicio."""
    return {"status": "ok", "service": "inventory"}

@app.get("/inventory/{product_id}")
def get_stock(product_id: int):
    """Consulta de stock."""
    try:
        # Abrimos conexión con PostgreSQL (inyectado por K8s)
        with get_db_connection() as conn:
            with conn.cursor() as cur:
                # Consultamos el stock actual del producto solicitado
                cur.execute("SELECT stock FROM products WHERE id = %s", (product_id,))
                result = cur.fetchone()
                # Si el cursor no devuelve filas, el producto no existe
                if not result:
                    logger.warning(f"Consulta: Producto {product_id} inexistente.")
                    raise HTTPException(status_code=404, detail="Producto no encontrado")
                # Devolvemos el valor del stock (mapeado por RealDictCursor)
                return {"product_id": product_id, "stock": result["stock"]}
    except psycopg2.Error as e:
        # Si hay error en la DB, registramos y devolvemos error 500
        logger.error(f"Fallo de Persistencia: Error en consulta de stock: {e}")
        raise HTTPException(status_code=500, detail="Error interno de base de datos")

@app.put("/inventory/{product_id}")
def update_stock(product_id: int, update: StockUpdate):
    """Actualización transaccional de stock."""
    try:
        with get_db_connection() as conn:
            with conn.cursor() as cur:
                # Aplicamos un bloqueo de fila (FOR UPDATE) para que ningún otro proceso 
                # modifique este producto hasta que termine nuestra transacción.
                # Esto garantiza consistencia en las ventas concurrentes.
                cur.execute("SELECT stock FROM products WHERE id = %s FOR UPDATE", (product_id,))
                result = cur.fetchone()
                
                if not result:
                    raise HTTPException(status_code=404, detail="Producto no encontrado")
                
                # Calculamos el stock resultante de la operación
                nuevo_stock = result["stock"] + update.cantidad
                
                # Verificamos la regla de negocio: el stock nunca puede ser menor que cero.
                if nuevo_stock < 0:
                    logger.warning(f"Validación Fallida: Intento de stock negativo para producto {product_id}.")
                    raise HTTPException(status_code=400, detail="Existencias insuficientes para completar la operación")
                    
                # Ejecutamos la actualización atómica
                cur.execute("UPDATE products SET stock = %s WHERE id = %s", (nuevo_stock, product_id))
                # Confirmamos los cambios de forma definitiva
                conn.commit() 
                
                logger.info(f"Actualización: Producto {product_id} actualizado. Nuevo stock: {nuevo_stock}.")
                return {"product_id": product_id, "nuevo_stock": nuevo_stock}
    except psycopg2.Error as e:
        # Manejo de excepciones de PostgreSQL
        logger.error(f"Fallo de Persistencia: Error en actualización de stock: {e}")
        raise HTTPException(status_code=500, detail="Error crítico en la transacción de inventario")
