// Imad Habib Jali 901421
// Jorge Bellido Lobera 903080
#include "imagen.h"
#include <fstream>
#include <iostream>
#include <cmath>
#include <limits>
#include <cctype> // Para isspace()
#include <string>

using namespace std;

static void leerDatoSeguro(ifstream& archivo, int& valor) {
    // Saltamos espacios iniciales
    archivo >> ws;
    
    // Mientras encontremos un comentario, saltamos la línea entera
    while (archivo.peek() == '#') {
        archivo.ignore(numeric_limits<streamsize>::max(), '\n');
        archivo >> ws; // Saltamos espacios después del comentario
    }
    
    // Leemos el dato real
    archivo >> valor;
}

Imagen::Imagen() : ancho(0), alto(0), maxGris(255) {}

Imagen::Imagen(int w, int h, int val) : ancho(w), alto(h), maxGris(255) {
    // Inicializamos el vector con el tamaño correcto y el color de fondo
    pixeles.assign(w * h, val);
}

int Imagen::get(int x, int y) const {
    // Protección de límites: Retorna blanco (255) si está fuera de rango
    if (x < 0 || x >= ancho || y < 0 || y >= alto) {
        return 255;
    }
    // Mapeo 2D -> 1D
    return pixeles[y * ancho + x];
}

void Imagen::set(int x, int y, int valor) {
    // Solo escribimos si estamos dentro de los límites
    if (x >= 0 && x < ancho && y >= 0 && y < alto) {
        pixeles[y * ancho + x] = valor;
    }
}

bool cargarPGM(const string& nombreArchivo, Imagen& img) {
    ifstream archivo(nombreArchivo, ios::binary);
    if (!archivo.is_open()) return false;

    // 1. Leer número mágico (P2 o P5)
    string tipo;
    archivo >> tipo;
    if (tipo != "P2" && tipo != "P5") return false;

    // 2. Leer dimensiones y valor máximo (saltando comentarios de forma robusta)
    leerDatoSeguro(archivo, img.ancho);
    leerDatoSeguro(archivo, img.alto);
    leerDatoSeguro(archivo, img.maxGris);

    // Redimensionar el vector de píxeles
    long totalPixeles = (long)img.ancho * img.alto;
    img.pixeles.resize(totalPixeles);

    // 3. Leer datos de la imagen según el formato
    if (tipo == "P2") { // --- Formato ASCII (Texto) ---
        for (int i = 0; i < totalPixeles; ++i) {
            archivo >> img.pixeles[i];
        }

    } else { // --- Formato Binario (P5) ---
        //Saltar exactamente hasta el inicio de los datos binarios.
        // isspace() saltará espacios, tabs, y el \n (o \r\n) final del header.
        while (isspace(archivo.peek())) {
            archivo.get();
        }

        // Leer bloque de bytes
        vector<unsigned char> buffer(totalPixeles);
        archivo.read((char*)buffer.data(), buffer.size());

        // Verificar si la lectura fue exitosa
        if (!archivo) return false;

        // Convertir de unsigned char (1 byte) a int (4 bytes)
        for (size_t i = 0; i < buffer.size(); ++i) {
            img.pixeles[i] = static_cast<int>(buffer[i]);
        }
    }

    return true;
}

bool guardarPGM(const string& nombreArchivo, const Imagen& img) {
    ofstream archivo(nombreArchivo);
    if (!archivo.is_open()) return false;

    // Escribir cabecera estándar PGM (Formato P2 Texto)
    archivo << "P2\n" 
            << img.ancho << " " << img.alto << "\n" 
            << "255\n";

    // Escribir cuerpo de la imagen
    long totalPixeles = (long)img.ancho * img.alto;
    for (int i = 0; i < totalPixeles; ++i) {
        archivo << img.pixeles[i];
        
        // Formato visual: Salto de línea al final de cada fila de la imagen,
        // espacio entre píxeles normales.
        if ((i + 1) % img.ancho == 0) {
            archivo << "\n";
        } else {
            archivo << " ";
        }
    }
    
    return true;
}

double calcularErrorGlobal(const Imagen& original, const Imagen& generada) {
    // Verificación de seguridad: Las dimensiones deben coincidir
    if (original.ancho != generada.ancho || original.alto != generada.alto) {
        cerr << "[Error] Las dimensiones de las imagenes no coinciden para calcular el error." << endl;
        return -1.0; // Código de error
    }

    double errorAcumulado = 0.0;
    long totalPixeles = (long)original.ancho * original.alto;

    // Calcular suma de diferencias absolutas
    for (int i = 0; i < totalPixeles; ++i) {
        int dif = abs(original.pixeles[i] - generada.pixeles[i]);
        errorAcumulado += dif;
    }

    return errorAcumulado;
}