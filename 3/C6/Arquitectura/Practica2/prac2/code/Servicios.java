import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
    Clase Servicios para encapsular la lista de servicios disponibles en el servidor, incluyendo:
    - listaServicios: una lista de objetos InfoServicio que describen cada servicio
*/
public class Servicios implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<InfoServicio> listaServicios;

    // Constructor vacío que inicializa la lista de servicios
    public Servicios() {
        this.listaServicios = new ArrayList<>(); 
    }

    // Constructor que recibe una lista de servicios y la copia para evitar aliasing
    public Servicios(List<InfoServicio> lista) {
        this.listaServicios = new ArrayList<>(lista);
    }

    // Método para agregar un servicio a la lista
    public void addServicio(InfoServicio info) {
        this.listaServicios.add(info);
    }

    // Getter para obtener la lista de servicios
    public List<InfoServicio> getListaServicios() {
        return listaServicios;
    }
}