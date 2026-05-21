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

import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.database.QuadRepository;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

@RunWith(AndroidJUnit4.class)
public class TestsUnitariosModifyQuads {

    private Quad quadTest;

    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    // Válido (Matrícula correcta, precio > 0, esMonoplaza true, desc "")
    @Test
    public void testUpdateQuadValidoMonoplaza() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            //Insertamos un Quad base
            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            assertThat("Fallo previo: no se insertó", idGenerado, is(greaterThan(0L)));
            quadTest.setId((int) idGenerado);

            quadTest.setMatricula("1234-UAA");
            quadTest.setPrecio(45.0);
            quadTest.setEsmonoplaza(true);
            quadTest.setDescripcion("");

            int filasActualizadas = repository.update(quadTest);
            assertThat("UNIT UPDATE 1: Éxito esperado (Monoplaza)", filasActualizadas, is(greaterThan(0)));
        });
    }

    // Válido (esMonoplaza false)
    @Test
    public void testUpdateQuadValidoBiplaza() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            quadTest.setMatricula("1234-UAB");
            quadTest.setPrecio(45.0);
            quadTest.setEsmonoplaza(false);
            quadTest.setDescripcion("");

            int filasActualizadas = repository.update(quadTest);
            assertThat("UNIT UPDATE 2: Éxito esperado (Biplaza)", filasActualizadas, is(greaterThan(0)));
        });
    }

    //Matrícula vacía
    @Test
    public void testUpdateMatriculaVacia() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            quadTest.setMatricula("");
            quadTest.setPrecio(45.0);
            quadTest.setEsmonoplaza(true);
            quadTest.setDescripcion("");

            int filasActualizadas = repository.update(quadTest);
            assertThat("UNIT UPDATE 3: Fallo esperado por Matrícula vacía", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    // Matrícula inválida (5 números)
    @Test
    public void testUpdateMatriculaFormatoInvalido() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            quadTest.setMatricula("11234-UAC");
            quadTest.setPrecio(50.0);
            quadTest.setEsmonoplaza(true);
            quadTest.setDescripcion("");

            int filasActualizadas = repository.update(quadTest);
            assertThat("UNIT UPDATE 4: Fallo esperado por Formato Matrícula", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    // Matrícula Null
    @Test
    public void testUpdateMatriculaNull() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            // Modificaciones Caso 5
            quadTest.setMatricula(null);
            quadTest.setPrecio(45.0);
            quadTest.setEsmonoplaza(true);
            quadTest.setDescripcion("");

            int filasActualizadas = repository.update(quadTest);
            assertThat("UNIT UPDATE 5: Fallo esperado por Matrícula null. Debió devolver 0.", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    // Precio negativo
    @Test
    public void testUpdatePrecioNegativo() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            quadTest.setMatricula("1234-UAD");
            quadTest.setPrecio(-1.0);
            quadTest.setEsmonoplaza(true);
            quadTest.setDescripcion("");

            int filasActualizadas = repository.update(quadTest);
            assertThat("UNIT UPDATE 6: Fallo esperado por Precio negativo", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    // Nota: El Caso 7 (esMonoplaza null) no se implementa porque el compilador de Java
    // no permite asignar 'null' a un tipo primitivo 'boolean'.

    //  Descripción null
    @Test
    public void testUpdateDescripcionNull() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            quadTest.setMatricula("1234-UAE");
            quadTest.setPrecio(45.0);
            quadTest.setEsmonoplaza(true);
            quadTest.setDescripcion(null);

            int filasActualizadas = repository.update(quadTest);
            assertThat("UNIT UPDATE 8: Éxito esperado con Descripción null (respetando diseño original)", filasActualizadas, is(greaterThan(0)));
        });
    }

    // Quad no existe en BD (Intentar actualizar algo que no existe)
    @Test
    public void testUpdateQuadInexistenteEnBD() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Creamos el quad pero no lo insertamos en la BD
            quadTest = new Quad("1235-UAF", true, 50.0, "");
            quadTest.setId(999999); // Le ponemos un ID inventado

            // Intentamos actualizarlo
            int filasActualizadas = repository.update(quadTest);

            assertThat("UNIT UPDATE 9: Fallo esperado por registro inexistente", filasActualizadas, is(lessThanOrEqualTo(0)));

            // Evitamos que el tearDown intente borrarlo
            quadTest = null;
        });
    }

    @After
    public void tearDown() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            if (quadTest != null && quadTest.getId() > 0) {
                repository.delete(quadTest);
            }
        });
    }
}
