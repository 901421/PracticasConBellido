# Plan de Acción Detallado: Práctica 5 - Depuración y Análisis Estático

Este documento detalla exhaustivamente cada paso necesario para completar con éxito la Práctica 5. Actúa como una lista de verificación (checklist) granular.

## Fase 1: Setup y Preparación del Entorno

*   [ ] **1.1. Obtención del código fuente:**
    *   [ ] Clonar o descargar el repositorio base indicado en el PDF: `https://github.com/miguel-latre/unizar-vv-practica5` o el repositorio asignado para la práctica.
    *   [ ] Ubicar los archivos descargados dentro del directorio de trabajo actual (`C:\Users\imadx\Documents\GitHub\PracticasConBellido\3\C6\vv\Practicas\Practica5`).
*   [ ] **1.2. Configuración del Proyecto en el IDE:**
    *   [ ] Abrir el proyecto en IntelliJ IDEA.
    *   [ ] Verificar que el IDE reconoce la estructura de módulos (`GildedRose0`, `GildedRose1`, `GildedRose2`).
    *   [ ] Asegurar que el SDK de Java está correctamente configurado para el proyecto.
    *   [ ] Asegurar que JUnit (versión 4 o 5, según el código) está disponible en las dependencias.
*   [ ] **1.3. Verificación Inicial de Estado:**
    *   [ ] Navegar al módulo `GildedRose0`.
    *   [ ] Localizar la clase `InventoryTest` en `src/test/java`.
    *   [ ] Ejecutar la clase `InventoryTest` en modo normal y comprobar que **todas las pruebas pasan correctamente sin errores (barra verde)**.

## Fase 2: Análisis de Pruebas y Cobertura (Módulo `GildedRose0`)

*   [ ] **2.1. Análisis y Mapeo Inicial:**
    *   [ ] Abrir el archivo `pruebas-kata-Gilded-Rose.xlsx`.
    *   [ ] Abrir la clase `InventoryTest` de `GildedRose0`.
    *   [ ] Contrastar cada método de prueba en `InventoryTest` con las filas de la hoja de cálculo.
    *   [ ] Añadir comentarios en el código de `InventoryTest` indicando a qué caso del Excel corresponde cada prueba (considerando `sellIn` y `quality`).
*   [ ] **2.2. Identificación de Diferencias (Documento Entregable - Puntos 1 y 2):**
    *   [ ] Documentar en un borrador: ¿Qué pruebas existen en `InventoryTest` que NO están en la hoja de cálculo/pizarra?
    *   [ ] Documentar en un borrador: ¿Qué pruebas de la hoja de cálculo/pizarra NO están implementadas en `InventoryTest`?
*   [ ] **2.3. Medición Inicial de Cobertura:**
    *   [ ] Ejecutar `InventoryTest` utilizando el modo **"Run with Coverage"** en IntelliJ.
    *   [ ] Anotar el porcentaje de cobertura inicial de la clase `Inventory` (líneas y ramas).
*   [ ] **2.4. Implementación de Pruebas Faltantes:**
    *   [ ] Escribir el código para las pruebas identificadas en el paso 2.2 que faltaban de la pizarra.
    *   [ ] Ejecutar "Run with Coverage" después de cada prueba añadida para observar el incremento.
*   [ ] **2.5. Búsqueda del 100% de Cobertura (Documento Entregable - Punto 3):**
    *   [ ] Examinar el reporte de cobertura en `Inventory.java` buscando líneas de código rojas (no ejecutadas) o amarillas (cobertura parcial de ramas).
    *   [ ] Diseñar nuevas pruebas específicas para cubrir esas condiciones no visitadas.
    *   [ ] Implementar las nuevas pruebas.
    *   [ ] Repetir la ejecución con cobertura hasta alcanzar el **100% en líneas y métodos** de `Inventory`.
    *   [ ] Recopilar el código de estas nuevas pruebas para el entregable final.

## Fase 3: Análisis Forense y Depuración (Módulos `GildedRose1` y `GildedRose2`)

*   [ ] **3.1. Propagación de Batería de Tests:**
    *   [ ] Copiar la clase `InventoryTest` (ahora con el 100% de cobertura) del módulo `GildedRose0`.
    *   [ ] Reemplazar la clase `InventoryTest` en los módulos `GildedRose1` y `GildedRose2` con esta versión completa.
