#include <iostream>
#include <vector>
#include <fstream>
#include <chrono>
#include <algorithm>
#include <numeric>

using namespace std;
using namespace std::chrono;

/**
 * Práctica 4: Ramificación y Poda - Formación de Equipos Óptimos
 * Autores: Imad Habib Jali (901421) y Jorge Bellido Lobera (903080)
 * 
 * Este programa resuelve el problema de agrupar N participantes en equipos de 3
 * minimizando el conflicto total utilizando un algoritmo de Branch & Bound.
 */

// Valor constante para representar el infinito
const int INF = 1e9;

// Variables globales para el problema
int N;                               // Número total de participantes
vector<vector<int>> C;               // Matriz de conflicto original leída del archivo
vector<vector<int>> W;               // Matriz simetrizada: W[i][j] = conflicto mutuo entre i y j
vector<vector<int>> minW;            // Precalculo: minW[i][k] es la suma de los k menores conflictos de i

int best_cost;                       // Almacena el mejor coste (mínimo conflicto) hallado hasta el momento
long long nodes_generated;           // Contador para el número total de nodos (llamadas) en el árbol

/**
 * Fase inicial voraz (Greedy) para obtener una buena cota superior (Upper Bound).
 * Esto permite podar ramas ineficientes desde el principio del proceso de búsqueda.
 */
void greedy_init() {
    vector<vector<int>> teams(N / 3); // Estructura temporal para los equipos
    int current_total_cost = 0;        // Coste acumulado de la solución voraz
    
    // Asignamos a cada participante i al mejor equipo disponible en ese momento
    for (int i = 0; i < N; ++i) {
        int best_t = -1;
        int min_added = INF;
        for (int t = 0; t < N / 3; ++t) {
            if (teams[t].size() < 3) { // Si el equipo tiene hueco
                int added = 0;
                // Calculamos cuánto conflicto añade el participante i al unirse al equipo t
                for (int member : teams[t]) added += W[i][member];
                if (added < min_added) {
                    min_added = added;
                    best_t = t;
                }
                // Si encontramos un equipo vacío, lo usamos y paramos para romper simetrías
                if (teams[t].empty()) break; 
            }
        }
        teams[best_t].push_back(i);    // Asignamos i al equipo elegido
        current_total_cost += min_added; // Sumamos el conflicto generado
    }
    best_cost = current_total_cost;    // Establecemos la cota superior inicial
}

/**
 * Algoritmo principal de Ramificación y Poda (Branch & Bound) usando DFS.
 * p: Índice del participante que estamos intentando asignar actualmente.
 * current_cost: Conflicto total real acumulado por las asignaciones ya cerradas.
 * current_lb2: Valor de la cota inferior multiplicado por 2 (para trabajar con enteros).
 * teams: Estado de ocupación de los equipos.
 * active_teams: Número de equipos que ya tienen al menos un integrante.
 */
