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

    /** DAO para el acceso a los datos de los vehículos (necesario para validaciones). */
    private final QuadDao mQuadDao;

    /**
     * Constructor del repositorio de reservas.
     * @param application Contexto de la aplicación utilizado para inicializar la base de datos.
     */
    public ReservaRepository(Application application) {
        QuadRoomDatabase db = QuadRoomDatabase.getDatabase(application);
        mReservaDao = db.ReservaDao();
        mQuadDao = db.QuadDao();
        mAllReservas = mReservaDao.getReservasConQuads();
    }

    /** @return LiveData que permite observar cambios en la lista completa de reservas. */
    public LiveData<List<ReservaConQuads>> getAllReservas() {
        return mAllReservas;
    }

    /** Elimina todas las reservas (uso exclusivo para tests). */
    public void deleteAll() {
        deleteAllData();
    }

    /** @return El número total de reservas (uso exclusivo para tests). */
    public int getNumeroReservas() {
        java.util.concurrent.Future<Integer> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> mReservaDao.getReservasConQuadsSync().size());
        try { return future.get(15000, java.util.concurrent.TimeUnit.MILLISECONDS); } catch (Exception e) { return 0; }
    }

    /**
     * Obtiene una lista de reservas filtrada y ordenada mediante SQL (RNF 1).
     */
    public LiveData<List<ReservaConQuads>> getFilteredReservas(int filter, String today, String sort, String dir) {
        return mReservaDao.getFilteredReservas(filter, today, sort, dir);
    }

    /**
     * Realiza la validación integral de una reserva (RF 6).
     * @return true si es válida, false en caso contrario.
     */
    private boolean validateReserva(Reserva reserva, List<ReservaQuad> quads) {
        if (reserva == null) return false;
        if (reserva.getNombreCliente() == null || reserva.getNombreCliente().trim().isEmpty() || reserva.getTelefono() <= 0) return false;
        if (quads == null || quads.isEmpty()) return false;
        
        String fIn = reserva.getFechaRecogida();
        String fOut = reserva.getFechaDevolucion();
        if (fIn == null || fOut == null) return false;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false); // Validación estricta de días/meses reales

        try {
            Date dateIn = sdf.parse(fIn);
            Date dateOut = sdf.parse(fOut);
            
            // Validación de coherencia temporal (RF 5)
            if (dateOut.before(dateIn)) return false;

        } catch (java.text.ParseException e) {
            // El formato no es yyyy-MM-dd o la fecha no es real
            return false;
        }

        // Validación de Cascos (RF 6): Un monoplaza no puede tener > 1 casco
        for (ReservaQuad rq : quads) {
            Quad q = mQuadDao.getQuadSync(rq.getQuadId());
            if (q != null && q.getEsmonoplaza() && rq.getNumCascos() > 1) {
                return false; 
            }
        }
        return true;
    }

    /**
     * Realiza la inserción asíncrona de una reserva y sus quads asociados previa validación.
     */
    public void insert(Reserva reserva, List<ReservaQuad> quads) {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            if (!validateReserva(reserva, quads)) return;
            long reservaId = mReservaDao.insert(reserva);
            for (ReservaQuad rq : quads) {
                rq.setReservaId((int) reservaId);
            }
            mReservaDao.insertReservaQuads(quads);
        });
    }

    /**
     * Elimina una reserva de forma lógica marcándola como inactiva.
     * @return 1 si se desactivó, 0 en caso contrario.
     */
    public int delete(Reserva reserva) {
        if (reserva == null) return 0;
        java.util.concurrent.Future<Integer> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> mReservaDao.logicalDelete(reserva.getId()));
        try { return future.get(10, java.util.concurrent.TimeUnit.SECONDS); } catch (Exception e) { return 0; }
    }
    
    /**
     * Actualiza una reserva existente y gestiona la actualización de sus vehículos.
     * @return 1 si se actualizó, 0 en caso contrario.
     */
    public int update(Reserva reserva, List<ReservaQuad> quads) {
        java.util.concurrent.Future<Integer> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> {
            if (!validateReserva(reserva, quads)) return 0;
            int affected = mReservaDao.update(reserva);
            if (affected > 0) {
                mReservaDao.deleteReservaQuads(reserva.getId());
                for (ReservaQuad rq : quads) {
                    rq.setReservaId(reserva.getId());
                }
                mReservaDao.insertReservaQuads(quads);
            }
            return affected;
        });
        try { return future.get(10, java.util.concurrent.TimeUnit.SECONDS); } catch (Exception e) { return 0; }
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
        try {
            QuadRoomDatabase.databaseWriteExecutor.submit(() -> {
                mReservaDao.deleteAllRelaciones(); // Limpia tabla intermedia
                mReservaDao.deleteAll();           // Limpia reservas
            }).get(15, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            android.util.Log.e("ReservaRepository", "Error deleteAllData", e);
        }
    }

    /**
     * Inserta una reserva de forma síncrona validando reglas de negocio específicas.
     */
    public long insertSync(Reserva reserva, List<ReservaQuad> quads) {
        java.util.concurrent.Future<Long> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> {
            if (!validateReserva(reserva, quads)) return -1L;
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