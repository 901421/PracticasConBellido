import java.io.Serializable;
import java.util.List;

/**
 * Encapsula la información de un servicio registrado en el Broker.
 * Debe ser Serializable para viajar por la red.
 */
public class InfoServicio implements Serializable {
    private static final long serialVersionUID = 1L; 
    
    private String nombreServidor;
    private String nombreServicio;
    private List<Object> listaParametros; // Tipos o nombres de los parámetros esperados
    private String tipoRetorno;

    // Constructor completo
    public InfoServicio(String nombreServidor, String nombreServicio, List<Object> listaParametros, String tipoRetorno) {
        this.nombreServidor = nombreServidor;
        this.nombreServicio = nombreServicio;
        this.listaParametros = listaParametros;
        this.tipoRetorno = tipoRetorno;
    }

    // Getters
    public String getNombreServidor() { return nombreServidor; }
    public String getNombreServicio() { return nombreServicio; }
    public List<Object> getListaParametros() { return listaParametros; }
    public String getTipoRetorno() { return tipoRetorno; }
    
    // toString para facilitar la depuración y visualización de la información del servicio
    @Override
    public String toString() {
        return nombreServicio + " (Params: " + listaParametros + ") -> " + tipoRetorno + " [Servidor: " + nombreServidor + "]";
    }
}