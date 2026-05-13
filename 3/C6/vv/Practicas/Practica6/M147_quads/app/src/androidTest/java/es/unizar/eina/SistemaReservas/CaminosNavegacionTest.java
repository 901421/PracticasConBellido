package es.unizar.eina.SistemaReservas;

import android.content.Context;
import android.view.View;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import androidx.test.espresso.IdlingRegistry;
import es.unizar.eina.SistemaReservas.util.EspressoIdlingResource;
import java.util.Arrays;
import java.util.Collection;

import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Suite de pruebas parametrizadas para validar los caminos de navegación "Edge-Pair".
 * 
 * OBJETIVO:
 * Garantizar que todas las transiciones posibles entre pantallas de la aplicación funcionen
 * correctamente siguiendo los caminos críticos definidos en el grafo de navegación.
 * 
 * DISEÑO TÉCNICO:
 * - Determinismo Absoluto: Antes de cada test, se limpia y pre-puebla la base de datos (Room)
 *   para asegurar que el estado inicial sea idéntico en cada ejecución.
 * - Híbrido Espresso/UI Automator: Utiliza Espresso para interacciones rápidas con vistas
 *   y UI Automator para manejar componentes fuera del árbol de Espresso (Diálogos de sistema,
 *   Bottom Sheets, selectores de fecha y navegación de botones físicos).
 * - Sincronización Avanzada: Implementa Espresso Idling Resources para esperar a tareas en
 *   segundo plano y mecanismos de reintento con "Esperas Elásticas" para soportar hardware lento.
 */
@LargeTest
@RunWith(Parameterized.class)
public class CaminosNavegacionTest {

    private UiDevice device; 
    /** Máximo número de reintentos por acción en caso de flakiness (inestabilidad del UI). */
    private static final int MAX_RETRIES = 3; 
    /** Tiempo de espera máximo (ms) para que aparezcan elementos en pantalla. */
    private static final long UI_TIMEOUT = 15000; 

    /** Regla que lanza la actividad principal antes de cada test. */
    @Rule
    public ActivityScenarioRule<SistemaReservas> activityRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    /** Camino de navegación actual (serie de identificadores de aristas). */
    private final String[] camino;

    /** Constructor para la inyección de parámetros de JUnit. */
    public CaminosNavegacionTest(String caminoStr) {
        this.camino = caminoStr.split(",");
    }

    /**
     * Preparación del entorno antes de cada test individual.
     * 1. Inicializa el dispositivo UI Automator.
     * 2. Registra el IdlingResource para sincronización asíncrona.
     * 3. Limpia y pre-puebla la base de datos con datos conocidos.
     */
    @Before
    public void setUp() throws Exception {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // Registro del recurso de espera para que Espresso sepa cuándo la DB está ocupada
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());

        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        es.unizar.eina.SistemaReservas.database.QuadRoomDatabase db = 
                es.unizar.eina.SistemaReservas.database.QuadRoomDatabase.getDatabase(ctx);

