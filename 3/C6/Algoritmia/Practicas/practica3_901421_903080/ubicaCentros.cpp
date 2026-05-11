/*
 * ===============================================================================
 * PRÁCTICA 3: BÚSQUEDA CON RETROCESO - Jorge Bellido(903080), Imad Habib(901421)
 * ===============================================================================
 * * Algoritmia Basica — Practica 3: Busqueda con retroceso
 * Problema: ubicacion optima de k nuevos centros de urgencias en un grafo
 * ponderado no dirigido para minimizar el peor tiempo de acceso.
 *
 * Uso: ./ubicaCentros <fichero_entrada> <fichero_salida>
 */
 
#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <chrono>
#include <iomanip>
 
using namespace std;
 
const int INF = 1e8;
 
// ---------------------------------------------------------------------------
// Estructuras de Datos
// ---------------------------------------------------------------------------

struct Contexto {
    // Parámetros principales del problema
    int num_localidades; // Numero total de localidades (nodos)
    int num_carreteras; // Numero total de carreteras (aristas)
    int num_centros_existentes; // Centros de urgencias ya existentes
    int num_nuevos_centros; // Centros de urgencias que debemos ubicar
    
    // Representación del mapa y prebúsquedas
    vector<vector<int>> distancias_minimas;   // distancias_minimas[i][j]: tiempo entre i y j
    vector<vector<int>> cota_inferior_sufijo; // cota optimista para podar el árbol
    vector<bool> localidad_tiene_centro;      // Evita construir donde ya hay un hospital
    
    // Estado global de la búsqueda óptima    
    int          mejor_coste; // Mejor coste encontrado hasta ahora (peor tiempo de acceso mínimo)
    vector<int>  mejor_solucion; // Localidades donde se ubican los nuevos centros en la mejor solución
    long long    nodos_explorados; // Contador de nodos explorados para análisis de rendimiento
};
 
// ---------------------------------------------------------------------------
// Funciones Matemáticas y Auxiliares
// ---------------------------------------------------------------------------
 
/*
 * Calcula el peor tiempo de acceso de toda la región.
 */
int calcular_peor_tiempo_regional(const vector<int>& accesos_localidades) {
    int peor_tiempo = 0;
    for (int tiempo : accesos_localidades) {
        if (tiempo > peor_tiempo) {
            peor_tiempo = tiempo;
        }
    }
    return peor_tiempo;
}
 
/*
 * Algoritmo de Floyd-Warshall para encontrar todos los caminos mínimos.
 */
void calcular_caminos_minimos(Contexto& ctx) {
    int n = ctx.num_localidades;
    for (int intermedio = 0; intermedio < n; intermedio++) {
        for (int origen = 0; origen < n; origen++) {
            if (ctx.distancias_minimas[origen][intermedio] < INF) {
                for (int destino = 0; destino < n; destino++) {
                    if (ctx.distancias_minimas[intermedio][destino] < INF) {
                        ctx.distancias_minimas[origen][destino] = min(
                            ctx.distancias_minimas[origen][destino], 
                            ctx.distancias_minimas[origen][intermedio] + ctx.distancias_minimas[intermedio][destino]
                        );
                    }
                }
            }
        }
    }
}

/*
 * Precalcula la distancia mínima desde cualquier localidad 'u' a los 
 * candidatos restantes para fortalecer la poda por cota inferior.
 */
