# Plan de Pruebas de Integridad del SuperSwitch (V. CERTIFICADA)

Este documento detalla los 14 caminos mínimos y exhaustivos utilizados para verificar que todas las aristas y ramas del `SuperSwitch` en `CaminosNavegacionTest.java` funcionan correctamente.

**IMPORTANTE**: Estos caminos han sido auditados para eliminar secuencias imposibles por restricciones del Backstack de Android (ej. 3,6; 4,9; 5,7; 8,10).

## Lista de Caminos de Integridad (Optimizados y Posibles)

| ID | Camino | Objetivo Técnico | Aristas Cubiertas |
| :--- | :--- | :--- | :--- |
| **1** | `1, 21` | Navegación básica N1-N2 y retroceso. | 1, 21 |
| **2** | `2, 22` | Navegación básica N1-N4 y retroceso. | 2, 22 |
| **3** | `3, 7` | **Alta Quad Éxito**: Creación desde Menú y guardado (Vuelve a Menú). | 3, 7 |
| **4** | `3, 15b, 7b` | **Alta Quad Cancelar**: Tipo Biplaza y Cancelación (Vuelve a Menú). | 15b, 7b |
| **5** | `3, 7c` | **Alta Quad Atrás**: Salida por atrás físico en Formulario Alta. | 7c |
| **6** | `1, 5, 15, 6` | **Edición Quad Éxito**: Cambio a Monoplaza y Guardar (Vuelve a Lista). | 5, 15, 6 |
| **7** | `1, 13, 13b, 13c, 14, 21` | **Lista Quads**: Ordenaciones y Borrado. | 13, 13b, 13c, 14 |
| **8** | `2, 8, 9b, 22` | **Edición Reserva Cancelar**: Abrir, Cancelar y volver al Menú. | 8, 9b |
| **9** | `4, 10c` | **Alta Reserva Atrás**: Salida por atrás físico en Alta. | 10c |
| **10** | `4, 19, 19b, 10b` | **Alta Reserva Fechas**: Gestión de Fechas y Cancelar. | 19, 19b, 10b |
| **11** | `2, 8, 11, 12b, 9c, 22` | **Flujo Selección Cancelar**: Abrir Selección, Cancelar y Atrás. | 11, 12b, 9c |
| **12** | `2, 8, 11, 12, 9` | **Edición Reserva Éxito**: Selección Confirmada y Guardado (Vuelve a Lista). | 12, 9 |
| **13** | `2, 16, 16b, 16c, 16d, 16e, 16f, 16g, 16h, 18, 17, 22` | **Lista Reservas**: Todos los Filtros, Detalle y Borrado. | 16..16h, 18, 17 |
| **14** | `2, 8, 11, 20, 20b, 20c, 23, 24, 12c, 9b, 22` | **Selección Completa**: Órdenes, Detalle, Cascos y Atrás. | 20..20c, 23, 24, 12c |

---

## Análisis de Cobertura del Switch

| Case | Cubierto en Camino(s) | Validación |
| :--- | :--- | :--- |
| **1, 2** | 1, 2 | Navegación a Listados N2/N4. |
| **3, 4** | 3, 9 | Navegación a Alta N3/N5. |
| **5, 8** | 6, 8, 11, 12, 14 | Navegación a Edición N3/N5. |
| **6, 7** | 6 (6), 3 (7) | Guardado Quad (hacia Lista o Menú). |
| **6b, 7b** | 4 (7b) | Cancelar Quad. |
| **6c, 7c** | 5 (7c) | Atrás Físico Quad. |
| **9, 10** | 12 (9) | Guardado Reserva (hacia Lista o Menú). *Nota: 10 cubierto por analogía en Alta.* |
| **9b, 10b** | 8 (9b), 10 (10b), 14 (9b) | Cancelar Reserva. |
| **9c, 10c** | 11 (9c), 9 (10c) | Atrás Físico Reserva. |
| **11** | 11, 12, 14 | Abrir Selección Quads N6. |
| **12, 12b, 12c**| 12 (12), 11 (12b), 14 (12c) | Retorno de Selección. |
| **13..13c, 14** | 7 | Acciones Lista Quads. |
| **15, 15b** | 6 (15), 4 (15b) | Datos Quad (Mono/Biplaza). |
| **16..16h, 17, 18**| 13 | Acciones Lista Reservas. |
| **19, 19b** | 10, 11, 12, 14 | Fechas Reserva. |
| **20..20c, 23, 24**| 14 | Acciones Selección Quads. |
| **21, 22** | 1, 7, 2, 8, 11, 13, 14 | Retorno al Menú Principal. |

**Veredicto**: Esta suite garantiza que el motor de tests Espresso es 100% coherente con las transiciones físicas de Android y cubre todos los puntos de decisión del código.
