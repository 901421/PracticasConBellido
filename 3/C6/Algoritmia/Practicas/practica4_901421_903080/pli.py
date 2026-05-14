import sys
import time as time_mod
try:
    # Intentamos importar la librería PuLP para programación lineal
    from pulp import *
except ImportError:
    # Si no está instalada, mostramos un mensaje de error y salimos
    print("Error: La librería 'pulp' no está instalada. Instálela con 'pip install pulp'.")
    sys.exit(1)

"""
Modelo de Programación Lineal Entera (PLI) para el problema de formación de equipos.
Esta implementación utiliza una formulación basada en variables binarias z_ij, donde
z_ij = 1 si los participantes i y j están en el mismo equipo.
"""

def solve_case(N, C):
    # Creamos el objeto del problema como una minimización
    prob = LpProblem("Hackathon_Teams", LpMinimize)
    
    # DEFINICIÓN DE VARIABLES DE DECISIÓN
    # z[(i, j)] es 1 si i y j están en el mismo equipo, 0 en caso contrario.
    # Solo creamos variables para i < j para evitar redundancia (grafo no dirigido).
    z = {}
    for i in range(N):
        for j in range(i + 1, N):
            z[(i, j)] = LpVariable(f"z_{i}_{j}", cat='Binary')
    
    # FUNCIÓN OBJETIVO
    # Minimizamos la suma de conflictos mutuos (C_ij + C_ji) para cada pareja que esté en el mismo equipo.
    prob += lpSum((C[i][j] + C[j][i]) * z[(i, j)] for i in range(N) for j in range(i + 1, N))
    
    # RESTRICCIONES
    
    # 1. Grado de los nodos: Cada participante debe tener exactamente 2 compañeros de equipo.
    # Esto asegura que los equipos sean de tamaño 3 (en combinación con la transitividad).
    for i in range(N):
        prob += lpSum(z[(min(i, j), max(i, j))] for j in range(N) if i != j) == 2
        
    # 2. Desigualdad triangular (Transitividad de clicas):
    # Si i está con j (z_ij=1) y j está con k (z_jk=1), entonces i debe estar con k (z_ik=1).
    # La restricción z_ij + z_jk - 1 <= z_ik fuerza este comportamiento.
    for i in range(N):
        for j in range(i + 1, N):
            for k in range(j + 1, N):
                # Para cada triplete (i, j, k), aplicamos las 3 combinaciones posibles
                prob += z[(i, j)] + z[(j, k)] - 1 <= z[(i, k)]
                prob += z[(i, j)] + z[(i, k)] - 1 <= z[(j, k)]
                prob += z[(j, k)] + z[(i, k)] - 1 <= z[(i, j)]
                
    # RESOLUCIÓN DEL MODELO
    # Usamos el solver CBC en modo silencioso (msg=0) para no ensuciar la salida
    prob.solve(PULP_CBC_CMD(msg=0))
    
    # Si no se encuentra una solución óptima, devolvemos None
    if LpStatus[prob.status] != 'Optimal':
        return None
        
    # Devolvemos el valor de la función objetivo (conflicto mínimo)
    return int(value(prob.objective))

def main():
    # Verificación de que se ha pasado el archivo de entrada
    if len(sys.argv) != 2:
        print("Uso: python pli.py <archivo_casos>")
        sys.exit(1)
        
    # Lectura del archivo de entrada
    try:
        # Intentamos leer con codificación ascii estándar
        with open(sys.argv[1], 'r', encoding='ascii') as f:
            data = f.read().split()
    except UnicodeDecodeError:
        # Si hay caracteres especiales (UTF-16/BOM), intentamos con utf-8
        with open(sys.argv[1], 'r', encoding='utf-8') as f:
            data = f.read().split()
        
    idx = 0
    # Procesamos todos los casos presentes en el archivo
    while idx < len(data):
        try:
            # Primero leemos el número de participantes N
            N = int(data[idx])
        except ValueError:
            break
        idx += 1
        
        # Leemos la matriz de conflictos C de tamaño N x N
        C = []
        for i in range(N):
            row = []
            for j in range(N):
                row.append(int(data[idx]))
                idx += 1
            C.append(row)
        
        # Medimos el tiempo y resolvemos el caso
        start_time = time_mod.time()
        opt_val = solve_case(N, C)
        elapsed_ms = int((time_mod.time() - start_time) * 1000)
        
        # Imprimimos el resultado por consola
        if opt_val is not None:
            print(f"PLI Tiempo: {elapsed_ms} ms, Optimo: {opt_val}")
        else:
            print(f"PLI Error: No se encontró solución óptima para N={N}")

if __name__ == '__main__':
    # Ejecutamos la función principal
    main()
