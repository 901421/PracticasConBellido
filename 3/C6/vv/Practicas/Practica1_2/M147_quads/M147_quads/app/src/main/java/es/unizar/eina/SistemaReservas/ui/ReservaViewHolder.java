package es.unizar.eina.SistemaReservas.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import es.unizar.eina.SistemaReservas.R;
import es.unizar.eina.SistemaReservas.database.ReservaConQuads; 

/**
 * Contenedor de vistas (ViewHolder) para los elementos del listado de reservas.
 * Almacena las referencias a los componentes de la interfaz de cada fila para optimizar
 * el rendimiento del RecyclerView y gestiona la vinculación de los datos del cliente.
 */
class ReservaViewHolder extends RecyclerView.ViewHolder {

    /** Vista de texto para el nombre del cliente titular. */
    private final TextView tvCliente;
    /** Vista de texto para el teléfono de contacto. */
    private final TextView tvTelefono;
    /** Vista de texto para el rango de fechas (recogida y devolución). */
    private final TextView tvFechas;
    
    /** Botón para visualizar el desglose de la reserva. */
    private final Button btnDetails;
    /** Botón para acceder al formulario de edición de la reserva. */
    private final Button btnEdit;
    /** Botón para solicitar la eliminación de la reserva. */
    private final Button btnDelete;
    /** Botón para iniciar el proceso de envío de confirmación. */
    private final Button btnSend;

    /**
     * Constructor del ViewHolder. Realiza el mapeo de los componentes XML a los atributos de la clase.
     * @param itemView Vista raíz de la fila de reserva.
     */
    private ReservaViewHolder(View itemView) {
        super(itemView);
        tvCliente = itemView.findViewById(R.id.tv_res_cliente);
        tvTelefono = itemView.findViewById(R.id.tv_res_telefono);
        tvFechas = itemView.findViewById(R.id.tv_res_fechas);
        
        btnDetails = itemView.findViewById(R.id.btnDetailsReserva);
        btnEdit = itemView.findViewById(R.id.btnEditReserva);
        btnDelete = itemView.findViewById(R.id.btnDeleteReserva);
        btnSend = itemView.findViewById(R.id.btnSendReserva);
    }

    /**
     * Asocia los datos de un objeto {@link ReservaConQuads} con los elementos visuales de la fila.
     * Configura el texto descriptivo del cliente, sus datos de contacto y el periodo temporal,
     * además de asignar los escuchadores de eventos para todas las acciones disponibles.
     * 
     * @param item Objeto de relación que contiene la reserva y sus vehículos asociados.
     * @param listener Implementación de la interfaz de clics del adaptador.
     */
    public void bind(ReservaConQuads item, ReservaListAdapter.OnItemClickListener listener) {
        tvCliente.setText(item.reserva.getNombreCliente());
        tvTelefono.setText("Tlf: " + item.reserva.getTelefono());
        tvFechas.setText(item.reserva.getFechaRecogida() + " -> " + item.reserva.getFechaDevolucion());

        btnDetails.setOnClickListener(v -> listener.onDetails(item));
        btnEdit.setOnClickListener(v -> listener.onEdit(item));
        btnDelete.setOnClickListener(v -> listener.onDelete(item));
        btnSend.setOnClickListener(v -> listener.onSend(item));
    }

    /**
     * Método de factoría para instanciar el ViewHolder inflando su layout correspondiente.
     * 
     * @param parent El contenedor padre donde se alojará la vista.
     * @return Una nueva instancia de {@link ReservaViewHolder}.
     */
    static ReservaViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }
}