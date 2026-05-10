package es.unizar.eina.SistemaReservas.test.steps;

import android.view.View;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import es.unizar.eina.SistemaReservas.R;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static es.unizar.eina.SistemaReservas.CaminosNavegacionTest.clickOnViewChild;
import static org.hamcrest.Matchers.containsString;

/**
 * Clase unificada de definiciones de pasos (Steps) para Cucumber.
 * Blindada contra PerformException mediante forceClick y scrollTo inteligente.
 */
public class CommonSteps {

    private UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    private static final long UI_TIMEOUT = 3000;

    // --- SHARED ACTIONS ---

    private void handleOkSync() throws Exception {
        UiObject okButton = device.findObject(new UiSelector().textMatches("(?i)OK|ACEPTAR|DONE|CONFIRMAR|CERRAR"));
        if (okButton.waitForExists(UI_TIMEOUT)) {
            okButton.click();
        }
    }

    /**
     * Fuerza el click para evitar fallos de coordenadas en API 16.
     */
    public static ViewAction forceClick() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "force click"; }
            @Override public void perform(UiController uiController, View view) {
                view.performClick();
            }
        };
    }

    // --- GIVEN ---

    @Given("que existe un quad llamado {string} con precio {string}")
    public void crearQuad(String matricula, String precio) throws Exception {
        onView(withId(R.id.button_crear_quad)).perform(scrollTo(), forceClick());
        onView(withId(R.id.matricula)).perform(scrollTo(), replaceText(matricula));
        onView(withId(R.id.precio)).perform(scrollTo(), replaceText(precio));
        onView(withId(R.id.button_save)).perform(scrollTo(), forceClick());
    }

    @Given("que estoy en la pantalla principal de la aplicación")
    public void enPantallaPrincipal() {
        onView(withId(R.id.button_crear_reserva)).check(matches(isDisplayed()));
    }

    // --- WHEN / AND ---

    @When("pulso el botón de añadir reserva")
    public void pulsarAnadirReserva() {
        onView(withId(R.id.button_crear_reserva)).perform(scrollTo(), forceClick());
    }

    @And("hago una reserva para el quad {string} con cliente {string} y telefono {string}")
    public void hacerReservaCompleta(String quad, String cliente, String telefono) throws Exception {
        pulsarAnadirReserva();
        rellenarDatosCliente(cliente, telefono);
        seleccionarFechas();
        seleccionarPrimerQuad();
        confirmarReserva();
    }

    @And("relleno los datos del cliente {string} y telefono {string}")
    public void rellenarDatosCliente(String cliente, String telefono) {
        onView(withId(R.id.edit_cliente)).perform(scrollTo(), replaceText(cliente));
        onView(withId(R.id.edit_telefono)).perform(scrollTo(), replaceText(telefono));
    }

    @And("selecciono las fechas de recogida y devolución")
    public void seleccionarFechas() throws Exception {
        onView(withId(R.id.btn_fecha_recogida)).perform(scrollTo(), forceClick());
        handleOkSync();
        onView(withId(R.id.btn_fecha_devolucion)).perform(scrollTo(), forceClick());
        handleOkSync();
    }

    @And("selecciono el primer quad disponible")
    public void seleccionarPrimerQuad() {
        onView(withId(R.id.btn_select_quads)).perform(scrollTo(), forceClick());
        onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.cb_select)));
        onView(withId(R.id.btn_confirm_selection)).perform(forceClick());
    }

    @And("confirmo la reserva")
    public void confirmarReserva() {
        onView(withId(R.id.button_confirm)).perform(forceClick());
    }

    @When("cambio el precio del quad {string} a {string}")
    public void cambiarPrecioQuad(String quad, String nuevoPrecio) throws Exception {
        onView(withId(R.id.button_listar_quads)).perform(scrollTo(), forceClick());
        onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnEdit)));
        onView(withId(R.id.precio)).perform(scrollTo(), replaceText(nuevoPrecio));
        onView(withId(R.id.button_save)).perform(scrollTo(), forceClick());
        device.pressBack(); 
    }

    // --- THEN ---

    @Then("la reserva del cliente {string} debe mantener el precio de {string} en sus detalles")
    public void verificarPrecioHistorico(String cliente, String precioEsperado) throws Exception {
        onView(withId(R.id.button_listar_reservas)).perform(scrollTo(), forceClick());
        onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDetailsReserva)));
        
        onView(withId(R.id.dialog_lista_quads)).check(matches(withText(containsString(precioEsperado))));
        handleOkSync(); 
    }

    @Then("debo ver la reserva de {string} en el listado de reservas")
    public void verificarReservaEnListado(String cliente) {
        onView(withId(R.id.button_listar_reservas)).perform(scrollTo(), forceClick());
        onView(withText(cliente)).check(matches(isDisplayed()));
    }
}
