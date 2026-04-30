// Imad Habib Jali 901421
// Jorge Bellido Lobera 903080
#include <iostream>
#include <vector>
#include <cmath>
#include <algorithm>
#include <ctime>
#include <fstream>
#include <iomanip>
#include "imagen.h"
#include "geometria.h"

using namespace std;

// Ajustes de calidad y rendimiento
const int MAX_HILOS = 60000;      // Límite de hilos a colocar
const int FRECUENCIA_LOG = 500;   // Cada cuántos hilos mostramos info en consola
const double UMBRAL_LAZY = 0.7;   // Si la puntuación baja al 70%, se descarta (Lazy Eval)

// Estructura para almacenar los parámetros del programa
struct Configuracion {
    int cantidadClavos;       // N
    int candidatosPorLote;    // P
    int seleccionadosPorLote; // S
    string rutaImagenEntrada;
    string rutaArchivoSalida;
    int intensidadHilo;       // MEJORA: Intensidad/opacidad parametrizable
};

// Estructura interna para gestionar posibles líneas
struct Candidato {
    int clavoA, clavoB;
    long puntuacion;
    vector<Punto> pixeles;
};

/**
 * Parsea los argumentos de línea de comandos y devuelve la configuración.
 */
Configuracion procesarArgumentos(int argc, char* argv[]) {
    if (argc < 6) {
        cerr << "Uso: " << argv[0] << " <N_Clavos> <P_Candidatos> <S_Seleccion> <Img_In> <File_Out> [Intensidad_Hilo]" << endl;
        exit(1);
    }
    return {
        stoi(argv[1]),
        stoi(argv[2]),
        stoi(argv[3]),
        argv[4],
        argv[5],
        (argc > 6 ? stoi(argv[6]) : 5) // 5 por defecto si no se especifica la mejora
    };
}

/**
 * Calcula las coordenadas (x, y) de los clavos en disposición circular.
 */
vector<Punto> calcularPosicionClavos(int cantidad, int ancho, int alto) {
    vector<Punto> clavos(cantidad);
    double radio = min(ancho, alto) / 2.0 - 5.0; // Margen de 5px
    double cx = ancho / 2.0;
    double cy = alto / 2.0;

    for (int i = 0; i < cantidad; ++i) {
        double angulo = 2.0 * M_PI * i / cantidad;
        clavos[i] = {
            (int)(cx + radio * cos(angulo)),
            (int)(cy + radio * sin(angulo))
        };
    }
    return clavos;
}

/**
 * Calcula cuánto "aporta" una línea basándose en la oscuridad de la imagen de trabajo.
 * A mayor oscuridad restante, mayor puntuación.
 */
inline long calcularPuntuacion(const vector<Punto>& pts, const Imagen& imgTrabajo) {
    long suma = 0;
    for (const auto& p : pts) {
        // Sumamos la "oscuridad que falta por cubrir" (255 - valor actual)
        // Si el pixel es blanco (255), suma 0. Si es negro (0), suma 255.
        suma += (255 - imgTrabajo.get(p.x, p.y));
    }
    return suma;
}

/**
 * Dibuja el hilo en las imágenes con la intensidad especificada:
 * - imgTrabajo: Se aclara (restamos oscuridad) para no volver a dibujar ahí.
 * - imgResultado: Se oscurece (añadimos tinta) para generar la salida.
 */
void dibujarHilo(Imagen& imgTrabajo, Imagen& imgResultado, const vector<Punto>& puntos, int intensidad) {
    for (const auto& p : puntos) {
        // Aclarar la imagen de referencia (ya "cubrimos" esta zona)
        int nuevoValorTrabajo = min(255, imgTrabajo.get(p.x, p.y) + intensidad);
        imgTrabajo.set(p.x, p.y, nuevoValorTrabajo);

        // Oscurecer la imagen final (simulamos el hilo)
        int nuevoValorResultado = max(0, imgResultado.get(p.x, p.y) - intensidad);
        imgResultado.set(p.x, p.y, nuevoValorResultado);
    }
}

/**
 * Genera un lote de P líneas aleatorias y calcula su puntuación inicial.
 */
vector<Candidato> generarCandidatos(const Configuracion& config, const vector<Punto>& clavos, const Imagen& imgTrabajo) {
    vector<Candidato> candidatos;
    candidatos.reserve(config.candidatosPorLote);

    for (int k = 0; k < config.candidatosPorLote; ++k) {
        int c1 = rand() % config.cantidadClavos;
        int c2 = rand() % config.cantidadClavos;

        // Filtro geométrico: evitar líneas muy cortas o adyacentes
        if (abs(c1 - c2) < (config.cantidadClavos / 12)) continue;

        // Obtener píxeles de la línea (algoritmo de Bresenham)
        vector<Punto> linea = obtenerLineaBresenham(clavos[c1].x, clavos[c1].y, clavos[c2].x, clavos[c2].y);
        
        long score = calcularPuntuacion(linea, imgTrabajo);
        candidatos.push_back({c1, c2, score, linea});
    }
    return candidatos;
}

/**
 * Función principal que ejecuta el algoritmo Greedy con evaluación perezosa (Lazy Eval).
 */
