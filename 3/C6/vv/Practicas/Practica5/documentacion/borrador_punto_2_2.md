# Borrador Punto 2.2: Identificación de Diferencias entre Diseño y Tests

Este documento contiene el análisis comparativo entre las pruebas diseñadas en la hoja de cálculo (`pruebas-kata-Gilded-Rose.xlsx`) y la implementación original de la clase `InventoryTest.java` en el módulo `GildedRose0` (hasta la línea 136).

## 1. Pruebas existentes en `InventoryTest` que NO están en la hoja de cálculo
Tras analizar el código fuente original, se observa que todos los métodos de prueba implementados tienen una correspondencia lógica con algún escenario de la hoja de cálculo. Sin embargo, la **estructura** de los tests originales es más granular que el diseño del Excel:

*   **Fragmentación por atributo:** Mientras que en el Excel una única prueba (ej. P9) define resultados esperados tanto para `sellIn` como para `quality`, el código fuente original a menudo separa estas validaciones en dos métodos distintos (ej. `should_lower_the_sellIn_by_one_for_normal_items` y `should_lower_the_quality_by_one_for_normal_items`).
*   **Tests de Límite Mínimo (Normal Item):** El test `should_not_lower_the_quality_below_zero` (P9b) estaba implementado en el código, validando explícitamente que la calidad no sea negativa, un caso de borde fundamental.

## 2. Pruebas de la hoja de cálculo que NO están implementadas en `InventoryTest` (Original)
Se identificaron numerosos casos diseñados en el Excel que no contaban con un test automatizado en la versión inicial:

*   **Aged Brie (P1, P1b, P2, P2b, P2c):** Ninguna de las variaciones del artículo "Aged Brie" (aumento normal, límite de calidad 50, aumento tras fecha de venta) estaba implementada.
*   **Backstage Passes (P4 a P8):** Aunque es uno de los artículos más complejos, la versión original carecía de tests para los incrementos de calidad (+1, +2, +3) según los días restantes, el límite de 50 y la caída a 0 tras el concierto.
*   **Artículos Normales Expirados (P10, P10b, P10c):** No había validaciones para el comportamiento de artículos normales una vez que `sellIn < 0` (decrecimiento doble de calidad).
*   **Valores Límite de Frontera (P2d, P5d, P10d):** Pruebas específicas para el valor `sellIn = 0` no estaban presentes originalmente.

---
*Nota: La columna "Test en InventoryTest" del archivo Excel estaba incompleta originalmente, conteniendo únicamente referencias para las pruebas P3 y P9 (sellIn).*
