import sys
import time as time_mod
try:
    from pulp import *
except ImportError:
    print("pulp no está instalado")
    sys.exit(1)

def solve_case(N, C):
    prob = LpProblem("Hackathon_Teams", LpMinimize)
    
    # Variables z_ij: 1 si i y j están en el mismo equipo (i < j)
    z = {}
    for i in range(N):
        for j in range(i + 1, N):
            z[(i, j)] = LpVariable(f"z_{i}_{j}", cat='Binary')
    
    # Función Objetivo: minimizar suma de conflictos entre miembros del mismo equipo
    # Si i y j están en el mismo equipo, el conflicto que añaden es C[i][j] + C[j][i]
    prob += lpSum((C[i][j] + C[j][i]) * z[(i, j)] for i in range(N) for j in range(i + 1, N))
    
    # Restricciones
    # 1. Cada participante debe estar en el mismo equipo con exactamente 2 personas más
    for i in range(N):
        prob += lpSum(z[(min(i, j), max(i, j))] for j in range(N) if i != j) == 2
        
    # 2. Desigualdad triangular: Si i y j están en el mismo equipo, y j y k también, entonces i y k deben estarlo
    for i in range(N):
        for j in range(i + 1, N):
            for k in range(j + 1, N):
                prob += z[(i, j)] + z[(j, k)] - 1 <= z[(i, k)]
                prob += z[(i, j)] + z[(i, k)] - 1 <= z[(j, k)]
                prob += z[(j, k)] + z[(i, k)] - 1 <= z[(i, j)]
                
    prob.solve(PULP_CBC_CMD(msg=0))
    return int(value(prob.objective))

def main():
    if len(sys.argv) != 2:
        print("Uso: python pli.py <archivo_casos>")
        sys.exit(1)
        
    with open(sys.argv[1], 'r') as f:
        data = f.read().split()
        
    idx = 0
    while idx < len(data):
        N = int(data[idx])
        idx += 1
        C = []
        for i in range(N):
            row = []
            for j in range(N):
                row.append(int(data[idx]))
                idx += 1
            C.append(row)
        start_time = time_mod.time()
        opt_val = solve_case(N, C)
        elapsed_ms = int((time_mod.time() - start_time) * 1000)
        print(f"PLI Tiempo: {elapsed_ms} ms, Optimo: {opt_val}")

if __name__ == '__main__':
    main()
