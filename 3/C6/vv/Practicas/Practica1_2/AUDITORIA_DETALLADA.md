# 🛡️ INFORME DE AUDITORÍA TÉCNICA - PROYECTO M147_QUADS

**Estado Actual:** 6.5 / 10  
**Objetivo:** 10 / 10 (Nivel Excelencia)  
**Auditor:** Senior Android Architect & QA Auditor

---

## 1. RESUMEN DE HALLAZGOS CRÍTICOS

El proyecto tiene una arquitectura **MVVM** bien planteada y usa patrones avanzados como **Bridge** (para el envío de mensajes) y **Singleton** (Base de Datos). Sin embargo, falla en el cumplimiento de **Reglas de Negocio Contables** explícitas en la memoria:

1.  **Pérdida de Historial:** Se están usando borrados físicos (`DELETE`) en lugar de lógicos, incumpliendo los requisitos de auditoría contable.
2.  **Inestabilidad de Precios:** Los precios de las reservas no son fijos; si el precio de un quad cambia hoy, las reservas de hace un mes se alteran retroactivamente.
3.  **Dashboards Incompletos:** Falta el filtrado por estados (Previstas, Vigentes, Caducadas).
4.  **Error de Ordenación:** Las fechas en formato String `dd/MM/yyyy` no se ordenan bien en SQL (el día `02` siempre va antes del `10`, sin importar el año).

---

## 2. TABLA DE CUMPLIMIENTO DE REQUISITOS (RF)

| ID | Requisito | Estado | Acción | Tarea Técnica Específica |
| :--- | :--- | :---: | :--- | :--- |
| **RF 1** | Creación Quads (Matrícula/Regex) | ✅ | Ninguna | Ya implementado en `QuadRepository` y `QuadEdit`. |
| **RF 2** | Listar y ordenar Quads | ✅ | Ninguna | Ya implementado en `QuadListActivity`. |
| **RF 3** | Modificar Quads | ✅ | Ninguna | Ya implementado. |
| **RF 4** | **Baja de Quad (Borrado Lógico)** | 🔴 | **Añadir** | Añadir columna `activo` en `Quad.java`. Cambiar `@Delete` por `@Query(UPDATE...)` en `QuadDao.java`. |
| **RF 5** | Crear Reserva (Campos/Dates) | ✅ | Ninguna | Ya implementado en `ReservaEdit`. |
| **RF 6** | Validación (Solapes/Cascos) | ✅ | Ninguna | Lógica correcta en `QuadRepository.getAvailableQuadsSync`. |
| **RF 7** | Listar y ordenar Reservas | ✅ | Ninguna | Implementado en `ReservaListActivity`. |
| **RF 8** | Modificar Reserva | ✅ | Ninguna | Implementado borrando y recreando relaciones en `ReservaDao`. |
| **RF 9** | **Baja Reserva (Borrado Lógico)** | 🔴 | **Añadir** | Añadir columna `activo` en `Reserva.java`. Cambiar `@Delete` por `UPDATE` en `ReservaDao.java`. |
| **RF 10** | Cálculo Automático Precio | 🟡 | **Corregir** | Mover el cálculo de `ReservaListActivity` a `ReservaViewModel`. |
| **RF 11** | Envío Confirmación (Bridge) | ✅ | Ninguna | Excelente uso del patrón Bridge. |
| **RF 12** | **Inmutabilidad del Precio** | 🔴 | **Añadir** | Añadir campo `precio_total` en `Reserva.java` para guardar el coste fijo al crearla. |
| **RF 13** | **Filtros por Estado** | 🔴 | **Añadir** | Implementar UI (Buttons) y Queries SQL (`WHERE fecha < hoy`) en `ReservaDao`. |

---

## 3. PROBLEMAS TÉCNICOS DETECTADOS (BUGS & DEUDA)

### A. El "Bug del String" en Fechas
*   **Problema:** Ordenar por `fecha_recogida` en Room usando el formato `dd/MM/yyyy` es alfabético, no cronológico.
*   **Impacto:** El listado de reservas aparecerá desordenado por años/meses.
*   **Solución:** Almacenar las fechas en formato `yyyy-MM-dd` o como `Long` (milisegundos) y usar un `TypeConverter`.

### B. Violación de MVVM
*   **Problema:** La clase `ReservaListActivity.java` contiene lógica de cálculo de días y precios.
*   **Impacto:** Código difícil de testear y Activity "pesada".
*   **Solución:** Mover la lógica de cálculo al `ReservaRepository` o `ReservaViewModel`.

### C. Bloqueo de Hilo Principal (UI)
*   **Problema:** Uso de `future.get(15000, ...)` en el hilo principal.
*   **Impacto:** Si la DB tarda, la app se congela (ANR).
*   **Solución:** Usar callbacks o `LiveData` para observar el resultado de las operaciones de escritura.

---

## 4. PLAN DE ACCIÓN (PASO A PASO)

### FASE 1: Refuerzo de la Base de Datos (Prioridad Crítica)
1.  **Modificar `Quad.java` y `Reserva.java`**: Añadir `@ColumnInfo(name = "activo") private boolean activo = true;`.
2.  **Modificar `Reserva.java`**: Añadir `private double precioTotal;`.
3.  **Actualizar `ReservaRepository.insertSync`**: Calcular el precio total (días * precio_quad) en el momento del insert y guardarlo en la nueva columna.
4.  **Actualizar DAOs**: Cambiar todos los `SELECT * FROM...` por `SELECT * FROM... WHERE activo = 1`.

### FASE 2: Lógica de Borrado Lógico
1.  En `QuadDao.java` y `ReservaDao.java`, eliminar la anotación `@Delete`.
2.  Crear `@Query("UPDATE Quad SET activo = 0 WHERE id = :id")`.
3.  Esto asegura que el historial se mantiene pero el usuario no lo ve en las listas.

### FASE 3: Dashboard de Estados (RF13)
1.  **Corregir Fechas**: Convertir el guardado a `yyyy-MM-dd`.
2.  **Nuevas Queries en `ReservaDao`**:
    *   `getReservasPrevistas()`: `WHERE fecha_recogida > DATE('now')`.
    *   `getReservasVigentes()`: `WHERE fecha_recogida <= DATE('now') AND fecha_devolucion >= DATE('now')`.
    *   `getReservasCaducadas()`: `WHERE fecha_devolucion < DATE('now')`.
3.  **Interfaz**: Añadir un `MaterialButtonToggleGroup` en la parte superior de la lista de reservas con: [TODAS] [PREVISTAS] [VIGENTES] [CADUCADAS].

### FASE 4: Pulido UX/UI
1.  **Validación Proactiva**: En `ReservaEdit`, desactivar el botón "Seleccionar Quads" hasta que ambas fechas estén introducidas y sean coherentes.
2.  **Toasts Informativos**: No decir siempre "Error de matrícula", diferenciar si es por formato o por duplicidad.

---

## 5. CONCLUSIÓN DE AUDITORÍA

El proyecto es **funcionalmente bueno** pero **contablemente deficiente**. Para alcanzar el **10**, es obligatorio implementar la persistencia del precio histórico y el borrado lógico. La arquitectura actual permite hacer estos cambios de forma quirúrgica en los DAOs y Repositorios sin romper la UI.

---
*Informe generado automáticamente por Gemini CLI Auditor - 2026*
