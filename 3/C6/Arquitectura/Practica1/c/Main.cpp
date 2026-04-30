#include <iostream>
#include <vector>
#include "Colmena.h"
#include "Dron.h"

int main() {
    std::cout << "=== TEST DE INTEGRACION OBSERVADOR ===" << std::endl;

    Colmena colmena;

    // 1. Vinculación dinámica inicial
    std::cout << "\n[TEST] Creando escuadron inicial (Alpha, Beta, Gamma)..." << std::endl;
    Dron* d1 = new Dron("Alpha", {"ROJO"}, &colmena);
    Dron* d2 = new Dron("Beta", {"VERDE", "AZUL"}, &colmena);
    Dron* d3 = new Dron("Gamma", {"AMARILLO"}, &colmena);

    // 2. Primera notificación (Todos deben responder)
    colmena.setEstado("ALERTA_INICIAL");

    // 3. Borrado manual de un nodo intermedio
    // Esto prueba que tu detach() reajusta bien el vector sin romper índices
    std::cout << "\n[TEST] Eliminando al Dron Beta..." << std::endl;
    delete d2; 
    d2 = nullptr; // Buena práctica

    // 4. Segunda notificación (Alpha y Gamma deben responder, Beta no)
    // Si tu lógica de punteros estuviera mal, aquí podría haber un crash
    colmena.setEstado("ALERTA_MEDIA");

    // 5. Añadir nuevos elementos tras un borrado
    std::cout << "\n[TEST] Creando refuerzos (Delta)..." << std::endl;
    Dron* d4 = new Dron("Delta", {"BLANCO"}, &colmena);

    // 6. Notificación final con la lista mezclada
    colmena.setEstado("ALERTA_FINAL");

    // 7. Limpieza de memoria
    std::cout << "\n[TEST] Limpiando memoria restante..." << std::endl;
    delete d1;
    delete d3;
    delete d4;

    std::cout << "=== FIN DEL TEST (SIN ERRORES) ===" << std::endl;
    return 0;
}