package es.unizar.eina.SistemaReservas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.database.QuadRepository;
import es.unizar.eina.SistemaReservas.database.Reserva;
import es.unizar.eina.SistemaReservas.database.ReservaQuad;
import es.unizar.eina.SistemaReservas.database.ReservaRepository;
import es.unizar.eina.SistemaReservas.ui.SistemaReservas;

@RunWith(AndroidJUnit4.class)
public class TestsUnitariosDeleteReservas {

    private Quad quadBase;
    
    @Rule
    public ActivityScenarioRule<SistemaReservas> scenarioRule = new ActivityScenarioRule<>(SistemaReservas.class);

    private QuadRepository getQuadRepository() {
        final QuadRepository[] repo = new QuadRepository[1];
        scenarioRule.getScenario().onActivity(activity -> repo[0] = activity.getQuadRepository());
        return repo[0];
    }

    private ReservaRepository getReservaRepository() {
        final ReservaRepository[] repo = new ReservaRepository[1];
        scenarioRule.getScenario().onActivity(activity -> repo[0] = activity.getReservaRepository());
        return repo[0];
    }

    // Preparamos un Quad válido antes de cada test porque las reservas exigen tener vehículos
    @Before
    public void setUp() {
        QuadRepository quadRepo = getQuadRepository();
        quadBase = new Quad("5555-DEL", true, 40.0, "Quad Base Delete Reserva");
        long idQ = quadRepo.insert(quadBase);
        assertTrue("Setup: Error al insertar Quad", idQ > 0);
        quadBase.setId((int) idQ);
    }

    @Test
    public void testDeleteReservaValida() {
        ReservaRepository reservaRepo = getReservaRepository();
        
        // 1. Preparación: Insertar una Reserva de prueba vinculada al Quad base
        List<ReservaQuad> listaVehiculos = new ArrayList<>();
        listaVehiculos.add(new ReservaQuad(0, quadBase.getId(), 1, 0.0));
        
        Reserva reservaTest = new Reserva("Juan Borrado", 600111222, "15/01/2026", "18/01/2026");
        long idR = reservaRepo.insertSync(reservaTest, listaVehiculos);
        assertTrue("Fallo previo: No se pudo insertar la reserva", idR > 0);
        reservaTest.setId((int) idR);

        // 2. Ejecución: Borramos la Reserva
        int filasBorradas = reservaRepo.delete(reservaTest);

        // 3. Validación: Comprobamos que se ha borrado 1 fila
        assertTrue("UNIT DELETE RES 1: Reserva válida no borrada", filasBorradas > 0);
    }

    @Test
    public void testDeleteReservaInexistente() {
        ReservaRepository reservaRepo = getReservaRepository();
        
        // 1. Preparación: Creamos una Reserva pero NO la insertamos
        Reserva reservaFantasma = new Reserva("Fantasma", 0, "01/01/2026", "02/01/2026");
        reservaFantasma.setId(999999); // ID falso que no está en base de datos

        // 2. Ejecución: Intentamos borrarla
        int filasBorradas = reservaRepo.delete(reservaFantasma);

        // 3. Validación: Devuelve 0 filas afectadas (Room no falla, solo ignora)
        assertEquals("UNIT DELETE RES 2: Borrar Reserva inexistente debe afectar a 0 filas", 0, filasBorradas);
    }

    @Test
    public void testDeleteReservaNula() {
        ReservaRepository reservaRepo = getReservaRepository();

        // Ejecución: Intentamos borrar null
        int filasBorradas = reservaRepo.delete(null);

        // Validación: Room gestiona el null de forma segura y devuelve 0 filas afectadas
        assertEquals("UNIT DELETE RES 3: Al borrar reserva nula, Room afecta a 0 filas", 0, filasBorradas);
    }

    // Limpiamos la base de datos eliminando el Quad base
    @After
    public void tearDown() {
        QuadRepository quadRepo = getQuadRepository();
        if (quadBase != null && quadBase.getId() > 0) {
            quadRepo.delete(quadBase);
        }
    }
}