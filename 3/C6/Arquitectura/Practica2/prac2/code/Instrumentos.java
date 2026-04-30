import java.rmi.Remote;
import java.rmi.RemoteException;

// Interfaz del servidor que controla los cacharros científicos del Rover
public interface Instrumentos extends Remote {
    // Usamos String para los parámetros para que la Reflexión del Broker sea más fácil
    String analizar_muestra(String tipo_roca) throws RemoteException;

    // Usamos String para los parámetros para que la Reflexión del Broker sea más fácil
    String tomar_foto(String camara) throws RemoteException;
}