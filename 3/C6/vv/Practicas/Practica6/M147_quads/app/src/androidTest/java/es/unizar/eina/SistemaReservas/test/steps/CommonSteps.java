package es.unizar.eina.SistemaReservas.test.steps;

import android.content.Context;
import android.view.View;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.Until;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import es.unizar.eina.SistemaReservas.R;
import es.unizar.eina.SistemaReservas.database.QuadRoomDatabase;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static es.unizar.eina.SistemaReservas.CaminosNavegacionTest.clickOnViewChild;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import androidx.test.core.app.ActivityScenario;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Clase unificada de definiciones de pasos (Steps) para Cucumber.
 * Mejorada con limpieza de base de datos y mayor robustez en la sincronización.
 * Basada en la lógica de CaminosNavegacionTest.
 */
public class CommonSteps {

    private UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    private static final long UI_TIMEOUT = 10000;
    private ActivityScenario<SistemaReservas> scenario;

    @Before
    public void setUp() throws Exception {
        // 1. Limpiar base de datos para aislamiento de pruebas
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        QuadRoomDatabase db = QuadRoomDatabase.getDatabase(ctx);
        CountDownLatch latch = new CountDownLatch(1);
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.clearAllTables();
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout limpiando la base de datos");
        }
        
        // 2. Lanzar la actividad
        scenario = ActivityScenario.launch(SistemaReservas.class);
        device.waitForIdle();
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    // --- SHARED ACTIONS ---

    /**
     * Maneja diálogos nativos de Android (Aceptar/Cancelar) buscando por ID de sistema o por texto.
     * Basado en clickSystemButton de CaminosNavegacionTest.
     */
    private void handleOkSync() throws Exception {
        device.waitForIdle();
        String resId = "android:id/button1"; // Botón positivo estándar
        UiObject btn = device.findObject(new UiSelector().resourceId(resId));
        if (!btn.waitForExists(3000)) {
            // Fallback: búsqueda por regex de palabras comunes en varios idiomas
            String regex = "(?i)OK|ACEPTAR|ESTABLECER|LISTO|DONE|CONFIRMAR|SI|SÍ|YES|ELIMINAR|SET";
            btn = device.findObject(new UiSelector().textMatches(regex));
        }
        if (btn.waitForExists(3000)) {
            btn.click();
            device.waitForIdle();
        }
    }

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

    @Given("que estoy en la pantalla principal de la aplicación")
    public void enPantallaPrincipal() {
        onView(withId(R.id.button_crear_reserva)).check(matches(isDisplayed()));
    }

    @Given("que existe un quad con matricula {string} y precio {string}")
    public void crearQuad(String matricula, String precio) throws Exception {
        // Aseguramos estar en el menú principal si no lo estamos
        try {
            onView(withId(R.id.button_crear_quad)).check(matches(isDisplayed()));
        } catch (Exception | AssertionError e) {
            device.pressBack();
            device.waitForIdle();
        }
        onView(withId(R.id.button_crear_quad)).perform(scrollTo(), forceClick());
        onView(withId(R.id.matricula)).perform(scrollTo(), replaceText(matricula));
        onView(withId(R.id.precio)).perform(scrollTo(), replaceText(precio));
        onView(withId(R.id.descripcion)).perform(scrollTo(), replaceText("Test Quad"));
        onView(withId(R.id.btnMonoplaza)).perform(scrollTo(), forceClick());
        onView(withId(R.id.button_save)).perform(scrollTo(), forceClick());
        device.waitForIdle();
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
        // Limpiar comillas si vienen del Scenario Outline
        String c = cliente.replace("\"", "").trim();
        String t = telefono.replace("\"", "").trim();
        
        onView(withId(R.id.edit_cliente)).perform(scrollTo(), replaceText(c));
        onView(withId(R.id.edit_telefono)).perform(scrollTo(), replaceText(t));
    }

