import random
import sys
import os

def generate_case(N):
    case_str = f"{N}\n"
    for i in range(N):
        row = [random.randint(0, 10) for _ in range(N)]
        row[i] = 0
        case_str += " ".join(map(str, row)) + "\n"
    return case_str

def main():
    if len(sys.argv) < 2:
        print("Uso: python gen_casos.py <N1> <N2> ...")
        print("Ejemplo: python gen_casos.py 12 15 18")
        sys.exit(1)

    for arg in sys.argv[1:]:
        try:
            N = int(arg)
            if N % 3 != 0:
                print(f"Saltando N={N} (no es múltiplo de 3)")
                continue
            
            filename = f"prueba_N{N}.txt"
            content = generate_case(N)
            
            with open(filename, 'w', encoding='ascii') as f:
                f.write(content)
            print(f"Generado: {filename}")
            
        except ValueError:
            print(f"Argumento inválido: {arg}")

if __name__ == "__main__":
    main()
