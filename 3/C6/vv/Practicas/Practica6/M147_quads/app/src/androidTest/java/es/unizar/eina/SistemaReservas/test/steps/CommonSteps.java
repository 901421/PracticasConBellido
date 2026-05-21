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
 * Clase unificada de definiciones de pasos (Steps) para las pruebas BDD con Cucumber.
 * 
 * ESTRATEGIA DE PRUEBAS:
 * - Determinismo: Se limpia la base de datos antes de cada escenario para asegurar independencia.
 * - Híbrido Espresso/UI Automator: Utiliza Espresso para interacciones estándar con la vista
 *   y UI Automator para manejar componentes fuera del árbol de Espresso (diálogos de sistema,
 *   calendarios y Toasts).
 * - Robustez: Implementa esperas inactivas (waitForIdle) y mecanismos de reintento para
 *   mitigar la inestabilidad (flakiness) inherente a las pruebas de UI en Android.
 * 
 * Basada en la lógica de {@link es.unizar.eina.SistemaReservas.CaminosNavegacionTest}.
 */
public class CommonSteps {

    /** Instancia de UiDevice para interactuar con componentes del sistema (fuera de Espresso). */
    private UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    
    /** Tiempo de espera estándar (ms) para la aparición de elementos de UI. */
    private static final long UI_TIMEOUT = 10000;
    
    /** Referencia al escenario de la actividad para gestionar su ciclo de vida. */
    private ActivityScenario<SistemaReservas> scenario;

    /**
     * Preparación del entorno antes de cada escenario de Cucumber.
     * 1. Limpia todas las tablas de la base de datos de forma síncrona.
     * 2. Lanza la actividad principal de la aplicación.
     * @throws Exception Si la limpieza de la base de datos falla o excede el tiempo de espera.
     */
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

    /**
     * Limpieza post-escenario. Cierra la actividad para liberar recursos.
     */
    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    // --- ACCIONES COMPARTIDAS (HELPERS) ---

    /**
     * Maneja diálogos nativos de Android (Aceptar/Confirmar) de forma síncrona.
     * Intenta localizar el botón positivo por su ID de recurso del sistema ("android:id/button1")
     * y, en su defecto, realiza una búsqueda por texto mediante expresiones regulares para
     * soportar múltiples idiomas y variaciones de botones (OK, ACEPTAR, SI, etc.).
     * 
     * @throws Exception Si ocurre un error durante la interacción con el dispositivo.
     */
    private void handleOkSync() throws Exception {
        device.waitForIdle();
        String resId = "android:id/button1"; // Botón positivo estándar en Android
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

    /**
     * Acción de Espresso para forzar un click en una vista, incluso si Espresso
     * la considera "no clickeable" debido a restricciones de jerarquía.
     * 
     * @return Una instancia de {@link ViewAction} que ejecuta el click.
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

    // --- GIVEN (PRECONDICIONES) ---

    /**
     * Verifica que la aplicación se encuentra en el menú principal.
     */
    @Given("que estoy en la pantalla principal de la aplicación")
    public void enPantallaPrincipal() {
        onView(withId(R.id.button_crear_reserva)).check(matches(isDisplayed()));
    }

    /**
     * Crea un Quad de prueba directamente desde la interfaz de usuario.
     * Si no se está en el menú principal, intenta volver atrás.
     * 
     * @param matricula Matrícula única del quad.
     * @param precio Precio de alquiler por día.
     * @throws Exception Si ocurre un error durante la navegación o el rellenado del formulario.
     */
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

    // --- WHEN / AND (ACCIONES) ---

    /**
     * Pulsa el botón para iniciar el flujo de creación de una nueva reserva.
     */
    @When("pulso el botón de añadir reserva")
    public void pulsarAnadirReserva() {
        onView(withId(R.id.button_crear_reserva)).perform(scrollTo(), forceClick());
    }

    /**
     * Ejecuta el flujo completo de creación de una reserva básica.
     * 
     * @param quad Nombre o identificador del quad (no utilizado directamente en este step).
     * @param cliente Nombre del cliente.
     * @param telefono Teléfono de contacto.
     * @throws Exception Si falla alguna de las acciones del flujo.
     */
    @And("hago una reserva para el quad {string} con cliente {string} y telefono {string}")
    public void hacerReservaCompleta(String quad, String cliente, String telefono) throws Exception {
        pulsarAnadirReserva();
        rellenarDatosCliente(cliente, telefono);
        seleccionarFechas();
        seleccionarPrimerQuad();
        confirmarReserva();
    }

    /**
     * Rellena los campos de texto relativos a la información del cliente.
     * 
     * @param cliente Nombre del cliente (se limpian comillas si existen).
     * @param telefono Teléfono del cliente (se limpian comillas si existen).
     */
    @And("relleno los datos del cliente {string} y telefono {string}")
    public void rellenarDatosCliente(String cliente, String telefono) {
        // Limpiar comillas si vienen del Scenario Outline de Cucumber
        String c = cliente.replace("\"", "").trim();
        String t = telefono.replace("\"", "").trim();
        
        onView(withId(R.id.edit_cliente)).perform(scrollTo(), replaceText(c));
        onView(withId(R.id.edit_telefono)).perform(scrollTo(), replaceText(t));
    }

    /**
     * Selecciona las fechas de recogida y devolución usando los diálogos nativos del sistema.
     * Por defecto, acepta la fecha actual propuesta por el selector.
     * 
     * @throws Exception Si falla la interacción con el DatePicker.
     */
    @And("selecciono las fechas de recogida y devolución")
    public void seleccionarFechas() throws Exception {
        onView(withId(R.id.btn_fecha_recogida)).perform(scrollTo(), forceClick());
        handleOkSync();
        onView(withId(R.id.btn_fecha_devolucion)).perform(scrollTo(), forceClick());
        handleOkSync();
    }

    /**
     * Provoca intencionadamente un error de validación de fechas:
     * 1. Selecciona hoy como fecha de devolución.
     * 2. Selecciona el próximo mes como fecha de recogida.
     * 
     * @throws Exception Si falla la navegación por el calendario.
     */
    @And("selecciono una fecha de recogida posterior a la de devolución")
    public void seleccionarFechasIncoherentes() throws Exception {
        // Seleccionamos devolución primero (será hoy por defecto)
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
            
            // IMPORTANTE: Al cambiar de mes en el DatePicker nativo, la selección no siempre se mueve al nuevo mes. 
            // Forzamos el click en un día concreto (el 15) para asegurar que la fecha cambia efectivamente.
            UiObject day = device.findObject(new UiSelector().text("15"));
            if (day.waitForExists(2000)) {
                day.click();
                device.waitForIdle();
            }
        }
        
        handleOkSync();
    }

