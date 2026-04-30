import java.rmi.Remote;
import java.rmi.RemoteException;

// Interfaz del servidor que controla el movimiento del Rover
public interface Navegacion extends Remote {
    // Usamos Integer en vez de int para que la Reflexión del Broker sea más fácil
    String calcular_ruta(Integer coordenadaX, Integer coordenadaY) throws RemoteException;

    // Usamos Integer en vez de int para que la Reflexión del Broker sea más fácil
    String mover_rover(Integer metros) throws RemoteException;
}