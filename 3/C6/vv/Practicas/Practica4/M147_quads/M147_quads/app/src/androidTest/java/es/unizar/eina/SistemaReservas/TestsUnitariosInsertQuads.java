package es.unizar.eina.SistemaReservas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    private QuadRepository getRepository() {
        final QuadRepository[] repo = new QuadRepository[1];
        scenarioRule.getScenario().onActivity(activity -> {
            repo[0] = activity.getQuadRepository();
        });
        return repo[0];
    }

    @Test
    public void testQuadVálido() {
        QuadRepository repository = getRepository();
        quadTest = new Quad("1234-QWE", true, 45.0, "Válido");
        long idQ1 = repository.insert(quadTest);
        assertTrue("UNIT QUAD 1: Válido (1234-QWE)", idQ1 > 0);
        quadTest.setId((int) idQ1);
    }

    @Test
    public void testMatriculaVacia() {
        QuadRepository repository = getRepository();
        quadTest = new Quad("", true, 45.0, "Vacio");
        long idQ2 = repository.insert(quadTest);
        assertTrue("UNIT QUAD 2: Matrícula vacía", idQ2 < 0);
    }

    @Test
    public void testMatriculaDuplicada() {
        QuadRepository repository = getRepository();
        quadTest = new Quad("9999-RTY", false, 50.0, "Duplicado");
        long idQ = repository.insert(quadTest);
        if(idQ > 0) quadTest.setId((int) idQ);
        long idQ3 = repository.insert(quadTest);
        assertTrue("UNIT QUAD 3: Matrícula duplicada", idQ3 < 0);
    }

    @Test
    public void testMatriculaInvalida() {
        QuadRepository repository = getRepository();
        quadTest = new Quad("11234-BBB", true, 50.0, "Formato mal");
        long idQ4 = repository.insert(quadTest);
        assertTrue("UNIT QUAD 4: Formato matrícula inválido (5 números)", idQ4 < 0);
    }

    @Test
    public void testPrecioNegativo() {
        QuadRepository repository = getRepository();
        quadTest = new Quad("5555-XXX", true, -1.0, "Negativo");
        long idQ5 = repository.insert(quadTest);
        assertTrue("UNIT QUAD 5: Precio negativo", idQ5 < 0);
    }

    @Test
    public void testQuadVálido2() {
        QuadRepository repository = getRepository();
        quadTest = new Quad("1234-ASD", false, 45.0, "Válido");
        long idQ6 = repository.insert(quadTest);
        assertTrue("UNIT QUAD 6: Válido (1234-QWE, no monoplaza)", idQ6 > 0);
        quadTest.setId((int) idQ6);
    }

    @Test
    public void testMatriculaNull() {
        QuadRepository repository = getRepository();
        quadTest = new Quad(null, true, 45.0, "Vacio");

        long idQ7 = repository.insert(quadTest);
        assertTrue("UNIT QUAD 7: Matrícula null rechazada correctamente (debe devolver < 0)", idQ7 < 0);
    }

    @After
    public void tearDown(){
        QuadRepository repository = getRepository();
        if (quadTest != null && quadTest.getId() > 0) {
            int quadBorrado = repository.delete(quadTest);
            assertTrue("Error: el quad no se ha borrado en el tearDown", quadBorrado > 0);
        }
    }
}
