// Imad Habib Jali - 901421
// Jorge Bellido Lobera - 903080
#ifndef PARTICION_PALABRAS_HPP
#define PARTICION_PALABRAS_HPP

#include <vector>
#include <string>
#include <unordered_set>
#include <unordered_map>

/**
 * @brief Variante 1: Solución por Fuerza Bruta (Recursiva Pura).
 * * Explora todas las combinaciones posibles de partición de la cadena.
 * Tiene una complejidad temporal exponencial O(2^n) en el peor de los casos 
 * debido a la superposición de subproblemas (se recalculan los mismos sufijos).
 * * @param var Cadena de texto que se desea segmentar.
 * @param diccionario Conjunto de palabras válidas.
 * @return std::vector<std::string> Lista con todas las frases válidas formadas.
 */
std::vector<std::string> variante1_recursiva(std::string var, const std::unordered_set<std::string>& diccionario);

/**
 * @brief Función auxiliar interna para la Variante 2.
 * * Realiza la recursividad pasando una estructura de datos por referencia
 * para actuar como caché (memoria) y evitar recalcular subproblemas.
 * * @param var Subcadena de texto actual a evaluar.
 * @param diccionario Conjunto de palabras válidas.
 * @param memoria Mapa que almacena los resultados ya calculados (Subcadena -> Soluciones).
 * @return std::vector<std::string> Lista con todas las frases válidas formadas.
 */
std::vector<std::string> variante2_memo_aux(std::string var, const std::unordered_set<std::string>& diccionario, std::unordered_map<std::string, std::vector<std::string>>& memoria);

/**
 * @brief Variante 2: Programación Dinámica Top-Down (Memoización).
 * * Función envoltorio (wrapper) pública. Inicializa la estructura de memoria
 * vacía y llama a la función auxiliar para comenzar la recursión. Mejora
 * drásticamente el rendimiento frente a la Variante 1 al evitar el recálculo.
 * * @param var Cadena de texto que se desea segmentar.
 * @param diccionario Conjunto de palabras válidas.
 * @return std::vector<std::string> Lista con todas las frases válidas formadas.
 */
std::vector<std::string> variante2_memoizacion(std::string var, const std::unordered_set<std::string>& diccionario);

/**
 * @brief Variante 3: Programación Dinámica Iterativa Bottom-Up (Tabulación).
 * * Solución óptima que elimina por completo la recursividad. Construye la 
 * solución de izquierda a derecha utilizando un vector (tabla) de memoria,
 * previniendo posibles desbordamientos de pila (stack overflow) en cadenas
 * muy largas.
 * * @param var Cadena de texto que se desea segmentar.
 * @param diccionario Conjunto de palabras válidas.
 * @return std::vector<std::string> Lista con todas las frases válidas formadas.
 */
std::vector<std::string> variante3_tabulacion(std::string var, const std::unordered_set<std::string>& diccionario);

#endif // PARTICION_PALABRAS_HPP