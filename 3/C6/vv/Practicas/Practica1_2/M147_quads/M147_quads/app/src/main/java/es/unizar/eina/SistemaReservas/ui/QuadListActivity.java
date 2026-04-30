package es.unizar.eina.SistemaReservas.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View; 
import android.widget.TextView;
import android.widget.Button; 
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.graphics.Color; 
import androidx.core.content.ContextCompat; 

import es.unizar.eina.SistemaReservas.R;
import es.unizar.eina.SistemaReservas.database.Quad;
import com.google.android.material.button.MaterialButtonToggleGroup;

/**
 * Actividad encargada de mostrar el listado de vehículos Quad registrados.
 * Proporciona funcionalidades para ordenar la lista por diferentes criterios,
 * iniciar la creación o edición de un vehículo y gestionar su eliminación.
 */
public class QuadListActivity extends AppCompatActivity {

    /** ViewModel encargado de gestionar los datos de los quads y su ordenación. */
    private QuadViewModel mQuadViewModel;
    /** Componente de interfaz para mostrar la lista de forma eficiente. */
    private RecyclerView recyclerView;
    /** Vista de texto que se muestra únicamente cuando la lista de quads está vacía. */
    private TextView emptyView;

    /** Botón para activar el orden por matrícula. */
    private Button btnMatricula;
    /** Botón para activar el orden por tipo de plaza. */
    private Button btnTipo;
    /** Botón para activar el orden por precio por hora. */
    private Button btnPrecio;

    /**
     * Inicializa la interfaz, los botones de ordenación y el observador del ViewModel.
     * @param savedInstanceState Estado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_quads); 

        recyclerView = findViewById(R.id.recyclerview);
        emptyView = findViewById(R.id.empty_view);

        btnMatricula = findViewById(R.id.sort_matricula);
        btnTipo = findViewById(R.id.sort_tipo);
        btnPrecio = findViewById(R.id.sort_precio);
        
        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);

        // Lógica del ToggleGroup (Mantiene la selección lógica)
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroupQuads);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.sort_matricula) {
                    mQuadViewModel.setSortOrder(0);
                } else if (checkedId == R.id.sort_tipo) {
                    mQuadViewModel.setSortOrder(1);
                } else if (checkedId == R.id.sort_precio) {
                    mQuadViewModel.setSortOrder(2);
                }
            }
        });
        
        btnMatricula.setOnClickListener(v -> {
            mQuadViewModel.setSortOrder(0);
            updateButtonsColor(btnMatricula);
        });

        btnTipo.setOnClickListener(v -> {
            mQuadViewModel.setSortOrder(1);
            updateButtonsColor(btnTipo);
        });

        btnPrecio.setOnClickListener(v -> {
            mQuadViewModel.setSortOrder(2);
            updateButtonsColor(btnPrecio);
        });

        // Estado visual inicial (Matrícula seleccionada)
        updateButtonsColor(btnMatricula);

        // --- CONFIGURACIÓN ADAPTADOR ---
        final QuadListAdapter adapter = new QuadListAdapter(new QuadListAdapter.QuadDiff(), 
            new QuadListAdapter.OnItemClickListener() {
                @Override
                public void onEdit(Quad quad) { editQuad(quad); }
                @Override
                public void onDelete(Quad quad) {
                     new androidx.appcompat.app.AlertDialog.Builder(QuadListActivity.this)
                        .setTitle("Eliminar Quad")
                        .setMessage("¿Eliminar quad " + quad.getMatricula() + "?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            mQuadViewModel.delete(quad);
                            Toast.makeText(QuadListActivity.this, "Eliminado", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                }
            });
            
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mQuadViewModel.getAllQuads().observe(this, quads -> {
            adapter.submitList(quads);
            boolean isEmpty = (quads == null || quads.isEmpty());
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * Actualiza el estado visual de los botones de ordenación, resaltando el botón activo.
     * @param activeButton El botón que ha sido pulsado y debe resaltarse.
     */
    private void updateButtonsColor(Button activeButton) {
        int colorActive = ContextCompat.getColor(this, R.color.colorBtnEdit); // Marrón
        int colorInactive = 0xFFCCCCCC; // Gris claro
        
        Button[] allButtons = {btnMatricula, btnTipo, btnPrecio};
        
        for (Button btn : allButtons) {
            if (btn == activeButton) {
                btn.setBackgroundColor(colorActive);
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundColor(colorInactive);
                btn.setTextColor(Color.BLACK);
            }
        }
    }

    /**
     * Prepara e inicia la actividad de edición para un quad específico.
     * @param current Objeto Quad que se desea editar.
     */
    private void editQuad(Quad current) {
        Intent intent = new Intent(this, QuadEdit.class);
        intent.putExtra(QuadEdit.QUAD_ID, current.getId());
        intent.putExtra(QuadEdit.QUAD_MATRICULA, current.getMatricula());
        intent.putExtra(QuadEdit.QUAD_DESCRIPCION, current.getDescripcion());
        intent.putExtra(QuadEdit.QUAD_PRECIO, current.getPrecio());
        intent.putExtra(QuadEdit.QUAD_ESMONOPLAZA, current.getEsmonoplaza());
        mStartForResult.launch(intent);
    }

    /**
     * Manejador para el resultado de la actividad QuadEdit.
     * Procesa la información devuelta para decidir si se debe insertar un nuevo 
     * registro o actualizar uno existente.
     */
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    String matricula = extras.getString(QuadEdit.QUAD_MATRICULA);
                    String descripcion = extras.getString(QuadEdit.QUAD_DESCRIPCION);
                    double precio = extras.getDouble(QuadEdit.QUAD_PRECIO);
                    boolean esMonoplaza = extras.getBoolean(QuadEdit.QUAD_ESMONOPLAZA);

                    Quad quad = new Quad(matricula, esMonoplaza, precio, descripcion);
                    int id = extras.getInt(QuadEdit.QUAD_ID, -1);

                    if (id != -1) {
                        quad.setId(id);
                        mQuadViewModel.update(quad);
                    } else {
                        if (mQuadViewModel.insert(quad) == -1) {
                            Toast.makeText(this, "Error: Matrícula duplicada", Toast.LENGTH_LONG).show();
                        }
                    }
                }
    });
}