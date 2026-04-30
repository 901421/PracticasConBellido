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

from fastapi import FastAPI, Request, HTTPException, Response
import requests
import os
import logging

# Configuración de logging profesional para auditoría de tráfico y depuración
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] Gateway: %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="API Gateway - P4 Arquitectura Software",
    description="Punto de entrada seguro para la gestión de pedidos e inventario.",
    version="1.0.0"
)

# Configuración dinámica mediante variables de entorno (Inyectadas por Kubernetes)
ORDERS_URL = os.getenv("ORDERS_URL", "http://orders-service:8000")
INVENTORY_URL = os.getenv("INVENTORY_URL", "http://inventory-service:8000")

# Requisito de seguridad P4: Clave de acceso cargada desde Secret de Kubernetes (No hardcoded)
API_KEY_SECRET = os.environ["API_TOKEN"]

@app.middleware("http")
async def security_middleware(request: Request, call_next):
    """
    Interceptor de seguridad (Middleware).
    """
    # Permitimos el acceso libre al endpoint /health para que Kubernetes 
    # pueda monitorizar si el pod está vivo sin necesidad de API Key.
    if request.url.path == "/health":
        return await call_next(request)

    # Extraemos la clave de la cabecera personalizada definida en el enunciado
    api_key = request.headers.get("X-API-Key")
    
    # Validamos la clave contra el secreto inyectado por Kubernetes
    if api_key != API_KEY_SECRET:
        # Si la clave falta o es incorrecta, bloqueamos la petición y devolvemos 401
        logger.warning(f"Seguridad: Intento de acceso no autorizado desde {request.client.host}")
        return Response(
            content='{"detail": "No autorizado: X-API-Key inválida o ausente"}', 
            status_code=401, 
            media_type="application/json"
        )

    # Si la validación es correcta, pasamos la petición al siguiente nivel (proxy)
    return await call_next(request)

@app.get("/health")
def health():
    """Endpoint de diagnóstico."""
    return {"status": "ok", "gateway": "activo"}

@app.api_route("/orders/{path_name:path}", methods=["GET", "POST", "PUT", "DELETE"])
async def proxy_orders(request: Request, path_name: str):
    """Redirección hacia el microservicio de pedidos."""
    # Construimos la URL destino usando la variable de entorno configurada en K8s
    target_url = f"{ORDERS_URL}/orders/{path_name}"
    logger.info(f"Proxy: Forwarding a Pedidos -> {target_url}")
    return await _proxy(target_url, request)

@app.api_route("/inventory/{path_name:path}", methods=["GET", "POST", "PUT", "DELETE"])
async def proxy_inventory(request: Request, path_name: str):
    """Redirección hacia el microservicio de inventario."""
    # Construimos la URL destino para inventario
    target_url = f"{INVENTORY_URL}/inventory/{path_name}"
    logger.info(f"Proxy: Forwarding a Inventario -> {target_url}")
    return await _proxy(target_url, request)

async def _proxy(url: str, request: Request):
    """Motor de Proxy Inverso."""
    method = request.method
    # Filtramos la cabecera 'host' para que el servidor interno no reciba el host original
    # y así evitar problemas de resolución de nombres dentro de la red de K8s.
    headers = {k: v for k, v in request.headers.items() if k.lower() != 'host'}
    # Leemos el cuerpo de la petición (necesario para POST/PUT)
    body = await request.body()
    
    try:
        # Re-enviamos la petición al servicio interno con todos los parámetros originales
        resp = requests.request(
            method, url, 
            headers=headers, 
            data=body, 
            params=request.query_params,
            timeout=10 # Tiempo de espera para evitar bloqueos infinitos
        )
        # Devolvemos la respuesta del servicio interno tal cual al cliente original
        return Response(
            content=resp.content, 
            status_code=resp.status_code, 
            headers=dict(resp.headers)
        )
    except Exception as e:
        # Capturamos fallos de red y devolvemos un error 502 (Bad Gateway)
        logger.error(f"Falla Sistémica: Error de red al contactar con servicio interno: {e}")
        raise HTTPException(
            status_code=502, 
            detail="Error de comunicación con el servicio de backend (servicio inalcanzable)."
        )
