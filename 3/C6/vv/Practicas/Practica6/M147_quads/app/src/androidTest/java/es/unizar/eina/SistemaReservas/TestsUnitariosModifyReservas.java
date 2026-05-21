package es.unizar.eina.SistemaReservas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
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
 * Suite de pruebas unitarias instrumentadas para la modificación de Reservas (Cubre RF 7).
 * Valida la actualización de datos de cliente, fechas y vínculos con quads, asegurando
 * que se cumplen las restricciones de negocio y formato en el repositorio.
 */
@RunWith(AndroidJUnit4.class)
public class TestsUnitariosModifyReservas {

    /** Quad auxiliar para asociar a las reservas en las pruebas. */
    private Quad quadTest;
    /** Reserva base que será modificada en cada caso de prueba. */
    private Reserva reservaBase;

    /** Regla que lanza la actividad principal para obtener el contexto de los repositorios. */
    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    /**
     * Helper para preparar el entorno de cada test: crea un quad y una reserva inicial vinculada.
     */
    private List<ReservaQuad> prepararEntorno(QuadRepository quadRepo, ReservaRepository reservaRepo, String matriculaQuad) {
        // Setup inicial: Crear Quad
        quadTest = new Quad(matriculaQuad, true, 45.0, "Quad Base");
        long idQ = quadRepo.insert(quadTest);
        quadTest.setId((int) idQ);

        // Setup inicial: Preparar lista de vínculos (1 quad, 1 casco)
        List<ReservaQuad> listaValida = new ArrayList<>();
        listaValida.add(new ReservaQuad(0, (int) idQ, 1, 0.0));

        // Setup inicial: Crear e insertar Reserva Base para luego modificarla
        reservaBase = new Reserva("Base", 123456789, "2026-01-01", "2026-01-02");
        long idR = reservaRepo.insertSync(reservaBase, listaValida);
        reservaBase.setId((int) idR);

        return listaValida;
    }

    /**
     * Prueba la modificación exitosa de una reserva con fechas válidas.
     */
    @Test
    public void testUpdateReservaValida() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository qRepo = activity.getQuadRepository();
            ReservaRepository rRepo = activity.getReservaRepository();
            List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0001-MOD");

