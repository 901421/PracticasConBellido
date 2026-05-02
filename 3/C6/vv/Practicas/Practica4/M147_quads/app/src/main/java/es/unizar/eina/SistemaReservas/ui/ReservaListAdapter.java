package es.unizar.eina.SistemaReservas.ui;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import es.unizar.eina.SistemaReservas.database.ReservaConQuads;

/**
 * Adaptador encargado de gestionar la visualización de la lista de reservas en el RecyclerView.
 * Utiliza {@link ListAdapter} para optimizar las actualizaciones de la interfaz mediante
 * el cálculo de diferencias entre listas en un hilo secundario.
 */
public class ReservaListAdapter extends ListAdapter<ReservaConQuads, ReservaViewHolder> {

    /**
     * Interfaz para la gestión de los eventos de pulsación sobre los elementos de la lista de reservas.
     */
    public interface OnItemClickListener {
        /**
         * Invocado para iniciar la edición de una reserva.
         * @param reserva Objeto de relación con los datos de la reserva seleccionada.
         */
        void onEdit(ReservaConQuads reserva);

        /**
         * Invocado para solicitar la eliminación de una reserva.
         * @param reserva Objeto de relación con los datos de la reserva seleccionada.
         */
        void onDelete(ReservaConQuads reserva);

        /**
         * Invocado para visualizar el desglose detallado de una reserva.
         * @param reserva Objeto de relación con los datos de la reserva seleccionada.
         */
        void onDetails(ReservaConQuads reserva);

        /**
         * Invocado para proceder al envío de la confirmación al cliente.
         * @param reserva Objeto de relación con los datos de la reserva seleccionada.
         */
        void onSend(ReservaConQuads reserva);
    }

    /** Escuchador para los eventos de interacción con los elementos de la lista. */
    private final OnItemClickListener listener;

    /**
     * Constructor del adaptador.
     * 
     * @param diffCallback Implementación de {@link DiffUtil.ItemCallback} para comparar reservas.
     * @param listener Implementación de la interfaz para el manejo de clics.
     */
    public ReservaListAdapter(@NonNull DiffUtil.ItemCallback<ReservaConQuads> diffCallback, OnItemClickListener listener) {
        super(diffCallback);
        this.listener = listener;
    }

    /**
     * Infla el layout de la reserva y crea el ViewHolder correspondiente.
     * 
     * @param parent El ViewGroup contenedor.
     * @param viewType El tipo de vista.
     * @return Una nueva instancia de {@link ReservaViewHolder}.
     */
    @Override
    public ReservaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ReservaViewHolder.create(parent);
    }

    /**
     * Vincula los datos de una reserva en una posición dada con el ViewHolder.
     * 
     * @param holder El contenedor visual de la reserva.
     * @param position La posición del elemento en la lista de datos.
     */
    @Override
    public void onBindViewHolder(ReservaViewHolder holder, int position) {
        ReservaConQuads current = getItem(position);
        holder.bind(current, listener);
    }

    /**
     * Clase auxiliar para el cálculo de diferencias entre listas de objetos {@link ReservaConQuads}.
     */
    static class ReservaDiff extends DiffUtil.ItemCallback<ReservaConQuads> {
        /**
         * Comprueba si dos objetos representan el mismo registro de reserva.
         * @param oldItem Reserva antigua.
         * @param newItem Reserva nueva.
         * @return true si comparten el mismo identificador único.
         */
        @Override
        public boolean areItemsTheSame(@NonNull ReservaConQuads oldItem, @NonNull ReservaConQuads newItem) {
            return oldItem.reserva.getId() == newItem.reserva.getId();
        }

        /**
         * Comprueba si el contenido de los campos visuales de la reserva ha cambiado.
         * Realiza una comparación exhaustiva de los datos del cliente, fechas y 
         * cantidad de vehículos asignados.
         * 
         * @param oldItem Reserva antigua.
         * @param newItem Reserva nueva.
         * @return true si los contenidos relevantes para la UI coinciden.
         */
        @Override
        public boolean areContentsTheSame(@NonNull ReservaConQuads oldItem, @NonNull ReservaConQuads newItem) {
            return oldItem.reserva.getNombreCliente().equals(newItem.reserva.getNombreCliente()) &&
                   oldItem.reserva.getFechaRecogida().equals(newItem.reserva.getFechaRecogida()) &&
                   oldItem.reserva.getFechaDevolucion().equals(newItem.reserva.getFechaDevolucion()) &&
                   oldItem.quads.size() == newItem.quads.size();
        }
    }
}