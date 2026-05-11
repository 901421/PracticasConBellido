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

import java.util.Arrays;
import java.util.Collection;

import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Suite de pruebas parametrizadas para validar los caminos de navegación Edge-Pair.
 * Utiliza Espresso para interacciones estándar y UiAutomator para diálogos del sistema.
 */
@LargeTest
@RunWith(Parameterized.class)
public class CaminosNavegacionTest {

    private UiDevice device; // Referencia al dispositivo para UiAutomator
    private static final int MAX_RETRIES = 2; // Reintentos en caso de fallo de sincronización
    private static final long UI_TIMEOUT = 8000; // Tiempo máximo de espera para elementos UI

    // Regla para lanzar la actividad principal del sistema de reservas
    @Rule
    public ActivityScenarioRule<SistemaReservas> activityRule =
            new ActivityScenarioRule<>(SistemaReservas.class);

    private final String[] camino; // Almacena el camino de aristas actual

    /** Constructor para la prueba parametrizada. */
    public CaminosNavegacionTest(String caminoStr) {
        this.camino = caminoStr.split(",");
    }

    /** Configuración inicial previa a cada camino de prueba. */
    @Before
    public void setUp() throws Exception {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        resetAlMenuFuerte(); // Asegura empezar siempre desde el Menú Principal (N1)
    }

    /** Método de seguridad que fuerza el retorno al menú principal limpiando la pila. */
    private void resetAlMenuFuerte() throws Exception {
        for (int i = 0; i < 6; i++) {
            // Si el logo está visible, ya estamos en el Menú Principal (N1)
            if (device.hasObject(By.res("es.unizar.eina.SistemaReservas:id/logo_app"))) return;
            handleSystemDialog(); // Cierra posibles diálogos abiertos
            device.pressBack(); // Retrocede un nivel en el stack
            Thread.sleep(1200);
        }
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
        Thread.sleep(2000); // Espera inicial para carga de app
        for (String arista : camino) {
            ejecutarAccionConSincronizacion(arista);
        }
    }

