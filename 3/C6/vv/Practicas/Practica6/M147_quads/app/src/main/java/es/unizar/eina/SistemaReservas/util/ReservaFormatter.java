package es.unizar.eina.SistemaReservas.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Clase de utilidad para el formateo de mensajes de confirmación de reserva.
 * Permite centralizar la lógica de negocio del texto que se envía al cliente
 * para facilitar su testeo unitario.
 */
public class ReservaFormatter {

    /**
     * Construye el cuerpo del mensaje de confirmación.
     * 
     * @param clientNombre Nombre del cliente.
     * @param telefono Número de contacto.
     * @param fechaIn Fecha de recogida (yyyy-MM-dd).
     * @param fechaOut Fecha de devolución (yyyy-MM-dd).
     * @param numQuads Cantidad de quads reservados.
     * @param totalCascos Cantidad total de cascos.
     * @param precioFinal Precio total calculado.
     * @param labels Mapa de etiquetas de idioma (para mantener la independencia de Android Context).
     * @return String con el mensaje formateado.
     */
    public static String formatReserva(
            String clientNombre,
            long telefono,
            String fechaIn,
            String fechaOut,
            int numQuads,
            int totalCascos,
            double precioFinal,
            java.util.Map<String, String> labels
    ) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat uiSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String displayIn = fechaIn;
        String displayOut = fechaOut;
        long dias = 1;

        try {
            Date in = dbSdf.parse(fechaIn);
            Date out = dbSdf.parse(fechaOut);
            if (in != null && out != null) {
                long diff = out.getTime() - in.getTime();
                dias = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                displayIn = uiSdf.format(in);
                displayOut = uiSdf.format(out);
            }
            if (dias < 1) dias = 1;
        } catch (Exception e) {
            // Logged in Activity
        }

        sb.append(getLabel(labels, "header")).append("\n\n");
        sb.append(getLabel(labels, "client")).append(clientNombre).append("\n");
        sb.append(getLabel(labels, "contact")).append(telefono).append("\n\n");
        sb.append(getLabel(labels, "in")).append(displayIn).append("\n");
        sb.append(getLabel(labels, "out")).append(displayOut).append("\n");
        sb.append(getLabel(labels, "duration")).append(dias).append(" ").append(getLabel(labels, "days")).append("\n\n");
        sb.append(getLabel(labels, "quads")).append(numQuads).append("\n");
        sb.append(getLabel(labels, "cascos")).append(totalCascos).append("\n\n");
        sb.append(getLabel(labels, "price")).append(String.format(Locale.getDefault(), "%.2f", precioFinal)).append("€");

        return sb.toString();
    }

    private static String getLabel(java.util.Map<String, String> labels, String key) {
        if (labels != null && labels.containsKey(key)) {
            return labels.get(key);
        }
        return "";
    }
}
