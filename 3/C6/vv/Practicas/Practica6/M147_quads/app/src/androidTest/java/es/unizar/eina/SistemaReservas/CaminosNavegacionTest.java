package es.unizar.eina.SistemaReservas;

import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import es.unizar.eina.SistemaReservas.database.QuadRoomDatabase;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Suite de Navegación - PUNTO DE CONTROL (Caminos 53-134).
 * Esta versión reanuda la ejecución tras el crash de memoria para verificar el resto.
 */
@LargeTest
@RunWith(Parameterized.class)
public class CaminosNavegacionTest {

    private UiDevice device;
    private static final int MAX_RETRIES = 3;
    private static final long UI_TIMEOUT = 4000;
    private boolean isProvisioning = false;

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
        limpiarBaseDeDatos();
        resetAlMenu();
    }

    private void limpiarBaseDeDatos() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        QuadRoomDatabase db = QuadRoomDatabase.getDatabase(context);
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            db.ReservaDao().deleteAllRelaciones();
            db.ReservaDao().deleteAll();
        });
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
    }

    private void resetAlMenu() throws Exception {
        for (int i = 0; i < 5; i++) {
            if (device.findObject(new UiSelector().resourceId("es.unizar.eina.SistemaReservas:id/logo_app")).exists()) return;
            UiObject btn = device.findObject(new UiSelector().textMatches("(?i)CANCELAR|CERRAR|ELIMINAR|ACEPTAR|OK|BACK|ATRÁS"));
            if (btn.exists()) btn.click();
            else device.pressBack();
            Thread.sleep(800);
        }
    }

    @Parameterized.Parameters(name = "Checkpoint Path {index}: {0}")
    public static Collection<Object[]> data() {
        // LISTA COMPLETA ORIGINAL (PARA EXTRAER SUBCOJUNTO)
        Object[][] todosLosCaminos = new Object[][]{
            {"1,13,5,6,5,6b,5,6c,5,15,6,13"}, {"1,13b,5,15b,6,13b,13,13,13b,13b,13c,5"}, {"1,13c,13,13c,13b,14,5,6,13c,13c,14,13"}, {"1,14,13b,23"}, {"1,23,1,5,6,14,13c,23"},
            {"4,20,20b,11,12,10,1,5,6,23"}, {"4,20,20b,11,12,10,2,8,9b,8,9c,8,20,9b,16,8,20b"}, {"4,20,20b,11,12,10,3,7"}, {"4,20,20b,11,12,10,4,10b"}, {"4,10b,1,5,6,5,6,5,6,5,6,5,6"},
            {"4,10b,2,16,16,16b,8,9b,16b,16,16c,8,9b"}, {"4,10b,3,7b"}, {"4,10b,4,10c"}, {"4,10c,1,5,6,5,6,5,6,5,6,5,6"}, {"4,10c,2,16b,16b,16c,16,16d,8,9b,16c,16b,16d"},
            {"4,10c,3,7c"}, {"4,10c,4,20b,10b"}, {"4,20,20b,11,12b,10b"}, {"4,20,20b,11,12c,10b"}, {"4,20,20b,11,21,12,10b"},
            {"4,20,20b,11,21b,12,10c"}, {"4,20,20b,11,21c,12,11,25,12,20,10"}, {"4,20,20b,11,26,12,20b,10"}, {"2,8,20,20b,11,12,9,8,9b,16d,16,16e,8,9b,16e,16,16f"}, {"2,8,20,20b,11,12,9b,16f,8,9b,16g,8,9b,16h,8,9b,17"},
            {"2,8,20,20b,11,12,9c,16,16g,16,16h,16,17,8,9b,18,8"}, {"4,20,20b,11,12,11,12b,10,1,5,6,5,6,5,6,5,6,5"}, {"4,20,20b,11,12b,10c,1,5,6,5,6,5,6,5,6,5"}, {"4,20,20b,11,12b,11,12,10"}, {"4,20,20b,11,12b,20,10b"},
            {"4,20,20b,11,12b,20b,10c"}, {"2,8,20,20b,11,12b,9,16,18,16,19,24"}, {"2,8,20,20b,11,12b,9b,24"}, {"2,8,20,20b,11,12b,9c,16b,16e,16b,16f,16,24"}, {"4,20,20b,11,12,11,12c,10,1,5,6,5,6,5,6,5,6,5"},
            {"4,20,20b,11,12c,10c,1,5,6,5,6,5,6,5,6,5"}, {"4,20,20b,11,12c,11,12,10"}, {"4,20,20b,11,12c,20,10c"}, {"4,20,20b,11,12c,20b,20,11,12,10"}, {"2,8,20,20b,11,12c,9,16b,16g,16b,16h,16b,17,16,8,9b,8"},
            {"2,8,20,20b,11,12c,9b,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,20,20b,11,12c,9c,16c,16c,16d,16b,18,16b,24"}, {"1,13,14,14,23"}, {"1,13,23,2,16c,16e,16c,16f,16b,8,9b,8,9b"}, {"3,15,15,7"},
            {"3,15,15b,7"}, {"1,5,15,6b,13,5,6,5,6,5,6,5,6,5"}, {"1,5,15,6c,13,5,6,5,6,5,6,5,6,5"}, {"3,15,7b,1,5,6,5,6,5,6,5,6,5"}, {"3,15,7c,1,5,6,5,6,5,6,5,6,5"},
            {"3,15b,15,7"}, {"3,15b,15b,7b"}, {"1,5,15b,6b,13b,5,6,5,6,5,6,5,6,5"}, {"1,5,15b,6c,13b,5,6,5,6,5,6,5,6,5"}, {"3,15b,7c,2,16d,16c,16g,16c,16h,16c,17,16b,8"},
            {"2,16c,18,16c,24"}, {"2,16d,16d,16e,16d,16f,16c,8,9b,8,9b,8,9b"}, {"2,16d,16g,16d,16h,16d,17,16c,8,9b,8,9b,8"}, {"2,16d,18,16d,24"}, {"2,16e,16e,16f,16d,8,9b,8,9b,8,9b,8,9b"},
            {"2,16e,16g,16e,16h,16e,17,16d,8,9b,8,9b,8"}, {"2,16e,18,16e,24"}, {"2,16f,16e,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,16f,16f,16g,16f,16h,16f,17,16e,8,9b,8,9b"}, {"2,16f,18,16f,24"},
            {"2,16g,16g,16h,16g,17,16f,8,9b,8,9b,8,9b"}, {"2,16g,18,16g,24"}, {"2,16h,16h,17,16g,8,9b,8,9b,8,9b,8,9b"}, {"2,16h,18,16h,24"}, {"2,17,16h,8,9b,8,9b,8,9b,8,9b,8,9b"},
            {"2,17,17,18,17,24"}, {"2,18,18,24"}, {"2,24,1,5,6,5,6,5,6,5,6,5"}, {"4,20,20,10b"}, {"2,8,20b,20,9,16c,8,9b,8,9b,8,9b,8,9b,8"},
            {"2,8,20,9c,16d,8,9b,8,9b,8,9b,8,9b,8"}, {"4,20b,20b,10b"}, {"2,8,20,20b,9,16d,8,9b,8,9b,8,9b,8,9b,8"}, {"2,8,20b,9b,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,20b,9c,16e,8,9b,8,9b,8,9b,8,9b,8"},
            {"4,20,20b,11,21,12b,10b"}, {"4,20,20b,11,21,12c,10b"}, {"4,20,20b,11,21,21,21b,12b,10b"}, {"4,20,20b,11,21,21c,12b,10b"}, {"4,20,20b,11,21,25,12b,10b"},
            {"4,20,20b,11,21,26,12b,10b"}, {"4,20,20b,11,21b,12c,10b"}, {"4,20,20b,11,21b,21,12,10"}, {"4,20,20b,11,21b,21b,21c,12c,10b"}, {"4,20,20b,11,21b,25,12c,10b"},
            {"4,20,20b,11,21b,26,12c,10b"}, {"4,20,20b,11,21c,21,12,10"}, {"4,20,20b,11,21c,21b,12,10"}, {"4,20,20b,11,21c,21c,25,21,12,10"}, {"4,20,20b,11,21c,26,21,12,10"},
            {"1,23,3,7"}, {"1,23,4,10b"}, {"2,24,2,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,24,3,7"}, {"2,24,4,10b"},
            {"4,20,20b,11,25,21b,12,10"}, {"4,20,20b,11,25,21c,12,10"}, {"4,20,20b,11,25,25,26,21b,12,10"}, {"4,20,20b,11,26,21c,12,10"}, {"4,20,20b,11,26,25,12,10"},
            {"4,20,20b,11,26,26,12,10"}, {"1,5,6b,13c,5,6,5,6,5,6,5,6,5,6"}, {"1,5,6b,14,5,6,5,6,5,6,5,6,5,6"}, {"1,5,6b,23,1,5,6,5,6,5,6,5,6,5"}, {"1,5,6c,13c,5,6,5,6,5,6,5,6,5,6"},
            {"1,5,6c,14,5,6,5,6,5,6,5,6,5,6"}, {"1,5,6c,23,1,5,6,5,6,5,6,5,6,5"}, {"3,7,1,5,6,5,6,5,6,5,6,5,6"}, {"3,7,2,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"3,7,3,7"},
            {"3,7,4,10b"}, {"3,7b,2,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"3,7b,3,7"}, {"3,7b,4,10b"}, {"3,7c,3,7"},
            {"3,7c,4,10b"}, {"2,8,20,20b,9,16e,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,20,20b,9,16f,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,20,20b,9,16g,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,20,20b,9,16h,8,9b,8,9b,8,9b,8,9b,8,9b"},
            {"2,8,20,20b,9,17,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,20,20b,9,18,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,20,20b,9,24,1,5,6,5,6,5,6,5,6,5"}, {"2,8,9c,16f,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,9c,16g,8,9b,8,9b,8,9b,8,9b,8,9b"},
            {"2,8,9c,16h,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,9c,17,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,9c,18,8,9b,8,9b,8,9b,8,9b,8,9b"}, {"2,8,9c,24,1,5,6,5,6,5,6,5,6,5"}
        };
        
        // REANUDAR DESDE CAMINO 53 (Índice 53 de la lista original)
        return Arrays.asList(Arrays.copyOfRange(todosLosCaminos, 53, todosLosCaminos.length));
    }

    private static int totalCaminosEjecutados = 0;

    @Test
    public void ejecutarCaminoCompleto() throws Exception {
        totalCaminosEjecutados++;
        // Cooldown cada 10 caminos para permitir GC (Garbage Collection)
        if (totalCaminosEjecutados % 10 == 0) {
            Thread.sleep(4000);
        }
        
        Thread.sleep(2000);
        for (String arista : camino) {
            ejecutarAccionConSincronizacion(arista);
        }
    }

    private void ejecutarAccionConSincronizacion(String arista) throws Exception {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                mapeoAristaAccion(arista);
                device.waitForIdle();
                Thread.sleep(600);
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts == MAX_RETRIES) throw e;
                handleSystemDialog();
                Thread.sleep(1200);
                device.waitForIdle();
            }
        }
    }

    private void mapeoAristaAccion(String arista) throws Exception {
        switch (arista) {
            case "1": if (!viewExists("sort_matricula")) clickMenuButton(R.id.button_listar_quads); break;
            case "2": if (!viewExists("recyclerview_reservas")) clickMenuButton(R.id.button_listar_reservas); break;
            case "3": clickMenuButton(R.id.button_crear_quad); break;
            case "4": clickMenuButton(R.id.button_crear_reserva); break;

            case "5": 
                asegurarContenidoLista(R.id.recyclerview, "QUAD");
                onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnEdit))); 
                break;
            case "13": waitAndPerform(withId(R.id.sort_matricula), forceClick()); break;
            case "13b": waitAndPerform(withId(R.id.sort_tipo), forceClick()); break;
            case "13c": waitAndPerform(withId(R.id.sort_precio), forceClick()); break;
            case "14": 
                asegurarContenidoLista(R.id.recyclerview, "QUAD");
                onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDelete)));
                handleSystemDialog();
                break;
            case "23": case "24": pressBackHastaVer(R.id.logo_app); break;

            case "6": case "7":
                waitAndPerform(withId(R.id.matricula), replaceText(String.format("%04d-TST", (int)(Math.random()*9000)+1000)));
                waitAndPerform(withId(R.id.precio), replaceText("55.0"));
                waitAndPerform(withId(R.id.button_save), forceClick());
                esperarCierreDeActividad(R.id.button_save);
                break;
            case "6b": case "7b": waitAndPerform(withId(R.id.button_cancel), forceClick()); esperarCierreDeActividad(R.id.button_cancel); break;
            case "6c": case "7c": device.pressBack(); Thread.sleep(1000); break;

            case "15": waitAndPerform(withId(R.id.btnMonoplaza), forceClick()); break;
            case "15b": waitAndPerform(withId(R.id.btnBiplaza), forceClick()); break;

            case "8": 
                asegurarContenidoLista(R.id.recyclerview_reservas, "RESERVA");
                onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnEditReserva))); 
                break;
            case "16": case "16b": case "16c": case "16d":
                waitAndPerform(withId(R.id.btn_open_sort), forceClick());
                int sid = R.id.option_sort_name;
                if (arista.equals("16b")) sid = R.id.option_sort_phone;
                else if (arista.equals("16c")) sid = R.id.option_sort_date_in;
                else if (arista.equals("16d")) sid = R.id.option_sort_date_out;
                handleBottomSheet(withId(sid));
                break;
            case "16e": case "16f": case "16g": case "16h":
                waitAndPerform(withId(R.id.btn_open_filter), forceClick());
                int fid = R.id.option_filter_todas;
                if (arista.equals("16e")) fid = R.id.option_filter_previstas;
                else if (arista.equals("16f")) fid = R.id.option_filter_vigentes;
                else if (arista.equals("16g")) fid = R.id.option_filter_caducadas;
                handleBottomSheet(withId(fid));
                break;
            case "17": 
                asegurarContenidoLista(R.id.recyclerview_reservas, "RESERVA");
                onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDeleteReserva)));
                handleSystemDialog();
                break;
            case "18": 
                asegurarContenidoLista(R.id.recyclerview_reservas, "RESERVA");
                onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDetailsReserva)));
                handleSystemDialog(); 
                break;
            case "19":
                asegurarContenidoLista(R.id.recyclerview_reservas, "RESERVA");
                onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnSendReserva)));
                device.pressBack();
                break;

            case "9": case "10":
                waitAndPerform(withId(R.id.edit_cliente), replaceText("Safe Test"));
                waitAndPerform(withId(R.id.edit_telefono), replaceText("600111222"));
                waitAndPerform(withId(R.id.button_confirm), forceClick());
                esperarCierreDeActividad(R.id.button_confirm);
                break;
            case "9b": case "10b": waitAndPerform(withId(R.id.button_cancel), forceClick()); esperarCierreDeActividad(R.id.button_cancel); break;
            case "9c": case "10c": device.pressBack(); Thread.sleep(1000); break;
            case "11": 
                onView(withId(R.id.btn_select_quads)).perform(scrollTo(), forceClick()); 
                break;
            case "20": waitAndPerform(withId(R.id.btn_fecha_recogida), forceClick()); handleDatePicker(); break;
            case "20b": waitAndPerform(withId(R.id.btn_fecha_devolucion), forceClick()); handleDatePicker(); break;

            case "12": 
                asegurarContenidoLista(R.id.recycler_selection, "SELECCION_QUAD");
                onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.cb_select)));
                waitAndPerform(withId(R.id.btn_confirm_selection), forceClick());
                esperarCierreDeActividad(R.id.btn_confirm_selection);
                break;
            case "12b": waitAndPerform(withId(R.id.btn_cancel_selection), forceClick()); esperarCierreDeActividad(R.id.btn_cancel_selection); break;
            case "12c": pressBackHastaVer(R.id.edit_cliente); break; 
            case "21": case "21b": case "21c":
                int selSortId = R.id.sort_matricula;
                if (arista.equals("21b")) selSortId = R.id.sort_tipo;
                else if (arista.equals("21c")) selSortId = R.id.sort_precio;
                waitAndPerform(withId(selSortId), forceClick());
                break;
            case "25": 
                asegurarContenidoLista(R.id.recycler_selection, "SELECCION_QUAD");
                onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btn_details_selection)));
                handleSystemDialog();
                break;
            case "26": 
                asegurarContenidoLista(R.id.recycler_selection, "SELECCION_QUAD");
                onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btn_cascos_popup)));
                Thread.sleep(800);
                device.pressBack(); 
                break;

            default: throw new IllegalArgumentException("Unknown: " + arista);
        }
    }

    private void asegurarContenidoLista(int recyclerId, String tipo) throws Exception {
        if (isProvisioning) return; 
        int itemCount = 0;
        try {
            UiObject recycler = device.findObject(new UiSelector().resourceId("es.unizar.eina.SistemaReservas:id/" + 
                    InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceEntryName(recyclerId)));
            itemCount = recycler.getChildCount();
        } catch (Exception ignored) {}

        if (itemCount == 0) {
            isProvisioning = true;
            if (tipo.equals("QUAD")) {
                device.pressBack(); 
                mapeoAristaAccion("3"); 
                mapeoAristaAccion("6"); 
                mapeoAristaAccion("1"); 
            } else if (tipo.equals("RESERVA")) {
                device.pressBack();
                mapeoAristaAccion("4"); 
                mapeoAristaAccion("20"); 
                mapeoAristaAccion("20b"); 
                mapeoAristaAccion("11"); 
                asegurarContenidoLista(R.id.recycler_selection, "SELECCION_QUAD");
                mapeoAristaAccion("12"); 
                mapeoAristaAccion("10"); 
                mapeoAristaAccion("2"); 
            } else if (tipo.equals("SELECCION_QUAD")) {
                device.pressBack(); 
                mapeoAristaAccion("10b"); 
                mapeoAristaAccion("3"); 
                mapeoAristaAccion("6"); 
                mapeoAristaAccion("4"); 
                mapeoAristaAccion("20");
                mapeoAristaAccion("20b");
                mapeoAristaAccion("11"); 
            }
            isProvisioning = false;
        }
    }

    private void esperarCierreDeActividad(int viewId) throws Exception {
        device.wait(Until.gone(By.res("es.unizar.eina.SistemaReservas:id/" + 
                InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceEntryName(viewId))), UI_TIMEOUT);
        Thread.sleep(1500); 
    }

    private void pressBackHastaVer(int targetId) throws Exception {
        for (int i = 0; i < 4; i++) {
            try {
                onView(withId(targetId)).check(matches(isDisplayed()));
                return;
            } catch (Exception e) {
                device.pressBack();
                Thread.sleep(1200);
            }
        }
    }

    private boolean viewExists(String idName) {
        return device.findObject(new UiSelector().resourceId("es.unizar.eina.SistemaReservas:id/" + idName)).exists();
    }

    private void clickMenuButton(int resId) throws UiObjectNotFoundException {
        String name = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceEntryName(resId);
        UiObject btn = device.findObject(new UiSelector().resourceId("es.unizar.eina.SistemaReservas:id/" + name));
        if (btn.waitForExists(2000)) btn.click();
        else onView(withId(resId)).perform(scrollTo(), forceClick());
    }

    private void handleDatePicker() throws UiObjectNotFoundException, InterruptedException {
        UiObject ok = device.findObject(new UiSelector().textMatches("(?i)OK|ACEPTAR|ESTABLECER|LISTO|DONE|CONFIRMAR"));
        if (ok.waitForExists(UI_TIMEOUT)) {
            ok.click();
            Thread.sleep(1200);
        }
    }

    private void handleSystemDialog() throws InterruptedException {
        UiObject btn = device.findObject(new UiSelector().textMatches("(?i)ELIMINAR|ACEPTAR|OK|CONFIRMAR|SI|SÍ|CERRAR|CLOSE|CANCELAR"));
        if (btn.exists()) {
            try { btn.click(); Thread.sleep(1000); } catch (UiObjectNotFoundException ignored) {}
        }
    }

    private void handleBottomSheet(Matcher<View> optionMatcher) {
        try { onView(optionMatcher).perform(forceClick()); Thread.sleep(1000); } catch (Exception e) { device.pressBack(); }
    }

    private void waitAndPerform(Matcher<View> matcher, ViewAction action) {
        onView(matcher).perform(action);
    }

    public static ViewAction forceClick() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "force click"; }
            @Override public void perform(UiController uiController, View view) { view.performClick(); }
        };
    }

    public static ViewAction clickOnViewChild(final int id) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return null; }
            @Override public String getDescription() { return "Click child"; }
            @Override public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) v.performClick();
            }
        };
    }
}
