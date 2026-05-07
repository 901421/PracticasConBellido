# AUDITORÍA FINAL - Práctica 4

## 1. Cumplimiento del Enunciado
- **Tarea 1: Diseño:** El algoritmo de Ramificación y Poda está diseñado con una cota inferior (Lower Bound) basada en los conflictos mínimos posibles para cada participante. El árbol de búsqueda rompe simetrías asignando participantes en orden y limitando la creación de nuevos equipos.
- **Tarea 2: Implementación:** Programa `formarEquipos` en C++ que lee de archivo y escribe en archivo. Formato de salida cumple con lo requerido (tiempo, nodos, óptimo).
- **Tarea 3: Experimentación:** Se han probado casos de N=6, 9, 12, 15. Los resultados coinciden con el modelo PLI.
- **Tarea 4: Programación Lineal:** Script `pli.py` implementado con PuLP. Formalización correcta.

## 2. Calidad del Código
- **Correctitud:** Verificada contra PLI.
- **Eficiencia:** El uso de una cota inferior dinámica (actualizada en O(S_t) en lugar de recalcular de cero) permite procesar N=15 en <20ms.
- **Robustez:** Gestión de archivos básica pero funcional.
- **Legibilidad:** Nombres de variables claros (C, W, lb, nodes_generated). Comentarios añadidos.

## 3. Problemas Detectados y Soluciones
| Problema | Severidad | Impacto | Estado |
| --- | --- | --- | --- |
| Falta de cota inferior fuerte inicialmente | Media | Rendimiento pobre en N >= 15 | Corregido |
| Bug en `pli.py` (uso de `time.time()`) | Baja | Error de ejecución | Corregido |
| `ejecutar.sh` usa sintaxis Bash en Windows | Baja | Fallo al ejecutar en PS | Mitigado (instrucciones en LEEME) |
| Falta Memoria Técnica en LaTeX | Alta | No se puede entregar | Pendiente |

## 4. Riesgos Técnicos
- **Escalabilidad:** Para N > 21, el Branch & Bound podría tardar significativamente a pesar de la cota. Sin embargo, para los propósitos de la práctica (N usualmente hasta 18-21), es suficiente.
- **Entorno:** PuLP debe estar instalado para `pli.py`. Se debe mencionar en `LEEME.txt`.

## 5. Conclusión
El proyecto está técnicamente sólido en cuanto a implementación. La prioridad ahora es generar la documentación (Memoria LaTeX) y asegurar que el paquete de entrega cumple con la estructura solicitada.
