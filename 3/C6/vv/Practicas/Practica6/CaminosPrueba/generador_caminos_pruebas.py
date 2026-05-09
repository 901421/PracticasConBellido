import collections
import os

GRAFO = {
    '1': ('N1', 'N2'), '2': ('N1', 'N4'), '3': ('N1', 'N3'), '4': ('N1', 'N5'),
    '5': ('N2', 'N3'), '13': ('N2', 'N2'), '13b': ('N2', 'N2'), '13c': ('N2', 'N2'), '14': ('N2', 'N2'), '23': ('N2', 'N1'),
    '6': ('N3', 'N2'), '6b': ('N3', 'N2'), '6c': ('N3', 'N2'), '7': ('N3', 'N1'), '7b': ('N3', 'N1'), '7c': ('N3', 'N1'), '15': ('N3', 'N3'), '15b': ('N3', 'N3'),
    '8': ('N4', 'N5'), '16': ('N4', 'N4'), '16b': ('N4', 'N4'), '16c': ('N4', 'N4'), '16d': ('N4', 'N4'), '16e': ('N4', 'N4'), '16f': ('N4', 'N4'), '16g': ('N4', 'N4'), '16h': ('N4', 'N4'), '17': ('N4', 'N4'), '18': ('N4', 'N4'), '24': ('N4', 'N1'),
    '9': ('N5', 'N4'), '9b': ('N5', 'N4'), '9c': ('N5', 'N4'), '10': ('N5', 'N1'), '10b': ('N5', 'N1'), '10c': ('N5', 'N1'), '11': ('N5', 'N6'), '20': ('N5', 'N5'), '20b': ('N5', 'N5'),
    '12': ('N6', 'N5'), '12b': ('N6', 'N5'), '12c': ('N6', 'N5'), '21': ('N6', 'N6'), '21b': ('N6', 'N6'), '21c': ('N6', 'N6'), '25': ('N6', 'N6'), '26': ('N6', 'N6')
}

def cargar_pares_validos(archivo):
    pares = set()
    with open(archivo, 'r') as f:
        for linea in f:
            linea = linea.strip()
            if linea and not linea.startswith('#'):
                parts = linea.split(',')
                if len(parts) == 2:
                    pares.add((parts[0].strip(), parts[1].strip()))
    return pares

def evaluar_estado(camino):
    """Evalúa el estado de UI al final de un camino."""
    has_dates = False
    has_quads = False
    for a in camino:
        if a == '4':
            has_dates = False
            has_quads = False
        elif a == '8':
            has_dates = True
            has_quads = True
        elif a in ['20', '20b']:
            has_dates = True
        elif a == '12':
            has_quads = True
    return has_dates, has_quads

def generar_caminos_optimos(pares_objetivo, max_len=25):
    pares_pendientes = set(pares_objetivo)
    caminos_finales = []
    
    adj = collections.defaultdict(list)
    for a1, a2 in pares_objetivo:
        adj[a1].append(a2)
        
    aristas_inicio = ['1', '2', '3', '4']

    def encontrar_camino_a_arista(inicio_aristas, arista1, arista2, base_dates, base_quads):
        queue = collections.deque()
        for a in inicio_aristas:
            d, q = base_dates, base_quads
            if a == '4': d, q = False, False
            elif a == '8': d, q = True, True
            elif a in ['20', '20b']: d = True
            elif a == '12': q = True
            
            queue.append((a, [a], d, q))
            
        visitados = set()
        for a in inicio_aristas:
            d, q = base_dates, base_quads
            if a == '4': d, q = False, False
            elif a == '8': d, q = True, True
            elif a in ['20', '20b']: d = True
            elif a == '12': q = True
            visitados.add((a, d, q))
        
        while queue:
            actual, camino, has_dates, has_quads = queue.popleft()
            
            if actual == arista1:
                legal = True
                if arista2 == '11' and not has_dates: legal = False
                if arista2 in ['9', '10'] and (not has_dates or not has_quads): legal = False
                
                if legal:
                    return camino
            
            for siguiente in adj[actual]:
                if siguiente == '11' and not has_dates: continue
                if siguiente in ['9', '10'] and (not has_dates or not has_quads): continue
                
                d, q = has_dates, has_quads
                if siguiente == '4': d, q = False, False
                elif siguiente == '8': d, q = True, True
                elif siguiente in ['20', '20b']: d = True
                elif siguiente == '12': q = True
                
                estado = (siguiente, d, q)
                if estado not in visitados:
                    visitados.add(estado)
                    queue.append((siguiente, camino + [siguiente], d, q))
        return None

    while pares_pendientes:
        par_actual = list(pares_pendientes)[0]
        arista1, arista2 = par_actual
        
        # 1. Encontrar camino al inicio del par
        camino = encontrar_camino_a_arista(aristas_inicio, arista1, arista2, False, False)
        
        if not camino:
            # Fallback seguro
            camino = ['4', '20', '11', '12', arista1] if GRAFO[arista1][0] == 'N5' else ['1', arista1]
            
        camino.append(arista2)
        
        # 2. Extender el camino vorazmente
        while len(camino) < max_len:
            ultima_arista = camino[-1]
            siguientes_posibles = adj[ultima_arista]
            
            has_dates, has_quads = evaluar_estado(camino)
            
            mejor_siguiente = None
            
            # Intentar encontrar una arista que cubra un par pendiente y sea legal
            opciones_legales = []
            for sig in siguientes_posibles:
                if sig == '11' and not has_dates: continue
                if sig in ['9', '10'] and (not has_dates or not has_quads): continue
                opciones_legales.append(sig)
                
            for sig in opciones_legales:
                if (ultima_arista, sig) in pares_pendientes:
                    mejor_siguiente = sig
                    break
            
            # Si no hay pendiente, seguir por cualquiera legal
            if not mejor_siguiente and opciones_legales:
                opciones_nuevas = [s for s in opciones_legales if s != ultima_arista]
                if opciones_nuevas:
                    mejor_siguiente = opciones_nuevas[0]
                else:
                    mejor_siguiente = opciones_legales[0]
                    
            if mejor_siguiente:
                camino.append(mejor_siguiente)
            else:
                break
                
        # 3. Registrar camino y actualizar pendientes
        caminos_finales.append(camino)
        
        pares_cubiertos = set()
        for i in range(len(camino) - 1):
            pares_cubiertos.add((camino[i], camino[i+1]))
            
        pares_pendientes -= pares_cubiertos

    return caminos_finales

def main():
    archivo_entrada = 'CaminosPrueba/pares_entrada.txt'
    archivo_salida = 'CaminosPrueba/caminos_resultantes.txt'
    
    pares_validos = cargar_pares_validos(archivo_entrada)
    caminos = generar_caminos_optimos(pares_validos, max_len=25)
    
    with open(archivo_salida, 'w') as f:
        for c in caminos:
            f.write(','.join(c) + '\n')

if __name__ == "__main__":
    main()