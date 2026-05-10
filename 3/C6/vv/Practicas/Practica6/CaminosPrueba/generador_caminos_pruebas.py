import collections

# GRAFO definitions
GRAFO = {
    '1': ('N1', 'N2'), '2': ('N1', 'N4'), '3': ('N1', 'N3'), '4': ('N1', 'N5'),
    '5': ('N2', 'N3'), 
    '13': ('N2', 'N2'), '13b': ('N2', 'N2'), '13c': ('N2', 'N2'), 
    '14': ('N2', 'N2'), '23': ('N2', 'N1'),
    '6': ('N3', 'N2'), '6b': ('N3', 'N2'), '6c': ('N3', 'N2'), 
    '7': ('N3', 'N1'), '7b': ('N3', 'N1'), '7c': ('N3', 'N1'), 
    '15': ('N3', 'N3'), '15b': ('N3', 'N3'),
    '8': ('N4', 'N5'), 
    '16': ('N4', 'N4'), '16b': ('N4', 'N4'), '16c': ('N4', 'N4'), '16d': ('N4', 'N4'), 
    '16e': ('N4', 'N4'), '16f': ('N4', 'N4'), '16g': ('N4', 'N4'), '16h': ('N4', 'N4'), 
    '17': ('N4', 'N4'), '18': ('N4', 'N4'), '19': ('N4', 'N4'), '24': ('N4', 'N1'),
    '9': ('N5', 'N4'), '9b': ('N5', 'N4'), '9c': ('N5', 'N4'), 
    '10': ('N5', 'N1'), '10b': ('N5', 'N1'), '10c': ('N5', 'N1'), 
    '11': ('N5', 'N6'), 
    '20': ('N5', 'N5'), '20b': ('N5', 'N5'),
    '12': ('N6', 'N5'), '12b': ('N6', 'N5'), '12c': ('N6', 'N5'), 
    '21': ('N6', 'N6'), '21b': ('N6', 'N6'), '21c': ('N6', 'N6'), 
    '25': ('N6', 'N6'), '26': ('N6', 'N6')
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
    hr, hd, hq = False, False, False
    ctx_res = None
    ctx_quad = None
    
    # REGLA ORO: hr y hd deben ser True SOLO si ocurrieron DESPUÉS del último A4 o A8
    for a in camino:
        if a == '4': 
            ctx_res = 'crear'
            hr, hd, hq = False, False, False # Reset total en nueva instancia
        elif a == '8': 
            ctx_res = 'editar'
            hr, hd = False, False # Reset de "botones pulsados" para el test (Requirement 11)
            hq = True # La reserva ya tiene quads por defecto en edición
        elif a == '20': hr = True
        elif a == '20b': hd = True
        elif a == '12': hq = True
        elif a in ['10', '10b', '10c', '9', '9b', '9c']:
            ctx_res = None; hr, hd, hq = False, False, False
        elif a == '3': ctx_quad = 'crear'
        elif a == '5': ctx_quad = 'editar'
        elif a in ['7', '7b', '7c', '6', '6b', '6c']:
            ctx_quad = None
    return hr, hd, hq, ctx_res, ctx_quad

def es_legal(arista_sig, hr, hd, hq, ctx_res, ctx_quad):
    # Reglas de Pila
    if arista_sig in ['7', '7b', '7c'] and ctx_quad != 'crear': return False
    if arista_sig in ['6', '6b', '6c'] and ctx_quad != 'editar': return False
    if arista_sig in ['10', '10b', '10c'] and ctx_res != 'crear': return False
    if arista_sig in ['9', '9b', '9c'] and ctx_res != 'editar': return False
    
    # REGLA ESTRICTA DE ESTADO (Arista 11)
    if arista_sig == '11' and not (hr and hd): return False
    
    # REGLA ESTRICTA DE GUARDADO (9, 10)
    # Según instrucción del usuario: "no puedes marcarlos salir y luego volver a entrar pensado que ya estan pulsados"
    # Entendemos que el test Espresso requiere la interacción física con fechas para que el guardado sea válido.
    if arista_sig in ['9', '10'] and not (hr and hd and hq): return False
    
    return True

def generar_caminos_optimizados(pares_objetivo, max_len=20):
    pares_pendientes = sorted(list(pares_objetivo))
    caminos_finales = []
    
    adj = collections.defaultdict(list)
    for arista, data in GRAFO.items():
        destino = data[1]
        for a_sig, data_sig in GRAFO.items():
            if data_sig[0] == destino:
                adj[arista].append(a_sig)
                
    aristas_inicio = ['1', '2', '3', '4']

    def encontrar_prefijo(par):
        a1, a2 = par
        queue = collections.deque()
        for a in aristas_inicio:
            hr, hd, hq, ctx_res, ctx_quad = evaluar_estado_completo([a])
            queue.append(([a], hr, hd, hq, ctx_res, ctx_quad))
            
        visitados = set()
        while queue:
            camino, hr, hd, hq, ctx_res, ctx_quad = queue.popleft()
            
            if camino[-1] == a1 and es_legal(a2, hr, hd, hq, ctx_res, ctx_quad):
                return camino
                
            if len(camino) > 15: continue 
            
            for sig in adj[camino[-1]]:
                if not es_legal(sig, hr, hd, hq, ctx_res, ctx_quad):
                    continue
                    
                n_camino = camino + [sig]
                nhr, nhd, nhq, nctx_res, nctx_quad = evaluar_estado_completo(n_camino)
                st = (sig, nhr, nhd, nhq, nctx_res, nctx_quad)
                
                if st not in visitados:
                    visitados.add(st)
                    queue.append((n_camino, nhr, nhd, nhq, nctx_res, nctx_quad))
        return None

    while pares_pendientes:
        par = pares_pendientes.pop(0)
        prefijo = encontrar_prefijo(par)
        
        # Si el prefijo no se encuentra por la restricción estricta, informamos
        if not prefijo:
            # Intentamos ver si flexibilizando la regla solo para el par objetivo (excepción técnica)
            # Pero el usuario pidió ESTRICTO. Así que el par simplemente no se puede cubrir.
            continue
            
        camino = prefijo + [par[1]]
        
        # Extensión Greedy
        for _ in range(10): 
            actual = camino[-1]
            hr, hd, hq, ctx_res, ctx_quad = evaluar_estado_completo(camino)
            opciones = [s for s in adj[actual] if es_legal(s, hr, hd, hq, ctx_res, ctx_quad)]
            if not opciones: break
            
            mejor = next((s for s in opciones if (actual, s) in pares_pendientes), None)
            if mejor:
                camino.append(mejor)
                pares_pendientes.remove((actual, mejor))
            else:
                mejor = next((s for s in opciones if s != actual), opciones[0])
                camino.append(mejor)
            
            if mejor in ['7', '7b', '7c', '10', '10b', '10c', '23', '24']: break
            if len(camino) >= max_len: break

        caminos_finales.append(camino)
        for i in range(len(camino)-1):
            p = (camino[i], camino[i+1])
            if p in pares_pendientes: 
                pares_pendientes.remove(p)

    return caminos_finales

def main():
    path_entrada = 'CaminosPrueba/pares_entrada.txt'
    path_salida = 'CaminosPrueba/caminos_resultantes.txt'
    pares = cargar_pares_objetivo(path_entrada)
    
    for a in ['17', '18', '19']:
        pares.add(('16', a))
        pares.add((a, '24'))

    caminos = generar_caminos_optimizados(pares)
    with open(path_salida, 'w') as f:
        for c in caminos:
            f.write(','.join(c) + '\n')
    print(f"Generados {len(caminos)} caminos con Lógica de Estado por Instancia (Stricto).")

if __name__ == "__main__":
    main()
