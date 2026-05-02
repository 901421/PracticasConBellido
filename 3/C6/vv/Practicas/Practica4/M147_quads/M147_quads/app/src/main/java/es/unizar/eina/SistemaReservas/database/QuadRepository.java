package es.unizar.eina.SistemaReservas.database;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Clase de repositorio encargada de gestionar el acceso a los datos de los Quads.
 * Actúa como mediador entre los DAOs y el resto de la aplicación, abstrayendo
 * la lógica de persistencia y las comprobaciones de disponibilidad.
 */
public class QuadRepository {

    /** DAO para la gestión de vehículos. */
    private final QuadDao mQuadDao;
    /** DAO para la gestión de reservas (necesario para verificar disponibilidad). */
    private final ReservaDao mReservaDao; 
    /** Lista observable de todos los quads ordenados por matrícula. */
    private final LiveData<List<Quad>> mAllQuads;
    /** Tiempo máximo de espera (en milisegundos) para operaciones síncronas. */
    private final long TIMEOUT = 15000;

    /**
     * Constructor del repositorio. Inicializa la base de datos y los DAOs.
     * @param application Contexto de la aplicación.
     */
    public QuadRepository(Application application) {
        QuadRoomDatabase db = QuadRoomDatabase.getDatabase(application);
        mQuadDao = db.QuadDao();
        mReservaDao = db.ReservaDao(); 
        mAllQuads = mQuadDao.getOrderedQuadsByMatricula();
    }

    /** @return LiveData con todos los quads ordenados por matrícula. */
    public LiveData<List<Quad>> getAllQuadsByMatricula() { return mAllQuads; }
    
    /** @return LiveData con todos los quads ordenados por tipo (monoplazas primero). */
    public LiveData<List<Quad>> getAllQuadsByTipo() { return mQuadDao.getOrderedQuadsByTipo(); }
    
    /** @return LiveData con todos los quads ordenados por precio ascendente. */
    public LiveData<List<Quad>> getAllQuadsByPrecio() { return mQuadDao.getOrderedQuadsByPrecio(); }

    /**
     * Realiza la validación integral de un Quad.
     * @return true si es válido, false en caso contrario.
     */
    private boolean validateQuad(Quad quad) {
        if (quad == null) return false;
        String regexMatricula = "^[0-9]{4}-[A-Z]{3}$";
        if (quad.getMatricula() == null || 
            quad.getMatricula().trim().isEmpty() || 
            !quad.getMatricula().matches(regexMatricula)) {
            return false;
        }
        if (quad.getPrecio() < 0) return false;
        return true;
    }

    /**
     * Inserta un nuevo Quad en la base de datos previa validación.
     * Realiza una comprobación de formato de matrícula (4 números, guion, 3 letras) 
     * y asegura que el precio no sea negativo.
     * 
     * @param quad El objeto Quad a insertar.
     * @return El ID de la fila insertada o -1 si la validación falla o hay error.
     */
    public long insert(Quad quad) {
        if (!validateQuad(quad)) return -1;

        Future<Long> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> mQuadDao.insert(quad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.e("QuadRepository", "Error insert", ex);
            return -1;
        }
    }

    /**
     * Actualiza la información de un Quad existente en la base de datos.
     * @param quad El objeto Quad con los datos actualizados.
     * @return El número de filas afectadas o -1 en caso de error.
     */
    public int update(Quad quad) {
        if (!validateQuad(quad)) return 0;
        Future<Integer> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> mQuadDao.update(quad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.e("QuadRepository", "Error update", ex);
            return -1;
        }
    }

    /**
     * Elimina un Quad de la base de datos de forma lógica.
     * @param quad El objeto Quad a desactivar.
     */
    public void logicalDelete(Quad quad) {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> mQuadDao.logicalDelete(quad.getId()));
    }

    /**
     * Elimina un registro de la tabla Quad de forma síncrona y física.
     * @param quad El objeto Quad a eliminar.
     * @return El número de filas eliminadas (1 si éxito, 0 si no).
     */
    public int delete(Quad quad) {
        if (quad == null) return 0;
        java.util.concurrent.Future<Integer> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> mQuadDao.delete(quad));
        try { return future.get(TIMEOUT, TimeUnit.MILLISECONDS); } catch (Exception e) { return 0; }
    }

    /**
     * Elimina todos los registros de la tabla Quad de forma física (síncrona para tests).
     */
    public void deleteAll() {
        try {
            QuadRoomDatabase.databaseWriteExecutor.submit(() -> mQuadDao.deleteAll()).get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Log.e("QuadRepository", "Error deleteAll", e);
        }
    }

    /**
     * Calcula la lista de vehículos disponibles para un rango de fechas determinado.
     * Cruza la información de todos los quads activos con las reservas activas que solapan 
     * con el periodo solicitado para identificar qué vehículos están libres.
     * 
     * @param fechaInStr Fecha de recogida en formato "dd/MM/yyyy".
     * @param fechaOutStr Fecha de devolución en formato "dd/MM/yyyy".
     * @param currentReservaId ID de la reserva actual (para ignorarla en caso de edición).
     * @return Lista de objetos Quad que están disponibles para las fechas indicadas.
     */
    public List<Quad> getAvailableQuadsSync(String fechaInStr, String fechaOutStr, int currentReservaId) {
        List<Quad> availableQuads = new ArrayList<>();

        try {
            // ISO 8601 permite comparaciones directas de cadenas si el formato es yyyy-MM-dd
            // No obstante, para mayor seguridad y cálculo de solapamientos complejos, seguimos parseando.
            SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            Date newStart = dbSdf.parse(fechaInStr);
            Date newEnd = dbSdf.parse(fechaOutStr);

            if (newStart == null || newEnd == null) return new ArrayList<>();

            List<Quad> allQuads = mQuadDao.getAllQuadsList();
            List<ReservaConQuads> allReservas = mReservaDao.getReservasConQuadsSync();
            Set<Integer> busyQuadIds = new HashSet<>();

            if (allReservas != null) {
                for (ReservaConQuads resConQuads : allReservas) {
                    if (resConQuads.reserva.getId() == currentReservaId) continue;

                    try {
                        Date resStart = dbSdf.parse(resConQuads.reserva.getFechaRecogida());
                        Date resEnd = dbSdf.parse(resConQuads.reserva.getFechaDevolucion());

                        if (resStart != null && resEnd != null) {
                            // Si hay solapamiento de fechas
                            if (!newStart.after(resEnd) && !newEnd.before(resStart)) {
                                for (Quad q : resConQuads.quads) {
                                    busyQuadIds.add(q.getId());
                                }
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace(); 
                    }
                }
            }

            for (Quad q : allQuads) {
                if (!busyQuadIds.contains(q.getId())) {
                    availableQuads.add(q);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return availableQuads;
    }

    /** @return El número total de quads activos (uso exclusivo para tests). */
    public int getNumeroQuads() {
        Future<Integer> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> mQuadDao.getAllQuadsList().size());
        try { return future.get(TIMEOUT, TimeUnit.MILLISECONDS); } catch (Exception e) { return 0; }
    }

    /** @return Un quad por su ID (uso exclusivo para tests). */
    public Quad getQuadById(int id) {
        Future<Quad> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> mQuadDao.getQuadSync(id));
        try { return future.get(TIMEOUT, TimeUnit.MILLISECONDS); } catch (Exception e) { return null; }
    }
}