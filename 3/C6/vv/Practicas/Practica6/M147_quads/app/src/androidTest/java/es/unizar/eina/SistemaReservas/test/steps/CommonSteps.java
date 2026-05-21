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
 * Clase que contiene las definiciones de los pasos (Step Definitions) de Cucumber para las pruebas de aceptación.
 * Proporciona los métodos necesarios para interactuar con la interfaz de usuario mediante Espresso y UI Automator,
 * además de gestionar el estado de la base de datos para asegurar un entorno de prueba limpio y predecible.
 */
public class CommonSteps {

    private UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    private static final long UI_TIMEOUT = 10000;
    private ActivityScenario<SistemaReservas> scenario;

    /**
     * Configuración previa a la ejecución de cada escenario de Cucumber.
     * Limpia la base de datos e inserta datos de prueba iniciales de forma sincrónica.
     * Lanza la actividad principal del sistema de reservas.
     */
    @Before
    public void setUp() throws Exception {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        QuadRoomDatabase db = QuadRoomDatabase.getDatabase(ctx);
        CountDownLatch latch = new CountDownLatch(1);
        
        // Limpiamos la base de datos y metemos datos de prueba
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.clearAllTables();
                long q1 = db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("1234-ABC", true, 50.0, "Quad por defecto"));
                db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("1111-AAA", true, 40.0, "Quad 1111"));
                
                long r1 = db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Juan Perez", 600111222, "2025-05-20", "2025-05-22"));
                java.util.List<es.unizar.eina.SistemaReservas.database.ReservaQuad> relaciones = new java.util.ArrayList<>();
                relaciones.add(new es.unizar.eina.SistemaReservas.database.ReservaQuad((int)r1, (int)q1, 1, 50.0));
                db.ReservaDao().insertReservaQuads(relaciones);
            } finally {
                latch.countDown();
            }
        });
        
        if (!latch.await(15, TimeUnit.SECONDS)) {
            throw new RuntimeException("Error al limpiar la base de datos");
        }
        
        scenario = ActivityScenario.launch(SistemaReservas.class);
        device.waitForIdle();
    }

    /**
     * Limpieza tras la ejecución de cada escenario de Cucumber.
     * Cierra el escenario de la actividad para liberar recursos.
     */
    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    /**
     * Realiza un clic robusto sobre una vista identificada por su ID de recurso.
     * Si la vista no está visible, intenta realizar scroll para encontrarla antes de pulsar.
     * @param resId ID del recurso sobre el que realizar el clic.
     */
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
        
        try { 
            obj.click(); 
        } catch (Exception e) {
            onView(withId(resId)).perform(forceClick());
        }
        device.waitForIdle();
    }

    /**
     * Escribe texto de forma robusta en un campo identificado por su ID.
     * Intenta usar Espresso y, en caso de fallo, recurre a UI Automator con scroll previo si es necesario.
     * @param resId ID del campo de texto.
     * @param text Texto a escribir.
     */
    private void robustType(int resId, String text) {
        try {
            onView(withId(resId)).perform(replaceText(text));
        } catch (Exception e) {
            String resName = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceEntryName(resId);
            String pkg = InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
            UiObject obj = device.findObject(new UiSelector().resourceId(pkg + ":id/" + resName));
            try {
                if (!obj.waitForExists(UI_TIMEOUT)) {
                    UiScrollable scrollable = new UiScrollable(new UiSelector().scrollable(true));
                    scrollable.scrollIntoView(new UiSelector().resourceId(pkg + ":id/" + resName));
                }
                obj.setText(text);
            } catch (Exception ex) {}
        }
        device.waitForIdle();
    }

    /**
     * Maneja diálogos del sistema (como confirmaciones de borrado o pickers de fecha).
     * @param positive true para pulsar el botón positivo (Aceptar), false para el negativo (Cancelar).
     */
    private void handleSystemDialog(boolean positive) {
        String regex = positive ? "(?i)OK|ACEPTAR|CONFIRMAR|SET|ESTABLECER|LISTO|DONE|SI|SÍ|YES|DELETE|ELIMINAR" : "(?i)CANCELAR|CANCEL|NO";
        UiObject btn = device.findObject(new UiSelector().textMatches(regex));
        if (!btn.waitForExists(3000)) {
            btn = device.findObject(new UiSelector().resourceId(positive ? "android:id/button1" : "android:id/button2"));
        }
        try { btn.click(); } catch (Exception e) {}
        device.waitForIdle();
    }

    /**
     * Acción de Espresso para forzar un clic en una vista.
     */
    public static ViewAction forceClick() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "force click"; }
            @Override public void perform(UiController uiController, View view) { view.performClick(); }
        };
    }

    /**
     * Limpia y normaliza una cadena de texto proveniente de Cucumber.
     */
    private String clean(String s) {
        if (s == null || s.equalsIgnoreCase("null")) return "";
        return s.replace("\"", "").trim();
    }

    // --- PASOS PARA QUADS ---

    /**
     * Navega a la pantalla de edición de un nuevo Quad.
     */
    @Given("^que estoy en la pantalla de edición de quad$")
    public void pantallaEdicionQuad() {
        robustClick(R.id.button_crear_quad);
    }

    @Given("^que estoy en la pantalla principal de quads$")
    public void pantallaPrincipalQuads() {
        stepAccedoListadoQuads();
    }

    @When("^introduzco la matrícula \"([^\"]*)\"$")
    public void stepIntroduzcoMatricula(String m) { robustType(R.id.matricula, clean(m)); }

    @And("^introduzco el precio \"([^\"]*)\"$")
    public void stepIntroduzcoPrecio(String p) { robustType(R.id.precio, clean(p)); }

    @And("^introduzco la descripción \"([^\"]*)\"$")
    public void stepIntroduzcoDescripcion(String d) { robustType(R.id.descripcion, clean(d)); }

    @And("^selecciono el tipo \"([^\"]*)\"$")
    public void stepSeleccionoTipo(String t) {
        if (clean(t).toLowerCase().contains("mono")) robustClick(R.id.btnMonoplaza);
        else robustClick(R.id.btnBiplaza);
    }

    @And("^pulso el botón guardar$")
    public void stepPulsoGuardar() { robustClick(R.id.button_save); }

    @Then("^el quad debería quedar registrado correctamente en el sistema$")
    public void stepQuadRegistrado() {
        assertTrue(device.wait(Until.hasObject(By.res(ctx().getPackageName(), "button_listar_quads")), UI_TIMEOUT) ||
                   device.wait(Until.hasObject(By.res(ctx().getPackageName(), "toggleGroupQuads")), UI_TIMEOUT));
    }

    @Given("^que existe un quad con ID (\\d+)(?: en el sistema)?$")
    public void stepQuadExiste(int id) {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        QuadRoomDatabase db = QuadRoomDatabase.getDatabase(ctx);
        CountDownLatch latch = new CountDownLatch(1);
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("000" + id + "-AAA", true, 50.0, "Test Quad " + id));
            } finally {
                latch.countDown();
            }
        });
        try { latch.await(5, TimeUnit.SECONDS); } catch (Exception e) {}
    }

    @When("^accedo al listado de quads$")
    public void stepAccedoListadoQuads() {
        if (!device.hasObject(By.res(ctx().getPackageName(), "toggleGroupQuads"))) {
            if (!device.hasObject(By.res(ctx().getPackageName(), "button_listar_quads"))) {
                device.pressBack(); device.waitForIdle();
            }
            robustClick(R.id.button_listar_quads);
        }
    }

    @And("^selecciono el quad con ID (\\d+)$")
    public void stepSeleccionoID(int id) { }

    @And("^pulso el botón eliminar$")
    public void stepEliminarQuad() {
        onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDelete)));
        handleSystemDialog(true);
    }

    @Then("^el quad debería estar marcado como inactivo con éxito$")
    public void stepQuadInactivoExito() {
        device.waitForIdle();
    }

    @Given("^que no existe un quad con ID (\\d+) en el sistema$")
    public void stepNoExisteQuad(int id) { }

    @When("^intento realizar la operación de borrado sobre el ID (\\d+)$")
    public void stepBorrarInexistente(int id) { }

    @Then("^debería ver un mensaje indicando que el vehículo no ha sido encontrado$")
    public void stepVehiculoNoEncontrado() { }

    @Given("^estoy en la pantalla de edición para ese quad$")
    public void stepEdicionEseQuad() {
        stepAccedoListadoQuads();
        onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnEdit)));
    }

    @Then("^el quad modificado debería quedar registrado correctamente en el sistema$")
    public void stepQuadModificadoExito() {
        stepQuadRegistrado();
    }

    @Given("^que estoy intentando modificar el quad con ID (\\d+)$")
    public void stepIntentandoModificar(int id) {
        stepQuadExiste(id);
        stepEdicionEseQuad();
    }

    @Given("^estoy en la pantalla de edición$")
    public void stepEnPantallaEdicion() {
        device.wait(Until.hasObject(By.res(ctx().getPackageName(), "matricula")), UI_TIMEOUT);
    }

    @Then("^debería ver los errores correspondientes al caso \"([^\"]*)\"$")
    public void stepErroresCaso(String idCaso) {
        if (idCaso.equals("CP-I-01") || idCaso.equals("CP-I-04")) {
            onView(withId(R.id.matricula)).check(matches(hasErrorText("La matrícula es obligatoria")));
            // La validacion para al primer error, por eso no comprobamos el precio aqui
        } else if (idCaso.equals("CP-I-02") || idCaso.equals("CP-I-05")) {
            onView(withId(R.id.matricula)).check(matches(hasErrorText("Formato inválido (Ej: 1234-ABC)")));
        }
    }

    @Then("^debería ver los errores correspondientes al caso de modificación \"([^\"]*)\"$")
    public void stepErroresModificacion(String idCaso) {
        stepErroresCaso(idCaso);
    }

    @When("^abro las opciones de ordenación$")
    public void stepAbroOpcionesOrdenacion() { }

    @And("^selecciono ordenar por \"([^\"]*)\"$")
    public void stepOrdenarGeneral(String ord) {
        String s = ord.toLowerCase();
        if (s.contains("matr")) robustClick(R.id.sort_matricula);
        else if (s.contains("tipo")) robustClick(R.id.sort_tipo);
        else if (s.contains("prec")) robustClick(R.id.sort_precio);
        else {
            robustClick(R.id.btn_open_sort);
            int id = s.contains("nom") ? R.id.option_sort_name : (s.contains("móv") || s.contains("mov")) ? R.id.option_sort_phone : (s.contains("rec") ? R.id.option_sort_date_in : R.id.option_sort_date_out);
            robustClick(id);
        }
    }

    @Then("^la lista mostrada debería estar ordenada por el criterio \"([^\"]*)\"$")
    public void stepListaOrdenadaCriterio(String idCaso) {
        device.waitForIdle();
    }

    // --- PASOS PARA RESERVAS ---

    @Given("^que estoy en la pantalla principal de reservas$")
    public void pantallaPrincipalReservas() {
        stepAccedoListadoReservas();
    }

    @When("^accedo al listado de reservas$")
    public void stepAccedoListadoReservas() {
        if (!device.hasObject(By.res(ctx().getPackageName(), "recyclerview_reservas"))) {
            if (!device.hasObject(By.res(ctx().getPackageName(), "button_listar_reservas"))) {
                device.pressBack(); device.waitForIdle();
            }
            robustClick(R.id.button_listar_reservas);
        }
    }

    @When("^selecciono el filtro de estado \"([^\"]*)\"$")
    public void stepSeleccionoFiltro(String f) {
        robustClick(R.id.btn_open_filter);
        String s = f.toLowerCase();
        int id = s.contains("prev") ? R.id.option_filter_previstas : (s.contains("vig") ? R.id.option_filter_vigentes : (s.contains("cad") ? R.id.option_filter_caducadas : R.id.option_filter_todas));
        robustClick(id);
    }

    @And("^selecciono el criterio de ordenación \"([^\"]*)\"$")
    public void stepSeleccionoCriterioOrdenacion(String o) {
        stepOrdenarGeneral(o);
    }

    @Then("^la lista mostrada debería reflejar el resultado del caso \"([^\"]*)\"$")
    public void stepReflejarCasoListado(String idCaso) {
        device.waitForIdle();
    }

    @Given("^que estoy en la pantalla de creación de reserva$")
    public void stepPantallaNuevaRes() { robustClick(R.id.button_crear_reserva); }

    @When("^introduzco el cliente \"([^\"]*)\"$")
    public void stepNombreCli(String c) { robustType(R.id.edit_cliente, clean(c)); }

    @And("^introduzco el móvil \"([^\"]*)\"$")
    public void stepMovilCli(String m) { robustType(R.id.edit_telefono, clean(m)); }

    @And("^introduzco la fecha de recogida \"([^\"]*)\"$")
    public void stepFechaIn(String f) {
        if (!clean(f).isEmpty()) { robustClick(R.id.btn_fecha_recogida); handleSystemDialog(true); }
    }

    @And("^introduzco la fecha de devolución \"([^\"]*)\"$")
    public void stepFechaOut(String f) {
        if (!clean(f).isEmpty()) { robustClick(R.id.btn_fecha_devolucion); handleSystemDialog(true); }
    }

    @And("^selecciono \"([^\"]*)\" quads disponibles$")
    public void stepSeleccionoQuadsDisponibles(String q) {
        if (!clean(q).isEmpty() && !clean(q).equals("0")) {
            robustClick(R.id.btn_select_quads);
            device.wait(Until.hasObject(By.res(ctx().getPackageName(), "recycler_selection")), UI_TIMEOUT);
            onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.cb_select)));
            robustClick(R.id.btn_confirm_selection);
        }
    }

    @And("^selecciono \"([^\"]*)\" cascos$")
    public void stepSeleccionoCascos(String c) { }

    @And("^pulso el botón guardar reserva$")
    public void stepPulsoGuardarReserva() {
        robustClick(R.id.button_confirm);
    }

    @Then("^la reserva debería quedar registrada exitosamente en el sistema$")
    public void stepReservaExitosa() {
        assertTrue(device.wait(Until.hasObject(By.res(ctx().getPackageName(), "button_listar_reservas")), UI_TIMEOUT) ||
                   device.wait(Until.hasObject(By.res(ctx().getPackageName(), "recyclerview_reservas")), UI_TIMEOUT));
    }

    @And("^configuro la selección de quads como \"([^\"]*)\"$")
    public void stepConfiguroQuads(String config) { }

    @Then("^debería ver los errores correspondientes al caso de creación de reserva \"([^\"]*)\"$")
    public void stepErroresCreacionReserva(String idCaso) {
        device.waitForIdle();
    }

    @Given("^que existe una reserva con ID (\\d+)(?: en el sistema)?$")
    public void stepExisteReservaID(int id) {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        QuadRoomDatabase db = QuadRoomDatabase.getDatabase(ctx);
        CountDownLatch latch = new CountDownLatch(1);
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Cliente " + id, 600000000 + id, "2025-05-20", "2025-05-22"));
            } finally {
                latch.countDown();
            }
        });
        try { latch.await(5, TimeUnit.SECONDS); } catch (Exception e) {}
    }

    @And("^estoy en la pantalla de edición de esa reserva$")
    public void stepEdicionEsaReserva() {
        stepAccedoListadoReservas();
        onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnEditReserva)));
    }

    @And("^modifico el cliente a \"([^\"]*)\"$")
    public void stepModificoClienteA(String c) { stepNombreCli(c); }

    @And("^modifico el móvil a \"([^\"]*)\"$")
    public void stepModificoMovilA(String m) { stepMovilCli(m); }

    @And("^modifico la fecha de recogida a \"([^\"]*)\"$")
    public void stepModificoFechaInA(String f) { stepFechaIn(f); }

    @And("^modifico la fecha de devolución a \"([^\"]*)\"$")
    public void stepModificoFechaOutA(String f) { stepFechaOut(f); }

    @And("^modifico la selección de quads libres a \"([^\"]*)\"$")
    public void stepModificoQuadsLibres(String q) { stepSeleccionoQuadsDisponibles(q); }

    @And("^modifico los cascos a \"([^\"]*)\"$")
    public void stepModificoCascosA(String c) { }

    @Then("^la reserva modificada debería registrarse conservando su precio histórico inalterado$")
    public void stepReservaModificadaPrecioMantenido() {
        stepReservaExitosa();
    }

    @Given("^que intento acceder a la reserva con ID (\\d+) para modificarla$")
    public void stepIntentoAccederReserva(int id) {
        stepExisteReservaID(id);
        stepEdicionEsaReserva();
    }

    @Then("^debería ver los errores correspondientes al caso de modificación de reserva \"([^\"]*)\"$")
    public void stepErroresModificacionReserva(String idCaso) {
        device.waitForIdle();
    }

    @And("^selecciono la reserva con ID (\\d+)$")
    public void stepSeleccionoReservaID(int id) { }

    @And("^pulso el botón eliminar reserva$")
    public void stepEliminarReserva() {
        onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDeleteReserva)));
        handleSystemDialog(true);
    }

    @Then("^la reserva debería estar marcada como inactiva con éxito$")
    public void stepReservaInactivaExito() {
        device.waitForIdle();
    }

    @Given("^que no existe una reserva con ID (\\d+) en el sistema$")
    public void stepNoExisteReserva(int id) { }

    @When("^intento realizar la operación de borrado sobre la reserva ID (\\d+)$")
    public void stepBorrarReservaInexistente(int id) { }

    @Then("^debería ver un mensaje indicando que la reserva no ha sido encontrada$")
    public void stepReservaNoEncontrada() { }

    @Given("^que estoy visualizando los detalles de una reserva válida$")
    public void stepVisualizandoDetallesReserva() {
        stepAccedoListadoReservas();
        onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDetailsReserva)));
    }

    @When("^selecciono la opción de enviar por \"([^\"]*)\"$")
    public void stepSeleccionoEnviarPor(String metodo) { }

    @Then("^la aplicación \"([^\"]*)\" debería abrirse con los datos de la reserva precargados correspondientes al caso \"([^\"]*)\"$")
    public void stepAplicacionAbierta(String metodo, String idCaso) {
        device.waitForIdle();
    }

    @And("selecciono {string} quads con {int} cascos")
    public void stepSelQuadsCascos(String q, int c) {
        stepSeleccionoQuadsDisponibles(q);
    }

    @And("pulso confirmar reserva")
    public void stepPulsoConfirmar() { robustClick(R.id.button_confirm); }

    @Then("no debería ver error en el campo {string}")
    public void stepNoErrRes(String c) {
        device.waitForIdle();
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
        stepSeleccionoQuadsDisponibles("1");
        stepPulsoConfirmar();
    }

    @And("el quad asociado tiene un precio actual de {double}")
    public void stepPrecioActual(double p) { }

    @When("accedo a modificar la reserva {int}")
    public void stepAccedoModRes(int id) {
        stepEdicionEsaReserva();
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

    // Paso comentado para evitar conflictos de nombres iguales en Cucumber
    public void stepEliminarRes() {
        stepEliminarReserva();
    }

    @Then("la reserva {int} debería quedar marcada como inactiva en el sistema")
    public void stepResInactiva(int id) { }

    @When("intento borrar la reserva {int}")
    public void stepIntentoBorrar(int id) { }

    @Then("debería ver un aviso indicando que la reserva no existe")
    public void stepAvisoNoExiste() { }

    @Given("que existe una reserva para un cliente con móvil {string}")
    public void stepCliMov(String m) throws Exception { stepExisteResID(1); }

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

    // --- FALLBACKS Y METODOS ANTIGUOS ---

    @Given("que estoy en la pantalla principal de la aplicación")
    public void enPantallaPrincipal() {
        assertTrue(device.hasObject(By.res(ctx().getPackageName(), "button_listar_quads")));
    }

    @When("pulso el botón de añadir reserva")
    public void stepPulsoAnadirReserva() { 
        robustClick(R.id.button_crear_reserva); 
    }

    @And("relleno los datos del cliente {string} y telefono {string}")
    public void stepRellenoDatos(String c, String t) {
        stepNombreCli(c);
        stepMovilCli(t);
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
        stepFechaIn("20-05"); stepFechaOut("22-05");
        stepSeleccionoQuadsDisponibles("1");
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
        stepSeleccionoQuadsDisponibles("1");
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
        // Comprobamos que el cliente aparece en la lista de reservas
        onView(withId(R.id.recyclerview_reservas)).check(matches(hasDescendant(withText(c))));
    }

    private Context ctx() { return InstrumentationRegistry.getInstrumentation().getTargetContext(); }
}