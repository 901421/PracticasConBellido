package es.unizar.eina.SistemaReservas.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import es.unizar.eina.SistemaReservas.R;

/**
 * Actividad encargada de la creación y edición de reservas.
 * Gestiona la entrada de datos del cliente, la selección de fechas mediante calendarios 
 * emergentes y la navegación hacia la selección de vehículos disponibles.
 */
public class ReservaEdit extends AppCompatActivity {

    /** Clave para el identificador de la reserva. */
    public static final String RES_ID = "res_id"; 
    /** Clave para el nombre del cliente. */
    public static final String RES_CLIENTE = "cliente";
    /** Clave para el teléfono de contacto. */
    public static final String RES_TELEFONO = "telefono";
    /** Clave para la fecha de inicio. */
    public static final String RES_FECHA_IN = "fecha_in";
    /** Clave para la fecha de fin. */
    public static final String RES_FECHA_OUT = "fecha_out";
    /** Clave para la lista de quads seleccionados. */
    public static final String RES_LISTA_QUADS = "lista_quads";

    /** Entrada de texto para el nombre del cliente. */
    private EditText mEditCliente;
    /** Entrada de texto para el teléfono. */
    private EditText mEditTelefono;
    /** Botón selector para la fecha de recogida. */
    private Button mBtnFechaRecogida;
    /** Botón selector para la fecha de devolución. */
    private Button mBtnFechaDevolucion;
    /** Botón de acceso a la pantalla de selección de vehículos. */
    private Button mBtnSelectQuads;

    /** Almacén temporal de la fecha de recogida en formato texto. */
    private String mFechaRecogidaStr = "";
    /** Almacén temporal de la fecha de devolución en formato texto. */
    private String mFechaDevolucionStr = "";
    /** ID de la reserva actual. Permite distinguir entre creación (null) y edición. */
    private Integer mReservaId = null;

    /** Lista de vehículos seleccionados para la reserva actual. */
    private ArrayList<SelectedQuad> mSelectedQuads = new ArrayList<>();
    /** Formateador de fechas estándar para la aplicación. */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    /**
     * Inicializa la actividad, configura los lanzadores de resultados y los 
     * escuchadores para las validaciones de fechas y selección de vehículos.
     * 
     * @param savedInstanceState Estado de la instancia guardada.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserva_edit);

        mEditCliente = findViewById(R.id.edit_cliente);
        mEditTelefono = findViewById(R.id.edit_telefono);
        mBtnFechaRecogida = findViewById(R.id.btn_fecha_recogida);
        mBtnFechaDevolucion = findViewById(R.id.btn_fecha_devolucion);
        mBtnSelectQuads = findViewById(R.id.btn_select_quads);
        Button btnCancel = findViewById(R.id.button_cancel);
        Button btnConfirm = findViewById(R.id.button_confirm);

        ActivityResultLauncher<Intent> mSelectQuadsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    mSelectedQuads = (ArrayList<SelectedQuad>) result.getData().getSerializableExtra(QuadSelectionActivity.EXTRA_SELECTED_QUADS);
                    updateBotonQuadsUI(); 
                }
            }
        );

        mBtnFechaRecogida.setOnClickListener(v -> showDatePicker(mBtnFechaRecogida, true));
        mBtnFechaDevolucion.setOnClickListener(v -> showDatePicker(mBtnFechaDevolucion, false));

        mBtnSelectQuads.setOnClickListener(v -> {
            if (mFechaRecogidaStr.isEmpty() || mFechaDevolucionStr.isEmpty()) {
                Toast.makeText(this, "Primero selecciona las fechas", Toast.LENGTH_LONG).show();
                
                if(mFechaRecogidaStr.isEmpty()) {
                    mBtnFechaRecogida.setError("Obligatorio");
                }
                if(mFechaDevolucionStr.isEmpty()) {
                    mBtnFechaDevolucion.setError("Obligatorio");
                }
                return;
            }

            if (!isFechaValida(mFechaRecogidaStr, mFechaDevolucionStr)) {
                Toast.makeText(this, "La fecha de devolución debe ser posterior", Toast.LENGTH_LONG).show();
                mBtnFechaDevolucion.setError("Fecha inválida"); 
                return;
            }

            Intent intent = new Intent(ReservaEdit.this, QuadSelectionActivity.class);
            intent.putExtra(QuadSelectionActivity.EXTRA_PRESELECTED_QUADS, mSelectedQuads);
            intent.putExtra("EXTRA_FECHA_IN", mFechaRecogidaStr);
            intent.putExtra("EXTRA_FECHA_OUT", mFechaDevolucionStr);
            intent.putExtra("EXTRA_RESERVA_ID", mReservaId == null ? -1 : mReservaId);
            mSelectQuadsLauncher.launch(intent);
        });

        btnCancel.setOnClickListener(v -> { setResult(RESULT_CANCELED); finish(); });

        btnConfirm.setOnClickListener(v -> saveReserva());

        populateFields();
    }

    /**
     * Actualiza el texto del botón de selección de vehículos para mostrar 
     * el conteo actual de quads y cascos seleccionados.
     */
    private void updateBotonQuadsUI() {
        if (mSelectedQuads != null && !mSelectedQuads.isEmpty()) {
            int totalCascos = 0;
            for(SelectedQuad sq : mSelectedQuads) totalCascos += sq.getNumCascos();
            String texto = mSelectedQuads.size() + " Quads (" + totalCascos + " cascos)";
            
            mBtnSelectQuads.setText(texto);
            mBtnSelectQuads.setError(null);
        } else {
            mBtnSelectQuads.setText("SELECCIONAR");
        }
    }

