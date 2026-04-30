package es.unizar.eina.SistemaReservas.database;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;
import java.util.List;

/**
 * Clase POJO (Plain Old Java Object) utilizada para representar una relación 
 * Muchos a Muchos entre las entidades {@link Reserva} y {@link Quad}.
 * 
 * Permite a Room realizar consultas complejas y devolver un objeto que 
 * agrupa los datos de una reserva junto con la lista completa de vehículos asignados.
 */
public class ReservaConQuads {
    
    /** 
     * Objeto base de la reserva. 
     * La anotación @Embedded incrusta todos los campos de la tabla Reserva en este objeto.
     */
    @Embedded
    public Reserva reserva;

    /** 
     * Lista de vehículos Quad asociados a la reserva.
     * Room resuelve automáticamente esta lista consultando la tabla intermedia 
     * {@link ReservaQuad} mediante el uso de parentColumn y entityColumn.
     */
    @Relation(
         parentColumn = "id",      // ID en la tabla Reserva
         entityColumn = "id",      // ID en la tabla Quad
         associateBy = @Junction(  // Tabla intermedia que las une
                 value = ReservaQuad.class,
                 parentColumn = "reserva_id",
                 entityColumn = "quad_id"
         )
    )
    public List<Quad> quads;
}