        // Uso de CountDownLatch para esperar a que la inicialización de la DB termine en el hilo de fondo
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        es.unizar.eina.SistemaReservas.database.QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                db.clearAllTables();
                // Inserción de Quads de prueba
                long q1 = db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("0001-TST", true, 40.0, "Quad 1"));
                db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("0002-TST", false, 80.0, "Quad 2"));
                db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("0003-TST", true, 45.0, "Quad 3"));
                db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("0004-TST", false, 85.0, "Quad 4"));
                db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("0005-TST", true, 50.0, "Quad 5"));

                // Inserción de Reservas de prueba (Caducadas, Vigentes y Previstas para testear filtros)
                long r1 = db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Caducada 1", 600000001, "2025-01-01", "2025-12-05"));
                long r2 = db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Caducada 2", 600000002, "2025-01-01", "2025-12-05"));
                long r3 = db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Caducada 3", 600000003, "2025-01-01", "2025-12-05"));
                long r4 = db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Vigente 1", 600000011, "2026-01-01", "2026-12-10"));
                long r5 = db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Vigente 2", 600000012, "2026-01-01", "2026-12-10"));
                long r6 = db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Vigente 3", 600000013, "2026-01-01", "2026-12-10"));
                long r7 = db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Prevista 1", 600000021, "2030-01-01", "2030-12-10"));
                long r8 = db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Prevista 2", 600000022, "2030-01-01", "2030-12-10"));
                long r9 = db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Prevista 3", 600000023, "2030-01-01", "2030-12-10"));

                // Vinculación de reservas con quads
                java.util.List<es.unizar.eina.SistemaReservas.database.ReservaQuad> relaciones = new java.util.ArrayList<>();
                long[] reservas = {r1, r2, r3, r4, r5, r6, r7, r8, r9};
                for (long rId : reservas) {
                    relaciones.add(new es.unizar.eina.SistemaReservas.database.ReservaQuad((int)rId, (int)q1, 1, 40.0));
                }
                db.ReservaDao().insertReservaQuads(relaciones);
            } finally {
                latch.countDown();
            }
        });
        
        // Espera de seguridad para asegurar que la DB está lista antes de empezar el test UI
        if (!latch.await(20, java.util.concurrent.TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout en setUp: Base de Datos bloqueada.");
        }
        device.waitForIdle();
    }

    /** Limpieza post-test: Desregistro del IdlingResource. */
    @org.junit.After
    public void tearDown() throws Exception {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    /**
     * Definición de los datos de entrada para las pruebas parametrizadas.
     * Cada cadena representa una secuencia de aristas (acciones) que componen un camino de navegación.
     */
    @Parameterized.Parameters(name = "Path {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"1,13,5,6,5,6b,5,6c,5,15,6,13"}, {"1,13b,5,15b,6,13b,13,13,13b,13b,13c,5"},
            {"1,13c,13,13c,13b,14,5,6,13c,13c,14,13"}, {"1,14,13b,21"},
            {"1,21,1,5,6,14,13c,21"}, {"4,19,19b,11,24,12,10,1,5,6,21"},
            {"4,19,19b,11,24,12,10,2,8,9,8,9b,8,9c,8,11,12,9"}, {"4,19,19b,11,24,12,10,3,7"},
            {"4,19,19b,11,24,12,10,4,10b"}, {"4,10b,1,5,6,5,6,5,6,5,6,5,6"},
            {"4,10b,2,16,8,19,9,16,16,16b,8,19b,9"}, {"4,10b,3,7b"}, {"4,10b,4,10c"},
            {"4,10c,1,5,6,5,6,5,6,5,6,5,6"}, {"4,10c,2,16b,16,16c,8,9,16b,16b,16c,16,16d"},
            {"4,10c,3,7c"}, {"4,10c,4,19b,10b"}, {"2,8,11,12b,9,16c,16b,16d,8,9,16d,16,16e,8"},
            {"2,8,11,12c,9,16e,16,16f,8,9,16f,16,16g,8"}, {"2,8,11,20,12,9b,16,16h,8,9,16g,16,17,8"},
            {"2,8,11,20b,12,9c,16,18,8,9,16h,16,22"}, {"2,8,11,20c,12,11,23,12,19,9b,16b,16e,16b,16f"},
            {"4,19,19b,11,12,10b,1,5,6,5,6,5,6,5,6,5"}, {"4,19,19b,11,12,10c,1,5,6,5,6,5,6,5,6,5"},
            {"2,8,11,12,19b,9b,16c,16c,16d,16b,16g,16b,16h,16b,17"},
            {"4,19,19b,11,24,12,11,12b,10,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,19b,11,12b,10b,1,5,6,5,6,5,6,5,6,5"}, {"4,19,19b,11,12b,10c,1,5,6,5,6,5,6,5,6,5"},
            {"2,8,11,12b,11,12,9,17,16,8,9,18,16,8,9"}, {"2,8,11,12b,19,9c,16b,18,16b,22"},
            {"2,8,11,12b,19b,9c,16c,16e,16c,16f,16b,8,9,22"}, {"2,8,11,12b,9b,16d,16c,16g,16c,16h,16c,17,16b,8,9"},
            {"2,8,11,12b,9c,16d,16d,16e,16d,16f,16c,18,16c,22"},
            {"4,19,19b,11,24,12,11,12c,10,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,19b,11,12c,10b,1,5,6,5,6,5,6,5,6,5"}, {"4,19,19b,11,12c,10c,1,5,6,5,6,5,6,5,6,5"},
            {"2,8,11,12c,11,12,9,8,9,8,9,8,9,8,9"}, {"2,8,11,12c,19,11,12,9,8,9,8,9,8,9,8"},
            {"2,8,11,12c,19b,19,19,9,8,9,8,9,8,9,8"}, {"2,8,11,12c,9b,16e,16e,16f,16d,16g,16d,16h,16d,17,16c"},
            {"2,8,11,12c,9c,16e,16g,16e,16h,16e,17,16d,18,16d,22"}, {"1,13,14,14,21"},
            {"1,13,21,2,16c,8,9,8,9,8,9,8,9"}, {"3,15,15,7"}, {"3,15,15b,7"},
            {"1,5,15,6b,13,5,6,5,6,5,6,5,6,5"}, {"1,5,15,6c,13,5,6,5,6,5,6,5,6,5"},
            {"3,15,7b,1,5,6,5,6,5,6,5,6,5"}, {"3,15,7c,1,5,6,5,6,5,6,5,6,5"},
            {"3,15b,15,7"}, {"3,15b,15b,7b"}, {"1,5,15b,6b,13b,5,6,5,6,5,6,5,6,5"},
            {"1,5,15b,6c,13b,5,6,5,6,5,6,5,6,5"}, {"3,15b,7c,2,16d,8,9,8,9,8,9,8,9"},
            {"2,16e,18,16e,22"}, {"2,16f,16e,8,9,8,9,8,9,8,9,8,9"},
            {"2,16f,16f,16g,16f,16h,16f,17,16e,8,9,8,9"}, {"2,16f,18,16f,22"},
            {"2,16g,16g,16h,16g,17,16f,8,9,8,9,8,9"}, {"2,16g,18,16g,22"},
            {"2,16h,16h,17,16g,8,9,8,9,8,9,8,9"}, {"2,16h,18,16h,22"},
            {"2,17,16h,8,9,8,9,8,9,8,9,8,9"}, {"2,17,17,18,17,22"}, {"2,18,18,22"},
            {"4,19,19b,11,24,12,19,10,1,5,6,5,6,5,6,5,6,5"}, {"4,19,10b,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,10c,1,5,6,5,6,5,6,5,6,5"}, {"4,19,19b,11,24,12,19b,10,1,5,6,5,6,5,6,5,6,5"},
            {"4,19b,10c,1,5,6,5,6,5,6,5,6,5"}, {"4,19b,19b,10b"}, {"2,22,1,5,6,5,6,5,6,5,6,5"},
            {"2,8,11,20,12b,9,8,9,8,9,8,9,8,9,8"}, {"2,8,11,20,12c,9,8,9,8,9,8,9,8,9,8"},
            {"2,8,11,20,20,20b,12b,9,8,9,8,9,8,9,8"}, {"2,8,11,20,20c,12b,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20,23,12b,9,8,9,8,9,8,9,8,9"}, {"2,8,11,20,24,12b,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20b,12c,9,8,9,8,9,8,9,8,9,8"}, {"2,8,11,20b,20,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20b,20b,20c,12c,9,8,9,8,9,8,9,8"}, {"2,8,11,20b,23,12c,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20b,24,12c,9,8,9,8,9,8,9,8,9"}, {"2,8,11,20c,20,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20c,20b,12,9,8,9,8,9,8,9,8,9"}, {"2,8,11,20c,20c,23,20,12,9,8,9,8,9,8,9"},
            {"2,8,11,20c,24,20,12,9,8,9,8,9,8,9,8"}, {"1,21,3,7"}, {"1,21,4,10b"},
            {"2,22,2,8,9,8,9,8,9,8,9,8,9"}, {"2,22,3,7"}, {"2,22,4,10b"},
            {"2,8,11,23,20b,12,9,8,9,8,9,8,9,8,9"}, {"2,8,11,23,20c,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,23,23,24,20b,12,9,8,9,8,9,8,9"}, {"2,8,11,24,20c,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,24,23,12,9,8,9,8,9,8,9,8,9"}, {"2,8,11,24,24,12,9,8,9,8,9,8,9,8,9"},
            {"1,5,6b,13c,5,6,5,6,5,6,5,6,5,6"}, {"1,5,6b,14,5,6,5,6,5,6,5,6,5,6"},
            {"1,5,6b,21,1,5,6,5,6,5,6,5,6,5"}, {"1,5,6c,13c,5,6,5,6,5,6,5,6,5,6"},
            {"1,5,6c,14,5,6,5,6,5,6,5,6,5,6"}, {"1,5,6c,21,1,5,6,5,6,5,6,5,6,5"},
            {"3,7,1,5,6,5,6,5,6,5,6,5,6"}, {"3,7,2,8,9,8,9,8,9,8,9,8,9"}, {"3,7,3,7"},
            {"3,7,4,10b"}, {"3,7b,2,8,9,8,9,8,9,8,9,8,9"}, {"3,7b,3,7"}, {"3,7b,4,10b"},
            {"3,7c,3,7"}, {"3,7c,4,10b"}, {"2,8,9b,16f,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9b,16g,8,9,8,9,8,9,8,9,8,9"}, {"2,8,9b,16h,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9b,17,8,9,8,9,8,9,8,9,8,9"}, {"2,8,9b,18,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9b,22,1,5,6,5,6,5,6,5,6,5"}, {"2,8,9c,16f,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9c,16g,8,9,8,9,8,9,8,9,8,9"}, {"2,8,9c,16h,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9c,17,8,9,8,9,8,9,8,9,8,9"}, {"2,8,9c,18,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9c,22,1,5,6,5,6,5,6,5,6,5"}
        });
    }

    /**
     * Método principal del test. Itera sobre cada arista del camino parametrizado
     * y ejecuta la acción correspondiente.
     */
    @Test
    public void ejecutarCaminoCompleto() throws Exception {
        device.waitForIdle(UI_TIMEOUT);
        for (String arista : camino) {
            ejecutarAccionConSincronizacion(arista);
        }
    }

    /**
     * Ejecutor de acciones con política de reintentos.
     * Espera a que el dispositivo esté inactivo (idle) antes y después de cada acción
     * para mitigar condiciones de carrera en el UI.
     */
    private void ejecutarAccionConSincronizacion(String arista) throws Exception {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                device.waitForIdle(UI_TIMEOUT); 
                mapeoAristaAccion(arista);
                device.waitForIdle(UI_TIMEOUT); 
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts == MAX_RETRIES) throw e;
                // Pequeño retardo de polling antes del reintento para dar tiempo al UI
                device.waitForIdle(2000); 
            }
        }
    }

    /**
     * Valida que tras una acción se haya llegado a la pantalla esperada.
     * Utiliza UI Automator para una verificación robusta fuera del hilo de Espresso.
     */
    private void verificarPantalla(BySelector selector, String nombreArista, String nombrePantalla) {
        device.waitForIdle(UI_TIMEOUT); 
        if (!device.wait(Until.hasObject(selector), UI_TIMEOUT)) {
            device.waitForIdle(2000);
            if (!device.hasObject(selector)) {
                throw new RuntimeException("ERROR NAVEGACIÓN (Arista " + nombreArista + "): No se detectó " + nombrePantalla);
            }
        }
    }

    /** Rellena el formulario de creación/edición de Quad con datos deterministas basados en el test actual. */
    private void completarFormularioQuad() {
        int hash = Math.abs(java.util.Arrays.toString(camino).hashCode() % 10000);
        String matriculaDeter = String.format("%04d-TST", hash);
        onView(withId(R.id.matricula)).perform(replaceText(matriculaDeter));
        onView(withId(R.id.precio)).perform(replaceText("55.0"));
        onView(withId(R.id.descripcion)).perform(replaceText("Audit Test Quad " + hash));
        onView(withId(R.id.btnMonoplaza)).perform(forceClick());
        device.waitForIdle(UI_TIMEOUT);
    }

    /** Rellena el formulario de Reserva. */
    private void completarFormularioReserva(String sufijo) {
        onView(withId(R.id.edit_cliente)).perform(replaceText("Audit " + sufijo));
        onView(withId(R.id.edit_telefono)).perform(replaceText("600111222"));
        device.waitForIdle(UI_TIMEOUT);
    }

    /**
     * Mapeo Lógico: Convierte un identificador de arista en una acción de UI real.
     * Este es el "cerebro" del test que conoce la navegación de la App.
     */
    private void mapeoAristaAccion(String arista) throws Exception {
        String pkg = ctx().getPackageName();
        switch (arista) {
            case "1": // Ir a Lista Quads
                if (device.hasObject(By.res(pkg, "toggleGroupQuads"))) return;
                onView(withId(R.id.button_listar_quads)).perform(scrollTo(), click()); 
                verificarPantalla(By.res(pkg, "toggleGroupQuads"), "1", "Lista Quads");
                break;
            case "2": // Ir a Lista Reservas
                if (device.hasObject(By.res(pkg, "recyclerview_reservas"))) return;
                onView(withId(R.id.button_listar_reservas)).perform(scrollTo(), click()); 
                verificarPantalla(By.res(pkg, "recyclerview_reservas"), "2", "Lista Reservas");
                break;
            case "3": // Botón Crear Quad
                onView(withId(R.id.button_crear_quad)).perform(scrollTo(), click()); 
                verificarPantalla(By.res(pkg, "matricula"), "3", "Alta Quad");
                break;
            case "4": // Botón Crear Reserva
                onView(withId(R.id.button_crear_reserva)).perform(scrollTo(), click()); 
                verificarPantalla(By.res(pkg, "edit_cliente"), "4", "Alta Reserva");
                break;
            case "5": // Editar primer Quad de la lista
                safeRecyclerViewInteraction(R.id.recyclerview, clickOnViewChild(R.id.btnEdit)); 
                verificarPantalla(By.res(pkg, "matricula"), "5", "Edición Quad");
                break;
            case "13": onView(withId(R.id.sort_matricula)).perform(forceClick()); device.waitForIdle(UI_TIMEOUT); break;
            case "13b": onView(withId(R.id.sort_tipo)).perform(forceClick()); device.waitForIdle(UI_TIMEOUT); break;
            case "13c": onView(withId(R.id.sort_precio)).perform(forceClick()); device.waitForIdle(UI_TIMEOUT); break;
            case "14": // Eliminar Quad
                safeRecyclerViewInteraction(R.id.recyclerview, clickOnViewChild(R.id.btnDelete));
                handleDestructiveDialog(); 
                break;
            case "21": // Volver al Menú Principal (Botón Atrás)
                device.pressBack(); 
                device.waitForIdle(UI_TIMEOUT);
                verificarPantalla(By.res(pkg, "button_listar_quads"), "21", "Menú Principal");
                break;
            case "6": // Guardar Formulario Quad
                completarFormularioQuad();
                onView(withId(R.id.button_save)).perform(forceClick());
                verificarPantalla(By.res(pkg, "toggleGroupQuads"), "6", "Lista Quads");
                break;
            case "7": // Guardar Formulario Quad (y volver a menú)
                completarFormularioQuad();
                onView(withId(R.id.button_save)).perform(forceClick());
                verificarPantalla(By.res(pkg, "button_listar_quads"), "7", "Menú Principal");
                break;
            case "6b": // Cancelar Formulario Quad
                onView(withId(R.id.button_cancel)).perform(forceClick()); 
                verificarPantalla(By.res(pkg, "toggleGroupQuads"), "6b", "Lista Quads");
                break;
            case "7b": // Cancelar Formulario Quad
                onView(withId(R.id.button_cancel)).perform(forceClick()); 
                verificarPantalla(By.res(pkg, "button_listar_quads"), "7b", "Menú Principal");
                break;
            case "6c": // Volver Atrás desde Formulario Quad
                device.pressBack(); 
                device.waitForIdle(UI_TIMEOUT);
                verificarPantalla(By.res(pkg, "toggleGroupQuads"), "6c", "Lista Quads");
                break;
            case "7c": // Volver Atrás desde Formulario Quad
                device.pressBack(); 
                device.waitForIdle(UI_TIMEOUT);
                verificarPantalla(By.res(pkg, "button_listar_quads"), "7c", "Menú Principal");
                break;
            case "15": onView(withId(R.id.btnMonoplaza)).perform(forceClick()); device.waitForIdle(UI_TIMEOUT); break;
            case "15b": onView(withId(R.id.btnBiplaza)).perform(forceClick()); device.waitForIdle(UI_TIMEOUT); break;
            case "8": // Editar primera Reserva
                safeRecyclerViewInteraction(R.id.recyclerview_reservas, clickOnViewChild(R.id.btnEditReserva)); 
                verificarPantalla(By.res(pkg, "edit_cliente"), "8", "Edición Reserva");
                break;
            case "16": case "16b": case "16c": case "16d": // Opciones de Ordenación (Bottom Sheet)
                onView(withId(R.id.btn_open_sort)).perform(forceClick()); 
                int sid = arista.equals("16b") ? R.id.option_sort_phone : (arista.equals("16c") ? R.id.option_sort_date_in : (arista.equals("16d") ? R.id.option_sort_date_out : R.id.option_sort_name));
                handleBottomSheet(sid); 
                break;
            case "16e": case "16f": case "16g": case "16h": // Opciones de Filtrado (Bottom Sheet)
                onView(withId(R.id.btn_open_filter)).perform(forceClick()); 
                int fid = arista.equals("16e") ? R.id.option_filter_previstas : (arista.equals("16f") ? R.id.option_filter_vigentes : (arista.equals("16g") ? R.id.option_filter_caducadas : R.id.option_filter_todas));
                handleBottomSheet(fid); 
                break;
            case "17": // Eliminar Reserva
                safeRecyclerViewInteraction(R.id.recyclerview_reservas, clickOnViewChild(R.id.btnDeleteReserva));
                handleDestructiveDialog(); 
                break;
            case "18": // Ver Detalles Reserva (Dialogo info)
                safeRecyclerViewInteraction(R.id.recyclerview_reservas, clickOnViewChild(R.id.btnDetailsReserva));
                UiObject btnC = device.findObject(new UiSelector().textMatches("(?i)CERRAR|CLOSE|OK|ACEPTAR"));
                if (btnC.waitForExists(UI_TIMEOUT)) btnC.click(); else device.pressBack();
                device.waitForIdle(UI_TIMEOUT);
                break;
            case "22": // Volver Atrás desde Lista Reservas
                device.pressBack(); 
                device.waitForIdle(UI_TIMEOUT);
                verificarPantalla(By.res(pkg, "button_listar_reservas"), "22", "Menú Principal");
                break;
            case "9": // Confirmar Reserva
                completarFormularioReserva("E");
                onView(withId(R.id.button_confirm)).perform(forceClick());
                verificarPantalla(By.res(pkg, "recyclerview_reservas"), "9", "Lista Reservas");
                break;
            case "10": // Confirmar Reserva (y volver a menú)
                completarFormularioReserva("A");
                onView(withId(R.id.button_confirm)).perform(forceClick());
                verificarPantalla(By.res(pkg, "button_listar_reservas"), "10", "Menú Principal");
                break;
            case "9b": // Cancelar Reserva
                onView(withId(R.id.button_cancel)).perform(forceClick()); 
                verificarPantalla(By.res(pkg, "recyclerview_reservas"), "9b", "Lista Reservas");
                break;
            case "10b": // Cancelar Reserva
                onView(withId(R.id.button_cancel)).perform(forceClick()); 
                verificarPantalla(By.res(pkg, "button_listar_reservas"), "10b", "Menú Principal");
                break;
            case "9c": // Atrás desde Formulario Reserva
                device.pressBack(); 
                device.waitForIdle(UI_TIMEOUT);
                verificarPantalla(By.res(pkg, "recyclerview_reservas"), "9c", "Lista Reservas");
                break;
            case "10c": // Atrás desde Formulario Reserva
                device.pressBack(); 
                device.waitForIdle(UI_TIMEOUT);
                verificarPantalla(By.res(pkg, "button_listar_reservas"), "10c", "Menú Principal");
                break;
            case "11": // Abrir pantalla selección de Quads para una reserva
                onView(withId(R.id.btn_select_quads)).perform(scrollTo(), forceClick()); 
                verificarTransicionASeleccion();
                break;
            case "19": case "19b": // Selectores de Fecha (Usa UI Automator por ser diálogo de sistema)
                int fId = arista.equals("19") ? R.id.btn_fecha_recogida : R.id.btn_fecha_devolucion;
                UiObject bF = device.findObject(new UiSelector().resourceId(pkg + ":id/" + ctx().getResources().getResourceEntryName(fId)));
                boolean eN = bF.exists() && bF.getText().toUpperCase().contains("SELECCIONAR");
                onView(withId(fId)).perform(forceClick()); 
                handleDatePicker(eN); 
                break;
            case "12": // Confirmar selección de Quads
                device.waitForIdle();
                UiObject cb = device.findObject(new UiSelector().resourceId(pkg + ":id/cb_select").instance(0));
                if (cb.exists() && !cb.isChecked()) cb.click();
                onView(withId(R.id.btn_confirm_selection)).perform(forceClick());
                verificarPantalla(By.res(pkg, "edit_cliente"), "12", "Formulario Reserva");
                break;
            case "12b": // Cancelar selección
                onView(withId(R.id.btn_cancel_selection)).perform(forceClick()); 
                verificarPantalla(By.res(pkg, "edit_cliente"), "12b", "Formulario Reserva");
                break;
            case "12c": // Atrás desde selección
                device.pressBack(); device.waitForIdle(2000);
                if (device.hasObject(By.res(pkg, "recycler_selection"))) device.pressBack();
                device.waitForIdle(UI_TIMEOUT);
                verificarPantalla(By.res(pkg, "edit_cliente"), "12c", "Formulario Reserva");
                break; 
            case "20": case "20b": case "20c": // Ordenación en pantalla de selección
                int sId = arista.equals("20b") ? R.id.sort_tipo : (arista.equals("20c") ? R.id.sort_precio : R.id.sort_matricula);
                onView(withId(sId)).perform(forceClick()); device.waitForIdle(UI_TIMEOUT);
                break;
            case "23": // Ver detalles de un Quad en la lista de selección
                onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btn_details_selection)));
                handleInfoDialog();
                break;
            case "24": // Abrir popup de configuración de cascos/extras
                onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, setChecked(R.id.cb_select, true)));
                onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btn_cascos_popup)));
                UiObject op = device.findObject(new UiSelector().textMatches("(?i).*Casco.*"));
                if (op.waitForExists(UI_TIMEOUT)) op.click();
                device.waitForIdle(UI_TIMEOUT);
                if (device.hasObject(By.res("android:id/content"))) device.pressBack();
                break;
            default: throw new IllegalArgumentException("Unknown edge: " + arista);
        }
    }

    private Context ctx() { return InstrumentationRegistry.getInstrumentation().getTargetContext(); }

    private void verificarTransicionASeleccion() {
        if (!device.wait(Until.hasObject(By.res(ctx().getPackageName(), "recycler_selection")), UI_TIMEOUT)) {
            throw new RuntimeException("ERROR NAVEGACIÓN (Arista 11): No se llegó a Selección.");
        }
    }

    /** Maneja diálogos nativos de Android (Aceptar/Cancelar) buscando por ID de sistema o por texto. */
    private void clickSystemButton(boolean positive) throws UiObjectNotFoundException {
        String resId = positive ? "android:id/button1" : "android:id/button2";
        UiObject btn = device.findObject(new UiSelector().resourceId(resId));
        if (!btn.waitForExists(5000)) {
            // Fallback: búsqueda por regex de palabras comunes en varios idiomas
            String regex = positive ? "(?i)OK|ACEPTAR|ESTABLECER|LISTO|DONE|CONFIRMAR|SI|SÍ|YES|DELETE|ELIMINAR" : "(?i)CANCELAR|CANCEL|NO";
            btn = device.findObject(new UiSelector().textMatches(regex));
        }
        if (btn.waitForExists(5000)) { btn.click(); device.waitForIdle(UI_TIMEOUT); }
    }

    private void handleDatePicker(boolean c) throws UiObjectNotFoundException { clickSystemButton(c); }
    private void handleDestructiveDialog() throws UiObjectNotFoundException { clickSystemButton(true); }
    private void handleInfoDialog() throws UiObjectNotFoundException { clickSystemButton(true); }

    /** Maneja la interacción con Bottom Sheets de Material Design. */
    private void handleBottomSheet(int resId) throws Exception {
        String name = ctx().getResources().getResourceEntryName(resId);
        UiObject opt = device.findObject(new UiSelector().resourceId(ctx().getPackageName() + ":id/" + name));
        if (opt.waitForExists(UI_TIMEOUT)) { 
            opt.click(); 
            // Espera a que el bottom sheet desaparezca
            device.wait(Until.gone(By.res(ctx().getPackageName(), "design_bottom_sheet")), UI_TIMEOUT);
            device.waitForIdle(UI_TIMEOUT);
        } else device.pressBack();
    }

    /** ViewAction personalizada para forzar clicks en elementos que Espresso a veces considera "no clickeables". */
    public static ViewAction forceClick() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "force click"; }
            @Override public void perform(UiController uiController, View view) { view.performClick(); }
        };
    }

    /** ViewAction para hacer click en un hijo específico dentro de un elemento de un RecyclerView. */
    public static ViewAction clickOnViewChild(final int id) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "Click child"; }
            @Override public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) v.performClick();
            }
        };
    }

    /** ViewAction para marcar/desmarcar un CheckBox dentro de un RecyclerView. */
    public static ViewAction setChecked(final int id, final boolean checked) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "Set checked state"; }
            @Override public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v instanceof android.widget.CompoundButton) {
                    android.widget.CompoundButton cb = (android.widget.CompoundButton) v;
                    if (cb.isChecked() != checked) cb.performClick();
                }
            }
        };
    }

    /** Helper para interactuar con el primer elemento de un RecyclerView. */
    private void safeRecyclerViewInteraction(int rvId, ViewAction action) {
        onView(withId(rvId)).perform(RecyclerViewActions.actionOnItemAtPosition(0, action));
    }
}