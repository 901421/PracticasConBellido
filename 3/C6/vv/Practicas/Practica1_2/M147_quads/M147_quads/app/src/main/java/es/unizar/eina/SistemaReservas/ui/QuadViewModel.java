package es.unizar.eina.SistemaReservas.ui;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import java.util.List;
import es.unizar.eina.SistemaReservas.database.QuadRepository;
import es.unizar.eina.SistemaReservas.database.Quad;
import java.util.function.Consumer; 
import es.unizar.eina.SistemaReservas.database.QuadRoomDatabase;

/**
 * ViewModel encargado de gestionar la lógica de negocio y la preparación de datos 
 * para las vistas relacionadas con los vehículos Quad.
 * 
 * Actúa como puente entre la interfaz de usuario y el repositorio, permitiendo 
 * operaciones de persistencia y gestión reactiva de listados mediante LiveData.
 */
public class QuadViewModel extends AndroidViewModel {

    /** Repositorio para la gestión de datos de Quads. */
    private QuadRepository mRepository;
    
    /** 
     * Estado observable que define el criterio de ordenación actual.
     * 0: Matrícula, 1: Tipo, 2: Precio.
     */
    private final MutableLiveData<Integer> sortOrder = new MutableLiveData<>(0);

    /** Lista observable de Quads que se actualiza automáticamente según el criterio de ordenación. */
    private final LiveData<List<Quad>> mAllQuads;

    /**
     * Constructor del ViewModel. Inicializa el repositorio y configura la transformación 
     * switchMap para reaccionar a los cambios en el orden seleccionado.
     * 
     * @param application Contexto de la aplicación.
     */
    public QuadViewModel(Application application) {
        super(application);
        mRepository = new QuadRepository(application);

        mAllQuads = Transformations.switchMap(sortOrder, order -> {
            switch (order) {
                case 1: return mRepository.getAllQuadsByTipo();
                case 2: return mRepository.getAllQuadsByPrecio();
                default: return mRepository.getAllQuadsByMatricula();
            }
        });
    }

    /** @return LiveData que contiene la lista de quads bajo el orden actual. */
    public LiveData<List<Quad>> getAllQuads() { return mAllQuads; }

    /**
     * Actualiza el criterio de ordenación de la lista.
     * 
     * @param order Entero que representa el nuevo orden (0: Matrícula, 1: Tipo, 2: Precio).
     */
    public void setSortOrder(int order) {
        sortOrder.setValue(order);
    }

    /**
     * Solicita la inserción de un nuevo Quad.
     * @param quad Objeto Quad a insertar.
     * @return El ID generado o -1 en caso de error.
     */
    public long insert(Quad quad) { return mRepository.insert(quad); }
    
    /**
     * Solicita la actualización de un Quad existente.
     * @param quad Objeto Quad con los datos actualizados.
     */
    public void update(Quad quad) { mRepository.update(quad); }
    
    /**
     * Solicita la eliminación de un Quad.
     * @param quad Objeto Quad a eliminar.
     */
    public void delete(Quad quad) { mRepository.delete(quad); }

    /**
     * Obtiene de forma asíncrona la lista de quads disponibles para un rango de fechas.
     * Ejecuta la lógica en un hilo de fondo y devuelve el resultado al hilo principal 
     * mediante un callback.
     * 
     * @param fechaIn Fecha de inicio de disponibilidad.
     * @param fechaOut Fecha de fin de disponibilidad.
     * @param reservaId ID de la reserva a excluir de la comprobación (edición).
     * @param callback Interfaz funcional que recibirá la lista de quads disponibles.
     */
    public void getAvailableQuads(String fechaIn, String fechaOut, int reservaId, Consumer<List<Quad>> callback) {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            List<Quad> result = mRepository.getAvailableQuadsSync(fechaIn, fechaOut, reservaId);
            
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                callback.accept(result);
            });
        });
    }
}