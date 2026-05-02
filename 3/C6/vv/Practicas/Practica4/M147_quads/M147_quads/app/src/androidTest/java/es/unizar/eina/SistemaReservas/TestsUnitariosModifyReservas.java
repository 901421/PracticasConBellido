package es.unizar.eina.SistemaReservas;

import static org.junit.Assert.assertTrue;

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

@RunWith(AndroidJUnit4.class)
public class TestsUnitariosModifyReservas {

    private Quad quadTest;
    private Reserva reservaBase;

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

    // Metodo para preparar el entorno del test

    private List<ReservaQuad> prepararEntorno(QuadRepository quadRepo, ReservaRepository reservaRepo, String matriculaQuad) {
        // Crear Quad
        quadTest = new Quad(matriculaQuad, true, 45.0, "Quad Base");
        long idQ = quadRepo.insert(quadTest);
        quadTest.setId((int) idQ);

        // Preparar lista de vínculos
        List<ReservaQuad> listaValida = new ArrayList<>();
        listaValida.add(new ReservaQuad(0, (int) idQ, 1, 0.0));

        // Crear e insertar Reserva Base
        reservaBase = new Reserva("Base", 123456789, "01/01/2026", "02/01/2026");
        long idR = reservaRepo.insertSync(reservaBase, listaValida);
        reservaBase.setId((int) idR);

        return listaValida;
    }

    // Válida (Devolución posterior a recogida)
    @Test
    public void testUpdateReservaValida() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0001-MOD");

        Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "10/01/2026", "12/01/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 1: Éxito esperado (Fechas válidas)", filasActualizadas > 0);
    }

    // Válida (Devolución igual a recogida - 1 día de alquiler)
    @Test
    public void testUpdateFechasIguales() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0002-MOD");

        Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "10/01/2026", "10/01/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 2: Éxito esperado (Devolución mismo día)", filasActualizadas > 0);
    }

    // Cliente vacío
    @Test
    public void testUpdateClienteVacio() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0003-MOD");

        Reserva reservaModificada = new Reserva("", 600111222, "10/01/2026", "12/01/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 3: Fallo esperado por Cliente vacío", filasActualizadas <= 0);
    }

    // Cliente Null
    @Test
    public void testUpdateClienteNull() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0004-MOD");

        Reserva reservaModificada = new Reserva(null, 600111222, "10/01/2026", "12/01/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 4: Fallo esperado por Cliente null. Debió devolver 0.", filasActualizadas <= 0);
    }

    // Teléfono vacío
    @Test
    public void testUpdateTelefonoVacio() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0005-MOD");

        // Según tu tabla: Teléfono vacío y además fechas incoherentes
        Reserva reservaModificada = new Reserva("Juan Pérez", 0, "15/01/2026", "10/01/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 5: Fallo esperado por Teléfono vacío", filasActualizadas <= 0);
    }

    // Teléfono Null
    @Test
    public void testUpdateTelefonoNull() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0006-MOD");

        Reserva reservaModificada = new Reserva("Juan Pérez", 0, "10/01/2026", "12/01/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 6: Fallo esperado por Teléfono null. Debió devolver 0.", filasActualizadas <= 0);
    }

    // F. Recogida Formato Incorrecto
    @Test
    public void testUpdateFechaRecogidaFormatoInvalido() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0007-MOD");

        Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "2026/01/10", "12/01/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 7: Fallo esperado por formato de F. Recogida", filasActualizadas <= 0);
    }

    // F. Recogida Null

    @Test
    public void testUpdateFechaRecogidaNull() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0008-MOD");

        Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, null, "12/01/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 8: Fallo esperado por F. Recogida null. Debió devolver 0.", filasActualizadas <= 0);
    }


    // F. Recogida Inexistente

    @Test
    public void testUpdateFechaRecogidaInexistente() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0009-MOD");

        Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "30/02/2026", "01/01/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 9: Fallo esperado por F. Recogida inexistente", filasActualizadas <= 0);
    }

    // F. Devolución Formato Incorrecto
    @Test
    public void testUpdateFechaDevolucionFormatoInvalido() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0010-MOD");

        Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "10/01/2026", "2026/01/10");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 10: Fallo esperado por formato de F. Devolución", filasActualizadas <= 0);
    }

    // F. Devolución Null
    @Test
    public void testUpdateFechaDevolucionNull() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0011-MOD");

        Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "10/01/2026", null);
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 11: Fallo esperado por F. Devolución null. Debió devolver 0.", filasActualizadas <= 0);
    }


    // F. Devolución Inexistente
    @Test
    public void testUpdateFechaDevolucionInexistente() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0012-MOD");

        Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "10/01/2026", "30/02/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 12: Fallo esperado por F. Devolución inexistente", filasActualizadas <= 0);
    }

    // Devolución anterior a Recogida
    @Test
    public void testUpdateDevolucionAnteriorARecogida() {
        QuadRepository qRepo = getQuadRepository();
        ReservaRepository rRepo = getReservaRepository();
        List<ReservaQuad> lista = prepararEntorno(qRepo, rRepo, "0013-MOD");

        Reserva reservaModificada = new Reserva("Juan Pérez", 600111222, "12/01/2026", "10/01/2026");
        reservaModificada.setId(reservaBase.getId());

        int filasActualizadas = rRepo.update(reservaModificada, lista);
        assertTrue("UNIT UPDATE RES 13: Fallo esperado por Devolución anterior a Recogida", filasActualizadas <= 0);
    }


    // ID de Reserva Inexistente en la Base de Datos
    @Test
    public void testUpdateReservaInexistenteEnBD() {
        QuadRepository quadRepo = getQuadRepository();
        ReservaRepository reservaRepo = getReservaRepository();

        // Insertamos Quad pero no insertamos la Reserva
        quadTest = new Quad("0015-MOD", true, 45.0, "Quad Base");
        long idQ = quadRepo.insert(quadTest);
        quadTest.setId((int) idQ);

        List<ReservaQuad> listaValida = new ArrayList<>();
        listaValida.add(new ReservaQuad(0, (int) idQ, 1, 0.0));

        Reserva reservaInexistente = new Reserva("Juan Pérez", 600111222, "10/01/2026", "12/01/2026");
        reservaInexistente.setId(999999); // ID inventado

        int filasActualizadas = reservaRepo.update(reservaInexistente, listaValida);
        assertTrue("UNIT UPDATE RES 15: Fallo esperado por Reserva inexistente", filasActualizadas <= 0);

        reservaBase = null; // Evitar que el tearDown intente borrar basura
    }


    // Limpieza de base de datos

    @After
    public void tearDown() {
        ReservaRepository reservaRepo = getReservaRepository();
        QuadRepository quadRepo = getQuadRepository();

        // Borrar primero la Reserva (tiene clave foránea al Quad)
        if (reservaBase != null && reservaBase.getId() > 0) {
            reservaRepo.delete(reservaBase);
        }

        // Borrar el Quad
        if (quadTest != null && quadTest.getId() > 0) {
            quadRepo.delete(quadTest);
        }
    }
}