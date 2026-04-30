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
    
    /** DAO para acceder a los quads y calcular el precio. */
    private final QuadDao mQuadDao;
    
    /** Lista observable de todas las reservas incluyendo sus vehículos asociados. */
    private final LiveData<List<ReservaConQuads>> mAllReservas;

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

    public LiveData<List<ReservaConQuads>> getPrevistas() {
        return mReservaDao.getPrevistas();
    }

    public LiveData<List<ReservaConQuads>> getVigentes() {
        return mReservaDao.getVigentes();
    }

    public LiveData<List<ReservaConQuads>> getCaducadas() {
        return mReservaDao.getCaducadas();
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
            double totalPrecioQuads = 0.0;
            for (ReservaQuad rq : quads) {
                Quad q = mQuadDao.getQuadByIdSync(rq.getQuadId());
                if (q != null) totalPrecioQuads += q.getPrecio();
            }

            long dias = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date in = sdf.parse(reserva.getFechaRecogida());
                Date out = sdf.parse(reserva.getFechaDevolucion());
                if (in != null && out != null) {
                    long diff = out.getTime() - in.getTime();
                    dias = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (dias < 1) dias = 1;
                }
            } catch (Exception e) {}

            reserva.setPrecioTotal(totalPrecioQuads * dias);

            long reservaId = mReservaDao.insert(reserva);
            for (ReservaQuad rq : quads) {
                rq.setReservaId((int) reservaId);
            }
            mReservaDao.insertReservaQuads(quads);
        });
    }

    /**
     * Elimina una reserva de forma lógica (soft delete).
     * Utiliza un objeto Future para esperar la confirmación de la base de datos.
     * 
     * @param reserva El objeto reserva a eliminar.
     * @return El número de filas eliminadas o -1 en caso de error o timeout.
     */
    public int delete(Reserva reserva) {
        java.util.concurrent.Future<Integer> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> 
            mReservaDao.delete(reserva.getId())
        );

        try {
            return future.get(15000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            android.util.Log.e("ReservaRepository", "Error al eliminar reserva", e);
            return -1; 
        }
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
        // 1. Definir el formato (ISO 8601)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        
        final java.util.Date finalFechaIn;
        final java.util.Date finalFechaOut;

        try {
            // 2. Validar Nombre y Teléfono (Casos 2 y 5 de tu tabla)
            if (reserva.getNombreCliente().trim().isEmpty() || reserva.getTelefono().trim().isEmpty()) {
                return -1;
            }

            // 3. Validar que hay al menos un Quad (Caso 4 de tu tabla)
            if (quads == null || quads.isEmpty()) {
                return -1;
            }

            // 4. Validar coherencia de fechas (Caso 3 de tu tabla)
            // USAMOS LOS NOMBRES CORRECTOS: getFechaRecogida() y getFechaDevolucion()
            finalFechaIn = sdf.parse(reserva.getFechaRecogida());
            finalFechaOut = sdf.parse(reserva.getFechaDevolucion());

            if (finalFechaIn != null && finalFechaOut != null) {
                if (finalFechaOut.before(finalFechaIn)) {
                    return -1; // Fallo: Devolución anterior a la recogida
                }
            } else {
                return -1;
            }

        } catch (Exception e) {
            // Si hay error en el formato de fecha, devolvemos -1
            return -1;
        }

        // 5. Si todo es correcto, procedemos con la inserción
        java.util.concurrent.Future<Long> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> {
            double totalPrecioQuads = 0.0;
            for (ReservaQuad rq : quads) {
                Quad q = mQuadDao.getQuadByIdSync(rq.getQuadId());
                if (q != null) totalPrecioQuads += q.getPrecio();
            }

            long dias = 1;
            try {
                // Fechas ya están validadas, calculamos diff
                long diff = finalFechaOut.getTime() - finalFechaIn.getTime();
                dias = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (dias < 1) dias = 1;
            } catch (Exception e) {}

            reserva.setPrecioTotal(totalPrecioQuads * dias);

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