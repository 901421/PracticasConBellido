// Imad Habib Jali 901421
// Jorge Bellido Lobera 903080
#include "geometria.h"
#include <cmath>
#include <cstdlib>

using namespace std;

vector<Punto> obtenerLineaBresenham(int x1, int y1, int x2, int y2) {
    vector<Punto> linea;

    // 1. Cálculo de distancias absolutas en ambos ejes
    int deltaX = abs(x2 - x1);
    int deltaY = abs(y2 - y1);

    // 2. Determinación del sentido del avance (paso unitario)
    // sx/sy será 1 si la línea avanza o -1 si retrocede
    int pasoX = (x1 < x2) ? 1 : -1;
    int pasoY = (y1 < y2) ? 1 : -1;

    // 3. Inicialización del error acumulado
    // Usamos deltaY negativo para equilibrar el error en el bucle
    int error = deltaX - deltaY;
    int errorDoble;

    // 4. Bucle principal de trazado
    while (true) {
        // Añadimos el punto actual a la lista de píxeles
        linea.push_back({x1, y1});

        // Condición de parada: hemos llegado al clavo de destino
        if (x1 == x2 && y1 == y2) {
            break;
        }

        // Calculamos el error doble para decidir el siguiente movimiento
        errorDoble = 2 * error;

        // ¿Debemos avanzar en el eje X?
        if (errorDoble > -deltaY) {
            error -= deltaY;
            x1 += pasoX;
        }

        // ¿Debemos avanzar en el eje Y?
        if (errorDoble < deltaX) {
            error += deltaX;
            y1 += pasoY;
        }
    }

    return linea;
}