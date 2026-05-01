package es.unizar.eina.SistemaReservas.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase que representa la base de datos de la aplicación, basada en Room.
 * Define la configuración de las entidades (Quad, Reserva y ReservaQuad) y 
 * actúa como punto de acceso principal para la conexión de datos.
 * 
 * Implementa el patrón Singleton para prevenir la apertura de múltiples instancias 
 * de la base de datos simultáneamente.
 */
@Database(entities = {Quad.class, Reserva.class, ReservaQuad.class}, version = 8, exportSchema = false)
public abstract class QuadRoomDatabase extends RoomDatabase {

    /**
     * Método abstracto que devuelve una instancia del DAO para la entidad Quad.
     * Room generará la implementación de este método.
     * @return Una instancia de {@link QuadDao}.
     */
    public abstract QuadDao QuadDao();

    /**
     * Método abstracto que devuelve una instancia del DAO para la entidad Reserva.
     * Room generará la implementación de este método.
     * @return Una instancia de {@link ReservaDao}.
     */
    public abstract ReservaDao ReservaDao();

    /** Instancia única (singleton) de la base de datos. */
    private static volatile QuadRoomDatabase INSTANCE;
    
    /** Número de hilos asignados para el pool de ejecución de la base de datos. */
    private static final int NUMBER_OF_THREADS = 4;
    
    /** 
     * ExecutorService para ejecutar operaciones de base de datos en hilos de fondo. 
     * Centraliza las tareas de escritura para evitar colisiones.
     */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Devuelve la instancia singleton de la base de datos. 
     * Si la instancia no existe, la crea utilizando el patrón de doble comprobación (double-check locking).
     * 
     * @param context El contexto de la aplicación.
     * @return La instancia única de {@link QuadRoomDatabase}.
     */
    static QuadRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (QuadRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            QuadRoomDatabase.class, "quad_database")
                            .fallbackToDestructiveMigration() 
                            .addCallback(sRoomDatabaseCallback) 
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback encargado de realizar tareas específicas al momento de la creación de la base de datos.
     * En esta implementación, se utiliza para pre-poblar la base de datos con datos de prueba 
     * (Quads iniciales y una reserva de ejemplo).
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        /**
         * Se ejecuta cuando la base de datos es creada por primera vez.
         * @param db La instancia de la base de datos SQLite.
         */
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                QuadDao qDao = INSTANCE.QuadDao();
                ReservaDao rDao = INSTANCE.ReservaDao();

                // 1. Insertar Quads iniciales
                // Guardamos los IDs generados para poder usarlos en la relación de la reserva
                long idQ1 = qDao.insert(new Quad("1234-BBB", true, 45.0, "Quad monoplaza ágil para montaña"));
                long idQ2 = qDao.insert(new Quad("5678-CCC", false, 70.0, "Quad biplaza gran potencia"));

                // 2. Insertar Reserva de "verdillo"
                long idRes = rDao.insert(new Reserva(
                        "verdillo", 
                        603705590, 
                        "2026-01-15", 
                        "2026-01-17"
                ));

                // 3. Vincular la reserva con los Quads (Tabla intermedia ReservaQuad)
                // Vamos a asignarle el primer quad (idQ1) con 1 casco, y guardamos el precio acordado de 45.0
                List<ReservaQuad> relaciones = new ArrayList<>();
                relaciones.add(new ReservaQuad((int)idRes, (int)idQ1, 1, 45.0));
                
                rDao.insertReservaQuads(relaciones);
            });
        }
    };
}