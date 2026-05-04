import requests
import json
import time

# ConfiguraciÃ³n
BASE_URL = "http://localhost:8000"
API_KEY = "mi_clave_secreta"
HEADERS = {"X-API-Key": API_KEY, "Content-Type": "application/json"}

def test_health():
    print("--- Probando Salud de los Servicios ---")
    # Usamos endpoints que el Gateway pueda mapear fácilmente a los /health internos
    endpoints = {
        "Gateway": "/health",
        "Pedidos": "/orders/health",
        "Inventario": "/inventory/health"
    }
    for name, ep in endpoints.items():
        try:
            resp = requests.get(f"{BASE_URL}{ep}", headers=HEADERS)
            print(f"GET {ep} ({name}): {resp.status_code} - {resp.json()}")
        except Exception as e:
            print(f"ERROR en {ep}: {e}")

def test_purchase():
    print("\n--- Probando Flujo de Compra (Sincrónico + Atómico) ---")
    product_id = 1
    quantity = 1

    # 1. Consultar stock inicial -> GET /inventory/1
    # El gateway lo mandará a inventory-service:8000/inventory/1
    resp = requests.get(f"{BASE_URL}/inventory/{product_id}", headers=HEADERS)
    if resp.status_code != 200:
        print(f"FALLO al consultar stock: {resp.status_code} - {resp.text}")
        return

    initial_stock = resp.json().get("stock")
    print(f"Stock inicial del producto {product_id}: {initial_stock}")

    # 2. Realizar pedido -> POST /orders
    # El gateway lo mandará a orders-service:8000/orders
    order_data = {
        "product_id": product_id,
        "quantity": quantity,
        "customer_name": "Tester de Auditoria"
    }
    print(f"Enviando pedido: {order_data}")
    resp = requests.post(f"{BASE_URL}/orders", headers=HEADERS, json=order_data)

    if resp.status_code == 200:
        print(f"ÉXITO: {resp.json()}")
        # 3. Verificar stock final
        resp = requests.get(f"{BASE_URL}/inventory/{product_id}", headers=HEADERS)
        final_stock = resp.json().get("stock")
        print(f"Stock final: {final_stock}")
        if final_stock == initial_stock - quantity:
            print("VERIFICACIÓN: El stock se descontó correctamente.")
        else:
            print("VERIFICACIÓN: ¡ERROR! El stock no coincide.")
    else:
        print(f"FALLO en el pedido: {resp.status_code} - {resp.text}")

def test_insufficient_stock():
    print("\n--- Probando ValidaciÃ³n de Stock Insuficiente ---")
    order_data = {
        "product_id": 2,
        "quantity": 1000, # Mucho mÃ¡s de lo que hay
        "customer_name": "Tester Ambicioso"
    }
    resp = requests.post(f"{BASE_URL}/orders/", headers=HEADERS, json=order_data)
    print(f"Respuesta (esperada 400): {resp.status_code} - {resp.json()}")

if __name__ == "__main__":
    print("Iniciando pruebas de integraciÃ³n...")
    test_health()
    test_purchase()
    test_insufficient_stock()
