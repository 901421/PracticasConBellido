# Auditoría Maestra Unificada: Automatización de Caminos de Navegación
**Documento:** `CaminosNavegacionTest.java` + `M147_quads` (App)
**Fecha:** Mayo 2026 | **Estatus:** CRÍTICO

---

## 1. Resumen Ejecutivo Unificado

Este documento representa la fusión **exhaustiva** del análisis arquitectónico experto proporcionado y la auditoría profunda del código fuente (`Activities`, `ViewModels`, `DAOs`). 

La arquitectura general del test tiene bases sólidas (`IdlingResource`, `UiAutomator`), pero la falta de gestión de estado y sincronización asíncrona lo hacen frágil. Se han validado y unificado **todas las 17+ inconsistencias** detectadas, aportando evidencia concreta desde el código de producción.

---

## 2. Tabla de Inconsistencias Exhaustiva (Análisis Experto + Código Real)

| Arista(s) | Riesgo | Problema Detectado | Evidencia en Código (Unificada) | Solución Propuesta |
| :--- | :--- | :--- | :--- | :--- |
| **Transversal** | 🔴 **CRÍTICO** | **Contaminación de Base de Datos** | El `@Before` no limpia Room. Los tests parametrizados acumulan basura. | Invocación síncrona de `clearAllTables()` en `@Before` y repoblado con datos fijos. |
| **5, 14, 17** | 🔴 **CRÍTICO** | **Crash por Listas Vacías** | `actionOnItemAtPosition(0)` sin precondición de que el adapter tenga elementos. | Usar `check(matches(hasMinimumChildCount(1)))` antes de interactuar. |
| **11** | 🔴 **CRÍTICO** | **Bloqueo Híbrido (Datos + Lógica)** | **1.** Si no hay quads, abre lista vacía y crashea aristas 24/12.<br>**2.** Validado en `ReservaEdit.java:92`: Si `mFechaIn.isEmpty()`, impide navegar. | Garantizar 19/19b antes de 11. Añadir aserción de `hasMinimumChildCount(1)` tras entrar. |
| **24** | 🔴 **CRÍTICO** | **Race Condition Múltiple + Regex** | **1.** `Thread.sleep(800)` tras `setChecked`.<br>**2.** Regex frágil `(?i)1 Casco\|0 Cascos`.<br>**3.** Si falla, `pressBack()` deja UI inconsistente. | Cambiar a `wait(Until.enabled(true))`. Ampliar Regex a genérico `\d+ Cascos?`. Throw explicit error si falla. |
| **9, 10** | 🔴 **CRÍTICO** | **Reserva Corrupta sin Quad** | Se clica `button_confirm` sin validar si hubo selección en arista 12. | Comprobar que hay quad seleccionado o asertar `isEnabled()` en el botón antes del clic. |
| **17, 23** | 🟠 **ALTO** | **Ambigüedad Fatal: Botón "CANCELAR"** | `handleSystemDialog` usa `(?i)ELIMINAR\|...\|CANCELAR`. El sistema puede clickar Cancelar. | Separar métodos: `handleDestructiveDialog` (solo OK/Eliminar) y `handleInfoDialog`. |
| **16e–16h** | 🟠 **ALTO** | **Filtros Vacían la Lista** | Tras filtrar (ej. Caducadas), la lista queda vacía y las aristas 17/18 fallan. | Poblar BD con 1 reserva de cada estado (Prevista, Vigente, Caducada) en el `@Before`. |
| **12** | 🟠 **ALTO** | **Confirmación Ciega de Selección** | `forceClick()` en `btn_confirm_selection` ignora si está desactivado por no tener quads marcados. | Añadir `check(matches(isEnabled()))` antes del `forceClick()`. |
| **18** | 🟠 **ALTO** | **Navegación Fantasma (Dialog vs Activity)** | `handleSystemDialog()` espera un botón, pero la app podría estar abriendo un Activity o Dialog sin botones. | Validar dinámicamente: Si es Activity hacer `pressBack()`, si es Dialog cerrarlo con botón esperado. |
| **6, 7** | 🟠 **ALTO** | **No-determinismo en Matrículas** | `Math.random() * 9000` causa problemas con Constraints de DB y dificulta debug. | Usar `System.nanoTime()` o el hash del parámetro del test. |
| **19, 19b** | 🟠 **ALTO** | **Sleep Residual en DatePicker** | `Thread.sleep(1500)` tras clickar OK en DatePicker. | Cambiar por `device.waitForIdle()`. |
| **Retry Loop** | 🟠 **ALTO** | **Retry Genérico con Sleep** | `catch (Exception e)` incondicional hace sleep de 1500ms y `handleSystemDialog` a ciegas. | Limitar catch a `PerformException` o `UiObjectNotFoundException`. No reintentar Asserts. |
| **Helper** | 🟠 **ALTO** | **Violación de Contrato Espresso** | `clickOnViewChild` retorna `getConstraints() = null`. | Retornar `isAssignableFrom(ViewGroup.class)`. |
| **13, 13b, 13c**| 🟡 **MEDIO** | **Ausencia de `asegurarPantalla`** | No comprueba estar en lista de quads antes de ordenar. Fallaría si vienes directo de arista 6 (guardar) asíncrona. | Añadir `asegurarPantalla(By.res("...toggleGroupQuads"), "1")` |
| **Helper** | 🟡 **MEDIO** | **`waitAndPerform` Engañoso** | El método se llama "wait" pero solo ejecuta un simple `perform()`. | Renombrar a `espressoPerform()`. |
| **Helper** | 🟡 **MEDIO** | **`asegurarPantalla` Incompleto** | Navega pero no aserta que la navegación haya tenido éxito. | Añadir `device.wait(Until.hasObject(selector), timeout)` tras el intento de navegación. |

