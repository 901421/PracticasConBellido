import itertools
import os

# DEFINICIÓN DEL GRAFO (Basado en mapa_navegacion.tex)
# Cada arista tiene un nodo de origen (org) y un nodo de destino (dest).
GRAFO = {
    # N1: Menú Principal
    '1':  {'org': 'N1', 'dest': 'N2'},
    '2':  {'org': 'N1', 'dest': 'N4'},
    '3':  {'org': 'N1', 'dest': 'N3'},
    '4':  {'org': 'N1', 'dest': 'N5'},
    
    # N2: Listado Quads
    '5':  {'org': 'N2', 'dest': 'N3'},
    '13': {'org': 'N2', 'dest': 'N2'},
    '13b':{'org': 'N2', 'dest': 'N2'},
    '13c':{'org': 'N2', 'dest': 'N2'},
    '14': {'org': 'N2', 'dest': 'N2'},
    '21': {'org': 'N2', 'dest': 'N1'},
    
    # N3: Formulario Quad
    '6':  {'org': 'N3', 'dest': 'N2'},
    '6b': {'org': 'N3', 'dest': 'N2'},
    '6c': {'org': 'N3', 'dest': 'N2'},
    '7':  {'org': 'N3', 'dest': 'N1'},
    '7b': {'org': 'N3', 'dest': 'N1'},
    '7c': {'org': 'N3', 'dest': 'N1'},
    '15': {'org': 'N3', 'dest': 'N3'},
    '15b':{'org': 'N3', 'dest': 'N3'},
    
    # N4: Listado Reservas
    '8':  {'org': 'N4', 'dest': 'N5'},
    '16': {'org': 'N4', 'dest': 'N4'},
    '16b':{'org': 'N4', 'dest': 'N4'},
    '16c':{'org': 'N4', 'dest': 'N4'},
    '16d':{'org': 'N4', 'dest': 'N4'},
    '16e':{'org': 'N4', 'dest': 'N4'},
    '16f':{'org': 'N4', 'dest': 'N4'},
    '16g':{'org': 'N4', 'dest': 'N4'},
    '16h':{'org': 'N4', 'dest': 'N4'},
    '17': {'org': 'N4', 'dest': 'N4'},
    '18': {'org': 'N4', 'dest': 'N4'},
    '22': {'org': 'N4', 'dest': 'N1'},
    
    # N5: Formulario Reserva
    '9':  {'org': 'N5', 'dest': 'N4'},
    '9b': {'org': 'N5', 'dest': 'N4'},
    '9c': {'org': 'N5', 'dest': 'N4'},
    '10': {'org': 'N5', 'dest': 'N1'},
    '10b':{'org': 'N5', 'dest': 'N1'},
    '10c':{'org': 'N5', 'dest': 'N1'},
    '11': {'org': 'N5', 'dest': 'N6'},
    '19': {'org': 'N5', 'dest': 'N5'},
    '19b':{'org': 'N5', 'dest': 'N5'},
    
    # N6: Selección Quads
    '12': {'org': 'N6', 'dest': 'N5'},
    '12b':{'org': 'N6', 'dest': 'N5'},
    '12c':{'org': 'N6', 'dest': 'N5'},
    '20': {'org': 'N6', 'dest': 'N6'},
    '20b':{'org': 'N6', 'dest': 'N6'},
    '20c':{'org': 'N6', 'dest': 'N6'},
    '23': {'org': 'N6', 'dest': 'N6'},
    '24': {'org': 'N6', 'dest': 'N6'},
}

def generar_pares_grafo():
    aristas = sorted(GRAFO.keys())
    # Todas las combinaciones posibles de aristas (A, B)
    todas_las_parejas = list(itertools.product(aristas, aristas))
    
    pares_navegacion = []

    for a1, a2 in todas_las_parejas:
        data1 = GRAFO[a1]
        data2 = GRAFO[a2]
        
        # Regla de Navegación del Grafo: Destino de A == Origen de B
        if data1['dest'] == data2['org']:
            pares_navegacion.append((a1, a2))

    # Escribir resultados en el formato solicitado
    output_path = "ParesAristas/resultado_parejas.txt"
    with open(output_path, "w") as f:
        for a1, a2 in pares_navegacion:
            f.write(f"{a1},{a2}\n")

    print(f"Análisis estructural completado.")
    print(f"Se han generado {len(pares_navegacion)} pares de aristas basados puramente en la navegación del grafo.")
    print(f"Resultado guardado en: {output_path}")

if __name__ == "__main__":
    generar_pares_grafo()
