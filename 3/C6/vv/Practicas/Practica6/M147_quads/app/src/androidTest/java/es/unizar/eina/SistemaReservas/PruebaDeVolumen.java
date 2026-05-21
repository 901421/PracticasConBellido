package es.unizar.eina.SistemaReservas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.database.QuadRepository;
import es.unizar.eina.SistemaReservas.database.Reserva;
import es.unizar.eina.SistemaReservas.database.ReservaQuad;
import es.unizar.eina.SistemaReservas.database.ReservaRepository;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

/**
 * Clase que implementa las pruebas de volumen para el sistema de reservas.
 * Esta clase valida que el sistema es capaz de manejar una carga masiva de datos (Quads y Reservas)
 * manteniendo la integridad y un rendimiento aceptable.
 */
@RunWith(AndroidJUnit4.class)
public class PruebaDeVolumen {
    private static final String TAG = "VolumeTest";

    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    /**
     * Verifica la capacidad del sistema para insertar un gran volumen de datos:
     * 100 Quads y 20,000 Reservas vinculadas a dichos Quads.
     * Valida que el rendimiento sea adecuado y que no ocurran errores de desbordamiento o agotamiento de recursos.
     */
    @Test
    @Ignore("No es una prueba de regresión continua")
    public void testVolumenQuadsYReservas() {
        scenarioRule.getScenario().onActivity(activity -> {
            // Setup / Given: Obtención de repositorios y preparación de estructuras
            QuadRepository quadRepo = activity.getQuadRepository();
            ReservaRepository resRepo = activity.getReservaRepository();

            Log.d(TAG, "===== INICIANDO PRUEBA DE VOLUMEN (DATOS VÁLIDOS) =====");
            long startTime = System.currentTimeMillis();

            List<ReservaQuad> quadsParaVinculo = new ArrayList<>();

            // Execution / When: Inserción masiva de 100 Quads
            for (int i = 1; i <= 100; i++) {
                String matriculaValida = String.format(Locale.getDefault(), "%04d-VOL", i);
                long idGenerado = quadRepo.insert(new Quad(matriculaValida, true, 20.0, "Volumen " + i));

                // Verification / Then: Verificar que cada Quad se insertó correctamente
                assertThat("Fallo insertando el Quad de volumen número " + i, idGenerado, is(greaterThan(0L)));

                if (quadsParaVinculo.isEmpty()) {
                    quadsParaVinculo.add(new ReservaQuad(0, (int) idGenerado, 1, 0.0));
                }
            }
            Log.d(TAG, "SUCCESS: 100 Quads insertados cumpliendo formato Regex.");

            assertThat("ERROR: Abortando volumen. No hay quads válidos para vincular.", quadsParaVinculo.isEmpty(), is(false));

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(2026, java.util.Calendar.JANUARY, 1);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // Execution / When: Inserción masiva de 20,000 Reservas
            for (int i = 1; i <= 20000; i++) {
                // Generar fechas sin solape avanzando de 2 en 2 días
                String fechaIn = sdf.format(cal.getTime());
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
                String fechaOut = sdf.format(cal.getTime());
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1); // Separación con la siguiente reserva

                Reserva r = new Reserva("Cliente Vol " + i, 600000000, fechaIn, fechaOut);

                long idReserva = resRepo.insertMassiveSync(r, quadsParaVinculo);
                
                // Verification / Then: Verificar que cada reserva se insertó correctamente
                assertThat("Fallo insertando la reserva número " + i, idReserva, is(greaterThan(0L)));

                if (i % 1000 == 0) {
                    Log.d(TAG, "Progreso: " + i + "/20000 reservas insertadas...");
                }
            }

            long endTime = System.currentTimeMillis();
            double totalSeconds = (endTime - startTime) / 1000.0;
            Log.d(TAG, "===== ÉXITO VOLUMEN: 20.100 registros en " + totalSeconds + " segundos =====");
        });
    }

    /**
     * Limpieza de la base de datos después de la ejecución de la prueba de volumen.
     * Elimina todos los registros de reservas y quads creados.
     */
    @After
    public void tearDown() {
        scenarioRule.getScenario().onActivity(activity -> {
            activity.getReservaRepository().deleteAll();
            activity.getQuadRepository().deleteAll();
        });
    }
}
