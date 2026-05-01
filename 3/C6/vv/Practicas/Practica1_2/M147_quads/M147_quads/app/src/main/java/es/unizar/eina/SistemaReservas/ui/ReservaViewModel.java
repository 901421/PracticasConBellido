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
    
    /** Flujo de datos final procesado (Filtrado + Ordenado). */
    private final MediatorLiveData<List<ReservaConQuads>> mProcessedReservas = new MediatorLiveData<>();

    /**
     * Constructor del ViewModel. Configura el MediatorLiveData para reaccionar a cambios 
     * en la base de datos, el criterio de ordenación, la dirección o el filtro.
     */
    public ReservaViewModel(Application application) {
        super(application);
        mRepository = new ReservaRepository(application);
        
        LiveData<List<ReservaConQuads>> source = mRepository.getAllReservas();

        // Reaccionar a cambios en los datos base
        mProcessedReservas.addSource(source, data -> updateProcessedList(data, mSortType.getValue(), mSortDirection.getValue(), mFilterType.getValue()));
        // Reaccionar a cambios en el tipo de orden
        mProcessedReservas.addSource(mSortType, sort -> updateProcessedList(source.getValue(), sort, mSortDirection.getValue(), mFilterType.getValue()));
        // Reaccionar a cambios en la dirección del orden
        mProcessedReservas.addSource(mSortDirection, dir -> updateProcessedList(source.getValue(), mSortType.getValue(), dir, mFilterType.getValue()));
        // Reaccionar a cambios en el filtro
        mProcessedReservas.addSource(mFilterType, filter -> updateProcessedList(source.getValue(), mSortType.getValue(), mSortDirection.getValue(), filter));
    }

    /** Helper para disparar la actualización de la lista procesada. */
    private void updateProcessedList(List<ReservaConQuads> data, SortType sort, SortDirection dir, FilterType filter) {
        mProcessedReservas.setValue(processList(data, sort, dir, filter));
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
     * Aplica secuencialmente el filtrado por estado y la ordenación elegida.
     */
    private List<ReservaConQuads> processList(List<ReservaConQuads> originalList, SortType sort, SortDirection dir, FilterType filter) {
        if (originalList == null) return new ArrayList<>();

        // 1. FILTRADO
        List<ReservaConQuads> filteredList = new ArrayList<>();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (ReservaConQuads item : originalList) {
            boolean matches = false;
            String fIn = item.reserva.getFechaRecogida();
            String fOut = item.reserva.getFechaDevolucion();

            switch (filter) {
                case TODAS: matches = true; break;
                case PREVISTAS: matches = (fIn.compareTo(today) > 0); break;
                case VIGENTES:  matches = (fIn.compareTo(today) <= 0 && fOut.compareTo(today) >= 0); break;
                case CADUCADAS: matches = (fOut.compareTo(today) < 0); break;
            }
            if (matches) filteredList.add(item);
        }

        // 2. ORDENACIÓN
        Collections.sort(filteredList, (o1, o2) -> {
            int result = 0;
            switch (sort) {
                case CLIENTE:
                    result = o1.reserva.getNombreCliente().compareToIgnoreCase(o2.reserva.getNombreCliente());
                    break;
                case TELEFONO:
                    result = Integer.compare(o1.reserva.getTelefono(), o2.reserva.getTelefono());
                    break;
                case FECHA_IN:
                    result = o1.reserva.getFechaRecogida().compareTo(o2.reserva.getFechaRecogida());
                    break;
                case FECHA_OUT:
                    result = o1.reserva.getFechaDevolucion().compareTo(o2.reserva.getFechaDevolucion());
                    break;
            }
            return (dir == SortDirection.ASC) ? result : -result;
        });

        return filteredList;
    }

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