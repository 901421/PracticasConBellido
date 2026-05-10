package es.unizar.eina.SistemaReservas.test;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * Runner oficial de Cucumber para Android (Versión 7.x).
 * Cumple con el estándar JUnit 4 para ejecución en Android Instrumentation.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "features",
    glue = "es.unizar.eina.SistemaReservas.test.steps",
    plugin = {"pretty"}
)
public class RunCucumberTest {
    // Clase de entrada para el motor de Cucumber.
}
