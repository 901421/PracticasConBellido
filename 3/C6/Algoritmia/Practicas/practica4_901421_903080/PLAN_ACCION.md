# PLAN DE ACCIÓN - Práctica 4: Ramificación y Poda, Programación Lineal

## FASE 1: Análisis y Definición del Problema (Tarea 1.1)
*   **Objetivo:** Agrupar $N$ participantes ($N \pmod 3 = 0$) en $N/3$ equipos de 3 personas para minimizar el conflicto total.
*   **Entrada:** $N$ seguido de matriz $C_{N \times N}$ ($C_{i,j} \in [0, 10]$). No simétrica.
*   **Coste Equipo $T=\{u,v,w\}$:** $\sum_{x,y \in T, x \neq y} C_{x,y}$. (6 términos).
*   **Salida (por caso):**
    1. Tiempo de ejecución (ms).
    2. Número de nodos generados.
    3. Valor óptimo (mínimo conflicto).

## FASE 2: Diseño de Ramificación y Poda (Tarea 1 - 8 puntos total con T2 y T3)
### Tarea 2.1: Representación y Espacio de Búsqueda
*   **Representación:** Vector de asignación o equipos parciales.
*   **Árbol de búsqueda:** Definir niveles (ej: participante $i$ decide a qué equipo ir o qué equipo se forma).
*   **Dimensión:** Calcular teóricamente el tamaño del espacio de búsqueda (combinatoria de particiones en clicas de 3).
### Tarea 2.2: Funciones de Coste y Cota
*   **Coste actual $g(n)$:** Conflicto de equipos ya cerrados + conflicto parcial de equipos en formación.
*   **Cota inferior $h(n)$:** Estimación optimista de los conflictos futuros.
    *   *Propuesta:* Para cada persona libre, sumar sus 2 menores conflictos salientes y 2 menores entrantes, ajustar para evitar doble conteo.
*   **Poda:** Si $g(n) + h(n) \ge \text{Cota Global}$, podar.

## FASE 3: Implementación B&B en C++ (Tarea 2)
### Tarea 3.1: Estructura del Programa
*   **Archivo:** `main.cpp`.
*   **Entrada:** Leer de `pruebas.txt` (formato bloques).
*   **Salida:** Escribir en `resultados.txt` (formato exacto: tiempo, nodos, óptimo).
*   **Algoritmo:** DFS con B&B (mejor para memoria y encontrar cotas rápido).
### Tarea 3.2: Automatización
*   **Archivo:** `ejecutar.sh`. Debe compilar `main.cpp` y ejecutarlo con los casos de prueba.

## FASE 4: Programación Lineal Entera (Tarea 4 - 2 puntos)
### Tarea 4.1: Formalización Matemática
*   Definir variables de decisión, función objetivo y restricciones en el informe.
### Tarea 4.2: Implementación en Python
*   **Archivo:** `pli.py` usando PuLP o OR-Tools.
*   **Funcionalidad:** Resolver los mismos casos que el B&B.
### Tarea 4.3: Validación Cruce
*   Comparar resultados óptimos (deben coincidir).
*   Comparar tiempos de ejecución (analizar cuál escala mejor).

## FASE 5: Experimentación y Análisis (Tarea 3)
### Tarea 5.1: Diseño de Casos de Prueba
*   Casos pequeños ($N=6, 9$), medianos ($N=12, 15$) y grandes ($N=18+$ si el tiempo lo permite).
*   Casos con matrices dispersas (muchos ceros) y densas.
### Tarea 5.2: Recolección de Métricas
*   Generar tablas/gráficas de:
    *   Tiempo vs $N$.
    *   Nodos vs $N$.

## FASE 6: Elaboración de la Memoria (Sección 1.2 PDF)
*   **Límite:** Máximo 4 páginas (sin contar portada).
*   **Estructura Obligatoria:**
    1. **Diseño:** Representación, árbol, dimensiones, funciones de coste/cota.
    2. **Implementación:** Tipos de datos (matrices, vectores, estructuras de poda), organización del código.
    3. **Experimentación:** Definición de casos, objetivos, resultados y análisis.
    4. **PLI:** Formalización y detalles de la implementación.
    5. **Conclusiones.**

## FASE 7: Entrega Final
*   **Contenido:** `LEEME.txt`, fuentes comentadas, `ejecutar.sh`, ficheros de entrada, `memoria.pdf`.
*   **Empaquetado:** `practica4.tar`.
