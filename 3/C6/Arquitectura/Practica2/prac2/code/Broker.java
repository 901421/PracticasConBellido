import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfaz RMI del Broker. Todos los métodos deben lanzar RemoteException.
 */
public interface Broker extends Remote {
    
    // ==========================================================
    // API PARA LOS SERVIDORES
    // ==========================================================
    void registrar_servidor(String nombre_servidor, String host_remoto_IP_puerto) throws RemoteException;
    
    void alta_servicio(String nombre_servidor, String nom_servicio, List<Object> lista_param, String tipo_retorno) throws RemoteException;
    
    void baja_servicio(String nombre_servidor, String nom_servicio) throws RemoteException;

    // ==========================================================
    // API PARA LOS CLIENTES
    // ==========================================================
    Servicios lista_servicios() throws RemoteException;
    
    Respuesta ejecutar_servicio(String nom_servicio, List<Object> parametros_servicio) throws RemoteException;
    
    // ----- Nivel Asíncrono -----
    void ejecutar_servicio_asinc(String nom_servicio, List<Object> parametros_servicio) throws RemoteException;
    
    Respuesta obtener_respuesta_asinc(String nom_servicio) throws RemoteException;
}