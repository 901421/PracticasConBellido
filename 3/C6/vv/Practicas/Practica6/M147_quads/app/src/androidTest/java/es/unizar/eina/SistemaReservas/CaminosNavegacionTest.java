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
 * Suite de pruebas parametrizadas para validar los caminos de navegación Edge-Pair.
 * Utiliza Espresso para interacciones estándar y UiAutomator para diálogos del sistema.
 * Sincronización robusta mediante IdlingResource y eliminación de transiciones redundantes.
 */
@LargeTest
@RunWith(Parameterized.class)
public class CaminosNavegacionTest {

    private UiDevice device; 
    private static final int MAX_RETRIES = 2; 
    private static final long UI_TIMEOUT = 8000; 

    @Rule
    public ActivityScenarioRule<SistemaReservas> activityRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    private final String[] camino;

    public CaminosNavegacionTest(String caminoStr) {
        this.camino = caminoStr.split(",");
    }

    @Before
    public void setUp() throws Exception {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());

        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        es.unizar.eina.SistemaReservas.database.QuadRoomDatabase db = 
                es.unizar.eina.SistemaReservas.database.QuadRoomDatabase.getDatabase(ctx);

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        es.unizar.eina.SistemaReservas.database.QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            db.clearAllTables();
            
            // --- 5 QUADS ---
            db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("0001-TST", true, 40.0, "Quad 1"));
            db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("0002-TST", false, 80.0, "Quad 2"));
            db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("0003-TST", true, 45.0, "Quad 3"));
            db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("0004-TST", false, 85.0, "Quad 4"));
            db.QuadDao().insert(new es.unizar.eina.SistemaReservas.database.Quad("0005-TST", true, 50.0, "Quad 5"));

            // --- 9 RESERVAS (3 por tipo) ---
            // Caducadas (2025)
            db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Caducada 1", 600000001, "2025-01-01", "2025-01-05"));
            db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Caducada 2", 600000002, "2025-02-01", "2025-02-05"));
            db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Caducada 3", 600000003, "2025-03-01", "2025-03-05"));
            
            // Vigentes (2026)
            db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Vigente 1", 600000011, "2026-05-01", "2026-05-10"));
            db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Vigente 2", 600000012, "2026-06-01", "2026-06-10"));
            db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Vigente 3", 600000013, "2026-07-01", "2026-07-10"));
            
            // Previstas (2030)
            db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Prevista 1", 600000021, "2030-01-01", "2030-01-10"));
            db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Prevista 2", 600000022, "2030-02-01", "2030-02-10"));
            db.ReservaDao().insert(new es.unizar.eina.SistemaReservas.database.Reserva("Prevista 3", 600000023, "2030-03-01", "2030-03-10"));

            latch.countDown();
        });
        try {
            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @org.junit.After
    public void tearDown() throws Exception {
        // Desregistramos para evitar contaminación entre suites
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    /** Definición de los caminos de prueba generados para cubrir Pares de Aristas. */
    @Parameterized.Parameters(name = "Path {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"1,13,5,6,5,6b,5,6c,5,15,6,13"},
            {"1,13b,5,15b,6,13b,13,13,13b,13b,13c,5"},
            {"1,13c,13,13c,13b,14,5,6,13c,13c,14,13"},
            {"1,14,13b,21"},
            {"1,21,1,5,6,14,13c,21"},
            {"4,19,19b,11,24,12,10,1,5,6,21"},
            {"4,19,19b,11,24,12,10,2,8,9,8,9b,8,9c,8,11,12,9"},
            {"4,19,19b,11,24,12,10,3,7"},
            {"4,19,19b,11,24,12,10,4,10b"},
            {"4,10b,1,5,6,5,6,5,6,5,6,5,6"},
            {"4,10b,2,16,8,19,9,16,16,16b,8,19b,9"},
            {"4,10b,3,7b"},
            {"4,10b,4,10c"},
            {"4,10c,1,5,6,5,6,5,6,5,6,5,6"},
            {"4,10c,2,16b,16,16c,8,9,16b,16b,16c,16,16d"},
            {"4,10c,3,7c"},
            {"4,10c,4,19b,10b"},
            {"2,8,11,12b,9,16c,16b,16d,8,9,16d,16,16e,8"},
            {"2,8,11,12c,9,16e,16,16f,8,9,16f,16,16g,8"},
            {"2,8,11,20,12,9b,16,16h,8,9,16g,16,17,8"},
            {"2,8,11,20b,12,9c,16,18,8,9,16h,16,22"},
            {"2,8,11,20c,12,11,23,12,19,9b,16b,16e,16b,16f"},
            {"4,19,19b,11,12,10b,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,19b,11,12,10c,1,5,6,5,6,5,6,5,6,5"},
            {"2,8,11,12,19b,9b,16c,16c,16d,16b,16g,16b,16h,16b,17"},
            {"4,19,19b,11,24,12,11,12b,10,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,19b,11,12b,10b,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,19b,11,12b,10c,1,5,6,5,6,5,6,5,6,5"},
            {"2,8,11,12b,11,12,9,17,16,8,9,18,16,8,9"},
            {"2,8,11,12b,19,9c,16b,18,16b,22"},
            {"2,8,11,12b,19b,9c,16c,16e,16c,16f,16b,8,9,22"},
            {"2,8,11,12b,9b,16d,16c,16g,16c,16h,16c,17,16b,8,9"},
            {"2,8,11,12b,9c,16d,16d,16e,16d,16f,16c,18,16c,22"},
            {"4,19,19b,11,24,12,11,12c,10,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,19b,11,12c,10b,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,19b,11,12c,10c,1,5,6,5,6,5,6,5,6,5"},
            {"2,8,11,12c,11,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,12c,19,11,12,9,8,9,8,9,8,9,8"},
            {"2,8,11,12c,19b,19,19,9,8,9,8,9,8,9,8"},
            {"2,8,11,12c,9b,16e,16e,16f,16d,16g,16d,16h,16d,17,16c"},
            {"2,8,11,12c,9c,16e,16g,16e,16h,16e,17,16d,18,16d,22"},
            {"1,13,14,14,21"},
            {"1,13,21,2,16c,8,9,8,9,8,9,8,9"},
            {"3,15,15,7"},
            {"3,15,15b,7"},
            {"1,5,15,6b,13,5,6,5,6,5,6,5,6,5"},
            {"1,5,15,6c,13,5,6,5,6,5,6,5,6,5"},
            {"3,15,7b,1,5,6,5,6,5,6,5,6,5"},
            {"3,15,7c,1,5,6,5,6,5,6,5,6,5"},
            {"3,15b,15,7"},
            {"3,15b,15b,7b"},
            {"1,5,15b,6b,13b,5,6,5,6,5,6,5,6,5"},
            {"1,5,15b,6c,13b,5,6,5,6,5,6,5,6,5"},
            {"3,15b,7c,2,16d,8,9,8,9,8,9,8,9"},
            {"2,16e,18,16e,22"},
            {"2,16f,16e,8,9,8,9,8,9,8,9,8,9"},
            {"2,16f,16f,16g,16f,16h,16f,17,16e,8,9,8,9"},
            {"2,16f,18,16f,22"},
            {"2,16g,16g,16h,16g,17,16f,8,9,8,9,8,9"},
            {"2,16g,18,16g,22"},
            {"2,16h,16h,17,16g,8,9,8,9,8,9,8,9"},
            {"2,16h,18,16h,22"},
            {"2,17,16h,8,9,8,9,8,9,8,9,8,9"},
            {"2,17,17,18,17,22"},
            {"2,18,18,22"},
            {"4,19,19b,11,24,12,19,10,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,10b,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,10c,1,5,6,5,6,5,6,5,6,5"},
            {"4,19,19b,11,24,12,19b,10,1,5,6,5,6,5,6,5,6,5"},
            {"4,19b,10c,1,5,6,5,6,5,6,5,6,5"},
            {"4,19b,19b,10b"},
            {"2,22,1,5,6,5,6,5,6,5,6,5"},
            {"2,8,11,20,12b,9,8,9,8,9,8,9,8,9,8"},
            {"2,8,11,20,12c,9,8,9,8,9,8,9,8,9,8"},
            {"2,8,11,20,20,20b,12b,9,8,9,8,9,8,9,8"},
            {"2,8,11,20,20c,12b,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20,23,12b,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20,24,12b,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20b,12c,9,8,9,8,9,8,9,8,9,8"},
            {"2,8,11,20b,20,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20b,20b,20c,12c,9,8,9,8,9,8,9,8"},
            {"2,8,11,20b,23,12c,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20b,24,12c,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20c,20,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20c,20b,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,20c,20c,23,20,12,9,8,9,8,9,8,9"},
            {"2,8,11,20c,24,20,12,9,8,9,8,9,8,9,8"},
            {"1,21,3,7"},
            {"1,21,4,10b"},
            {"2,22,2,8,9,8,9,8,9,8,9,8,9"},
            {"2,22,3,7"},
            {"2,22,4,10b"},
            {"2,8,11,23,20b,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,23,20c,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,23,23,24,20b,12,9,8,9,8,9,8,9"},
            {"2,8,11,24,20c,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,24,23,12,9,8,9,8,9,8,9,8,9"},
            {"2,8,11,24,24,12,9,8,9,8,9,8,9,8,9"},
            {"1,5,6b,13c,5,6,5,6,5,6,5,6,5,6"},
            {"1,5,6b,14,5,6,5,6,5,6,5,6,5,6"},
            {"1,5,6b,21,1,5,6,5,6,5,6,5,6,5"},
            {"1,5,6c,13c,5,6,5,6,5,6,5,6,5,6"},
            {"1,5,6c,14,5,6,5,6,5,6,5,6,5,6"},
            {"1,5,6c,21,1,5,6,5,6,5,6,5,6,5"},
            {"3,7,1,5,6,5,6,5,6,5,6,5,6"},
            {"3,7,2,8,9,8,9,8,9,8,9,8,9"},
            {"3,7,3,7"},
            {"3,7,4,10b"},
            {"3,7b,2,8,9,8,9,8,9,8,9,8,9"},
            {"3,7b,3,7"},
            {"3,7b,4,10b"},
            {"3,7c,3,7"},
            {"3,7c,4,10b"},
            {"2,8,9b,16f,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9b,16g,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9b,16h,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9b,17,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9b,18,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9b,22,1,5,6,5,6,5,6,5,6,5"},
            {"2,8,9c,16f,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9c,16g,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9c,16h,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9c,17,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9c,18,8,9,8,9,8,9,8,9,8,9"},
            {"2,8,9c,22,1,5,6,5,6,5,6,5,6,5"}
        });
    }

    /** Ejecución secuencial del camino de aristas. */
    @Test
    public void ejecutarCaminoCompleto() throws Exception {
        // Ya no se requiere resetAlMenuFuerte ni sleeps iniciales largos 
        // gracias a IdlingResource y el ciclo de vida de ActivityScenarioRule.
        for (String arista : camino) {
            ejecutarAccionConSincronizacion(arista);
        }
    }

    /** Wrapper para ejecutar la acción de la arista con lógica de reintentos selectiva. */
    private void ejecutarAccionConSincronizacion(String arista) throws Exception {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                mapeoAristaAccion(arista);
                device.waitForIdle(); 
                return;
            } catch (androidx.test.espresso.NoMatchingViewException | androidx.test.espresso.PerformException e) {
                attempts++;
                if (attempts == MAX_RETRIES) throw e;
                Thread.sleep(1000); 
            } catch (Exception e) {
                throw e; // Errores lógicos o asserts fallan al instante
            }
        }
    }

    /** Mapeo centralizado: Convierte el ID de arista en una interacción real con la App. */
    private void mapeoAristaAccion(String arista) throws Exception {
        switch (arista) {
            // --- NAVEGACIÓN INICIAL ---
            case "1": 
                if (device.hasObject(By.res("es.unizar.eina.SistemaReservas:id/toggleGroupQuads"))) return;
                onView(withId(R.id.button_listar_quads)).perform(scrollTo(), click()); 
                break;
            case "2": 
                if (device.hasObject(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"))) return;
                onView(withId(R.id.button_listar_reservas)).perform(scrollTo(), click()); 
                break;
            case "3": 
                onView(withId(R.id.button_crear_quad)).perform(scrollTo(), click()); 
                break;
            case "4": 
                onView(withId(R.id.button_crear_reserva)).perform(scrollTo(), click()); 
                break;

            // --- GESTIÓN DE QUADS (N2) ---
            case "5": 
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/toggleGroupQuads"), "1");
                safeRecyclerViewInteraction(R.id.recyclerview, clickOnViewChild(R.id.btnEdit)); 
                break;
            case "13": 
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/toggleGroupQuads"), "1");
                espressoPerform(withId(R.id.sort_matricula), forceClick()); break;
            case "13b": 
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/toggleGroupQuads"), "1");
                espressoPerform(withId(R.id.sort_tipo), forceClick()); break;
            case "13c": 
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/toggleGroupQuads"), "1");
                espressoPerform(withId(R.id.sort_precio), forceClick()); break;
            case "14": 
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/toggleGroupQuads"), "1");
                safeRecyclerViewInteraction(R.id.recyclerview, clickOnViewChild(R.id.btnDelete));
                handleDestructiveDialog(); 
                break;
            case "21": case "22": 
                device.pressBack(); 
                break;

            // --- FORMULARIO QUAD (N3) ---
            case "6": case "7": // Aristas 6/7: GUARDAR cambios en Quad
                // Matrícula determinista basada en el hash del camino para reproducibilidad
                String matriculaDeter = String.format("T%03d-TST", Math.abs(Arrays.toString(camino).hashCode() % 1000));
                espressoPerform(withId(R.id.matricula), replaceText(matriculaDeter));
                espressoPerform(withId(R.id.precio), replaceText("55.0"));
                espressoPerform(withId(R.id.button_save), forceClick());
                break;
            case "6b": case "7b": 
                espressoPerform(withId(R.id.button_cancel), forceClick()); break;
            case "6c": case "7c": 
                device.pressBack(); break;

            case "15": 
                espressoPerform(withId(R.id.btnMonoplaza), forceClick()); break;
            case "15b": 
                espressoPerform(withId(R.id.btnBiplaza), forceClick()); break;

            // --- GESTIÓN DE RESERVAS (N4) ---
            case "8": 
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"), "2");
                safeRecyclerViewInteraction(R.id.recyclerview_reservas, clickOnViewChild(R.id.btnEditReserva)); 
                break;
            case "16": case "16b": case "16c": case "16d": 
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"), "2");
                espressoPerform(withId(R.id.btn_open_sort), forceClick()); 
                int sid = R.id.option_sort_name;
                if (arista.equals("16b")) sid = R.id.option_sort_phone;
                else if (arista.equals("16c")) sid = R.id.option_sort_date_in;
                else if (arista.equals("16d")) sid = R.id.option_sort_date_out;
                handleBottomSheet(sid); 
                break;
            case "16e": case "16f": case "16g": case "16h": 
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"), "2");
                espressoPerform(withId(R.id.btn_open_filter), forceClick()); 
                int fid = R.id.option_filter_todas;
                if (arista.equals("16e")) fid = R.id.option_filter_previstas;
                else if (arista.equals("16f")) fid = R.id.option_filter_vigentes;
                else if (arista.equals("16g")) fid = R.id.option_filter_caducadas;
                handleBottomSheet(fid); 
                // Seguridad: Validamos que el filtro no ha dejado la lista vacía
                if (!arista.equals("16h")) {
                    verificarRecyclerViewNoVacio(R.id.recyclerview_reservas);
                }
                break;
            case "17": 
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"), "2");
                safeRecyclerViewInteraction(R.id.recyclerview_reservas, clickOnViewChild(R.id.btnDeleteReserva));
                handleDestructiveDialog(); 
                break;
            case "18": 
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"), "2");
                safeRecyclerViewInteraction(R.id.recyclerview_reservas, clickOnViewChild(R.id.btnDetailsReserva));
                
                // Detección dinámica: ¿es diálogo o activity?
                UiObject btnCierre = device.findObject(new UiSelector().textMatches("(?i)CERRAR|CLOSE|OK|ACEPTAR"));
                if (btnCierre.waitForExists(2000)) {
                    btnCierre.click();
                } else {
                    device.pressBack(); // Asumimos Activity si no hay diálogo
                }
                device.waitForIdle();
                break;

            // --- FORMULARIO RESERVA (N5) ---
            case "9": case "10": 
                espressoPerform(withId(R.id.edit_cliente), replaceText("Audit Test"));
                espressoPerform(withId(R.id.edit_telefono), replaceText("600111222"));
                // Validación de integridad: el botón solo habilita si hay quads seleccionados
                onView(withId(R.id.button_confirm)).check(matches(isEnabled()));
                espressoPerform(withId(R.id.button_confirm), forceClick());
                break;
            case "9b": case "10b": 
                espressoPerform(withId(R.id.button_cancel), forceClick()); break;
            case "9c": case "10c": 
                device.pressBack(); break;
            case "11": 
                onView(withId(R.id.btn_select_quads)).perform(scrollTo(), forceClick()); 
                verificarTransicionASeleccion();
                break;
            case "19": 
                espressoPerform(withId(R.id.btn_fecha_recogida), forceClick()); handleDatePicker(); break;
            case "19b": 
                espressoPerform(withId(R.id.btn_fecha_devolucion), forceClick()); handleDatePicker(); break;

            // --- SELECCIÓN DE QUADS (N6) ---
            case "12": 
                // Validación: no confirmar si el botón está deshabilitado (ningún quad marcado)
                onView(withId(R.id.btn_confirm_selection)).check(matches(isEnabled()));
                espressoPerform(withId(R.id.btn_confirm_selection), forceClick());
                break;
            case "12b": 
                espressoPerform(withId(R.id.btn_cancel_selection), forceClick()); break;
            case "12c": 
                device.pressBack(); break; 
            case "20": case "20b": case "20c": 
                int selSortId = R.id.sort_matricula;
                if (arista.equals("20b")) selSortId = R.id.sort_tipo;
                else if (arista.equals("20c")) selSortId = R.id.sort_precio;
                espressoPerform(withId(selSortId), forceClick());
                break;
            case "23": 
                onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btn_details_selection)));
                handleInfoDialog();
                break;
            case "24": 
                onView(withId(R.id.recycler_selection)).perform(
                        RecyclerViewActions.actionOnItemAtPosition(0, setChecked(R.id.cb_select, true)));
                
                // Espera activa a que el botón se habilite tras el marcado (asincronía UI)
                if (!device.wait(Until.hasObject(By.res("es.unizar.eina.SistemaReservas:id/btn_cascos_popup").enabled(true)), 3000)) {
                    throw new RuntimeException("ERROR ARISTA 24: El botón de cascos no se habilitó tras seleccionar el Quad.");
                }

                onView(withId(R.id.recycler_selection)).perform(
                        RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btn_cascos_popup)));

                // Regex robusto para cualquier variante de "Casco"
                UiObject op = device.findObject(new UiSelector().textMatches("(?i).*Casco.*"));
                if (op.waitForExists(2000)) {
                    op.click();
                } else {
                    throw new RuntimeException("ERROR ARISTA 24: No se encontraron opciones en el diálogo de cascos.");
                }
                break;

            default: throw new IllegalArgumentException("Arista Desconocida en Switch: " + arista);
        }
    }

    /** Verifica que la navegación a selección fue exitosa y tiene datos. */
    private void verificarTransicionASeleccion() {
        if (!device.wait(Until.hasObject(By.res("es.unizar.eina.SistemaReservas:id/recycler_selection")), 3500)) {
            throw new RuntimeException("BLOQUEO NAVEGACIÓN (Arista 11): Revisa fechas (19/19b).");
        }
        verificarRecyclerViewNoVacio(R.id.recycler_selection);
    }

    /** Verifica que un RecyclerView tiene al menos un elemento. */
    private void verificarRecyclerViewNoVacio(int resId) {
        onView(withId(resId)).perform(new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "assert list not empty"; }
            @Override public void perform(UiController uiController, View view) {
                androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) view;
                if (rv.getAdapter() == null || rv.getAdapter().getItemCount() == 0) {
                    throw new RuntimeException("LISTA VACÍA en " + view.getResources().getResourceEntryName(resId));
                }
            }
        });
    }

    /** Helper: Asegura que la App está en la pantalla correcta antes de interaccionar. */
    private void asegurarPantalla(BySelector selector, String aristaNav) throws Exception {
        if (!device.hasObject(selector)) {
            // Intentamos navegar
            mapeoAristaAccion(aristaNav); 
            
            // Verificamos si hemos llegado con éxito (espera activa)
            if (!device.wait(Until.hasObject(selector), UI_TIMEOUT)) {
                throw new RuntimeException("ERROR DE NAVEGACIÓN: Se intentó ir a la pantalla '" + aristaNav + 
                        "' pero no se detectó el objeto esperado: " + selector.toString());
            }
        }
    }

    /** Helper: Maneja el diálogo nativo DatePickerDialog de Android. */
    private void handleDatePicker() throws UiObjectNotFoundException, InterruptedException {
        UiObject ok = device.findObject(new UiSelector().textMatches("(?i)OK|ACEPTAR|ESTABLECER|LISTO|DONE|CONFIRMAR"));
        if (ok.waitForExists(4000)) { 
            ok.click(); 
            device.waitForIdle(); 
        }
    }

    /** Maneja diálogos donde se requiere confirmar una acción destructiva (Borrar). */
    private void handleDestructiveDialog() throws InterruptedException {
        UiObject btn = device.findObject(new UiSelector().textMatches("(?i)ELIMINAR|BORRAR|ACEPTAR|OK|CONFIRMAR|SI|SÍ|DELETE"));
        if (btn.waitForExists(3000)) {
            btn.click();
            device.waitForIdle();
        }
    }

    /** Maneja diálogos informativos o de cierre. */
    private void handleInfoDialog() throws InterruptedException {
        UiObject btn = device.findObject(new UiSelector().textMatches("(?i)CERRAR|CLOSE|OK|ACEPTAR|ENTENDIDO"));
        if (btn.waitForExists(3000)) {
            btn.click();
            device.waitForIdle();
        }
    }

    /** Helper: Interactúa con elementos dentro de un BottomSheetDialog de Material Design. */
    private void handleBottomSheet(int resId) throws Exception {
        String name = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceEntryName(resId);
        UiObject opt = device.findObject(new UiSelector().resourceId("es.unizar.eina.SistemaReservas:id/" + name));
        if (opt.waitForExists(3000)) { opt.click(); Thread.sleep(1500); }
        else device.pressBack();
    }

    /** Helper: Sincroniza Espresso con la UI. */
    private void espressoPerform(Matcher<View> matcher, ViewAction action) {
        onView(matcher).perform(action);
    }

    /** ViewAction personalizado para forzar el click ignorando restricciones de jerarquía. */
    public static ViewAction forceClick() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "force click"; }
            @Override public void perform(UiController uiController, View view) { view.performClick(); }
        };
    }

    /** ViewAction para interaccionar con un componente hijo específico dentro de un elemento de lista. */
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

    /** 
     * ViewAction para establecer el estado de un CheckBox (hijo de la vista actual) 
     * de forma idempotente. Solo realiza el clic si el estado actual difiere del deseado.
     */
    public static ViewAction setChecked(final int id, final boolean checked) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "Set checked state idempotently"; }
            @Override public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v instanceof android.widget.CompoundButton) {
                    android.widget.CompoundButton cb = (android.widget.CompoundButton) v;
                    if (cb.isChecked() != checked) {
                        cb.performClick();
                    }
                }
            }
        };
    }

    /** Helper para interaccionar con RecyclerView de forma segura, verificando que no esté vacío. */
    private void safeRecyclerViewInteraction(int recyclerViewId, ViewAction action) {
        onView(withId(recyclerViewId)).check(matches(isDisplayed()));
        onView(withId(recyclerViewId)).perform(new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "check if recycler has items"; }
            @Override public void perform(UiController uiController, View view) {
                if (view instanceof androidx.recyclerview.widget.RecyclerView) {
                    androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) view;
                    if (rv.getAdapter() == null || rv.getAdapter().getItemCount() == 0) {
                        throw new RuntimeException("RecyclerView con ID " + recyclerViewId + " está vacío. Abortando acción.");
                    }
                }
            }
        });
        onView(withId(recyclerViewId)).perform(RecyclerViewActions.actionOnItemAtPosition(0, action));
    }
}