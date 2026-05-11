#!/bin/bash

# Script de automatización para Práctica 4

if [ "$#" -ne 2 ]; then
    echo "Uso: $0 <archivo_entrada> <archivo_salida>"
    exit 1
fi

INPUT_FILE=$1
OUTPUT_FILE=$2

echo "Compilando formarEquipos..."
g++ -O3 -std=c++17 main.cpp -o formarEquipos

if [ $? -ne 0 ]; then
    echo "Error en la compilación de main.cpp"
    exit 1
fi

echo "Ejecutando formarEquipos..."
./formarEquipos "$INPUT_FILE" "$OUTPUT_FILE"

echo "Proceso finalizado. Resultados guardados en $OUTPUT_FILE"
