package es.unizar.eina.SistemaReservas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import es.unizar.eina.SistemaReservas.R;
import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.database.Reserva;

import es.unizar.eina.SistemaReservas.database.ReservaQuad;
import java.util.ArrayList;
import java.util.List;

/**
 * Actividad de entrada principal que gestiona el menú de navegación de la aplicación.
 * 
 * Se encarga de coordinar el acceso a los listados de gestión y de procesar la recepción 
 * de datos provenientes de los formularios de creación de Quads y Reservas mediante 
 * el uso de manejadores de resultados asíncronos.
 */
public class SistemaReservas extends AppCompatActivity {

    /** ViewModel para la gestión lógica y persistencia de vehículos. */
    private QuadViewModel mQuadViewModel;
    /** ViewModel para la gestión lógica y persistencia de reservas. */
    private ReservaViewModel mReservaViewModel;

    /**
     * Configura la interfaz del menú principal, inicializa los proveedores de datos 
     * y define la lógica de respuesta para la creación de nuevos registros.
     * 
     * @param savedInstanceState Estado de la instancia guardada.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);
        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);

        /**
         * Manejador de resultado para la creación de un nuevo Quad.
         * Extrae los datos del formulario QuadEdit e intenta su persistencia 
         * validando la existencia previa de la matrícula.
         */
        ActivityResultLauncher<Intent> mCreateQuadLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            String matricula = extras.getString(QuadEdit.QUAD_MATRICULA);
                            String descripcion = extras.getString(QuadEdit.QUAD_DESCRIPCION);
                            double precio = extras.getDouble(QuadEdit.QUAD_PRECIO);
                            boolean esMonoplaza = extras.getBoolean(QuadEdit.QUAD_ESMONOPLAZA);

                            if (matricula == null) {
                                Toast.makeText(this, "Error: No se recibió la matrícula", Toast.LENGTH_LONG).show();
                                return;
                            }

                            Quad quad = new Quad(matricula, esMonoplaza, precio, descripcion);

                            long insertResult = mQuadViewModel.insert(quad);

                            if (insertResult == -1) {
                                Toast.makeText(this, "ERROR: La matrícula " + matricula + " ya existe.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Quad guardado correctamente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        /**
         * Manejador de resultado para la creación de una nueva Reserva.
         * Procesa los datos del cliente, fechas y la lista de vehículos seleccionados 
         * para realizar un guardado relacional completo en la base de datos.
         */
        ActivityResultLauncher<Intent> mCreateReservaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            String cliente = extras.getString(ReservaEdit.RES_CLIENTE);
                            String tlf = extras.getString(ReservaEdit.RES_TELEFONO);
                            String fechaIn = extras.getString(ReservaEdit.RES_FECHA_IN);
                            String fechaOut = extras.getString(ReservaEdit.RES_FECHA_OUT);
                            
                            ArrayList<SelectedQuad> selectedUI = (ArrayList<SelectedQuad>) extras.getSerializable(ReservaEdit.RES_LISTA_QUADS);

                            Reserva reserva = new Reserva(cliente, tlf, fechaIn, fechaOut);

                            List<ReservaQuad> dbQuads = new ArrayList<>();
                            if (selectedUI != null) {
                                for (SelectedQuad sq : selectedUI) {
                                    dbQuads.add(new ReservaQuad(0, sq.getId(), sq.getNumCascos()));
                                }
                            }

                            mReservaViewModel.insert(reserva, dbQuads);

                            Toast.makeText(this, "Reserva guardada correctamente", Toast.LENGTH_SHORT).show();
                        }
                    }
        });

        Button crearQuadButton = findViewById(R.id.button_crear_quad);
        crearQuadButton.setOnClickListener(v -> {
            Intent intent = new Intent(SistemaReservas.this, QuadEdit.class);
            mCreateQuadLauncher.launch(intent);
        });

        Button listarQuadsButton = findViewById(R.id.button_listar_quads);
        listarQuadsButton.setOnClickListener(v -> {
            Intent intent = new Intent(SistemaReservas.this, QuadListActivity.class);
            startActivity(intent);
        });

        Button crearReservaButton = findViewById(R.id.button_crear_reserva);
        crearReservaButton.setOnClickListener(v -> {
            Intent intent = new Intent(SistemaReservas.this, ReservaEdit.class);
            mCreateReservaLauncher.launch(intent);
        });

        Button listarReservasButton = findViewById(R.id.button_listar_reservas);
        listarReservasButton.setOnClickListener(v -> {
            Intent intent = new Intent(SistemaReservas.this, ReservaListActivity.class);
            startActivity(intent);
        });

        Button panelPruebasButton = findViewById(R.id.button_panel_pruebas);
        panelPruebasButton.setOnClickListener(v -> {
            Intent intent = new Intent(SistemaReservas.this, TestActivity.class);
            startActivity(intent);
        });
    }
}