# PLAN DE ACCIÓN - Práctica 4: Ramificación y Poda, Programación Lineal

## FASE 1: Análisis y Definición del Problema
### Tarea 1.1: Entendimiento de los Requisitos
*   **Objetivo:** Agrupar $N$ participantes (con $N \pmod 3 = 0$) en $N/3$ equipos de 3 personas de manera que se minimice el nivel total de conflicto.
*   **Entrada:** Número de participantes $N$, seguido de una matriz $C$ de tamaño $N \times N$ donde $C_{i,j} \in [0, 10]$ es el conflicto que la persona $i$ tiene hacia la persona $j$. Ojo: $C_{i,j}$ no es necesariamente igual a $C_{j,i}$.
*   **Conflicto de un equipo $T=\{u,v,w\}$:** $\text{Conflicto}(T) = C_{u,v} + C_{u,w} + C_{v,u} + C_{v,w} + C_{w,u} + C_{w,v}$.
*   **Restricciones implícitas:**
    *   Todos los participantes deben pertenecer exactamente a 1 equipo.
    *   Cada equipo debe tener exactamente 3 participantes.
    *   El problema es NP-Hard (es equivalente a particionar un grafo ponderado dirigido en clicas de tamaño 3).
*   **Salida por caso:** Tiempo de ejecución en ms, Número de nodos del árbol generados, Nivel total de conflicto óptimo (mínimo).

## FASE 2: Diseño de Ramificación y Poda (Branch & Bound)
### Tarea 2.1: Representación de la Solución
*   **Estructura:** Podemos procesar los participantes en orden $0, 1, ..., N-1$. Una solución parcial asigna a los participantes evaluados hasta ahora a un equipo específico. Para evitar simetrías (equipos sin identidad), podemos mantener una lista de equipos activos, cada uno con capacidad máxima de 3.
*   **Nodos del árbol:** Cada nodo contiene el estado actual de las asignaciones, el conflicto acumulado hasta ahora (coste actual) y el nivel del árbol (participante actual a asignar).
### Tarea 2.2: Funciones de Coste y Cota
*   **Coste actual ($g(n)$):** Suma de los conflictos entre los participantes que ya han sido asignados al mismo equipo. Cuando se añade el participante $i$ al equipo $k$, el coste aumenta en la suma de $C_{i, j} + C_{j, i}$ para todo participante $j$ que ya esté en el equipo $k$.
*   **Cota inferior ($h(n)$):** Para los participantes restantes sin equipo completo, necesitamos una estimación optimista (mínimo posible conflicto) que añadirán al ser asignados.
    *   *Opción 1 (Básica):* $h(n) = 0$. (Poda simple, muy lenta).
    *   *Opción 2 (Mejorada):* Para cada participante no asignado, necesitamos agregar 2 conflictos salientes y 2 entrantes a su equipo final. Podemos precalcular la suma de los 2 menores conflictos salientes y entrantes para cada participante (excluyendo a sí mismo). La suma de esto dividida por 2 (o cuidadosamente sumada para evitar contar doble) podría ser una cota.
*   **Cota global (Mejor coste - Upper Bound):** Se inicializa a infinito o mediante una solución voraz (Greedy) rápida.

### Tarea 2.3: Estrategia de Ramificación
*   Prioridad basada en Menor Coste (Best-First Search) usando una cola de prioridad, o Depth-First Search (DFS) con B&B (que suele consumir mucha menos memoria y es más rápido de programar encontrar una solución subóptima rápida para podar más).
*   *Decisión:* Implementaremos un DFS recursivo (Backtracking con poda o Branch & Bound en profundidad) porque en problemas de agrupamiento, DFS encuentra soluciones completas rápido que ayudan a podar drásticamente el árbol de búsqueda sin consumir $O(b^d)$ memoria. Contaremos cada llamada recursiva (o creación de nodo) como un nodo generado.

