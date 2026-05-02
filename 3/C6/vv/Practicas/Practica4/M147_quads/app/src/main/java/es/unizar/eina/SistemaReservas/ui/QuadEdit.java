package es.unizar.eina.SistemaReservas.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import es.unizar.eina.SistemaReservas.R;

/**
 * Actividad que permite la creación y edición de vehículos Quad.
 * Proporciona un formulario para introducir la matrícula, descripción, precio
 * y seleccionar si el vehículo es monoplaza o biplaza.
 */
public class QuadEdit extends AppCompatActivity {

    /** Clave para el extra del Intent: Identificador del Quad. */
    public static final String QUAD_ID = "id";
    /** Clave para el extra del Intent: Matrícula del Quad. */
    public static final String QUAD_MATRICULA = "matricula";
    /** Clave para el extra del Intent: Descripción del Quad. */
    public static final String QUAD_DESCRIPCION = "descripcion";
    /** Clave para el extra del Intent: Precio por hora del Quad. */
    public static final String QUAD_PRECIO = "precio";
    /** Clave para el extra del Intent: Booleano que indica si es monoplaza. */
    public static final String QUAD_ESMONOPLAZA = "esmonoplaza";

    /** Campo de texto para la matrícula. */
    private EditText mMatriculaText;
    /** Campo de texto para la descripción. */
    private EditText mDescripcionText;
    /** Campo de texto para el precio. */
    private EditText mPrecioText;
    /** Botón para seleccionar tipo monoplaza. */
    private Button mBtnMonoplaza;
    /** Botón para seleccionar tipo biplaza. */
    private Button mBtnBiplaza;

    /** Estado interno: true si es monoplaza, false si es biplaza. Por defecto true. */
    private boolean mIsMonoplaza = true;
    /** ID del registro actual en caso de edición; null si se está creando. */
    private Integer mRowId;

    /**
     * Inicializa la actividad, vincula las vistas y configura los escuchadores de eventos.
     * @param savedInstanceState Estado guardado de la instancia.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quadedit);

        mMatriculaText = findViewById(R.id.matricula);
        mDescripcionText = findViewById(R.id.descripcion);
        mPrecioText = findViewById(R.id.precio);
        mBtnMonoplaza = findViewById(R.id.btnMonoplaza);
        mBtnBiplaza = findViewById(R.id.btnBiplaza);
        
        mBtnMonoplaza.setOnClickListener(v -> {
            mIsMonoplaza = true;
            updateButtonsColor();
        });

        mBtnBiplaza.setOnClickListener(v -> {
            mIsMonoplaza = false;
            updateButtonsColor();
        });

        findViewById(R.id.button_cancel).setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        findViewById(R.id.button_save).setOnClickListener(view -> {
            Intent replyIntent = new Intent();
            String matriculaInput = mMatriculaText.getText().toString().trim().toUpperCase();
            String precioInput = mPrecioText.getText().toString().trim();
            
            if (TextUtils.isEmpty(matriculaInput)) {
                mMatriculaText.setError("La matrícula es obligatoria");
                return;
            } 
            if (!matriculaInput.matches("^[0-9]{4}-[A-Z]{3}$")) {
                mMatriculaText.setError("Formato inválido (Ej: 1234-ABC)");
                return;
            }
            if (TextUtils.isEmpty(precioInput)) {
                mPrecioText.setError("El precio es obligatorio");
                return;
            }

            replyIntent.putExtra(QUAD_MATRICULA, matriculaInput); 
            replyIntent.putExtra(QUAD_DESCRIPCION, mDescripcionText.getText().toString());
            replyIntent.putExtra(QUAD_ESMONOPLAZA, mIsMonoplaza);
            replyIntent.putExtra(QUAD_PRECIO, Double.parseDouble(precioInput));

            if (mRowId != null) {
                replyIntent.putExtra(QUAD_ID, mRowId);
            }
            setResult(RESULT_OK, replyIntent);
            finish();
        });

        populateFields();
    }

    /**
     * Actualiza la apariencia visual de los botones de selección de plazas (Monoplaza/Biplaza)
     * basándose en el estado de mIsMonoplaza para resaltar la opción seleccionada.
     */
    private void updateButtonsColor() {
        int colorActive = ContextCompat.getColor(this, R.color.colorBtnEdit);
        int colorInactive = 0xFFCCCCCC; 

        mBtnMonoplaza.setBackgroundTintList(ColorStateList.valueOf(mIsMonoplaza ? colorActive : colorInactive));
        mBtnMonoplaza.setTextColor(mIsMonoplaza ? Color.WHITE : Color.BLACK);

        mBtnBiplaza.setBackgroundTintList(ColorStateList.valueOf(!mIsMonoplaza ? colorActive : colorInactive));
        mBtnBiplaza.setTextColor(!mIsMonoplaza ? Color.WHITE : Color.BLACK);
    }

    /**
     * Rellena los campos del formulario si la actividad se inició para editar un Quad existente.
     * Recupera los datos de los extras del Intent y actualiza el título de la actividad.
     */
    private void populateFields() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setTitle("Editar Quad");
            mRowId = extras.getInt(QUAD_ID);
            mMatriculaText.setText(extras.getString(QUAD_MATRICULA));
            mDescripcionText.setText(extras.getString(QUAD_DESCRIPCION));
            mPrecioText.setText(String.valueOf(extras.getDouble(QUAD_PRECIO)));
            mIsMonoplaza = extras.getBoolean(QUAD_ESMONOPLAZA);
        } else {
            setTitle("Crear Quad");
            mIsMonoplaza = true;
        }
        updateButtonsColor();
    }
}