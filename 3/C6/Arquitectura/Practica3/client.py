"""
Librería de Cliente para el Broker de Mensajes (MOM).

Proporciona una interfaz orientada a objetos de alto nivel (Fachada) para 
interactuar con el Broker. Gestiona la serialización, el protocolo de tramas 
TCP y el ciclo de vida de los mensajes confirmados (ACK).
"""

import socket # Librería base para comunicaciones de red a bajo nivel (TCP/IP)
import json # Usado para construir los payloads que se enviarán por la red
import logging # Para reportar el estado de la comunicación en la consola del cliente

# Configuración de Logging exclusiva para el lado del cliente
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] Cliente: %(message)s'
)
logger = logging.getLogger(__name__)

class MOMClient:
    """
    Fachada de comunicación para el Message-Oriented Middleware.
    
    Encapsula toda la complejidad de la red (TCP, timeouts, serialización) 
    para que los productores y consumidores interactúen con métodos simples.
    """
    
    def __init__(self, host='127.0.0.1', port=5555):
        """
        Constructor del cliente.
        
        Args:
            host (str): IP o hostname del broker (por defecto localhost).
            port (int): Puerto TCP del servicio del broker (por defecto 5555).
        """
        # Guardamos la dirección IP objetivo
        self.host = host
        # Guardamos el puerto objetivo
        self.port = port
        # Inicializamos el socket a None. Se creará solo cuando se intente usar (Lazy Initialization)
        self.socket = None

    def _get_connection(self):
        """
        Gestiona la conexión TCP persistente hacia el servidor.
        
        Implementa 'Lazy Initialization': solo abre la conexión si no existe previamente.
        Reutiliza el mismo socket para múltiples peticiones de control o publicación.
        """
        # Si no tenemos un socket activo, intentamos crearlo
        if self.socket is None:
            try:
                # Creamos un socket de tipo IPv4 (AF_INET) y flujo TCP (SOCK_STREAM)
                self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                # Ponemos un timeout de 5 segundos para no quedarnos colgados infinitamente si el broker está caído
                self.socket.settimeout(5.0) 
                # Intentamos conectar físicamente al servidor
                self.socket.connect((self.host, self.port))
                # Una vez conectados, quitamos el timeout general para que las operaciones puedan fluir libremente
                self.socket.settimeout(None)
                logger.debug(f"Conexión TCP establecida con {self.host}:{self.port}")
            except Exception as e:
                # Si falla la conexión, limpiamos el socket y reportamos el error
                self.socket = None
                logger.error(f"Error de red: {e}")
        # Devolvemos el socket (puede ser None si falló)
        return self.socket

    def close(self):
        """Libera los recursos del sistema cerrando el socket persistente ordenadamente."""
        if self.socket:
            try:
                # Ordenamos al sistema operativo cerrar la conexión TCP
                self.socket.close()
            except Exception: 
                # Si falla al cerrar (ej. ya estaba cerrado), lo ignoramos silenciosamente
                pass
            finally: 
                # Aseguramos que la variable vuelva a None para futuras reconexiones
                self.socket = None

    def _send_request(self, payload):
        """
        Centraliza el envío de peticiones síncronas al broker y la recepción de su respuesta.
        
        Envía un diccionario como JSON, y espera una respuesta JSON del broker.
        Implementa un protocolo delimitado por salto de línea ('\\n').
        
        Args:
            payload (dict): Diccionario con la acción y los datos a enviar.
        Returns:
            dict/None: Respuesta del broker parseada, o None si hay error.
        """
        # Obtenemos la conexión activa (creándola si es necesario)
        conn = self._get_connection()
        # Si no se pudo conectar, abortamos la petición
        if not conn: return None

        try:
            # Convertimos el diccionario a un string JSON, le añadimos el salto de línea (delimitador)
            # y lo codificamos en bytes (UTF-8) para poder enviarlo por la red
            raw_data = (json.dumps(payload) + "\n").encode('utf-8')
            
            # sendall asegura que absolutamente todos los bytes se envíen al buffer del SO
            conn.sendall(raw_data)
            
            # Verificamos si esta acción espera una respuesta inmediata del servidor
            if payload.get("action") in ["declare", "list", "delete"]:
                # Como esperamos respuesta, fijamos un timeout de 5s para no bloquear al cliente
                # si el servidor procesa lento o se cae en medio.
                conn.settimeout(5.0)
                # Leemos hasta 4096 bytes de la respuesta
                resp_bytes = conn.recv(4096)
                # Restauramos el socket a modo bloqueante normal
                conn.settimeout(None)
                
                # Si recv devuelve bytes vacíos, el servidor cerró la conexión
                if not resp_bytes: return None
                
                # Decodificamos los bytes recibidos a texto y quitamos espacios en blanco o saltos de línea
                resp_str = resp_bytes.decode('utf-8').strip()
                try:
                    # Parseamos la respuesta de texto a diccionario Python
                    return json.loads(resp_str)
                except json.JSONDecodeError as e:
                    # Si el servidor mandó basura o la trama se cortó a la mitad (corrupta)
                    logger.error(f"Trama corrupta recibida: {resp_str}")
                    raise Exception(f"Protocolo violado. El broker envió datos inválidos: {e}")
                
        except Exception as e:
            # Si hay cualquier error de I/O de red, cerramos la conexión corrupta
            logger.error(f"Excepción en comunicación: {e}")
            self.close()
            return None

    def declarar_cola(self, nombre_cola):
        """
        Solicita al broker crear una cola si no existe. 
        Es una operación idempotente (llamarlo muchas veces no hace daño).
        """
        return self._send_request({"action": "declare", "queue": nombre_cola})

    def publicar(self, nombre_cola, mensaje):
        """
        Inyecta un mensaje en una cola. Es una operación asíncrona "Fire-and-Forget".
        El cliente no espera un OK para publicar, solo lo envía al socket.
        """
        return self._send_request({
            "action": "publish", 
            "queue": nombre_cola, 
            "message": mensaje
        })

    def listar_colas(self):
        """
        Pide al broker la lista de colas creadas.
        Devuelve una lista de strings con los nombres.
        """
        resp = self._send_request({"action": "list"})
        # Retorna la lista de colas, o una lista vacía si hubo error
        return resp.get("queues", []) if resp else []

    def eliminar_cola(self, nombre_cola):
        """Pide al broker eliminar por completo una cola y todos sus mensajes internos."""
        return self._send_request({"action": "delete", "queue": nombre_cola})

    def consumir(self, nombre_cola, callback):
        """
        Patrón 'Push' Asíncrono.
        Conecta un socket exclusivo para escuchar mensajes de la cola continuamente.
        
        Implementa ACK Automático: Tras ejecutar el callback del usuario sin fallos,
        informa al servidor que el mensaje ha sido procesado correctamente.
        
        Args:
            nombre_cola (str): Nombre de la cola objetivo.
            callback (function): Función inyectada por el usuario que recibirá el mensaje.
        """
        # Creamos un socket de consumo totalmente separado del socket de control (self.socket)
        # Esto es vital para que este socket se pueda bloquear infinitamente escuchando al servidor.
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            # Nos conectamos al servidor
            s.connect((self.host, self.port))
            # Formamos la petición de consumo y la enviamos al broker
            req = json.dumps({"action": "consume", "queue": nombre_cola}) + "\n"
            s.sendall(req.encode('utf-8'))
            logger.info(f"Suscrito a '{nombre_cola}'.")
        except Exception as e:
            logger.error(f"Fallo en suscripción: {e}")
            return

        # Buffer para almacenar bytes crudos en caso de que TCP fragmente los paquetes en la red
        raw_buffer = b""
        try:
            # Bucle infinito: el hilo se quedará aquí bloqueado esperando recibir eventos (mensajes)
            while True:
                # recv() bloquea hasta que el servidor envía algo por la red
                data = s.recv(4096)
                # Si data está vacío, el servidor nos ha desconectado o cerrado bruscamente
                if not data: break
                
                # Añadimos los bytes recién llegados al buffer general
                raw_buffer += data
                
                # Revisamos si dentro del buffer acumulado hay un delimitador de mensaje (salto de línea)
                # El bucle while permite procesar varios mensajes si llegaron todos juntos en el mismo paquete TCP
                while b"\n" in raw_buffer:
                    # Partimos el buffer en dos: la línea actual completa, y lo que quede de buffer (resto)
                    line_bytes, raw_buffer = raw_buffer.split(b"\n", 1)
                    
                    # Si era una línea en blanco (ej. múltiples \n), la ignoramos
                    if not line_bytes.strip(): continue
                        
                    try:
                        # Ahora que tenemos una trama segura y completa de bytes, la decodificamos a texto
                        line = line_bytes.decode('utf-8')
                        # Parseamos el texto JSON a un diccionario Python
                        msg = json.loads(line)
                        
                        # Comprobamos que sea un diccionario y que sea efectivamente un mensaje del servidor
                        if isinstance(msg, dict) and msg.get("action") == "message":
                            
                            # --- LÓGICA DE NEGOCIO (INYECCIÓN DE DEPENDENCIA) ---
                            # Llamamos a la función que nos pasó el programa principal, pasándole el contenido del mensaje.
                            callback(msg.get("message"))
                            
                            # --- RECONOCIMIENTO (ACK) ---
                            # Si el callback terminó bien (no lanzó excepción), formamos el paquete de ACK.
                            # El ACK lleva el ID único del mensaje para que el servidor sepa cuál borrar definitivamente.
                            ack = {"action": "ack", "queue": nombre_cola, "msg_id": msg.get("msg_id")}
                            # Enviamos el ACK al servidor por el mismo socket
                            s.sendall((json.dumps(ack) + "\n").encode('utf-8'))
                    
                    except Exception as e:
                        # Si el callback falla por culpa de la lógica del usuario, o el ACK falla por red,
                        # se reporta. IMPORTANTE: Al no enviar el ACK, el servidor detectará "ACK Timeout"
                        # y re-encolará el mensaje internamente para que sea procesado de nuevo.
                        logger.error(f"Error en callback/ack: {e}")
                        
        except KeyboardInterrupt:
            # Captura de Control+C para que la salida en consola sea limpia si el usuario cierra la aplicación
            logger.info("Consumidor detenido.")
        finally:
            # Siempre, ocurra lo que ocurra, cerramos el socket para liberar puertos en el sistema operativo
            s.close()
