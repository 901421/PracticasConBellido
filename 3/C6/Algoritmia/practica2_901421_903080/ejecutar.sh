#!/bin/bash

echo "Compilando código..."
g++ -O3 -std=c++17 main.cpp particion_palabras.cpp -o separarPalabras
if [ $? -ne 0 ]; then
    echo "Error de compilación. Abortando."
    exit 1
fi

# ==============================================================================
# PREPARACIÓN DEL DIRECTORIO
# ==============================================================================
DIR_DATOS="datos_generados"
mkdir -p $DIR_DATOS

# ==============================================================================
# SCRIPT DE GENERACIÓN DE DATOS (Python embebido)
# ==============================================================================
cat << 'EOF' > generador_datos.py
import sys
import random
import string
import os

numPal = int(sys.argv[1])
dir_datos = sys.argv[2]

# 1. Generar diccionario aleatorio
diccionario = set()
while len(diccionario) < numPal:
    lon = random.randint(3, 8)
    palabra = ''.join(random.choices(string.ascii_lowercase, k=lon))
    diccionario.add(palabra)

diccionario = list(diccionario)
with open(os.path.join(dir_datos, f"dic_{numPal}.txt"), "w") as f:
    f.write("\n".join(diccionario))

# 2. Generar frase limpia
num_palabras_frase = numPal // 10
palabras_elegidas = random.choices(diccionario, k=num_palabras_frase)
frase_limpia = "".join(palabras_elegidas)

with open(os.path.join(dir_datos, f"texto_limpio_{numPal}.txt"), "w") as f:
    f.write(frase_limpia + "\n")

# 3. Generar frase sucia
LF = len(frase_limpia)
probabilidad = 1.0 / (LF * 10)

frase_sucia = ""
for char in frase_limpia:
    if random.random() < probabilidad:
        frase_sucia += random.choice(string.ascii_lowercase)
    else:
        frase_sucia += char

with open(os.path.join(dir_datos, f"texto_sucio_{numPal}.txt"), "w") as f:
    f.write(frase_sucia + "\n")
EOF
# ==============================================================================

# Inicializamos los ficheros de salida
echo "TamanioDiccionario,Escenario,Variante,Tiempo(us)" > resultados.csv
echo "==========================================================" > resumen.txt
echo "       RESUMEN DE EJECUCIÓN - PARTICIÓN DE PALABRAS       " >> resumen.txt
echo "==========================================================" >> resumen.txt

# --- PRUEBA BASE (Archivos del enunciado) ---
echo -e "\n1. PRUEBA CON ARCHIVOS DEL ENUNCIADO (diccionario.txt / texto.txt)" >> resumen.txt
if [ -f "diccionario.txt" ] && [ -f "texto.txt" ]; then
    for VAR in 1 2 3; do
        echo "Ejecutando Variante $VAR..." >> resumen.txt
        ./separarPalabras $VAR diccionario.txt texto.txt >> resumen.txt
    done
else
    echo "Faltan diccionario.txt o texto.txt en el directorio raiz." >> resumen.txt
fi

# --- BATERIA DE EXPERIMENTOS ---
TAMANOS=(100 1000)
echo -e "\n2. BATERIA DE EXPERIMENTOS ALEATORIOS (Guardados en resultados.csv)" >> resumen.txt
echo "Iniciando bateria de experimentos aleatorios..."

for TAM in "${TAMANOS[@]}"; do
    echo "Generando datos en $DIR_DATOS/ para diccionario de $TAM palabras..."
    python3 generador_datos.py $TAM $DIR_DATOS
    
    for ESCENARIO in "limpio" "sucio"; do
        ARCHIVO_TEXTO="$DIR_DATOS/texto_${ESCENARIO}_${TAM}.txt"
        ARCHIVO_DIC="$DIR_DATOS/dic_${TAM}.txt"
        
        for VAR in 1 2 3; do
            echo -n "Probando Variante $VAR con Diccionario $TAM ($ESCENARIO)... "
            
            if [ "$VAR" -eq 1 ] && [ "$TAM" -ge 1000 ]; then
                TIEMPO="TIMEOUT"
                echo "Omitido (Exceso de tiempo)"
                echo "- Variante 1 (Dic: $TAM, $ESCENARIO): OMITIDA (O(2^n))" >> resumen.txt
            else
                SALIDA=$(timeout 5s ./separarPalabras $VAR $ARCHIVO_DIC $ARCHIVO_TEXTO 2>/dev/null)
                
                if [ $? -eq 124 ]; then
                    TIEMPO="TIMEOUT(>5s)"
                    echo "TIMEOUT!"
                    echo "- Variante $VAR (Dic: $TAM, $ESCENARIO): TIMEOUT (>5s)" >> resumen.txt
                else
                    TIEMPO=$(echo "$SALIDA" | grep "Tiempo de ejecucion" | awk '{print $4}')
                    echo "${TIEMPO} us"
                    echo "- Variante $VAR (Dic: $TAM, $ESCENARIO): ${TIEMPO} us" >> resumen.txt
                fi
            fi
            
            echo "$TAM,$ESCENARIO,V$VAR,$TIEMPO" >> resultados.csv
        done
    done
done

echo "========================================"
echo "Experimentos finalizados con exito."
echo " - Los archivos generados estan en la carpeta '$DIR_DATOS/'"
echo " - El resumen de ejecucion esta en 'resumen.txt'"
echo " - Los datos para las graficas estan en 'resultados.csv'"