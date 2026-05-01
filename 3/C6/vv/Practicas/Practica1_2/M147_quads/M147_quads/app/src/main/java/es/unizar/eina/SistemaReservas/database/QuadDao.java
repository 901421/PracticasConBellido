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
     * Elimina un Quad específico de la base de datos de forma lógica.
     * 
     * @param id El identificador del Quad a desactivar.
     */
    @Query("UPDATE Quad SET estaActivo = 0 WHERE Id = :id")
    void logicalDelete(int id);

    /**
     * Elimina todos los registros de la tabla Quad.
     * 
     * @return El número de filas eliminadas.
     */
    @Query("DELETE FROM Quad")
    int deleteAll();

    /**
     * Obtiene todos los Quads activos ordenados alfabéticamente por matrícula.
     * 
     * @return Un objeto LiveData que contiene la lista de Quads activos.
     */
    @Query("SELECT * FROM Quad WHERE estaActivo = 1 ORDER BY Matricula ASC")
    LiveData<List<Quad>> getOrderedQuadsByMatricula();
    
    /**
     * Obtiene la lista completa de Quads activos de forma síncrona.
     * 
     * @return Una lista con todos los objetos Quad activos almacenados.
     */
    @Query("SELECT * FROM Quad WHERE estaActivo = 1 ORDER BY Matricula ASC")
    List<Quad> getAllQuadsList();

    /**
     * Obtiene todos los Quads activos ordenados por tipo (monoplazas primero) y matrícula.
     * 
     * @return LiveData con la lista de Quads activos ordenada por tipo.
     */
    @Query("SELECT * FROM Quad WHERE estaActivo = 1 ORDER BY esMonoplaza DESC, Matricula ASC")
    LiveData<List<Quad>> getOrderedQuadsByTipo();

    /**
     * Obtiene todos los Quads activos ordenados por precio ascendente y matrícula.
     * 
     * @return LiveData con la lista de Quads activos ordenada por precio.
     */
    @Query("SELECT * FROM Quad WHERE estaActivo = 1 ORDER BY precio ASC, Matricula ASC")
    LiveData<List<Quad>> getOrderedQuadsByPrecio();

    /**
     * Obtiene un Quad específico por su identificador.
     * @param id Identificador del quad.
     * @return El objeto Quad o null si no se encuentra.
     */
    @Query("SELECT * FROM Quad WHERE Id = :id")
    Quad getQuadSync(int id);
}