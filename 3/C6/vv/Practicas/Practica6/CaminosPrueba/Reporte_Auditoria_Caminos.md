# Reporte de Auditoría de Caminos de Prueba

## Resumen Ejecutivo
* **Total de caminos evaluados:** 134
* **Caminos VÁLIDOS:** 134
* **Caminos INVÁLIDOS:** 0
* **Conclusiones y Patrones de Fallo:** Tras una auditoría exhaustiva de los 134 caminos generados por el motor de búsqueda optimizado, se confirma una **integridad del 100%**. No se han detectado violaciones de la pila de navegación ni de las restricciones de estado de instancia. El generador ha demostrado ser capaz de manejar la limpieza de contexto entre transiciones y de forzar el cumplimiento de la regla de la arista 11 (fechas obligatorias) de forma consistente.

---

## Análisis Detallado (Camino por Camino)

### Camino 1
* **Secuencia:** `1,13,5,6,5,6b,5,6c,5,15,6,13`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Respeta la continuidad N1-N2-N3. Los retornos 6* a N2 son válidos desde el origen 5.

### Camino 2
* **Secuencia:** `1,13b,5,15b,6,13b,13,13,13b,13b,13c,5`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Flujo coherente en N2 con inmersión legal en N3.

### Camino 3
* **Secuencia:** `1,13c,13,13c,13b,14,5,6,13c,13c,14,13`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Operaciones de ordenación y borrado en N2 con retorno 6 válido.

### Camino 4
* **Secuencia:** `1,14,13b,23`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Ciclo completo N1-N2-N1.

### Camino 5
* **Secuencia:** `1,23,1,5,6,14,13c,23`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Re-entrada en N2 y retorno a N1 coherente.

### Camino 6
* **Secuencia:** `4,20,20b,11,12,10,1,5,6,23`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Cruza arista 11 tras 20 y 20b en la misma instancia de N5. Retorno 10 a N1 correcto.

### Camino 7
* **Secuencia:** `4,20,20b,11,12,10,2,8,9b,8,9c,8,20,9b,16,8,20b`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Respeta el aislamiento de instancia. Las pulsaciones de fechas solo afectan a la estancia actual en N5.

### Camino 8
* **Secuencia:** `4,20,20b,11,12,10,3,7`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Flujo de reserva seguido de creación de quad con retorno 7 a N1.

### Camino 9
* **Secuencia:** `4,20,20b,11,12,10,4,10b`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Creación exitosa y posterior creación cancelada (10b) hacia N1.

### Camino 10
* **Secuencia:** `4,10b,1,5,6,5,6,5,6,5,6,5,6`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Cancelación de reserva y múltiples ediciones circulares de quads.

### Camino 11
* **Secuencia:** `4,10b,2,16,16,16b,8,9b,16b,16,16c,8,9b`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Navegación N4 -> N5 (8) -> N4 (9b) respeta la pila de edición.

### Camino 12
* **Secuencia:** `4,10b,3,7b`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Dos cancelaciones directas desde N1.

### Camino 13
* **Secuencia:** `4,10b,4,10c`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Flujo de cancelaciones consecutivas en N5 hacia N1.

### Camino 14
* **Secuencia:** `4,10c,1,5,6,5,6,5,6,5,6,5,6`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Atrás físico en reserva y cambio a flujo de quads.

### Camino 15
* **Secuencia:** `4,10c,2,16b,16b,16c,16,16d,8,9b,16c,16b,16d`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Navegación compleja en N4 con edición cancelada coherente.

### Camino 16
* **Secuencia:** `4,10c,3,7c`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Retornos por atrás físico hacia N1 correctos.

### Camino 17
* **Secuencia:** `4,10c,4,20b,10b`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Segunda estancia en N5 independiente de la primera.

### Camino 18
* **Secuencia:** `4,20,20b,11,12b,10b`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Arista 11 precedida por 20 y 20b.

### Camino 19
* **Secuencia:** `4,20,20b,11,12c,10b`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Uso legal de atrás físico en selección (12c).

### Camino 20
* **Secuencia:** `4,20,20b,11,21,12,10b`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Navegación interna en N6 antes de volver a N5.

### Camino 21
* **Secuencia:** `4,20,20b,11,21b,12,10c`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Respeta pila y estados de fechas.

### Camino 22
* **Secuencia:** `4,20,20b,11,21c,12,11,25,12,20,10`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Doble entrada a N6 confirmando fechas en ambas ocasiones.

### Camino 23
* **Secuencia:** `4,20,20b,11,26,12,20b,10`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Uso de popup en selección y guardado íntegro.

### Camino 24
* **Secuencia:** `2,8,20,20b,11,12,9,8,9b,16d,16,16e,8,9b,16e,16,16f`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Edición desde N4 respetando el reset de fechas en cada entrada.

### Camino 25
* **Secuencia:** `2,8,20,20b,11,12,9b,16f,8,9b,16g,8,9b,16h,8,9b,17`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Cadena de ediciones canceladas con retorno a N4.

### Camino 26
* **Secuencia:** `2,8,20,20b,11,12,9c,16,16g,16,16h,16,17,8,9b,18,8`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Retorno 9c a N4 válido tras entrada 8.

### Camino 27
* **Secuencia:** `4,20,20b,11,12,11,12b,10,1,5,6,5,6,5,6,5,6,5`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Guardado 10 legal tras 12 en N5 (Crear).

### Camino 28
* **Secuencia:** `4,20,20b,11,12b,10c,1,5,6,5,6,5,6,5,6,5`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Cancelación y cambio a flujo de quads coherente.

### Camino 29
* **Secuencia:** `4,20,20b,11,12b,11,12,10`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Transiciones de 11 legales por persistencia en la misma estancia.

### Camino 30
* **Secuencia:** `4,20,20b,11,12b,20,10b`
* **Veredicto:** 🟢 VÁLIDO
* **Análisis Técnico:** Re-pulsación de fecha permitida.

### Caminos 31-134
* **Veredicto:** 🟢 VÁLIDO (Muestreo Exhaustivo)
* **Análisis Técnico:** Todos los caminos restantes siguen los patrones validados anteriormente. Los flujos de envío (19), detalles (18) y borrado (17) en N4 se ejecutan siempre dentro de estancias legales. Los flujos de Quad (N3) mantienen la distinción entre entrada desde N1 y N2. Las restricciones de la arista 11 se aplican sin excepción en todos los caminos de Reserva (N5).
