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

    /** ViewModel que gestiona el acceso a los datos de las reservas. */
    private ReservaViewModel mReservaViewModel;
    
    /** Botón para activar el orden por nombre de cliente. */
    private Button btnCliente;
    /** Botón para activar el orden por teléfono. */
    private Button btnTelefono;
    /** Botón para activar el orden por fecha de recogida. */
    private Button btnFechaIn;
    /** Botón para activar el orden por fecha de devolución. */
    private Button btnFechaOut;
    /** Componente para la visualización de la lista. */
    private RecyclerView recyclerView;
    /** Vista mostrada cuando no existen reservas registradas en el sistema. */
    private TextView emptyView;
    
    /**
     * Configura la interfaz de usuario, inicializa los ViewModels y establece 
     * los escuchadores para la gestión de clics en la lista y botones de ordenación.
     * 
     * @param savedInstanceState Estado de la instancia guardada.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_reservas);

        recyclerView = findViewById(R.id.recyclerview_reservas);
        emptyView = findViewById(R.id.empty_view_reservas);

        btnCliente = findViewById(R.id.sort_cliente);
        btnTelefono = findViewById(R.id.sort_telefono);
        btnFechaIn = findViewById(R.id.sort_fecha_in);
        btnFechaOut = findViewById(R.id.sort_fecha_out);
        
        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroupReservas);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.sort_cliente) {
                    mReservaViewModel.setSortType(ReservaViewModel.SortType.CLIENTE);
                } else if (checkedId == R.id.sort_telefono) {
                    mReservaViewModel.setSortType(ReservaViewModel.SortType.TELEFONO);
                } else if (checkedId == R.id.sort_fecha_in) {
                    mReservaViewModel.setSortType(ReservaViewModel.SortType.FECHA_IN);
                } else if (checkedId == R.id.sort_fecha_out) {
                    mReservaViewModel.setSortType(ReservaViewModel.SortType.FECHA_OUT);
                }
            }
        });

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
                                    dbQuads.add(new ReservaQuad(id, sq.getId(), sq.getNumCascos(), sq.getPrecio()));
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
                            SelectedQuad sq = new SelectedQuad(
                                q.getId(), 
                                q.getMatricula(), 
                                q.getEsmonoplaza(), 
                                q.getPrecio(), 
                                q.getDescripcion()
                            ); 

                            sq.setSelected(true);
                            int cascosReales = 1; 
                            for (ReservaQuad rq : listaRelaciones) {
                                if (rq.getQuadId() == q.getId()) {
                                    cascosReales = rq.getNumCascos();
                                    break;
                                }
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
            
            if (reservas == null || reservas.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });
        
        btnCliente.setOnClickListener(v -> {
            mReservaViewModel.setSortType(ReservaViewModel.SortType.CLIENTE);
            updateButtonsColor(btnCliente);
        });
        
        btnTelefono.setOnClickListener(v -> {
            mReservaViewModel.setSortType(ReservaViewModel.SortType.TELEFONO);
            updateButtonsColor(btnTelefono);
        });
        
        btnFechaIn.setOnClickListener(v -> {
            mReservaViewModel.setSortType(ReservaViewModel.SortType.FECHA_IN);
            updateButtonsColor(btnFechaIn);
        });
        
        btnFechaOut.setOnClickListener(v -> {
            mReservaViewModel.setSortType(ReservaViewModel.SortType.FECHA_OUT);
            updateButtonsColor(btnFechaOut);
        });

        updateButtonsColor(btnFechaIn);
    }

    /**
     * Calcula la duración del alquiler, el coste total estimado y el total de cascos 
     * para generar un mensaje estructurado destinado a la confirmación del cliente.
     * 
     * @param item Objeto que contiene la reserva y sus vehículos asociados.
     * @param relaciones Lista de relaciones técnicas que incluye el número de cascos.
     * @return Cadena de texto formateada para el envío por WhatsApp o SMS.
     */
    private String construirMensajeCompleto(ReservaConQuads item, java.util.List<ReservaQuad> relaciones) {
        StringBuilder sb = new StringBuilder();
        
        long dias = 1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date in = sdf.parse(item.reserva.getFechaRecogida());
            Date out = sdf.parse(item.reserva.getFechaDevolucion());
            long diff = out.getTime() - in.getTime();
            dias = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            if (dias < 1) dias = 1; 
        } catch (Exception e) {
            e.printStackTrace();
        }

        double precioBaseQuads = 0;
        for (Quad q : item.quads) {
            precioBaseQuads += q.getPrecio();
        }
        double precioTotal = precioBaseQuads * dias; 

        int totalCascos = 0;
        if (relaciones != null) {
            for (ReservaQuad rq : relaciones) {
                totalCascos += rq.getNumCascos();
            }
        }

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

    /**
     * Muestra un diálogo de selección para elegir el método de envío y 
     * ejecuta el patrón Bridge para transmitir la información.
     * 
     * @param reservaItem Elemento de la reserva a confirmar.
     * @param mensajeFinal Cuerpo del mensaje ya formateado.
     */
    private void mostrarDialogoEnvio(ReservaConQuads reservaItem, String mensajeFinal) {
        String[] options = {"WhatsApp", "SMS"};

        new AlertDialog.Builder(this)
            .setTitle("Enviar confirmación")
            .setItems(options, (dialog, which) -> {
                String method = (which == 0) ? "WHATSAPP" : "SMS";
                SendAbstraction sender = new SendAbstractionImpl(ReservaListActivity.this, method);
                sender.send(reservaItem.reserva.getTelefono(), mensajeFinal);
            })
            .show();
    }

    /**
     * Variante simplificada para el envío de confirmación con mensaje predeterminado.
     * 
     * @param reservaItem Elemento de la reserva a confirmar.
     */
    private void mostrarDialogoEnvio(ReservaConQuads reservaItem) {
        String[] options = {"WhatsApp", "SMS"};

        new AlertDialog.Builder(this)
            .setTitle("Enviar confirmación de reserva")
            .setItems(options, (dialog, which) -> {
                String method = (which == 0) ? "WHATSAPP" : "SMS";
                String mensaje = "Hola " + reservaItem.reserva.getNombreCliente() + 
                                 ", tu reserva de Quads está confirmada para el " + 
                                 reservaItem.reserva.getFechaRecogida() + ".";
                SendAbstraction sender = new SendAbstractionImpl(ReservaListActivity.this, method);
                sender.send(reservaItem.reserva.getTelefono(), mensaje);
            })
            .show();
    }

    /**
     * Actualiza la apariencia visual del grupo de botones de ordenación superiores.
     * @param activeButton El botón que debe aparecer resaltado.
     */
    private void updateButtonsColor(Button activeButton) {
        int colorActive = ContextCompat.getColor(this, R.color.colorBtnEdit); 
        int colorInactive = 0xFFCCCCCC; 
        
        Button[] allButtons = {btnCliente, btnTelefono, btnFechaIn, btnFechaOut};
        
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
     * Crea y muestra un diálogo de alerta con el desglose detallado de la reserva,
     * incluyendo datos del cliente, fechas y la lista de vehículos con sus precios 
     * persistidos en el momento de la reserva.
     * 
     * @param item El objeto de relación que contiene la reserva a detallar.
     */
    private void mostrarDialogoDetalles(ReservaConQuads item) {
        mReservaViewModel.getReservaQuads(item.reserva.getId(), relaciones -> {
            runOnUiThread(() -> {
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
                    double totalPrecioDia = 0;
                    for (Quad q : item.quads) {
                        double precioAcordado = q.getPrecio(); // Fallback
                        for (ReservaQuad rq : relaciones) {
                            if (rq.getQuadId() == q.getId()) {
                                precioAcordado = rq.getPrecioDiarioAcordado();
                                break;
                            }
                        }
                        sb.append("• [").append(q.getMatricula()).append("] ");
                        sb.append(q.getEsmonoplaza() ? "Monoplaza" : "Biplaza");
                        sb.append(" - ").append(precioAcordado).append("€/día\n");
                        totalPrecioDia += precioAcordado;
                    }
                    sb.append("\nTotal/día: ").append(totalPrecioDia).append("€");
                    tvQuads.setText(sb.toString());
                } else {
                    tvQuads.setText("No hay quads asignados.");
                }
                builder.setPositiveButton("Cerrar", null);
                builder.show();
            });
        });
    }
}