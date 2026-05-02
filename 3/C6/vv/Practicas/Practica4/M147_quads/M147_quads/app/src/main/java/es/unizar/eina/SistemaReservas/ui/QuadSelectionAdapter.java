package es.unizar.eina.SistemaReservas.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import es.unizar.eina.SistemaReservas.R;

/**
 * Adaptador para el RecyclerView encargado de la selección de Quads en una reserva.
 * Gestiona la visualización de los vehículos disponibles, su estado de selección
 * y la asignación individual del número de cascos por vehículo mediante un diálogo emergente.
 */
public class QuadSelectionAdapter extends RecyclerView.Adapter<QuadSelectionAdapter.ViewHolder> {

    /**
     * Interfaz para la gestión de eventos de visualización de detalles.
     */
    public interface OnDetailsClickListener {
        /**
         * Invocado cuando el usuario solicita ver la información detallada de un vehículo.
         * @param quad El objeto {@link SelectedQuad} pulsado.
         */
        void onDetailsClick(SelectedQuad quad);
    }

    /** Lista de elementos visuales con estado de selección. */
    private List<SelectedQuad> mItems = new ArrayList<>();
    
    /** Escuchador para los eventos de click en detalles. */
    private OnDetailsClickListener listener;

    /**
     * Define el escuchador para los eventos de detalles.
     * @param listener Implementación de la interfaz de click.
     */
    public void setOnDetailsClickListener(OnDetailsClickListener listener) {
        this.listener = listener;
    }

    /**
     * Actualiza la lista de elementos del adaptador y refresca la interfaz.
     * @param items Nueva lista de quads con estado de selección.
     */
    public void setItems(List<SelectedQuad> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    /**
     * Filtra y devuelve únicamente los elementos que han sido marcados por el usuario.
     * @return Lista de objetos {@link SelectedQuad} seleccionados.
     */
    public List<SelectedQuad> getSelectedItems() {
        List<SelectedQuad> selected = new ArrayList<>();
        for (SelectedQuad item : mItems) {
            if (item.isSelected()) selected.add(item);
        }
        return selected;
    }
    
    /**
     * Devuelve la totalidad de los elementos gestionados por el adaptador.
     * @return Lista completa de elementos.
     */
    public List<SelectedQuad> getAllItems() {
        return mItems;
    }

    /**
     * Crea el contenedor visual para un elemento de la lista.
     * @param parent Contenedor padre.
     * @param viewType Tipo de vista.
     * @return Una nueva instancia de {@link ViewHolder}.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quad_selection, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Asocia los datos de un quad en una posición dada con su representación visual.
     * @param holder El contenedor de la vista.
     * @param position Índice del elemento en la lista.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

    /** @return Cantidad total de elementos en el adaptador. */
    @Override
    public int getItemCount() { return mItems.size(); }

    /**
     * Clase interna que describe la vista de un elemento y sus metadatos dentro del RecyclerView.
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        /** Casilla de selección del vehículo. */
        CheckBox cbSelect;
        /** Texto de la matrícula. */
        TextView tvMatricula;
        /** Texto del tipo de plaza. */
        TextView tvTipo;
        /** Botón para ver detalles técnicos. */
        Button btnDetails;
        /** Contenedor del selector de cascos. */
        LinearLayout layoutCascos;
        /** Botón que despliega el selector de cascos. */
        Button btnCascosPopup;

        /**
         * Constructor del ViewHolder. Vincula los componentes del layout.
         * @param itemView Vista raíz del elemento de la lista.
         */
        ViewHolder(View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_select);
            tvMatricula = itemView.findViewById(R.id.tv_matricula);
            tvTipo = itemView.findViewById(R.id.tv_tipo);
            btnDetails = itemView.findViewById(R.id.btn_details_selection);
            layoutCascos = itemView.findViewById(R.id.layout_cascos);
            btnCascosPopup = itemView.findViewById(R.id.btn_cascos_popup);
        }

        /**
         * Vincula un objeto de datos con los componentes de la interfaz.
         * Configura la lógica de selección, visibilidad de controles y el diálogo de cascos.
         * @param item El objeto {@link SelectedQuad} a visualizar.
         */
        void bind(SelectedQuad item) {
            tvMatricula.setText(item.getMatricula());
            tvTipo.setText(item.isMonoplaza() ? "Monoplaza" : "Biplaza");

            btnDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetailsClick(item);
                }
            });

            cbSelect.setOnCheckedChangeListener(null); 
            cbSelect.setChecked(item.isSelected());
            layoutCascos.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);

            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                layoutCascos.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });

            btnCascosPopup.setText(String.valueOf(item.getNumCascos()));

            btnCascosPopup.setOnClickListener(v -> {
                final String[] opciones;
                if (item.isMonoplaza()) {
                    opciones = new String[]{"0 Cascos", "1 Casco"};
                } else {
                    opciones = new String[]{"0 Cascos", "1 Casco", "2 Cascos"};
                }

                new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Número de Cascos")
                        .setItems(opciones, (dialog, which) -> {
                            item.setNumCascos(which);
                            btnCascosPopup.setText(String.valueOf(which));
                        })
                        .show();
            });
        }
    }
}