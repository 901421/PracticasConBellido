# ==============================================================================
# Makefile para Práctica 1: Algoritmos Voraces (Hilorama)
# Asignatura: Algoritmia Básica - Universidad de Zaragoza
# ==============================================================================

# Compilador y banderas
CXX = g++
CXXFLAGS = -O3 -Wall -std=c++11

# Nombre del ejecutable final
TARGET = hilos

# Objetos necesarios
OBJS = main.o imagen.o geometria.o

# Regla por defecto: Compilar el proyecto
all: $(TARGET)

# Enlace del ejecutable
$(TARGET): $(OBJS)
	$(CXX) $(CXXFLAGS) -o $(TARGET) $(OBJS)

# Compilación de main.o (Depende de ambos encabezados)
main.o: main.cpp imagen.h geometria.h
	$(CXX) $(CXXFLAGS) -c main.cpp

# Compilación de imagen.o (Solo depende de su encabezado)
imagen.o: imagen.cpp imagen.h
	$(CXX) $(CXXFLAGS) -c imagen.cpp

# Compilación de geometria.o (Solo depende de su encabezado)
geometria.o: geometria.cpp geometria.h
	$(CXX) $(CXXFLAGS) -c geometria.cpp

# Limpieza de archivos temporales y ejecutable
clean:
	rm -f $(TARGET) $(OBJS)

# Regla para forzar la recompilación completa
rebuild: clean all

.PHONY: all clean rebuild