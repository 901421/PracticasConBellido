"""
Aplicación Productora de Ejemplo (Script principal).

Simula un componente de sistema externo que genera eventos continuos 
(ej. telemetría, logs, acciones de usuario) y los encola en el Broker 
de Mensajes para que sean procesados asíncronamente por otros servicios.
"""

import time
import sys
from client import MOMClient

def main():
    """
    Punto de entrada principal para el productor.
    Obtiene la configuración por argumentos, inicializa la conexión con la 
    API del MOMClient e inyecta mensajes en un bucle infinito (mocking de datos).
    """
    # Se extrae la IP del broker desde línea de comandos para facilitar su uso 
    # en contenedores Docker o entornos de red distribuidos.
    # Uso: python productor.py [IP_DEL_BROKER]
    broker_ip = sys.argv[1] if len(sys.argv) > 1 else "127.0.0.1"
    
    # Instanciamos la fachada de cliente configurada hacia nuestro broker
    broker = MOMClient(host=broker_ip)
    
    # Constante con el nombre de la cola sobre la cual inyectaremos los datos
    cola_nombre = "mi_cola_pruebas"
    
    print(f"[*] Intentando conexión con el Broker en {broker_ip}...")
    
    # Garantizamos que la cola exista antes de publicar, operacion idempotente
    broker.declarar_cola(cola_nombre)
    
    contador = 1
    try:
        # Bucle principal de generación de eventos simulados
        while True:
            # Estructura del evento, se puede serializar a JSON
            mensaje = {
                "id_mensaje": contador, 
                "texto": f"Generado evento simulado de telemetría número #{contador}",
                "timestamp": time.time() # Marca temporal del suceso
            }
            
            print(f"[*] Productor Inyectando -> {mensaje}")
            # Llamada asíncrona a la red para enviar el paquete
            broker.publicar(cola_nombre, mensaje)
            
            contador += 1
            # Se incluye una pausa para no saturar el servidor y simular ráfagas espaciadas
            time.sleep(2)
            
    except KeyboardInterrupt:
        # Finalización elegante si se pulsa Ctrl+C en consola
        print("\n[*] Señal de terminación recibida. Productor deteniéndose ordenadamente.")
        # Se cierran las conexiones de red persistentes
        broker.close()
        # Salida limpia al sistema operativo
        sys.exit(0)

if __name__ == "__main__":
    # Ejecuta el flujo principal solo si el script se llama de forma directa
    main()
