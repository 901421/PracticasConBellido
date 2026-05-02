package es.unizar.eina.SistemaReservas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
public class TestsUnitariosDeleteQuads {

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

    @Test
    public void testDeleteQuadValido() {
        QuadRepository repository = getQuadRepository();
        
        // 1. Preparación: Insertar un Quad de prueba
        Quad quadTest = new Quad("7777-DEL", true, 30.0, "Para borrar");
        long id = repository.insert(quadTest);
        assertTrue("Fallo previo: No se pudo insertar el Quad", id > 0);
        quadTest.setId((int) id);

        // 2. Ejecución: Borramos el Quad
        int filasBorradas = repository.delete(quadTest);

        // 3. Validación: Comprobamos que se ha borrado 1 fila
        assertTrue("UNIT DELETE 1: Quad válido no borrado", filasBorradas > 0);
        
        // Verificación extra: Intentar buscarlo en la BD (debería devolver null o no encontrarlo)
        Quad quadComprobacion = repository.getQuadById((int) id);
        assertTrue("UNIT DELETE 1: El Quad sigue existiendo en BD", quadComprobacion == null);
    }

    @Test
    public void testDeleteQuadInexistente() {
        QuadRepository repository = getQuadRepository();
        
        // 1. Preparación: Creamos un Quad pero NO lo insertamos, le damos un ID falso
        Quad quadFantasma = new Quad("9999-GHO", true, 30.0, "No existe");
        quadFantasma.setId(999999); // ID que sabemos que no existe

        // 2. Ejecución: Intentamos borrarlo
        int filasBorradas = repository.delete(quadFantasma);

        // 3. Validación: Room no debe petar, simplemente devuelve 0 filas afectadas
        assertEquals("UNIT DELETE 2: Borrar Quad inexistente debe afectar a 0 filas", 0, filasBorradas);
    }

    @Test
    public void testDeleteQuadNulo() {
        QuadRepository repository = getQuadRepository();

        // Ejecución: Intentamos borrar null
        int filasBorradas = repository.delete(null);

        // Validación: El repositorio captura la excepción interna y devuelve -1
        assertEquals("UNIT DELETE 3: Al borrar nulo, Room afecta a 0 filas", 0, filasBorradas);
    }

    @Test
    public void testDeleteQuadConCascada() {
        QuadRepository quadRepo = getQuadRepository();
        ReservaRepository reservaRepo = getReservaRepository();

        // 1. Preparación: Insertar Quad
        Quad quadTest = new Quad("8888-CAS", false, 60.0, "Quad en Cascada");
        long idQuad = quadRepo.insert(quadTest);
        assertTrue(idQuad > 0);
        quadTest.setId((int) idQuad);

        // 2. Preparación: Insertar Reserva vinculada a ese Quad
        List<ReservaQuad> listaVehiculos = new ArrayList<>();
        listaVehiculos.add(new ReservaQuad(0, (int) idQuad, 1, 0.0));
        
        Reserva reservaTest = new Reserva("Pedro Cascada", 654321987, "20/01/2026", "22/01/2026");
        long idReserva = reservaRepo.insertSync(reservaTest, listaVehiculos);
        assertTrue(idReserva > 0);
        reservaTest.setId((int) idReserva);

        // 3. Ejecución: Borrar el Quad (aquí entra en juego el CASCADE)
        int filasBorradas = quadRepo.delete(quadTest);

        // 4. Validación: El Quad debe borrarse sin lanzar error de clave foránea
        assertTrue("UNIT DELETE 4: Fallo al borrar Quad vinculado (Falla el CASCADE)", filasBorradas > 0);

        // Limpieza manual de la reserva que se ha quedado huérfana para no ensuciar BD
        reservaRepo.delete(reservaTest);
    }
}