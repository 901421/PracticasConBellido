# Borrador Punto 2.5: Búsqueda del 100% de Cobertura

Tras la implementación de las pruebas diseñadas en la fase de "Caja Negra" (Excel), la cobertura de ramas (branch coverage) se situó en un **91% (33/36)**. El análisis visual en IntelliJ reveló tres líneas en amarillo (cobertura parcial) dentro del bloque de artículos caducados (`sellIn < 0`).

Este documento detalla el análisis de esas ramas y los métodos de prueba "quirúrgicos" añadidos para alcanzar la cobertura total.

## 1. Análisis de Ramas No Cubiertas (Líneas Amarillas)

Para alcanzar el 100%, fue necesario diseñar pruebas que forzaran la evaluación a `False` de las siguientes condiciones lógicas que solo habían sido evaluadas como `True`:

*   **Línea 49: `if (items[i].getQuality() > 0)`**
    *   *Situación:* Se habían probado artículos normales caducados con calidad > 0, pero no con calidad 0.
    *   *Solución:* Probar un artículo normal con `sellIn = 0` (que pasa a -1) y `quality = 0`.
*   **Línea 50: `if (items[i].getName() != "Sulfuras, Hand of Ragnaros")`**
    *   *Situación:* No se había probado que un objeto de tipo "Sulfuras" entrara en el bloque de caducados.
    *   *Solución:* Crear un objeto Sulfuras con `sellIn` negativo inicial.
*   **Línea 59: `if (items[i].getQuality() < 50)`**
    *   *Situación:* Se probó el aumento de calidad de Aged Brie caducado, pero no el caso en que ya tiene la calidad máxima (50).
    *   *Solución:* Probar Aged Brie con `sellIn = 0` y `quality = 50`.

## 2. Métodos de Prueba Añadidos para el 100%

A continuación se detallan los métodos finales incorporados a `InventoryTest.java`:

```java
// Prueba P2d (quality): Aged Brie en el limite de caducidad (sellIn=0) aumenta +2
@Test
public void should_increase_the_quality_of_aged_brie_twice_as_fast_once_the_sell_in_date_has_passed() {
    Item agedBrie = new Item("Aged Brie", 0, 25);
    Inventory inventory = createInventory(agedBrie);
    inventory.updateQuality();
    assertEquals(27, agedBrie.getQuality());
}

// Prueba P10c (quality): Articulo normal caducado con calidad 0 (Cubre rama False linea 49)
@Test
public void should_not_lower_the_quality_below_zero_when_expired() {
    Item normalItem = new Item("+5 Dexterity Vest", 0, 0);
    Inventory inventory = createInventory(normalItem);
    inventory.updateQuality();
    assertEquals(0, normalItem.getQuality());
}

// Prueba P3 variante (quality): Sulfuras con sellIn negativo (Cubre rama False linea 50)
@Test
public void should_never_changes_quailty_of_Sulfuras_when_expired() {
    Item sulfuras = new Item("Sulfuras, Hand of Ragnaros", -1, 80);
    Inventory inventory = createInventory(sulfuras);
    inventory.updateQuality();
    assertEquals(80, sulfuras.getQuality());
}

// Prueba P2b (quality): Aged Brie caducado con calidad al maximo (Cubre rama False linea 59)
@Test
public void should_not_increase_the_quality_of_aged_brie_over_50_when_expired() {
    Item agedBrie = new Item("Aged Brie", 0, 50);
    Inventory inventory = createInventory(agedBrie);
    inventory.updateQuality();
    assertEquals(50, agedBrie.getQuality());
}
```

## 3. Resultado Final
Tras la incorporación de estos métodos, la ejecución con cobertura en IntelliJ reportó:
*   **Clase Inventory:** 100% Lines, 100% Methods, **100% Branches**.
*   **Estado:** Barra verde y desaparición de todas las marcas amarillas/rojas en el código de producción.
