package es.unizar.eina.SistemaReservas.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import es.unizar.eina.SistemaReservas.R;
import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.database.Reserva;
import es.unizar.eina.SistemaReservas.database.ReservaConQuads;
import es.unizar.eina.SistemaReservas.database.ReservaQuad;
import es.unizar.eina.SistemaReservas.send.SendAbstraction;
import es.unizar.eina.SistemaReservas.send.SendAbstractionImpl;

import com.google.android.material.button.MaterialButtonToggleGroup;

/**
 * Actividad encargada de gestionar y visualizar el listado completo de reservas.
 * Permite realizar la ordenación dinámica de los datos, consultar detalles,
 * editar registros, eliminar reservas y enviar confirmaciones mediante servicios externos
 * (WhatsApp/SMS) aplicando el patrón Bridge.
 */
public class ReservaListActivity extends AppCompatActivity {

    private ReservaViewModel mReservaViewModel;
    private RecyclerView recyclerView;
    private TextView emptyView;

    // Elementos de la nueva UI
    private View viewDimmer;
    private com.google.android.material.card.MaterialCardView panelOrdenar, panelFiltrar;
    
    // Opciones de ordenación para actualizar flechas
    private TextView optSortNombre, optSortTelefono, optSortFechaIn, optSortFechaOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_reservas);

        recyclerView = findViewById(R.id.recyclerview_reservas);
        emptyView = findViewById(R.id.empty_view_reservas);
        viewDimmer = findViewById(R.id.view_dimmer);
        panelOrdenar = findViewById(R.id.panel_ordenar);
        panelFiltrar = findViewById(R.id.panel_filtrar);
        
        optSortNombre = findViewById(R.id.opt_sort_nombre);
        optSortTelefono = findViewById(R.id.opt_sort_telefono);
        optSortFechaIn = findViewById(R.id.opt_sort_fecha_in);
        optSortFechaOut = findViewById(R.id.opt_sort_fecha_out);

        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);

        // 1. CONFIGURACIÓN DE DISPARADORES (ORDENAR / FILTRAR)
        findViewById(R.id.btn_ordenar_trigger).setOnClickListener(v -> togglePanel(panelOrdenar));
        findViewById(R.id.btn_filtrar_trigger).setOnClickListener(v -> togglePanel(panelFiltrar));

        // 2. BOTONES CERRAR PANELES
        findViewById(R.id.btn_close_ordenar).setOnClickListener(v -> hidePanels());
        findViewById(R.id.btn_close_filtrar).setOnClickListener(v -> hidePanels());
        viewDimmer.setOnClickListener(v -> hidePanels());

        // 3. OPCIONES DE ORDENACIÓN (CON LÓGICA DE TOGGLE Y FLECHAS)
        optSortNombre.setOnClickListener(v -> applySort(ReservaViewModel.SortType.CLIENTE));
        optSortTelefono.setOnClickListener(v -> applySort(ReservaViewModel.SortType.TELEFONO));
        optSortFechaIn.setOnClickListener(v -> applySort(ReservaViewModel.SortType.FECHA_IN));
        optSortFechaOut.setOnClickListener(v -> applySort(ReservaViewModel.SortType.FECHA_OUT));

        // 4. OPCIONES DE FILTRADO
        findViewById(R.id.opt_filter_previstas).setOnClickListener(v -> { mReservaViewModel.setFilterType(ReservaViewModel.FilterType.PREVISTAS); hidePanels(); });
        findViewById(R.id.opt_filter_vigentes).setOnClickListener(v -> { mReservaViewModel.setFilterType(ReservaViewModel.FilterType.VIGENTES); hidePanels(); });
        findViewById(R.id.opt_filter_caducadas).setOnClickListener(v -> { mReservaViewModel.setFilterType(ReservaViewModel.FilterType.CADUCADAS); hidePanels(); });
        findViewById(R.id.opt_filter_todas).setOnClickListener(v -> { mReservaViewModel.setFilterType(ReservaViewModel.FilterType.TODAS); hidePanels(); });

        updateSortArrows();

        ActivityResultLauncher<Intent> editReservaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null && extras.containsKey(ReservaEdit.RES_ID)) {
                            int id = extras.getInt(ReservaEdit.RES_ID);
                            String cliente = extras.getString(ReservaEdit.RES_CLIENTE);
                            String tlf = extras.getString(ReservaEdit.RES_TELEFONO);
                            String fechaIn = extras.getString(ReservaEdit.RES_FECHA_IN);
                            String fechaOut = extras.getString(ReservaEdit.RES_FECHA_OUT);
                            ArrayList<SelectedQuad> selectedUI = (ArrayList<SelectedQuad>) extras.getSerializable(ReservaEdit.RES_LISTA_QUADS);

                            Reserva reserva = new Reserva(cliente, tlf, fechaIn, fechaOut);
                            reserva.setId(id);

                            List<ReservaQuad> dbQuads = new ArrayList<>();
                            if (selectedUI != null) {
                                for (SelectedQuad sq : selectedUI) {
                                    dbQuads.add(new ReservaQuad(id, sq.getId(), sq.getNumCascos()));
                                }
                            }
                            mReservaViewModel.update(reserva, dbQuads);
                            Toast.makeText(this, "Reserva actualizada", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        final ReservaListAdapter adapter = new ReservaListAdapter(new ReservaListAdapter.ReservaDiff(), 
            new ReservaListAdapter.OnItemClickListener() {
                @Override
                public void onDetails(ReservaConQuads reserva) { mostrarDialogoDetalles(reserva); }

                @Override
                public void onEdit(ReservaConQuads reservaItem) {
                    mReservaViewModel.getReservaQuads(reservaItem.reserva.getId(), listaRelaciones -> {
                        ArrayList<SelectedQuad> listForEdit = new ArrayList<>();
                        for(Quad q : reservaItem.quads) {
                            SelectedQuad sq = new SelectedQuad(q.getId(), q.getMatricula(), q.getEsmonoplaza(), q.getPrecio(), q.getDescripcion()); 
                            sq.setSelected(true);
                            int cascosReales = 1; 
                            for (ReservaQuad rq : listaRelaciones) {
                                if (rq.getQuadId() == q.getId()) { cascosReales = rq.getNumCascos(); break; }
                            }
                            sq.setNumCascos(cascosReales);
                            listForEdit.add(sq);
                        }
                        runOnUiThread(() -> {
                            Intent intent = new Intent(ReservaListActivity.this, ReservaEdit.class);
                            intent.putExtra(ReservaEdit.RES_ID, reservaItem.reserva.getId());
                            intent.putExtra(ReservaEdit.RES_CLIENTE, reservaItem.reserva.getNombreCliente());
                            intent.putExtra(ReservaEdit.RES_TELEFONO, reservaItem.reserva.getTelefono());
                            intent.putExtra(ReservaEdit.RES_FECHA_IN, reservaItem.reserva.getFechaRecogida());
                            intent.putExtra(ReservaEdit.RES_FECHA_OUT, reservaItem.reserva.getFechaDevolucion());
                            intent.putExtra(ReservaEdit.RES_LISTA_QUADS, listForEdit);
                            editReservaLauncher.launch(intent);
                        });
                    });
                }

                @Override
                public void onDelete(ReservaConQuads reserva) {
                    new AlertDialog.Builder(ReservaListActivity.this)
                        .setTitle("Eliminar Reserva")
                        .setMessage("¿Eliminar reserva de " + reserva.reserva.getNombreCliente() + "?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            mReservaViewModel.delete(reserva.reserva);
                            Toast.makeText(ReservaListActivity.this, "Reserva eliminada", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                }

                @Override
                public void onSend(ReservaConQuads reservaItem) {
                    mReservaViewModel.getReservaQuads(reservaItem.reserva.getId(), listaRelaciones -> {
                        runOnUiThread(() -> {
                            String mensajeDetallado = construirMensajeCompleto(reservaItem, listaRelaciones);
                            mostrarDialogoEnvio(reservaItem, mensajeDetallado);
                        });
                    });
                }
            });
        
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mReservaViewModel.getAllReservas().observe(this, reservas -> {
            adapter.submitList(reservas);
            recyclerView.setVisibility((reservas == null || reservas.isEmpty()) ? View.GONE : View.VISIBLE);
            emptyView.setVisibility((reservas == null || reservas.isEmpty()) ? View.VISIBLE : View.GONE);
        });
    }

    private void applySort(ReservaViewModel.SortType type) {
        mReservaViewModel.setSortType(type);
        updateSortArrows();
        // Opcional: Cerrar el panel al cambiar el tipo, o dejarlo abierto para el segundo click
        // hidePanels(); 
    }

    private void updateSortArrows() {
        ReservaViewModel.SortType currentType = mReservaViewModel.getSortType();
        ReservaViewModel.SortDirection currentDir = mReservaViewModel.getSortDirection();
        String arrow = (currentDir == ReservaViewModel.SortDirection.ASC) ? " ↑" : " ↓";

        optSortNombre.setText("Nombre" + (currentType == ReservaViewModel.SortType.CLIENTE ? arrow : ""));
        optSortTelefono.setText("Teléfono" + (currentType == ReservaViewModel.SortType.TELEFONO ? arrow : ""));
        optSortFechaIn.setText("Fecha de recogida" + (currentType == ReservaViewModel.SortType.FECHA_IN ? arrow : ""));
        optSortFechaOut.setText("Fecha de devolución" + (currentType == ReservaViewModel.SortType.FECHA_OUT ? arrow : ""));
    }

    private void togglePanel(com.google.android.material.card.MaterialCardView panel) {
        boolean isVisible = (panel.getVisibility() == View.VISIBLE);
        hidePanels();
        if (!isVisible) {
            panel.setVisibility(View.VISIBLE);
            viewDimmer.setVisibility(View.VISIBLE);
        }
    }

    private void hidePanels() {
        panelOrdenar.setVisibility(View.GONE);
        panelFiltrar.setVisibility(View.GONE);
        viewDimmer.setVisibility(View.GONE);
    }

    private String construirMensajeCompleto(ReservaConQuads item, java.util.List<ReservaQuad> relaciones) {
        StringBuilder sb = new StringBuilder();
        long dias = 1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date in = sdf.parse(item.reserva.getFechaRecogida());
            Date out = sdf.parse(item.reserva.getFechaDevolucion());
            long diff = out.getTime() - in.getTime();
            dias = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            if (dias < 1) dias = 1; 
        } catch (Exception e) {}

        double precioTotal = item.reserva.getPrecioTotal();
        int totalCascos = 0;
        if (relaciones != null) { for (ReservaQuad rq : relaciones) totalCascos += rq.getNumCascos(); }

        sb.append("📅 *CONFIRMACIÓN DE RESERVA* 📅\n\n");
        sb.append("👤 *Cliente:* ").append(item.reserva.getNombreCliente()).append("\n");
        sb.append("📞 *Contacto:* ").append(item.reserva.getTelefono()).append("\n\n");
        sb.append("🗓 *Recogida:* ").append(item.reserva.getFechaRecogida()).append("\n");
        sb.append("🗓 *Devolución:* ").append(item.reserva.getFechaDevolucion()).append("\n");
        sb.append("⏳ *Duración:* ").append(dias).append(" día(s)\n\n");
        sb.append("🏍 *Vehículos:* ").append(item.quads.size()).append("\n");
        sb.append("🪖 *Total Cascos:* ").append(totalCascos).append("\n\n");
        sb.append("💶 *PRECIO ESTIMADO:* ").append(String.format(Locale.getDefault(), "%.2f", precioTotal)).append("€");
        return sb.toString();
    }

    private void mostrarDialogoEnvio(ReservaConQuads reservaItem, String mensajeFinal) {
        String[] options = {"WhatsApp", "SMS"};
        new AlertDialog.Builder(this)
            .setTitle("Enviar confirmación")
            .setItems(options, (dialog, which) -> {
                String method = (which == 0) ? "WHATSAPP" : "SMS";
                SendAbstraction sender = new SendAbstractionImpl(ReservaListActivity.this, method);
                sender.send(reservaItem.reserva.getTelefono(), mensajeFinal);
            }).show();
    }

    private void mostrarDialogoDetalles(ReservaConQuads item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reserva_detalles, null);
        builder.setView(view);

        TextView tvCliente = view.findViewById(R.id.dialog_cliente);
        TextView tvTelefono = view.findViewById(R.id.dialog_telefono);
        TextView tvFechas = view.findViewById(R.id.dialog_fechas);
        TextView tvQuads = view.findViewById(R.id.dialog_lista_quads);

        tvCliente.setText(item.reserva.getNombreCliente());
        tvTelefono.setText(item.reserva.getTelefono());
        tvFechas.setText("Desde: " + item.reserva.getFechaRecogida() + "\nHasta: " + item.reserva.getFechaDevolucion());

        if (item.quads != null && !item.quads.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Quad q : item.quads) {
                sb.append("• [").append(q.getMatricula()).append("] ").append(q.getEsmonoplaza() ? "Monoplaza" : "Biplaza").append(" - ").append(q.getPrecio()).append("€/d\n");
            }
            sb.append("\nPrecio Total Reserva: ").append(String.format(Locale.getDefault(), "%.2f", item.reserva.getPrecioTotal())).append("€");
            tvQuads.setText(sb.toString());
        } else {
            tvQuads.setText("No hay quads asignados.");
        }
        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }
}