// Imad Habib Jali 901421
// Jorge Bellido Lobera 903080
#ifndef IMAGEN_H
#define IMAGEN_H

#include <string>
#include <vector>

/**
 * @struct Imagen
 * @brief Representa una imagen en escala de grises mediante una matriz de píxeles linealizada[cite: 355].
 */
struct Imagen {
    int ancho;      ///< Número de columnas de la imagen.
    int alto;       ///< Número de filas de la imagen.
    int maxGris;    ///< Valor máximo de intensidad (típicamente 255)[cite: 358].
    std::vector<int> pixeles; ///< Contenedor lineal de los valores de gris de cada píxel.

    /**
     * @brief Constructor por defecto. 
     * Crea una imagen con dimensiones 0 y vector de píxeles vacío.
     */
    Imagen();

    /**
     * @brief Constructor con dimensiones.
     * Reserva memoria para una imagen de w x h y la inicializa con un valor de gris constante.
     * @param w Ancho de la imagen.
     * @param h Alto de la imagen.
     * @param val Valor de gris inicial para todos los píxeles.
     */
    Imagen(int w, int h, int val);

    /**
     * @brief Obtiene el valor de un píxel de forma segura.
     * @param x Coordenada de la columna.
     * @param y Coordenada de la fila.
     * @return El valor de gris del píxel o un valor por defecto (ej. 255) si está fuera de rango.
     */
    int get(int x, int y) const;

    /**
     * @brief Modifica el valor de un píxel específico.
     * Realiza comprobación de límites antes de escribir en memoria.
     * @param x Coordenada de la columna.
     * @param y Coordenada de la fila.
     * @param valor Nuevo nivel de gris (0-255).
     */
    void set(int x, int y, int valor);
};

// Funciones de Entrada / Salida (E/S)

/**
 * @brief Carga una imagen desde un fichero en formato PGM (P2 o P5).
 * @param nombre Ruta del archivo de entrada.
 * @param img Referencia a la estructura donde se cargarán los datos.
 * @return true si la carga fue exitosa, false en caso contrario.
 */
bool cargarPGM(const std::string& nombre, Imagen& img);

/**
 * @brief Guarda la imagen actual en un fichero con formato PGM.
 * @param nombre Ruta del archivo de salida.
 * @param img Estructura Imagen que contiene los datos a guardar.
 * @return true si se pudo guardar el archivo, false en caso contrario.
 */
bool guardarPGM(const std::string& nombre, const Imagen& img);

// Funciones de Análisis

/**
 * @brief Evalúa la calidad de la imagen generada respecto a la original.
 * Calcula la suma de las diferencias de intensidad de los grises entre píxeles homólogos[cite: 371].
 * @param original Imagen de referencia cargada al inicio.
 * @param generada Imagen construida mediante el algoritmo de hilos.
 * @return Diferencia acumulada (error global).
 */
double calcularErrorGlobal(const Imagen& original, const Imagen& generada);

#endif
