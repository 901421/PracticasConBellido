package es.unizar.eina.SistemaReservas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.database.QuadRepository;
import es.unizar.eina.SistemaReservas.database.Reserva;
import es.unizar.eina.SistemaReservas.database.ReservaQuad;
import es.unizar.eina.SistemaReservas.database.ReservaRepository;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

@RunWith(AndroidJUnit4.class)
public class TestsUnitariosDeleteReservas {

    private Quad quadBase;
    
    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule = new ActivityScenarioRule<>(SistemaReservas.class);

    // Preparamos un Quad válido antes de cada test porque las reservas exigen tener vehículos
    @Before
    public void setUp() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository quadRepo = activity.getQuadRepository();
            quadBase = new Quad("5555-DEL", true, 40.0, "Quad Base Delete Reserva");
            long idQ = quadRepo.insert(quadBase);
            assertThat("Setup: Error al insertar Quad", idQ, is(greaterThan(0L)));
            quadBase.setId((int) idQ);
        });
    }

    @Test
    public void testDeleteReservaValida() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository reservaRepo = activity.getReservaRepository();
            
            // 1. Preparación: Insertar una Reserva de prueba vinculada al Quad base
            List<ReservaQuad> listaVehiculos = new ArrayList<>();
            listaVehiculos.add(new ReservaQuad(0, quadBase.getId(), 1, 0.0));
            
            Reserva reservaTest = new Reserva("Juan Borrado", 600111222, "2026-01-15", "2026-01-18");
            long idR = reservaRepo.insertSync(reservaTest, listaVehiculos);
            assertThat("Fallo previo: No se pudo insertar la reserva", idR, is(greaterThan(0L)));
            reservaTest.setId((int) idR);

            // 2. Ejecución: Borramos la Reserva
            int filasBorradas = reservaRepo.delete(reservaTest);

            // 3. Validación: Comprobamos que se ha borrado 1 fila
            assertThat("UNIT DELETE RES 1: Reserva válida no borrada", filasBorradas, is(greaterThan(0)));
        });
    }

    @Test
    public void testDeleteReservaInexistente() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository reservaRepo = activity.getReservaRepository();
            
            // 1. Preparación: Creamos una Reserva pero NO la insertamos
            Reserva reservaFantasma = new Reserva("Fantasma", 0, "2026-01-01", "2026-01-02");
            reservaFantasma.setId(999999); // ID falso que no está en base de datos

            // 2. Ejecución: Intentamos borrarla
            int filasBorradas = reservaRepo.delete(reservaFantasma);

            // 3. Validación: Devuelve 0 filas afectadas (Room no falla, solo ignora)
            assertThat("UNIT DELETE RES 2: Borrar Reserva inexistente debe afectar a 0 filas", filasBorradas, is(0));
        });
    }

    @Test
    public void testDeleteReservaNula() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository reservaRepo = activity.getReservaRepository();

            // Ejecución: Intentamos borrar null
            int filasBorradas = reservaRepo.delete(null);

            // Validación: Room gestiona el null de forma segura y devuelve 0 filas afectadas
            assertThat("UNIT DELETE RES 3: Al borrar reserva nula, Room afecta a 0 filas", filasBorradas, is(0));
        });
    }

    // Limpiamos la base de datos eliminando el Quad base
    @After
    public void tearDown() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository quadRepo = activity.getQuadRepository();
            if (quadBase != null && quadBase.getId() > 0) {
                quadRepo.delete(quadBase);
            }
        });
    }
}
