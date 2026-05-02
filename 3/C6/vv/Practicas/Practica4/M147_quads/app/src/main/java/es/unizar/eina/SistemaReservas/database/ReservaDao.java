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
     * Elimina una reserva de forma lógica marcándola como inactiva.
     * 
     * @param id Identificador de la reserva a desactivar.
     * @return El número de filas actualizadas.
     */
    @Query("UPDATE Reserva SET estaActivo = 0 WHERE Id = :id")
    int logicalDelete(int id);

    /**
     * Elimina todos los vínculos de quads asociados a una reserva específica.
     * Útil durante el proceso de edición de una reserva para actualizar los vehículos asignados.
     * 
     * @param reservaId Identificador de la reserva de la cual se desean borrar los vínculos.
     */
    @Query("DELETE FROM Quad_Reserva WHERE ReservaId = :reservaId")
    void deleteReservaQuads(int reservaId);

    /**
     * Obtiene todas las reservas activas junto con sus quads asociados, ordenadas por fecha de recogida.
     * Se ejecuta en una transacción para garantizar la consistencia al recuperar datos de múltiples tablas.
     * 
     * @return LiveData que contiene una lista de objetos {@link ReservaConQuads} observables.
     */
    @Transaction
    @Query("SELECT * FROM Reserva WHERE estaActivo = 1 ORDER BY fecha_recogida ASC")
    LiveData<List<ReservaConQuads>> getReservasConQuads();

    /**
     * Obtiene reservas filtradas por estado (previstas, vigentes, caducadas) y ordenadas 
     * según el criterio solicitado. Implementa la lógica de filtrado en SQL para RNF 1.
     * 
     * @param filter Filtro de estado: 0=Todas, 1=Previstas, 2=Vigentes, 3=Caducadas.
     * @param today Fecha actual en formato ISO 8601 (yyyy-MM-dd).
     * @return LiveData con la lista de reservas filtrada y ordenada.
     */
    @Transaction
    @Query("SELECT * FROM Reserva WHERE estaActivo = 1 AND (" +
           "(:filter = 0) OR " +
           "(:filter = 1 AND fecha_recogida > :today) OR " +
           "(:filter = 2 AND fecha_recogida <= :today AND fecha_devolucion >= :today) OR " +
           "(:filter = 3 AND fecha_devolucion < :today)) " +
           "ORDER BY " +
           "CASE WHEN :sort = 'CLIENTE' AND :dir = 'ASC' THEN nombre_cliente END ASC, " +
           "CASE WHEN :sort = 'CLIENTE' AND :dir = 'DESC' THEN nombre_cliente END DESC, " +
           "CASE WHEN :sort = 'TELEFONO' AND :dir = 'ASC' THEN num_telefono END ASC, " +
           "CASE WHEN :sort = 'TELEFONO' AND :dir = 'DESC' THEN num_telefono END DESC, " +
           "CASE WHEN :sort = 'FECHA_IN' AND :dir = 'ASC' THEN fecha_recogida END ASC, " +
           "CASE WHEN :sort = 'FECHA_IN' AND :dir = 'DESC' THEN fecha_recogida END DESC, " +
           "CASE WHEN :sort = 'FECHA_OUT' AND :dir = 'ASC' THEN fecha_devolucion END ASC, " +
           "CASE WHEN :sort = 'FECHA_OUT' AND :dir = 'DESC' THEN fecha_devolucion END DESC")
    LiveData<List<ReservaConQuads>> getFilteredReservas(int filter, String today, String sort, String dir);

    /**
     * Obtiene todos los vínculos de quads asociados a una reserva específica.
     * 
     * @param reservaId Identificador de la reserva consultada.
     * @return Lista síncrona de objetos {@link ReservaQuad}.
     */
    @Query("SELECT * FROM Quad_Reserva WHERE ReservaId = :reservaId")
    List<ReservaQuad> getReservaQuadsList(int reservaId);

    /**
     * Obtiene la lista completa de reservas activas con sus quads de forma síncrona.
     * 
     * @return Lista síncrona de objetos {@link ReservaConQuads}.
     */
    @Transaction
    @Query("SELECT * FROM Reserva WHERE estaActivo = 1")
    List<ReservaConQuads> getReservasConQuadsSync();

    /**
     * Elimina todos los registros de la tabla Reserva.
     */
    @Query("DELETE FROM Reserva")
    void deleteAll();

    /**
     * Elimina todos los registros de la tabla intermedia Quad_Reserva.
     */
    @Query("DELETE FROM Quad_Reserva")
    void deleteAllRelaciones();
}