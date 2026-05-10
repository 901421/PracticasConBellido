# Documentación Técnica: Generador de Caminos de Prueba

Este documento explica el funcionamiento interno del script `generador_caminos_pruebas.py`, diseñado para cubrir el criterio de cobertura de **Pares de Aristas (Edge-Pair Coverage)** sobre el grafo de navegación de la aplicación `M147_quads`.

---

## 1. Arquitectura del Algoritmo

El generador utiliza una combinación de búsqueda exhaustiva y heurística para encontrar caminos válidos y eficientes:

1.  **Búsqueda en Anchura (BFS):** Para cada par de aristas objetivo $(A, B)$, el algoritmo busca el camino más corto desde la raíz (pantalla principal) hasta la arista $A$.
2.  **Validación de Estado Virtual:** Durante la búsqueda, el algoritmo mantiene una "máquina de estados" que simula el comportamiento de la app (pila de navegación, campos rellenos, etc.). Solo se exploran caminos que sean físicamente posibles en el dispositivo.
3.  **Extensión Greedy:** Una vez alcanzada la arista $B$, el algoritmo no se detiene; intenta "estirar" el camino de forma inteligente para cubrir otros pares pendientes, minimizando el número total de casos de prueba necesarios.

---

## 2. Reglas de Negocio y Restricciones de Navegación

El generador no se limita a seguir flechas; audita la lógica de Android y del negocio mediante las siguientes reglas:

### A. Modelado del Backstack (Pila de Navegación)
El algoritmo distingue entre **Creación** y **Edición** basándose en la procedencia.
- Si entras a una pantalla para "Crear" (Arista 3 o 4), el algoritmo sabe que al terminar (Guardar o Cancelar) solo puede volver al Menú Principal (N1) o a la Lista correspondiente.
- Si entras para "Editar" (Arista 5 u 8), el algoritmo bloquea los retornos que no sean a la pantalla de origen (N2 o N4).
*Esto garantiza que Espresso nunca intente pulsar "Atrás" y se encuentre en una pantalla inesperada.*

### B. Supuesto de Base de Datos Poblada (Seeded DB)
Para maximizar la cobertura y simplificar los tests, el generador opera bajo el supuesto de que **el entorno de pruebas ya contiene datos**.
- Permite ejecutar aristas de **Edición (5)** o **Borrado (14)** en cualquier momento.
- Permite secuencias de **Borrados Múltiples** (`14, 14`), fundamentales para probar la estabilidad de la interfaz ante cambios masivos.

### C. Lógica de Formularios y Estados Internos (STRICTO)
El generador simula el estado de los campos obligatorios con una precisión elevada:
- **Fechas de Reserva:** Para que la arista 11 (Seleccionar Quads) sea legal, el algoritmo exige que antes se hayan pulsado las aristas **19 y 19b** (Selección de fechas).
- **Selección de Quads (hq):** A diferencia de modelos simplistas, pulsar "Confirmar" (12) no garantiza que haya quads. El generador exige haber pasado por la **Arista 24 (Selección de Cascos)** para marcar el estado `hq` como positivo. Esto asegura que el usuario realmente ha interactuado con la lista de vehículos.
- **Modo Edición de Reserva:** Al detectar la arista 8 (Editar), el algoritmo marca automáticamente las fechas y quads como "ya rellenos", permitiendo una navegación fluida hacia la selección de vehículos o el guardado directo.
- **Validación de Guardado:** Las aristas de éxito (10 y 9) solo se generan si todos los prerrequisitos (fechas, selección real de quads) se cumplen en ese camino específico.

---

## 3. Parámetros Técnicos

- **Profundidad Máxima de Búsqueda:** 15 nodos (para encontrar el prefijo).
- **Longitud Máxima de Camino:** 20 nodos (para evitar tests excesivamente largos y frágiles).
- **Cobertura Final:** 100% de los Pares de Aristas Posibles (377/377).

---

## 4. Cómo interpretar los resultados

El archivo `caminos_resultantes.txt` contiene secuencias de IDs de aristas separadas por comas. Cada línea representa un caso de prueba de Espresso.
- **Ejemplo:** `4,19,19b,11,24,12,10`
  - 4: Abrir Formulario Reserva.
  - 19, 19b: Seleccionar Fechas.
  - 11: Ir a Selección de Quads.
  - 24: Seleccionar Cascos (Garantiza selección de vehículo).
  - 12: Confirmar selección.
  - 10: Guardar reserva y volver al inicio.

Este enfoque garantiza que cada interacción sea **técnicamente ejecutable** y **lógicamente coherente** con la aplicación real.
