package es.unizar.eina.SistemaReservas;

import static org.junit.Assert.assertTrue;

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
public class TestsUnitariosInsertReservas {
    private Quad quadTest;
    private Reserva reservaTest;
    private List<ReservaQuad> listaValida;

    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule = new ActivityScenarioRule<>(SistemaReservas.class);

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

    // Este método se ejecuta automáticamente ANTES de cada @Test
    @Before
    public void setUp() {
        // 1. Insertamos un Quad válido que usarán todos los tests
        QuadRepository quadRepo = getQuadRepository();
        quadTest = new Quad("1234-WWW", true, 45.0, "Válido");
        long idQ1 = quadRepo.insert(quadTest);
        assertTrue("Setup: Error al insertar Quad base (1234-WWW)", idQ1 > 0);
        
        // 2. Preparamos la lista de vehículos para la reserva
        quadTest.setId((int) idQ1);
        listaValida = new ArrayList<>();
        listaValida.add(new ReservaQuad(0, (int)idQ1, 1, 0.0));
    }

    @Test
    public void testReservaValida() {
        ReservaRepository repository = getReservaRepository();
        reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-10", "2026-01-12");
        long idR1 = repository.insertSync(reservaTest, listaValida);
        assertTrue("UNIT RES 1: Juan Pérez (Válida)", idR1 > 0);
        
        // Asignamos el ID generado para que el tearDown pueda borrarla
        reservaTest.setId((int) idR1); 
    }

    @Test
    public void testReservaNombreVacio() {
        ReservaRepository repository = getReservaRepository();
        reservaTest = new Reserva("", 600111222, "2026-01-10", "2026-01-12");
        long idR2 = repository.insertSync(reservaTest, listaValida);
        assertTrue("UNIT RES 2: Fallo por nombre vacío", idR2 < 0);
    }

    @Test
    public void testReservaFechasIncoherentes() {
        ReservaRepository repository = getReservaRepository();
        reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-15", "2026-01-10");
        long idR3 = repository.insertSync(reservaTest, listaValida);
        assertTrue("UNIT RES 3: Fallo por fechas incoherentes", idR3 < 0);
    }

    @Test
    public void testReservaFaltaDeVehiculos() {
        ReservaRepository repository = getReservaRepository();
        reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-10", "2026-01-12");
        // Le pasamos intencionadamente un ArrayList vacío en vez de listaValida
        long idR4 = repository.insertSync(reservaTest, new ArrayList<>());
        assertTrue("UNIT RES 4: Fallo por falta de vehículos", idR4 < 0);
    }

    @Test
    public void testReservaTelefonoVacio() {
        ReservaRepository repository = getReservaRepository();
        reservaTest = new Reserva("Juan Pérez", 0, "2026-01-10", "2026-01-12");
        long idR5 = repository.insertSync(reservaTest, listaValida);
        assertTrue("UNIT RES 5: Fallo por teléfono vacío", idR5 < 0);
    }

    @Test
    public void testReservaValida2() {
        ReservaRepository repository = getReservaRepository();
        // Test de valor límite: Fecha de inicio igual a fecha de fin
        reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-12", "2026-01-12");
        long idR6 = repository.insertSync(reservaTest, listaValida);
        assertTrue("UNIT RES 6: Juan Pérez (Válida, con fechas iguales)", idR6 > 0);
        
        // Asignamos el ID generado para que el tearDown pueda borrarla
        reservaTest.setId((int) idR6);
    }

    @Test
    public void testReservaTelefonoNull() {
        ReservaRepository repository = getReservaRepository();
        reservaTest = new Reserva("Juan Pérez", 0, "2026-01-10", "2026-01-12");
        long idR8 = repository.insertSync(reservaTest, listaValida);
        assertTrue("UNIT RES 8: Fallo por teléfono null", idR8 < 0);
    }

    @Test
    public void testReservaFechaNull() {
        ReservaRepository repository = getReservaRepository();
        reservaTest = new Reserva("Juan Pérez", 600111222, null, "2026-01-12");
        long idR9 = repository.insertSync(reservaTest, listaValida);
        assertTrue("UNIT RES 9: Fallo por fecha null", idR9 < 0);
    }

    @Test
    public void testReservaFechaFormatoIncorrecto() {
        ReservaRepository repository = getReservaRepository();
        reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-10", "2026/01/10");
        long idR10 = repository.insertSync(reservaTest, listaValida);
        assertTrue("UNIT RES 10: Fallo por fecha con formato incorrecto", idR10 < 0);
    }

    @Test
    public void testReservaFechaNull2() {
        ReservaRepository repository = getReservaRepository();
        reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-12", null);
        long idR11 = repository.insertSync(reservaTest, listaValida);
        assertTrue("UNIT RES 11: Fallo por fecha null 2", idR11 < 0);
    }

    // Este método se ejecuta automáticamente DESPUÉS de cada @Test
    @After
    public void tearDown() {
        QuadRepository quadRepo = getQuadRepository();
        if (quadTest != null && quadTest.getId() > 0) {
            int quadBorrado = quadRepo.delete(quadTest);
            assertTrue("Error: el quad no se ha borrado en el tearDown", quadBorrado > 0);
        }
        
        ReservaRepository repository = getReservaRepository();
        if (reservaTest != null && reservaTest.getId() > 0) {
            int reservaBorrada = repository.delete(reservaTest);
            // Corregido el mensaje de error que antes ponía "quad" en vez de "reserva"
            assertTrue("Error: la reserva no se ha borrado en el tearDown", reservaBorrada > 0);
        }
    }
}