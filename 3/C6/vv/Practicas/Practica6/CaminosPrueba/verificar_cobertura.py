import os
import collections

# CONFIGURACIÓN DEL GRAFO Y REGLAS (Para validación independiente)
GRAFO = {
    '1': ('N1', 'N2'), '2': ('N1', 'N4'), '3': ('N1', 'N3'), '4': ('N1', 'N5'),
    '5': ('N2', 'N3'), '13': ('N2', 'N2'), '13b': ('N2', 'N2'), '13c': ('N2', 'N2'), '14': ('N2', 'N2'), '23': ('N2', 'N1'),
    '6': ('N3', 'N2'), '6b': ('N3', 'N2'), '6c': ('N3', 'N2'), '7': ('N3', 'N1'), '7b': ('N3', 'N1'), '7c': ('N3', 'N1'), '15': ('N3', 'N3'), '15b': ('N3', 'N3'),
    '8': ('N4', 'N5'), '16': ('N4', 'N4'), '16b': ('N4', 'N4'), '16c': ('N4', 'N4'), '16d': ('N4', 'N4'), '16e': ('N4', 'N4'), '16f': ('N4', 'N4'), '16g': ('N4', 'N4'), '16h': ('N4', 'N4'), '17': ('N4', 'N4'), '18': ('N4', 'N4'), '24': ('N4', 'N1'),
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
    
    # 2. VALIDACIÓN DE LÓGICA DE ESTADO POR CAMINO
    errores_logica = []
    for idx, camino in enumerate(caminos, 1):
        hr, hd, hq = False, False, False
        ctx = None
        for i, a in enumerate(camino):
            # Reglas de Pila
            if a == '4': ctx = 'crear'; hr, hd, hq = False, False, False
            if a == '8': ctx = 'editar'; hr, hd, hq = True, True, True
            
            # Reglas de Estado
            if a == '20': hr = True
            if a == '20b': hd = True
            if a == '12': hq = True
            
            # Verificaciones
            if a == '11' and not (hr and hd):
                errores_logica.append(f"Camino {idx}: Selección quads (11) sin fechas completas en paso {i+1}")
            if a in ['9', '10'] and not (hr and hd and hq):
                errores_logica.append(f"Camino {idx}: Guardado ({a}) sin quads/fechas en paso {i+1}")
            if a in ['10', '10b', '10c'] and ctx == 'editar':
                errores_logica.append(f"Camino {idx}: Retorno a N1 ({a}) en contexto de EDICION en paso {i+1}")
            if a in ['9', '9b', '9c'] and ctx == 'crear':
                errores_logica.append(f"Camino {idx}: Retorno a N4 ({a}) en contexto de CREACION en paso {i+1}")

    # 3. ANÁLISIS DE SUFICIENCIA
    nodos_cubiertos = set()
    aristas_cubiertas = set()
    for camino in caminos:
        for a in camino:
            aristas_cubiertas.add(a)
            nodos_cubiertos.add(GRAFO[a][0])
            nodos_cubiertos.add(GRAFO[a][1])

    nodos_totales = {'N1', 'N2', 'N3', 'N4', 'N5', 'N6'}
    aristas_totales = set(GRAFO.keys())

    # INFORME
    print("=== AUDITORÍA DE CAMINOS DE PRUEBA ===")
    print(f"Total de caminos analizados: {len(caminos)}")
    print(f"Pares de aristas objetivo: {len(pares_objetivo)}")
    print(f"Pares cubiertos: {len(total_pares_detectados.intersection(set(pares_objetivo)))}")
    
    missing = [p for p in pares_objetivo if p not in total_pares_detectados]
    if missing:
        print(f"[!] Pares FALTANTES: {len(missing)}")
        for m in missing[:5]: print(f"  - {m}")
    else:
        print("[OK] Cobertura Edge-Pair: 100%")

    print("\n--- Validación de Lógica de Estado ---")
    if errores_logica:
        print(f"[!] ERRORES detectados: {len(errores_logica)}")
        for e in errores_logica[:10]: print(f"  - {e}")
    else:
        print("[OK] Todos los caminos respetan las reglas de la App y el Stack.")

    print("\n--- Análisis de Suficiencia ---")
    print(f"Cobertura de Nodos: {len(nodos_cubiertos)}/{len(nodos_totales)} ({int(len(nodos_cubiertos)/len(nodos_totales)*100)}%)")
    print(f"Cobertura de Aristas: {len(aristas_cubiertas)}/{len(aristas_totales)} ({int(len(aristas_cubiertas)/len(aristas_totales)*100)}%)")
    
    if len(nodos_cubiertos) == len(nodos_totales) and len(aristas_cubiertas) == len(aristas_totales):
        print("[CONCLUSIÓN] Los caminos son SUFICIENTES para testear toda la funcionalidad de la App.")
    else:
        print("[ADVERTENCIA] Los caminos podrían no cubrir toda la funcionalidad.")

    # Guardar mapeo detallado para el desarrollador
    with open('CaminosPrueba/auditoria_detallada.txt', 'w') as f:
        f.write("MAPEO DE TRAZABILIDAD (PAR -> CAMINOS)\n")
        f.write("=====================================\n")
        for p in sorted(pares_objetivo):
            f.write(f"Par {p:10} | Caminos: {', '.join(map(str, cobertura_map[p]))}\n")

if __name__ == "__main__":
    check_coverage_advanced()