void procesarImagen(const Configuracion& config, Imagen& original, Imagen& imgTrabajo, Imagen& imgResultado, const vector<Punto>& clavos) {
    cout << "--- RENDERIZADO OPTIMIZADO (LAZY EVAL) ---" << endl;
    
    vector<pair<int, int>> hilosFinales;
    int hilosTotales = 0;
    double errorActual = calcularErrorGlobal(original, imgResultado);
    double errorInicial = errorActual; // Para estadísticas finales
    clock_t inicio = clock();

    while (hilosTotales < MAX_HILOS) {
        // 1. Feedback visual
        if (hilosTotales % FRECUENCIA_LOG == 0) {
            cout << "\rHilos: " << setw(5) << hilosTotales << " | Error: " << (long)errorActual << flush;
        }

        // 2. Fase A: Generar y Ordenar Candidatos
        vector<Candidato> candidatos = generarCandidatos(config, clavos, imgTrabajo);
        
        // Ordenamos de mayor puntuación a menor
        sort(candidatos.begin(), candidatos.end(), [](const Candidato& a, const Candidato& b) {
            return a.puntuacion > b.puntuacion;
        });

        // 3. Fase B: Selección Inteligente con Re-validación
        int seleccionadosEnRonda = 0;
        for (int i = 0; i < config.seleccionadosPorLote && hilosTotales < MAX_HILOS; ++i) {
            if (i >= (int)candidatos.size()) break;

            const auto& mejor = candidatos[i];

            // OPTIMIZACIÓN CLAVE: "Lazy Re-evaluation"
            long scoreActual = calcularPuntuacion(mejor.pixeles, imgTrabajo);

            // Si la puntuación ha caído drásticamente respecto al cálculo original, descartamos
            if (scoreActual < mejor.puntuacion * UMBRAL_LAZY) continue;

            // Filtro de calidad mínima absoluta (evitar ruido)
            if (scoreActual < 10) continue;

            // 4. Aplicar cambios usando la intensidad configurada
            dibujarHilo(imgTrabajo, imgResultado, mejor.pixeles, config.intensidadHilo);
            
            // Guardar secuencia usando make_pair para evitar errores en Mingw64
            hilosFinales.push_back(make_pair(mejor.clavoA, mejor.clavoB));
            hilosTotales++;
            seleccionadosEnRonda++;
        }

        // CONDICIÓN DE PARADA 1: Si no encontramos ningún hilo útil en toda la ronda
        if (seleccionadosEnRonda == 0) break;

        // CONDICIÓN DE PARADA 2: Estabilización del error global 
        double nuevoError = calcularErrorGlobal(original, imgResultado);
        if (nuevoError >= errorActual) {
            cout << "\n\n[!] Parada temprana: El error global se ha estabilizado (dejó de reducirse)." << endl;
            errorActual = nuevoError;
            break;
        }
        errorActual = nuevoError; // Actualizamos el error para la siguiente iteración
    }

    // 5. Guardado de resultados y estadísticas
    double tiempo = (double)(clock() - inicio) / CLOCKS_PER_SEC;

    // FORMATO DE SALIDA TEXTUAL RESUMIDA ESTRICTA
    cout << "\n\n------------------------------------------------" << endl;
    cout << "RESUMEN DE LA EJECUCION" << endl;
    cout << "------------------------------------------------" << endl;
    cout << "Parametros utilizados:" << endl;
    cout << "  - Clavos (N): " << config.cantidadClavos << endl;
    cout << "  - Hilos generados por lote (p): " << config.candidatosPorLote << endl;
    cout << "  - Hilos seleccionados (s): " << config.seleccionadosPorLote << endl;
    cout << "  - Intensidad del hilo: " << config.intensidadHilo << endl;
    cout << "\nResultados:" << endl;
    cout << "  - Tiempo de ejecucion: " << tiempo << "s" << endl;
    cout << "  - Numero de lineas dibujadas: " << hilosTotales << endl;
    cout << "  - Error final (SAD): " << (long)errorActual << endl;
    cout << "  - Mejora sobre lienzo blanco: " << (100.0 * (errorInicial - errorActual) / errorInicial) << "%" << endl;
    cout << "------------------------------------------------" << endl;

    ofstream f(config.rutaArchivoSalida);
    for (const auto& p : hilosFinales) f << p.first << " " << p.second << endl;
    guardarPGM(config.rutaArchivoSalida + ".pgm", imgResultado);
}

int main(int argc, char* argv[]) {
    srand(time(NULL));

    // 1. Configuración
    Configuracion config = procesarArgumentos(argc, argv);

    // 2. Carga de Imágenes
    Imagen original;
    if (!cargarPGM(config.rutaImagenEntrada, original)) {
        cerr << "Error al cargar la imagen." << endl;
        return 1;
    }

    // Preparar lienzos
    Imagen imgTrabajo = original; // Imagen que iremos "borrando" virtualmente
    Imagen imgResultado(original.ancho, original.alto, 255); // Lienzo blanco que iremos oscureciendo

    // 3. Geometría
    vector<Punto> clavos = calcularPosicionClavos(config.cantidadClavos, original.ancho, original.alto);

    // 4. Ejecución
    procesarImagen(config, original, imgTrabajo, imgResultado, clavos);

    return 0;
}