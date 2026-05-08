# Generación y Verificación de Caminos de Prueba

Este directorio contiene los scripts y resultados correspondientes a la fase de generación de caminos de prueba para la automatización con **Espresso**, asegurando el cumplimiento del criterio de cobertura **Edge-Pair (Pares de Aristas)** requerido en la Práctica 6.

## 1. El Generador: `generador_caminos_pruebas.py`

El script principal es el encargado de leer los pares de aristas válidos y trazar las rutas que la herramienta Espresso deberá ejecutar en la aplicación Android real.

### 📥 Entrada: `pares_entrada.txt`
El script toma como entrada una "Lista Blanca" de pares permitidos. Estos 379 pares provienen del análisis exhaustivo previo (`ParesAristas/Análisis_Pares_Aristas.md`), donde se filtraron las transiciones que violaban la pila de actividades (Back Stack) de Android. 
**Regla estricta:** El generador *solo* puede moverse de una arista a otra si esa combinación exacta existe en el archivo de entrada.

### ⚙️ Lógica Voraz (Greedy) y Eficiencia
El algoritmo implementa una estrategia **voraz consciente del estado**:
*   **Menos es Más:** El objetivo de la automatización UI es minimizar el número de tests (`@Test` en Espresso) para que la suite no tarde horas en ejecutarse.
*   **Caminos Largos vs. Cortos:** En lugar de hacer un camino corto (N1 -> N2 -> N3) por cada par de aristas, el script prefiere encadenar transiciones válidas para crear caminos largos (hasta 25 aristas) que "barran" y cubran múltiples pares pendientes de una sola pasada.
*   **Reducción Drástica:** Gracias a esta optimización, se logró reducir el volumen de pruebas necesarias de más de 140 caminos ineficientes a **solo 87 caminos optimizados**.

### 🛡️ Guardas de Interfaz de Usuario (UI)
El generador no es ciego ante la lógica de negocio. Se ha programado para respetar las "guardas" de la aplicación. Por ejemplo:
*   Si el algoritmo desea saltar a la Arista 11 (Selección de Quads libres), primero verifica que en el camino actual ya se hayan recorrido las aristas 20 o 20b (Introducir Fechas).
*   Esto garantiza que el 100% de los caminos generados son **ejecutables** en el emulador sin que la aplicación bloquee la navegación por falta de datos obligatorios.

### 📤 Salida: `caminos_resultantes.txt`
Un archivo de texto limpio donde cada línea es un array separado por comas (ej. `4,20,11,12,10`). Cada línea representa un flujo de navegación continuo y lógicamente válido que será programado como un escenario de prueba en Espresso.

---

## 2. El Verificador: `verificar_cobertura.py`

Para garantizar la integridad académica de la práctica, se ha incluido este script auditor.

### Propósito
Su única misión es comparar la lista de pares exigidos (`pares_entrada.txt`) contra los caminos generados (`caminos_resultantes.txt`). Extrae cada par consecutivo de los caminos y verifica matemáticamente que todos los casos estructuralmente posibles han sido visitados al menos una vez.

### Ejecución
```bash
python verificar_cobertura.py
```
**Resultado esperado:**
* Total de pares esperados: 379
* Total de pares únicos cubiertos: 379
* Pares sin cubrir: 0
* ¡100% de Cobertura Edge-Pair alcanzada!
