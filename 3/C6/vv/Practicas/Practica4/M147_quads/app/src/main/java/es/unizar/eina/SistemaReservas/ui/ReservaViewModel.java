package es.unizar.eina.SistemaReservas.ui;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList; // Importante
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import es.unizar.eina.SistemaReservas.database.QuadRoomDatabase;
import es.unizar.eina.SistemaReservas.database.Reserva;
import es.unizar.eina.SistemaReservas.database.ReservaConQuads;
import es.unizar.eina.SistemaReservas.database.ReservaRepository;
import es.unizar.eina.SistemaReservas.database.ReservaQuad;

/**
 * ViewModel encargado de la gestión de la lógica de negocio para las reservas.
 * 
 * Proporciona un flujo de datos reactivo y ordenado para la interfaz de usuario, 
 * encargándose de coordinar las operaciones de persistencia mediante el repositorio
 * y de procesar la ordenación dinámica de los listados en memoria.
 */
public class ReservaViewModel extends AndroidViewModel {
    
    /** Referencia al repositorio de reservas para el acceso a datos. */
    private ReservaRepository mRepository;

    /** @return El repositorio de reservas (uso exclusivo para tests). */
    public ReservaRepository getRepository() { return mRepository; }

    /** Enumeración para los criterios de ordenación. */
    public enum SortType { CLIENTE, TELEFONO, FECHA_IN, FECHA_OUT }
    /** Enumeración para la dirección de ordenación. */
    public enum SortDirection { ASC, DESC }
    /** Enumeración para los tipos de filtrado por estado. */
    public enum FilterType { TODAS, PREVISTAS, VIGENTES, CADUCADAS }

    /** Estado observable del criterio de ordenación. */
    private final MutableLiveData<SortType> mSortType = new MutableLiveData<>(SortType.FECHA_IN);
    /** Estado observable de la dirección de ordenación. */
    private final MutableLiveData<SortDirection> mSortDirection = new MutableLiveData<>(SortDirection.ASC);
    /** Estado observable del tipo de filtrado. */
    private final MutableLiveData<FilterType> mFilterType = new MutableLiveData<>(FilterType.TODAS);
    
    /** Flujo de datos final procesado (Filtrado + Ordenado vía SQL). */
    private final LiveData<List<ReservaConQuads>> mProcessedReservas;

    /**
     * Constructor del ViewModel. Configura el switchMap para reaccionar a cambios 
     * en el criterio de ordenación, la dirección o el filtro delegando en el DAO.
     */
    public ReservaViewModel(Application application) {
        super(application);
        mRepository = new ReservaRepository(application);
        
        // Combinamos los estados en un único trigger para el switchMap
        MediatorLiveData<CombinedParams> combinedParams = new MediatorLiveData<>();
        combinedParams.setValue(new CombinedParams(mFilterType.getValue(), mSortType.getValue(), mSortDirection.getValue()));

        combinedParams.addSource(mFilterType, f -> combinedParams.setValue(new CombinedParams(f, mSortType.getValue(), mSortDirection.getValue())));
        combinedParams.addSource(mSortType, s -> combinedParams.setValue(new CombinedParams(mFilterType.getValue(), s, mSortDirection.getValue())));
        combinedParams.addSource(mSortDirection, d -> combinedParams.setValue(new CombinedParams(mFilterType.getValue(), mSortType.getValue(), d)));

        mProcessedReservas = androidx.lifecycle.Transformations.switchMap(combinedParams, params -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            int filterId = 0;
            switch (params.filter) {
                case PREVISTAS: filterId = 1; break;
                case VIGENTES:  filterId = 2; break;
                case CADUCADAS: filterId = 3; break;
            }
            return mRepository.getFilteredReservas(filterId, today, params.sort.name(), params.dir.name());
        });
    }

    /** Helper class para agrupar parámetros de consulta */
    private static class CombinedParams {
        FilterType filter;
        SortType sort;
        SortDirection dir;
        CombinedParams(FilterType f, SortType s, SortDirection d) {
            this.filter = f; this.sort = s; this.dir = d;
        }
    }

    /** Getters y Setters de estado */
    public void setSortType(SortType type) { mSortType.setValue(type); }
    public SortType getSortType() { return mSortType.getValue(); }

    public void setSortDirection(SortDirection dir) { mSortDirection.setValue(dir); }
    public SortDirection getSortDirection() { return mSortDirection.getValue(); }

    public void setFilterType(FilterType type) { mFilterType.setValue(type); }
    public FilterType getFilterType() { return mFilterType.getValue(); }

    /** @return LiveData con la lista final para la UI. */
    public LiveData<List<ReservaConQuads>> getAllReservas() { return mProcessedReservas; }

    /**
     * Solicita la inserción de una nueva reserva junto con sus vínculos a quads.
     * @param reserva Objeto reserva a insertar.
     * @param quads Lista de relaciones para la tabla intermedia.
     */
    public void insert(Reserva reserva, List<ReservaQuad> quads) { mRepository.insert(reserva, quads); }
    
    /**
     * Solicita la eliminación de una reserva de la base de datos.
     * @param reserva Objeto reserva a eliminar.
     */
    public void delete(Reserva reserva) { mRepository.delete(reserva); }
    
    /**
     * Solicita la actualización de los datos de una reserva y sus quads asignados.
     * @param reserva Objeto reserva con datos actualizados.
     * @param quads Nueva lista de relaciones para la tabla intermedia.
     */
    public void update(Reserva reserva, List<ReservaQuad> quads) { mRepository.update(reserva, quads); }
    
    /**
     * Obtiene de forma asíncrona la lista de quads y cascos asociados a una reserva.
     * 
     * @param reservaId ID de la reserva a consultar.
     * @param callback Interfaz funcional que procesará el resultado en el hilo principal.
     */
    public void getReservaQuads(int reservaId, Consumer<List<ReservaQuad>> callback) {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            List<ReservaQuad> result = mRepository.getReservaQuadsSync(reservaId);
            callback.accept(result);
        });
    }
}