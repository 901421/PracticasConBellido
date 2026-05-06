// Imad Habib Jali - 901421
// Jorge Bellido Lobera - 903080
#include "particion_palabras.hpp"

using namespace std;

// ============================================================================
// VARIANTE 1: RECURSIVA PURA (Fuerza Bruta)
// ============================================================================
vector<string> variante1_recursiva(string var, const unordered_set<string>& diccionario) {
    // Caso Base: Si la cadena está vacía, hemos segmentado todo el texto con éxito.
    if (var.empty()) {
        return {""};
    }

    vector<string> resultados;

    // Exploración: Generamos todos los prefijos posibles de la cadena actual.
    for (int i = 1; i <= var.length(); ++i) {
        string prefijo = var.substr(0, i);

        // Poda / Comprobación: Si el prefijo es una palabra válida del diccionario...
        if (diccionario.count(prefijo)) {
            // Extraemos el resto de la cadena (sufijo) y lo procesamos recursivamente.
            string sufijo = var.substr(i);
            vector<string> particionesSufijo = variante1_recursiva(sufijo, diccionario);

            // Reconstrucción de la solución: 
            for (const string& particion : particionesSufijo) {
                // Controlamos los espacios: si es la última palabra, no añadimos espacio final.
                string espacio = particion.empty() ? "" : " ";
                resultados.push_back(prefijo + espacio + particion);
            }
        }
    }

    return resultados;
}

// ============================================================================
// VARIANTE 2: RECURSIVA CON MEMOIZACIÓN (Top-Down)
// ============================================================================

// --- Función Auxiliar (Oculta la complejidad de la memoria al usuario) ---
vector<string> variante2_memo_aux(string var, const unordered_set<string>& diccionario, unordered_map<string, vector<string>>& memoria) {
    // Caso Base
    if (var.empty()) {
        return {""};
    }

    // Comprobación de Caché (Memoización):
    // Si la subcadena 'var' ya fue procesada anteriormente, retornamos la solución almacenada en O(1)
    if (memoria.count(var)) {
        return memoria[var];
    }

    vector<string> resultados;

    // Exploración (Idéntica a la Variante 1)
    for (int i = 1; i <= var.length(); ++i) {
        string prefijo = var.substr(0, i);

        if (diccionario.count(prefijo)) {
            string sufijo = var.substr(i);
            
            // Pasamos la estructura de memoria por referencia en cada llamada
            vector<string> particionesSufijo = variante2_memo_aux(sufijo, diccionario, memoria);

            for (const string& particion : particionesSufijo) {
                string espacio = particion.empty() ? "" : " ";
                resultados.push_back(prefijo + espacio + particion);
            }
        }
    }

    // Almacenamiento en Caché:
    // Guardamos las soluciones encontradas para esta subcadena ANTES de retornar.
    memoria[var] = resultados;
    return resultados;
}

// --- Función Wrapper (Interfaz Pública) ---
vector<string> variante2_memoizacion(string var, const unordered_set<string>& diccionario) {
    // Inicializamos la caché (tabla hash) y disparamos la recursión
    unordered_map<string, vector<string>> memo;
    return variante2_memo_aux(var, diccionario, memo);
}

// ============================================================================
// VARIANTE 3: PROGRAMACIÓN DINÁMICA TABULADA (Bottom-Up)
// ============================================================================
std::vector<std::string> variante3_tabulacion(std::string var, const std::unordered_set<std::string>& diccionario){
    int varSize = var.length();
    
    // Tabla DP: dp[i] almacenará todas las frases válidas que se pueden formar 
    // utilizando exactamente los primeros 'i' caracteres de la cadena 'var'.
    vector<vector<string>> dp(varSize + 1);
    
    // Caso Base: Un prefijo de longitud 0 se considera una partición válida (cadena vacía).
    dp[0] = {""};

    // Construcción Bottom-Up: Llenamos la tabla de izquierda a derecha.
    for (int i = 1; i <= varSize; ++i) {
        
        // Transición de Estados: Buscamos un punto de corte 'j' anterior a 'i'.
        for (int j = 0; j < i; ++j) {
            
            // Verificamos si logramos formar frases válidas hasta el índice 'j'.
            if (!dp[j].empty()) {
                
                // Extraemos el sufijo que va desde el punto de corte 'j' hasta 'i'
                string sufijo = var.substr(j, i - j); 
                
                // Si el trozo restante es una palabra válida en el diccionario...
                if (diccionario.count(sufijo)) {
                    
                    // ... concatenamos esta nueva palabra a todas las frases válidas 
                    // que ya habíamos construido hasta el punto 'j'.
                    for (const string& frasePrevia : dp[j]) {
                        string espacio = frasePrevia.empty() ? "" : " ";
                        dp[i].push_back(frasePrevia + espacio + sufijo);
                    }
                }
            }
        }
    }

    // Solución Final: La última posición de la tabla contiene todas las combinaciones válidas.
    return dp[varSize];
}