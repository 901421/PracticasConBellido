package es.unizar.eina.SistemaReservas.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import es.unizar.eina.SistemaReservas.R;

/**
 * Actividad que actúa como panel de control para la ejecución de pruebas del sistema.
 * 
 * Permite al administrador ejecutar pruebas unitarias, de volumen y de sobrecarga,
 * así como realizar operaciones de limpieza masiva de la base de datos (Quads y Reservas).
 * Implementa diálogos de confirmación para prevenir ejecuciones accidentales de tareas pesadas 
 * o destructivas.
 */
public class TestActivity extends AppCompatActivity {

    /**
     * Inicializa la interfaz del panel de pruebas y configura los escuchadores de eventos
     * para cada una de las pruebas y herramientas de gestión de memoria.
     * 
     * @param savedInstanceState Estado de la instancia guardada.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_panel);
        setTitle("Panel de Control de Pruebas");

        TestRunner runner = new TestRunner(this.getApplication());

        // --- BOTONES DE EJECUCIÓN DE TESTS ---
        
        findViewById(R.id.btn_run_unit).setOnClickListener(v -> 
            mostrarConfirmacion("Ejecutar Pruebas Unitarias", 
                "Se insertarán quads de prueba. ¿Continuar?", 
                () -> {
                    new Thread(runner::runUnitTests).start();
                    Toast.makeText(this, "Ejecutando Unitarios (Ver Logcat)", Toast.LENGTH_SHORT).show();
                })
        );

        findViewById(R.id.btn_run_vol).setOnClickListener(v -> 
            mostrarConfirmacion("Ejecutar Prueba de Volumen", 
                "Se insertarán 100 quads y 1000 reservas. Esto puede tardar. ¿Continuar?", 
                () -> {
                    runner.runVolumeTest();
                    Toast.makeText(this, "Carga de Volumen iniciada...", Toast.LENGTH_SHORT).show();
                })
        );

        findViewById(R.id.btn_run_stress).setOnClickListener(v -> 
            mostrarConfirmacion("Ejecutar Prueba de Sobrecarga", 
                "Se probará el límite de caracteres de la base de datos. ¿Continuar?", 
                () -> {
                    runner.runStressTest();
                    Toast.makeText(this, "Test de Sobrecarga iniciado...", Toast.LENGTH_SHORT).show();
                })
        );

        // --- BOTONES DE LIMPIEZA DE MEMORIA ---
        
        findViewById(R.id.btn_clear_quads).setOnClickListener(v -> 
            mostrarConfirmacion("Limpiar Quads", 
                "¿Borrar TODOS los quads? Esta acción no se puede deshacer.", 
                () -> {
                    runner.clearQuads();
                    Toast.makeText(this, "Memoria de Quads limpia", Toast.LENGTH_SHORT).show();
                })
        );

        findViewById(R.id.btn_clear_res).setOnClickListener(v -> 
            mostrarConfirmacion("Limpiar Reservas", 
                "¿Borrar TODAS las reservas? Esta acción no se puede deshacer.", 
                () -> {
                    runner.clearReservas();
                    Toast.makeText(this, "Memoria de Reservas limpia", Toast.LENGTH_SHORT).show();
                })
        );

        findViewById(R.id.btn_clear_all).setOnClickListener(v -> 
            mostrarConfirmacion("!!! RESET TOTAL !!!", 
                "¿ESTÁS SEGURO? Se borrarán todos los datos de la aplicación.", 
                () -> {
                    runner.clearAll();
                    Toast.makeText(this, "BASE DE DATOS RESETEADA", Toast.LENGTH_LONG).show();
                })
        );
    }

    /**
     * Muestra un cuadro de diálogo de confirmación antes de ejecutar una acción sensible.
     * Utiliza un objeto {@link Runnable} para encapsular la lógica que debe ejecutarse 
     * solo si el usuario pulsa el botón de confirmación.
     * 
     * @param titulo Título del diálogo informativo.
     * @param mensaje Cuerpo del mensaje de advertencia.
     * @param accionConfirmada Bloque de código a ejecutar tras la confirmación.
     */
    private void mostrarConfirmacion(String titulo, String mensaje, Runnable accionConfirmada) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("SÍ, PROCEDER", (dialog, which) -> accionConfirmada.run())
                .setNegativeButton("CANCELAR", null)
                .show();
    }
}