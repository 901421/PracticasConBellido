package es.unizar.eina.SistemaReservas.send;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

/**
 * Implementador concreto para el envío de mensajes a través de la plataforma WhatsApp.
 * 
 * Esta clase forma parte de la implementación del patrón Bridge. Se encarga de la 
 * normalización del número de teléfono, la verificación de la presencia de la aplicación 
 * en el dispositivo y la construcción de una URL compatible con la API oficial de WhatsApp 
 * para iniciar una conversación de forma automática.
 */
public class WhatsAppImplementor implements SendImplementor {

    /** Actividad de origen utilizada como contexto para operaciones de sistema y UI. */
    private Activity sourceActivity;

    /**
     * Constructor del implementador de WhatsApp.
     * @param sourceActivity Actividad de origen que servirá de contexto.
     */
    public WhatsAppImplementor(Activity sourceActivity) {
        this.sourceActivity = sourceActivity;
    }

    /** 
     * Actualiza la actividad de referencia para el proceso de envío.
     * @param source Nueva instancia de {@link Activity}.
     */
    public void setSourceActivity(Activity source) {
        sourceActivity = source;
    }

    /** 
     * Recupera la actividad de contexto configurada actualmente.
     * @return La actividad de origen.
     */
    public Activity getSourceActivity(){
        return sourceActivity;
    }

    /**
     * Ejecuta la lógica técnica para realizar el envío de información vía WhatsApp.
     * 
     * El proceso incluye:
     * 1. Limpieza de caracteres no numéricos del teléfono para evitar errores en la URL.
     * 2. Normalización del prefijo internacional (conversión de 00 a formato directo).
     * 3. Comprobación de seguridad para verificar si WhatsApp está instalado en el sistema.
     * 4. Codificación del mensaje en formato UTF-8 y lanzamiento de un {@link Intent} 
     *    dirigido específicamente al paquete de WhatsApp mediante la API "wa.me".
     * 
     * @param phone El número de teléfono de destino (admite formatos con símbolos que serán limpiados).
     * @param message El cuerpo del mensaje de confirmación de reserva.
     */
    @Override
    public void send(String phone, String message) {
        String cleanPhone = phone.replaceAll("\\D", ""); 

        if (cleanPhone.startsWith("00")) {
            cleanPhone = cleanPhone.substring(2);
        }

        boolean app_installed = false;
        try {
            sourceActivity.getPackageManager().getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }

        if (app_installed) {
            try {
                String url = "https://wa.me/" + cleanPhone + "?text=" + java.net.URLEncoder.encode(message, "UTF-8");
                
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.setPackage("com.whatsapp"); 
                
                sourceActivity.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(sourceActivity, "Error al formatear el mensaje", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(sourceActivity, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show();
        }
    }
}