    /**
     * Carga la información de una reserva en el formulario si la actividad 
     * ha sido iniciada en modo edición.
     */
    private void populateFields() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(RES_ID)) {
            setTitle("Editar Reserva");
            mReservaId = extras.getInt(RES_ID);
            
            mEditCliente.setText(extras.getString(RES_CLIENTE));
            mEditTelefono.setText(extras.getString(RES_TELEFONO));
            
            mFechaRecogidaStr = extras.getString(RES_FECHA_IN);
            mBtnFechaRecogida.setText(mFechaRecogidaStr); 

            mFechaDevolucionStr = extras.getString(RES_FECHA_OUT);
            mBtnFechaDevolucion.setText(mFechaDevolucionStr); 

            mSelectedQuads = (ArrayList<SelectedQuad>) extras.getSerializable(RES_LISTA_QUADS);
            updateBotonQuadsUI();
        } else {
            setTitle("Crear Reserva");
        }
    }

    /**
     * Realiza la validación final de todos los campos obligatorios y la coherencia 
     * de fechas antes de devolver los datos a la actividad llamadora.
     */
    private void saveReserva() {
        Intent replyIntent = new Intent();
        boolean isValid = true;

        if (TextUtils.isEmpty(mEditCliente.getText())) {
            mEditCliente.setError("El nombre es obligatorio");
            isValid = false;
        }

        if (TextUtils.isEmpty(mEditTelefono.getText())) {
            mEditTelefono.setError("El teléfono es obligatorio");
            isValid = false;
        }

        if (mFechaRecogidaStr.isEmpty()) {
            mBtnFechaRecogida.setError("Selecciona una fecha");
            isValid = false;
        } else {
            mBtnFechaRecogida.setError(null); 
        }

        if (mFechaDevolucionStr.isEmpty()) {
            mBtnFechaDevolucion.setError("Selecciona una fecha");
            isValid = false;
        } else {
            mBtnFechaDevolucion.setError(null); 
        }

        if (mSelectedQuads == null || mSelectedQuads.isEmpty()) { 
            mBtnSelectQuads.setError("Debes seleccionar al menos un vehículo");
            isValid = false; 
        } else {
            mBtnSelectQuads.setError(null); 
        }

        if (!isValid) {
            Toast.makeText(this, "Revisa los campos marcados con error", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValid) {
            Toast.makeText(this, "Revisa los errores.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isFechaValida(mFechaRecogidaStr, mFechaDevolucionStr)) {
            Toast.makeText(this, "Fechas incoherentes.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mReservaId != null) {
            replyIntent.putExtra(RES_ID, mReservaId);
        }
        replyIntent.putExtra(RES_CLIENTE, mEditCliente.getText().toString());
        replyIntent.putExtra(RES_TELEFONO, mEditTelefono.getText().toString());
        replyIntent.putExtra(RES_FECHA_IN, mFechaRecogidaStr);
        replyIntent.putExtra(RES_FECHA_OUT, mFechaDevolucionStr);
        replyIntent.putExtra(RES_LISTA_QUADS, mSelectedQuads);

        setResult(RESULT_OK, replyIntent);
        finish();
    }

    /**
     * Comprueba si un rango de fechas es lógico (fecha de inicio anterior o igual a la de fin).
     * 
     * @param inicio Fecha de recogida en formato texto.
     * @param fin Fecha de devolución en formato texto.
     * @return true si el rango es correcto, false en caso contrario.
     */
    private boolean isFechaValida(String inicio, String fin) {
        try {
            Date dateInicio = dateFormat.parse(inicio);
            Date dateFin = dateFormat.parse(fin);
            if (dateInicio != null && dateFin != null) return !dateInicio.after(dateFin);
        } catch (ParseException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Despliega un diálogo de selección de fecha y actualiza los estados internos.
     * 
     * @param buttonToUpdate Referencia al botón que debe mostrar la fecha elegida.
     * @param isRecogida true si se está configurando la recogida, false para devolución.
     */
    private void showDatePicker(Button buttonToUpdate, boolean isRecogida) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.BrownDatePickerTheme,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    
                    buttonToUpdate.setText(date);
                    buttonToUpdate.setError(null);

                    if (isRecogida) mFechaRecogidaStr = date;
                    else mFechaDevolucionStr = date;
                }, year, month, day);
        datePickerDialog.show();
    }
}