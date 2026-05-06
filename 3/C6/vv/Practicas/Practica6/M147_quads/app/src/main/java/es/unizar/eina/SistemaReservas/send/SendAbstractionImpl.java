package es.unizar.eina.SistemaReservas.send;

import android.app.Activity;

/** 
 * Implementación concreta de la abstracción para el envío de mensajes.
 * 
 * Esta clase actúa como la parte refinada del patrón de diseño Bridge. 
 * Su responsabilidad es decidir, en tiempo de ejecución, qué canal de 
 * comunicación se utilizará (WhatsApp o SMS) basándose en el parámetro 
 * de método recibido, delegando la ejecución técnica a un objeto implementador.
 */
public class SendAbstractionImpl implements SendAbstraction {
	
	/** 
     * Referencia al objeto implementador encargado de realizar la 
     * comunicación técnica con la plataforma destino. 
     */
	private SendImplementor implementor;
	
	/** 
	 * Constructor de la clase. Inicializa el implementador correspondiente 
     * según el canal de envío solicitado.
     * 
	 * @param sourceActivity La actividad desde la cual se lanzará la acción de envío.
	 * @param method El canal seleccionado: "SMS" o "WHATSAPP" (por defecto).
	 */
	public SendAbstractionImpl(Activity sourceActivity, String method) {
        if (method.equalsIgnoreCase("SMS")) {
            implementor = new SMSImplementor(sourceActivity);
        } else {
            implementor = new WhatsAppImplementor(sourceActivity);
        }
	}

	/** 
     * Ejecuta la operación de envío de información.
     * Delega la responsabilidad al objeto implementador configurado en el constructor.
     * 
     * @param phone Número de teléfono al que se destina el mensaje.
     * @param message Contenido del mensaje de confirmación de reserva.
     */
	public void send(String phone, String message) {
		implementor.send(phone, message);
	}
}