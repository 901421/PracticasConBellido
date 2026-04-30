#include "Colmena.h"
#include <iostream>

std::string Colmena::getEstado() { 
    return estadoAlerta; 
}

void Colmena::setEstado(std::string nuevoEstado) {
    estadoAlerta = nuevoEstado;
    std::cout << "\n[COLMENA] Cambio: " << nuevoEstado << std::endl;
    notifyObservers(&estadoAlerta);
}