"""
Microservicio API Gateway (Frontend Proxy).

Este componente actÃºa como el punto Ãºnico de entrada (Single Point of Entry) para el
ecosistema de microservicios. Implementa el patrÃ³n Gateway para centralizar la 
seguridad perimetral y el enrutamiento inverso hacia los servicios internos de la malla.

Responsabilidades:
    - ValidaciÃ³n de autenticaciÃ³n mediante tokens estÃ¡ticos (SecDevOps).
    - Enrutamiento de peticiones (Routing) a servicios de Pedidos e Inventario.
    - AbstracciÃ³n de la topologÃ­a interna del clÃºster ante clientes externos.
"""

from fastapi import FastAPI, Request, HTTPException, Response
import httpx
import os
import logging

# ConfiguraciÃ³n de logging profesional para auditorÃ­a de trÃ¡fico y depuraciÃ³n
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] Gateway: %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="API Gateway - P4 Arquitectura Software",
    description="Punto de entrada seguro para la gestiÃ³n de pedidos e inventario.",
    version="1.0.0"
)

# ConfiguraciÃ³n dinÃ¡mica mediante variables de entorno (Inyectadas por Kubernetes)
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
    # pueda monitorizar si el pod estÃ¡ vivo sin necesidad de API Key.
    if request.url.path == "/health":
        return await call_next(request)

    # Extraemos la clave de la cabecera personalizada definida en el enunciado
    api_key = request.headers.get("X-API-Key")
    
    # Validamos la clave contra el secreto inyectado por Kubernetes
    if api_key != API_KEY_SECRET:
        # Si la clave falta o es incorrecta, bloqueamos la peticiÃ³n y devolvemos 401
        logger.warning(f"Seguridad: Intento de acceso no autorizado desde {request.client.host}")
        return Response(
            content='{"detail": "No autorizado: X-API-Key invÃ¡lida o ausente"}', 
            status_code=401, 
            media_type="application/json"
        )

    # Si la validaciÃ³n es correcta, pasamos la peticiÃ³n al siguiente nivel (proxy)
    return await call_next(request)

@app.get("/health")
def health():
    """Endpoint de diagnÃ³stico."""
    return {"status": "ok", "gateway": "activo"}

@app.api_route("/orders/{path_name:path}", methods=["GET", "POST", "PUT", "DELETE"])
async def proxy_orders(request: Request, path_name: str):
    """Redirección hacia el microservicio de pedidos."""
    # path_name vendrá vacío si se pide /orders/ o traerá ej. "health"
    # El backend espera /orders o /orders/health
    target_path = f"orders/{path_name}" if path_name else "orders"
    target_url = f"{ORDERS_URL}/{target_path}"
    logger.info(f"Proxy: Forwarding a Pedidos -> {target_url}")
    return await _proxy(target_url, request)

@app.api_route("/inventory/{path_name:path}", methods=["GET", "POST", "PUT", "DELETE"])
async def proxy_inventory(request: Request, path_name: str):
    """Redirección hacia el microservicio de inventario."""
    # El backend espera /inventory/{id} o /inventory/health
    target_path = f"inventory/{path_name}" if path_name else "inventory"
    target_url = f"{INVENTORY_URL}/{target_path}"
    logger.info(f"Proxy: Forwarding a Inventario -> {target_url}")
    return await _proxy(target_url, request)

async def _proxy(url: str, request: Request):
    """Motor de Proxy Inverso."""
    method = request.method
    # Filtramos la cabecera 'host' para que el servidor interno no reciba el host original
    # y asÃ­ evitar problemas de resoluciÃ³n de nombres dentro de la red de K8s.
    headers = {k: v for k, v in request.headers.items() if k.lower() != 'host'}
    # Leemos el cuerpo de la peticiÃ³n (necesario para POST/PUT)
    body = await request.body()
    
    try:
        # Re-enviamos la peticiÃ³n al servicio interno con todos los parÃ¡metros originales
        async with httpx.AsyncClient() as client:
            resp = await client.request(
                method, url, 
                headers=headers, 
                content=body, 
                params=request.query_params,
                timeout=10 # Tiempo de espera para evitar bloqueos infinitos
            )
        # Devolvemos la respuesta del servicio interno tal cual al cliente original
        content = resp.content
        status_code = resp.status_code
        headers = dict(resp.headers)

        # Si hay una redirecciÃ³n, reescribimos la cabecera Location para que apunte al Gateway
        if status_code in (301, 302, 303, 307, 308) and "location" in headers:
            loc = headers["location"]
            if ORDERS_URL in loc:
                headers["location"] = loc.replace(ORDERS_URL, str(request.base_url) + "orders")
            elif INVENTORY_URL in loc:
                headers["location"] = loc.replace(INVENTORY_URL, str(request.base_url) + "inventory")

        return Response(
            content=content, 
            status_code=status_code, 
            headers=headers
        )
    except Exception as e:
        # Capturamos fallos de red y devolvemos un error 502 (Bad Gateway)
        logger.error(f"Falla SistÃ©mica: Error de red al contactar con servicio interno: {e}")
        raise HTTPException(
            status_code=502, 
            detail="Error de comunicaciÃ³n con el servicio de backend (servicio inalcanzable)."
        )
