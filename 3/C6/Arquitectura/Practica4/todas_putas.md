# 🛑 Informe de Auditoría Técnica: Prácticas 3 & 4 (Arquitectura Software)

Este documento representa un análisis quirúrgico de los fallos detectados en el sistema de Broker de Mensajería (P3) y el ecosistema de Microservicios (P4). Diseñado para eliminar cualquier ambigüedad en la refactorización.

---

## 🏗️ PRÁCTICA 3: Broker de Mensajería (MOM)

### 1. El fallo de la Persistencia Selectiva (Data Wipe)
*   **Mecánica del error:** El servidor utiliza *Lazy Loading* en `get_queue()`. Al arrancar, el diccionario `queues` está vacío `{}`. Si un cliente interactúa con la cola "A", el servidor la carga. Al ejecutarse el primer `save_broker_state()`, el sistema guarda **solo lo que hay en memoria**.
*   **Traceback Lógico:**
    1.  Disco contiene: `{"A": [...], "B": [...]}`.
    2.  Petición: `publicar("A", "dato")`.
    3.  Memoria del servidor tras la petición: `{"A": ["dato"]}` (Cola "B" ignorada).
    4.  Persistencia: `storage.save_state` sobrescribe el archivo con el contenido de la memoria.
    5.  **Resultado:** El archivo en disco ahora solo tiene la cola "A". **La cola "B" y todos sus mensajes han sido eliminados permanentemente.**
*   **Solución Técnica:** Implementar un **Bootstrap Completo** al inicio del `main()` que lea el archivo JSON íntegro y pueble el diccionario `queues` antes de aceptar conexiones.

### 2. El "Agujero Negro" de mensajes Unacked (Pérdida de Datos)
*   **Mecánica del error:** El método `dispatch_messages` extrae el mensaje de la lista persistente (`pop(0)`) y lo mueve a un diccionario volátil (`unacked`) en RAM.
*   **Traceback Lógico:**
    1.  Estado en disco: Mensaje ID:101 está en la lista `messages`.
    2.  Acción: Se despacha al consumidor. Se borra de `messages` y se pone en `unacked`.
    3.  Persistencia: Se guarda el estado en disco. Como `unacked` no se persiste, el mensaje 101 desaparece del archivo.
    4.  Evento: Crash del servidor Broker.
    5.  **Resultado:** Al reiniciar, el mensaje 101 se ha perdido aunque nunca fue confirmado (ACK).
*   **Solución Técnica:** Rediseñar el esquema de persistencia para incluir una lista de mensajes en vuelo: `{"nombre_cola": {"messages": [], "unacked": []}}`.

### 3. El problema del "Fair Dispatch" Infinito
*   **Mecánica del error:** El broker usa `prefetch_count=1`. Si un consumidor recibe un mensaje pero se queda bloqueado (por ejemplo, esperando una DB externa) y no envía el ACK, el broker no le enviará más mensajes pero tampoco liberará el actual.
*   **Traceback Lógico:**
    1.  Consumidor recibe mensaje, socket sigue abierto.
    2.  Consumidor entra en bucle infinito o latencia extrema.
    3.  El mensaje permanece en `unacked` indefinidamente.
    4.  **Resultado:** Si es el único consumidor, el procesamiento de la cola se detiene totalmente (*Starvation*).
*   **Solución Técnica:** Implementar un **ACK Timeout**. Si un mensaje en `unacked` no recibe confirmación en N segundos, debe ser devuelto a la cola `messages` automáticamente.

---

## 🚀 PRÁCTICA 4: Orquestación de Microservicios

### 1. El "Starvation" del Gateway por I/O síncrono
*   **Mecánica del error:** FastAPI es asíncrono, pero el código usa la librería `requests` (síncrona) dentro de funciones `async def`.
*   **Análisis de Red:** Cuando se ejecuta `requests.get()`, el hilo principal de ejecución se detiene bloqueando el *Event Loop* de FastAPI mientras espera los paquetes TCP de vuelta.
*   **Impacto:** Si el servicio de inventario tarda 5 segundos, el Gateway queda **totalmente congelado** y no puede procesar ninguna otra petición de otros usuarios durante ese tiempo.
*   **Solución Técnica:** Migrar a **`httpx.AsyncClient()`**. Esto permite realizar llamadas no bloqueantes, liberando el hilo del Gateway para atender a otros clientes mientras espera la respuesta del backend.

### 2. El fallo de Atomicidad Distribuida (Saga Incompleta)
*   **Mecánica del error:** El flujo de compra es lineal: `Restar Stock (Postgres) -> Guardar Pedido (MariaDB)`. No hay gestión de fallos cruzada.
*   **Traceback de Error:**
    1.  `inventory-service` descuenta 1 unidad (Éxito en Postgres).
    2.  `orders-service` intenta guardar en MariaDB, pero hay un error de conexión o timeout.
    3.  `orders-service` responde Error 500 al cliente.
    4.  **Resultado:** El cliente no tiene su pedido, pero el inventario ha perdido una unidad. El stock está descuadrado.
*   **Solución Técnica:** Implementar una **Transacción de Compensación (Patrón Saga)**. El bloque `except` de la persistencia en MariaDB debe enviar automáticamente una petición al Inventario para sumar (+1) la unidad previamente restada.

### 3. Deadlock en el Despliegue de Bases de Datos
*   **Mecánica del error:** Se usa `kind: Deployment` para bases de datos con volúmenes `ReadWriteOnce` (RWO).
*   **Conflicto de K8s:** Durante un `RollingUpdate`, Kubernetes intenta arrancar el Pod V2 antes de apagar el Pod V1. El almacenamiento RWO rechaza al Pod V2 porque el V1 aún tiene bloqueado el volumen.
*   **Resultado:** El Pod V2 queda en `ContainerCreating` y el Pod V1 nunca se apaga. El sistema queda bloqueado en un bucle de despliegue fallido.
*   **Solución Técnica:** Cambiar el tipo de recurso de `Deployment` a **`StatefulSet`**. Esto garantiza que K8s apague ordenadamente el pod actual antes de intentar montar el volumen en el nuevo.

### 4. Latencia Innecesaria (Anti-patrón de Validación)
*   **Mecánica del error:** El `orders-service` hace un `GET` para ver si hay stock y luego un `PUT` para restarlo.
*   **Análisis de Concurrencia:** Entre el `GET` y el `PUT`, otro proceso puede haber agotado el stock. El `GET` inicial no garantiza nada y añade un salto de red (latencia) innecesario.
*   **Solución Técnica:** Realizar directamente la operación **atómica** (`PUT`). El `inventory-service` ya implementa `FOR UPDATE` y lógica de validación; si no hay stock, responderá un error 400 que el orquestador puede manejar en un solo paso.

---

**Veredicto Final:** El sistema actual es "funcional" solo bajo condiciones ideales. Ante fallos de red, reinicios de pod o carga concurrente, el sistema sufrirá corrupción de datos e indisponibilidad.
