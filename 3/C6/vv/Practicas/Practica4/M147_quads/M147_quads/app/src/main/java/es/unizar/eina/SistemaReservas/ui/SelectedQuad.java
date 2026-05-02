package es.unizar.eina.SistemaReservas.ui;

import java.io.Serializable;

/**
 * Clase POJO (Plain Old Java Object) que actúa como envoltorio (wrapper) de un Quad 
 * para la interfaz de usuario.
 * 
 * A diferencia de la entidad de base de datos, esta clase incluye atributos de estado 
 * temporal como si el vehículo ha sido seleccionado y el número de cascos asignado, 
 * permitiendo gestionar la lógica de selección en los formularios de reserva.
 * 
 * Implementa {@link Serializable} para facilitar el envío de listas de selección 
 * entre diferentes actividades mediante objetos Intent.
 */
public class SelectedQuad implements Serializable {
    
    /** Identificador único del vehículo en la base de datos. */
    private int id;
    
    /** Matrícula del vehículo. */
    private String matricula;
    
    /** Indica si el vehículo es de tipo monoplaza. */
    private boolean esMonoplaza;
    
    /** Precio base por hora del vehículo. */
    private double precio;
    
    /** Descripción técnica o detalles del vehículo. */
    private final String descripcion;
    
    /** Estado de selección en la interfaz de usuario. Por defecto es false. */
    private boolean isSelected = false;
    
    /** Número de cascos asignados a este vehículo para una reserva concreta. Por defecto es 1. */
    private int numCascos = 1; 

    /**
     * Constructor para inicializar los datos base de un quad en la interfaz de selección.
     * 
     * @param id Identificador único.
     * @param matricula Matrícula identificativa.
     * @param esMonoplaza true para monoplaza, false para biplaza.
     * @param precio Tarifa horaria.
     * @param descripcion Detalles adicionales.
     */
    public SelectedQuad(int id, String matricula, boolean esMonoplaza, double precio, String descripcion) {
        this.id = id;
        this.matricula = matricula;
        this.esMonoplaza = esMonoplaza;
        this.precio = precio;
        this.descripcion = descripcion;
    }

    /** @return El identificador del quad. */
    public int getId() { return id; }

    /** @return La matrícula del quad. */
    public String getMatricula() { return matricula; }

    /** @return true si es monoplaza, false en caso contrario. */
    public boolean isMonoplaza() { return esMonoplaza; }

    /** @return El precio por hora del vehículo. */
    public double getPrecio() { return precio; }

    /** @return La descripción almacenada del vehículo. */
    public String getDescripcion() { return descripcion; }

    /** @return true si el usuario ha marcado este vehículo para la reserva. */
    public boolean isSelected() { return isSelected; }
    
    /** @param selected Nuevo estado de selección del vehículo. */
    public void setSelected(boolean selected) { isSelected = selected; }

    /** @return Cantidad de cascos seleccionados para este vehículo. */
    public int getNumCascos() { return numCascos; }
    
    /** @param numCascos Nueva cantidad de cascos asignada. */
    public void setNumCascos(int numCascos) { this.numCascos = numCascos; }
}