    /** Wrapper para ejecutar la acción de la arista con lógica de reintentos. */
    private void ejecutarAccionConSincronizacion(String arista) throws Exception {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                mapeoAristaAccion(arista); // Intenta ejecutar la acción
                device.waitForIdle(); // Espera a que el hilo de UI se asiente
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts == MAX_RETRIES) throw e; // Falla si agota reintentos
                handleSystemDialog(); // Intenta cerrar diálogos que bloqueen la UI
                Thread.sleep(2500); // Pausa para recuperación de estado
            }
        }
    }

    /** Mapeo centralizado: Convierte el ID de arista en una interacción real con la App. */
    private void mapeoAristaAccion(String arista) throws Exception {
        switch (arista) {
            // --- NAVEGACIÓN INICIAL ---
            case "1": // Arista 1: Menú Principal -> Listado Quads (N2)
                if (device.hasObject(By.res("es.unizar.eina.SistemaReservas:id/toggleGroupQuads"))) return;
                onView(withId(R.id.button_listar_quads)).perform(scrollTo(), click()); 
                if (!device.wait(Until.hasObject(By.res("es.unizar.eina.SistemaReservas:id/toggleGroupQuads")), UI_TIMEOUT)) {
                    throw new RuntimeException("Fallo al abrir N2 (Listado Quads)");
                }
                break;
            case "2": // Arista 2: Menú Principal -> Listado Reservas (N4)
                if (device.hasObject(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"))) return;
                onView(withId(R.id.button_listar_reservas)).perform(scrollTo(), click()); 
                if (!device.wait(Until.hasObject(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort")), UI_TIMEOUT)) {
                    throw new RuntimeException("Fallo al abrir N4 (Listado Reservas)");
                }
                break;
            case "3": // Arista 3: Menú Principal -> Formulario Quad (N3) - Modo CREAR
                onView(withId(R.id.button_crear_quad)).perform(scrollTo(), click()); 
                device.wait(Until.hasObject(By.res("es.unizar.eina.SistemaReservas:id/button_save")), UI_TIMEOUT);
                break;
            case "4": // Arista 4: Menú Principal -> Formulario Reserva (N5) - Modo CREAR
                onView(withId(R.id.button_crear_reserva)).perform(scrollTo(), click()); 
                device.wait(Until.hasObject(By.res("es.unizar.eina.SistemaReservas:id/button_confirm")), UI_TIMEOUT);
                break;

            // --- GESTIÓN DE QUADS (N2) ---
            case "5": // Arista 5: Listado Quads -> Formulario Quad (N3) - Modo EDITAR
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/toggleGroupQuads"), "1");
                // Click en el botón de editar (btnEdit) del primer elemento de la lista
                onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnEdit))); 
                device.wait(Until.hasObject(By.res("es.unizar.eina.SistemaReservas:id/button_save")), UI_TIMEOUT);
                break;
            case "13": // Arista 13: Listado Quads -> Ordenar por MATRÍCULA
                waitAndPerform(withId(R.id.sort_matricula), forceClick()); break;
            case "13b": // Arista 13b: Listado Quads -> Ordenar por TIPO
                waitAndPerform(withId(R.id.sort_tipo), forceClick()); break;
            case "13c": // Arista 13c: Listado Quads -> Ordenar por PRECIO
                waitAndPerform(withId(R.id.sort_precio), forceClick()); break;
            case "14": // Arista 14: Listado Quads -> ELIMINAR Quad
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/toggleGroupQuads"), "1");
                // Click en el botón de borrar (btnDelete) del primer elemento
                onView(withId(R.id.recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDelete)));
                handleSystemDialog(); // Acepta el diálogo de confirmación de borrado
                Thread.sleep(3000); // Pausa para asegurar la actualización de la DB
                break;
            case "21": case "22": // Aristas 21/22: Volver Atrás al Menú Principal (N1)
                device.pressBack(); 
                device.wait(Until.hasObject(By.res("es.unizar.eina.SistemaReservas:id/logo_app")), UI_TIMEOUT);
                break;

            // --- FORMULARIO QUAD (N3) ---
            case "6": case "7": // Aristas 6/7: GUARDAR cambios en Quad (vuelve a N2 o N1)
                // Rellena campos obligatorios con datos aleatorios para evitar duplicados de matrícula
                waitAndPerform(withId(R.id.matricula), replaceText(String.format("%04d-TST", (int)(Math.random()*9000)+1000)));
                waitAndPerform(withId(R.id.precio), replaceText("55.0"));
                waitAndPerform(withId(R.id.button_save), forceClick());
                Thread.sleep(2000); // Espera cierre de actividad
                break;
            case "6b": case "7b": // Aristas 6b/7b: CANCELAR edición de Quad
                waitAndPerform(withId(R.id.button_cancel), forceClick()); Thread.sleep(2000); break;
            case "6c": case "7c": // Aristas 6c/7c: Botón Atrás Físico en Quad
                device.pressBack(); Thread.sleep(2000); break;

            case "15": // Arista 15: Seleccionar tipo MONOPLAZA
                waitAndPerform(withId(R.id.btnMonoplaza), forceClick()); break;
            case "15b": // Arista 15b: Seleccionar tipo BIPLAZA
                waitAndPerform(withId(R.id.btnBiplaza), forceClick()); break;

            // --- GESTIÓN DE RESERVAS (N4) ---
            case "8": // Arista 8: Listado Reservas -> Formulario Reserva (N5) - Modo EDITAR
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"), "2");
                onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnEditReserva))); 
                device.wait(Until.hasObject(By.res("es.unizar.eina.SistemaReservas:id/button_confirm")), UI_TIMEOUT);
                break;
            case "16": case "16b": case "16c": case "16d": // Aristas 16: Ordenación de Reservas
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"), "2");
                waitAndPerform(withId(R.id.btn_open_sort), forceClick()); // Abre panel de ordenación
                int sid = R.id.option_sort_name;
                if (arista.equals("16b")) sid = R.id.option_sort_phone;
                else if (arista.equals("16c")) sid = R.id.option_sort_date_in;
                else if (arista.equals("16d")) sid = R.id.option_sort_date_out;
                handleBottomSheet(sid); // Selecciona la opción en el BottomSheet
                break;
            case "16e": case "16f": case "16g": case "16h": // Aristas 16: Filtrado de Reservas
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"), "2");
                waitAndPerform(withId(R.id.btn_open_filter), forceClick()); // Abre panel de filtrado
                int fid = R.id.option_filter_todas;
                if (arista.equals("16e")) fid = R.id.option_filter_previstas;
                else if (arista.equals("16f")) fid = R.id.option_filter_vigentes;
                else if (arista.equals("16g")) fid = R.id.option_filter_caducadas;
                handleBottomSheet(fid); // Selecciona el filtro
                break;
            case "17": // Arista 17: Listado Reservas -> ELIMINAR Reserva
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"), "2");
                onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDeleteReserva)));
                handleSystemDialog(); // Confirma eliminación
                Thread.sleep(3000);
                break;
            case "18": // Arista 18: Listado Reservas -> VER DETALLES de Reserva
                asegurarPantalla(By.res("es.unizar.eina.SistemaReservas:id/btn_open_sort"), "2");
                onView(withId(R.id.recyclerview_reservas)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btnDetailsReserva)));
                handleSystemDialog(); // Cierra el diálogo de detalles tras inspeccionarlo
                break;

            // --- FORMULARIO RESERVA (N5) ---
            case "9": case "10": // Aristas 9/10: GUARDAR Reserva
                // Rellena datos del cliente para cumplir validación
                waitAndPerform(withId(R.id.edit_cliente), replaceText("Audit Test"));
                waitAndPerform(withId(R.id.edit_telefono), replaceText("600111222"));
                waitAndPerform(withId(R.id.button_confirm), forceClick());
                Thread.sleep(2000);
                break;
            case "9b": case "10b": // Aristas 9b/10b: CANCELAR Reserva
                waitAndPerform(withId(R.id.button_cancel), forceClick()); Thread.sleep(2000); break;
            case "9c": case "10c": // Aristas 9c/10c: Botón Atrás Físico en Reserva
                device.pressBack(); Thread.sleep(2000); break;
            case "11": // Arista 11: Abrir SELECCIÓN de Quads (N5->N6)
                onView(withId(R.id.btn_select_quads)).perform(scrollTo(), forceClick()); 
                if (!device.wait(Until.hasObject(By.res("es.unizar.eina.SistemaReservas:id/recycler_selection")), UI_TIMEOUT)) {
                    throw new RuntimeException("Fallo al abrir N6 (Selección Quads)");
                }
                break;
            case "19": // Arista 19: Seleccionar FECHA RECOGIDA
                waitAndPerform(withId(R.id.btn_fecha_recogida), forceClick()); handleDatePicker(); break;
            case "19b": // Arista 19b: Seleccionar FECHA DEVOLUCIÓN
                waitAndPerform(withId(R.id.btn_fecha_devolucion), forceClick()); handleDatePicker(); break;

            // --- SELECCIÓN DE QUADS (N6) ---
            case "12": // Arista 12: CONFIRMAR Quads seleccionados (Vuelve a N5)
                waitAndPerform(withId(R.id.btn_confirm_selection), forceClick());
                Thread.sleep(2000);
                break;
            case "12b": // Arista 12b: CANCELAR selección de Quads
                waitAndPerform(withId(R.id.btn_cancel_selection), forceClick()); Thread.sleep(2000); break;
            case "12c": // Arista 12c: Botón Atrás Físico en Selección
                device.pressBack(); Thread.sleep(2000); break; 
            case "20": case "20b": case "20c": // Aristas 20: Ordenación en Selección
                int selSortId = R.id.sort_matricula;
                if (arista.equals("20b")) selSortId = R.id.sort_tipo;
                else if (arista.equals("20c")) selSortId = R.id.sort_precio;
                waitAndPerform(withId(selSortId), forceClick());
                break;
            case "23": // Arista 23: Ver DETALLES del Quad en la lista de selección
                onView(withId(R.id.recycler_selection)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btn_details_selection)));
                handleSystemDialog();
                break;
            case "24": // Arista 24: SELECCIONAR Quad y ajustar CASCOS (Trigger de selección)
                // 1. IMPORTANTE: Marcamos el CheckBox explícitamente. 
                // Esto pone isSelected=true en el adaptador y hace visible el botón de cascos.
                onView(withId(R.id.recycler_selection)).perform(
                        RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.cb_select)));
                Thread.sleep(1000);
                // 2. Abrimos el popup de cascos (confirma que el quad está activo)
                onView(withId(R.id.recycler_selection)).perform(
                        RecyclerViewActions.actionOnItemAtPosition(0, clickOnViewChild(R.id.btn_cascos_popup)));
                Thread.sleep(1000);
                // 3. Selecciona una opción del diálogo del sistema mediante texto
                UiObject op = device.findObject(new UiSelector().textMatches("(?i)1 Casco|0 Cascos|2 Cascos"));
                if (op.waitForExists(2000)) op.click();
                else device.pressBack();
                Thread.sleep(1200);
                break;

            default: throw new IllegalArgumentException("Arista Desconocida en Switch: " + arista);
        }
    }

    /** Helper: Asegura que la App está en la pantalla correcta antes de interaccionar. */
    private void asegurarPantalla(BySelector selector, String aristaNav) throws Exception {
        if (!device.hasObject(selector)) {
            mapeoAristaAccion(aristaNav); // Navega si no encuentra el elemento
        }
    }

    /** Helper: Maneja el diálogo nativo DatePickerDialog de Android. */
    private void handleDatePicker() throws UiObjectNotFoundException, InterruptedException {
        UiObject ok = device.findObject(new UiSelector().textMatches("(?i)OK|ACEPTAR|ESTABLECER|LISTO|DONE|CONFIRMAR"));
        if (ok.waitForExists(4000)) { ok.click(); Thread.sleep(1500); }
    }

    /** Helper: Maneja diálogos genéricos de confirmación o alerta mediante UiAutomator. */
    private void handleSystemDialog() throws InterruptedException {
        UiObject btn = device.findObject(new UiSelector().textMatches("(?i)ELIMINAR|ACEPTAR|OK|CONFIRMAR|SI|SÍ|CERRAR|CLOSE|CANCELAR"));
        if (btn.waitForExists(3000)) {
            try { btn.click(); Thread.sleep(1500); } catch (Exception ignored) {}
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
    private void waitAndPerform(Matcher<View> matcher, ViewAction action) {
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
            @Override public Matcher<View> getConstraints() { return null; }
            @Override public String getDescription() { return "Click child"; }
            @Override public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) v.performClick();
            }
        };
    }
}