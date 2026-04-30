package es.unizar.eina.SistemaReservas.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad Quad.
 * Define las operaciones permitidas sobre la tabla "Quad" en la base de datos.
 */
@Dao
public interface QuadDao {

    /**
     * Inserta un nuevo Quad en la base de datos.
     * 
     * @param quad El objeto Quad a insertar.
     * @return El identificador de la fila recién insertada, o -1 si hubo un conflicto 
     *         (debido a OnConflictStrategy.IGNORE).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Quad quad);

    /**
     * Actualiza la información de un Quad existente.
     * 
     * @param quad El objeto Quad con la información actualizada.
     * @return El número de filas actualizadas en la base de datos (debería ser 1).
     */
    @Update
    int update(Quad quad);

    /**
     * Elimina lógicamente un Quad de la base de datos (lo marca como inactivo).
     * 
     * @param id El identificador del Quad a eliminar.
     * @return El número de filas actualizadas (debería ser 1).
     */
    @Query("UPDATE Quad SET activo = 0 WHERE id = :id")
    int delete(int id);

    /**
     * Elimina todos los registros de la tabla Quad (físico, usado para tests/limpieza).
     * 
     * @return El número de filas eliminadas.
     */
    @Query("DELETE FROM Quad")
    int deleteAll();

    /**
     * Obtiene todos los Quads activos ordenados alfabéticamente por matrícula.
     * 
     * @return Un objeto LiveData que contiene la lista de Quads.
     */
    @Query("SELECT * FROM Quad WHERE activo = 1 ORDER BY matricula ASC")
    LiveData<List<Quad>> getOrderedQuadsByMatricula();
    
    /**
     * Obtiene la lista completa de Quads activos de forma síncrona.
     * 
     * @return Una lista con todos los objetos Quad almacenados y activos.
     */
    @Query("SELECT * FROM Quad WHERE activo = 1 ORDER BY matricula ASC")
    List<Quad> getAllQuadsList();

    /**
     * Obtiene un Quad activo de forma síncrona según su ID.
     * 
     * @param id El identificador único del quad.
     * @return El objeto Quad o null si no se encuentra o está inactivo.
     */
    @Query("SELECT * FROM Quad WHERE id = :id AND activo = 1 LIMIT 1")
    Quad getQuadByIdSync(int id);

    /**
     * Obtiene todos los Quads activos ordenados por tipo (monoplazas primero) y matrícula.
     * 
     * @return LiveData con la lista de Quads ordenada por tipo.
     */
    @Query("SELECT * FROM Quad WHERE activo = 1 ORDER BY esmonoplaza DESC, matricula ASC")
    LiveData<List<Quad>> getOrderedQuadsByTipo();

    /**
     * Obtiene todos los Quads activos ordenados por precio ascendente y matrícula.
     * 
     * @return LiveData con la lista de Quads ordenada por precio.
     */
    @Query("SELECT * FROM Quad WHERE activo = 1 ORDER BY precio ASC, matricula ASC")
    LiveData<List<Quad>> getOrderedQuadsByPrecio();
}