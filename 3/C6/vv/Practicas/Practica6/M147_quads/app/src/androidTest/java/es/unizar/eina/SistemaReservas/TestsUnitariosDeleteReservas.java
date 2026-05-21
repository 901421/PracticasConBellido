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

/**
 * Clase que implementa los tests unitarios para la operación de eliminación de reservas.
 * Valida el comportamiento del repositorio al intentar borrar reservas existentes,
 * inexistentes o nulas.
 * Nivel de prueba: Unitario (Capa de Repositorio).
 */
@RunWith(AndroidJUnit4.class)
public class TestsUnitariosDeleteReservas {

    private Quad quadBase;
    
    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule = new ActivityScenarioRule<>(SistemaReservas.class);

    /**
     * Configuración previa a cada test.
     * Inserta un Quad base necesario para poder crear reservas en los tests de eliminación.
     */
    @Before
    public void setUp() {
        scenarioRule.getScenario().onActivity(activity -> {
            // Setup / Given: Preparación de un Quad base
            QuadRepository quadRepo = activity.getQuadRepository();
            quadBase = new Quad("5555-DEL", true, 40.0, "Quad Base Delete Reserva");
            long idQ = quadRepo.insert(quadBase);
            assertThat("Setup: Error al insertar Quad", idQ, is(greaterThan(0L)));
            quadBase.setId((int) idQ);
        });
    }

    /**
     * Verifica la eliminación correcta de una reserva existente en la base de datos.
     */
    @Test
    public void testDeleteReservaValida() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository reservaRepo = activity.getReservaRepository();
            
            // Setup / Given: Insertar una Reserva de prueba vinculada al Quad base
            List<ReservaQuad> listaVehiculos = new ArrayList<>();
            listaVehiculos.add(new ReservaQuad(0, quadBase.getId(), 1, 0.0));
            
            Reserva reservaTest = new Reserva("Juan Borrado", 600111222, "2026-01-15", "2026-01-18");
            long idR = reservaRepo.insertSync(reservaTest, listaVehiculos);
            assertThat("Fallo previo: No se pudo insertar la reserva", idR, is(greaterThan(0L)));
            reservaTest.setId((int) idR);

            // Execution / When: Borramos la Reserva
            int filasBorradas = reservaRepo.delete(reservaTest);

            // Verification / Then: Comprobamos que se ha borrado correctamente
            assertThat("UNIT DELETE RES 1: Reserva válida no borrada", filasBorradas, is(greaterThan(0)));
        });
    }

    /**
     * Verifica el comportamiento al intentar eliminar una reserva que no existe en el sistema.
     */
    @Test
    public void testDeleteReservaInexistente() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository reservaRepo = activity.getReservaRepository();
            
            // Setup / Given: Creación de un objeto Reserva con un ID que no existe en la BD
            Reserva reservaFantasma = new Reserva("Fantasma", 0, "2026-01-01", "2026-01-02");
            reservaFantasma.setId(999999);

            // Execution / When: Intentamos borrarla
            int filasBorradas = reservaRepo.delete(reservaFantasma);

            // Verification / Then: Room devuelve 0 filas afectadas
            assertThat("UNIT DELETE RES 2: Borrar Reserva inexistente debe afectar a 0 filas", filasBorradas, is(0));
        });
    }

    /**
     * Verifica que el intento de borrar una reserva nula se gestiona de forma segura.
     */
    @Test
    public void testDeleteReservaNula() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository reservaRepo = activity.getReservaRepository();

            // Execution / When: Intento de borrar null
            int filasBorradas = reservaRepo.delete(null);

            // Verification / Then: Se gestiona de forma segura devolviendo 0 filas afectadas
            assertThat("UNIT DELETE RES 3: Al borrar reserva nula, Room afecta a 0 filas", filasBorradas, is(0));
        });
    }

    /**
     * Limpieza tras cada test, eliminando el Quad base creado.
     */
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
