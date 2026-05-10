import os
import collections

# CONFIGURACIÓN DEL GRAFO Y REGLAS (Para validación independiente)
GRAFO = {
    '1': ('N1', 'N2'), '2': ('N1', 'N4'), '3': ('N1', 'N3'), '4': ('N1', 'N5'),
    '5': ('N2', 'N3'), '13': ('N2', 'N2'), '13b': ('N2', 'N2'), '13c': ('N2', 'N2'), '14': ('N2', 'N2'), '23': ('N2', 'N1'),
    '6': ('N3', 'N2'), '6b': ('N3', 'N2'), '6c': ('N3', 'N2'), '7': ('N3', 'N1'), '7b': ('N3', 'N1'), '7c': ('N3', 'N1'), '15': ('N3', 'N3'), '15b': ('N3', 'N3'),
    '8': ('N4', 'N5'), '16': ('N4', 'N4'), '16b': ('N4', 'N4'), '16c': ('N4', 'N4'), '16d': ('N4', 'N4'), '16e': ('N4', 'N4'), '16f': ('N4', 'N4'), '16g': ('N4', 'N4'), '16h': ('N4', 'N4'), '17': ('N4', 'N4'), '18': ('N4', 'N4'), '19': ('N4', 'N4'), '24': ('N4', 'N1'),
    '9': ('N5', 'N4'), '9b': ('N5', 'N4'), '9c': ('N5', 'N4'), '10': ('N5', 'N1'), '10b': ('N5', 'N1'), '10c': ('N5', 'N1'), '11': ('N5', 'N6'), '20': ('N5', 'N5'), '20b': ('N5', 'N5'),
    '12': ('N6', 'N5'), '12b': ('N6', 'N5'), '12c': ('N6', 'N5'), '21': ('N6', 'N6'), '21b': ('N6', 'N6'), '21c': ('N6', 'N6'), '25': ('N6', 'N6'), '26': ('N6', 'N6')
}

