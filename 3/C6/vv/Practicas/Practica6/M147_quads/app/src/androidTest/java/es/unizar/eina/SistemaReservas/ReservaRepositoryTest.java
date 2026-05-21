package es.unizar.eina.SistemaReservas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.database.QuadRepository;
import es.unizar.eina.SistemaReservas.database.Reserva;
import es.unizar.eina.SistemaReservas.database.ReservaConQuads;
import es.unizar.eina.SistemaReservas.database.ReservaQuad;
import es.unizar.eina.SistemaReservas.database.ReservaRepository;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

/**
 * Tests de integración para el filtrado de reservas (Cubre RF 14 y CP-V-07 a 10).
 * Valida que el repositorio filtra correctamente las reservas por estado
 * (Previstas, Vigentes, Caducadas) basándose en la fecha actual.
 */
@RunWith(AndroidJUnit4.class)
public class ReservaRepositoryTest {

    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    private ReservaRepository reservaRepo;
    private QuadRepository quadRepo;
    private int idQuad;

    @Before
    public void setUp() {
        scenarioRule.getScenario().onActivity(activity -> {
            reservaRepo = activity.getReservaRepository();
            quadRepo = activity.getQuadRepository();
            reservaRepo.deleteAll();
            quadRepo.deleteAll();

            // Insertamos un quad para las pruebas
            idQuad = (int) quadRepo.insert(new Quad("1234-TES", true, 50.0, "Test"));
        });
    }

    @Test
    public void testFiltradoPorEstados() throws InterruptedException {
        scenarioRule.getScenario().onActivity(activity -> {
            // 1. Crear set de datos: Una en cada estado (suponiendo hoy es 2026-05-15)
            String today = "2026-05-15";
            
            // Prevista (In > today)
            insertarReservaHelper("Prevista", "2026-06-01", "2026-06-05");
            // Vigente (In <= today <= Out)
            insertarReservaHelper("Vigente", "2026-05-10", "2026-05-20");
            // Caducada (Out < today)
            insertarReservaHelper("Caducada", "2026-05-01", "2026-05-05");
        });

        // Esperar a que Room procese
        Thread.sleep(1000);

        String today = "2026-05-15";

        // Probar Filtro: PREVISTAS (1)
        validarFiltro(1, today, 1, "Prevista");

        // Probar Filtro: VIGENTES (2)
        validarFiltro(2, today, 1, "Vigente");

        // Probar Filtro: CADUCADAS (3)
        validarFiltro(3, today, 1, "Caducada");

        // Probar Filtro: TODAS (0)
        validarFiltro(0, today, 3, null);
    }

    private void insertarReservaHelper(String cliente, String fIn, String fOut) {
        Reserva r = new Reserva(cliente, 600000000L, fIn, fOut);
        List<ReservaQuad> rqs = new ArrayList<>();
        rqs.add(new ReservaQuad(0, idQuad, 1, 50.0));
        reservaRepo.insertSync(r, rqs);
    }

    private void validarFiltro(int filter, String today, int expectedCount, String expectedName) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        scenarioRule.getScenario().onActivity(activity -> {
            LiveData<List<ReservaConQuads>> liveData = reservaRepo.getFilteredReservas(filter, today, "CLIENTE", "ASC");
            liveData.observeForever(new Observer<List<ReservaConQuads>>() {
                @Override
                public void onChanged(List<ReservaConQuads> reservas) {
                    if (reservas != null && reservas.size() >= 0) {
                        assertThat("Filtro " + filter + " - Conteo incorrecto", reservas.size(), is(expectedCount));
                        if (expectedName != null && !reservas.isEmpty()) {
                            assertThat("Filtro " + filter + " - Nombre incorrecto", reservas.get(0).reserva.getNombreCliente(), is(expectedName));
                        }
                        liveData.removeObserver(this);
                        latch.countDown();
                    }
                }
            });
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        scenarioRule.getScenario().onActivity(activity -> {
            reservaRepo.deleteAll();
            quadRepo.deleteAll();
        });
    }
}
