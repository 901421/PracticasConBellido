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
 */
class ReservaViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvCliente;
    private final TextView tvFechas;
    
    private final Button btnEdit;
    private final Button btnDelete;
    private final Button btnSend;

    private ReservaViewHolder(View itemView) {
        super(itemView);
        tvCliente = itemView.findViewById(R.id.tv_res_cliente);
        tvFechas = itemView.findViewById(R.id.tv_res_fechas);
        
        btnEdit = itemView.findViewById(R.id.btnEditReserva);
        btnDelete = itemView.findViewById(R.id.btnDeleteReserva);
        btnSend = itemView.findViewById(R.id.btnSendReserva);
    }

    public void bind(ReservaConQuads item, ReservaListAdapter.OnItemClickListener listener) {
        tvCliente.setText(item.reserva.getNombreCliente());
        tvFechas.setText(item.reserva.getFechaRecogida() + " -> " + item.reserva.getFechaDevolucion());

        // Al pulsar sobre el nombre o el fondo de la card se abren los detalles
        itemView.setOnClickListener(v -> listener.onDetails(item));
        
        btnEdit.setOnClickListener(v -> listener.onEdit(item));
        btnDelete.setOnClickListener(v -> listener.onDelete(item));
        btnSend.setOnClickListener(v -> listener.onSend(item));
    }

    static ReservaViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }
}