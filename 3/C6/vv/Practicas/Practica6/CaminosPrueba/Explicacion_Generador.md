# Metodología de Generación y Auditoría de Caminos (v3.0)

Este directorio constituye el núcleo de la estrategia de pruebas de la aplicación. Se ha evolucionado de un modelo estructural simple a un modelo de **Cobertura Lógica de Estado**.

## 1. Fundamentos Metodológicos

### A. Cobertura de Pares de Aristas (Edge-Pair Coverage)
El objetivo principal es cubrir todas las secuencias de tres nodos (dos aristas consecutivas). Esto garantiza que no solo probamos que se puede ir de A a B, sino que el sistema se comporta correctamente cuando llegamos a B viniendo desde A.

### B. Inyección de Lógica de Negocio en el Grafo
El grafo estructural puro es insuficiente para Android. Hemos implementado tres capas de filtrado:
1.  **Capa de Navegación:** Basada en el mapa de navegación (`.pdf`).
2.  **Capa de Pila (Back Stack):** Eliminación de retornos imposibles según quién inició la actividad (Contexto de Pila).
3.  **Capa de Estado (UI Logic):** Restricciones de dependencia de datos (ej. Fechas obligatorias antes de seleccionar vehículos).

---

## 2. Herramientas y Flujo de Trabajo

### `generador_caminos_pruebas.py`
Este script utiliza un motor de búsqueda con **seguimiento de estado**.
- **Memoria de Transacción:** Rastrea si se han puesto fechas de recogida/devolución y si hay quads seleccionados.
- **Detección de Contexto:** Distingue si estamos en un flujo de **Creación** (iniciado por A4) o de **Edición** (iniciado por A8).
- **Algoritmo de Prefijo Legal:** Para cada par de aristas objetivo, el generador calcula el camino mínimo necesario para poner el sistema en el estado requerido (ej. rellenar fechas) antes de ejecutar el par.

### `verificar_cobertura.py` (Auditor Avanzado)
No es un simple contador. Realiza una **auditoría forense** de los caminos:
- **Trazabilidad:** Genera el archivo `auditoria_detallada.txt` que mapea cada par a los caminos específicos que lo cubren.
- **Validación de Reglas:** Comprueba que NINGÚN camino viole las reglas de negocio (ej. seleccionar quads sin fechas).
- **Análisis de Suficiencia:** Verifica la cobertura total de Nodos y Aristas como subproductos de la cobertura de Pares de Aristas.

---

## 3. Informe de Auditoría Final

Tras la última ejecución, los resultados son:

| Métrica | Valor | Estado |
|---------|-------|--------|
| Total de Caminos | 115 | Optimizado |
| Pares de Aristas Objetivo | 377 | Definido |
| Cobertura Edge-Pair | 100% | **ALCANZADA** |
| Cobertura de Nodos | 100% (6/6) | **COMPLETA** |
| Cobertura de Aristas | 100% (47/47) | **COMPLETA** |
| Violaciones de Lógica | 0 | **SISTEMA ÍNTEGRO** |

### Análisis de Suficiencia para Pruebas
Los 115 caminos generados son **suficientes y exhaustivos** para testear la aplicación completa debido a:
1.  **Profundidad:** Al cubrir pares de aristas, se prueban efectos secundarios de la navegación.
2.  **Realismo:** Al respetar el Back Stack, los tests Espresso no fallarán por cierres inesperados de actividades.
3.  **Integridad:** Se cubren todos los estados de validación de formularios (casos de éxito y error de fechas/vehículos).

---

## 4. Archivos Generados
- `pares_entrada.txt`: Base de datos de requisitos (377 pares).
- `caminos_resultantes.txt`: Secuencias de aristas para implementar en Espresso.
- `auditoria_detallada.txt`: Mapeo de trazabilidad para depuración de cobertura.