            // Execution: Modificamos datos del cliente y fechas
            Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "2026-01-10", "2026-01-12");
            reservaModificada.setId(reservaBase.getId());

            int filasActualizadas = rRepo.update(reservaModificada, lista);
            
            // Verification: Éxito esperado
            assertThat("UNIT UPDATE RES 1: Éxito esperado (Fechas válidas)", filasActualizadas, is(greaterThan(0)));
        });
    }

    /**
     * Prueba la modificación exitosa con recogida y devolución el mismo día (mínimo alquiler).
     */
    @Test
    public void testUpdateFechasIguales() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository qRepo = activity.getQuadRepository();
            ReservaRepository rRepo = activity.getReservaRepository();
            List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0002-MOD");

            // Execution: Misma fecha in/out
            Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "2026-01-10", "2026-01-10");
            reservaModificada.setId(reservaBase.getId());

            int filasActualizadas = rRepo.update(reservaModificada, lista);
            
            // Verification
            assertThat("UNIT UPDATE RES 2: Éxito esperado (Devolución mismo día)", filasActualizadas, is(greaterThan(0)));
        });
    }

    /**
     * Prueba el rechazo de modificación cuando el nombre del cliente está vacío.
     */
    @Test
    public void testUpdateClienteVacio() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository qRepo = activity.getQuadRepository();
            ReservaRepository rRepo = activity.getReservaRepository();
            List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0003-MOD");

            // Execution: Nombre vacío
            Reserva reservaModificada = new Reserva("", 600111222, "2026-01-10", "2026-01-12");
            reservaModificada.setId(reservaBase.getId());

            int filasActualizadas = rRepo.update(reservaModificada, lista);
            
            // Verification: Debe fallar
            assertThat("UNIT UPDATE RES 3: Fallo esperado por Cliente vacío", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    /**
     * Prueba el rechazo de modificación con cliente null.
     */
    @Test
    public void testUpdateClienteNull() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository qRepo = activity.getQuadRepository();
            ReservaRepository rRepo = activity.getReservaRepository();
            List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0004-MOD");

            // Execution: Nombre null
            Reserva reservaModificada = new Reserva(null, 600111222, "2026-01-10", "2026-01-12");
            reservaModificada.setId(reservaBase.getId());

            int filasActualizadas = rRepo.update(reservaModificada, lista);
            
            // Verification
            assertThat("UNIT UPDATE RES 4: Fallo esperado por Cliente null", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    /**
     * Prueba el rechazo de modificación con teléfono inválido (valor 0).
     */
    @Test
    public void testUpdateTelefonoVacio() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository qRepo = activity.getQuadRepository();
            ReservaRepository rRepo = activity.getReservaRepository();
            List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0005-MOD");

            // Execution: Teléfono inválido
            Reserva reservaModificada = new Reserva("Juan Pérez", 0, "2026-01-15", "2026-01-10");
            reservaModificada.setId(reservaBase.getId());

            int filasActualizadas = rRepo.update(reservaModificada, lista);
            
            // Verification
            assertThat("UNIT UPDATE RES 5: Fallo esperado por Teléfono vacío", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    /**
     * Prueba el rechazo con fecha de recogida en formato incorrecto (no ISO 8601).
     */
    @Test
    public void testUpdateFechaRecogidaFormatoInvalido() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository qRepo = activity.getQuadRepository();
            ReservaRepository rRepo = activity.getReservaRepository();
            List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0007-MOD");

            // Execution: Formato dd/mm/yyyy no aceptado por repositorio
            Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "10/01/2026", "2026-01-12");
            reservaModificada.setId(reservaBase.getId());

            int filasActualizadas = rRepo.update(reservaModificada, lista);
            
            // Verification
            assertThat("UNIT UPDATE RES 7: Fallo esperado por formato de F. Recogida", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    /**
     * Prueba el rechazo con fecha de recogida inexistente (30 de febrero).
     */
    @Test
    public void testUpdateFechaRecogidaInexistente() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository qRepo = activity.getQuadRepository();
            ReservaRepository rRepo = activity.getReservaRepository();
            List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0009-MOD");

            // Execution: Fecha absurda
            Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "2026-02-30", "2026-01-01");
            reservaModificada.setId(reservaBase.getId());

            int filasActualizadas = rRepo.update(reservaModificada, lista);
            
            // Verification
            assertThat("UNIT UPDATE RES 9: Fallo esperado por F. Recogida inexistente", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    /**
     * Prueba el rechazo cuando la fecha de devolución es anterior a la de recogida.
     */
    @Test
    public void testUpdateDevolucionAnteriorARecogida() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository qRepo = activity.getQuadRepository();
            ReservaRepository rRepo = activity.getReservaRepository();
            List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0013-MOD");

            // Execution: Fechas incoherentes
            Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "2026-01-12", "2026-01-10");
            reservaModificada.setId(reservaBase.getId());

            int filasActualizadas = rRepo.update(reservaModificada, lista);
            
            // Verification
            assertThat("UNIT UPDATE RES 13: Fallo esperado por Devolución anterior a Recogida", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    /**
     * Prueba el fallo al intentar actualizar una reserva cuyo ID no existe.
     */
    @Test
    public void testUpdateReservaInexistenteEnBD() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository quadRepo = activity.getQuadRepository();
            ReservaRepository reservaRepo = activity.getReservaRepository();

            // Setup: Insertamos Quad pero no la Reserva
            quadTest = new Quad("0015-MOD", true, 45.0, "Quad Base");
            long idQ = quadRepo.insert(quadTest);
            quadTest.setId((int) idQ);

            List<ReservaQuad> listaValida = new ArrayList<>();
            listaValida.add(new ReservaQuad(0, (int) idQ, 1, 0.0));

            // Execution: ID inexistente
            Reserva reservaInexistente = new Reserva("Juan Pérez", 600111222, "2026-01-10", "2026-01-12");
            reservaInexistente.setId(999999); 

            int filasActualizadas = reservaRepo.update(reservaInexistente, listaValida);
            
            // Verification
            assertThat("UNIT UPDATE RES 15: Fallo esperado por Reserva inexistente", filasActualizadas, is(lessThanOrEqualTo(0)));

            reservaBase = null; // Evitar que el tearDown intente borrar basura
        });
    }

    /**
     * Limpieza tras cada prueba: elimina la reserva y el quad creados para mantener la BD inmaculada.
     */
    @After
    public void tearDown() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository reservaRepo = activity.getReservaRepository();
            QuadRepository quadRepo = activity.getQuadRepository();

            // Borrar primero la Reserva (por integridad referencial)
            if (reservaBase != null && reservaBase.getId() > 0) {
                reservaRepo.delete(reservaBase);
            }

            // Borrar el Quad
            if (quadTest != null && quadTest.getId() > 0) {
                quadRepo.delete(quadTest);
            }
        });
    }
}