void solve(int p, int current_cost, int current_lb2, vector<vector<int>>& teams, int active_teams) {
    nodes_generated++; // Incrementamos el contador de nodos generados
    
    // CASO BASE: Hemos asignado a todos los participantes
    if (p == N) {
        if (current_cost < best_cost) {
            best_cost = current_cost; // Actualizamos el óptimo global
        }
        return;
    }
    
    // PODA POR COTA: Si la estimación optimista ya es peor que el mejor resultado, podar rama
    int lb = (current_lb2 + 1) / 2; // Redondeo hacia arriba del valor real de la cota
    if (lb >= best_cost) return;
    
    // Definimos las posibles elecciones (equipos a los que p puede unirse)
    struct Choice {
        int team_idx;
        int added_cost;
        bool is_new;
    };
    
    vector<Choice> choices;
    // Opción A: Unirse a un equipo ya activo que no esté lleno
    for (int t = 0; t < active_teams; ++t) {
        if (teams[t].size() < 3) {
            int added = 0;
            for (int m : teams[t]) added += W[p][m]; // Coste real que añadiría
            choices.push_back({t, added, false});
        }
    }
    // Opción B: Iniciar un equipo nuevo (si quedan disponibles)
    if (active_teams < N / 3) {
        choices.push_back({active_teams, 0, true});
    }
    
    // HEURÍSTICA DE RAMIFICACIÓN: Ordenamos las opciones por menor coste añadido (greedy local)
    sort(choices.begin(), choices.end(), [](const Choice& a, const Choice& b) {
        return a.added_cost < b.added_cost;
    });
    
    // Exploramos cada opción válida
    for (const auto& c : choices) {
        int next_cost = current_cost + c.added_cost; // Nuevo coste real
        
        // ACTUALIZACIÓN DINÁMICA DE LA COTA INFERIOR (Incremental)
        int next_lb2 = current_lb2;
        next_lb2 -= minW[p][2]; // El participante p ya no cuenta como "totalmente libre"
        
        int t = c.team_idx;
        int sz = teams[t].size();
        for (int m : teams[t]) {
            next_lb2 += 2 * W[p][m]; // Añadimos el conflicto bilateral real al doble (p-m y m-p)
            
            // El compañero m ahora necesita un socio menos, actualizamos su estimación optimista
            int m_needs_before = 3 - sz;
            int m_needs_after = 3 - (sz + 1);
            next_lb2 -= minW[m][m_needs_before];
            next_lb2 += minW[m][m_needs_after];
        }
        // El propio participante p ahora necesita menos socios futuros según el tamaño del equipo t
        int p_needs_after = 3 - (sz + 1);
        next_lb2 += minW[p][p_needs_after];
        
        // Solo llamamos a la recursión si la rama sigue siendo prometedora después del cambio
        if ((next_lb2 + 1) / 2 < best_cost) {
            teams[t].push_back(p); // Marcar como asignado
            solve(p + 1, next_cost, next_lb2, teams, c.is_new ? active_teams + 1 : active_teams);
            teams[t].pop_back();   // Retroceder (Backtracking)
        }
    }
}

/**
 * Función que encapsula la lógica para resolver un único caso de prueba.
 */
void process_case(ifstream& in, ofstream& out) {
    C.assign(N, vector<int>(N)); // Inicializar matrices
    W.assign(N, vector<int>(N));
    
    // Lectura de la matriz de conflictos del participante i hacia j
    for (int i = 0; i < N; ++i) {
        for (int j = 0; j < N; ++j) {
            if (!(in >> C[i][j])) return;
        }
    }
    
    // Simetrización de la matriz: W[i][j] es el coste total si i y j están en el mismo equipo
    for (int i = 0; i < N; ++i) {
        for (int j = 0; j < N; ++j) {
            W[i][j] = (i == j) ? 0 : C[i][j] + C[j][i];
        }
    }
    
    // Precalculamos las sumas de los k menores conflictos para cada persona
    minW.assign(N, vector<int>(3, 0));
    for (int i = 0; i < N; ++i) {
        vector<int> row;
        for (int j = 0; j < N; ++j) {
            if (i != j) row.push_back(W[i][j]);
        }
        sort(row.begin(), row.end()); // Ordenamos conflictos de i con todos los demás
        minW[i][1] = row[0];          // El menor conflicto
        minW[i][2] = row[0] + row[1]; // Los dos menores conflictos
    }
    
    greedy_init(); // Obtener cota superior inicial
    nodes_generated = 0;
    vector<vector<int>> teams(N / 3); // Estado de los equipos vacío
    
    // Cálculo de la cota inferior inicial: todos los participantes necesitan 2 socios más
    int initial_lb2 = 0;
    for (int i = 0; i < N; ++i) initial_lb2 += minW[i][2];
    
    // Medición de tiempo de ejecución
    auto start_time = high_resolution_clock::now();
    solve(0, 0, initial_lb2, teams, 0); // Iniciar búsqueda desde participante 0
    auto end_time = high_resolution_clock::now();
    auto duration = duration_cast<milliseconds>(end_time - start_time).count();
    
    // Escritura de resultados en el archivo de salida: tiempo (ms), nodos, coste mínimo
    out << duration << " " << nodes_generated << " " << best_cost << endl;
}

/**
 * Punto de entrada del programa. Gestiona la apertura de archivos y el bucle de casos.
 */
int main(int argc, char* argv[]) {
    // Verificación de argumentos de línea de comandos
    if (argc != 3) {
        cerr << "Uso: " << argv[0] << " <archivo_entrada> <archivo_salida>" << endl;
        return 1;
    }
    
    ifstream in(argv[1]);
    ofstream out(argv[2]);
    if (!in || !out) {
        cerr << "Error al abrir los archivos de entrada/salida." << endl;
        return 1;
    }

    // Leemos N y procesamos casos hasta fin de archivo
    while (in >> N) {
        process_case(in, out);
    }
    
    return 0;
}