def check_coverage_advanced():
    input_file = 'CaminosPrueba/pares_entrada.txt'
    output_file = 'CaminosPrueba/caminos_resultantes.txt'

    if not os.path.exists(input_file) or not os.path.exists(output_file):
        print("Error: No se encuentran los archivos necesarios.")
        return

    with open(input_file, 'r') as f:
        pares_objetivo = [line.strip() for line in f if line.strip() and not line.startswith('#')]
        # Asegurar par 16,19 y 19,24 si no están
        p_obj_set = set(pares_objetivo)
        for a in ['17', '18', '19']:
            p_obj_set.add(f"16,{a}")
            p_obj_set.add(f"{a},24")
    
    with open(output_file, 'r') as f:
        caminos = [line.strip().split(',') for line in f if line.strip()]
    
    # 1. MAPEADO DE COBERTURA
    cobertura_map = collections.defaultdict(list)
    total_pares_detectados = set()
    
    for idx, camino in enumerate(caminos, 1):
        for i in range(len(camino) - 1):
            pair = f"{camino[i]},{camino[i+1]}"
            cobertura_map[pair].append(idx)
            total_pares_detectados.add(pair)
    
    # 2. VALIDACIÓN DE LÓGICA DE ESTADO POR CAMINO (Simulador Riguroso)
    errores_logica = []
    for idx, camino in enumerate(caminos, 1):
        hr, hd, hq = False, False, False
        ctx_res = None
        ctx_quad = None
        
        for i, a in enumerate(camino):
            # Transiciones de entrada a contextos
            if a == '4': 
                ctx_res = 'crear'; hr, hd, hq = False, False, False
            elif a == '8': 
                ctx_res = 'editar'; hr, hd, hq = True, True, True
            elif a == '3':
                ctx_quad = 'crear'
            elif a == '5':
                ctx_quad = 'editar'
            
            # Actualización de estados internos de N5/N6
            if a == '20': hr = True
            elif a == '20b': hd = True
            elif a == '12': hq = True
            
            # --- VERIFICACIONES DE REGLAS ---
            
            # Reglas de Selección (Arista 11)
            if a == '11' and not (hr and hd):
                errores_logica.append(f"C{idx}: (A11) sin fechas completas en paso {i+1}")
            
            # Reglas de Guardado (A9, A10)
            if a in ['9', '10'] and not (hr and hd and hq):
                errores_logica.append(f"C{idx}: Guardado ({a}) sin quads/fechas en paso {i+1}")
            
            # Reglas de Pila Reserva (N5 -> N1/N4)
            if a in ['10', '10b', '10c'] and ctx_res != 'crear':
                errores_logica.append(f"C{idx}: Salida ({a}) ilegal para contexto {ctx_res} en paso {i+1}")
            if a in ['9', '9b', '9c'] and ctx_res != 'editar':
                errores_logica.append(f"C{idx}: Salida ({a}) ilegal para contexto {ctx_res} en paso {i+1}")
                
            # Reglas de Pila Quad (N3 -> N1/N2)
            if a in ['7', '7b', '7c'] and ctx_quad != 'crear':
                errores_logica.append(f"C{idx}: Salida Quad ({a}) ilegal para contexto {ctx_quad} en paso {i+1}")
            if a in ['6', '6b', '6c'] and ctx_quad != 'editar':
                errores_logica.append(f"C{idx}: Salida Quad ({a}) ilegal para contexto {ctx_quad} en paso {i+1}")

            # Transiciones de salida (Limpieza de contexto)
            if a in ['10', '10b', '10c', '9', '9b', '9c']:
                ctx_res = None; hr, hd, hq = False, False, False
            elif a in ['7', '7b', '7c', '6', '6b', '6c']:
                ctx_quad = None

    # 3. ANÁLISIS DE SUFICIENCIA
    nodos_cubiertos = set()
    aristas_cubiertas = set()
    for camino in caminos:
        for a in camino:
            if a in GRAFO:
                aristas_cubiertas.add(a)
                nodos_cubiertos.add(GRAFO[a][0])
                nodos_cubiertos.add(GRAFO[a][1])

    nodos_totales = {'N1', 'N2', 'N3', 'N4', 'N5', 'N6'}
    aristas_totales = set(GRAFO.keys())

    # INFORME
    print("=== AUDITORÍA FORENSE DE CAMINOS (Matemáticamente Robusta) ===")
    print(f"Total de caminos: {len(caminos)}")
    print(f"Pares objetivo: {len(p_obj_set)}")
    
    intersect = total_pares_detectados.intersection(p_obj_set)
    print(f"Pares cubiertos: {len(intersect)}")
    
    missing = [p for p in p_obj_set if p not in total_pares_detectados]
    if missing:
        print(f"[!] Pares FALTANTES: {len(missing)}")
        for m in missing[:10]: print(f"  - {m}")
    else:
        print("[OK] Cobertura Edge-Pair: 100%")

    print("\n--- Validación de Lógica de Pila y Estado ---")
    if errores_logica:
        print(f"[!] VIOLACIONES detectadas: {len(errores_logica)}")
        for e in errores_logica[:15]: print(f"  - {e}")
    else:
        print("[OK] Integridad de Navegación: 100%. NINGÚN camino viola el Back Stack.")

    print("\n--- Cobertura Estructural ---")
    print(f"Nodos: {len(nodos_cubiertos)}/{len(nodos_totales)} | Aristas: {len(aristas_cubiertas)}/{len(aristas_totales)}")
    
    with open('CaminosPrueba/auditoria_detallada.txt', 'w') as f:
        f.write("REPORTE DE TRAZABILIDAD Y COBERTURA\n")
        f.write("==================================\n")
        for p in sorted(list(p_obj_set)):
            f.write(f"Par {p:10} | Cubierto en caminos: {', '.join(map(str, cobertura_map[p]))}\n")

if __name__ == "__main__":
    check_coverage_advanced()
