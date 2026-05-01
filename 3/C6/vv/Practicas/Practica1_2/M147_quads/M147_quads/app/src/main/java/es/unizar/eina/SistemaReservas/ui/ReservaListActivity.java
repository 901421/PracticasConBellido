package es.unizar.eina.SistemaReservas.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.ImageView;

/**
 * Actividad encargada de gestionar y visualizar el listado completo de reservas.
 */
public class ReservaListActivity extends AppCompatActivity {

    private static final String TAG = "ReservaListActivity";

    /** ViewModel que gestiona el acceso a los datos de las reservas. */
    private ReservaViewModel mReservaViewModel;
    
    /** Botones de control de UI. */
    private Button btnOpenSort;
    private Button btnOpenFilter;
    
    /** Componentes de la lista. */
    private RecyclerView recyclerView;
    private TextView emptyView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_reservas);

        recyclerView = findViewById(R.id.recyclerview_reservas);
        emptyView = findViewById(R.id.empty_view_reservas);
        btnOpenSort = findViewById(R.id.btn_open_sort);
        btnOpenFilter = findViewById(R.id.btn_open_filter);
        
        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);

        btnOpenSort.setOnClickListener(v -> mostrarPanelOrdenacion());
        btnOpenFilter.setOnClickListener(v -> mostrarPanelFiltrado());

        ActivityResultLauncher<Intent> editReservaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null && extras.containsKey(ReservaEdit.RES_ID)) {
                            procesarResultadoEdicion(extras);
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
                        ArrayList<SelectedQuad> listForEdit = buildSelectedQuadList(reservaItem, listaRelaciones);
                        
                        runOnUiThread(() -> {
                            Intent intent = new Intent(ReservaListActivity.this, ReservaEdit.class);
                            intent.putExtra(ReservaEdit.RES_ID, reservaItem.reserva.getId());
                            intent.putExtra(ReservaEdit.RES_CLIENTE, reservaItem.reserva.getNombreCliente());
                            intent.putExtra(ReservaEdit.RES_TELEFONO, String.valueOf(reservaItem.reserva.getTelefono()));
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
                        .setTitle(R.string.delete_reserva_title)
                        .setMessage(getString(R.string.delete_reserva_msg, reserva.reserva.getNombreCliente()))
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            mReservaViewModel.delete(reserva.reserva);
                            Toast.makeText(ReservaListActivity.this, R.string.reserva_deleted, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.cancel, null)
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
            boolean isEmpty = (reservas == null || reservas.isEmpty());
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        updateButtonsColor();
    }

    private void procesarResultadoEdicion(@NonNull Bundle extras) {
        int id = extras.getInt(ReservaEdit.RES_ID);
        String cliente = extras.getString(ReservaEdit.RES_CLIENTE, "");
        String tlfStr = extras.getString(ReservaEdit.RES_TELEFONO, "0");
        int tlf = 0;
        try { tlf = Integer.parseInt(tlfStr); } catch (NumberFormatException ignored) {}
        
        String fechaIn = extras.getString(ReservaEdit.RES_FECHA_IN, "");
        String fechaOut = extras.getString(ReservaEdit.RES_FECHA_OUT, "");
        
        @SuppressWarnings("unchecked")
        ArrayList<SelectedQuad> selectedUI = (ArrayList<SelectedQuad>) extras.getSerializable(ReservaEdit.RES_LISTA_QUADS);

        Reserva reserva = new Reserva(cliente, tlf, fechaIn, fechaOut);
        reserva.setId(id);

        List<ReservaQuad> dbQuads = new ArrayList<>();
        if (selectedUI != null) {
            for (SelectedQuad sq : selectedUI) {
                dbQuads.add(new ReservaQuad(id, sq.getId(), sq.getNumCascos(), sq.getPrecio()));
            }
        }
        mReservaViewModel.update(reserva, dbQuads);
        Toast.makeText(this, R.string.reserva_updated, Toast.LENGTH_SHORT).show();
    }

    private ArrayList<SelectedQuad> buildSelectedQuadList(ReservaConQuads item, List<ReservaQuad> relaciones) {
        ArrayList<SelectedQuad> listForEdit = new ArrayList<>();
        for (Quad q : item.quads) {
            SelectedQuad sq = new SelectedQuad(q.getId(), q.getMatricula(), q.getEsmonoplaza(), q.getPrecio(), q.getDescripcion());
            sq.setSelected(true);
            int cascosReales = 1; 
            for (ReservaQuad rq : relaciones) {
                if (rq.getQuadId() == q.getId()) {
                    cascosReales = rq.getNumCascos();
                    break;
                }
            }
            sq.setNumCascos(cascosReales);
            listForEdit.add(sq);
        }
        return listForEdit;
    }

    private void mostrarPanelOrdenacion() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_sort, null);
        dialog.setContentView(view);

        updateSortIcons(view);

        int colorHighlight = ContextCompat.getColor(this, R.color.app_background_pastel);
        ReservaViewModel.SortType current = mReservaViewModel.getSortType();
        
        if (current == ReservaViewModel.SortType.CLIENTE) view.findViewById(R.id.option_sort_name).setBackgroundColor(colorHighlight);
        else if (current == ReservaViewModel.SortType.TELEFONO) view.findViewById(R.id.option_sort_phone).setBackgroundColor(colorHighlight);
        else if (current == ReservaViewModel.SortType.FECHA_IN) view.findViewById(R.id.option_sort_date_in).setBackgroundColor(colorHighlight);
        else if (current == ReservaViewModel.SortType.FECHA_OUT) view.findViewById(R.id.option_sort_date_out).setBackgroundColor(colorHighlight);

        view.findViewById(R.id.btn_close_sort).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.option_sort_name).setOnClickListener(v -> { toggleSort(ReservaViewModel.SortType.CLIENTE); dialog.dismiss(); });
        view.findViewById(R.id.option_sort_phone).setOnClickListener(v -> { toggleSort(ReservaViewModel.SortType.TELEFONO); dialog.dismiss(); });
        view.findViewById(R.id.option_sort_date_in).setOnClickListener(v -> { toggleSort(ReservaViewModel.SortType.FECHA_IN); dialog.dismiss(); });
        view.findViewById(R.id.option_sort_date_out).setOnClickListener(v -> { toggleSort(ReservaViewModel.SortType.FECHA_OUT); dialog.dismiss(); });

        dialog.show();
    }

    private void toggleSort(ReservaViewModel.SortType type) {
        if (mReservaViewModel.getSortType() == type) {
            ReservaViewModel.SortDirection currentDir = mReservaViewModel.getSortDirection();
            mReservaViewModel.setSortDirection(currentDir == ReservaViewModel.SortDirection.ASC ? 
                    ReservaViewModel.SortDirection.DESC : ReservaViewModel.SortDirection.ASC);
        } else {
            mReservaViewModel.setSortType(type);
            mReservaViewModel.setSortDirection(ReservaViewModel.SortDirection.ASC);
        }
        updateButtonsColor();
    }

    private void mostrarPanelFiltrado() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_filter, null);
        dialog.setContentView(view);

        int colorHighlight = ContextCompat.getColor(this, R.color.app_background_pastel);
        ReservaViewModel.FilterType current = mReservaViewModel.getFilterType();

        if (current == ReservaViewModel.FilterType.PREVISTAS) view.findViewById(R.id.option_filter_previstas).setBackgroundColor(colorHighlight);
        else if (current == ReservaViewModel.FilterType.VIGENTES) view.findViewById(R.id.option_filter_vigentes).setBackgroundColor(colorHighlight);
        else if (current == ReservaViewModel.FilterType.CADUCADAS) view.findViewById(R.id.option_filter_caducadas).setBackgroundColor(colorHighlight);
        else if (current == ReservaViewModel.FilterType.TODAS) view.findViewById(R.id.option_filter_todas).setBackgroundColor(colorHighlight);

        view.findViewById(R.id.option_filter_previstas).setOnClickListener(v -> { mReservaViewModel.setFilterType(ReservaViewModel.FilterType.PREVISTAS); dialog.dismiss(); updateButtonsColor(); });
        view.findViewById(R.id.option_filter_vigentes).setOnClickListener(v -> { mReservaViewModel.setFilterType(ReservaViewModel.FilterType.VIGENTES); dialog.dismiss(); updateButtonsColor(); });
        view.findViewById(R.id.option_filter_caducadas).setOnClickListener(v -> { mReservaViewModel.setFilterType(ReservaViewModel.FilterType.CADUCADAS); dialog.dismiss(); updateButtonsColor(); });
        view.findViewById(R.id.option_filter_todas).setOnClickListener(v -> { mReservaViewModel.setFilterType(ReservaViewModel.FilterType.TODAS); dialog.dismiss(); updateButtonsColor(); });
        view.findViewById(R.id.btn_close_filter).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateSortIcons(View panelView) {
        ReservaViewModel.SortType currentType = mReservaViewModel.getSortType();
        int iconRes = (mReservaViewModel.getSortDirection() == ReservaViewModel.SortDirection.ASC) ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down;
        
        ImageView ivName = panelView.findViewById(R.id.iv_sort_name_dir);
        ImageView ivPhone = panelView.findViewById(R.id.iv_sort_phone_dir);
        ImageView ivIn = panelView.findViewById(R.id.iv_sort_date_in_dir);
        ImageView ivOut = panelView.findViewById(R.id.iv_sort_date_out_dir);

        ivName.setVisibility(currentType == ReservaViewModel.SortType.CLIENTE ? View.VISIBLE : View.INVISIBLE);
        ivPhone.setVisibility(currentType == ReservaViewModel.SortType.TELEFONO ? View.VISIBLE : View.INVISIBLE);
        ivIn.setVisibility(currentType == ReservaViewModel.SortType.FECHA_IN ? View.VISIBLE : View.INVISIBLE);
        ivOut.setVisibility(currentType == ReservaViewModel.SortType.FECHA_OUT ? View.VISIBLE : View.INVISIBLE);

        if (currentType == ReservaViewModel.SortType.CLIENTE) ivName.setImageResource(iconRes);
        else if (currentType == ReservaViewModel.SortType.TELEFONO) ivPhone.setImageResource(iconRes);
        else if (currentType == ReservaViewModel.SortType.FECHA_IN) ivIn.setImageResource(iconRes);
        else if (currentType == ReservaViewModel.SortType.FECHA_OUT) ivOut.setImageResource(iconRes);
    }

    private void updateButtonsColor() {
        ReservaViewModel.FilterType filter = mReservaViewModel.getFilterType();
        String filterLabel = getString(R.string.filter);
        switch (filter) {
            case PREVISTAS: filterLabel = "VER:\nPREVISTAS"; break;
            case VIGENTES:  filterLabel = "VER:\nVIGENTES";  break;
            case CADUCADAS: filterLabel = "VER:\nCADUCADAS"; break;
            case TODAS:     filterLabel = getString(R.string.filter);        break;
        }
        btnOpenFilter.setText(filterLabel);

        ReservaViewModel.SortType sort = mReservaViewModel.getSortType();
        ReservaViewModel.SortDirection dir = mReservaViewModel.getSortDirection();
        String dirSymbol = (dir == ReservaViewModel.SortDirection.ASC) ? " ↑" : " ↓";
        
        String sortLabel = getString(R.string.sort);
        switch (sort) {
            case CLIENTE:  sortLabel = "ORDEN:\nNOMBRE" + dirSymbol; break;
            case TELEFONO: sortLabel = "ORDEN:\nTEL." + dirSymbol; break;
            case FECHA_IN: sortLabel = "ORDEN:\nINICIO" + dirSymbol; break;
            case FECHA_OUT:sortLabel = "ORDEN:\nFINAL" + dirSymbol; break;
        }
        btnOpenSort.setText(sortLabel);
    }

    private String construirMensajeCompleto(ReservaConQuads item, List<ReservaQuad> relaciones) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat uiSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        String displayIn = item.reserva.getFechaRecogida();
        String displayOut = item.reserva.getFechaDevolucion();
        long dias = 1;
        
        try {
            Date in = dbSdf.parse(item.reserva.getFechaRecogida());
            Date out = dbSdf.parse(item.reserva.getFechaDevolucion());
            if (in != null && out != null) {
                long diff = out.getTime() - in.getTime();
                dias = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                displayIn = uiSdf.format(in);
                displayOut = uiSdf.format(out);
            }
            if (dias < 1) dias = 1; 
        } catch (Exception e) { 
            Log.e(TAG, "Error al parsear fechas del mensaje", e);
        }

        double precioTotalAcordado = 0;
        int totalCascos = 0;
        if (relaciones != null) {
            for (ReservaQuad rq : relaciones) {
                precioTotalAcordado += rq.getPrecioDiarioAcordado();
                totalCascos += rq.getNumCascos();
            }
        }
        double precioFinalCalculado = precioTotalAcordado * dias; 

        sb.append(getString(R.string.msg_confirm_header)).append("\n\n");
        sb.append(getString(R.string.msg_client)).append(item.reserva.getNombreCliente()).append("\n");
        sb.append(getString(R.string.msg_contact)).append(item.reserva.getTelefono()).append("\n\n");
        sb.append(getString(R.string.msg_in)).append(displayIn).append("\n");
        sb.append(getString(R.string.msg_out)).append(displayOut).append("\n");
        sb.append(getString(R.string.msg_duration)).append(dias).append(" ").append(getString(R.string.days)).append("\n\n");
        sb.append(getString(R.string.msg_quads)).append(item.quads.size()).append("\n");
        sb.append(getString(R.string.msg_cascos)).append(totalCascos).append("\n\n");
        sb.append(getString(R.string.msg_price)).append(String.format(Locale.getDefault(), "%.2f", precioFinalCalculado)).append("€");

        return sb.toString();
    }

    private void mostrarDialogoEnvio(ReservaConQuads reservaItem, String mensajeFinal) {
        String[] options = {getString(R.string.whatsapp), getString(R.string.sms)};

        new AlertDialog.Builder(this)
            .setTitle(R.string.send_confirm_title)
            .setItems(options, (dialog, which) -> {
                String method = (which == 0) ? "WHATSAPP" : "SMS";
                SendAbstraction sender = new SendAbstractionImpl(ReservaListActivity.this, method);
                sender.send(String.valueOf(reservaItem.reserva.getTelefono()), mensajeFinal);
            })
            .show();
    }

    private void mostrarDialogoDetalles(ReservaConQuads item) {
        mReservaViewModel.getReservaQuads(item.reserva.getId(), relaciones -> runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_reserva_detalles, findViewById(android.R.id.content), false);
            builder.setView(view);

            TextView tvCliente = view.findViewById(R.id.dialog_cliente);
            TextView tvTelefono = view.findViewById(R.id.dialog_telefono);
            TextView tvFechas = view.findViewById(R.id.dialog_fechas);
            TextView tvQuads = view.findViewById(R.id.dialog_lista_quads);

            SimpleDateFormat uiSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String displayIn = item.reserva.getFechaRecogida();
            String displayOut = item.reserva.getFechaDevolucion();
            try {
                Date dIn = dbSdf.parse(displayIn);
                Date dOut = dbSdf.parse(displayOut);
                if (dIn != null) displayIn = uiSdf.format(dIn);
                if (dOut != null) displayOut = uiSdf.format(dOut);
            } catch (Exception e) {
                Log.e(TAG, "Error parseando fechas en detalles", e);
            }

            tvCliente.setText(item.reserva.getNombreCliente());
            tvTelefono.setText(String.valueOf(item.reserva.getTelefono()));
            tvFechas.setText(getString(R.string.date_range, displayIn, displayOut));

            if (item.quads != null && !item.quads.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                double totalPrecioDia = 0;
                for (Quad q : item.quads) {
                    double precioAcordado = getPrecioAcordado(q, relaciones);
                    sb.append("• [").append(q.getMatricula()).append("] ");
                    sb.append(q.getEsmonoplaza() ? getString(R.string.esmonoplaza) : getString(R.string.biplaza));
                    sb.append(" - ").append(precioAcordado).append("€/día\n");
                    totalPrecioDia += precioAcordado;
                }
                sb.append("\n").append(getString(R.string.total_day)).append(totalPrecioDia).append("€");
                tvQuads.setText(sb.toString());
            } else {
                tvQuads.setText(R.string.no_quads_assigned);
            }
            builder.setPositiveButton(R.string.close, null);
            builder.show();
        }));
    }

    private double getPrecioAcordado(Quad q, List<ReservaQuad> relaciones) {
        for (ReservaQuad rq : relaciones) {
            if (rq.getQuadId() == q.getId()) {
                return rq.getPrecioDiarioAcordado();
            }
        }
        return q.getPrecio();
    }
}