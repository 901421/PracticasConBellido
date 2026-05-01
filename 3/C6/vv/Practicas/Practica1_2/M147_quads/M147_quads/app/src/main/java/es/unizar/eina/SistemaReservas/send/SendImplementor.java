package es.unizar.eina.SistemaReservas.send;

import android.app.Activity;

/** 
 * Interfaz que define la parte técnica del patrón de diseño Bridge para el envío de mensajes.
 * 
 * A diferencia de la abstracción, el implementador se encarga de las operaciones 
 * de bajo nivel específicas de cada plataforma de mensajería (SMS, WhatsApp, etc.), 
 * como la gestión del contexto de la actividad necesario para lanzar Intents de sistema.
 */
public interface SendImplementor {
	   
   /** 
    * Configura la actividad de origen necesaria para iniciar los procesos de envío externos.
    * 
    * @param source La instancia de {@link Activity} desde la cual se invocará el servicio.
    */
   public void setSourceActivity(Activity source);

   /** 
    * Recupera la actividad de origen configurada actualmente.
    * 
    * @return La instancia de {@link Activity} utilizada como contexto de ejecución.
    */
   public Activity getSourceActivity();

   /** 
    * Ejecuta la lógica técnica de envío de mensajes específica de cada plataforma.
    * 
    * @param phone El número de teléfono destino para la comunicación.
    * @param message El contenido del mensaje o cuerpo de la confirmación.
    */
   public void send (String phone, String message);

}