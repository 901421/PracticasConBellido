#ifndef COLMENA_H
#define COLMENA_H

#include <string>
#include "Subject.h"

class Colmena : public Subject {
private:
    std::string estadoAlerta;

public:
    std::string getEstado();
    void setEstado(std::string nuevoEstado);
};

#endif