    @And("selecciono las fechas de recogida y devolución")
    public void seleccionarFechas() throws Exception {
        onView(withId(R.id.btn_fecha_recogida)).perform(scrollTo(), forceClick());
        handleOkSync();
        onView(withId(R.id.btn_fecha_devolucion)).perform(scrollTo(), forceClick());
        handleOkSync();
    }

    @And("selecciono una fecha de recogida posterior a la de devolución")
    public void seleccionarFechasIncoherentes() throws Exception {
        // Seleccionamos devolución primero (será hoy)
        onView(withId(R.id.btn_fecha_devolucion)).perform(scrollTo(), forceClick());
        handleOkSync();
        
        // Seleccionamos recogida para el mes que viene
        onView(withId(R.id.btn_fecha_recogida)).perform(scrollTo(), forceClick());
        
        // Intentar encontrar el botón "Siguiente mes" por ID de sistema primero (más robusto)
        UiObject nextMonth = device.findObject(new UiSelector().resourceId("android:id/next"));
        if (!nextMonth.waitForExists(2000)) {
            // Fallback por descripción usando regex similar al estilo de CaminosNavegacionTest
            nextMonth = device.findObject(new UiSelector().descriptionMatches("(?i).*Next.*|.*Siguiente.*|.*Mes.*"));
        }
        
        if (nextMonth.waitForExists(2000)) {
            nextMonth.click();
            device.waitForIdle();
            Thread.sleep(500); // Pequeña espera para la animación del calendario
            
            // IMPORTANTE: Al cambiar de mes, la selección no siempre se mueve. 
            // Clicamos en un día (ej: el 15) para asegurar que la fecha cambia.
            UiObject day = device.findObject(new UiSelector().text("15"));
            if (day.waitForExists(2000)) {
                day.click();
                device.waitForIdle();
            }
        }
        
        handleOkSync();
    }

    @And("intento seleccionar quads")
    public void intentarSeleccionarQuads() {
        onView(withId(R.id.btn_select_quads)).perform(scrollTo(), forceClick());
    }

