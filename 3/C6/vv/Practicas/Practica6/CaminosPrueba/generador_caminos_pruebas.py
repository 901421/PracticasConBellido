import collections
import os

# DEFINICIÓN DEL GRAFO (Nodos y sus conexiones básicas)
GRAFO = {
    '1': ('N1', 'N2'), '2': ('N1', 'N4'), '3': ('N1', 'N3'), '4': ('N1', 'N5'),
    '5': ('N2', 'N3'), '13': ('N2', 'N2'), '13b': ('N2', 'N2'), '13c': ('N2', 'N2'), '14': ('N2', 'N2'), '23': ('N2', 'N1'),
    '6': ('N3', 'N2'), '6b': ('N3', 'N2'), '6c': ('N3', 'N2'), '7': ('N3', 'N1'), '7b': ('N3', 'N1'), '7c': ('N3', 'N1'), '15': ('N3', 'N3'), '15b': ('N3', 'N3'),
    '8': ('N4', 'N5'), '16': ('N4', 'N4'), '16b': ('N4', 'N4'), '16c': ('N4', 'N4'), '16d': ('N4', 'N4'), '16e': ('N4', 'N4'), '16f': ('N4', 'N4'), '16g': ('N4', 'N4'), '16h': ('N4', 'N4'), '17': ('N4', 'N4'), '18': ('N4', 'N4'), '24': ('N4', 'N1'),
    '9': ('N5', 'N4'), '9b': ('N5', 'N4'), '9c': ('N5', 'N4'), '10': ('N5', 'N1'), '10b': ('N5', 'N1'), '10c': ('N5', 'N1'), '11': ('N5', 'N6'), '20': ('N5', 'N5'), '20b': ('N5', 'N5'),
    '12': ('N6', 'N5'), '12b': ('N6', 'N5'), '12c': ('N6', 'N5'), '21': ('N6', 'N6'), '21b': ('N6', 'N6'), '21c': ('N6', 'N6'), '25': ('N6', 'N6'), '26': ('N6', 'N6')
}

def cargar_pares_objetivo(archivo):
    pares = set()
    with open(archivo, 'r') as f:
        for linea in f:
            linea = linea.strip()
            if linea and not linea.startswith('#'):
                parts = linea.split(',')
                if len(parts) == 2:
                    pares.add((parts[0].strip(), parts[1].strip()))
    return pares

def evaluar_estado_completo(camino):
    """
    Evalúa el estado lógico del sistema tras recorrer un camino.
    - has_recogida: Se ha puesto fecha de inicio (20)
    - has_devolucion: Se ha puesto fecha de fin (20b)
    - has_quads: Se han seleccionado vehículos (12)
    - contexto: 'crear' (iniciado por 4) o 'editar' (iniciado por 8)
    """
    has_recogida = False
    has_devolucion = False
    has_quads = False
    contexto = None # 'crear' o 'editar'

    for a in camino:
        if a == '4': 
            contexto = 'crear'
            has_recogida = False
            has_devolucion = False
            has_quads = False
        elif a == '8': 
            contexto = 'editar'
            has_recogida = True
            has_devolucion = True
            has_quads = True
        elif a == '20': has_recogida = True
        elif a == '20b': has_devolucion = True
        elif a == '12': has_quads = True
    
    return has_recogida, has_devolucion, has_quads, contexto

def es_legal(arista_sig, has_recogida, has_devolucion, has_quads, contexto):
    """Verifica si una transición a arista_sig es legal según el estado y contexto."""
    
    # Regla de Selección de Quads (11): Requiere AMBAS fechas
    if arista_sig == '11':
        return has_recogida and has_devolucion
    
    # Regla de Confirmación/Guardado (9, 10): Requiere fechas Y quads
    if arista_sig in ['9', '10']:
        if not (has_recogida and has_devolucion and has_quads):
            return False
        # Regla de la Pila (Contexto)
        if arista_sig == '9' and contexto != 'editar': return False
        if arista_sig == '10' and contexto != 'crear': return False

    # Regla de Cancelación/Atrás (9b, 9c, 10b, 10c)
    if arista_sig in ['9b', '9c'] and contexto != 'editar': return False
    if arista_sig in ['10b', '10c'] and contexto != 'crear': return False

    return True

