"""
Librería de Cliente para el Broker de Mensajes (MOM).

Proporciona una interfaz orientada a objetos (API de alto nivel) para interactuar 
con el Broker. Oculta la complejidad de los sockets y el protocolo JSON.
Soporta conexiones persistentes para operaciones de baja latencia (publicar/declarar) 
y un bucle de consumo asíncrono robusto con confirmaciones automáticas (ACK).
"""

import socket
import json
import logging

# Configuración del logger local del cliente
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] Cliente: %(message)s'
)
logger = logging.getLogger(__name__)

class MOMClient:
    """
    Clase Fachada para simplificar la comunicación con el Message-Oriented Middleware.
    
    Atributos:
        host (str): Dirección IP o hostname del servidor Broker.
        port (int): Puerto TCP en el que el Broker acepta conexiones.
        socket (socket.socket): Objeto de socket persistente mantenido para 
                                optimizar operaciones consecutivas y no crear 
                                un socket por cada peticion.
    """
    
    def __init__(self, host='127.0.0.1', port=5555):
        """
        Inicializa el estado del cliente con los parámetros de conexión al servidor.
        
        Args:
            host (str): IP del servidor broker. Por defecto localhost.
            port (int): Puerto del servidor broker. Por defecto 5555.
        """
        self.host = host
        self.port = port
        self.socket = None # Inicialmente desconectado

    def _get_connection(self):
        """
        Mantiene una única conexión TCP abierta para reutilizarla en múltiples peticiones.
        Implementa un patrón simple de Lazy Initialization (inicialización diferida).
        
        Returns:
            socket.socket: Objeto Socket conectado y listo para usarse, 
                           o None si ocurre un error de red o timeout.
        """
        if self.socket is None:
            try:
                # Creación del socket TCP sobre IPv4
                self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                # Tiempo de espera máximo de conexión para no bloquear indefinidamente
                self.socket.settimeout(5.0) 
                self.socket.connect((self.host, self.port))
                # Tras conectar con éxito, se desactiva el timeout general de operaciones
                self.socket.settimeout(None)
                logger.debug(f"Conexión persistente TCP establecida exitosamente con {self.host}:{self.port}")
            except Exception as e:
                # En caso de fallo (conexión rechazada, broker apagado), limpiamos estado
                self.socket = None
                logger.error(f"Fallo de red conectando al broker: {e}")
        return self.socket

    def close(self):
        """
        Libera los recursos de red de forma prolija.
        Cierra explícitamente el socket asociado al cliente.
        """
        if self.socket:
            try:
                self.socket.close()
            except Exception:
                pass # Ignora excepciones menores al cerrar
            finally:
                self.socket = None

    def _send_request(self, payload):
        """
        Serializa un diccionario Python a JSON, le añade el delimitador de trama (\n)
        y lo envía al servidor broker a través de la conexión persistente.
        
        Args:
            payload (dict): Diccionario conteniendo la acción ("action") y sus parámetros.
            
        Returns:
            dict: Respuesta del broker parseada a diccionario si se espera una (ej. declare), 
                  o None si hubo error o no se espera respuesta (ej. publish).
        """
        conn = self._get_connection()
        if not conn: return None # Falla rápido si no hay conexión

        try:
            # Serialización a cadena JSON + salto de línea y conversión a bytes UTF-8
            raw_data = (json.dumps(payload) + "\n").encode('utf-8')
            # Garantiza el envío completo del paquete a la red
            conn.sendall(raw_data)
            
            # Solo esperamos una respuesta sincrónica del broker para acciones de control.
            # Operaciones como 'publish' son asíncronas para mejorar el rendimiento del productor.
            if payload.get("action") in ["declare", "list", "delete"]:
                conn.settimeout(5.0) # Evita quedarse colgado esperando respuesta eternamente
                # Recepción de la respuesta en bytes
                resp_bytes = conn.recv(4096)
                conn.settimeout(None) # Restaura a modo bloqueante normal
                
                # Decodificación y des-serialización
                resp_str = resp_bytes.decode('utf-8').strip()
                return json.loads(resp_str) if resp_str else None
                
        except Exception as e:
            # Si se rompe el socket durante el envío, registramos y limpiamos
            logger.error(f"Error de comunicación en envío: {e}")
            self.close()
            return None

    def declarar_cola(self, nombre_cola):
        """
        Asegura la existencia de una cola de mensajes en el broker.
        Esta operación es idempotente (se puede llamar 1 o 100 veces con el mismo 
        resultado sin efectos colaterales).
        
        Args:
            nombre_cola (str): Identificador alfanumérico para la cola.
        Returns:
            dict: Respuesta de status del servidor.
        """
        return self._send_request({"action": "declare", "queue": nombre_cola})

    def publicar(self, nombre_cola, mensaje):
        """
        Envía información asíncronamente a una cola previamente declarada.
        El productor no espera acuse de recibo de entrega a consumidores finales.
        
        Args:
            nombre_cola (str): Nombre de la cola destino.
            mensaje (any): El payload o cuerpo del mensaje (string, dict, int, etc).
        Returns:
            None: No se espera acuse de recibo explícito en este modelo simple.
        """
        return self._send_request({
            "action": "publish", 
            "queue": nombre_cola, 
            "message": mensaje
        })

    def listar_colas(self):
        """
        Interroga al broker por todas las colas creadas y activas actualmente.
        
        Returns:
            list: Lista de strings con los nombres de las colas.
        """
        resp = self._send_request({"action": "list"})
        return resp.get("queues", []) if resp else []

    def eliminar_cola(self, nombre_cola):
        """
        Destruye definitivamente una cola en el broker y purga todos los 
        mensajes que aún conservara sin entregar.
        
        Args:
            nombre_cola (str): Cola a destruir.
        Returns:
            dict: Resultado de la operación.
        """
        return self._send_request({"action": "delete", "queue": nombre_cola})

    def consumir(self, nombre_cola, callback):
        """
        Se inscribe en una cola y entra en un bucle infinito y bloqueante para procesar 
        continuamente los mensajes que el broker empuje (Push System).
        
        Implementa el reconocimiento (ACK): el broker recibe un ACK solo si el 'callback' 
        termina de ejecutarse sin lanzar excepciones. Esto garantiza que no se pierdan 
        mensajes por fallos de lógica de negocio (Message Durability).
        
        Args:
            nombre_cola (str): Identificador de la cola a escuchar.
            callback (callable): Función de primera clase que recibirá el mensaje 
                                 como su único argumento (ej. `def mi_func(msg):`).
        """
        # Se abre una conexión de red exclusiva y separada solo para consumir.
        # No se reutiliza la persistente para evitar bloqueos bidireccionales.
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            s.connect((self.host, self.port))
            # Envío de trama inicial de suscripción
            req = json.dumps({"action": "consume", "queue": nombre_cola}) + "\n"
            s.sendall(req.encode('utf-8'))
            logger.info(f"Suscripción activa en cola '{nombre_cola}'. Esperando push del broker.")
        except Exception as e:
            logger.error(f"Error fatal al iniciar la suscripción: {e}")
            return

        buffer = "" # Buffer temporal para lectura particionada desde la red TCP
        try:
            # Bucle infinito de recepción y procesamiento
            while True:
                data = s.recv(4096)
                if not data: 
                    # El servidor cerró la conexión por su lado
                    break
                
                buffer += data.decode('utf-8')
                
                # Permite procesar múltiples mensajes agrupados en una sola lectura de red
                while "\n" in buffer:
                    line, buffer = buffer.split("\n", 1)
                    if not line.strip(): continue
                        
                    try:
                        msg = json.loads(line)
                        if msg.get("action") == "message":
                            payload = msg.get("message")
                            
                            # Invocación sincrónica de la lógica de usuario (Lógica de negocio del cliente)
                            callback(payload)
                            
                            # Si el callback tuvo éxito, construimos y mandamos el ACK
                            ack = {
                                "action": "ack",
                                "queue": nombre_cola,
                                "msg_id": msg.get("msg_id")
                            }
                            # Confirmamos la correcta digestión del mensaje
                            s.sendall((json.dumps(ack) + "\n").encode('utf-8'))
                    except Exception as e:
                        # Si hay un error en el procesamiento, NO mandamos ACK.
                        # El servidor mantendrá el mensaje en unacked y lo rescatará 
                        # cuando este consumidor muera o se desconecte.
                        logger.error(f"Error en el callback al procesar mensaje: {e}")
        except KeyboardInterrupt:
            # Permite salida limpia con Ctrl+C del proceso consumidor
            logger.info("Bucle de consumo interrumpido voluntariamente por el usuario.")
        finally:
            # Cierre obligatorio del socket en cualquier caso (éxito, error, ctrl+c)
            s.close()
