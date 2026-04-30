import java.io.Serializable;

/*
    Clase Respuesta para encapsular el resultado de una operación, incluyendo:
    - valorRetorno: el resultado de la operación (si es exitosa)
    - mensajeError: un mensaje de error (si ocurre un error)
    - exito: indica si la operación fue exitosa
    - procesando: indica si la operación aún está en proceso
*/
public class Respuesta implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Object valorRetorno;
    private String mensajeError;
    private boolean exito;
    private boolean procesando;

    // Constructor privado
    private Respuesta(Object valorRetorno, String mensajeError, boolean exito, boolean procesando) {
        this.valorRetorno = valorRetorno;
        this.mensajeError = mensajeError;
        this.exito = exito;
        this.procesando = procesando;
    }

    // Métodos estáticos para crear respuestas

    // Respuesta de éxito con valor de retorno
    public static Respuesta exito(Object valorRetorno) {
        return new Respuesta(valorRetorno, null, true, false);
    }

    // Respuesta de error con mensaje de error
    public static Respuesta error(String mensajeError) {
        return new Respuesta(null, mensajeError, false, false);
    }

    // Respuesta de proceso en curso
    public static Respuesta enProceso() {
        return new Respuesta(null, "PACIENCIA: El rover sigue haciendo cosas...", false, true);
    }

    // Getters
    public Object getValorRetorno() { return valorRetorno; }
    public String getMensajeError() { return mensajeError; }
    public boolean isExito() { return exito; }    
    public boolean isProcesando() { return procesando; }
}