#ifndef OBSERVER_H
#define OBSERVER_H

class Subject;

class Observer {
public:
    virtual ~Observer() {}
    virtual void update(Subject* s, void* arg) = 0;
};

#endif