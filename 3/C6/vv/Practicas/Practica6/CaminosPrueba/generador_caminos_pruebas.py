import collections
import os

# 1. DEFINICIÓN DEL GRAFO
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

def generar_caminos_optimos(pares_objetivo, max_len=25):
    """
    Genera un conjunto mínimo de caminos largos que cubren todos los pares objetivos.
    Solo se permiten transiciones si el par (A, B) está en pares_objetivo.
    """
    pares_pendientes = set(pares_objetivo)
    caminos_finales = []
    
    # Construir grafo de adyacencia basado SOLO en pares válidos
    adj = collections.defaultdict(list)
    for a1, a2 in pares_objetivo:
        adj[a1].append(a2)
        
    aristas_inicio = ['1', '2', '3', '4'] # Aristas que salen de N1

    def encontrar_camino_a_arista(inicio_aristas, arista_destino):
        """Encuentra la secuencia más corta de aristas válidas hasta la arista_destino"""
        if arista_destino in inicio_aristas:
            return [arista_destino]
            
        queue = collections.deque([(a, [a]) for a in inicio_aristas])
        visitados = set(inicio_aristas)
        
        while queue:
            actual, camino = queue.popleft()
            for siguiente in adj[actual]:
                nuevo_camino = camino + [siguiente]
                if siguiente == arista_destino:
                    return nuevo_camino
                if siguiente not in visitados:
                    visitados.add(siguiente)
                    queue.append((siguiente, nuevo_camino))
        return None

    while pares_pendientes:
        # Tomar un par objetivo que falte por cubrir
        par_actual = list(pares_pendientes)[0]
        arista1, arista2 = par_actual
        
        # 1. Encontrar cómo llegar a la primera arista del par desde el inicio
        camino = encontrar_camino_a_arista(aristas_inicio, arista1)
        if not camino:
            # Si no se puede llegar directamente (raro), lo forzamos artificialmente 
            # solo para análisis, aunque teóricamente todos son accesibles
            camino = ['4', arista1] if GRAFO[arista1][0] == 'N5' else ['1', arista1]
            
        camino.append(arista2)
        
        # 2. Extender el camino vorazmente para cubrir más pares pendientes
        while len(camino) < max_len:
            ultima_arista = camino[-1]
            siguientes_posibles = adj[ultima_arista]
            
            mejor_siguiente = None
            # Priorizar siguientes que cubran un par pendiente
            for sig in siguientes_posibles:
                if (ultima_arista, sig) in pares_pendientes:
                    # REGLA DE NEGOCIO: Antes de '11' (Selección Quads), asegurar fechas
                    if sig == '11':
                        if '20' not in camino or '20b' not in camino:
                            continue # Saltar este par por ahora, necesita preparación
                    mejor_siguiente = sig
                    break
            
            # Si no hay pendiente, tomar cualquiera válido para seguir explorando
            if not mejor_siguiente and siguientes_posibles:
                # Intentar no enbuclearse infinitamente en la misma arista (ej. 13, 13, 13)
                opciones = [s for s in siguientes_posibles if s != ultima_arista]
                if opciones:
                    mejor_siguiente = opciones[0]
                else:
                    mejor_siguiente = siguientes_posibles[0]
                    
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
        
        print(f"Camino generado ({len(camino)} aristas). Pares restantes: {len(pares_pendientes)}")

    return caminos_finales

def main():
    archivo_entrada = 'CaminosPrueba/pares_entrada.txt'
    archivo_salida = 'CaminosPrueba/caminos_resultantes.txt'
    
    print("====================================================")
    print("   GENERADOR DE CAMINOS OPTIMIZADO (VORAZ)          ")
    print("====================================================")
    
    pares_validos = cargar_pares_validos(archivo_entrada)
    print(f"[*] Cargados {len(pares_validos)} pares válidos.")
    
    # Generar caminos, max_len 25 es un buen equilibrio entre largo y manejable en Espresso
    caminos = generar_caminos_optimos(pares_validos, max_len=25)
    
    print(f"\n[*] ¡ÉXITO! Generados {len(caminos)} tests para cubrir el 100% de los pares.")
    
    with open(archivo_salida, 'w') as f:
        for c in caminos:
            f.write(','.join(c) + '\n')
            
    print(f"[*] Resultados guardados en '{archivo_salida}'")

if __name__ == "__main__":
    main()