*   [ ] **3.2. Depuración del Defecto en `GildedRose1` (Documento Entregable - Punto 4):**
    *   [ ] Ejecutar `InventoryTest` en `GildedRose1` en **modo normal**.
    *   [ ] Anotar exactamente **cuáles pruebas fallan**.
    *   [ ] Poner un **punto de interrupción (breakpoint)** en la primera línea del método `updateQuality()` de la clase `Inventory`.
    *   [ ] Ejecutar la prueba que falla en modo **Depuración (Debug)**.
    *   [ ] Una vez detenido, usar **Step Into (F7)**, **Step Over (F8)** y **Step Out (Shift+F8)** para seguir la ejecución paso a paso.
    *   [ ] *Práctica de herramientas del IDE:*
        *   [ ] Cambiar manualmente el valor de un atributo de un artículo en el panel de variables (botón derecho -> Set Value...).
        *   [ ] Crear un formateador ("Renderer") personalizado para la clase `Item` que muestre su estado de forma legible (View as -> Create Renderer...).
        *   [ ] Establecer un "Watchpoint" (punto de observación) en el atributo `quality` de la clase `Item`.
        *   [ ] Añadir puntos de interrupción al invocar `setQuality`, cargar la clase `Item`, etc., tal como indica el guion.
        *   [ ] Configurar un breakpoint condicional (pass count o condición) para que se detenga *solo* justo antes de procesar el artículo específico que causa el fallo.
    *   [ ] Aislar la línea exacta que causa el defecto lógico y **documentar el problema** detalladamente.
    *   [ ] Corregir el defecto en `GildedRose1.Inventory`.
    *   [ ] Verificar que todos los tests pasan.
*   [ ] **3.3. Depuración del Defecto en `GildedRose2` (Documento Entregable - Punto 4):**
    *   [ ] Repetir el proceso completo del paso 3.2, pero esta vez analizando el código del módulo `GildedRose2`.
    *   [ ] Aislar y documentar el defecto.
    *   [ ] Corregir el defecto.
    *   [ ] Verificar que todos los tests pasan.

## Fase 4: Análisis Estático de Código (`GildedRose0`)

*   [ ] **4.1. Análisis Base (Documento Entregable - Punto 5):**
    *   [ ] Volver al módulo `GildedRose0`.
    *   [ ] Ejecutar la herramienta: Menú `Code` -> `Inspect Code...`.
    *   [ ] Seleccionar el ámbito: Módulo `GildedRose0` (asegurar que "Include test sources" está marcado).
    *   [ ] Revisar el panel inferior con los problemas detectados ("Inspections").
    *   [ ] Tomar nota de los problemas más relevantes y evaluar si son razonables.
    *   [ ] Aplicar las correcciones automáticas sugeridas por el IDE donde sea prudente.
*   [ ] **4.2. Análisis Extremo (Documento Entregable - Punto 5):**
    *   [ ] Ejecutar nuevamente `Inspect Code...`.
    *   [ ] En "Inspection profile", hacer clic en los puntos suspensivos (`...`).
    *   [ ] **Marcar TODAS las reglas disponibles para Java**.
    *   [ ] Ejecutar el análisis masivo.
    *   [ ] Filtrar y observar los resultados (habrá cientos).
    *   [ ] Documentar este experimento: Analizar críticamente por qué es una mala idea activar todas las reglas a la vez (contradicciones, ruido excesivo, reglas obsoletas).

## Fase 5: Consolidación y Entrega

*   [ ] **5.1. Preparación del Documento Final:**
    *   [ ] Crear un documento (PDF, Word, etc.).
    *   [ ] **Sección 1:** Responder: ¿Qué métodos de prueba en `InventoryTest` (original) NO habíamos diseñado en la clase de pizarra?
    *   [ ] **Sección 2:** Responder: ¿Qué métodos que diseñamos en clase de pizarra NO aparecen en la clase `InventoryTest` original?
    *   [ ] **Sección 3:** Pegar el código de todas las pruebas añadidas en el paso 2.5 para lograr el 100% de cobertura.
    *   [ ] **Sección 4:** Redactar la descripción técnica de los defectos encontrados en `GildedRose1` y `GildedRose2` (resultado de la fase 3).
    *   [ ] **Sección 5:** Redactar el informe sobre las violaciones de reglas del inspector en `GildedRose0` y la opinión crítica sobre las reglas activadas (resultado de la fase 4).
*   [ ] **5.2. Revisión Final:**
    *   [ ] Verificar formato y claridad del documento.
    *   [ ] Entregar el documento según el canal estipulado por la asignatura.