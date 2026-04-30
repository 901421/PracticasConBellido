#include "Subject.h"
#include <algorithm>
#include <iostream>

Subject::~Subject() {
    notificando = true;
    for (Observer* o : observers) {
        if (o != nullptr) o->update(this, nullptr); // Señal de destrucción
    }
    notificando = false;
}

void Subject::limpiarNulos() {
    observers.erase(std::remove(observers.begin(), observers.end(), nullptr), observers.end());
    limpiezaNecesaria = false;
}

void Subject::attach(Observer* o) {
    observers.push_back(o);
}

void Subject::detach(Observer* o) {
    auto it = std::find(observers.begin(), observers.end(), o);
    if (it != observers.end() && *it != nullptr) {
        if (notificando) {
            *it = nullptr;
            limpiezaNecesaria = true;
        } else {
            observers.erase(it);
        }
    }
}

void Subject::notifyObservers(void* arg) {
    notificando = true;
    for (size_t i = 0; i < observers.size(); ++i) {
        if (observers[i] != nullptr) observers[i]->update(this, arg);
    }
    notificando = false;
    if (limpiezaNecesaria) limpiarNulos();
}