"""
Módulo de Gestión de Almacenamiento Persistente.

Proporciona la capa de persistencia para el Broker de Mensajes. Se encarga de
serializar el estado de las colas en un sistema de archivos para garantizar
la durabilidad de los datos ante reinicios o fallos inesperados del sistema.
"""

import json
import os
import tempfile
import logging

# Configuración del logger para este módulo
logger = logging.getLogger(__name__)

class StorageManager:
    """
    Controlador de persistencia en disco mediante formato JSON.
    Asegura que las operaciones de guardado sean lo más atómicas posible.
    
    Atributos:
        file_path (str): Ruta al archivo físico de almacenamiento principal.
    """
    
    def __init__(self, file_path='broker_storage.json'):
        """
        Inicializa el gestor asignando la ruta del archivo de base de datos.
        
        Args:
            file_path (str): Nombre o ruta del archivo de persistencia de datos.
        """
        self.file_path = file_path

    def save_state(self, queues):
        """
        Serializa y guarda el estado actual de las colas en disco.
        
        Filtra los datos para guardar únicamente la información que requiere 
        durabilidad (los mensajes). Omite el estado volátil del sistema como 
        sockets activos, candados (locks) y listas de consumidores, los cuales 
        pierden su validez tras un reinicio.
        
        Para garantizar la integridad del archivo y evitar corrupciones en caso 
        de caída en medio de una escritura, se emplea una estrategia atómica: 
        se escribe en un archivo temporal y luego se renombra.
        
        Args:
            queues (dict): Estructura maestra de colas del Broker.
        """
        try:
            state = {}
            # Iteramos sobre el diccionario de colas para extraer solo lo persistible
            for q_name, q_data in queues.items():
                state[q_name] = {
                    "messages": q_data["messages"] # Únicamente los mensajes se guardan
                }
            
            # Obtiene el directorio base del archivo de destino
            dir_name = os.path.dirname(self.file_path) or '.'
            
            # Crea un archivo temporal en el mismo directorio. El atributo delete=False es crucial
            # para poder renombrarlo posteriormente en Windows sin conflictos de bloqueo.
            fd, temp_path = tempfile.mkstemp(dir=dir_name, prefix='broker_tmp_', suffix='.json')
            
            # Escribimos los datos en el archivo temporal
            with os.fdopen(fd, 'w') as f:
                json.dump(state, f, indent=4)
                # Forzamos la escritura física a disco para evitar buffers en memoria caché del SO
                f.flush()
                os.fsync(f.fileno())
                
            # Renombramiento atómico. Reemplaza el archivo antiguo por el nuevo de un solo golpe.
            # Esto previene que si hay un corte de energía, el archivo original quede medio escrito (corrupto).
            os.replace(temp_path, self.file_path)
            
        except Exception as e:
            # Registramos el error de escritura sin tumbar el broker, aunque implica riesgo de pérdida de datos
            logger.error(f"Fallo crítico en la persistencia de datos a disco: {e}")

    def load_state(self):
        """
        Recupera el estado previamente guardado desde el sistema de archivos físico.
        
        Returns:
            dict: Datos de las colas recuperados y reconstruidos. Retorna un 
                  diccionario vacío si el archivo no existe o está corrupto.
        """
        # Si no hay archivo previo (primer arranque del servidor), retorna estado vacío
        if not os.path.exists(self.file_path):
            return {}
        
        try:
            # Intenta abrir y parsear el archivo JSON
            with open(self.file_path, 'r') as f:
                return json.load(f)
        except json.JSONDecodeError:
            # Si el archivo existe pero no es un JSON válido (ej. corrupción), avisa y arranca limpio
            logger.error("Archivo de almacenamiento corrupto. Se inicializará un estado vacío.")
            return {}
        except Exception as e:
            # Captura errores de I/O de disco genéricos
            logger.error(f"Error inesperado al cargar el estado desde el disco: {e}")
            return {}
