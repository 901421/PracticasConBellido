# Análisis Forense: Defecto en GildedRose1 (Punto 3.2)

Durante la ejecución de la batería de tests completa (100% cobertura) en el módulo `GildedRose1`, se detectó un fallo crítico de regresión en las reglas de negocio.

## 1. Síntomas del Fallo
La prueba `should_not_increase_backstage_passes_above_a_quality_of_50` falló con el siguiente error:
*   **Esperado:** 50
*   **Obtenido:** 51

El fallo ocurrió específicamente con un artículo de tipo "Backstage passes" que tenía un `sellIn` de 10 y una `quality` inicial de 49.

## 2. Proceso de Depuración (Debug)
Se emplearon las siguientes técnicas en el IDE:
1.  **Breakpoint:** Se estableció un punto de interrupción al inicio del método `updateQuality()`.
2.  **Watchpoint:** Se observó la variable `quality` del artículo afectado.
3.  **Step-by-step:** Se realizó un seguimiento paso a paso (F8) del flujo lógico.

### Hallazgo:
Al llegar a la lógica de incremento por proximidad de fecha (bloque `sellIn < 11`), el código presentaba la siguiente condición:
```java
// Código defectuoso detectado en Línea 27
if (items[i].getQuality() <= 50) { 
    items[i].setQuality(items[i].getQuality() + 1);
}
```

## 3. Descripción Técnica del Defecto
*   **Localización:** `Inventory.java`, línea 27.
*   **Tipo:** Error de límite (Off-by-one error) por uso incorrecto de operador relacional.
*   **Explicación:** El uso de `<=` en lugar de `<` provoca que, si la calidad ya ha alcanzado el límite máximo de 50 (debido al incremento previo), la condición siga siendo verdadera. Esto permite un incremento adicional ilegal que eleva la calidad a 51.

## 4. Solución
Se corrigió el operador eliminando la igualdad, asegurando que solo se incremente la calidad si es estrictamente menor que 50:
```java
// Código corregido
if (items[i].getQuality() < 50) { 
    items[i].setQuality(items[i].getQuality() + 1);
}
```

## 5. Verificación
Tras aplicar el cambio, todos los tests de la clase `InventoryTest` pasan satisfactoriamente (Barra Verde).
