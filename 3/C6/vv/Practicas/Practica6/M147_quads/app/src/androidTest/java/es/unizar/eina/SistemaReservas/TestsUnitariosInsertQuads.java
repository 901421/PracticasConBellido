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

/**
 * Clase que implementa los tests unitarios para la operación de inserción de Quads.
 * Valida el cumplimiento de las reglas de negocio para matrículas (formato, unicidad),
 * precios (no negativos) y tipos de vehículo.
 * Nivel de prueba: Unitario (Capa de Repositorio).
 */
@RunWith(AndroidJUnit4.class)
public class TestsUnitariosInsertQuads {
    private Quad quadTest;

    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule
            = new ActivityScenarioRule<>(SistemaReservas.class);

    /**
     * Verifica la inserción correcta de un Quad con datos válidos.
     */
    @Test
    public void testQuadVálido() {
        scenarioRule.getScenario().onActivity(activity -> {
            // Setup / Given: Creación de un Quad con datos que cumplen todas las reglas
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("1234-QWE", true, 45.0, "Válido");
            
            // Execution / When: Intento de inserción
            long idQ1 = repository.insert(quadTest);
            
            // Verification / Then: Se debe retornar un ID de base de datos válido (> 0)
            assertThat("UNIT QUAD 1: Válido (1234-QWE)", idQ1, is(greaterThan(0L)));
            quadTest.setId((int) idQ1);
        });
    }

    /**
     * Verifica que no se permite la inserción de un Quad si su matrícula ya existe en el sistema.
     */
    @Test
    public void testQuadValidoInsercion2() {
        scenarioRule.getScenario().onActivity(activity -> {
            // Setup / Given: Inserción de un Quad inicial
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("1234-QWE", true, 45.0, "Quad inicial");
            long idQ = repository.insert(quadTest);
            if (idQ > 0) quadTest.setId((int) idQ);
            
            // Execution / When: Intento de insertar otro Quad con la misma matrícula
            Quad quadDuplicado = new Quad("1234-QWE", false, 60.0, "Intento duplicado");
            long idRechazado = repository.insert(quadDuplicado);
            
            // Verification / Then: La inserción debe ser rechazada (-1)
            assertThat("UNIT QUAD 1.2: Inserción rechazada por matrícula existente", idRechazado, is(lessThan(0L)));
        });
    }

    /**
     * Verifica que no se permite insertar un Quad con matrícula vacía.
     */
    @Test
    public void testMatriculaVacia() {
        scenarioRule.getScenario().onActivity(activity -> {
            // Setup / Given: Matrícula vacía
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("", true, 45.0, "Vacio");
            
            // Execution / When: Inserción
            long idQ2 = repository.insert(quadTest);
            
            // Verification / Then: Fallo esperado
            assertThat("UNIT QUAD 2: Matrícula vacía", idQ2, is(lessThan(0L)));
        });
    }

    /**
     * Verifica que no se permite insertar un Quad con matrícula duplicada (usando el mismo objeto).
     */
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

    /**
     * Verifica que la matrícula debe seguir el formato Regex (4 números, un guion y 3 letras).
     */
    @Test
    public void testMatriculaInvalida() {
        scenarioRule.getScenario().onActivity(activity -> {
            // Setup / Given: Formato incorrecto (5 números en vez de 4)
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("11234-BBB", true, 50.0, "Formato mal");
            
            // Execution / When: Inserción
            long idQ4 = repository.insert(quadTest);
            
            // Verification / Then: Fallo esperado por formato inválido
            assertThat("UNIT QUAD 4: Formato matrícula inválido (5 números)", idQ4, is(lessThan(0L)));
        });
    }

    /**
     * Verifica que no se permite insertar un Quad con precio de alquiler negativo.
     */
    @Test
    public void testPrecioNegativo() {
        scenarioRule.getScenario().onActivity(activity -> {
            // Setup / Given: Precio negativo
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad("5555-XXX", true, -1.0, "Negativo");
            
            // Execution / When: Inserción
            long idQ5 = repository.insert(quadTest);
            
            // Verification / Then: Fallo esperado
            assertThat("UNIT QUAD 5: Precio negativo", idQ5, is(lessThan(0L)));
        });
    }

    /**
     * Verifica la inserción correcta de un Quad biplaza.
     */
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

    /**
     * Verifica que la matrícula no puede ser nula.
     */
    @Test
    public void testMatriculaNull() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            quadTest = new Quad(null, true, 45.0, "Vacio");

            long idQ7 = repository.insert(quadTest);
            assertThat("UNIT QUAD 7: Matrícula null rechazada correctamente (debe devolver < 0)", idQ7, is(lessThan(0L)));
        });
    }

    /**
     * Limpieza tras cada test, eliminando el Quad creado para mantener la independencia entre pruebas.
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