## FASE 3: Implementación en C++ / Python
### Tarea 3.1: Configuración del Proyecto
*   **Archivos afectados:** `main.cpp`, `ejecutar.sh`, `LEEME.txt`.
*   **Justificación:** C++ es ideal para B&B por su velocidad y control de memoria, vital para superar casos con $N \ge 15$. Si no, Python es aceptable pero podría ser penalizado por tiempos excesivos. Elegiremos C++ por rendimiento.
*   **Riesgos:** Falta de familiaridad con la sintaxis, memory leaks. (Usaremos C++17/20, `std::vector`).

### Tarea 3.2: Implementación de la Lógica B&B
*   **Objetivo:** Desarrollar la función de B&B.
*   **Validaciones:** Verificar si para $N=6$ y $N=9$ el programa devuelve el óptimo (ej: 0 si hay equipos de ceros) y tiempos en ms.
*   **Revisión del Subagente:** El subagente verificará la correctitud algorítmica y que el cálculo del conflicto acumulado sea exacto.

### Tarea 3.3: Integración de I/O
*   **Objetivo:** Leer `pruebas.txt` (y múltiples casos) y generar la salida requerida en el archivo especificado.
*   **Validaciones:** Verificar parseo correcto del archivo de entrada multicaso.

## FASE 4: Modelo de Programación Lineal Entera (PLI)
### Tarea 4.1: Modelado
*   **Variables:** $x_{i,e} \in \{0,1\}$. 1 si el participante $i$ está en el equipo $e$ ($e \in \{1, \dots, N/3\}$).
    Variable auxiliar para linealizar el coste: $y_{i,j,e} \in \{0,1\}$ que es 1 si tanto $i$ como $j$ están en el equipo $e$.
*   **Restricciones:**
    *   Cada participante $i$ pertenece a exactamente 1 equipo: $\sum_{e} x_{i,e} = 1 \quad \forall i$.
    *   Cada equipo $e$ tiene exactamente 3 miembros: $\sum_{i} x_{i,e} = 3 \quad \forall e$.
    *   Linealización: $y_{i,j,e} \le x_{i,e}$ y $y_{i,j,e} \le x_{j,e}$. Además $x_{i,e} + x_{j,e} - 1 \le y_{i,j,e}$.
*   **Función Objetivo:** Minimizar $\sum_{e} \sum_{i \ne j} C_{i,j} \cdot y_{i,j,e}$.
*   **Alternativa más eficiente:** Variables $z_{i,j} \in \{0,1\}$, 1 si $i$ y $j$ están en el MISMO equipo.
    *   Minimizar $\sum_{i \ne j} C_{i,j} \cdot z_{i,j}$
    *   $\sum_{j \ne i} z_{i,j} = 2 \quad \forall i$.
    *   $z_{i,j} = z_{j,i}$.
    *   Desigualdad triangular: $z_{i,j} + z_{j,k} - 1 \le z_{i,k} \quad \forall i,j,k$.
    *   Esta formulación es mucho más eficiente porque rompe la simetría de los IDs de los equipos.

### Tarea 4.2: Implementación del Script PLI
*   **Objetivo:** Desarrollar `pli.py` usando PuLP o OR-Tools para resolver los casos y validar el B&B.
*   **Validaciones:** Comparar los resultados (conflictos óptimos) con el programa B&B.

## FASE 5: Pruebas y Auditoría
### Tarea 5.1: Casos Adicionales
*   Generar $N=12$, $N=15$.
*   Ejecutar tanto `main.cpp` (B&B) como `pli.py` (PLI) y anotar tiempos y nodos.

### Tarea 5.2: Generación del Informe (LaTeX)
*   **Objetivo:** Redactar `memoria.tex` (máximo 4 páginas).
*   **Secciones:** Diseños de algoritmos, Implementación, Experimentación, PLI, Conclusiones.
*   **Validaciones:** Verificar que compila, no excede 4 páginas, e incluye gráficos de nodos vs N y tiempo vs N.

## FASE 6: Entrega
*   Verificar contenido: `LEEME.txt`, `main.cpp`, `ejecutar.sh`, código PLI, `pruebas.txt`, PDF de la memoria.
*   Empaquetar en `practica4.tar`.
