package es.unizar.eina.SistemaReservas.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AlertDialog; 
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import es.unizar.eina.SistemaReservas.R;
import es.unizar.eina.SistemaReservas.database.Quad;
import com.google.android.material.button.MaterialButtonToggleGroup;

/**
 * Actividad encargada de la selección de vehículos Quad para una reserva.
 * Permite filtrar por disponibilidad en fechas específicas, ordenar los vehículos 
 * por diversos criterios y gestionar la selección individual junto con el número de cascos.
 */
public class QuadSelectionActivity extends AppCompatActivity {

    /** Clave para devolver la lista de quads seleccionados en el resultado. */
    public static final String EXTRA_SELECTED_QUADS = "extra_selected_quads";
    /** Clave para recibir quads previamente seleccionados y restaurar su estado. */
    public static final String EXTRA_PRESELECTED_QUADS = "extra_preselected";
    /** Clave para recibir la fecha de recogida de la reserva. */
    public static final String EXTRA_FECHA_IN = "EXTRA_FECHA_IN";
    /** Clave para recibir la fecha de devolución de la reserva. */
    public static final String EXTRA_FECHA_OUT = "EXTRA_FECHA_OUT";
    /** Clave para recibir el ID de la reserva actual (útil en ediciones). */
    public static final String EXTRA_RESERVA_ID = "EXTRA_RESERVA_ID";

    /** Adaptador para la gestión de la lista de selección. */
    private QuadSelectionAdapter adapter;
    /** Componente de interfaz para mostrar la lista. */
    private RecyclerView recyclerView;
    /** Vista mostrada cuando no existen vehículos disponibles. */
    private TextView emptyView;
    
    /** Botón para ordenar la lista por matrícula. */
    private Button btnSortMatricula;
    /** Botón para ordenar la lista por tipo (monoplaza/biplaza). */
    private Button btnSortTipo;
    /** Botón para ordenar la lista por precio. */
    private Button btnSortPrecio;

    /** Almacén local de la lista bruta de quads cargada desde la base de datos. */
    private List<Quad> mLoadedQuads = new ArrayList<>(); 
    /** Lista de quads que ya estaban seleccionados antes de abrir la pantalla. */
    private List<SelectedQuad> mPreSelectedList; 
    /** Modo de ordenación actual: 0=Matricula, 1=Tipo, 2=Precio. */
    private int mCurrentSortMode = 0; 

    /**
     * Inicializa la actividad, recupera los parámetros del Intent y configura 
     * el observador para cargar los datos desde el ViewModel.
     * 
     * @param savedInstanceState Estado guardado de la instancia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quad_selection);

        // Vincular vistas
        recyclerView = findViewById(R.id.recycler_selection);
        emptyView = findViewById(R.id.empty_view_selection);
        btnSortMatricula = findViewById(R.id.sort_matricula);
        btnSortTipo = findViewById(R.id.sort_tipo);
        btnSortPrecio = findViewById(R.id.sort_precio);

        adapter = new QuadSelectionAdapter();

        adapter.setOnDetailsClickListener(this::showQuadDetails);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Recuperar datos del Intent
        mPreSelectedList = (List<SelectedQuad>) getIntent().getSerializableExtra(EXTRA_PRESELECTED_QUADS);
        String fechaIn = getIntent().getStringExtra(EXTRA_FECHA_IN);
        String fechaOut = getIntent().getStringExtra(EXTRA_FECHA_OUT);
        int reservaId = getIntent().getIntExtra(EXTRA_RESERVA_ID, -1);

        // Cargar datos desde ViewModel
        QuadViewModel quadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);

        if (fechaIn != null && fechaOut != null) {
            quadViewModel.getAvailableQuads(fechaIn, fechaOut, reservaId, this::onQuadsLoaded);
        } else {
            quadViewModel.getAllQuads().observe(this, this::onQuadsLoaded);
        }

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroupSelection);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.sort_matricula) applySort(0);
                else if (checkedId == R.id.sort_tipo) applySort(1);
                else if (checkedId == R.id.sort_precio) applySort(2);
            }
        });

        // Botones Cancelar/Confirmar
        findViewById(R.id.btn_cancel_selection).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        findViewById(R.id.btn_confirm_selection).setOnClickListener(v -> {
            ArrayList<SelectedQuad> selected = (ArrayList<SelectedQuad>) adapter.getSelectedItems();
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_SELECTED_QUADS, selected);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Estado visual inicial de botones
        updateSortButtonsUI(0);
    }

    /**
     * Muestra un diálogo informativo con todos los detalles de un quad seleccionado.
     * 
     * @param quad El objeto visual que contiene la información del vehículo.
     */
    private void showQuadDetails(SelectedQuad quad) {
        new AlertDialog.Builder(this)
                .setTitle("Detalles del Quad")
                .setMessage(
                        "Matrícula: " + quad.getMatricula() + "\n" +
                        "Tipo: " + (quad.isMonoplaza() ? "Monoplaza" : "Biplaza") + "\n" +
                        "Precio: " + quad.getPrecio() + "€/h\n\n" +
                        "Descripción:\n" + quad.getDescripcion()
                )
                .setPositiveButton("Cerrar", null)
                .show();
    }

