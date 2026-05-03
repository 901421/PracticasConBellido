"""
Módulo de Gestión de Almacenamiento Persistente.

Proporciona la capa de persistencia para el Broker de Mensajes. Se encarga de
serializar el estado de las colas en un sistema de archivos para garantizar
la durabilidad de los datos ante reinicios o fallos inesperados del sistema.
Utiliza una estrategia de escritura atómica para prevenir la corrupción de datos.
"""

import json
import os
import tempfile
import logging

# Configuración del logger para seguimiento de operaciones de I/O
logger = logging.getLogger(__name__)

class StorageManager:
    """
    Controlador de persistencia en disco mediante formato JSON.
    
    Asegura que las operaciones de guardado sean lo más atómicas posible utilizando
    archivos temporales y renombrado a nivel de sistema operativo.
    
    Attributes:
        file_path (str): Ruta al archivo físico de almacenamiento principal.
    """
    
    def __init__(self, file_path='broker_storage.json'):
        """
        Inicializa el gestor asignando la ruta del archivo de base de datos.
        
        Args:
            file_path (str): Nombre o ruta del archivo de persistencia.
        """
        self.file_path = file_path

    def save_state(self, queues):
        """
        Serializa y guarda el estado actual de las colas en disco de forma atómica.
        
        Filtra los datos para guardar únicamente la información que requiere 
        durabilidad (mensajes en espera y mensajes unacked). Omite el estado 
        volátil (sockets, locks).
        
        Estrategia Atómica:
            1. Crea un archivo temporal.
            2. Escribe los datos y fuerza el volcado físico (fsync).
            3. Reemplaza el archivo original mediante os.replace (atómico en POSIX/Win32).
        
        Args:
            queues (dict): Estructura maestra de colas del Broker.
        """
        try:
            state = {}
            # Iteramos sobre el diccionario de colas para extraer solo lo persistible
            for q_name, q_data in queues.items():
                # Extraemos mensajes en vuelo (unacked) para evitar el "Agujero Negro" de datos
                # La tupla en memoria es: (mensaje_dict, socket_obj, timestamp_float)
                unacked_msgs = [m for m, c, t in q_data.get("unacked", {}).values()]
                
                state[q_name] = {
                    "messages": q_data["messages"], # Mensajes en cola FIFO
                    "unacked": unacked_msgs         # Mensajes despachados sin confirmar
                }
            
            # Obtiene el directorio base para el archivo temporal
            dir_name = os.path.dirname(self.file_path) or '.'
            
            # Creación de archivo temporal para garantizar atomicidad
            fd, temp_path = tempfile.mkstemp(dir=dir_name, prefix='broker_tmp_', suffix='.json')
            
            try:
                with os.fdopen(fd, 'w') as f:
                    json.dump(state, f, indent=4)
                    f.flush()
                    os.fsync(f.fileno()) # Asegura que los datos toquen el plato del disco
                
                # Renombrado atómico: si el sistema falla aquí, el original sigue intacto
                os.replace(temp_path, self.file_path)
            except Exception as e:
                # Limpieza de archivo temporal si falla la escritura
                if os.path.exists(temp_path):
                    os.remove(temp_path)
                raise e
            
        except Exception as e:
            logger.error(f"Fallo crítico en la persistencia de datos: {e}")

    def load_state(self):
        """
        Recupera el estado persistente completo desde el disco.
        
        Es fundamental para el proceso de Bootstrap del servidor para evitar 
        la pérdida de colas inactivas (Data Wipe fix).
        
        Returns:
            dict: Datos de las colas recuperados. Retorna un diccionario vacío 
                  si el archivo no existe o es ilegible.
        """
        if not os.path.exists(self.file_path):
            return {}
        
        try:
            with open(self.file_path, 'r') as f:
                return json.load(f)
        except json.JSONDecodeError:
            logger.error("Archivo de almacenamiento corrupto. Se iniciará estado limpio.")
            return {}
        except Exception as e:
            logger.error(f"Error inesperado al cargar el estado: {e}")
            return {}
