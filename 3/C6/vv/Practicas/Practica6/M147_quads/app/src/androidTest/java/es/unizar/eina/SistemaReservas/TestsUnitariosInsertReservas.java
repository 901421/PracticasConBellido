package es.unizar.eina.SistemaReservas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

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

    // Este método se ejecuta automáticamente ANTES de cada @Test
    @Before
    public void setUp() {
        scenarioRule.getScenario().onActivity(activity -> {
            // 1. Insertamos un Quad válido que usarán todos los tests
            QuadRepository quadRepo = activity.getQuadRepository();
            quadTest = new Quad("1234-WWW", true, 45.0, "Válido");
            long idQ1 = quadRepo.insert(quadTest);
            assertThat("Setup: Error al insertar Quad base (1234-WWW)", idQ1, is(greaterThan(0L)));
            
            // 2. Preparamos la lista de vehículos para la reserva
            quadTest.setId((int) idQ1);
            listaValida = new ArrayList<>();
            listaValida.add(new ReservaQuad(0, (int) idQ1, 1, 0.0));
        });
    }

    @Test
    public void testReservaValida() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-10", "2026-01-12");
            long idR1 = repository.insertSync(reservaTest, listaValida);
            assertThat("UNIT RES 1: Juan Pérez (Válida)", idR1, is(greaterThan(0L)));
            
            // Asignamos el ID generado para que el tearDown pueda borrarla
            reservaTest.setId((int) idR1); 
        });
    }

    @Test
    public void testReservaNombreVacio() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            reservaTest = new Reserva("", 600111222, "2026-01-10", "2026-01-12");
            long idR2 = repository.insertSync(reservaTest, listaValida);
            assertThat("UNIT RES 2: Fallo por nombre vacío", idR2, is(lessThan(0L)));
        });
    }

    @Test
    public void testReservaFechasIncoherentes() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-15", "2026-01-10");
            long idR3 = repository.insertSync(reservaTest, listaValida);
            assertThat("UNIT RES 3: Fallo por fechas incoherentes", idR3, is(lessThan(0L)));
        });
    }

    @Test
    public void testReservaFaltaDeVehiculos() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-10", "2026-01-12");
            // Le pasamos intencionadamente un ArrayList vacío en vez de listaValida
            long idR4 = repository.insertSync(reservaTest, new ArrayList<>());
            assertThat("UNIT RES 4: Fallo por falta de vehículos", idR4, is(lessThan(0L)));
        });
    }

    @Test
    public void testReservaTelefonoVacio() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            reservaTest = new Reserva("Juan Pérez", 0, "2026-01-10", "2026-01-12");
            long idR5 = repository.insertSync(reservaTest, listaValida);
            assertThat("UNIT RES 5: Fallo por teléfono vacío", idR5, is(lessThan(0L)));
        });
    }

    @Test
    public void testReservaValida2() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            // Test de valor límite: Fecha de inicio igual a fecha de fin
            reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-12", "2026-01-12");
            long idR6 = repository.insertSync(reservaTest, listaValida);
            assertThat("UNIT RES 6: Juan Pérez (Válida, con fechas iguales)", idR6, is(greaterThan(0L)));
            
            // Asignamos el ID generado para que el tearDown pueda borrarla
            reservaTest.setId((int) idR6);
        });
    }

    @Test
    public void testReservaTelefonoNull() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            reservaTest = new Reserva("Juan Pérez", 0, "2026-01-10", "2026-01-12");
            long idR8 = repository.insertSync(reservaTest, listaValida);
            assertThat("UNIT RES 8: Fallo por teléfono null", idR8, is(lessThan(0L)));
        });
    }

    @Test
    public void testReservaFechaNull() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            reservaTest = new Reserva("Juan Pérez", 600111222, null, "2026-01-12");
            long idR9 = repository.insertSync(reservaTest, listaValida);
            assertThat("UNIT RES 9: Fallo por fecha null", idR9, is(lessThan(0L)));
        });
    }

    @Test
    public void testReservaFechaFormatoIncorrecto() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-10", "2026/01/10");
            long idR10 = repository.insertSync(reservaTest, listaValida);
            assertThat("UNIT RES 10: Fallo por fecha con formato incorrecto", idR10, is(lessThan(0L)));
        });
    }

    @Test
    public void testReservaFechaNull2() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-12", null);
            long idR11 = repository.insertSync(reservaTest, listaValida);
            assertThat("UNIT RES 11: Fallo por fecha null 2", idR11, is(lessThan(0L)));
        });
    }

    @Test
    public void testReservaExcedeCascos() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            // quadTest es monoplaza (ver setup). Intentamos reservar con 2 cascos.
            List<ReservaQuad> listaInvalida = new ArrayList<>();
            listaInvalida.add(new ReservaQuad(0, quadTest.getId(), 2, 0.0));
            
            reservaTest = new Reserva("Juan Pérez", 600111222, "2026-01-10", "2026-01-12");
            long idR12 = repository.insertSync(reservaTest, listaInvalida);
            assertThat("UNIT RES 12: Fallo por exceso de cascos en monoplaza", idR12, is(lessThan(0L)));
        });
    }

    @Test
    public void testReservaSolapeFechas() {
        scenarioRule.getScenario().onActivity(activity -> {
            ReservaRepository repository = activity.getReservaRepository();
            
            // 1. Insertamos una reserva válida previa (Juan)
            Reserva r1 = new Reserva("Juan Pérez", 600111222, "2026-02-10", "2026-02-15");
            long idR1 = repository.insertSync(r1, listaValida);
            assertThat("Setup: Error al insertar reserva base para solape", idR1, is(greaterThan(0L)));

            // 2. Intentamos insertar otra (Ana) que pisa las fechas (Solape RF 6)
            reservaTest = new Reserva("Ana López", 600222333, "2026-02-14", "2026-02-18");
            long idR13 = repository.insertSync(reservaTest, listaValida);
            
            // Si el repositorio cumple RF 6, debe devolver -1
            assertThat("UNIT RES 13: Fallo esperado por solape de quad", idR13, is(lessThan(0L)));
            
            // Limpieza manual de r1 para no dejar basura si el test sigue
            r1.setId((int) idR1);
            repository.delete(r1);
        });
    }

    // Este método se ejecuta automáticamente DESPUÉS de cada @Test
    @After
    public void tearDown() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository quadRepo = activity.getQuadRepository();
            if (quadTest != null && quadTest.getId() > 0) {
                int quadBorrado = quadRepo.delete(quadTest);
                assertThat("Error: el quad no se ha borrado en el tearDown", quadBorrado, is(greaterThan(0)));
            }
            
            ReservaRepository repository = activity.getReservaRepository();
            if (reservaTest != null && reservaTest.getId() > 0) {
                int reservaBorrada = repository.delete(reservaTest);
                assertThat("Error: la reserva no se ha borrado en el tearDown", reservaBorrada, is(greaterThan(0)));
            }
        });
    }
}
