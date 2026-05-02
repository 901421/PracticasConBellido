package es.unizar.eina.SistemaReservas.send;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Implementador concreto para el envío de mensajes a través del servicio de SMS nativo.
 * 
 * Esta clase forma parte de la implementación del patrón Bridge. Se encarga de 
 * configurar y lanzar un {@link Intent} de sistema para abrir la aplicación de 
 * mensajería predeterminada del dispositivo con el destinatario y el cuerpo 
 * del mensaje pre-cargados.
 */
public class SMSImplementor implements SendImplementor {

    /** Actividad desde la cual se lanzará el Intent de envío. */
    private Activity sourceActivity;

    /**
     * Constructor del implementador de SMS.
     * @param sourceActivity Actividad de origen que servirá de contexto.
     */
    public SMSImplementor(Activity sourceActivity) {
        this.sourceActivity = sourceActivity;
    }

    /** 
     * Actualiza la actividad de referencia para el envío del SMS.
     * @param source Nueva actividad de contexto.
     */
    public void setSourceActivity(Activity source) {
        sourceActivity = source;
    }

    /** 
     * Recupera la actividad configurada para el envío.
     * @return La actividad de origen actual.
     */
    public Activity getSourceActivity(){
        return sourceActivity;
    }

    /**
     * Ejecuta la lógica técnica para realizar el envío de un SMS.
     * Crea un Intent con la acción ACTION_VIEW y el esquema "sms:" para 
     * delegar la comunicación al sistema operativo.
     * 
     * @param phone El número de teléfono de destino.
     * @param message El cuerpo del mensaje de confirmación.
     */
    @Override
    public void send(String phone, String message) {
        Uri smsUri = Uri.parse("sms:" + phone);
        Intent sendIntent = new Intent(Intent.ACTION_VIEW, smsUri);
        sendIntent.putExtra("sms_body", message);
        getSourceActivity().startActivity(sendIntent);
    }
}