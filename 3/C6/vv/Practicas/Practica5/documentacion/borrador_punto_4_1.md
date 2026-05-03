# Borrador Punto 4.1: Análisis Estático de Código (Hallazgos Iniciales)

Este documento detalla los resultados obtenidos tras ejecutar la herramienta `Inspect Code` de IntelliJ IDEA sobre el módulo `GildedRose0`. Se han identificado varias violaciones de reglas de diseño y buenas prácticas que afectan a la robustez y legibilidad del código.

## 1. Violaciones Críticas y Sugerencias de Mejora

A continuación se listan los hallazgos más relevantes, su justificación técnica y la localización en el código:

### A. Comparación de Cadenas mediante Identidad (`==` / `!=`)
*   **Regla:** *String comparison using '==' or '!='*.
*   **Localización:** `Inventory.java` (8 ocurrencias en total).
*   **Descripción:** El código utiliza operadores de identidad para comparar nombres de artículos (ej. `items[i].getName() != "Aged Brie"`). En Java, esto compara referencias de memoria en lugar de contenido. 
*   **Riesgo:** Aunque funciona actualmente por el "String Interning" de literales, fallaría si los datos provinieran de fuentes externas (DB, archivos).
*   **Corrección:** Sustituir por el método `.equals()` (ej. `!"Aged Brie".equals(items[i].getName())`).

### B. Uso de Campos Finales (`final`)
*   **Regla:** *Field can be final*.
*   **Localización:** 
    *   `Inventory.java`: Atributo `private Item[] items;`
    *   `Item.java`: Atributo `private String name;`
*   **Descripción:** El inspector detectó que estos campos se inicializan una sola vez en el constructor y nunca se vuelven a asignar.
*   **Beneficio:** Facilita el razonamiento sobre el estado inmutable del objeto y previene reasignaciones accidentales.

### C. Bucle "For" Optimizable (Foreach)
*   **Regla:** *'for' loop replaceable with enhanced 'for'*.
*   **Localización:** `Inventory.java`, línea 14 (Método `updateQuality`).
*   **Descripción:** El bucle tradicional que utiliza un índice (`int i = 0; i < items.length; i++`) puede reemplazarse por la sintaxis de "for-each", más limpia y menos propensa a errores de índice.
*   **Corrección:** `for (Item item : items) { ... }`.

### D. Expresiones Aritméticas Sin Sentido
*   **Regla:** *Pointless arithmetic expression*.
*   **Localización:** `Inventory.java`, línea 55 (dentro del bloque de artículos caducados).
*   **Descripción:** La expresión `items[i].getQuality() - items[i].getQuality()` es redundante.
*   **Corrección:** Simplificar directamente a `items[i].setQuality(0)`.

### E. Declaraciones No Utilizadas
*   **Regla:** *Unused declaration*.
*   **Localización:** Clase `Item.java`.
*   **Descripción:** Se detectaron métodos o campos (como el constructor por defecto o métodos de acceso) que no son invocados desde ningún punto de entrada del proyecto (incluyendo los tests).
*   **Corrección:** Eliminar el código muerto para reducir la superficie de mantenimiento.

## 2. Evaluación de Resultados
La mayoría de las sugerencias del inspector son razonables y apuntan a una refactorización necesaria. Especialmente, la corrección de las comparaciones de `String` y la simplificación de la lógica aritmética mejoran la robustez inmediata del sistema. Los avisos sobre el uso de `final` y bucles optimizados contribuyen a una mejor calidad de código a largo plazo.
