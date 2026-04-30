// Imad Habib Jali - 901421
// Jorge Bellido Lobera - 903080
#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <unordered_set>
#include <chrono>
#include <cstdlib> // Necesario para usar exit()
#include "particion_palabras.hpp"

using namespace std;
using namespace std::chrono;

// Función para cargar y validar los parámetros
void cargarParametros(int argc, char* argv[], int& variante, string& ruta_diccionario, string& ruta_texto) {
    if (argc != 4) {
        cerr << "Uso: " << argv[0] << " <variante> <fichero_diccionario> <fichero_texto>" << endl;
        cerr << "Variantes: 1 (Recursiva), 2 (Memoizacion), 3 (Tabulacion)" << endl;
        exit(1); // Detiene el programa con código de error
    }

    variante = stoi(argv[1]);
    ruta_diccionario = argv[2];
    ruta_texto = argv[3];
}

// Función para cargar el diccionario
unordered_set<string> cargarDiccionario(const string& ruta_diccionario) {
    unordered_set<string> diccionario;
    ifstream arch_dic(ruta_diccionario);
    
    if (!arch_dic.is_open()) {
        cerr << "Error al abrir el diccionario: " << ruta_diccionario << endl;
        exit(1);
    }
    
    string palabra;
    while (arch_dic >> palabra) {
        diccionario.insert(palabra);
    }
    arch_dic.close();
    
    return diccionario;
}

// Función para cargar el texto a evaluar
vector<string> cargarTextos(const string& ruta_texto) {
    vector<string> textos;
    ifstream arch_txt(ruta_texto);
    
    if (!arch_txt.is_open()) {
        cerr << "Error al abrir el fichero de textos: " << ruta_texto << endl;
        exit(1);
    }
    
    string linea;
    while (getline(arch_txt, linea)) {
        if (!linea.empty()) {
            textos.push_back(linea);
        }
    }
    arch_txt.close();
    
    return textos;
}

// Función para imprimir los resultados
void imprimirResultados(size_t indice, size_t longitud, const vector<string>& resultados, long long duracion) {
    cout << "Texto " << indice << " | Longitud: " << longitud << " | Soluciones: " << resultados.size() << endl;
    cout << "Tiempo de ejecucion: " << duracion << " microsegundos" << endl;
    
    if (resultados.empty()) {
        cout << "  [!] No se encontro ninguna particion valida en el diccionario." << endl;
    } else {
        cout << "  Frases validas generadas:" << endl;
        for (const string& frase : resultados) {
            cout << "    - " << frase << endl;
        }
    }
    
    cout << "--------------------------------------------------" << endl;
}

// Función para procesar el texto, medir el tiempo y llamar a imprimir
void procesarTextoYMedir(int variante, const vector<string>& textos, const unordered_set<string>& diccionario) {
    for (size_t i = 0; i < textos.size(); ++i) {
        vector<string> resultados;
        
        // Iniciamos el cronómetro
        auto inicio = high_resolution_clock::now();

        if (variante == 1) {
            resultados = variante1_recursiva(textos[i], diccionario);
        } else if (variante == 2) {
            resultados = variante2_memoizacion(textos[i], diccionario);
        } else if (variante == 3) {
            resultados = variante3_tabulacion(textos[i], diccionario);
        } else {
            cerr << "Variante no valida. Elija 1, 2 o 3." << endl;
            exit(1);
        }

        // Paramos el cronómetro
        auto fin = high_resolution_clock::now();
        auto duracion = duration_cast<microseconds>(fin - inicio).count();

        // Llamada a la función de imprimir resultados
        imprimirResultados(i + 1, textos[i].length(), resultados, duracion);
    }
}

// MAIN PRINCIPAL
int main(int argc, char* argv[]) {
    int variante;
    string ruta_diccionario;
    string ruta_texto;

    // Cargar parámetros
    cargarParametros(argc, argv, variante, ruta_diccionario, ruta_texto);

    // Cargar diccionario
    unordered_set<string> diccionario = cargarDiccionario(ruta_diccionario);

    // Cargar textos
    vector<string> textos = cargarTextos(ruta_texto);

    // Procesar cada texto, medir tiempos e imprimir
    procesarTextoYMedir(variante, textos, diccionario);

    return 0;
}