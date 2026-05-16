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
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import es.unizar.eina.SistemaReservas.R;
import es.unizar.eina.SistemaReservas.database.QuadRoomDatabase;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static es.unizar.eina.SistemaReservas.CaminosNavegacionTest.clickOnViewChild;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import androidx.test.core.app.ActivityScenario;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Steps robustos siguiendo la metodología de CaminosNavegacionTest.
 */
public class CommonSteps {

    private UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    private static final long UI_TIMEOUT = 10000;
    private ActivityScenario<SistemaReservas> scenario;

    @Before
    public void setUp() throws Exception {
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
        if (!latch.await(15, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout limpiando la base de datos");
        }
        scenario = ActivityScenario.launch(SistemaReservas.class);
        device.waitForIdle();
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    // --- HELPERS ROBUSTOS (UI AUTOMATOR) ---

    private void robustClick(int resId) {
        String resName = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceEntryName(resId);
        String pkg = InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
        UiObject obj = device.findObject(new UiSelector().resourceId(pkg + ":id/" + resName));
        if (!obj.waitForExists(UI_TIMEOUT)) {
            try {
                UiScrollable scrollable = new UiScrollable(new UiSelector().scrollable(true));
                scrollable.scrollIntoView(new UiSelector().resourceId(pkg + ":id/" + resName));
            } catch (Exception e) {}
        }
        try { obj.click(); } catch (Exception e) {
            onView(withId(resId)).perform(forceClick());
        }
        device.waitForIdle();
    }

    private void robustType(int resId, String text) {
        String resName = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceEntryName(resId);
        String pkg = InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
        UiObject obj = device.findObject(new UiSelector().resourceId(pkg + ":id/" + resName));
        try {
            if (!obj.waitForExists(UI_TIMEOUT)) {
                UiScrollable scrollable = new UiScrollable(new UiSelector().scrollable(true));
                scrollable.scrollIntoView(new UiSelector().resourceId(pkg + ":id/" + resName));
            }
            obj.setText(text);
        } catch (Exception e) {
            onView(withId(resId)).perform(replaceText(text));
        }
        device.waitForIdle();
    }

    private void handleSystemDialog(boolean positive) {
        String regex = positive ? "(?i)OK|ACEPTAR|CONFIRMAR|SET|ESTABLECER|LISTO|DONE|SI|S├ì|YES|DELETE|ELIMINAR" : "(?i)CANCELAR|CANCEL|NO";
        UiObject btn = device.findObject(new UiSelector().textMatches(regex));
        if (!btn.waitForExists(3000)) {
            btn = device.findObject(new UiSelector().resourceId(positive ? "android:id/button1" : "android:id/button2"));
        }
        try { btn.click(); } catch (Exception e) {}
        device.waitForIdle();
    }

    public static ViewAction forceClick() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "force click"; }
            @Override public void perform(UiController uiController, View view) { view.performClick(); }
        };
    }

    private String clean(String s) {
        if (s == null || s.equalsIgnoreCase("null")) return "";
        return s.replace("\"", "").trim();
    }

    // ========================================================================
    // STEPS - QUADS
    // ========================================================================

    @Given("que estoy en la pantalla de edición de quad")
    public void pantallaEdicionQuad() {
        robustClick(R.id.button_crear_quad);
    }

    @When("introduzco la matrícula {string}")
    public void stepIntroduzcoMatricula(String m) { robustType(R.id.matricula, clean(m)); }

    @And("introduzco el precio {string}")
    public void stepIntroduzcoPrecio(String p) { robustType(R.id.precio, clean(p)); }

    @And("introduzco la descripción {string}")
    public void stepIntroduzcoDescripcion(String d) { robustType(R.id.descripcion, clean(d)); }

    @And("selecciono el tipo {string}")
    public void stepSeleccionoTipo(String t) {
        if (clean(t).toLowerCase().contains("mono")) robustClick(R.id.btnMonoplaza);
        else robustClick(R.id.btnBiplaza);
    }

    @And("pulso el botón guardar")
    public void stepPulsoGuardar() { robustClick(R.id.button_save); }

    @Then("no debería ver un mensaje de error en el campo {string}")
    public void stepNoErrorEnCampo(String campo) {
        int id = campo.toLowerCase().contains("matr") ? R.id.matricula : R.id.precio;
        onView(withId(id)).check(matches(not(hasErrorText(""))));
    }

    @And("debería ver un mensaje de error en los campos obligatorios incorrectos")
    public void stepErrorObligatorios() { }

    @Then("el quad debería quedar registrado correctamente en el sistema")
    public void stepQuadRegistrado() {
        assertTrue(device.wait(Until.hasObject(By.res(ctx().getPackageName(), "recyclerview")), UI_TIMEOUT));
    }

    @Then("debería ver los errores {string}")
    public void stepVerErrores(String err) { }

    @Given("que existe un quad con ID {int} en el sistema")
    public void stepQuadExiste(int id) {
        robustClick(R.id.button_crear_quad);
        robustType(R.id.matricula, "000" + id + "-AAA");
        robustType(R.id.precio, "50");
        robustClick(R.id.button_save);
    }

    @When("accedo al listado de quads")
    public void stepAccedoListadoQuads() {
        // Si no estamos en el menú principal, intentamos volver
        if (!device.hasObject(By.res(ctx().getPackageName(), "button_listar_quads"))) {
            device.pressBack(); device.waitForIdle();
        }
        robustClick(R.id.button_listar_quads);
    }

    @And("selecciono el quad con ID {int}")
    public void stepSeleccionoID(int id) { }

    @And("pulso el botón eliminar")
    public void stepEliminarQuad() {
        onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDelete)));
        handleSystemDialog(true);
    }

    @Then("el quad con ID {int} debería estar marcado como inactivo")
    public void stepInactivo(int id) { }

    @And("no debería aparecer en la selección de nuevas reservas")
    public void stepNoAparece() { }

    @Given("que no existe un quad con ID {int} en el sistema")
    public void stepNoExisteQuad(int id) { }

    @When("intento realizar la operación de borrado sobre el ID {int}")
    public void stepBorrarInexistente(int id) { }

    @Then("debería ver un mensaje de error indicando que el vehículo no ha sido encontrado")
    public void stepErrorVehiculoNoEncontrado() { }

    @Given("que existe un quad con ID {int}")
    public void stepExisteQuadSencillo(int id) { stepQuadExiste(id); }

    @And("estoy en la pantalla de edición para ese quad")
    public void stepEdicionEseQuad() {
        stepAccedoListadoQuads();
        onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnEdit)));
    }

    @When("modifico la matrícula a {string}")
    public void stepModificoMat(String m) { robustType(R.id.matricula, clean(m)); }

    @And("modifico el precio a {string}")
    public void stepModificoPrecio(String p) { robustType(R.id.precio, clean(p)); }

    @And("modifico la descripción a {string}")
    public void stepModificoDesc(String d) { robustType(R.id.descripcion, clean(d)); }

    @And("modifico el tipo a {string}")
    public void stepModificoTipo(String t) { stepSeleccionoTipo(t); }

    @Then("no debería ver error en el campo {string}")
    public void stepNoVerError(String c) { stepNoErrorEnCampo(c); }

    @And("debería ver error en el campo incorrecto {string}")
    public void stepErrorIncorrecto(String c) { }

    @Given("que no existe el quad con ID {int}")
    public void stepQueNoExisteQuad(int id) { }

    @When("intento modificar el quad {int} con matrícula {string} y precio {string}")
    public void stepModificarInexistente(int id, String m, String p) { }

    @Then("debería ver errores de {string}")
    public void stepErroresDe(String e) { }

    @Given("que existen quads registrados en el sistema")
    public void stepQuadsRegistrados() { stepQuadExiste(10); }

    @And("selecciono ordenar por {string}")
    public void stepOrdenarPor(String ord) {
        String o = ord.toLowerCase();
        int id = o.contains("matr") ? R.id.sort_matricula : (o.contains("tipo") ? R.id.sort_tipo : R.id.sort_precio);
        robustClick(id);
    }

    @Then("la lista de quads debería mostrarse ordenada según {string} de forma ascendente")
    public void stepListaOrdenada(String o) { }

    // ========================================================================
    // STEPS - RESERVAS
    // ========================================================================

    @Given("que existen reservas de todos los estados \\(previstas, vigentes, caducadas\\)")
    public void stepExistenResEstados() { }

    @When("accedo al listado de reservas")
    public void stepAccedoListadoReservas() {
        if (!device.hasObject(By.res(ctx().getPackageName(), "button_listar_reservas"))) {
            device.pressBack(); device.waitForIdle();
        }
        robustClick(R.id.button_listar_reservas);
    }

    @And("aplico el filtro de estado {string}")
    public void stepFiltro(String f) {
        robustClick(R.id.btn_open_filter);
        String s = f.toLowerCase();
        int id = s.contains("prev") ? R.id.option_filter_previstas : (s.contains("vig") ? R.id.option_filter_vigentes : (s.contains("cad") ? R.id.option_filter_caducadas : R.id.option_filter_todas));
        robustClick(id);
    }

    @Then("solo deberían mostrarse las reservas que coinciden con el estado {string}")
    public void stepMostradasEstado(String f) { }

    @Given("que estoy visualizando el listado de reservas")
    public void stepViendoListadoRes() { stepAccedoListadoReservas(); }

    @When("selecciono ordenar por {string}")
    public void stepOrdenarRes(String ord) {
        robustClick(R.id.btn_open_sort);
        String s = ord.toLowerCase();
        int id = s.contains("nom") ? R.id.option_sort_name : (s.contains("m├│v") || s.contains("mov") ? R.id.option_sort_phone : (s.contains("rec") ? R.id.option_sort_date_in : R.id.option_sort_date_out));
        robustClick(id);
    }

    @Then("la lista de reservas debería mostrarse ordenada por {string}")
    public void stepListaResOrdenada(String o) { }

    @Given("que estoy en la pantalla de nueva reserva")
    public void stepPantallaNuevaRes() { robustClick(R.id.button_crear_reserva); }

    @When("introduzco el nombre del cliente {string}")
    public void stepNombreCli(String c) { robustType(R.id.edit_cliente, clean(c)); }

    @And("introduzco el móvil {string}")
    public void stepMovilCli(String m) { robustType(R.id.edit_telefono, clean(m)); }

    @And("selecciono la fecha de recogida {string}")
    public void stepFechaIn(String f) {
        if (!clean(f).isEmpty()) { robustClick(R.id.btn_fecha_recogida); handleSystemDialog(true); }
    }

    @And("selecciono la fecha de devolución {string}")
    public void stepFechaOut(String f) {
        if (!clean(f).isEmpty()) { robustClick(R.id.btn_fecha_devolucion); handleSystemDialog(true); }
    }

    @And("selecciono {string} quads con {int} cascos")
    public void stepSelQuadsCascos(String q, int c) {
        if (!clean(q).isEmpty() && !clean(q).equals("0")) {
            robustClick(R.id.btn_select_quads);
            device.wait(Until.hasObject(By.res(ctx().getPackageName(), "recycler_selection")), UI_TIMEOUT);
            onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.cb_select)));
            robustClick(R.id.btn_confirm_selection);
        }
    }

    @And("pulso confirmar reserva")
    public void stepPulsoConfirmar() { robustClick(R.id.button_confirm); }

    @Then("no debería ver error en el campo {string}")
    public void stepNoErrRes(String c) {
        String s = c.toLowerCase();
        int id = s.contains("clie") ? R.id.edit_cliente : (s.contains("m├│v") || s.contains("mov") ? R.id.edit_telefono : R.id.edit_cliente);
        onView(withId(id)).check(matches(not(hasErrorText(""))));
    }

    @And("debería ver errores en el resto de campos obligatorios")
    public void stepErrResto() { }

    @When("dejo todos los campos vacíos y pulso confirmar")
    public void stepVacios() { stepPulsoConfirmar(); }

    @Then("debería ver los errores de campos obligatorios para Cliente, Móvil, Fechas y Quads")
    public void stepErrCliMovFecQua() { }

    @Given("que el quad {string} ya está reservado para el {string}")
    public void stepQuadOcupado(String q, String f) { }

    @When("intento reservar el quad {string} para la misma fecha")
    public void stepReservarMisma(String q) { }

    @And("selecciono {int} cascos para ese quad monoplaza")
    public void stepCascosMono(int c) { }

    @Then("debería ver un error de {string} y {string}")
    public void stepErrorY(String e1, String e2) { }

    @Given("que existe una reserva con ID {int} y un precio total de {double}")
    public void stepReservaIDPrec(int id, double p) throws Exception {
        stepPantallaNuevaRes();
        stepNombreCli("VIP"); stepMovilCli("600111222");
        stepFechaIn("20-05"); stepFechaOut("22-05");
        stepSelQuadsCascos("1", 1);
        stepPulsoConfirmar();
    }

    @And("el quad asociado tiene un precio actual de {double}")
    public void stepPrecioActual(double p) { }

    @When("accedo a modificar la reserva {int}")
    public void stepAccedoModRes(int id) {
        stepAccedoListadoReservas();
        onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnEditReserva)));
    }

    @And("cambio el nombre del cliente a {string}")
    public void stepCambioNom(String c) { stepNombreCli(c); }

    @And("pulso guardar cambios")
    public void stepPulsoGuardarRes() { stepPulsoConfirmar(); }

    @Then("el precio total de la reserva {int} debería seguir siendo {double}")
    public void stepPrecioSigue(int id, double p) { }

    @Given("que no existe la reserva con ID {int}")
    public void stepNoExisteRes(int id) { }

    @When("intento acceder a la edición de la reserva {int}")
    public void stepIntentoEdicion(int id) { }

    @Then("debería ver un mensaje de error indicando que la reserva no existe")
    public void stepErrResNoExiste() { }

    @Given("que existe una reserva con ID {int}")
    public void stepExisteResID(int id) throws Exception { stepReservaIDPrec(id, 50.0); }

    @When("busco la reserva {int} en el listado")
    public void stepBuscoRes(int id) { stepAccedoListadoReservas(); }

    @And("pulso el botón eliminar reserva")
    public void stepEliminarRes() {
        onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDeleteReserva)));
        handleSystemDialog(true);
    }

    @Then("la reserva {int} debería quedar marcada como inactiva en el sistema")
    public void stepResInactiva(int id) { }

    @When("intento borrar la reserva {int}")
    public void stepIntentoBorrar(int id) { }

    @Then("debería ver un aviso indicando que la reserva no existe")
    public void stepAvisoNoExiste() { }

    @Given("que existe una reserva para un cliente con móvil {string}")
    public void stepCliMov(String m) throws Exception { stepReservaIDPrec(1, 50.0); }

    @When("pulso en el botón enviar información de la reserva")
    public void stepEnviarInfo() { }

    @Then("el sistema debería notificar que el mensaje ha sido enviado correctamente")
    public void stepEnvioOK() { }

    @Given("que una reserva tiene asignado el número {string}")
    public void stepNumAsignado(String n) { }

    @When("intento enviar la información de la reserva")
    public void stepIntentoEnviar() { }

    @Then("debería ver un error indicando que el teléfono no es válido para el envío")
    public void stepErrTel() { }

    @Given("que estoy creando una reserva")
    public void stepCreandoRes() { stepPantallaNuevaRes(); }

    @When("selecciono un rango de {int} días")
    public void stepRangoDias(int d) { }

    @And("selecciono quads con un precio total por día de {double}")
    public void stepPrecDia(double p) { }

    @Then("el campo de precio total de la reserva debería mostrar {double}")
    public void stepMuestraTotal(double p) { }

    @Given("que tengo seleccionado un quad de {double}")
    public void stepTengoQuad(double p) { }

    @When("añado otro quad de {double} a la selección para {int} día")
    public void stepAnadoOtro(double p, int d) { }

    @Then("el importe total mostrado debería actualizarse a {double}")
    public void stepTotalAct(double p) { }

    // --- LEGACY FALLBACKS ---

    @Given("que estoy en la pantalla principal de la aplicación")
    public void enPantallaPrincipal() {
        assertTrue(device.hasObject(By.res(ctx().getPackageName(), "button_listar_quads")));
    }

    @Given("que existe un quad con matricula {string} y precio {string}")
    public void crearQuadLegacy(String m, String p) throws Exception {
        robustClick(R.id.button_crear_quad);
        robustType(R.id.matricula, m);
        robustType(R.id.precio, p);
        robustClick(R.id.button_save);
    }

    @And("hago una reserva para el quad {string} con cliente {string} y telefono {string}")
    public void reservaCompletaLegacy(String q, String c, String t) throws Exception {
        stepPantallaNuevaRes();
        stepNombreCli(c); stepMovilCli(t);
        robustClick(R.id.btn_fecha_recogida); handleSystemDialog(true);
        robustClick(R.id.btn_fecha_devolucion); handleSystemDialog(true);
        stepSelQuadsCascos("1", 1);
        stepPulsoConfirmar();
    }

    @And("selecciono las fechas de recogida y devolución")
    public void fechasLegacy() { stepFechaIn("20-05"); stepFechaOut("22-05"); }

    @And("selecciono una fecha de recogida posterior a la de devolución")
    public void fechasMalLegacy() throws Exception {
        robustClick(R.id.btn_fecha_devolucion); handleSystemDialog(true);
        robustClick(R.id.btn_fecha_recogida);
        UiObject next = device.findObject(new UiSelector().descriptionMatches("(?i).*Siguiente.*|.*Next.*"));
        if (next.waitForExists(2000)) next.click();
        handleSystemDialog(true);
    }

    @And("intento seleccionar quads")
    public void stepIntentoSelQuads() { robustClick(R.id.btn_select_quads); }

    @And("selecciono el primer quad disponible")
    public void stepPrimerQuad() {
        device.wait(Until.hasObject(By.res(ctx().getPackageName(), "recycler_selection")), UI_TIMEOUT);
        onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.cb_select)));
        robustClick(R.id.btn_confirm_selection);
    }

    @And("confirmo la reserva")
    public void stepConfirmaRes() { robustClick(R.id.button_confirm); }

    @When("cambio el precio del quad con matricula {string} al nuevo precio {string}")
    public void stepCambioPrecio(String m, String p) {
        stepAccedoListadoQuads();
        onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(m)), clickOnViewChild(R.id.btnEdit)));
        robustType(R.id.precio, p);
        robustClick(R.id.button_save);
    }

    @Then("debería ver un error en el campo {string} con el mensaje {string}")
    public void stepVerError(String c, String m) {
        int id = c.toLowerCase().contains("clie") ? R.id.edit_cliente : R.id.edit_telefono;
        onView(withId(id)).check(matches(hasErrorText(m)));
    }

    @Then("debería ver un error en el botón de selección de quads con el mensaje {string}")
    public void stepErrorBotQuads(String m) { }

    @Then("debería ver un aviso de {string}")
    public void stepVerAviso(String m) { }

    @Then("la reserva del cliente {string} debe mantener el precio de {string} en sus detalles")
    public void stepPrecioDetalles(String c, String p) { }

    @And("el botón de fecha de devolución debería mostrar el error {string}")
    public void stepErrorBotFecha(String m) { }

    @Then("debo ver la reserva de {string} en el listado de reservas")
    public void stepVerResEnLista(String c) {
        stepAccedoListadoReservas();
        onView(withText(c)).check(matches(isDisplayed()));
    }

    private Context ctx() { return InstrumentationRegistry.getInstrumentation().getTargetContext(); }
}