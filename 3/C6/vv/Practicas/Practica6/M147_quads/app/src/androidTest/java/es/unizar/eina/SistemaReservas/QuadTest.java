package es.unizar.eina.SistemaReservas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;

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

    @Test
    public void testCreationIncreasesNumberOfQuads() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            int quadsAntesDeAgnadir = repository.getNumeroQuads();

            quadTest = new Quad("1114-AAA", true, 0.00, "Quad de test");
            long idGenerado = repository.insert(quadTest);
            assertThat("Error: no se inserta", idGenerado, is(greaterThan(0L)));
            quadTest.setId((int) idGenerado);

            int quadsDespuesDeAgnadir = repository.getNumeroQuads();

            assertThat(quadsDespuesDeAgnadir, is(quadsAntesDeAgnadir + 1));
        });
    }

    @Test
    public void testLosCamposDelQuadSonIguales() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            quadTest = new Quad("1234-ADD", true, 0.00, "Quad de test");
            long idQuadAgnadido = repository.insert(quadTest);

            Quad quadRecogido = repository.getQuadById((int) idQuadAgnadido);
            assertThat("Fallo al insertar", idQuadAgnadido, is(greaterThan(0L)));
            quadTest.setId((int) idQuadAgnadido); //sin esto no va
            assertNotNull("El quad no puede ser nulo", quadRecogido);
            
            assertThat("El quad tiene que tener igual id", (long) quadRecogido.getId(), is(idQuadAgnadido));
            assertThat("El quad tiene que ser igual al insertado", quadRecogido, is(quadTest));
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