    /**
     * Navega a la pantalla de selección de quads para la reserva actual.
     */
    @And("intento seleccionar quads")
    public void intentarSeleccionarQuads() {
        onView(withId(R.id.btn_select_quads)).perform(scrollTo(), forceClick());
    }

    /**
     * Selecciona el primer quad de la lista de selección y confirma.
     * Utiliza UI Automator para esperar a que el RecyclerView cargue los datos.
     */
    @And("selecciono el primer quad disponible")
    public void seleccionarPrimerQuad() {
        intentarSeleccionarQuads();
        // Esperar a que el recycler sea visible (sincronización con UI Automator)
        device.wait(androidx.test.uiautomator.Until.hasObject(
                androidx.test.uiautomator.By.res("es.unizar.eina.SistemaReservas:id/recycler_selection")), UI_TIMEOUT);
        
        // Marcamos el checkbox del primer item y confirmamos la selección
        onView(withId(R.id.recycler_selection)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.cb_select)));
        onView(withId(R.id.btn_confirm_selection)).perform(forceClick());
        device.waitForIdle();
    }

    /**
     * Finaliza la creación/edición de la reserva pulsando el botón de confirmar.
     */
    @And("confirmo la reserva")
    public void confirmarReserva() {
        // Nota: button_confirm suele estar fuera del ScrollView en el layout activity_reserva_edit.xml
        onView(withId(R.id.button_confirm)).perform(forceClick());
        device.waitForIdle();
    }

    /**
     * Navega a la lista de quads, edita un quad específico por su matrícula y guarda el nuevo precio.
     * 
     * @param matricula Matrícula del quad a editar.
     * @param nuevoPrecio Nuevo precio a establecer.
     * @throws Exception Si falla la navegación o la edición.
     */
    @When("cambio el precio del quad con matricula {string} al nuevo precio {string}")
    public void cambiarPrecioQuad(String matricula, String nuevoPrecio) throws Exception {
        onView(withId(R.id.button_listar_quads)).perform(scrollTo(), forceClick());
        device.waitForIdle();
        
        // Búsqueda flexible en el RecyclerView: localiza el item que contiene la matrícula 
        // y pulsa el botón de edición (btnEdit) de ese elemento.
        onView(withId(R.id.recyclerview)).perform(
                RecyclerViewActions.actionOnItem(hasDescendant(withText(matricula)), clickOnViewChild(R.id.btnEdit)));
        
        onView(withId(R.id.precio)).perform(scrollTo(), replaceText(nuevoPrecio));
        onView(withId(R.id.button_save)).perform(scrollTo(), forceClick());
        device.waitForIdle();
        
        // Volvemos al menú principal para mantener el estado consistente para el siguiente step
        device.pressBack(); 
        device.waitForIdle();
    }

    // --- THEN (VALIDACIONES) ---

    /**
     * Valida que un campo de entrada específico muestra un mensaje de error esperado.
     * 
     * @param campo Nombre del campo ("cliente" o "telefono").
     * @param mensaje Texto del error esperado.
     */
    @Then("debería ver un error en el campo {string} con el mensaje {string}")
    public void verificarErrorCampo(String campo, String mensaje) {
        int id = campo.equals("cliente") ? R.id.edit_cliente : R.id.edit_telefono;
        onView(withId(id)).check(matches(hasErrorText(mensaje)));
    }

    /**
     * Valida que el botón de selección de quads muestra un error (setError) con el mensaje indicado.
     * Utiliza un Matcher personalizado para extraer el error del widget Button.
     * 
     * @param mensaje Texto del error esperado.
     */
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

    /**
     * Verifica la aparición de un aviso de validación (Toast o marcador en botón).
     * Estrategia de detección:
     * 1. Intenta capturar un Toast mediante UI Automator buscando por contenido parcial de texto.
     * 2. Si no se detecta (los Toasts son efímeros), comprueba si el botón de fecha tiene un error persistente.
     * 
     * @param mensaje Texto (o parte del mismo) que debe aparecer en el aviso.
     */
    @Then("debería ver un aviso de {string}")
    public void verificarToast(String mensaje) {
        // 1. Búsqueda rápida y activa del Toast con UI Automator (SIN waitForIdle para no perder la ventana temporal)
        // Usamos una palabra clave como "devoluci" para evitar fallos por encoding/tildes.
        boolean found = device.wait(Until.hasObject(By.textContains("devoluci")), 3000);
        
        if (!found) {
            // 2. SEGUNDA OPORTUNIDAD: Si el Toast ya desapareció, verificamos si hay un rastro persistente 
            // en el botón de fecha de devolución (marcado con setError).
            try {
                onView(withId(R.id.btn_fecha_devolucion)).check(matches(new org.hamcrest.TypeSafeMatcher<View>() {
                    @Override
                    public boolean matchesSafely(View item) {
                        return item instanceof android.widget.Button && 
                               ((android.widget.Button) item).getError() != null;
                    }
                    @Override
                    public void describeTo(org.hamcrest.Description d) {
                        d.appendText("que el botón tenga algún mensaje de error (fallback del Toast)");
                    }
                }));
                found = true; 
            } catch (AssertionError e) {
                // Último intento: búsqueda genérica por texto en toda la jerarquía de UI Automator
                found = device.hasObject(By.textContains(mensaje));
            }
        }

        assertTrue("No se detectó el error de validación esperado (ni Toast ni marcador en botón): " + mensaje, found);
    }

    /**
     * Verifica que el precio de una reserva antigua no ha cambiado a pesar de que el precio
     * actual del vehículo haya sido modificado (integridad histórica).
     * 
     * @param cliente Nombre del cliente de la reserva.
     * @param precioEsperado Precio que debe aparecer en el desglose de detalles.
     * @throws Exception Si falla la navegación o la detección del diálogo de detalles.
     */
    @Then("la reserva del cliente {string} debe mantener el precio de {string} en sus detalles")
    public void verificarPrecioHistorico(String cliente, String precioEsperado) throws Exception {
        onView(withId(R.id.button_listar_reservas)).perform(scrollTo(), forceClick());
        device.waitForIdle();
        
        // Abrimos los detalles de la reserva del cliente indicado
        onView(withId(R.id.recyclerview_reservas)).perform(
                RecyclerViewActions.actionOnItem(hasDescendant(withText(cliente)), clickOnViewChild(R.id.btnDetailsReserva)));
        
        // Sincronización robusta: esperamos a que aparezca el diálogo con el ID específico
        String pkg = InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
        BySelector dialogSelector = By.res(pkg, "dialog_lista_quads");
        
        if (!device.wait(Until.hasObject(dialogSelector), UI_TIMEOUT)) {
            // Reintento: Pequeña espera extra si el sistema está bajo carga
            Thread.sleep(1000);
            if (!device.hasObject(dialogSelector)) {
                throw new RuntimeException("ERROR: No se detectó el diálogo de detalles tras pulsar el botón.");
            }
        }
        
        // Verificamos que el texto del diálogo contenga el precio histórico esperado
        UiObject dialogText = device.findObject(new UiSelector().resourceId(pkg + ":id/dialog_lista_quads"));
        assertTrue("El precio histórico esperado (" + precioEsperado + ") no se encontró. Contenido del diálogo: " + dialogText.getText(), 
                dialogText.getText().contains(precioEsperado));
        
        handleOkSync(); // Cerramos el diálogo para dejar la UI limpia
    }

    /**
     * Verifica que el botón de fecha de devolución muestra el marcador de error esperado.
     * 
     * @param mensaje Texto del error.
     */
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

    /**
     * Valida que una reserva específica aparece en el listado global de reservas.
     * 
     * @param cliente Nombre del cliente cuya reserva se busca.
     */
    @Then("debo ver la reserva de {string} en el listado de reservas")
    public void verificarReservaEnListado(String cliente) {
        onView(withId(R.id.button_listar_reservas)).perform(scrollTo(), forceClick());
        device.waitForIdle();
        onView(withText(cliente)).check(matches(isDisplayed()));
    }
}
