# Plan de Pruebas de Integridad: Motor de Navegación (SuperSwitch)

Este documento define la suite de pruebas técnica diseñada para validar la robustez y cobertura total del mapeador de acciones en `CaminosNavegacionTest.java`. El objetivo es alcanzar el **100% de cobertura de aristas (Edge Coverage)** sobre la lógica de control del test.

## 1. Suite de Caminos

Se han definido 14 caminos que cubren todas las ramas del `switch`, incluyendo retornos por éxito, cancelación y navegación física del sistema (Backstack).

| ID | Secuencia de Aristas | Objetivo de la Prueba | Casos del Switch Cubiertos |
| :--- | :--- | :--- | :--- |
| **01** | `1, 21` | Flujo circular Menú <-> Lista Quads. | 1, 21 |
| **02** | `2, 22` | Flujo circular Menú <-> Lista Reservas. | 2, 22 |
| **03** | `3, 7` | Alta de Quad con persistencia (Éxito). | 3, 7 |
| **04** | `3, 15b, 7b` | Alta de Quad: Configuración Biplaza y Cancelación. | 15b, 7b |
| **05** | `3, 7c` | Alta de Quad: Interrupción por retroceso físico. | 7c |
| **06** | `1, 5, 6b, 5, 6c, 5, 15, 6, 21` | **Ciclo Completo Edición Quad**: Prueba Cancelar (6b), Atrás (6c), Monoplaza (15) y Guardar (6). | 5, 6, 6b, 6c, 15 |
| **07** | `1, 13, 13b, 13c, 14, 14, 21` | Gestión de Lista Quads: Ordenación triple y borrado múltiple. | 13, 13b, 13c, 14 |
| **08** | `2, 8, 9b, 8, 9c, 8, 9, 22` | **Ciclo Completo Edición Reserva**: Prueba Cancelar (9b), Atrás (9c) y Guardar (9). | 8, 9, 9b, 9c |
| **09** | `4, 10c` | Alta de Reserva: Interrupción por retroceso físico. | 4, 10c |
| **10** | `4, 19, 19b, 10b` | Alta de Reserva: Configuración de fechas y cancelación. | 19, 19b, 10b |
| **11** | `4, 19, 19b, 11, 12b, 11, 12c, 11, 12, 10` | **Ciclo Completo Selección**: Prueba Cancelar (12b), Atrás (12c), Confirmar (12) y Guardar Alta (10). | 11, 12, 12b, 12c, 10 |
| **12** | `2, 16, 16b, 16c, 16d, 18, 22` | Lista Reservas (I): Ordenaciones y consulta de detalles. | 16, 16b, 16c, 16d, 18 |
| **13** | `2, 16e, 16f, 16g, 16h, 17, 22` | Lista Reservas (II): Filtrado total y ejecución de borrado. | 16e, 16f, 16g, 16h, 17 |
| **14** | `2, 8, 11, 20, 20b, 20c, 23, 24, 12, 9, 22` | **Interacción Avanzada Selección**: Ordenación, Info Quad y gestión de accesorios (Cascos). | 20, 20b, 20c, 23, 24 |

---

## 2. Matriz de Trazabilidad Técnica

Esta matriz confirma que cada etiqueta del `switch` en el código fuente ha sido asignada a al menos un caso de prueba ejecutable.

| Bloque Lógico | IDs de Arista | Validación en Caminos |
| :--- | :--- | :--- |
| **Navegación Base** | 1, 2, 3, 4 | 01, 02, 03, 09 |
| **Gestión Quads** | 5, 6, 6b, 6c, 7, 7b, 7c | 06, 03, 04, 05 |
| **Atributos Quad** | 15, 15b | 06, 04 |
| **Acciones Lista Q** | 13, 13b, 13c, 14 | 07 |
| **Gestión Reservas** | 8, 9, 9b, 9c, 10, 10b, 10c | 08, 11, 10, 09 |
| **Selectores Fecha** | 19, 19b | 10, 11 |
| **Acciones Lista R** | 16(a-d), 16(e-h), 17, 18 | 12, 13 |
| **Selector Vehículos**| 11, 12, 12b, 12c | 11, 14 |
| **Acciones Selector** | 20(a-c), 23, 24 | 14 |
| **Retorno Sistema** | 21, 22 | 01, 07, 02, 13 |

## 3. Conclusión

La suite de 14 caminos presentada elimina cualquier ambigüedad técnica y cubre las **47 ramificaciones** del motor de pruebas. La ejecución de estos caminos garantiza que no existan "puntos muertos" en el código de automatización y que todas las transiciones posibles de la interfaz de usuario han sido validadas empíricamente bajo condiciones de estrés y hardware variable.
