package es.unizar.eina.SistemaReservas.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Representa la entidad Quad en la base de datos.
 * Esta clase define la estructura de la tabla "Quad" y sus restricciones, 
 * como la unicidad de la matrícula.
 */
@Entity(tableName = "Quad", indices = {@Index(value = {"Matricula"}, unique = true)})
public class Quad {

    /** Identificador único del Quad, autogenerado por la base de datos. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    private int id;

    /** Matrícula del vehículo. Actúa como identificador natural y es única. */
    @NonNull
    @ColumnInfo(name = "Matricula")
    private String matricula;

    /** Indica si el quad es monoplaza (true) o biplaza (false). */
    @ColumnInfo(name = "esMonoplaza")
    private boolean esmonoplaza;

    /** Precio de alquiler por hora del vehículo. */
    @NonNull
    @ColumnInfo(name = "precio")
    private double precio;

    /** Breve descripción del vehículo y sus características. */
    @ColumnInfo(name = "descripcion")
    private String descripcion;

    /** Indica si el quad está activo (borrado lógico). */
    @ColumnInfo(name = "estaActivo")
    private boolean estaActivo = true;

    /**
     * Constructor para crear una nueva instancia de Quad.
     * 
     * @param matricula Matrícula única del vehículo.
     * @param esmonoplaza Tipo de plazas del vehículo.
     * @param precio Tarifa por hora.
     * @param descripcion Detalles adicionales del vehículo.
     */
    public Quad(@NonNull String matricula, boolean esmonoplaza, double precio, String descripcion) {
        this.matricula = matricula;
        this.esmonoplaza = esmonoplaza;
        this.precio = precio;
        this.descripcion = descripcion;
        this.estaActivo = true;
    }

    /** @return El identificador de la base de datos. */
    public int getId() { return id; }
    
    /** @param id Nuevo identificador para el Quad. */
    public void setId(int id) { this.id = id; }

    /** @return La matrícula del Quad. */
    @NonNull
    public String getMatricula() { return matricula; }
    
    /** @param matricula Nueva matrícula del vehículo. */
    public void setMatricula(@NonNull String matricula) { this.matricula = matricula; }

    /** @return true si es monoplaza, false si es biplaza. */
    public boolean getEsmonoplaza() { return esmonoplaza; }
    
    /** @param esmonoplaza Definición de plazas del vehículo. */
    public void setEsmonoplaza(boolean esmonoplaza) { this.esmonoplaza = esmonoplaza; }

    /** @return El precio por hora actual. */
    public double getPrecio() { return precio; }
    
    /** @param precio Nuevo precio por hora. */
    public void setPrecio(double precio) { this.precio = precio; }

    /** @return La descripción del Quad. */
    public String getDescripcion() { return descripcion; }
    
    /** @param descripcion Nueva descripción para el vehículo. */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** @return true si el quad está activo. */
    public boolean getEstaActivo() { return estaActivo; }

    /** @param estaActivo Nuevo estado de activación. */
    public void setEstaActivo(boolean estaActivo) { this.estaActivo = estaActivo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quad quad = (Quad) o;
        return esmonoplaza == quad.esmonoplaza &&
               Double.compare(quad.precio, precio) == 0 &&
               estaActivo == quad.estaActivo &&
               matricula.equals(quad.matricula) &&
               (descripcion == null ? quad.descripcion == null : descripcion.equals(quad.descripcion));
    }
}