package es.unizar.eina.SistemaReservas.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad que representa una Reserva en la base de datos.
 * Almacena la información básica del cliente y el periodo temporal del alquiler.
 * Esta clase es el nodo principal de la relación Muchos a Muchos con los Quads.
 */
@Entity(tableName = "Reserva")
public class Reserva {

    /** Identificador único de la reserva, autogenerado por Room. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    /** Nombre completo del cliente que realiza la reserva. */
    @NonNull
    @ColumnInfo(name = "nombre_cliente")
    private String nombreCliente;

    /** Número de teléfono de contacto del cliente. */
    @NonNull
    @ColumnInfo(name = "telefono")
    private String telefono;

    /** Fecha programada para la recogida de los vehículos (formato String). */
    @NonNull
    @ColumnInfo(name = "fecha_recogida")
    private String fechaRecogida;

    /** Fecha programada para la devolución de los vehículos (formato String). */
    @NonNull
    @ColumnInfo(name = "fecha_devolucion")
    private String fechaDevolucion;

    /** Precio pactado y calculado al momento de la creación de la reserva. */
    @ColumnInfo(name = "precio_total")
    private double precioTotal = 0.0;

    /** Indica si la reserva está activa (true) o dada de baja lógica (false). */
    @ColumnInfo(name = "activo")
    private boolean activo = true;

    /**
     * Constructor para crear una nueva instancia de Reserva.
     * 
     * @param nombreCliente Nombre del titular de la reserva.
     * @param telefono Teléfono de contacto.
     * @param fechaRecogida Fecha de inicio del periodo de alquiler.
     * @param fechaDevolucion Fecha de fin del periodo de alquiler.
     */
    public Reserva(@NonNull String nombreCliente, @NonNull String telefono, 
                   @NonNull String fechaRecogida, @NonNull String fechaDevolucion) {
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.fechaRecogida = fechaRecogida;
        this.fechaDevolucion = fechaDevolucion;
    }

    /** @return El identificador único de la reserva. */
    public int getId() { return id; }

    /** @param id Nuevo identificador para la reserva (uso interno de Room). */
    public void setId(int id) { this.id = id; }

    /** @return El nombre del cliente. */
    public String getNombreCliente() { return nombreCliente; }

    /** @return El teléfono del cliente. */
    public String getTelefono() { return telefono; }

    /** @return La fecha de recogida en formato cadena. */
    public String getFechaRecogida() { return fechaRecogida; }

    /** @return La fecha de devolución en formato cadena. */
    public String getFechaDevolucion() { return fechaDevolucion; }
    
    /** @return El precio total de la reserva fijado en su creación. */
    public double getPrecioTotal() { return precioTotal; }

    /** @param precioTotal Nuevo precio calculado para la reserva. */
    public void setPrecioTotal(double precioTotal) { this.precioTotal = precioTotal; }

    /** @return true si la reserva está activa, false si está cancelada. */
    public boolean isActivo() { return activo; }

    /** @param activo Estado lógico de la reserva. */
    public void setActivo(boolean activo) { this.activo = activo; }
}