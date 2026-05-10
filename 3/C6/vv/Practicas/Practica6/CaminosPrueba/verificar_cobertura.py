import os
import collections

# GRAFO ACTUALIZADO (Sincronizado con generador_caminos_pruebas.py)
GRAFO = {
    '1': ('N1', 'N2'), '2': ('N1', 'N4'), '3': ('N1', 'N3'), '4': ('N1', 'N5'),
    '5': ('N2', 'N3'), 
    '13': ('N2', 'N2'), '13b': ('N2', 'N2'), '13c': ('N2', 'N2'), 
    '14': ('N2', 'N2'), '21': ('N2', 'N1'),
    '6': ('N3', 'N2'), '6b': ('N3', 'N2'), '6c': ('N3', 'N2'), 
    '7': ('N3', 'N1'), '7b': ('N3', 'N1'), '7c': ('N3', 'N1'), 
    '15': ('N3', 'N3'), '15b': ('N3', 'N3'),
    '8': ('N4', 'N5'), 
    '16': ('N4', 'N4'), '16b': ('N4', 'N4'), '16c': ('N4', 'N4'), '16d': ('N4', 'N4'), 
    '16e': ('N4', 'N4'), '16f': ('N4', 'N4'), '16g': ('N4', 'N4'), '16h': ('N4', 'N4'), 
    '17': ('N4', 'N4'), '18': ('N4', 'N4'), '22': ('N4', 'N1'),
    '9': ('N5', 'N4'), '9b': ('N5', 'N4'), '9c': ('N5', 'N4'), 
    '10': ('N5', 'N1'), '10b': ('N5', 'N1'), '10c': ('N5', 'N1'), 
    '11': ('N5', 'N6'), 
    '19': ('N5', 'N5'), '19b': ('N5', 'N5'),
    '12': ('N6', 'N5'), '12b': ('N6', 'N5'), '12c': ('N6', 'N5'), 
    '20': ('N6', 'N6'), '20b': ('N6', 'N6'), '20c': ('N6', 'N6'), 
    '23': ('N6', 'N6'), '24': ('N6', 'N6')
}

def check_coverage():
    input_file = 'CaminosPrueba/pares_entrada.txt'
    output_file = 'CaminosPrueba/caminos_resultantes.txt'

    if not os.path.exists(input_file) or not os.path.exists(output_file):
        print("Error: Archivos no encontrados.")
        return

    with open(input_file, 'r') as f:
        pares_objetivo = {line.strip() for line in f if line.strip() and not line.startswith('#')}
    
    with open(output_file, 'r') as f:
        caminos = [line.strip().split(',') for line in f if line.strip()]
    
    cobertura_map = collections.defaultdict(list)
    pares_detectados = set()
    
    for idx, camino in enumerate(caminos, 1):
        for i in range(len(camino) - 1):
            pair = f"{camino[i]},{camino[i+1]}"
            cobertura_map[pair].append(idx)
            pares_detectados.add(pair)
    
    errores_logica = []
    for idx, camino in enumerate(caminos, 1):
        hr, hd, hq = False, False, False
        temp_hq = False
        ctx_res = None
        ctx_quad = None
        
        for i, a in enumerate(camino):
            # Contextos
            if a == '4': ctx_res = 'crear'; hr, hd, hq = False, False, False
            elif a == '8': ctx_res = 'editar'; hr, hd, hq = True, True, True
            elif a == '3': ctx_quad = 'crear'
            elif a == '5': ctx_quad = 'editar'
            
            # Estados
            if a == '19': hr = True
            elif a == '19b': hd = True
            elif a == '11': temp_hq = hq
            elif a == '24': temp_hq = True
            elif a == '12': hq = temp_hq

            # Validaciones
            if a == '11' and not (hr and hd):
                errores_logica.append(f"C{idx} P{i+1}: 11 requiere fechas")
            if a in ['9', '10'] and not (hr and hd and hq):
                errores_logica.append(f"C{idx} P{i+1}: {a} requiere hq/fechas")
            if a in ['10', '10b', '10c'] and ctx_res != 'crear':
                errores_logica.append(f"C{idx} P{i+1}: {a} requiere ctx crear")
            if a in ['9', '9b', '9c'] and ctx_res != 'editar':
                errores_logica.append(f"C{idx} P{i+1}: {a} requiere ctx editar")
            if a in ['7', '7b', '7c'] and ctx_quad != 'crear':
                errores_logica.append(f"C{idx} P{i+1}: {a} requiere ctx crear (Q)")
            if a in ['6', '6b', '6c'] and ctx_quad != 'editar':
                errores_logica.append(f"C{idx} P{i+1}: {a} requiere ctx editar (Q)")

            # Limpieza
            if a in ['10', '10b', '10c', '9', '9b', '9c']:
                ctx_res = None; hr, hd, hq = False, False, False
            elif a in ['7', '7b', '7c', '6', '6b', '6c']:
                ctx_quad = None

    print(f"Total caminos: {len(caminos)}")
    print(f"Pares objetivo: {len(pares_objetivo)}")
    print(f"Pares cubiertos: {len(pares_detectados.intersection(pares_objetivo))}")
    
    missing = pares_objetivo - pares_detectados
    if missing:
        print(f"Faltan {len(missing)} pares.")
        for m in sorted(list(missing)): print(f"  - {m}")
    else:
        print("COBERTURA 100%")

    if errores_logica:
        print(f"ERRORES: {len(errores_logica)}")
        for e in errores_logica[:10]: print(f"  - {e}")
    else:
        print("INTEGRIDAD OK")

if __name__ == "__main__":
    check_coverage()
