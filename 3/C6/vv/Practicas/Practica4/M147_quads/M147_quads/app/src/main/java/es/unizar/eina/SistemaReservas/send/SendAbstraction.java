package es.unizar.eina.SistemaReservas.send;

/** 
 * Interfaz que define la abstracción para el envío de mensajes dentro del sistema.
 * 
 * Forma parte de la implementación del patrón de diseño Bridge, permitiendo desacoplar 
 * la lógica de negocio (el qué se envía) de la implementación técnica de los 
 * canales de comunicación (el cómo se envía).
 */
public interface SendAbstraction {

    /** 
     * Define la firma del método para realizar el envío de un mensaje de texto.
     * 
     * @param phone El número de teléfono de destino al que se enviará la notificación.
     * @param message El cuerpo del mensaje o información de la reserva.
     */
    public void send(String phone, String message);
}