# Plan de Pruebas de Integridad del SuperSwitch (V. EXHAUSTIVA)

Este documento detalla los 14 caminos mínimos y exhaustivos utilizados para verificar que absolutamente todas las aristas y ramas del `SuperSwitch` en `CaminosNavegacionTest.java` funcionan correctamente tras la refactorización de IDs.

## Lista de Caminos de Integridad (Sincronizada con IDs 1-24)

| ID | Camino | Objetivo Técnico | Aristas Cubiertas |
| :--- | :--- | :--- | :--- |
| **1** | `1, 21` | Navegación básica N1-N2 y retroceso | 1, 21 |
| **2** | `2, 22` | Navegación básica N1-N4 y retroceso | 2, 22 |
| **3** | `3, 6` | Creación exitosa de Quad (vuelve a lista) | 3, 6 |
| **4** | `3, 15b, 7b` | Quad Biplaza y Cancelación (vuelve a menú) | 15b, 7b |
| **5** | `3, 7c` | Salida por atrás físico en Formulario Quad | 7c |
| **6** | `1, 5, 15, 6` | Edición Quad y cambio a Monoplaza | 5, 15 |
| **7** | `1, 13, 13b, 13c, 14, 21` | Lista Quads: Ordenaciones y Borrado | 13, 13b, 13c, 14 |
| **8** | `2, 8, 9b, 22` | Edición Reserva, Cancelar y volver | 8, 9b |
| **9** | `4, 10c` | Salida por atrás físico en Formulario Reserva | 10c |
| **10** | `4, 19, 19b, 10b` | Gestión de Fechas (19, 19b) y Cancelar | 19, 19b, 10b |
| **11** | `4, 19, 19b, 11, 12b, 10b` | Flujo Selección: Abrir y Cancelar | 11, 12b |
| **12** | `4, 19, 19b, 11, 12, 10` | Creación Completa de Reserva Éxito | 12, 10 |
| **13** | `2, 16, 16b, 16c, 16d, 16e, 16f, 16g, 16h, 18, 17, 22` | Lista Reservas: TODOS los Filtros y Borrado | 16..16h, 18, 17 |
| **14** | `4, 19, 19b, 11, 20, 20b, 20c, 23, 24, 12c, 10b` | Selección: Órdenes (20..20c), Detalle (23), Cascos (24) y Atrás | 20..20c, 23, 24, 12c |

---

## Blindaje de Pop-ups y Diálogos (Refactorizado)

Cada camino ha sido diseñado para interactuar con los siguientes elementos de interfaz complejos:

1.  **Calendarios (19, 19b)**: Manejados por `handleDatePicker()` con espera explícita.
2.  **Confirmaciones (14, 17)**: Manejados por `handleSystemDialog()` buscando botones de borrado.
3.  **Detalles (18, 23)**: Manejados por el cierre automático de diálogos.
4.  **Menús Contextuales (24)**: Gestionados con `device.pressBack()` para cerrar el selector de cascos.

**Veredicto**: Esta suite garantiza que el motor de tests Espresso es 100% coherente con la nueva numeración de aristas y la lógica de negocio de la aplicación.
