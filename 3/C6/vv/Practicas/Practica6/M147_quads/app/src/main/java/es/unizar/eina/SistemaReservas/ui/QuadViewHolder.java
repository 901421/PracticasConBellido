package es.unizar.eina.SistemaReservas.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import es.unizar.eina.SistemaReservas.R;
import es.unizar.eina.SistemaReservas.database.Quad;

/**
 * Contenedor de vistas (ViewHolder) para los elementos de la lista de Quads.
 * Se encarga de almacenar las referencias a los componentes de la interfaz de cada fila
 * para evitar llamadas repetitivas a findViewById y mejorar el rendimiento del scroll.
 */
class QuadViewHolder extends RecyclerView.ViewHolder {
    
    /** Vista de texto para mostrar la matrícula del vehículo. */
    private final TextView mTvMatricula;
    
    /** Vista de texto para mostrar el tipo de vehículo (Monoplaza/Biplaza). */
    private final TextView mTvDetalles;
    
    /** Botón para iniciar la acción de edición del vehículo. */
    private final Button mBtnEdit;
    
    /** Botón para iniciar la acción de eliminación del vehículo. */
    private final Button mBtnDelete;

    /**
     * Constructor privado encargado de vincular los componentes del layout XML con los atributos de la clase.
     * @param itemView Vista raíz de la fila del listado.
     */
    private QuadViewHolder(View itemView) {
        super(itemView);
        mTvMatricula = itemView.findViewById(R.id.tvMatricula);
        mTvDetalles = itemView.findViewById(R.id.tvDetalles);
        mBtnEdit = itemView.findViewById(R.id.btnEdit);
        mBtnDelete = itemView.findViewById(R.id.btnDelete);
    }

    /**
     * Vincula los datos de un objeto {@link Quad} con los componentes visuales del contenedor.
     * Configura los textos informativos (matrícula, tipo, precio) y asigna los escuchadores 
     * de eventos para las acciones de edición y borrado.
     * 
     * @param quad El objeto Quad cuyos datos se van a mostrar.
     * @param listener El escuchador de eventos para manejar las pulsaciones en los botones.
     */
    public void bind(Quad quad, QuadListAdapter.OnItemClickListener listener) {
        mTvMatricula.setText(quad.getMatricula());
        
        String tipo = quad.getEsmonoplaza() ? "Monoplaza" : "Biplaza";
        mTvDetalles.setText(tipo);

        TextView tvPrecio = itemView.findViewById(R.id.tvPrecio);
        if (tvPrecio != null) {
            tvPrecio.setText(String.format("%.2f€/h", quad.getPrecio()));
        }

        mBtnEdit.setOnClickListener(v -> listener.onEdit(quad));
        mBtnDelete.setOnClickListener(v -> listener.onDelete(quad));
    }

    /**
     * Método de factoría estática para inflar el layout y crear una nueva instancia del ViewHolder.
     * 
     * @param parent El contenedor padre donde se alojará la nueva vista.
     * @return Una nueva instancia de {@link QuadViewHolder} vinculada al layout correspondiente.
     */
    static QuadViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new QuadViewHolder(view);
    }
}