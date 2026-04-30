#ifndef SUBJECT_H
#define SUBJECT_H

#include <vector>
#include "Observer.h"

class Subject {
private:
    std::vector<Observer*> observers;
    bool notificando = false;
    bool limpiezaNecesaria = false;
    void limpiarNulos();

public:
    virtual ~Subject();
    void attach(Observer* o);
    void detach(Observer* o);
    void notifyObservers(void* arg = nullptr);
};

#endif