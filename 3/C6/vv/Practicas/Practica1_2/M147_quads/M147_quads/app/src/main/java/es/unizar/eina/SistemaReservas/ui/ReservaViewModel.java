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

    /**
     * Enumeración que define los criterios de ordenación disponibles para la lista de reservas.
     */
    public enum SortType { CLIENTE, TELEFONO, FECHA_IN, FECHA_OUT }

    /** Estado observable que almacena el criterio de ordenación actual. */
    private final MutableLiveData<SortType> mSortOrder = new MutableLiveData<>(SortType.FECHA_IN);
    
    /** 
     * Flujo de datos final que combina la fuente de la base de datos con la lógica de ordenación.
     * Reacciona tanto a cambios en los datos como a cambios en el tipo de orden.
     */
    private final MediatorLiveData<List<ReservaConQuads>> mSortedReservas = new MediatorLiveData<>();

    /**
     * Constructor del ViewModel.
     * Inicializa el repositorio y configura el MediatorLiveData para observar tanto 
     * la fuente de datos original como los cambios en el criterio de ordenación.
     * 
     * @param application Contexto de la aplicación.
     */
    public ReservaViewModel(Application application) {
        super(application);
        mRepository = new ReservaRepository(application);
        
        LiveData<List<ReservaConQuads>> source = mRepository.getAllReservas();

        // Observamos cambios en la base de datos
        mSortedReservas.addSource(source, reservas -> {
            mSortedReservas.setValue(sortList(reservas, mSortOrder.getValue()));
        });

        // Observamos cambios en el botón de ordenación
        mSortedReservas.addSource(mSortOrder, sortType -> {
            List<ReservaConQuads> currentList = source.getValue();
            if (currentList != null) {
                mSortedReservas.setValue(sortList(currentList, sortType));
            }
        });
    }

    /**
     * Actualiza el tipo de ordenación de la lista de reservas.
     * Al actualizar mSortOrder, el MediatorLiveData disparará automáticamente la reordenación.
     * 
     * @param type El nuevo criterio de ordenación basado en {@link SortType}.
     */
    public void setSortType(SortType type) {
        mSortOrder.setValue(type);
    }

    /**
     * @return LiveData que emite la lista de reservas (con sus quads) ya ordenada.
     */
    public LiveData<List<ReservaConQuads>> getAllReservas() { 
        return mSortedReservas;
    }

    /**
     * Lógica interna para ordenar una lista de reservas según un criterio específico.
     * Crea una copia de la lista original para asegurar que los observadores de la UI 
     * detecten el cambio de referencia y refresquen la vista.
     * 
     * @param originalList Lista de reservas proveniente del repositorio.
     * @param type Criterio de ordenación a aplicar.
     * @return Una nueva lista con los elementos ordenados.
     */
    private List<ReservaConQuads> sortList(List<ReservaConQuads> originalList, SortType type) {
        if (originalList == null || originalList.isEmpty()) {
            return originalList;
        }

        List<ReservaConQuads> newList = new ArrayList<>(originalList);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Collections.sort(newList, new Comparator<ReservaConQuads>() {
            @Override
            public int compare(ReservaConQuads o1, ReservaConQuads o2) {
                try {
                    switch (type) {
                        case CLIENTE:
                            String c1 = o1.reserva.getNombreCliente() != null ? o1.reserva.getNombreCliente() : "";
                            String c2 = o2.reserva.getNombreCliente() != null ? o2.reserva.getNombreCliente() : "";
                            return c1.compareToIgnoreCase(c2);

                        case TELEFONO:
                            return Integer.compare(o1.reserva.getTelefono(), o2.reserva.getTelefono());

                        case FECHA_IN:
                            Date d1 = sdf.parse(o1.reserva.getFechaRecogida());
                            Date d2 = sdf.parse(o2.reserva.getFechaRecogida());
                            if (d1 == null || d2 == null) return 0;
                            return d1.compareTo(d2);

                        case FECHA_OUT:
                            Date do1 = sdf.parse(o1.reserva.getFechaDevolucion());
                            Date do2 = sdf.parse(o2.reserva.getFechaDevolucion());
                            if (do1 == null || do2 == null) return 0;
                            return do1.compareTo(do2);
                            
                        default: return 0;
                    }
                } catch (ParseException e) {
                    if (type == SortType.FECHA_IN) {
                        return o1.reserva.getFechaRecogida().compareTo(o2.reserva.getFechaRecogida());
                    }
                    return 0;
                } catch (Exception ex) {
                    return 0;
                }
            }
        });

        return newList;
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