# Imad Habib Jali 901421
# Jorge Bellido Lobera 309080
import random

def generar_caso(n, m, c, k, archivo):
    # Escribimos la cabecera en el archivo
    print(f"{n} {m} {c} {k}", file=archivo)
    
    # 1. Crear un camino circular base para garantizar que es conexo.
    for i in range(1, n):
        peso = random.randint(5, 45)
        print(f"{i} {i+1} {peso}", file=archivo)
    
    # Cerramos el círculo
    peso_cierre = random.randint(5, 45)
    print(f"{n} 1 {peso_cierre}", file=archivo)
    
    # 2. Añadir aristas extra
    aristas_extra = m - n
    aristas_generadas = set()
    
    while len(aristas_generadas) < aristas_extra:
        u = random.randint(1, n)
        v = random.randint(1, n)
        
        if u != v and (u, v) not in aristas_generadas and (v, u) not in aristas_generadas:
            aristas_generadas.add((u, v))
            peso_extra = random.randint(5, 45)
            print(f"{u} {v} {peso_extra}", file=archivo)
            
    # 3. Elegir los centros existentes
    if c > 0:
        centros_existentes = random.sample(range(1, n + 1), c)
        print(" ".join(map(str, centros_existentes)), file=archivo)
    else:
        print("", file=archivo)

# ==========================================
# EJECUCIÓN Y ESCRITURA AUTOMÁTICA EN ARCHIVO
# ==========================================

nombre_archivo = "pruebas_py.txt"

# Abrimos el archivo en modo escritura ("w" = write). 
# Si no existe, lo crea. Si existe, lo machaca con los nuevos datos.
with open(nombre_archivo, "w") as f:
    
    num_casos = 4
    print(num_casos, file=f)

    # Caso 1: Pequeño
    generar_caso(n=10, m=15, c=1, k=2, archivo=f)

    # Caso 2: Mediano
    generar_caso(n=30, m=45, c=2, k=3, archivo=f)

    # Caso 3: Grande
    generar_caso(n=50, m=80, c=3, k=4, archivo=f)

    # Caso 4: Muy Grande
    generar_caso(n=70, m=120, c=4, k=5, archivo=f)

print(f"¡Listo! Los {num_casos} casos de prueba se han guardado correctamente en '{nombre_archivo}'.")