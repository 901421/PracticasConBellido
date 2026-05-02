package es.unizar.eina.SistemaReservas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
public class QuadTest {
    private Quad quadTest;
    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule
            = new ActivityScenarioRule<>(SistemaReservas.class);

    private QuadRepository getQuadRepository() {
        final QuadRepository[] repo = new QuadRepository[1];
        scenarioRule.getScenario().onActivity(activity -> repo[0] = activity.getQuadRepository());
        return repo[0];
    }

    @Test
    public void testCreationIncreasesNumberOfQuads() {
        QuadRepository repository = getQuadRepository();

        int quadsAntesDeAgnadir = repository.getNumeroQuads();

        quadTest = new Quad("1114-AAA",true, 0.00, "Quad de test");
        long idGenerado =repository.insert(quadTest);
        assertTrue("Error: no se inserta", idGenerado > 0);
        quadTest.setId((int) idGenerado);

        int quadsDespuesDeAgnadir= repository.getNumeroQuads();

        assertEquals(quadsAntesDeAgnadir+1,quadsDespuesDeAgnadir);
    }

    @Test
    public void testLosCamposDelQuadSonIguales() {
        QuadRepository repository = getQuadRepository();

        quadTest = new Quad("1234-ADD",true, 0.00, "Quad de test");
        long idQuadAgnadido=repository.insert(quadTest);

        Quad quadRecogido = repository.getQuadById((int)idQuadAgnadido);
        assertTrue("Fallo al insertar", idQuadAgnadido > 0);
        quadTest.setId((int) idQuadAgnadido); //sin esto no va
        assertNotNull("El quad no puede ser nulo", quadRecogido);
        assertEquals("El quad tiene que tener igual id",idQuadAgnadido,quadRecogido.getId());

        assertEquals("El quad tiene que tener igual matricula",quadTest.getMatricula(),quadRecogido.getMatricula());
        assertEquals("El quad tiene que tener igual tipo",quadTest.getEsmonoplaza(),quadRecogido.getEsmonoplaza());

        assertEquals("El quad tiene que tener igual precio",quadTest.getPrecio(),quadRecogido.getPrecio(),0.001);

        assertEquals("El quad tiene que tener igual descripcion",quadTest.getDescripcion(),quadRecogido.getDescripcion());
    }

    @After
    public void tearDown(){
        QuadRepository repository = getQuadRepository();
        if (quadTest != null && quadTest.getId() > 0) {
            int quadBorrado = repository.delete(quadTest);
            assertTrue("Error: el quad no se ha borrado en el tearDown", quadBorrado > 0);
        }
    }

}
