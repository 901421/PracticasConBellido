"""
Librería de Cliente para el Broker de Mensajes (MOM).

Proporciona una interfaz orientada a objetos de alto nivel (Fachada) para 
interactuar con el Broker. Gestiona la serialización, el protocolo de tramas 
TCP y el ciclo de vida de los mensajes confirmados (ACK).
"""

import socket
import json
import logging

# Configuración de Logging para el lado del cliente
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] Cliente: %(message)s'
)
logger = logging.getLogger(__name__)

class MOMClient:
    """
    Fachada de comunicación para el Message-Oriented Middleware.
    
    Attributes:
        host (str): IP o hostname del broker.
        port (int): Puerto TCP del servicio (default 5555).
        socket (socket.socket): Conexión persistente para operaciones de control.
    """
    
    def __init__(self, host='127.0.0.1', port=5555):
        """
        Args:
            host (str): Dirección del servidor.
            port (int): Puerto del servidor.
        """
        self.host = host
        self.port = port
        self.socket = None

    def _get_connection(self):
        """
        Implementa Lazy Initialization para la conexión persistente.
        Reutiliza el socket para múltiples peticiones de control/publicación.
        """
        if self.socket is None:
            try:
                self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.socket.settimeout(5.0) 
                self.socket.connect((self.host, self.port))
                self.socket.settimeout(None)
                logger.debug(f"Conexión TCP establecida con {self.host}:{self.port}")
            except Exception as e:
                self.socket = None
                logger.error(f"Error de red: {e}")
        return self.socket

    def close(self):
        """Libera recursos cerrando el socket persistente."""
        if self.socket:
            try:
                self.socket.close()
            except Exception: pass
            finally: self.socket = None

    def _send_request(self, payload):
        """
        Centraliza el envío de peticiones y la recepción de respuestas JSON.
        
        Punto 5: Implementa validación estricta de la respuesta del servidor para 
        detectar tramas corruptas o mal formadas.
        
        Args:
            payload (dict): Datos de la acción a enviar.
        Returns:
            dict/None: Respuesta del broker parseada.
        """
        conn = self._get_connection()
        if not conn: return None

        try:
            raw_data = (json.dumps(payload) + "\n").encode('utf-8')
            conn.sendall(raw_data)
            
            # Espera sincrónica para acciones que requieren confirmación inmediata
            if payload.get("action") in ["declare", "list", "delete"]:
                conn.settimeout(5.0)
                resp_bytes = conn.recv(4096)
                conn.settimeout(None)
                
                if not resp_bytes: return None
                
                resp_str = resp_bytes.decode('utf-8').strip()
                try:
                    return json.loads(resp_str)
                except json.JSONDecodeError as e:
                    logger.error(f"Trama corrupta recibida: {resp_str}")
                    raise Exception(f"Protocolo violado. El broker envió datos inválidos: {e}")
                
        except Exception as e:
            logger.error(f"Excepción en comunicación: {e}")
            self.close()
            return None

    def declarar_cola(self, nombre_cola):
        """Declara una cola. Operación idempotente."""
        return self._send_request({"action": "declare", "queue": nombre_cola})

    def publicar(self, nombre_cola, mensaje):
        """Publica un mensaje asíncronamente en una cola."""
        return self._send_request({
            "action": "publish", 
            "queue": nombre_cola, 
            "message": mensaje
        })

    def listar_colas(self):
        """Obtiene la lista de colas activas en el sistema."""
        resp = self._send_request({"action": "list"})
        return resp.get("queues", []) if resp else []

    def eliminar_cola(self, nombre_cola):
        """Elimina una cola y sus mensajes permanentemente."""
        return self._send_request({"action": "delete", "queue": nombre_cola})

    def consumir(self, nombre_cola, callback):
        """
        Se suscribe a una cola e inicia un bucle de escucha activa (Push).
        
        Implementa ACK automático: el mensaje se confirma al servidor solo si el
        callback se ejecuta con éxito.
        
        Args:
            nombre_cola (str): Cola a la que suscribirse.
            callback (function): Procesador del mensaje.
        """
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            s.connect((self.host, self.port))
            req = json.dumps({"action": "consume", "queue": nombre_cola}) + "\n"
            s.sendall(req.encode('utf-8'))
            logger.info(f"Suscrito a '{nombre_cola}'.")
        except Exception as e:
            logger.error(f"Fallo en suscripción: {e}")
            return

        buffer = ""
        try:
            while True:
                data = s.recv(4096)
                if not data: break
                
                buffer += data.decode('utf-8')
                while "\n" in buffer:
                    line, buffer = buffer.split("\n", 1)
                    if not line.strip(): continue
                    
                    try:
                        msg = json.loads(line)
                        if msg.get("action") == "message":
                            # Ejecución de lógica de negocio
                            callback(msg.get("message"))
                            
                            # Envío de ACK tras éxito
                            ack = {"action": "ack", "queue": nombre_cola, "msg_id": msg.get("msg_id")}
                            s.sendall((json.dumps(ack) + "\n").encode('utf-8'))
                    except Exception as e:
                        logger.error(f"Error en callback/ack: {e}")
        except KeyboardInterrupt:
            logger.info("Consumidor detenido.")
        finally:
            s.close()
