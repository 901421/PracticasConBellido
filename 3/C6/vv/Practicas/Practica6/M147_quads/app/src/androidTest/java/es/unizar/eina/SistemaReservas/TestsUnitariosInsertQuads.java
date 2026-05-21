package es.unizar.eina.SistemaReservas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.database.QuadRepository;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

@RunWith(AndroidJUnit4.class)
public class TestsUnitariosInsertQuads {
    private Quad quadTest;

    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule
            = new ActivityScenarioRule<>(SistemaReservas.class);

    @Test
    public void testQuadVálido() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("1234-QWE", true, 45.0, "Válido");
            long idQ1 = repository.insert(quadTest);
            assertThat("UNIT QUAD 1: Válido (1234-QWE)", idQ1, is(greaterThan(0L)));
            quadTest.setId((int) idQ1);
        });
    }

    @Test
    public void testQuadValidoInsercion2() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            // Test 2 del informe: Matrícula ya existente.
            quadTest = new Quad("1234-QWE", true, 45.0, "Quad inicial");
            long idQ = repository.insert(quadTest);
            if (idQ > 0) quadTest.setId((int) idQ);
            
            Quad quadDuplicado = new Quad("1234-QWE", false, 60.0, "Intento duplicado");
            long idRechazado = repository.insert(quadDuplicado);
            assertThat("UNIT QUAD 1.2: Inserción rechazada por matrícula existente", idRechazado, is(lessThan(0L)));
        });
    }

    @Test
    public void testMatriculaVacia() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("", true, 45.0, "Vacio");
            long idQ2 = repository.insert(quadTest);
            assertThat("UNIT QUAD 2: Matrícula vacía", idQ2, is(lessThan(0L)));
        });
    }

    @Test
    public void testMatriculaDuplicada() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("9999-RTY", false, 50.0, "Duplicado");
            long idQ = repository.insert(quadTest);
            if (idQ > 0) quadTest.setId((int) idQ);
            long idQ3 = repository.insert(quadTest);
            assertThat("UNIT QUAD 3: Matrícula duplicada", idQ3, is(lessThan(0L)));
        });
    }

    @Test
    public void testMatriculaInvalida() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("11234-BBB", true, 50.0, "Formato mal");
            long idQ4 = repository.insert(quadTest);
            assertThat("UNIT QUAD 4: Formato matrícula inválido (5 números)", idQ4, is(lessThan(0L)));
        });
    }

    @Test
    public void testPrecioNegativo() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("5555-XXX", true, -1.0, "Negativo");
            long idQ5 = repository.insert(quadTest);
            assertThat("UNIT QUAD 5: Precio negativo", idQ5, is(lessThan(0L)));
        });
    }

    @Test
    public void testQuadVálido2() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("1234-ASD", false, 45.0, "Válido");
            long idQ6 = repository.insert(quadTest);
            assertThat("UNIT QUAD 6: Válido (1234-QWE, no monoplaza)", idQ6, is(greaterThan(0L)));
            quadTest.setId((int) idQ6);
        });
    }

    @Test
    public void testMatriculaNull() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad(null, true, 45.0, "Vacio");

            long idQ7 = repository.insert(quadTest);
            assertThat("UNIT QUAD 7: Matrícula null rechazada correctamente (debe devolver < 0)", idQ7, is(lessThan(0L)));
        });
    }

    @After
    public void tearDown() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            if (quadTest != null && quadTest.getId() > 0) {
                int quadBorrado = repository.delete(quadTest);
                assertThat("Error: el quad no se ha borrado en el tearDown", quadBorrado, is(greaterThan(0)));
            }
        });
    }
}
