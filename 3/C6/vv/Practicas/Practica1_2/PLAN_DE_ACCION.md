# 🚀 PLAN DE ACCIÓN TÉCNICO: PROYECTO M147_QUADS

Este documento detalla la hoja de ruta técnica para elevar la calidad del proyecto de **6.5 a 10/10**, resolviendo los incumplimientos de requisitos y bugs detectados en la auditoría.

---

## 🛠️ FASE 1: INTEGRIDAD Y PERSISTENCIA (CRÍTICA)
**Objetivo:** Resolver el borrado físico (RF-04, RF-09) y la inestabilidad de precios (RF-12).

### 1.1. Modificación de Entidades (`.database`)
*   **Archivos:** `Quad.java`, `Reserva.java`.
*   **Cambio:** Añadir atributo `private boolean activo = true;` con su Getter/Setter.
*   **Archivo:** `Reserva.java`.
*   **Cambio:** Añadir atributo `private double precioTotal;`.

### 1.2. Refactor de DAOs (`.database`)
*   **Archivos:** `QuadDao.java`, `ReservaDao.java`.
*   **Acción:** Eliminar la anotación `@Delete`.
*   **Acción:** Implementar `@Query("UPDATE Tabla SET activo = 0 WHERE id = :id")`.
*   **Acción:** Filtrar todas las consultas `SELECT` con `WHERE activo = 1`.

### 1.3. Lógica de Precio Fijo
*   **Archivo:** `ReservaRepository.java` -> `insertSync()`.
*   **Acción:** Antes de insertar, calcular: `(Suma de precios de quads) * (días de reserva)`.
*   **Acción:** Asignar el resultado a `reserva.setPrecioTotal()`.

---

## 📅 FASE 2: MOTOR DE FECHAS Y FILTRADO (ALTA)
**Objetivo:** Corregir el bug de ordenación y habilitar el Dashboard de estados (RF-13).

### 2.1. Normalización ISO-8601
*   **Acción:** Al guardar en DB, convertir `dd/MM/yyyy` a `yyyy-MM-dd`.
*   **Por qué:** SQLite permite usar la función `DATE('now')` y ordena cronológicamente por defecto.

### 2.2. Implementación de Queries de Estado
*   **Archivo:** `ReservaDao.java`.
*   **Nuevos Métodos:**
    *   `getPrevistas()`: `WHERE fecha_recogida > DATE('now')`.
    *   `getVigentes()`: `WHERE fecha_recogida <= DATE('now') AND fecha_devolucion >= DATE('now')`.
    *   `getCaducadas()`: `WHERE fecha_devolucion < DATE('now')`.

---

## 📱 FASE 3: UI/UX Y DASHBOARD (ALTA)
**Objetivo:** Implementar la interfaz requerida por la memoria y mejorar la fluidez.

### 3.1. Filtros Visuales
*   **Archivo:** `activity_lista_reservas.xml`.
*   **Acción:** Añadir un `MaterialButtonToggleGroup` superior con botones: [Todas, Previstas, Vigentes, Caducadas].
*   **Archivo:** `ReservaListActivity.java`.
*   **Acción:** Configurar los listeners para que el ViewModel cargue la query correspondiente según el botón pulsado.

### 3.2. Validación Proactiva
*   **Archivo:** `ReservaEdit.java`.
*   **Acción:** Desactivar el botón "Seleccionar Quads" (`setEnabled(false)`) hasta que el usuario haya elegido fechas válidas.

---

## 🏗️ FASE 4: ARQUITECTURA Y LIMPIEZA (MEDIA)
**Objetivo:** Cumplir estrictamente con MVVM y evitar bloqueos de la aplicación.

### 4.1. Refactor de la Capa UI
*   **Archivo:** `ReservaListActivity.java`.
*   **Acción:** Extraer el método `construirMensajeCompleto` y moverlo al `ReservaViewModel`. La Activity no debe conocer las reglas de cálculo de precios.

### 4.2. Optimización Asíncrona
*   **Archivo:** `ReservaRepository.java`.
*   **Acción:** Eliminar `future.get()` bloqueantes. Usar `LiveData` o una interfaz de callback para notificar a la UI el éxito o error de la operación de forma asíncrona.

---

## ✅ CRITERIOS DE VALIDACIÓN FINAL
1.  **¿Historial mantenido?** Al borrar un quad, ¿sigue apareciendo en las reservas antiguas? (Debe ser SÍ).
2.  **¿Precio congelado?** Al editar el precio de un quad, ¿las reservas ya creadas mantienen su precio original? (Debe ser SÍ).
3.  **¿Ordenación correcta?** ¿El 01/01/2027 aparece después del 31/12/2026? (Debe ser SÍ).
4.  **¿Filtrado dinámico?** ¿Funcionan los botones de Previstas/Vigentes/Caducadas? (Debe ser SÍ).

---
*Este plan garantiza el cumplimiento del 100% del catálogo de requisitos de la asignatura.*
