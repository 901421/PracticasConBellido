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
    @ColumnInfo(name = "Id")
    private int id;

    /** Nombre completo del cliente que realiza la reserva. */
    @NonNull
    @ColumnInfo(name = "nombre_cliente")
    private String nombreCliente;

    /** Número de teléfono de contacto del cliente. */
    @ColumnInfo(name = "num_telefono")
    private int telefono;

    /** Fecha programada para la recogida de los vehículos (formato String). */
    @NonNull
    @ColumnInfo(name = "fecha_recogida")
    private String fechaRecogida;

    /** Fecha programada para la devolución de los vehículos (formato String). */
    @NonNull
    @ColumnInfo(name = "fecha_devolucion")
    private String fechaDevolucion;

    /** Indica si la reserva está activa (borrado lógico). */
    @ColumnInfo(name = "estaActivo")
    private boolean estaActivo = true;

    /**
     * Constructor para crear una nueva instancia de Reserva.
     * 
     * @param nombreCliente Nombre del titular de la reserva.
     * @param telefono Teléfono de contacto.
     * @param fechaRecogida Fecha de inicio del periodo de alquiler.
     * @param fechaDevolucion Fecha de fin del periodo de alquiler.
     */
    public Reserva(@NonNull String nombreCliente, int telefono, 
                   @NonNull String fechaRecogida, @NonNull String fechaDevolucion) {
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.fechaRecogida = fechaRecogida;
        this.fechaDevolucion = fechaDevolucion;
        this.estaActivo = true;
    }

    /** @return El identificador único de la reserva. */
    public int getId() { return id; }

    /** @param id Nuevo identificador para la reserva (uso interno de Room). */
    public void setId(int id) { this.id = id; }

    /** @return El nombre del cliente. */
    public String getNombreCliente() { return nombreCliente; }

    /** @return El teléfono del cliente. */
    public int getTelefono() { return telefono; }

    /** @param telefono Nuevo teléfono del cliente. */
    public void setTelefono(int telefono) { this.telefono = telefono; }

    /** @return La fecha de recogida en formato cadena. */
    public String getFechaRecogida() { return fechaRecogida; }

    /** @return La fecha de devolución en formato cadena. */
    public String getFechaDevolucion() { return fechaDevolucion; }

    /** @return true si la reserva está activa. */
    public boolean getEstaActivo() { return estaActivo; }

    /** @param estaActivo Nuevo estado de activación de la reserva. */
    public void setEstaActivo(boolean estaActivo) { this.estaActivo = estaActivo; }
}