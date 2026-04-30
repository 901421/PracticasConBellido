// Imad Habib Jali 901421
// Jorge Bellido Lobera 903080
#ifndef GEOMETRIA_H
#define GEOMETRIA_H

#include <vector>

/**
 * @struct Punto
 * @brief Representacion de una coordenada (x, y) en el espacio de la imagen.
 */
struct Punto {
    int x; ///< Posicion en el eje horizontal (columna).
    int y; ///< Posicion en el eje vertical (fila).
};

/**
 * @brief Calcula el conjunto de puntos que forman una linea recta entre dos clavos.
 * * Basado en el Algoritmo de Bresenham[cite: 449], este metodo devuelve todos los 
 * pixeles que deben ser pintados para conectar (x1, y1) con (x2, y2) 
 * utilizando unicamente aritmetica de enteros[cite: 389, 423].
 * * @param x1 Coordenada X del clavo de origen.
 * @param y1 Coordenada Y del clavo de origen.
 * @param x2 Coordenada X del clavo de destino.
 * @param y2 Coordenada Y del clavo de destino.
 * @return std::vector<Punto> Lista de coordenadas que componen la linea.
 */
std::vector<Punto> obtenerLineaBresenham(int x1, int y1, int x2, int y2);

#endif
