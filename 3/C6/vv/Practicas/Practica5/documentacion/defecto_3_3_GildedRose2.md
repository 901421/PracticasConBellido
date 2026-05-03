# Análisis Forense: Defecto en GildedRose2 (Punto 3.3)

Tras la propagación de la batería de tests al módulo `GildedRose2`, se observó un comportamiento anómalo masivo: fallaban tanto los tests de artículos legendarios (Sulfuras) como los de artículos normales.

## 1. Síntomas del Fallo
Se detectaron múltiples errores de aserción con un patrón inverso:
*   **Sulfuras:** Esperado 80, Obtenido 79 (Pierde calidad ilegalmente).
*   **Artículos Normales:** Esperado 19, Obtenido 20 (No pierden calidad cuando deberían).
*   **Artículos Normales Caducados:** Esperado 23, Obtenido 24 (Pierden la mitad de la calidad requerida).

## 2. Proceso de Depuración (Debug)
Se utilizó el **Data Type Renderer** personalizado para monitorizar el estado de los objetos en tiempo real. 

### Hallazgo Principal:
Al realizar el seguimiento (Step Over) en la línea 20, se detectó que el flujo de ejecución entraba en el bloque de reducción de calidad **solo** cuando el nombre del artículo coincidía exactamente con "Sulfuras, Hand of Ragnaros".

```java
// Código defectuoso detectado en Línea 20
if (items[i].getName() == "Sulfuras, Hand of Ragnaros") {
    items[i].setQuality(items[i].getQuality() - 1);
}
```

## 3. Descripción Técnica del Defecto
*   **Localización:** `Inventory.java`, línea 20.
*   **Tipo:** Inversión Lógica (Logical Inversion).
*   **Explicación:** El programador utilizó el operador de igualdad (`==`) en una cláusula de exclusión donde se requería desigualdad (`!=`). 
*   **Impacto:** El sistema invirtió las reglas de negocio: protegió a los artículos que deben degradarse y degradó al único artículo que debe ser inmutable. Este error se propaga también a la lógica de artículos caducados, duplicando el impacto negativo.

## 4. Solución
Se corrigió la condición lógica sustituyendo la igualdad por la desigualdad. Esto restaura la protección de Sulfuras y permite que el resto de artículos normales sigan el flujo de degradación estándar.

```java
// Código corregido
if (items[i].getName() != "Sulfuras, Hand of Ragnaros") {
    items[i].setQuality(items[i].getQuality() - 1);
}
```

## 5. Verificación
La corrección de esta única línea resolvió de forma inmediata todos los fallos en el módulo `GildedRose2`, logrando la **Barra Verde** en todos los tests de regresión.
