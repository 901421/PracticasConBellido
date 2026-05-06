#!/bin/bash
# ======================================================================
# Script de automatizacion - Practica 3: Busqueda con retroceso 
# Autores:  Imad Habib (901421), Jorge Bellido (903080)
# ======================================================================

# --- 1. Comprobaciones de seguridad ---
if [ ! -f "ubicaCentros.cpp" ]; then
    echo "Error: No se encuentra el fichero 'ubicaCentros.cpp' en este directorio."
    exit 1
fi

echo "========================================"
echo "COMPILANDO ubicaCentros.cpp..."
echo "========================================"
g++ -std=c++11 -O3 ubicaCentros.cpp -o ubicaCentros

if [ $? -ne 0 ]; then
    echo "Error de compilacion. Por favor, revisa el codigo."
    exit 1
fi
echo "Compilacion exitosa (-O3 activado)."
echo ""

# --- 2. Ejecucion de casos base ---
echo "========================================"
echo "FASE 1: Ejecutando casos base (pruebas.txt)..."
echo "========================================"
if [ -f "pruebas.txt" ]; then
    ./ubicaCentros pruebas.txt resultados.txt
    echo "Resultados base guardados. Contenido de resultados.txt:"
    echo "----------------------------------------"
    cat resultados.txt
    echo "----------------------------------------"
else
    echo "Advertencia: No se encontro 'pruebas.txt'. Omitiendo Fase 1."
fi
echo ""

# --- 3. Ejecucion de casos masivos (Experimentacion) ---
echo "========================================"
echo "FASE 2: Generacion y prueba de casos masivos..."
echo "========================================"
if [ -f "generadorPruebas.py" ]; then
    echo "Generando nuevos grafos con Python..."
    python3 generadorPruebas.py
    
    if [ -f "pruebas_py.txt" ]; then
        echo "Ejecutando algoritmo con casos masivos..."
        ./ubicaCentros pruebas_py.txt resultados_py.txt
        echo "Resultados masivos guardados. Contenido de resultados_py.txt:"
        echo "----------------------------------------"
        cat resultados_py.txt
        echo "----------------------------------------"
    else
        echo "Error: El script de Python no genero el archivo 'pruebas_py.txt'."
    fi
else
    echo "Advertencia: No se encontro 'generadorPruebas.py'. Omitiendo Fase 2."
fi

echo ""
echo "Flujo de pruebas completamente finalizado."