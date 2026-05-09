# Generación y Verificación de Caminos de Prueba (Versión Robusta)

Este directorio contiene los scripts y resultados para la generación de caminos de prueba Espresso bajo el criterio **Edge-Pair (Pares de Aristas)**.

## 1. El Generador de Estados: `generador_caminos_pruebas.py`

A diferencia de un generador puramente matemático, este script implementa un **Simulador de Estados de Aplicación**.

### ⚙️ Lógica de Máquina de Estados
El generador no solo sigue las flechas del grafo, sino que mantiene una memoria interna de la UI:
*   **Contexto de Datos:** Rastrea si el camino actual ha "seleccionado fechas" (`has_dates`) o "seleccionado quads" (`has_quads`).
*   **Validación de Transiciones:** Antes de añadir una arista como la `11` (Selección de Quads) o la `10` (Guardar), verifica que se cumplan las pre-condiciones de `ReservaEdit.java`.
*   **Reseteo de Estado:** Si el camino pasa por el Menú Principal (`N1`), los estados se limpian automáticamente para simular una nueva transacción limpia.

### ⚙️ Algoritmo Voraz (Greedy)
Para optimizar el tiempo de ejecución en Espresso:
*   **Caminos Multiobjetivo:** Cada test intenta cubrir la mayor cantidad posible de pares de aristas pendientes en una sola ejecución continua (máximo 25 pasos).
*   **Eficiencia:** Se ha reducido la suite de pruebas a **102 caminos ejecutables**, minimizando el número de arranques de la aplicación.

## 2. El Verificador de Integridad: `verificar_cobertura.py`

Script de auditoría que garantiza la calidad de la suite:
1.  Extrae todos los pares de aristas de `caminos_resultantes.txt`.
2.  Compara contra la lista blanca de pares viables.
3.  Asegura que no hay "huecos" de navegación.

**Resultado de la auditoría final:**
*   Total de pares viables analizados: 377
*   Total de pares únicos cubiertos: 377
*   **Cobertura Edge-Pair: 100%**
*   **Transiciones Inválidas (Bloqueos de UI): 0**

---

## 3. Instrucciones de Uso
Para regenerar y validar la suite:
```bash
python CaminosPrueba/generador_caminos_pruebas.py
python CaminosPrueba/verificar_cobertura.py
```
Los caminos resultantes en `caminos_resultantes.txt` están listos para ser implementados como `@Test` en la clase de pruebas de Espresso.
