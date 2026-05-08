import os

def check_coverage():
    input_file = 'pares_entrada.txt'
    output_file = 'caminos_resultantes.txt'
    
    # Ajuste de rutas para que funcione tanto si se ejecuta desde la raíz como desde la carpeta
    if not os.path.exists(input_file):
        input_file = 'CaminosPrueba/' + input_file
        output_file = 'CaminosPrueba/' + output_file

    if not os.path.exists(input_file) or not os.path.exists(output_file):
        print("Error: No se encuentran los archivos necesarios (pares_entrada.txt o caminos_resultantes.txt)")
        return

    with open(input_file, 'r') as f:
        pares = [line.strip() for line in f if line.strip() and not line.startswith('#')]
    
    with open(output_file, 'r') as f:
        caminos = [line.strip().split(',') for line in f if line.strip()]
    
    covered = set()
    for camino in caminos:
        for i in range(len(camino) - 1):
            pair = f"{camino[i]},{camino[i+1]}"
            covered.add(pair)
    
    missing = []
    for p in pares:
        if p not in covered:
            missing.append(p)
            
    print(f"Total de pares esperados: {len(pares)}")
    print(f"Total de pares únicos cubiertos: {len(covered)}")
    print(f"Pares sin cubrir: {len(missing)}")
    if missing:
        print(f"Ejemplo de pares faltantes: {missing[:5]}")
    else:
        print("¡100% de Cobertura Edge-Pair (Pares de Aristas) alcanzada!")

if __name__ == "__main__":
    check_coverage()
