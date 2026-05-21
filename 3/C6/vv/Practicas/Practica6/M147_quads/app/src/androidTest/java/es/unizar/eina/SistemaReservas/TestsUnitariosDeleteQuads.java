package es.unizar.eina.SistemaReservas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;

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

    @Test
    public void testDeleteQuadValido() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            
            // 1. Preparación: Insertar un Quad de prueba
            Quad quadTest = new Quad("7777-DEL", true, 30.0, "Para borrar");
            long id = repository.insert(quadTest);
            assertThat("Fallo previo: No se pudo insertar el Quad", id, is(greaterThan(0L)));
            quadTest.setId((int) id);

            // 2. Ejecución: Borramos el Quad
            int filasBorradas = repository.delete(quadTest);

            // 3. Validación: Comprobamos que se ha borrado 1 fila
            assertThat("UNIT DELETE 1: Quad válido no borrado", filasBorradas, is(greaterThan(0)));
            
            // Verificación extra: Intentar buscarlo en la BD (debería devolver null o no encontrarlo)
            Quad quadComprobacion = repository.getQuadById((int) id);
            assertThat("UNIT DELETE 1: El Quad sigue existiendo en BD", quadComprobacion, is(nullValue()));
        });
    }

    @Test
    public void testDeleteQuadInexistente() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();
            
            // 1. Preparación: Creamos un Quad pero NO lo insertamos, le damos un ID falso
            Quad quadFantasma = new Quad("9999-GHO", true, 30.0, "No existe");
            quadFantasma.setId(999999); // ID que sabemos que no existe

            // 2. Ejecución: Intentamos borrarlo
            int filasBorradas = repository.delete(quadFantasma);

            // 3. Validación: Room no debe petar, simplemente devuelve 0 filas afectadas
            assertThat("UNIT DELETE 2: Borrar Quad inexistente debe afectar a 0 filas", filasBorradas, is(0));
        });
    }

    @Test
    public void testDeleteQuadNulo() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository repository = activity.getQuadRepository();

            // Ejecución: Intentamos borrar null
            int filasBorradas = repository.delete(null);

            // Validación: El repositorio captura la excepción interna y devuelve -1
            assertThat("UNIT DELETE 3: Al borrar nulo, Room afecta a 0 filas", filasBorradas, is(0));
        });
    }

    @Test
    public void testDeleteQuadConCascada() {
        scenarioRule.getScenario().onActivity(activity -> {
            QuadRepository quadRepo = activity.getQuadRepository();
            ReservaRepository reservaRepo = activity.getReservaRepository();

            // 1. Preparación: Insertar Quad
            Quad quadTest = new Quad("8888-CAS", false, 60.0, "Quad en Cascada");
            long idQuad = quadRepo.insert(quadTest);
            assertThat(idQuad, is(greaterThan(0L)));
            quadTest.setId((int) idQuad);

            // 2. Preparación: Insertar Reserva vinculada a ese Quad
            List<ReservaQuad> listaVehiculos = new ArrayList<>();
            listaVehiculos.add(new ReservaQuad(0, (int) idQuad, 1, 0.0));
            
            Reserva reservaTest = new Reserva("Pedro Cascada", 654321987, "2026-01-20", "2026-01-22");
            long idReserva = reservaRepo.insertSync(reservaTest, listaVehiculos);
            assertThat(idReserva, is(greaterThan(0L)));
            reservaTest.setId((int) idReserva);

            // 3. Ejecución: Borrar el Quad (aquí entra en juego el CASCADE)
            int filasBorradas = quadRepo.delete(quadTest);

            // 4. Validación: El Quad debe borrarse sin lanzar error de clave foránea
            assertThat("UNIT DELETE 4: Fallo al borrar Quad vinculado (Falla el CASCADE)", filasBorradas, is(greaterThan(0)));

            // Limpieza manual de la reserva que se ha quedado huérfana para no ensuciar BD
            reservaRepo.delete(reservaTest);
        });
    }
}