def generar_caminos_con_logica(pares_objetivo, max_len=35):
    pares_pendientes = set(pares_objetivo)
    caminos_finales = []
    
    # Construir adjacencia desde el grafo
    adj = collections.defaultdict(list)
    for arista, data in GRAFO.items():
        destino = data[1]
        for a_sig, data_sig in GRAFO.items():
            if data_sig[0] == destino:
                adj[arista].append(a_sig)
                
    aristas_inicio = ['1', '2', '3', '4']

    def encontrar_prefijo_legal(par):
        """Encuentra el camino más corto desde el inicio (N1) que permita ejecutar el par."""
        a1, a2 = par
        queue = collections.deque()
        for a in aristas_inicio:
            # Inicializar estado según arista de entrada
            hr, hd, hq, ctx = evaluar_estado_completo([a])
            queue.append(([a], hr, hd, hq, ctx))
            
        visitados = set()
        
        while queue:
            camino, hr, hd, hq, ctx = queue.popleft()
            actual = camino[-1]
            
            # Si hemos llegado a la primera arista del par
            if actual == a1:
                # Comprobar si la segunda arista es legal desde aquí
                if es_legal(a2, hr, hd, hq, ctx):
                    return camino
            
            if len(camino) > 15: continue # Evitar explosión

            for sig in adj[actual]:
                # Actualizar estado para la siguiente arista
                nhr, nhd, nhq, nctx = hr, hd, hq, ctx
                if sig == '20': nhr = True
                elif sig == '20b': nhd = True
                elif sig == '12': nhq = True
                elif sig == '4': nctx, nhr, nhd, nhq = 'crear', False, False, False
                elif sig == '8': nctx, nhr, nhd, nhq = 'editar', True, True, True
                
                # Solo avanzar si es legal
                if es_legal(sig, hr, hd, hq, ctx):
                    estado = (sig, nhr, nhd, nhq, nctx)
                    if estado not in visitados:
                        visitados.add(estado)
                        queue.append((camino + [sig], nhr, nhd, nhq, nctx))
        return None

    while pares_pendientes:
        # Intentar cubrir el primer par pendiente
        par_actual = list(pares_pendientes)[0]
        prefijo = encontrar_prefijo_legal(par_actual)
        
        if not prefijo:
            print(f"Error: No se pudo encontrar un camino legal para el par {par_actual}")
            pares_pendientes.remove(par_actual)
            continue
            
        camino = prefijo + [par_actual[1]]
        
        # Extensión voraz para cubrir más pares
        while len(camino) < max_len:
            actual = camino[-1]
            hr, hd, hq, ctx = evaluar_estado_completo(camino)
            
            siguientes_legales = [s for s in adj[actual] if es_legal(s, hr, hd, hq, ctx)]
            if not siguientes_legales: break
            
            # Prioridad 1: Pares pendientes
            mejor = None
            for s in siguientes_legales:
                if (actual, s) in pares_pendientes:
                    mejor = s
                    break
            
            # Prioridad 2: No repetir la misma arista inmediatamente (evitar bucles infinitos en 13,13 etc si ya estan cubiertos)
            if not mejor:
                for s in siguientes_legales:
                    if s != actual:
                        mejor = s
                        break
            
            if not mejor: mejor = siguientes_legales[0]
            
            camino.append(mejor)
            if mejor in ['7', '7b', '7c', '10', '10b', '10c', '23', '24']: break # Fin de flujo
            
        caminos_finales.append(camino)
        # Actualizar pendientes
        for i in range(len(camino) - 1):
            p = (camino[i], camino[i+1])
            if p in pares_pendientes: pares_pendientes.remove(p)

    return caminos_finales

def main():
    path_entrada = 'CaminosPrueba/pares_entrada.txt'
    path_salida = 'CaminosPrueba/caminos_resultantes.txt'
    
    if not os.path.exists(path_entrada):
        print(f"Error: No existe {path_entrada}")
        return
        
    pares = cargar_pares_objetivo(path_entrada)
    caminos = generar_caminos_con_logica(pares)
    
    with open(path_salida, 'w') as f:
        for c in caminos:
            f.write(','.join(c) + '\n')
    
    print(f"Generados {len(caminos)} caminos en {path_salida}")

if __name__ == "__main__":
    main()