    /**
     * Procesa la lista de quads recibida desde la capa de datos.
     * Gestiona la visibilidad de la interfaz en caso de lista vacía e inicia la ordenación.
     * 
     * @param quads Lista de quads devuelta por el ViewModel.
     */
    private void onQuadsLoaded(List<Quad> quads) {
        if (quads == null) quads = new ArrayList<>();
        mLoadedQuads = quads; 
        
        if (mLoadedQuads.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            adapter.setItems(new ArrayList<>());
            Toast.makeText(this, "No hay quads disponibles en esas fechas.", Toast.LENGTH_LONG).show();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            applySort(mCurrentSortMode);
        }
    }

    /**
     * Ejecuta la lógica de ordenación de los vehículos cargados y sincroniza 
     * el estado de selección actual de la UI para no perder datos al reordenar.
     * 
     * @param sortMode Criterio de ordenación (0: Matrícula, 1: Tipo, 2: Precio).
     */
    private void applySort(int sortMode) {
        mCurrentSortMode = sortMode;
        updateSortButtonsUI(sortMode);

        if (mLoadedQuads == null || mLoadedQuads.isEmpty()) return;

        Collections.sort(mLoadedQuads, (q1, q2) -> {
            switch (sortMode) {
                case 1: 
                    int typeCompare = Boolean.compare(q2.getEsmonoplaza(), q1.getEsmonoplaza());
                    if (typeCompare != 0) return typeCompare;
                    return q1.getMatricula().compareTo(q2.getMatricula());
                case 2: 
                    int priceCompare = Double.compare(q1.getPrecio(), q2.getPrecio());
                    if (priceCompare != 0) return priceCompare;
                    return q1.getMatricula().compareTo(q2.getMatricula());
                case 0: 
                default:
                    return q1.getMatricula().compareTo(q2.getMatricula());
            }
        });

        List<SelectedQuad> currentAdapterItems = adapter.getAllItems(); 
        
        List<SelectedQuad> newUiList = new ArrayList<>();
        for (Quad q : mLoadedQuads) {
            SelectedQuad sq = new SelectedQuad(
                q.getId(), 
                q.getMatricula(), 
                q.getEsmonoplaza(), 
                q.getPrecio(), 
                q.getDescripcion() 
            );
            
            boolean restored = false;
            
            if (currentAdapterItems != null) {
                for (SelectedQuad current : currentAdapterItems) {
                    if (current.getId() == q.getId()) {
                        sq.setSelected(current.isSelected());
                        sq.setNumCascos(current.getNumCascos());
                        restored = true;
                        break;
                    }
                }
            }
            
            if (!restored && mPreSelectedList != null) {
                for (SelectedQuad pre : mPreSelectedList) {
                    if (pre.getId() == q.getId()) {
                        sq.setSelected(true);
                        sq.setNumCascos(pre.getNumCascos());
                        break;
                    }
                }
            }
            newUiList.add(sq);
        }

        adapter.setItems(newUiList);
    }

    /**
     * Actualiza el estado visual de los botones de ordenación para reflejar 
     * cuál es el filtro que se está aplicando actualmente.
     * 
     * @param activeIndex Índice del botón pulsado.
     */
    private void updateSortButtonsUI(int activeIndex) {
        int colorActive = getResources().getColor(R.color.colorBtnEdit);
        int colorInactive = 0xFFCCCCCC;

        btnSortMatricula.setBackgroundColor(activeIndex == 0 ? colorActive : colorInactive);
        btnSortMatricula.setTextColor(activeIndex == 0 ? Color.WHITE : Color.BLACK);

        btnSortTipo.setBackgroundColor(activeIndex == 1 ? colorActive : colorInactive);
        btnSortTipo.setTextColor(activeIndex == 1 ? Color.WHITE : Color.BLACK);

        btnSortPrecio.setBackgroundColor(activeIndex == 2 ? colorActive : colorInactive);
        btnSortPrecio.setTextColor(activeIndex == 2 ? Color.WHITE : Color.BLACK);
    }
}