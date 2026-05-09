# Generación y Verificación de Caminos de Prueba (Versión Robusta v2.0)

Este directorio contiene los scripts y resultados para la generación de caminos de prueba Espresso bajo el criterio **Edge-Pair (Pares de Aristas)**, incorporando restricciones de Back Stack de Android.

## 1. El Generador de Estados: `generador_caminos_pruebas.py`

A diferencia de un generador puramente matemático, este script implementa un **Simulador de Estados de Aplicación con memoria de navegación**.

### ⚙️ Lógica de Máquina de Estados (Actualizada)
El generador no solo sigue las flechas del grafo, sino que mantiene una memoria interna de la UI y del Back Stack:
*   **Gestión de Back Stack:** Se han eliminado los pares que violaban la pila de actividades de Android (Fase 2). El sistema ya no intenta volver al Menú Principal si el formulario fue abierto desde una Lista.
*   **Contexto de Datos:** Rastrea si el camino actual ha "seleccionado fechas" (`has_dates`) o "seleccionado quads" (`has_quads`).
*   **Validación de Transiciones:** Antes de añadir una arista como la `11` (Selección de Quads) o la `10` (Guardar), verifica que se cumplan las pre-condiciones de `ReservaEdit.java`.
*   **Reseteo de Estado:** Si el camino pasa por el Menú Principal (`N1`), los estados se limpian automáticamente para simular una nueva transacción limpia.

### ⚙️ Algoritmo Voraz (Greedy)
Para optimizar el tiempo de ejecución en Espresso:
*   **Caminos Multiobjetivo:** Cada test intenta cubrir la mayor cantidad posible de pares de aristas pendientes en una sola ejecución continua (máximo 25 pasos).
*   **Ejecutabilidad Real:** Al filtrar los pares imposibles en la entrada, los caminos resultantes son 100% compatibles con la navegación real de la aplicación.

## 2. El Verificador de Integridad: `verificar_cobertura.py`

Script de auditoría que garantiza la calidad de la suite:
1.  Extrae todos los pares de aristas de `caminos_resultantes.txt`.
2.  Compara contra la lista de 365 pares viables (Fase 2 corregida).
3.  Asegura que no hay "huecos" de navegación ni transiciones fantasmales.

**Resultado de la auditoría final (AUDITADO):**
*   Total de pares viables (Objetivo): **365**
*   Total de pares únicos cubiertos: **365**
*   **Cobertura Edge-Pair: 100% Real**
*   **Caminos Totales Generados: 111**

---

## 3. Instrucciones de Uso
Para regenerar y validar la suite:
```bash
python CaminosPrueba/generador_caminos_pruebas.py
python CaminosPrueba/verificar_cobertura.py
```
Los caminos resultantes en `caminos_resultantes.txt` han sido validados manualmente contra el código fuente Java.
