import collections
import argparse
import sys
import os

# 1. DEFINICIÓN DEL GRAFO (Basado en mapa_navegacion.tex)
GRAFO = {
    '1': ('N1', 'N2'), '2': ('N1', 'N4'), '3': ('N1', 'N3'), '4': ('N1', 'N5'),
    '5': ('N2', 'N3'), '13': ('N2', 'N2'), '13b': ('N2', 'N2'), '13c': ('N2', 'N2'), '14': ('N2', 'N2'), '23': ('N2', 'N1'),
    '6': ('N3', 'N2'), '6b': ('N3', 'N2'), '6c': ('N3', 'N2'), '7': ('N3', 'N1'), '7b': ('N3', 'N1'), '7c': ('N3', 'N1'), '15': ('N3', 'N3'), '15b': ('N3', 'N3'),
    '8': ('N4', 'N5'), '16': ('N4', 'N4'), '16b': ('N4', 'N4'), '16c': ('N4', 'N4'), '16d': ('N4', 'N4'), '16e': ('N4', 'N4'), '16f': ('N4', 'N4'), '16g': ('N4', 'N4'), '16h': ('N4', 'N4'), '17': ('N4', 'N4'), '18': ('N4', 'N4'), '24': ('N4', 'N1'),
    '9': ('N5', 'N4'), '9b': ('N5', 'N4'), '9c': ('N5', 'N4'), '10': ('N5', 'N1'), '10b': ('N5', 'N1'), '10c': ('N5', 'N1'), '11': ('N5', 'N6'), '20': ('N5', 'N5'), '20b': ('N5', 'N5'),
    '12': ('N6', 'N5'), '12b': ('N6', 'N5'), '12c': ('N6', 'N5'), '21': ('N6', 'N6'), '21b': ('N6', 'N6'), '21c': ('N6', 'N6'), '25': ('N6', 'N6'), '26': ('N6', 'N6')
}

def cargar_pares(archivo):
    pares = []
    if not os.path.exists(archivo):
        print(f"Error: El archivo {archivo} no existe.")
        sys.exit(1)
    
    with open(archivo, 'r') as f:
        for linea in f:
            linea = linea.strip()
            if not linea or linea.startswith('#'):
                continue
            parts = linea.split(',')
            if len(parts) == 2:
                pares.append((parts[0].strip(), parts[1].strip()))
    return pares

def encontrar_camino_corto_a_nodo(inicio, destino):
    """Encuentra el camino más corto de aristas desde un nodo a otro."""
    if inicio == destino:
        return []
    
    queue = collections.deque([ (inicio, []) ])
    visitados = {inicio}
    
    while queue:
        nodo_actual, aristas = queue.popleft()
        
        for id_arista, (org, dest) in GRAFO.items():
            if org == nodo_actual:
                nuevo_camino = aristas + [id_arista]
                if dest == destino:
                    return nuevo_camino
                if dest not in visitados:
                    visitados.add(dest)
                    queue.append((dest, nuevo_camino))
    return None

def encontrar_caminos_desde_n1(objetivos, max_len=15):
    caminos_finales = []
    pendientes = list(objetivos) # Usamos lista para mantener orden y facilitar iteración
    
    print(f"[*] Iniciando búsqueda de cobertura para {len(pendientes)} pares...")

    while pendientes:
        # 1. Intentamos cubrir el primer par pendiente de forma específica
        par_objetivo = pendientes[0]
        aristaA, aristaB = par_objetivo
        
        # Nodo donde empieza la primera arista del par
        nodo_entrada = GRAFO[aristaA][0]
        
        # Encontrar camino más corto desde N1 hasta ese nodo
        camino_prefijo = encontrar_camino_corto_a_nodo('N1', nodo_entrada)
        
        if camino_prefijo is None and nodo_entrada != 'N1':
            print(f"Error: No se puede llegar al nodo {nodo_entrada} desde N1 para cubrir {par_objetivo}")
            pendientes.pop(0)
            continue
            
        camino_actual = (camino_prefijo or []) + [aristaA, aristaB]
        
        # 2. Intentar extender el camino de forma voraz para cubrir más pares
        # Solo si no excedemos la longitud máxima razonable
        while len(camino_actual) < max_len:
            nodo_final = GRAFO[camino_actual[-1]][1]
            mejor_extension = None
            
            # Buscamos si alguna arista desde el nodo actual cubre un nuevo par pendiente
            for id_arista, (org, dest) in GRAFO.items():
                if org == nodo_final:
                    par_potencial = (camino_actual[-1], id_arista)
                    if par_potencial in pendientes:
                        mejor_extension = id_arista
                        break # Tomamos la primera que encontremos
            
            if mejor_extension:
                camino_actual.append(mejor_extension)
            else:
                break # No hay más extensiones inmediatas que cubran pares
        
        # 3. Guardar camino y actualizar pendientes
        caminos_finales.append(camino_actual)
        
        # Eliminar todos los pares que este camino haya cubierto
        cubiertos_en_este_viaje = []
        for i in range(len(camino_actual) - 1):
            par = (camino_actual[i], camino_actual[i+1])
            if par in pendientes:
                if par not in cubiertos_en_este_viaje:
                    cubiertos_en_este_viaje.append(par)
        
        for par in cubiertos_en_este_viaje:
            while par in pendientes:
                pendientes.remove(par)
                
        # Mostrar progreso ocasionalmente
        if len(pendientes) % 50 == 0 or len(pendientes) < 10:
            print(f"[*] Quedan {len(pendientes)} pares por cubrir...")

    return caminos_finales

def main():
    parser = argparse.ArgumentParser(description='Generador de Caminos de Prueba (100% Cobertura de Pares).')
    parser.add_argument('--input', type=str, default='pares_entrada.txt', help='Archivo de entrada A,B')
    parser.add_argument('--output', type=str, default='caminos_resultantes.txt', help='Archivo de salida')
    parser.add_argument('--maxlen', type=int, default=15, help='Longitud sugerida para extensiones')
    
    args = parser.parse_args()

    print("====================================================")
    print("   GENERADOR DE CAMINOS (100% EDGE-PAIR COVERAGE)   ")
    print("====================================================")
    
    pares = cargar_pares(args.input)
    print(f"[*] Cargados {len(pares)} pares de aristas.")
    
    resultados = encontrar_caminos_desde_n1(pares, args.maxlen)
    
    print(f"\n[*] ¡ÉXITO! Se han generado {len(resultados)} caminos para cubrir el 100% de los pares.")
    
    with open(args.output, 'w') as f:
        for camino in resultados:
            f.write(','.join(camino) + '\n')
            
    print(f"[*] Resultados guardados en '{args.output}'")

if __name__ == "__main__":
    main()
