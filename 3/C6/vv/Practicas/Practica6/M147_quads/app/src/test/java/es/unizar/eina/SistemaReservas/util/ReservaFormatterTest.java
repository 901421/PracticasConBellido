package es.unizar.eina.SistemaReservas.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test unitario para la lógica de formateo de mensajes de reserva (Cubre RF 11).
 * Verifica que el cuerpo del mensaje contiene los datos correctos del cliente, 
 * fechas, duración y precio, sin depender de la infraestructura de Android.
 */
public class ReservaFormatterTest {

    private Map<String, String> labels;

    @Before
    public void setUp() {
        labels = new HashMap<>();
        labels.put("header", "CONFIRMACIÓN DE RESERVA");
        labels.put("client", "Cliente: ");
        labels.put("contact", "Contacto: ");
        labels.put("in", "Recogida: ");
        labels.put("out", "Devolución: ");
        labels.put("duration", "Duración: ");
        labels.put("days", "días");
        labels.put("quads", "Nº Quads: ");
        labels.put("cascos", "Cascos: ");
        labels.put("price", "Precio Total: ");
    }

    @Test
    public void testFormatReservaValida() {
        String res = ReservaFormatter.formatReserva(
                "Juan Pérez",
                600111222L,
                "2026-05-20",
                "2026-05-22",
                2,
                1,
                120.50,
                labels
        );

        assertNotNull(res);
        assertThat(res, containsString("CONFIRMACIÓN DE RESERVA"));
        assertThat(res, containsString("Cliente: Juan Pérez"));
        assertThat(res, containsString("Contacto: 600111222"));
        assertThat(res, containsString("Recogida: 20/05/2026"));
        assertThat(res, containsString("Devolución: 22/05/2026"));
        assertThat(res, containsString("Duración: 2 días"));
        assertThat(res, containsString("Nº Quads: 2"));
        assertThat(res, containsString("Cascos: 1"));
        assertThat(res, containsString("Precio Total: 120,50€")); // Locale default depends on env, but typically , in Spanish
    }

    @Test
    public void testFormatReservaMismoDia() {
        // Reserva de un solo día (misma fecha in/out)
        String res = ReservaFormatter.formatReserva(
                "Ana",
                999888777L,
                "2026-06-01",
                "2026-06-01",
                1,
                0,
                45.0,
                labels
        );

        assertThat(res, containsString("Duración: 1 días")); // 1 día por defecto
        assertThat(res, containsString("Precio Total: 45,00€"));
    }
}
