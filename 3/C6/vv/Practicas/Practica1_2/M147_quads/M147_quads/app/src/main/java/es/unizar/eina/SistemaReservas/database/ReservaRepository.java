package es.unizar.eina.SistemaReservas.database;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Clase de repositorio encargada de gestionar el acceso a los datos de las Reservas.
 * Actúa como capa de abstracción sobre los DAOs para centralizar las operaciones de 
 * persistencia, validación de fechas y gestión de la relación Muchos a Muchos 
 * con los vehículos.
 */
public class ReservaRepository {
    
    /** DAO para el acceso a los datos de las reservas. */
    private final ReservaDao mReservaDao;
    
    /** Lista observable de todas las reservas incluyendo sus vehículos asociados. */
    private final LiveData<List<ReservaConQuads>> mAllReservas;

    /**
     * Constructor del repositorio de reservas.
     * @param application Contexto de la aplicación utilizado para inicializar la base de datos.
     */
    public ReservaRepository(Application application) {
        QuadRoomDatabase db = QuadRoomDatabase.getDatabase(application);
        mReservaDao = db.ReservaDao();
        mAllReservas = mReservaDao.getReservasConQuads();
    }

    /** @return LiveData que permite observar cambios en la lista completa de reservas. */
    public LiveData<List<ReservaConQuads>> getAllReservas() {
        return mAllReservas;
    }

    /**
     * Realiza la inserción asíncrona de una reserva y sus quads asociados.
     * Primero inserta la reserva para obtener su ID y luego asigna dicho ID 
     * a cada entrada de la tabla intermedia.
     * 
     * @param reserva Objeto reserva a insertar.
     * @param quads Lista de vehículos vinculados a dicha reserva.
     */
    public void insert(Reserva reserva, List<ReservaQuad> quads) {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            long reservaId = mReservaDao.insert(reserva);
            for (ReservaQuad rq : quads) {
                rq.setReservaId((int) reservaId);
            }
            mReservaDao.insertReservaQuads(quads);
        });
    }

    /**
     * Elimina una reserva de forma lógica marcándola como inactiva.
     * 
     * @param reserva El objeto reserva a desactivar.
     */
    public void delete(Reserva reserva) {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> 
            mReservaDao.logicalDelete(reserva.getId())
        );
    }
    
    /**
     * Actualiza una reserva existente y gestiona la actualización de sus vehículos.
     * El proceso consiste en actualizar los datos básicos, eliminar todos los quads 
     * previamente asignados e insertar la nueva lista proporcionada.
     * 
     * @param reserva Objeto reserva con los datos actualizados.
     * @param quads Nueva lista de vehículos para esta reserva.
     */
    public void update(Reserva reserva, List<ReservaQuad> quads) {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            mReservaDao.update(reserva);
            mReservaDao.deleteReservaQuads(reserva.getId());
            for (ReservaQuad rq : quads) {
                rq.setReservaId(reserva.getId());
            }
            mReservaDao.insertReservaQuads(quads);
        });
    }

    /**
     * Obtiene la lista de relaciones técnicas (incluyendo cascos) de forma síncrona.
     * @param reservaId ID de la reserva consultada.
     * @return Lista de objetos {@link ReservaQuad}.
     */
    public List<ReservaQuad> getReservaQuadsSync(int reservaId) {
        return mReservaDao.getReservaQuadsList(reservaId);
    }

    /**
     * Elimina de forma masiva todas las reservas y sus relaciones en la base de datos.
     */
    public void deleteAllData() {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            mReservaDao.deleteAllRelaciones(); // Limpia tabla intermedia
            mReservaDao.deleteAll();           // Limpia reservas
        });
    }

    /**
     * Inserta una reserva de forma síncrona validando reglas de negocio específicas.
     * Verifica que el nombre/teléfono no estén vacíos, que haya quads asignados y 
     * que la fecha de devolución sea posterior a la de recogida.
     * 
     * @param reserva Objeto reserva a validar e insertar.
     * @param quads Lista de vehículos vinculados.
     * @return El ID de la reserva insertada o -1 si falla alguna validación o hay error.
     */
    public long insertSync(Reserva reserva, List<ReservaQuad> quads) {
        try {
            // 1. Validar Nombre y Teléfono (Casos 2 y 5 de tu tabla)
            if (reserva.getNombreCliente().trim().isEmpty() || reserva.getTelefono() == 0) {
                return -1;
            }

            // 2. Validar que hay al menos un Quad (Caso 4 de tu tabla)
            if (quads == null || quads.isEmpty()) {
                return -1;
            }

            // 3. Validar coherencia de fechas (Caso 3 de tu tabla)
            // Al usar ISO 8601 (yyyy-MM-dd), la comparación alfanumérica es equivalente a la cronológica.
            String fechaIn = reserva.getFechaRecogida();
            String fechaOut = reserva.getFechaDevolucion();

            if (fechaIn == null || fechaOut == null || fechaOut.compareTo(fechaIn) < 0) {
                return -1; // Fallo: Devolución anterior a la recogida
            }

        } catch (Exception e) {
            return -1;
        }

        // 5. Si todo es correcto, procedemos con la inserción
        java.util.concurrent.Future<Long> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> {
            long id = mReservaDao.insert(reserva);
            for (ReservaQuad rq : quads) {
                rq.setReservaId((int) id);
            }
            mReservaDao.insertReservaQuads(quads);
            return id;
        });

        try {
            return future.get(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            return -1;
        }
    }
}