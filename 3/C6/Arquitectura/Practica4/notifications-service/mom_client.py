"""
Librería de Cliente para el Broker de Mensajes (MOM).
Proporciona una interfaz de alto nivel para interactuar con el broker mediante 
operaciones de declaración, publicación y consumo asíncrono.
"""

import socket
import json
import logging

# Configuración básica de logging para el cliente
logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(message)s')
logger = logging.getLogger(__name__)

class MOMClient:
    """
    Cliente para la interacción con el sistema Message-Oriented Middleware (MOM).
    Soporta patrones de comunicación asíncrona mediante el protocolo definido sobre TCP.
    """
    
    def __init__(self, host='127.0.0.1', port=5555):
        """
        Inicializa la configuración de conexión al broker.
        
        Args:
            host (str): Dirección IP o hostname del servidor broker.
            port (int): Puerto TCP del servidor broker.
        """
        self.host = host
        self.port = port

    def _send_request(self, payload):
        """Método interno para transacciones síncronas con el broker."""
        # Creamos un socket TCP estándar
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            try:
                # Conectamos al host (inyectado por K8s como 'broker-service')
                s.connect((self.host, self.port))
                # Serializamos la petición a JSON y añadimos salto de línea como delimitador de trama
                trama = (json.dumps(payload) + "\n").encode('utf-8')
                s.sendall(trama)
                
                # Si la acción es 'declare', esperamos el acuse de recibo del servidor
                if payload.get("action") == "declare":
                    raw_response = s.recv(1024).decode('utf-8')
                    if raw_response:
                        return json.loads(raw_response)
            except Exception as e:
                logger.error(f"Error de comunicación MOM: {e}")

    def declarar_cola(self, nombre_cola):
        """Asegura la existencia de una cola."""
        # Enviamos la acción 'declare' según el protocolo de la P3
        payload = {"action": "declare", "queue": nombre_cola}
        return self._send_request(payload)

    def publicar(self, nombre_cola, mensaje):
        """Emite un mensaje hacia una cola."""
        # Construimos la trama de publicación
        payload = {
            "action": "publish", 
            "queue": nombre_cola, 
            "message": mensaje
        }
        self._send_request(payload)

    def consumir(self, nombre_cola, callback):
        """Se suscribe para recibir mensajes y gestiona ACKs."""
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            s.connect((self.host, self.port))
            # Registramos la suscripción
            payload = {"action": "consume", "queue": nombre_cola}
            s.sendall((json.dumps(payload) + "\n").encode('utf-8'))
            
            buffer = ""
            while True:
                # Leemos datos del socket
                data = s.recv(4096)
                if not data: break
                
                buffer += data.decode('utf-8')
                # El protocolo usa '\n' para separar mensajes en el stream TCP
                while "\n" in buffer:
                    line, buffer = buffer.split("\n", 1)
                    if not line.strip(): continue
                        
                    msg_data = json.loads(line)
                    if msg_data.get("action") == "message":
                        # Identificadores necesarios para el ACK
                        msg_id = msg_data.get("msg_id")
                        queue = msg_data.get("queue")
                        
                        # Ejecutamos la lógica de negocio del servicio
                        callback(msg_data.get("message"))
                        
                        # IMPORTANTE (P3/P4): Enviamos el ACK tras procesar el mensaje.
                        # Esto permite al Broker saber que puede enviarnos el siguiente (prefetch=1).
                        ack_payload = {
                            "action": "ack",
                            "queue": queue,
                            "msg_id": msg_id
                        }
                        s.sendall((json.dumps(ack_payload) + "\n").encode('utf-8'))
        finally:
            s.close()
