package es.unizar.eina.SistemaReservas.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/**
 * Entidad que representa la tabla intermedia (Junction) para la relación 
 * Muchos a Muchos entre {@link Reserva} y {@link Quad}.
 * 
 * Además de vincular ambas entidades mediante sus claves foráneas, almacena 
 * información específica de la relación, como el número de cascos asignados 
 * a un vehículo concreto dentro de una reserva.
 */
@Entity(tableName = "Quad_Reserva",
        primaryKeys = {"ReservaId", "QuadId"},
        foreignKeys = {
                @ForeignKey(entity = Reserva.class,
                        parentColumns = "Id",
                        childColumns = "ReservaId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Quad.class,
                        parentColumns = "Id",
                        childColumns = "QuadId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("ReservaId"), @Index("QuadId")})
public class ReservaQuad {

    /** Identificador de la reserva vinculada. Parte de la clave primaria compuesta. */
    @ColumnInfo(name = "ReservaId")
    private int reservaId;

    /** Identificador del vehículo Quad vinculado. Parte de la clave primaria compuesta. */
    @ColumnInfo(name = "QuadId")
    private int quadId;

    /** Cantidad de cascos contratados para este vehículo en esta reserva específica. */
    @ColumnInfo(name = "num_cascos")
    private int numCascos;

    /** Precio diario acordado en el momento de crear la reserva. */
    @ColumnInfo(name = "precio_diario_acordado")
    private double precioDiarioAcordado;

    /**
     * Constructor para crear un nuevo vínculo entre una reserva y un quad.
     * 
     * @param reservaId ID de la reserva.
     * @param quadId ID del vehículo.
     * @param numCascos Número de cascos asignados.
     * @param precioDiarioAcordado Precio del vehículo en el momento de la reserva.
     */
    public ReservaQuad(int reservaId, int quadId, int numCascos, double precioDiarioAcordado) {
        this.reservaId = reservaId;
        this.quadId = quadId;
        this.numCascos = numCascos;
        this.precioDiarioAcordado = precioDiarioAcordado;
    }

    /** @return El ID de la reserva asociada. */
    public int getReservaId() { return reservaId; }
    
    /** @param reservaId Nuevo ID de reserva para el vínculo. */
    public void setReservaId(int reservaId) { this.reservaId = reservaId; }

    /** @return El ID del quad asociado. */
    public int getQuadId() { return quadId; }
    
    /** @param quadId Nuevo ID de quad para el vínculo. */
    public void setQuadId(int quadId) { this.quadId = quadId; }

    /** @return El número de cascos registrados para este vehículo. */
    public int getNumCascos() { return numCascos; }
    
    /** @param numCascos Nueva cantidad de cascos asignada. */
    public void setNumCascos(int numCascos) { this.numCascos = numCascos; }

    /** @return El precio diario acordado para este vehículo en esta reserva. */
    public double getPrecioDiarioAcordado() { return precioDiarioAcordado; }

    /** @param precioDiarioAcordado Nuevo precio diario acordado. */
    public void setPrecioDiarioAcordado(double precioDiarioAcordado) { this.precioDiarioAcordado = precioDiarioAcordado; }
}