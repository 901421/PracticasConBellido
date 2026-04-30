#ifndef DRON_H
#define DRON_H

#include <string>
#include <vector>
#include "Observer.h"
#include "Subject.h"

class Dron : public Observer {
private:
    std::string id;
    std::vector<std::string> secuenciaColores;
    int pasoActual = 0;
    Subject* colmenaAsignada;
    void avanzarSecuencia();

public:
    Dron(std::string id, std::vector<std::string> colores, Subject* s);
    virtual ~Dron();
    void update(Subject* s, void* arg) override;
};

#endif