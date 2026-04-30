package es.unizar.eina.SistemaReservas.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import java.util.List;

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad Reserva.
 * Proporciona los métodos necesarios para gestionar las reservas y sus relaciones 
 * con los vehículos Quads en la base de datos Room.
 */
@Dao
public interface ReservaDao {

    /**
     * Inserta una nueva Reserva en la base de datos.
     * 
     * @param reserva El objeto Reserva a insertar.
     * @return El identificador de la fila recién insertada.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Reserva reserva);

    /**
     * Inserta una lista de relaciones entre reservas y quads en la tabla intermedia.
     * Permite vincular múltiples vehículos a una misma reserva.
     * 
     * @param reservaQuads Lista de objetos {@link ReservaQuad} que definen la relación.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertReservaQuads(List<ReservaQuad> reservaQuads);

    /**
     * Actualiza la información de una reserva existente.
     * 
     * @param reserva El objeto Reserva con los datos actualizados.
     * @return El número de filas actualizadas.
     */
    @Update
    int update(Reserva reserva);

    /**
     * Elimina lógicamente una reserva de la base de datos.
     * 
     * @param id El identificador de la reserva a eliminar.
     * @return El número de filas actualizadas.
     */
    @Query("UPDATE Reserva SET activo = 0 WHERE id = :id")
    int delete(int id);

    /**
     * Elimina todos los vínculos de quads asociados a una reserva específica.
     * Útil durante el proceso de edición de una reserva para actualizar los vehículos asignados.
     * 
     * @param reservaId Identificador de la reserva de la cual se desean borrar los vínculos.
     */
    @Query("DELETE FROM ReservaQuad WHERE reserva_id = :reservaId")
    void deleteReservaQuads(int reservaId);

    /**
     * Obtiene todas las reservas activas junto con sus quads asociados, ordenadas por fecha de recogida.
     * 
     * @return LiveData que contiene una lista de objetos {@link ReservaConQuads} observables.
     */
    @Transaction
    @Query("SELECT * FROM Reserva WHERE activo = 1 ORDER BY fecha_recogida ASC")
    LiveData<List<ReservaConQuads>> getReservasConQuads();

    @Transaction
    @Query("SELECT * FROM Reserva WHERE activo = 1 AND fecha_recogida > DATE('now') ORDER BY fecha_recogida ASC")
    LiveData<List<ReservaConQuads>> getPrevistas();

    @Transaction
    @Query("SELECT * FROM Reserva WHERE activo = 1 AND fecha_recogida <= DATE('now') AND fecha_devolucion >= DATE('now') ORDER BY fecha_recogida ASC")
    LiveData<List<ReservaConQuads>> getVigentes();

    @Transaction
    @Query("SELECT * FROM Reserva WHERE activo = 1 AND fecha_devolucion < DATE('now') ORDER BY fecha_recogida ASC")
    LiveData<List<ReservaConQuads>> getCaducadas();

    /**
     * Obtiene la lista de vínculos técnicos (incluyendo número de cascos) para una reserva específica.
     * 
     * @param reservaId Identificador de la reserva consultada.
     * @return Lista síncrona de objetos {@link ReservaQuad}.
     */
    @Query("SELECT * FROM ReservaQuad WHERE reserva_id = :reservaId")
    List<ReservaQuad> getReservaQuadsList(int reservaId);

    /**
     * Obtiene la lista completa de reservas activas con sus quads de forma síncrona.
     * 
     * @return Lista síncrona de objetos {@link ReservaConQuads}.
     */
    @Transaction
    @Query("SELECT * FROM Reserva WHERE activo = 1")
    List<ReservaConQuads> getReservasConQuadsSync();

    /**
     * Elimina todos los registros de la tabla Reserva.
     */
    @Query("DELETE FROM Reserva")
    void deleteAll();

    /**
     * Elimina todos los registros de la tabla intermedia ReservaQuad.
     */
    @Query("DELETE FROM ReservaQuad")
    void deleteAllRelaciones();
}