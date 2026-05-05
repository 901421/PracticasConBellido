import requests
import json
import time
import threading
from concurrent.futures import ThreadPoolExecutor

# Configuración
BASE_URL = "http://localhost:8000"
API_KEY = "mi_clave_secreta"
HEADERS = {"X-API-Key": API_KEY, "Content-Type": "application/json"}

def test_security():
    print("\n--- [TEST SEGURIDAD] Verificando protección del Gateway ---")
    
    # 1. Petición sin cabecera
    resp = requests.get(f"{BASE_URL}/orders/health")
    print(f"Sin cabecera (esperado 401): {resp.status_code}")
    
    # 2. Petición con clave incorrecta
    bad_headers = {"X-API-Key": "clave_falsa"}
    resp = requests.get(f"{BASE_URL}/orders/health", headers=bad_headers)
    print(f"Clave incorrecta (esperado 401): {resp.status_code}")
    
    # 3. Petición a /health (debe ser libre)
    resp = requests.get(f"{BASE_URL}/health")
    print(f"Endpoint libre /health (esperado 200): {resp.status_code}")

def test_concurrency():
    print("\n--- [TEST CONCURRENCIA] Venta masiva simultánea (Race Condition Check) ---")
    product_id = 2 # Monitor 4K (Stock inicial: 5)
    threads = 10   # 10 personas intentando comprar a la vez
    
    # Aseguramos stock conocido
    requests.put(f"{BASE_URL}/inventory/{product_id}", headers=HEADERS, json={"cantidad": 100}) # Reponemos mucho
    resp = requests.get(f"{BASE_URL}/inventory/{product_id}", headers=HEADERS)
    current_stock = resp.json()["stock"]
    print(f"Stock preparado para la prueba: {current_stock}")
    
    # Queremos dejar el stock en exactamente 3 unidades para la prueba de estrés
    # Así que vaciamos hasta que queden 3.
    requests.put(f"{BASE_URL}/inventory/{product_id}", headers=HEADERS, json={"cantidad": -current_stock + 3})
    print(f"Stock ajustado para la carrera: 3 unidades.")

    def place_order(i):
        order_data = {
            "product_id": product_id,
            "quantity": 1,
            "customer_name": f"Comprador_Veloz_{i}"
        }
        return requests.post(f"{BASE_URL}/orders", headers=HEADERS, json=order_data)

    print(f"Lanzando {threads} peticiones simultáneas...")
    with ThreadPoolExecutor(max_workers=threads) as executor:
        results = list(executor.map(place_order, range(threads)))

    success = [r for r in results if r.status_code == 200]
    failed = [r for r in results if r.status_code == 400]
    
    print(f"Resultados: {len(success)} compras exitosas, {len(failed)} fallidas por falta de stock.")
    
    # Verificación final de stock
    resp = requests.get(f"{BASE_URL}/inventory/{product_id}", headers=HEADERS)
    final_stock = resp.json()["stock"]
    print(f"Stock final en base de datos: {final_stock}")
    
    if len(success) == 3 and final_stock == 0:
        print("VERIFICACIÓN: ¡ÉXITO! El bloqueo 'FOR UPDATE' evitó el sobre-stock negativo.")
    else:
        print("VERIFICACIÓN: ¡FALLO! El stock es inconsistente.")

def test_saga_compensation():
    print("\n--- [TEST SAGA] Verificando Compensación por fallo en DB de Pedidos ---")
    product_id = 1
    
    # Consultamos stock inicial
    resp = requests.get(f"{BASE_URL}/inventory/{product_id}", headers=HEADERS)
    initial_stock = resp.json()["stock"]
    print(f"Stock inicial del producto {product_id}: {initial_stock}")

    # Provocamos un fallo en MariaDB enviando un nombre de cliente nulo o vacío
    # El servicio de pedidos fallará al persistir, disparando la compensación.
    # Nota: Depende de si la DB acepta nulos, pero el código de Pedidos lanzará excepción si MariaDB falla por cualquier motivo.
    # Vamos a forzar un error de integridad (un string demasiado largo o algo que rompa el INSERT)
    bad_order = {
        "product_id": product_id,
        "quantity": 1,
        "customer_name": "A" * 500 # MariaDB suele tener límite 255 en VARCHAR. Esto debería romper el INSERT.
    }
    
    print("Enviando pedido malformado para forzar fallo en DB local...")
    resp = requests.post(f"{BASE_URL}/orders", headers=HEADERS, json=bad_order)
    
    print(f"Respuesta del servicio (esperado 500): {resp.status_code}")
    if resp.status_code == 500:
        print(f"Mensaje de error: {resp.json()['detail']}")
        
        # VERIFICACIÓN MAESTRA: El stock debe ser igual al inicial
        # Si la compensación funcionó, el stock se restó y se volvió a sumar.
        time.sleep(1) # Esperamos un poco para asegurar que la compensación terminó
        resp = requests.get(f"{BASE_URL}/inventory/{product_id}", headers=HEADERS)
        final_stock = resp.json()["stock"]
        print(f"Stock tras compensación: {final_stock}")
        
        if final_stock == initial_stock:
            print("VERIFICACIÓN: ¡ÉXITO! El Patrón Saga restauró el stock correctamente.")
        else:
            print("VERIFICACIÓN: ¡FALLO! El stock quedó inconsistente (no se restauró).")

if __name__ == "__main__":
    print("=== INICIANDO SUITE DE PRUEBAS DE INTEGRIDAD Y ARQUITECTURA ===")
    
    try:
        test_security()
        test_concurrency()
        test_saga_compensation()
    except Exception as e:
        print(f"\n[!] Error catastrófico durante los tests: {e}")
    
    print("\n=== FIN DE LAS PRUEBAS ===")
