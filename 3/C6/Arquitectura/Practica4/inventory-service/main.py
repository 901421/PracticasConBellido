"""
Microservicio de Inventario (Inventory Service).

Gestiona el catálogo de productos y su disponibilidad física. Este servicio
mantiene la consistencia del stock mediante el uso de una base de datos 
relacional PostgreSQL y garantiza la integridad transaccional en operaciones concurrentes.

Responsabilidades:
    - Consulta síncrona de niveles de existencias.
    - Actualización atómica de stock (sumas y restas).
    - Garantía de no-negatividad del stock (Regla de negocio core).
    - Persistencia políglota independiente (Data Decentralization).
"""

from fastapi import FastAPI, HTTPException # Framework web y manejo de errores
from pydantic import BaseModel # Validación de esquemas de datos (Tipado estático)
import os # Interacción con el sistema operativo y variables de entorno
import psycopg2 # Driver de conexión para PostgreSQL
from psycopg2.extras import RealDictCursor # Permite obtener resultados como diccionarios {'col': val}
import logging # Sistema de trazas

# Configuración de logging para trazabilidad de operaciones de almacén
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] Inventario: %(message)s'
)
logger = logging.getLogger(__name__)

# Creación de la instancia de la API
app = FastAPI(
    title="Inventory Service - P4 Arquitectura Software",
    description="Gestión de existencias con persistencia en PostgreSQL.",
    version="1.0.0"
)

# Parámetros de infraestructura inyectados dinámicamente por Kubernetes/Docker
# Estos valores permiten al código ser agnóstico del entorno (dev/prod).
DB_HOST = os.getenv("DB_HOST", "localhost") # Host del servidor PostgreSQL
DB_NAME = os.getenv("DB_NAME", "inventory_db") # Nombre de la base de datos
DB_USER = os.getenv("DB_USER", "user") # Usuario de conexión
DB_PASS = os.getenv("DB_PASS", "password") # Contraseña de conexión

def get_db_connection():
    """
    Factoría de conexiones para PostgreSQL.
    
    Configura un cursor de tipo RealDictCursor para facilitar el mapeo 
    automático de filas de la base de datos a objetos JSON para la API.
    """
    return psycopg2.connect(
        host=DB_HOST, 
        database=DB_NAME, 
        user=DB_USER, 
        password=DB_PASS,
        cursor_factory=RealDictCursor, # Devuelve resultados como dicts en lugar de tuplas
        connect_timeout=5 # Tiempo límite para el handshake inicial
    )

class StockUpdate(BaseModel):
    """
    Esquema de validación para la modificación de existencias.
    
    Attributes:
        cantidad (int): Valor algebraico a sumar al stock actual. 
                        Positivo para reposición, negativo para venta.
    """
    cantidad: int

@app.get("/health")
@app.get("/inventory/health")
def health():
    """Endpoint de diagnóstico para monitorización de salud del servicio (Liveness)."""
    return {"status": "ok", "service": "inventory"}

@app.get("/inventory/{product_id}")
def get_stock(product_id: int):
    """
    Consulta de niveles de stock para un producto específico.
    """
    try:
        # Abrimos conexión con PostgreSQL (Persistencia gestionada por PVC)
        with get_db_connection() as conn:
            with conn.cursor() as cur:
                # Ejecutamos consulta de lectura simple
                cur.execute("SELECT stock FROM products WHERE id = %s", (product_id,))
                result = cur.fetchone()
                
                # Si el cursor no devuelve filas, el producto no existe en el catálogo
                if not result:
                    logger.warning(f"Consulta: Producto {product_id} inexistente.")
                    raise HTTPException(status_code=404, detail="Producto no encontrado")
                
                # Retornamos el stock actual mapeado por el RealDictCursor
                return {"product_id": product_id, "stock": result["stock"]}
    except psycopg2.Error as e:
        # Capturamos fallos de base de datos y devolvemos 500 (Internal Server Error)
        logger.error(f"Fallo de Persistencia: Error en consulta de stock: {e}")
        raise HTTPException(status_code=500, detail="Error interno de base de datos")

@app.put("/inventory/{product_id}")
def update_stock(product_id: int, update: StockUpdate):
    """
    Actualización Transaccional y Atómica de Stock.
    
    Utiliza bloqueos de fila para garantizar la consistencia en entornos concurrentes.
    """
    try:
        # Iniciamos bloque de conexión (context manager asegura el cierre)
        with get_db_connection() as conn:
            with conn.cursor() as cur:
                # --- CONTROL DE CONCURRENCIA (Locking) ---
                # Aplicamos un bloqueo de fila 'FOR UPDATE'. 
                # Si otro hilo intenta leer/modificar este producto, quedará en espera
                # hasta que esta transacción haga commit o rollback. Previene Race Conditions.
                cur.execute("SELECT stock FROM products WHERE id = %s FOR UPDATE", (product_id,))
                result = cur.fetchone()
                
                if not result:
                    # Si el producto desapareció o no existe, abortamos
                    raise HTTPException(status_code=404, detail="Producto no encontrado")
                
                # Calculamos el balance resultante (Suma algebraica)
                nuevo_stock = result["stock"] + update.cantidad
                
                # --- REGLA DE NEGOCIO CRÍTICA ---
                # No se permite que el stock físico sea negativo bajo ninguna circunstancia.
                if nuevo_stock < 0:
                    logger.warning(f"Validación Fallida: Intento de stock negativo para producto {product_id}.")
                    raise HTTPException(status_code=400, detail="Existencias insuficientes para completar la operación")
                    
                # Si la validación es correcta, persistimos el nuevo valor
                cur.execute("UPDATE products SET stock = %s WHERE id = %s", (nuevo_stock, product_id))
                
                # Confirmamos los cambios de forma definitiva y liberamos el bloqueo de fila
                conn.commit() 
                
                logger.info(f"Actualización: Producto {product_id} actualizado. Nuevo stock: {nuevo_stock}.")
                return {"product_id": product_id, "nuevo_stock": nuevo_stock}
    except psycopg2.Error as e:
        # Manejo de excepciones de PostgreSQL (ej. fallos de red, integridad)
        logger.error(f"Fallo de Persistencia: Error en actualización de stock: {e}")
        raise HTTPException(status_code=500, detail="Error crítico en la transacción de inventario")