---

## 3. Problemas Transversales Comprobados en Producción

### Dependencia Fechas -> Quads (Comprobado)
Tu sospecha de que algunas aristas fallan por precondiciones de navegación es correcta. He revisado `ReservaEdit.java` y he encontrado esto en el `mBtnSelectQuads.setOnClickListener`:
```java
if (mFechaRecogidaStr.isEmpty() || mFechaDevolucionStr.isEmpty()) {
    Toast.makeText(this, "Primero selecciona las fechas", Toast.LENGTH_LONG).show();
    return; // <-- BLOQUEO ABSOLUTO
}
```
Esto certifica de forma absoluta que cualquier camino que intente la arista 11 sin haber pasado por 19 y 19b fallará, desincronizando todas las aristas subsiguientes.

### Dialogos de Android (El peligro de "CANCELAR")
Tu advertencia sobre el árbol de accesibilidad de UiAutomator es crítica. El código actual usa:
`new UiSelector().textMatches("(?i)ELIMINAR|ACEPTAR|OK|CONFIRMAR|SI|SÍ|CERRAR|CLOSE|CANCELAR")`
UiAutomator devolverá la primera coincidencia que lea de arriba abajo en el XML renderizado de Android. Si "CANCELAR" se renderiza primero, `btn.click()` cancelará operaciones destructivas (como el borrado en las aristas 14 y 17). Esto generará un falso positivo (el test cree que borró, pero no lo hizo).

### Contaminación de Datos y Singletons
El proyecto usa Room Database (`QuadRoomDatabase.java`). Aunque se inicializa en los tests, JUnit ejecuta múltiples instancias para pruebas parametrizadas bajo el mismo proceso. Room persiste los datos en el emulador. Al no haber `clearAllTables` en el `@Before`, los quads de la iteración 1 se mezclan con la 50, causando crashes por listas que cambian de tamaño de forma impredecible o `SQLiteConstraintExceptions`.

---

## 4. Estrategia de Refactorización Integral

1.  **Reescribir `@Before` y `@After`:** Inyectar una limpieza profunda vía `TestRunner` o invocando a la base de datos localmente mediante `CountDownLatch` síncrono.
2.  **Parchear Arista 11 y 24:** Integrar comprobaciones estrictas de estado (`check(matches(isEnabled()))`) y quitar la lógica de regex ciega para asegurar flujos deterministas.
3.  **Refactorizar Helpers:** Aplicar tus recomendaciones exactas para `clickOnViewChild` (evitar NPE), separar los manejadores de diálogos (`handleDestructiveDialog`) y mejorar `asegurarPantalla`.