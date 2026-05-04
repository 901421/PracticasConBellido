"""
Módulo de Gestión de Almacenamiento Persistente.

Proporciona la capa de persistencia para el Broker de Mensajes. Se encarga de
serializar el estado de las colas en un sistema de archivos para garantizar
la durabilidad de los datos ante reinicios o fallos inesperados del sistema.
Utiliza una estrategia de escritura atómica para prevenir la corrupción de datos.
"""

import json # Usado para serializar y deserializar estructuras de datos Python a texto
import os # Proporciona funciones de nivel del sistema operativo (renombrado atómico, fsync)
import tempfile # Utilizado para crear archivos temporales seguros donde escribir antes de reemplazar
import logging # Sistema de registro de eventos para depuración y auditoría

# Configuración del logger para seguimiento de operaciones de I/O en este módulo
logger = logging.getLogger(__name__)

class StorageManager:
    """
    Controlador de persistencia en disco mediante formato JSON.
    
    Asegura que las operaciones de guardado sean lo más atómicas posible utilizando
    archivos temporales y renombrado a nivel de sistema operativo.
    """
    
    def __init__(self, file_path='broker_storage.json'):
        """
        Inicializa el gestor asignando la ruta del archivo de base de datos.
        
        Args:
            file_path (str): Nombre o ruta del archivo físico de persistencia.
        """
        # Guardamos la ruta del archivo físico donde residirán los datos del broker
        self.file_path = file_path

    def save_state(self, queues):
        """
        Serializa y guarda el estado actual de las colas en disco de forma atómica.
        
        Filtra los datos para guardar únicamente la información que requiere 
        durabilidad (mensajes en espera y mensajes unacked). Omite el estado 
        volátil (sockets de red, locks de hilos).
        
        Args:
            queues (dict): Estructura maestra de colas del Broker que reside en memoria.
        """
        try:
            # Diccionario temporal que contendrá solo la información serializable
            state = {}
            
            # Iteramos sobre todas las colas existentes en el sistema
            for q_name, q_data in queues.items():
                # Extraemos mensajes en vuelo (unacked) para evitar pérdida de datos si el servidor cae
                # Usamos list() envolviendo el .values() para crear una copia en memoria y
                # evitar el error 'RuntimeError' si un hilo modifica el diccionario mientras iteramos.
                # La tupla original en memoria es (mensaje_dict, socket_obj, timestamp_float)
                # Extraemos solo 'm' (el mensaje_dict) ya que los sockets no se pueden guardar en disco.
                unacked_msgs = [m for m, c, t in list(q_data.get("unacked", {}).values())]
                
                # Construimos el estado a guardar para esta cola en particular
                state[q_name] = {
                    # La lista de mensajes pendientes por entregar (FIFO)
                    "messages": q_data["messages"], 
                    # La lista de mensajes que se han entregado pero aún no tienen ACK
                    "unacked": unacked_msgs         
                }
            
            # Obtenemos el directorio base donde se guardará el archivo (por defecto el actual)
            dir_name = os.path.dirname(self.file_path) or '.'
            
            # Estrategia de Guardado Atómico:
            # Creamos un archivo temporal manejado por el SO para evitar que, si hay un fallo
            # a mitad de escritura (corte de luz, etc.), el archivo original se corrompa.
            fd, temp_path = tempfile.mkstemp(dir=dir_name, prefix='broker_tmp_', suffix='.json')
            
            try:
                # Abrimos el archivo temporal usando el descriptor de archivo proporcionado (fd)
                with os.fdopen(fd, 'w') as f:
                    # Escribimos el diccionario 'state' en formato JSON, con indentación para legibilidad
                    json.dump(state, f, indent=4)
                    # Vaciamos los buffers internos de Python hacia el sistema operativo
                    f.flush()
                    # Forzamos al sistema operativo a que escriba físicamente los datos en el plato del disco
                    os.fsync(f.fileno()) 
                
                # Renombrado atómico: Reemplaza el archivo original por el nuevo archivo temporal.
                # En sistemas POSIX y Windows modernos, esta operación es atómica a nivel de inodo/tabla de archivos.
                # Si falla antes de esto, el archivo viejo sigue intacto.
                os.replace(temp_path, self.file_path)
            except Exception as e:
                # Si algo falla durante la escritura, intentamos limpiar la basura (el archivo temporal)
                if os.path.exists(temp_path):
                    os.remove(temp_path)
                # Relanzamos la excepción para que el log superior se entere
                raise e
            
        except Exception as e:
            # Si hay algún error catastrófico (disco lleno, permisos), lo registramos sin detener el servidor
            logger.error(f"Fallo crítico en la persistencia de datos: {e}")

    def load_state(self):
        """
        Recupera el estado persistente completo desde el disco.
        
        Es fundamental para el proceso de Bootstrap del servidor para recuperar
        los mensajes que no se llegaron a procesar antes del último apagado.
        
        Returns:
            dict: Datos de las colas recuperados. Retorna un diccionario vacío 
                  si el archivo no existe o es ilegible.
        """
        # Si es la primera vez que se ejecuta el broker y no hay archivo, devolvemos dict vacío
        if not os.path.exists(self.file_path):
            return {}
        
        try:
            # Abrimos el archivo principal en modo lectura
            with open(self.file_path, 'r') as f:
                # Parseamos el JSON y lo convertimos en un diccionario de Python
                return json.load(f)
        except json.JSONDecodeError:
            # Si el archivo está vacío o tiene JSON mal formado, asumimos que está corrupto.
            # Devolvemos un entorno limpio para que el servidor pueda arrancar sin fallar.
            logger.error("Archivo de almacenamiento corrupto. Se iniciará estado limpio.")
            return {}
        except Exception as e:
            # Captura de seguridad para problemas de I/O de bajo nivel (permisos, bloqueos)
            logger.error(f"Error inesperado al cargar el estado: {e}")
            return {}
