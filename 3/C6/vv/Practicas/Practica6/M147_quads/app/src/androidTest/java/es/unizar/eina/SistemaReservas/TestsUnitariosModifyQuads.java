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

/**
 * Suite de pruebas unitarias instrumentadas para la modificación de Quads (Cubre RF 3).
 * Valida las particiones de equivalencia y valores límite para la actualización de vehículos
 * en el repositorio, asegurando la integridad de los datos (matrículas, precios, tipos).
 */
@RunWith(AndroidJUnit4.class)
public class TestsUnitariosModifyQuads {

    /** Objeto Quad utilizado como base para las pruebas de modificación. */
    private Quad quadTest;

    /** Regla que lanza la actividad principal para obtener el contexto del repositorio. */
    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    /**
     * Prueba la modificación exitosa de un Quad monoplaza con datos válidos.
     * Escenario: Matrícula correcta, precio positivo y descripción vacía.
     */
    @Test
    public void testUpdateQuadValidoMonoplaza() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Setup: Insertamos un Quad base para modificar
            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            assertThat("Fallo previo: no se insertó", idGenerado, is(greaterThan(0L)));
            quadTest.setId((int) idGenerado);

            // Execution: Modificamos atributos
            quadTest.setMatricula("1234-UAA");
            quadTest.setPrecio(45.0);
            quadTest.setEsmonoplaza(true);
            quadTest.setDescripcion("");

            int filasActualizadas = repository.update(quadTest);
            
            // Verification: Comprobamos éxito de la actualización
            assertThat("UNIT UPDATE 1: Éxito esperado (Monoplaza)", filasActualizadas, is(greaterThan(0)));
        });
    }

    /**
     * Prueba la modificación exitosa de un Quad biplaza con datos válidos.
     */
    @Test
    public void testUpdateQuadValidoBiplaza() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Setup
            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            // Execution
            quadTest.setMatricula("1234-UAB");
            quadTest.setPrecio(45.0);
            quadTest.setEsmonoplaza(false);
            quadTest.setDescripcion("");

            int filasActualizadas = repository.update(quadTest);
            
            // Verification
            assertThat("UNIT UPDATE 2: Éxito esperado (Biplaza)", filasActualizadas, is(greaterThan(0)));
        });
    }

    /**
     * Prueba el rechazo de modificación cuando la matrícula está vacía.
     */
    @Test
    public void testUpdateMatriculaVacia() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Setup
            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            // Execution: Intentamos poner matrícula vacía
            quadTest.setMatricula("");
            int filasActualizadas = repository.update(quadTest);

            // Verification: Debe fallar la validación
            assertThat("UNIT UPDATE 3: Fallo esperado por Matrícula vacía", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    /**
     * Prueba el rechazo de modificación con formato de matrícula inválido (demasiados números).
     */
    @Test
    public void testUpdateMatriculaFormatoInvalido() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Setup
            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            // Execution: Formato incorrecto
            quadTest.setMatricula("11234-UAC");
            int filasActualizadas = repository.update(quadTest);

            // Verification
            assertThat("UNIT UPDATE 4: Fallo esperado por Formato Matrícula", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    /**
     * Prueba el rechazo de modificación con matrícula null.
     */
    @Test
    public void testUpdateMatriculaNull() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Setup
            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            // Execution
            quadTest.setMatricula(null);
            int filasActualizadas = repository.update(quadTest);

            // Verification
            assertThat("UNIT UPDATE 5: Fallo esperado por Matrícula null", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    /**
     * Prueba el rechazo de modificación con precio negativo (Valor Límite).
     */
    @Test
    public void testUpdatePrecioNegativo() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Setup
            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            // Execution
            quadTest.setPrecio(-1.0);
            int filasActualizadas = repository.update(quadTest);

            // Verification
            assertThat("UNIT UPDATE 6: Fallo esperado por Precio negativo", filasActualizadas, is(lessThanOrEqualTo(0)));
        });
    }

    /**
     * Prueba la modificación exitosa con descripción null (se espera que el sistema lo soporte).
     */
    @Test
    public void testUpdateDescripcionNull() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Setup
            quadTest = new Quad("0000-BAS", true, 20.0, "Base");
            long idGenerado = repository.insert(quadTest);
            quadTest.setId((int) idGenerado);

            // Execution
            quadTest.setDescripcion(null);
            int filasActualizadas = repository.update(quadTest);

            // Verification
            assertThat("UNIT UPDATE 8: Éxito esperado con Descripción null", filasActualizadas, is(greaterThan(0)));
        });
    }

    /**
     * Prueba el fallo al intentar actualizar un Quad cuyo ID no existe en la base de datos.
     */
    @Test
    public void testUpdateQuadInexistenteEnBD() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Setup: Creamos el quad pero no lo insertamos
            quadTest = new Quad("1235-UAF", true, 50.0, "");
            quadTest.setId(999999); // ID inventado

            // Execution
            int filasActualizadas = repository.update(quadTest);

            // Verification: Room debe devolver 0 filas afectadas
            assertThat("UNIT UPDATE 9: Fallo esperado por registro inexistente", filasActualizadas, is(lessThanOrEqualTo(0)));

            // Evitamos que el tearDown intente borrarlo
            quadTest = null;
        });
    }

    /**
     * Limpieza tras cada prueba: elimina el quad de test si se llegó a crear en la base de datos.
     */
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
