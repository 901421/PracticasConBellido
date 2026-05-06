package es.unizar.eina.SistemaReservas;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertTrue;
import org.junit.After;
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

    private QuadRepository getQuadRepository() {
        final QuadRepository[] repo = new QuadRepository[1];
        scenarioRule.getScenario().onActivity(activity -> repo[0] = activity.getQuadRepository());
        return repo[0];
    }

    private ReservaRepository getReservaRepository() {
        final ReservaRepository[] repo = new ReservaRepository[1];
        scenarioRule.getScenario().onActivity(activity -> repo[0] = activity.getReservaRepository());
        return repo[0];
    }

    @Test
    public void testVolumenQuadsYReservas() {
        QuadRepository quadRepo = getQuadRepository();
        ReservaRepository resRepo = getReservaRepository();

        Log.d(TAG, "===== INICIANDO PRUEBA DE VOLUMEN (DATOS VÁLIDOS) =====");
        long startTime = System.currentTimeMillis();

        List<ReservaQuad> quadsParaVinculo = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            String matriculaValida = String.format(Locale.getDefault(), "%04d-VOL", i);
            long idGenerado = quadRepo.insert(new Quad(matriculaValida, true, 20.0, "Volumen " + i));

            assertTrue("Fallo insertando el Quad de volumen número " + i, idGenerado > 0);

            if (quadsParaVinculo.isEmpty()) {
                quadsParaVinculo.add(new ReservaQuad(0, (int) idGenerado, 1, 0.0));
            }
        }
        Log.d(TAG, "SUCCESS: 100 Quads insertados cumpliendo formato Regex.");

        assertTrue("ERROR: Abortando volumen. No hay quads válidos para vincular.", !quadsParaVinculo.isEmpty());

        for (int i = 1; i <= 20000; i++) {
            Reserva r = new Reserva("Cliente Vol " + i, 600000000, "2026-01-01", "2026-01-02");

            long idReserva = resRepo.insertSync(r, quadsParaVinculo);
            assertTrue("Fallo insertando la reserva número " + i, idReserva > 0);

            if (i % 1000 == 0) {
                Log.d(TAG, "Progreso: " + i + "/20000 reservas insertadas...");
            }
        }

        long endTime = System.currentTimeMillis();
        double totalSeconds = (endTime - startTime) / 1000.0;
        Log.d(TAG, "===== ÉXITO VOLUMEN: 20.100 registros en " + totalSeconds + " segundos =====");
    }

    @After
    public void tearDown() {
        getReservaRepository().deleteAll();
        getQuadRepository().deleteAll();
    }
}