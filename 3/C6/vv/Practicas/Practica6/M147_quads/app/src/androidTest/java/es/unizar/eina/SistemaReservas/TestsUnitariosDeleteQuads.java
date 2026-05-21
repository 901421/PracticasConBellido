package es.unizar.eina.SistemaReservas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
 * Clase que implementa los tests unitarios para la operación de eliminación de Quads.
 * Valida la correcta eliminación de registros, la gestión de casos inexistentes o nulos,
 * y el comportamiento del borrado en cascada con las reservas vinculadas.
 * Nivel de prueba: Unitario (Capa de Repositorio).
 */
@RunWith(AndroidJUnit4.class)
public class TestsUnitariosDeleteQuads {

    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule = new ActivityScenarioRule<>(SistemaReservas.class);

    /**
     * Verifica la eliminación exitosa de un Quad existente en la base de datos.
     */
    @Test
    public void testDeleteQuadValido() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            
            // Setup / Given: Preparación e inserción de un Quad de prueba
            Quad quadTest = new Quad("7777-DEL", true, 30.0, "Para borrar");
            long id = repository.insert(quadTest);
            assertThat("Fallo previo: No se pudo insertar el Quad", id, is(greaterThan(0L)));
            quadTest.setId((int) id);

            // Execution / When: Borramos el Quad
            int filasBorradas = repository.delete(quadTest);

            // Verification / Then: Comprobamos que se ha borrado 1 fila y no es recuperable
            assertThat("UNIT DELETE 1: Quad válido no borrado", filasBorradas, is(greaterThan(0)));
            
            Quad quadComprobacion = repository.getQuadById((int) id);
            assertThat("UNIT DELETE 1: El Quad sigue existiendo en BD", quadComprobacion, is(nullValue()));
        });
    }

    /**
     * Verifica que el intento de borrar un Quad inexistente se maneja correctamente.
     */
    @Test
    public void testDeleteQuadInexistente() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            
            // Setup / Given: Objeto Quad con un ID ficticio
            Quad quadFantasma = new Quad("9999-GHO", true, 30.0, "No existe");
            quadFantasma.setId(999999);

            // Execution / When: Intentamos borrarlo
            int filasBorradas = repository.delete(quadFantasma);

            // Verification / Then: Se debe informar de 0 filas afectadas
            assertThat("UNIT DELETE 2: Borrar Quad inexistente debe afectar a 0 filas", filasBorradas, is(0));
        });
    }

    /**
     * Verifica que el intento de borrar un Quad nulo no provoca un crash y devuelve 0 filas afectadas.
     */
    @Test
    public void testDeleteQuadNulo() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Execution / When: Intento de borrar null
            int filasBorradas = repository.delete(null);

            // Verification / Then: Se gestiona de forma segura
            assertThat("UNIT DELETE 3: Al borrar nulo, Room afecta a 0 filas", filasBorradas, is(0));
        });
    }

    /**
     * Verifica que la eliminación de un Quad funciona correctamente incluso si tiene reservas vinculadas.
     * Valida que no existen errores de restricción de clave foránea.
     */
    @Test
    public void testDeleteQuadConCascada() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository quadRepo = activity.getQuadRepository();
            ReservaRepository reservaRepo = activity.getReservaRepository();

            // Setup / Given: Insertar Quad y vincularle una reserva
            Quad quadTest = new Quad("8888-CAS", false, 60.0, "Quad en Cascada");
            long idQuad = quadRepo.insert(quadTest);
            assertThat(idQuad, is(greaterThan(0L)));
            quadTest.setId((int) idQuad);

            List<ReservaQuad> listaVehiculos = new ArrayList<>();
            listaVehiculos.add(new ReservaQuad(0, (int) idQuad, 1, 0.0));
            
            Reserva reservaTest = new Reserva("Pedro Cascada", 654321987, "2026-01-20", "2026-01-22");
            long idReserva = reservaRepo.insertSync(reservaTest, listaVehiculos);
            assertThat(idReserva, is(greaterThan(0L)));
            reservaTest.setId((int) idReserva);

            // Execution / When: Borrar el Quad vinculado
            int filasBorradas = quadRepo.delete(quadTest);

            // Verification / Then: El Quad debe borrarse sin errores de integridad
            assertThat("UNIT DELETE 4: Fallo al borrar Quad vinculado (Falla el CASCADE)", filasBorradas, is(greaterThan(0)));

            reservaRepo.delete(reservaTest);
        });
    }
}
