package es.unizar.eina.SistemaReservas.ui;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import es.unizar.eina.SistemaReservas.database.Quad;

/**
 * Adaptador para el RecyclerView que gestiona la visualización de la lista de Quads.
 * Utiliza {@link ListAdapter} para gestionar de forma eficiente las actualizaciones
 * de la lista mediante cálculos de diferencias en un hilo de fondo.
 */
public class QuadListAdapter extends ListAdapter<Quad, QuadViewHolder> {

    /**
     * Interfaz de definición para los eventos de pulsación sobre los elementos de la lista.
     */
    public interface OnItemClickListener {
        /**
         * Invocado cuando se solicita editar la información de un Quad.
         * @param quad El objeto Quad seleccionado para edición.
         */
        void onEdit(Quad quad);

        /**
         * Invocado cuando se solicita la eliminación de un Quad de la base de datos.
         * @param quad El objeto Quad seleccionado para eliminación.
         */
        void onDelete(Quad quad);
    }

    /** Referencia al escuchador de eventos de click. */
    private final OnItemClickListener listener;

    /**
     * Constructor del adaptador.
     * 
     * @param diffCallback Implementación de {@link DiffUtil.ItemCallback} para comparar quads.
     * @param listener Implementación de la interfaz para manejar los eventos de los botones.
     */
    public QuadListAdapter(@NonNull DiffUtil.ItemCallback<Quad> diffCallback, OnItemClickListener listener) {
        super(diffCallback);
        this.listener = listener;
    }

    /**
     * Crea nuevos componentes visuales para los elementos de la lista.
     * 
     * @param parent El ViewGroup en el que se añadirá la nueva vista.
     * @param viewType El tipo de vista (no utilizado en esta implementación simple).
     * @return Una nueva instancia de {@link QuadViewHolder}.
     */
    @Override
    public QuadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return QuadViewHolder.create(parent);
    }

    /**
     * Vincula los datos de un Quad en una posición específica con su contenedor visual.
     * 
     * @param holder El ViewHolder que debe ser actualizado.
     * @param position La posición del elemento dentro de la lista de datos.
     */
    @Override
    public void onBindViewHolder(QuadViewHolder holder, int position) {
        Quad current = getItem(position);
        holder.bind(current, listener);
    }

    /**
     * Clase auxiliar estática para calcular la diferencia entre dos listas de Quads.
     * Permite al RecyclerView realizar animaciones precisas y optimizar el rendimiento.
     */
    static class QuadDiff extends DiffUtil.ItemCallback<Quad> {
        /**
         * Comprueba si dos objetos representan el mismo registro en la base de datos.
         * @param oldItem Quad antiguo.
         * @param newItem Quad nuevo.
         * @return true si ambos tienen el mismo ID único.
         */
        @Override
        public boolean areItemsTheSame(@NonNull Quad oldItem, @NonNull Quad newItem) {
            return oldItem.getId() == newItem.getId();
        }

        /**
         * Comprueba si el contenido visual de dos quads con el mismo ID ha cambiado.
         * @param oldItem Quad antiguo.
         * @param newItem Quad nuevo.
         * @return true si todos los campos relevantes para la UI coinciden.
         */
        @Override
        public boolean areContentsTheSame(@NonNull Quad oldItem, @NonNull Quad newItem) {
            return oldItem.getMatricula().equals(newItem.getMatricula())
                    && oldItem.getDescripcion().equals(newItem.getDescripcion())
                    && oldItem.getPrecio() == newItem.getPrecio()
                    && oldItem.getEsmonoplaza() == newItem.getEsmonoplaza();
        }
    }
}