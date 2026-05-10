# Plan de Pruebas de Integridad del SuperSwitch (V. EXHAUSTIVA)

Este documento detalla los 14 caminos mínimos y exhaustivos utilizados para verificar que absolutamente todas las aristas y ramas del `SuperSwitch` en `CaminosNavegacionTest.java` funcionan correctamente.

## Lista de Caminos de Integridad (Sincronizada con el Código)

| ID | Camino | Objetivo Técnico | Aristas Cubiertas |
| :--- | :--- | :--- | :--- |
| **1** | `1, 23` | Navegación básica N1-N2 | 1, 23 |
| **2** | `2, 24` | Navegación básica N1-N4 | 2, 24 |
| **3** | `3, 6` | Creación exitosa de Quad | 3, 6 |
| **4** | `3, 15b, 7b` | Quad Biplaza y Cancelación | 15b, 7b |
| **5** | `3, 7c` | Salida por atrás en Quad | 7c |
| **6** | `1, 5, 15, 6` | Edición Quad y cambio a Monoplaza | 5, 15 |
| **7** | `1, 13, 13b, 13c, 14, 23` | Lista Quads: Ordenaciones y Borrado | 13, 13b, 13c, 14 |
| **8** | `2, 8, 9b, 2, 19, 24` | Edición Reserva, Cancelar y Popup Envío | 8, 9b, 19 |
| **9** | `4, 10c` | Salida por atrás en Reserva | 10c |
| **10** | `4, 20, 20b, 10b` | Gestión de Fechas y Cancelar Creación | 20, 20b, 10b |
| **11** | `4, 20, 20b, 11, 12b, 10b` | Flujo Selección: Abrir y Cancelar | 11, 12b |
| **12** | `4, 20, 20b, 11, 12, 10` | Creación Completa de Reserva Éxito | 12, 10, 9 |
| **13** | `2, 16..16h, 18, 17, 24` | Lista Reservas: TODOS los Filtros y Borrado | 16, 16b..16h, 18, 17 |
| **14** | `4..11, 21..21c, 25, 26, 12c, 10b` | Selección: Órdenes, Detalles, Cascos y Atrás | 21, 21b, 21c, 25, 26, 12c |

---

## Blindaje de Pop-ups y Diálogos

Cada camino ha sido diseñado para interactuar con los siguientes elementos de interfaz complejos:

1.  **Calendarios (20, 20b)**: Manejados por `handleDatePicker()` con espera explícita a que el diálogo desaparezca.
2.  **Confirmaciones (14, 17)**: Manejados por `handleSystemDialog()` buscando botones "BORRAR" o "ACEPTAR".
3.  **Detalles (18, 25)**: Manejados por el cierre automático de diálogos mediante botón "CERRAR".
4.  **Menús Contextuales (19, 26)**: Gestionados con `device.pressBack()` para asegurar que la UI quede limpia tras la interacción.

**Veredicto**: Esta suite garantiza que si los 14 caminos pasan, el motor de tests está listo para ejecutar los 134 caminos de la práctica con seguridad total.
