#include "Dron.h"
#include "Colmena.h"
#include <iostream>

Dron::Dron(std::string id, std::vector<std::string> colores, Subject* s) 
    : id(id), secuenciaColores(colores), colmenaAsignada(s) {
    if (colmenaAsignada) colmenaAsignada->attach(this);
}

Dron::~Dron() {
    if (colmenaAsignada) colmenaAsignada->detach(this);
}

void Dron::update(Subject* s, void* arg) {
    if (arg == nullptr) {
        std::cout << "[SISTEMA] Dron " << id << " desconectado (Colmena destruida)." << std::endl;
        colmenaAsignada = nullptr;
        return;
    }

    std::cout << "   > Dron " << id << " activo." << std::endl;
    Colmena* c = dynamic_cast<Colmena*>(s);
    if (c) std::cout << "     [PULL] Estado: " << c->getEstado() << std::endl;
    
    avanzarSecuencia();
}

void Dron::avanzarSecuencia() {
    if (secuenciaColores.empty()) return;
    std::cout << "     [LED] " << secuenciaColores[pasoActual] << std::endl;
    pasoActual = (pasoActual + 1) % secuenciaColores.size();
}