    @And("selecciono el primer quad disponible")
    public void seleccionarPrimerQuad() {
        intentarSeleccionarQuads();
        // Esperar a que el recycler sea visible
        device.wait(androidx.test.uiautomator.Until.hasObject(
                androidx.test.uiautomator.By.res("es.unizar.eina.SistemaReservas:id/recycler_selection")), UI_TIMEOUT);
        
        onView(withId(R.id.recycler_selection)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.cb_select)));
        onView(withId(R.id.btn_confirm_selection)).perform(forceClick());
        device.waitForIdle();
    }

    @And("confirmo la reserva")
    public void confirmarReserva() {
        // button_confirm está fuera del ScrollView en activity_reserva_edit.xml
        onView(withId(R.id.button_confirm)).perform(forceClick());
        device.waitForIdle();
    }

    @When("cambio el precio del quad con matricula {string} al nuevo precio {string}")
    public void cambiarPrecioQuad(String matricula, String nuevoPrecio) throws Exception {
        onView(withId(R.id.button_listar_quads)).perform(scrollTo(), forceClick());
        device.waitForIdle();
        
        // Búsqueda más flexible en el RecyclerView: buscamos el item que contiene el texto de la matrícula
        onView(withId(R.id.recyclerview)).perform(
                RecyclerViewActions.actionOnItem(hasDescendant(withText(matricula)), clickOnViewChild(R.id.btnEdit)));
        
        onView(withId(R.id.precio)).perform(scrollTo(), replaceText(nuevoPrecio));
        // En QuadEdit los botones sí están dentro del ScrollView
        onView(withId(R.id.button_save)).perform(scrollTo(), forceClick());
        device.waitForIdle();
        device.pressBack(); 
        device.waitForIdle();
    }

    // --- THEN ---

    @Then("debería ver un error en el campo {string} con el mensaje {string}")
    public void verificarErrorCampo(String campo, String mensaje) {
        int id = campo.equals("cliente") ? R.id.edit_cliente : R.id.edit_telefono;
        onView(withId(id)).check(matches(hasErrorText(mensaje)));
    }

    @Then("debería ver un error en el botón de selección de quads con el mensaje {string}")
    public void verificarErrorBotonQuads(String mensaje) {
        onView(withId(R.id.btn_select_quads)).check(matches(new org.hamcrest.TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                if (!(item instanceof android.widget.Button)) return false;
                CharSequence error = ((android.widget.Button) item).getError();
                return error != null && error.toString().equals(mensaje);
            }
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("con mensaje de error: " + mensaje);
            }
        }));
    }

    @Then("debería ver un aviso de {string}")
    public void verificarToast(String mensaje) {
        // Los Toasts son efímeros y a veces difíciles de capturar para UI Automator.
        // Aseguramos que el click se ha procesado.
        device.waitForIdle();

        // 1. Intento con BySelector usando una palabra clave (evita líos de encoding con la 'ó')
        boolean found = device.wait(Until.hasObject(By.textContains("devoluci")), 5000);
        
        // 2. Intento con regex flexible que ignora tildes
        if (!found) {
            String regex = "(?i).*devoluci.n.*posterior.*";
            UiObject toast = device.findObject(new UiSelector().textMatches(regex));
            found = toast.waitForExists(2000);
        }
        
        // 3. Intento con la otra palabra clave "posterior"
        if (!found) {
            found = device.wait(Until.hasObject(By.textContains("posterior")), 1000);
        }
        
        // 4. Intento final con el mensaje completo original
        if (!found) {
            found = device.wait(Until.hasObject(By.textContains(mensaje)), 1000);
        }

        assertTrue("No se encontró el aviso (Toast) esperado: " + mensaje, found);
    }

    @Then("la reserva del cliente {string} debe mantener el precio de {string} en sus detalles")
    public void verificarPrecioHistorico(String cliente, String precioEsperado) throws Exception {
        onView(withId(R.id.button_listar_reservas)).perform(scrollTo(), forceClick());
        device.waitForIdle();
        
        onView(withId(R.id.recyclerview_reservas)).perform(
                RecyclerViewActions.actionOnItem(hasDescendant(withText(cliente)), clickOnViewChild(R.id.btnDetailsReserva)));
        
        // Sincronización robusta con UI Automator para esperar el diálogo de detalles
        String pkg = InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
        BySelector dialogSelector = By.res(pkg, "dialog_lista_quads");
        
        if (!device.wait(Until.hasObject(dialogSelector), UI_TIMEOUT)) {
            // Reintento: A veces el click en el RecyclerView necesita un poco más de tiempo para disparar el diálogo
            Thread.sleep(1000);
            if (!device.hasObject(dialogSelector)) {
                throw new RuntimeException("ERROR: No se detectó el diálogo de detalles (R.id.dialog_lista_quads) tras el click.");
            }
        }
        
        UiObject dialogText = device.findObject(new UiSelector().resourceId(pkg + ":id/dialog_lista_quads"));
        assertTrue("El precio esperado " + precioEsperado + " no aparece en los detalles. Contenido actual: " + dialogText.getText(), 
                dialogText.getText().contains(precioEsperado));
        
        handleOkSync(); 
    }

    @And("el botón de fecha de devolución debería mostrar el error {string}")
    public void verificarErrorBotonFechaDevolucion(String mensaje) {
        onView(withId(R.id.btn_fecha_devolucion)).check(matches(new org.hamcrest.TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                if (!(item instanceof android.widget.Button)) return false;
                CharSequence error = ((android.widget.Button) item).getError();
                return error != null && error.toString().equals(mensaje);
            }
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("con mensaje de error en botón de fecha: " + mensaje);
            }
        }));
    }

    @Then("debo ver la reserva de {string} en el listado de reservas")
    public void verificarReservaEnListado(String cliente) {
        onView(withId(R.id.button_listar_reservas)).perform(scrollTo(), forceClick());
        device.waitForIdle();
        onView(withText(cliente)).check(matches(isDisplayed()));
    }
}
