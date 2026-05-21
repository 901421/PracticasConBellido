package es.unizar.eina.SistemaReservas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

/**
 * Clase que implementa las pruebas de sobrecarga (stress testing) para el sistema.
 * Evalúa el comportamiento del sistema cuando se introducen datos que exceden los límites normales
 * de operación, específicamente la longitud de la descripción de un Quad.
 */
@RunWith(AndroidJUnit4.class)
public class PruebaDeSobrecarga {

    private static final String TAG = "StressTest";

    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    /**
     * Prueba el límite de capacidad de almacenamiento para el campo descripción de un Quad.
     * Inserta descripciones de tamaño incremental (bloques de 1000 caracteres) hasta alcanzar el límite
     * del sistema o completar 2 millones de caracteres.
     * Valida si el sistema maneja el error de forma controlada o si ocurre un fallo crítico.
     */
    @Test
    @Ignore("No es una prueba de regresión continua")
    public void testSobrecargaDescripcion() {
        scenarioRule.getScenario().onActivity(activity -> {
            // Setup / Given: Preparación del escenario de sobrecarga
            Log.d(TAG, "===== INICIANDO PRUEBA DE SOBRECARGA =====");

            StringBuilder sb = new StringBuilder();
            String bloque1000 = new String(new char[1000]).replace("\0", "A");
            int iteracion = 1;
            boolean stop = false;
            long lastSuccessfulSize = 0;

            try {
                // Execution / When: Inserción incremental de datos masivos en un campo
                while (iteracion <= 2000 && !stop) {
                    sb.append(bloque1000);
                    int totalChars = sb.length();

                    if (iteracion % 100 == 0 || iteracion == 1) {
                        Log.d(TAG, "Iteración " + iteracion + ": Probando con " + totalChars + " caracteres...");
                    }

                    String matriculaValida = String.format(Locale.getDefault(), "%04d-STR", iteracion % 10000);

                    Quad q = new Quad(matriculaValida, true, 10.0, sb.toString());
                    long res = activity.getQuadRepository().insert(q);

                    // Verification / Then: Análisis de la respuesta del sistema ante la sobrecarga
                    if (res == -1) {
                        Log.e(TAG, "!!! LÍMITE LÓGICO ALCANZADO !!!");
                        Log.e(TAG, "Fallo devuelto por repositorio: " + totalChars + " caracteres.");
                        Log.e(TAG, "Último tamaño estable: " + lastSuccessfulSize);
                        stop = true;

                        assertThat("Límite de memoria alcanzado y detectado (Log registrado)", true, is(true));
                    } else {
                        q.setId((int) res);
                        activity.getQuadRepository().delete(q);
                        lastSuccessfulSize = totalChars;
                        iteracion++;
                    }
                }

                if (!stop) {
                    assertThat("Prueba terminada. El sistema soportó 2 millones de caracteres", true, is(true));
                }

            } catch (Throwable t) {
                // Verification / Then: Gestión de colapso físico si ocurre una excepción incontrolada
                Log.e(TAG, "COLAPSO FÍSICO DE SISTEMA: " + t.getClass().getSimpleName());
                Log.e(TAG, "Tamaño en el momento del crash: " + sb.length());
                assertThat("El test falló porque la app crasheó de forma incontrolada. Excepción: " + t.getClass().getSimpleName(), false, is(true));
            }

            Log.d(TAG, "===== FIN PRUEBA SOBRECARGA =====");
        });
    }

    /**
     * Limpieza de la base de datos tras la prueba de sobrecarga.
     */
    @After
    public void tearDown() {
        scenarioRule.getScenario().onActivity(activity -> {
            activity.getQuadRepository().deleteAll();
        });
    }
}