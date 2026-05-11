#!/bin/bash

# Autores 
# Imad Habib Jali: 901421
# Jorge Bellido Lobera: 903080

# ==============================================================================
# Script de automatización para Práctica 1 - Hilorama
# ==============================================================================

echo "================================================="
echo "1. COMPILANDO EL PROYECTO"
echo "================================================="
# Se utiliza la regla 'rebuild' del Makefile para forzar limpieza y recompilación
make rebuild

# Verificar si la compilación tuvo éxito
if [ $? -ne 0 ]; then
    echo "[ERROR] La compilación ha fallado. Abortando pruebas."
    exit 1
fi

echo -e "\n[ÉXITO] Compilación terminada correctamente."

# Crear un directorio para guardar los resultados y no mezclar archivos
mkdir -p resultados_pruebas

echo -e "\n================================================="
echo "2. EJECUTANDO PRUEBAS"
echo "================================================="
# El formato de ejecución es:
# ./hilos <N_Clavos> <P_Candidatos> <S_Seleccion> <Img_In> <File_Out> [Intensidad_Hilo]

# ---------------------------------------------------------
# PRUEBAS CON p1.pgm
# ---------------------------------------------------------

echo -e "\n-> Prueba 1: p1.pgm - Configuración BAJA (1000 clavos, 2000 candidatos, 40 selecciones)"
./hilos 1000 2000 40 pruebas/p1.pgm resultados_pruebas/p1_bajo 

echo -e "\n-> Prueba 2: p1.pgm - Configuración MEDIA (2500 clavos, 5000 candidatos, 100 selecciones)"
./hilos 2500 5000 100 pruebas/p1.pgm resultados_pruebas/p1_medio 4

echo -e "\n-> Prueba 3: p1.pgm - Configuración ALTA (5000 clavos, 10000 candidatos, 200 selecciones)"
./hilos 5000 10000 200 pruebas/p1.pgm resultados_pruebas/p1_alto 2

# ---------------------------------------------------------
# PRUEBA CON p2.pgm
# ---------------------------------------------------------

echo -e "\n-> Prueba 4: p2.pgm - Configuración ALTA (5000 clavos, 10000 candidatos, 200 selecciones)"
./hilos 5000 10000 200 pruebas/p2.pgm resultados_pruebas/p2_resultado 2

echo -e "\n================================================="
echo "EJECUCIÓN FINALIZADA"
echo "================================================="
echo "Revisa la carpeta 'resultados_pruebas' para ver los archivos .txt y .pgm generados."