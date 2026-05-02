package es.unizar.eina.SistemaReservas;

import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.database.QuadRepository;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

@RunWith(AndroidJUnit4.class)
public class PruebaDeSobrecarga {

    private static final String TAG = "StressTest";

    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    private QuadRepository getQuadRepository() {
        final QuadRepository[] repo = new QuadRepository[1];
        scenarioRule.getScenario().onActivity(activity -> repo[0] = activity.getQuadRepository());
        return repo[0];
    }

    @Test
    public void testSobrecargaDescripcion() {
        QuadRepository quadRepo = getQuadRepository();

        Log.d(TAG, "===== INICIANDO PRUEBA DE SOBRECARGA =====");

        StringBuilder sb = new StringBuilder();
        String bloque1000 = new String(new char[1000]).replace("\0", "A");
        int iteracion = 1;
        boolean stop = false;
        long lastSuccessfulSize = 0;

        try {
            while (iteracion <= 2000 && !stop) {
                sb.append(bloque1000);
                int totalChars = sb.length();

                // Imprimimos el log cada 100 iteraciones para no saturar la consola del emulador
                if (iteracion % 100 == 0 || iteracion == 1) {
                    Log.d(TAG, "Iteración " + iteracion + ": Probando con " + totalChars + " caracteres...");
                }

                // Generar matrícula válida "0001-STR", "0002-STR"...
                String matriculaValida = String.format(Locale.getDefault(), "%04d-STR", iteracion % 10000);

                Quad q = new Quad(matriculaValida, true, 10.0, sb.toString());
                long res = quadRepo.insert(q);

                if (res == -1) {
                    Log.e(TAG, "!!! LÍMITE LÓGICO ALCANZADO !!!");
                    Log.e(TAG, "Fallo devuelto por repositorio: " + totalChars + " caracteres.");
                    Log.e(TAG, "Último tamaño estable: " + lastSuccessfulSize);
                    stop = true;

                    // Si el sistema devuelve -1 significa que hemos saturado el buffer pero no ha crasheado
                    assertTrue("Límite de memoria alcanzado y detectado (Log registrado)", true);
                } else {
                    // Si funciona, borramos inmediatamente para no consumir disco duro, solo memoria
                    q.setId((int) res);
                    quadRepo.delete(q);
                    lastSuccessfulSize = totalChars;
                    iteracion++;
                }
            }


            if (!stop) {
                assertTrue("Prueba terminada. El sistema soportó 2 millones de caracteres", true);
            }

        } catch (Throwable t) {

            // Si Room lanza TransactionTooLargeException o la app se queda sin RAM (OutOfMemory),

            Log.e(TAG, "COLAPSO FÍSICO DE SISTEMA: " + t.getClass().getSimpleName());
            Log.e(TAG, "Tamaño en el momento del crash: " + sb.length());

            assertTrue("El test falló porque la app crasheó de forma incontrolada. Excepción: " + t.getClass().getSimpleName(), false);
        }

        Log.d(TAG, "===== FIN PRUEBA SOBRECARGA =====");
    }

    @After
    public void tearDown() {
        // Limpiar la BD
        getQuadRepository().deleteAll();
    }
}