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

@RunWith(AndroidJUnit4.class)
public class PruebaDeVolumen {
    private static final String TAG = "VolumeTest";

    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    @Test
    @Ignore("No es una prueba de regresión continua")
    public void testVolumenQuadsYReservas() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository quadRepo = activity.getQuadRepository();
            ReservaRepository resRepo = activity.getReservaRepository();

            Log.d(TAG, "===== INICIANDO PRUEBA DE VOLUMEN (DATOS VÁLIDOS) =====");
            long startTime = System.currentTimeMillis();

            List<ReservaQuad> quadsParaVinculo = new ArrayList<>();

            for (int i = 1; i <= 100; i++) {
                String matriculaValida = String.format(Locale.getDefault(), "%04d-VOL", i);
                long idGenerado = quadRepo.insert(new Quad(matriculaValida, true, 20.0, "Volumen " + i));

                assertThat("Fallo insertando el Quad de volumen número " + i, idGenerado, is(greaterThan(0L)));

                if (quadsParaVinculo.isEmpty()) {
                    quadsParaVinculo.add(new ReservaQuad(0, (int) idGenerado, 1, 0.0));
                }
            }
            Log.d(TAG, "SUCCESS: 100 Quads insertados cumpliendo formato Regex.");

            assertThat("ERROR: Abortando volumen. No hay quads válidos para vincular.", quadsParaVinculo.isEmpty(), is(false));

            for (int i = 1; i <= 20000; i++) {
                Reserva r = new Reserva("Cliente Vol " + i, 600000000, "2026-01-01", "2026-01-02");

                long idReserva = resRepo.insertSync(r, quadsParaVinculo);
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

    @After
    public void tearDown() {
        scenarioRule.getScenario().onActivity(activity -> {
            activity.getReservaRepository().deleteAll();
            activity.getQuadRepository().deleteAll();
        });
    }
}