void precalcular_podas(Contexto& ctx) {
    int n = ctx.num_localidades;
    ctx.cota_inferior_sufijo.assign(n, vector<int>(n + 1, INF));

    for (int localidad = 0; localidad < n; localidad++) {
        for (int candidato = n - 1; candidato >= 0; candidato--) {
            if (!ctx.localidad_tiene_centro[candidato]) {
                ctx.cota_inferior_sufijo[localidad][candidato] = min(
                    ctx.distancias_minimas[localidad][candidato], 
                    ctx.cota_inferior_sufijo[localidad][candidato + 1]
                );
            } else {
                ctx.cota_inferior_sufijo[localidad][candidato] = ctx.cota_inferior_sufijo[localidad][candidato + 1];
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Algoritmo Core: Backtracking
// ---------------------------------------------------------------------------
 
/*
 * Explora exhaustivamente las combinaciones de nuevos centros.
 */
void backtrack(int centros_colocados, int indice_candidato_inicial, vector<int>& solucion_actual, vector<int>& accesos_actuales, Contexto& ctx) {
    ctx.nodos_explorados++;
 
    // Caso base: Hemos colocado todos los hospitales nuevos
    if (centros_colocados == ctx.num_nuevos_centros) {
        int coste_final = calcular_peor_tiempo_regional(accesos_actuales);
        if (coste_final < ctx.mejor_coste) {
            ctx.mejor_coste = coste_final;
            ctx.mejor_solucion = solucion_actual;
        }
        return;
    }
 
    // Poda Combinatoria
    // Si quedan menos localidades disponibles que centros por poner, abandonamos.
    if (ctx.num_localidades - indice_candidato_inicial < ctx.num_nuevos_centros - centros_colocados) {
        return;
    }

    // Poda por Cota Inferior
    // Si vemos que alguna localidad no mejorará el 'mejor_coste' ni en el mejor caso posible.
    for (int localidad = 0; localidad < ctx.num_localidades; localidad++) {
        if (min(accesos_actuales[localidad], ctx.cota_inferior_sufijo[localidad][indice_candidato_inicial]) >= ctx.mejor_coste) {
            return; 
        }
    }
 
    // Copia de seguridad del estado de los accesos para poder hacer Backtrack
    vector<int> copia_accesos_seguridad = accesos_actuales; 
 
    // Exploración de ramas
    for (int candidato = indice_candidato_inicial; candidato < ctx.num_localidades; candidato++) {
        
        if (ctx.localidad_tiene_centro[candidato]) continue;

        solucion_actual.push_back(candidato);
        
        // Actualizamos los tiempos de acceso asumiendo que construimos aquí
        for (int localidad = 0; localidad < ctx.num_localidades; localidad++) {
            if (ctx.distancias_minimas[localidad][candidato] < accesos_actuales[localidad]) {
                accesos_actuales[localidad] = ctx.distancias_minimas[localidad][candidato];
            }
        }
 
        // Llamada recursiva al siguiente nivel
        backtrack(centros_colocados + 1, candidato + 1, solucion_actual, accesos_actuales, ctx);
 
        // Deshacemos el cambio (Backtrack puro)
        accesos_actuales = copia_accesos_seguridad;
        solucion_actual.pop_back();
 
        // Poda de optimalidad absoluta: 0 minutos es inmejorable
        if (ctx.mejor_coste == 0) return;
    }
}

// ---------------------------------------------------------------------------
// Lógica de I/O y Orquestación
// ---------------------------------------------------------------------------

void leer_grafo(ifstream& entrada, Contexto& ctx) {
    ctx.distancias_minimas.assign(ctx.num_localidades, vector<int>(ctx.num_localidades, INF));
    for (int i = 0; i < ctx.num_localidades; i++) {
        ctx.distancias_minimas[i][i] = 0;
    }
 
    for (int i = 0; i < ctx.num_carreteras; i++) {
        int origen, destino, tiempo;
        entrada >> origen >> destino >> tiempo;
        origen--; destino--; // Ajuste a 0-based index
        
        if (tiempo < ctx.distancias_minimas[origen][destino]) {
            ctx.distancias_minimas[origen][destino] = tiempo;
            ctx.distancias_minimas[destino][origen] = tiempo;
        }
    }
}

void leer_centros_y_accesos_iniciales(ifstream& entrada, Contexto& ctx, vector<int>& accesos_ini) {
    ctx.localidad_tiene_centro.assign(ctx.num_localidades, false);
    accesos_ini.assign(ctx.num_localidades, INF);
    vector<int> centros_existentes(ctx.num_centros_existentes);
    
    for (int i = 0; i < ctx.num_centros_existentes; i++) {
        entrada >> centros_existentes[i];
        centros_existentes[i]--; // Ajuste a 0-based index
        ctx.localidad_tiene_centro[centros_existentes[i]] = true; 
    }

    for (int localidad = 0; localidad < ctx.num_localidades; localidad++) {
        if (ctx.num_centros_existentes == 0) break; 
        
        for (int centro : centros_existentes) {
            accesos_ini[localidad] = min(accesos_ini[localidad], ctx.distancias_minimas[localidad][centro]);
        }
    }
}

void imprimir_resultado_caso(ofstream& salida, double tiempo_ms, const Contexto& ctx) {
    salida << fixed << setprecision(3);
    salida << tiempo_ms << " " << ctx.nodos_explorados << " " << ctx.mejor_coste;
    for (int localidad : ctx.mejor_solucion) {
        salida << " " << (localidad + 1); // Volver a 1-based index para la salida
    }
    salida << "\n";
}

/*
 * Orquestador que resuelve un único caso de prueba paso a paso.
 */
void resolver_caso_prueba(ifstream& entrada, ofstream& salida) {
    Contexto ctx;
    if (!(entrada >> ctx.num_localidades >> ctx.num_carreteras >> ctx.num_centros_existentes >> ctx.num_nuevos_centros)) return; 
 
    // Preparación del terreno
    leer_grafo(entrada, ctx);
    calcular_caminos_minimos(ctx);
    
    vector<int> accesos_iniciales;
    leer_centros_y_accesos_iniciales(entrada, ctx, accesos_iniciales);
    precalcular_podas(ctx);
 
    // Inicialización de la búsqueda
    ctx.mejor_coste = INF + 7;
    ctx.mejor_solucion.clear();
    ctx.nodos_explorados = 0;
    
    vector<int> solucion_actual;
    solucion_actual.reserve(ctx.num_nuevos_centros);
 
    // Ejecución y Cronometraje
    auto tiempo_inicio = chrono::high_resolution_clock::now();
    
    if (ctx.num_nuevos_centros > 0) {
        backtrack(0, 0, solucion_actual, accesos_iniciales, ctx);
    } else {
        ctx.mejor_coste = calcular_peor_tiempo_regional(accesos_iniciales);
    }
 
    auto tiempo_fin = chrono::high_resolution_clock::now();
    double tiempo_ms = chrono::duration<double, std::milli>(tiempo_fin - tiempo_inicio).count();
 
    // Salida de datos
    imprimir_resultado_caso(salida, tiempo_ms, ctx);
}
 
// ---------------------------------------------------------------------------
// Punto de Entrada Principal
// ---------------------------------------------------------------------------
 
int main(int argc, char* argv[]) {
 
    if (argc != 3) {
        cerr << "Uso: ubicaCentros <fichero_entrada> <fichero_salida>\n";
        return 1;
    }
 
    ifstream entrada(argv[1]);
    if (!entrada.is_open()) {
        cerr << "Error: no se puede abrir el fichero de entrada '" << argv[1] << "'\n";
        return 1;
    }
 
    ofstream salida(argv[2]);
    if (!salida.is_open()) {
        cerr << "Error: no se puede abrir el fichero de salida '" << argv[2] << "'\n";
        return 1;
    }
 
    int num_casos;
    if (!(entrada >> num_casos)) return 0;
 
    // Ejecución de todos los casos de prueba con medición global
    auto inicio_global = chrono::high_resolution_clock::now();

    for (int i = 0; i < num_casos; i++) {
        resolver_caso_prueba(entrada, salida);
    }

    auto fin_global = chrono::high_resolution_clock::now();
    double duracion_global = chrono::duration<double, std::milli>(fin_global - inicio_global).count();

    // Feedback final por terminal
    cout << "========================================\n";
    cout << "Procesamiento finalizado con exito.\n";
    cout << "Casos procesados: " << num_casos << "\n";
    cout << fixed << setprecision(3);
    cout << "Tiempo total de ejecucion: " << duracion_global << " ms\n";
    cout << "Resultados guardados en: " << argv[2] << "\n";
    cout << "========================================\n";

    return 0;
}