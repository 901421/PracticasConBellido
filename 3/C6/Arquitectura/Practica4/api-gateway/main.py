"""
Microservicio API Gateway (Frontend Proxy).

Este componente actúa como el punto único de entrada (Single Point of Entry) para el
ecosistema de microservicios. Implementa el patrón Gateway para centralizar la 
seguridad perimetral y el enrutamiento inverso hacia los servicios internos de la malla.

Responsabilidades:
    - Validación de autenticación mediante tokens estáticos (SecDevOps).
    - Enrutamiento de peticiones (Routing) a servicios de Pedidos e Inventario.
    - Abstracción de la topología interna del clúster ante clientes externos.
"""

from fastapi import FastAPI, Request, HTTPException, Response # Framework web y utilidades HTTP
import httpx # Cliente HTTP asíncrono para el reenvío de peticiones (Proxying)
import os # Acceso a variables de entorno del sistema
import logging # Sistema de trazas para auditoría

# Configuración de logging profesional para auditoría de tráfico y depuración
# Define el formato incluyendo timestamp y nivel de severidad.
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] Gateway: %(message)s'
)
logger = logging.getLogger(__name__)

# Instanciación de la aplicación FastAPI con metadatos para la documentación OpenAPI/Swagger
app = FastAPI(
    title="API Gateway - P4 Arquitectura Software",
    description="Punto de entrada seguro para la gestión de pedidos e inventario.",
    version="1.0.0"
)

# Configuración dinámica mediante variables de entorno (Inyectadas por Kubernetes/Docker)
# Define las direcciones internas de los servicios dentro de la red del clúster.
ORDERS_URL = os.getenv("ORDERS_URL", "http://orders-service:8000")
INVENTORY_URL = os.getenv("INVENTORY_URL", "http://inventory-service:8000")

# Requisito de seguridad P4: Clave de acceso cargada desde Secret de Kubernetes.
# Se utiliza os.environ para forzar el fallo si la variable no existe (Safe Start).
API_KEY_SECRET = os.environ["API_TOKEN"]

@app.middleware("http")
async def security_middleware(request: Request, call_next):
    """
    Interceptor de seguridad (Middleware).
    
    Analiza cada petición entrante antes de que llegue a las rutas de proxy.
    """
    # Permitimos el acceso libre al endpoint /health para que los Liveness/Readiness probes
    # de Kubernetes puedan monitorizar el estado del pod sin necesidad de credenciales.
    if request.url.path == "/health":
        return await call_next(request)

    # Extraemos la clave de la cabecera personalizada 'X-API-Key' definida en el contrato de la API
    api_key = request.headers.get("X-API-Key")
    
    # Validamos la clave recibida contra el secreto maestro inyectado en el entorno
    if api_key != API_KEY_SECRET:
        # Si la clave no coincide, registramos el intento fallido y bloqueamos con 401 (Unauthorized)
        logger.warning(f"Seguridad: Intento de acceso no autorizado desde {request.client.host}")
        return Response(
            content='{"detail": "No autorizado: X-API-Key inválida o ausente"}', 
            status_code=401, 
            media_type="application/json"
        )

    # Si la validación es exitosa, invocamos el siguiente eslabón de la cadena (el proxy)
    return await call_next(request)

@app.get("/health")
def health():
    """Endpoint de diagnóstico interno del propio Gateway."""
    return {"status": "ok", "gateway": "activo"}

@app.api_route("/orders/{path_name:path}", methods=["GET", "POST", "PUT", "DELETE"])
async def proxy_orders(request: Request, path_name: str):
    """
    Punto de enlace (Mount Point) para el microservicio de Pedidos.
    Captura cualquier ruta que empiece por /orders/ y la redirige internamente.
    """
    # Construimos la ruta de destino preservando los parámetros de la URL original
    target_path = f"orders/{path_name}" if path_name else "orders"
    target_url = f"{ORDERS_URL}/{target_path}"
    logger.info(f"Proxy: Forwarding a Pedidos -> {target_url}")
    # Invocamos el motor de proxy genérico
    return await _proxy(target_url, request)

@app.api_route("/inventory/{path_name:path}", methods=["GET", "POST", "PUT", "DELETE"])
async def proxy_inventory(request: Request, path_name: str):
    """
    Punto de enlace (Mount Point) para el microservicio de Inventario.
    """
    target_path = f"inventory/{path_name}" if path_name else "inventory"
    target_url = f"{INVENTORY_URL}/{target_path}"
    logger.info(f"Proxy: Forwarding a Inventario -> {target_url}")
    return await _proxy(target_url, request)

async def _proxy(url: str, request: Request):
    """
    Motor de Proxy Inverso Asíncrono.
    
    Actúa como cliente intermedio, reenviando la petición original al servicio interno
    y devolviendo la respuesta recibida al cliente externo.
    """
    method = request.method # Mantiene el verbo HTTP original (GET, POST, etc.)
    
    # Filtramos la cabecera 'host' original. 
    # Es crítico para que el servidor interno no intente resolver el dominio externo,
    # sino que acepte la conexión dentro de la red privada de Kubernetes.
    headers = {k: v for k, v in request.headers.items() if k.lower() != 'host'}
    
    # Leemos el cuerpo de la petición (necesario para transmitir payloads en POST/PUT)
    body = await request.body()
    
    try:
        # Iniciamos un cliente HTTP asíncrono para realizar la llamada interna
        async with httpx.AsyncClient() as client:
            resp = await client.request(
                method, url, 
                headers=headers, 
                content=body, 
                params=request.query_params,
                timeout=10 # Límite de 10s para evitar que hilos se bloqueen por servicios lentos
            )
        
        # Extraemos los datos de la respuesta del microservicio de backend
        content = resp.content
        status_code = resp.status_code
        headers = dict(resp.headers)

        # GESTIÓN DE REDIRECCIONES (Rewrite):
        # Si el backend devuelve un 3xx (Redirección), la URL 'Location' será interna (ej: http://orders-service...).
        # Debemos reescribirla para que el cliente externo reciba una URL que pase de nuevo por el Gateway.
        if status_code in (301, 302, 303, 307, 308) and "location" in headers:
            loc = headers["location"]
            if ORDERS_URL in loc:
                headers["location"] = loc.replace(ORDERS_URL, str(request.base_url) + "orders")
            elif INVENTORY_URL in loc:
                headers["location"] = loc.replace(INVENTORY_URL, str(request.base_url) + "inventory")

        # Devolvemos la respuesta reconstruida al cliente original
        return Response(
            content=content, 
            status_code=status_code, 
            headers=headers
        )
    except Exception as e:
        # En caso de fallo de red (servicio caído), devolvemos 502 Bad Gateway
        logger.error(f"Falla Sistémica: Error de red al contactar con servicio interno: {e}")
        raise HTTPException(
            status_code=502, 
            detail="Error de comunicación con el servicio de backend (servicio inalcanzable)."
        )
