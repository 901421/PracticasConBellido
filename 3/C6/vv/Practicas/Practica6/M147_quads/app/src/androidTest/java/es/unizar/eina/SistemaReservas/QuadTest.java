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

/**
 * Clase de pruebas instrumentadas para la entidad Quad.
 * Se enfoca en validar el comportamiento básico del repositorio (Pruebas de Humo)
 * y la integridad de la persistencia de datos tras inserciones.
 */
@RunWith(AndroidJUnit4.class)
public class QuadTest {

    /** Objeto Quad de prueba para ser insertado y validado. */
    private Quad quadTest;

    /** Regla que lanza la actividad principal para interactuar con el QuadRepository. */
    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule
            = new ActivityScenarioRule<>(SistemaReservas.class);

    /**
     * Prueba de Humo: Verifica que al insertar un quad, el contador total se incrementa en 1.
     */
    @Test
    public void testCreationIncreasesNumberOfQuads() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Setup: Contamos quads actuales
            int quadsAntesDeAgnadir = repository.getNumeroQuads();

            // Execution: Insertamos un nuevo Quad
            quadTest = new Quad("1114-AAA", true, 0.00, "Quad de test");
            long idGenerado = repository.insert(quadTest);
            assertThat("Error: no se inserta", idGenerado, is(greaterThan(0L)));
            quadTest.setId((int) idGenerado);

            // Verification: Comprobamos el incremento
            int quadsDespuesDeAgnadir = repository.getNumeroQuads();
            assertThat(quadsDespuesDeAgnadir, is(quadsAntesDeAgnadir + 1));
        });
    }

    /**
     * Prueba Exhaustiva de Recuperación: Verifica que un Quad recuperado por ID
     * mantiene exactamente los mismos valores que el objeto original insertado.
     */
    @Test
    public void testLosCamposDelQuadSonIguales() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Setup: Insertamos Quad con campos específicos
            quadTest = new Quad("1234-ADD", true, 0.00, "Quad de test");
            long idQuadAgnadido = repository.insert(quadTest);

            // Execution: Recuperamos de la BD
            Quad quadRecogido = repository.getQuadById((int) idQuadAgnadido);
            assertThat("Fallo al insertar", idQuadAgnadido, is(greaterThan(0L)));
            quadTest.setId((int) idQuadAgnadido); 
            
            // Verification: Comprobación de integridad del objeto (usa Quad.equals())
            assertNotNull("El quad no puede ser nulo", quadRecogido);
            assertThat("El quad tiene que tener igual id", (long) quadRecogido.getId(), is(idQuadAgnadido));
            assertThat("El quad tiene que ser igual al insertado", quadRecogido, is(quadTest));
        });
    }

    /**
     * Limpieza tras cada prueba: borra el quad generado para no dejar basura en la BD.
     */
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
