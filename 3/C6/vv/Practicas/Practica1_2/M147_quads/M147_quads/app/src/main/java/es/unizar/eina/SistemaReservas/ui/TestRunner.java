package es.unizar.eina.SistemaReservas.ui;

import android.app.Application;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import es.unizar.eina.SistemaReservas.database.Quad;
import es.unizar.eina.SistemaReservas.database.QuadRepository;
import es.unizar.eina.SistemaReservas.database.Reserva;
import es.unizar.eina.SistemaReservas.database.ReservaRepository;
import es.unizar.eina.SistemaReservas.database.ReservaQuad;

/**
 * Clase encargada de la ejecución técnica de las pruebas del sistema (Práctica 6).
 * 
 * Implementa la lógica para realizar pruebas unitarias de caja negra, pruebas de volumen 
 * masivo de datos y pruebas de sobrecarga de memoria. Además, proporciona métodos 
 * para la limpieza total o parcial de la base de datos Room.
 */
public class TestRunner {
    
    /** Etiqueta para los mensajes de depuración en el Logcat. */
    private static final String TAG = "TEST_SYSTEM";
    
    /** Referencia al repositorio de vehículos. */
    private QuadRepository mQuadRepo;
    
    /** Referencia al repositorio de reservas. */
    private ReservaRepository mResRepo;

    /**
     * Constructor que inicializa los repositorios necesarios para las pruebas.
     * @param app La aplicación para obtener el contexto de la base de datos.
     */
    public TestRunner(Application app) {
        mQuadRepo = new QuadRepository(app);
        mResRepo = new ReservaRepository(app);
    }

    /**
     * Ejecuta una serie de pruebas unitarias basadas en particiones de equivalencia.
     * 
     * Incluye casos de éxito y de error para la inserción de Quads (matrículas vacías, 
     * duplicadas, formatos inválidos, precios negativos) y de Reservas (nombres vacíos, 
     * fechas incoherentes, falta de vehículos asignados).
     */
    public void runUnitTests() {
        Log.d(TAG, "===== INICIANDO PRUEBAS UNITARIAS (VERSION FINAL) =====");

        // --- PARTE 1: QUADS ---
        long idQ1 = mQuadRepo.insert(new Quad("1234-BBB", true, 45.0, "Válido"));
        checkResult("UNIT QUAD 1: Válido (1234-BBB)", idQ1 > 0);

        long idQ2 = mQuadRepo.insert(new Quad("", true, 45.0, "Vacio"));
        checkResult("UNIT QUAD 2: Matrícula vacía", idQ2 > 0);

        long idQ3 = mQuadRepo.insert(new Quad("1234-BBB", false, 50.0, "Duplicado"));
        checkResult("UNIT QUAD 3: Matrícula duplicada", idQ3 > 0);

        long idQ4 = mQuadRepo.insert(new Quad("11234-BBB", true, 50.0, "Formato mal"));
        checkResult("UNIT QUAD 4: Formato matrícula inválido (5 números)", idQ4 > 0);

        long idQ5 = mQuadRepo.insert(new Quad("5555-XXX", true, -1.0, "Negativo"));
        checkResult("UNIT QUAD 5: Precio negativo", idQ5 > 0);


        // --- PARTE 2: RESERVAS (Basado en tu Tabla 4) ---
        List<ReservaQuad> listaValida = new ArrayList<>();
        if (idQ1 > 0) {
            listaValida.add(new ReservaQuad(0, (int)idQ1, 1));
        }

        Reserva r1 = new Reserva("Juan Pérez", "600111222", "10/01/2026", "12/01/2026");
        long idR1 = mResRepo.insertSync(r1, listaValida);
        checkResult("UNIT RES 1: Juan Pérez (Válida)", idR1 > 0);

        Reserva r2 = new Reserva("", "600111222", "10/01/2026", "12/01/2026");
        long idR2 = mResRepo.insertSync(r2, listaValida);
        checkResult("UNIT RES 2: Fallo por nombre vacío", idR2 > 0);

        Reserva r3 = new Reserva("Juan Pérez", "600111222", "15/01/2026", "10/01/2026");
        long idR3 = mResRepo.insertSync(r3, listaValida);
        checkResult("UNIT RES 3: Fallo por fechas incoherentes", idR3 > 0);

        Reserva r4 = new Reserva("Juan Pérez", "600111222", "10/01/2026", "12/01/2026");
        long idR4 = mResRepo.insertSync(r4, new ArrayList<>()); 
        checkResult("UNIT RES 4: Fallo por falta de vehículos", idR4 > 0);

        Reserva r5 = new Reserva("Juan Pérez", "", "10/01/2026", "12/01/2026");
        long idR5 = mResRepo.insertSync(r5, listaValida);
        checkResult("UNIT RES 5: Fallo por teléfono vacío", idR5 > 0);

        Log.d(TAG, "===== FIN PRUEBAS UNITARIAS =====");
    }

    /**
     * Ejecuta la prueba de volumen del sistema.
     * 
     * Inserta de forma automática 100 vehículos Quad y 20.000 Reservas en un hilo 
     * secundario. Para superar las validaciones de seguridad de los repositorios, 
     * genera matrículas con formato reglamentario (ej: 0001-VOL) y vincula cada 
     * reserva a al menos un vehículo existente.
     * 
     * Mide el tiempo total de ejecución y notifica el progreso cada 1.000 registros.
     */
    public void runVolumeTest() {
        Log.d(TAG, "===== INICIANDO PRUEBA DE VOLUMEN (DATOS VÁLIDOS) =====");
        
        new Thread(() -> {
            long startTime = System.currentTimeMillis();

            // 1. Insertar 100 Quads con matrícula válida (4 números - 3 letras)
            // Necesitamos guardar al menos un ID de quad para que las reservas no sean rechazadas
            List<ReservaQuad> quadsParaVinculo = new ArrayList<>();

            for (int i = 1; i <= 100; i++) {
                // Genera matrículas tipo "0001-VOL", "0002-VOL"... que cumplen el Regex
                String matriculaValida = String.format("%04d-VOL", i);
                long idGenerado = mQuadRepo.insert(new Quad(matriculaValida, true, 20.0, "Vehículo de prueba de volumen"));
                
                // Capturamos el ID del primer vehículo insertado con éxito para usarlo en las reservas
                if (idGenerado > 0 && quadsParaVinculo.isEmpty()) {
                    quadsParaVinculo.add(new ReservaQuad(0, (int)idGenerado, 1));
                }
            }
            Log.d(TAG, "SUCCESS: 100 Quads insertados cumpliendo formato Regex.");

            // 2. Insertar 20.000 Reservas
            // Si por algún motivo no hay vehículos, el repositorio rechazará las reservas (Integridad)
            if (quadsParaVinculo.isEmpty()) {
                Log.e(TAG, "ERROR: Abortando volumen. No hay quads válidos para vincular.");
                return;
            }

            for (int i = 1; i <= 20000; i++) {
                Reserva r = new Reserva("Cliente Vol " + i, "600000000", "2026-01-01", "2026-01-02");
                
                // Usamos insertSync para asegurar que la inserción termina antes de la siguiente iteración
                // Pasamos la lista con el vehículo vinculado para cumplir la regla de "reserva no vacía"
                mResRepo.insertSync(r, quadsParaVinculo); 
                
                if (i % 1000 == 0) {
                    Log.d(TAG, "Progreso: " + i + "/20000 reservas insertadas...");
                }
            }

            long endTime = System.currentTimeMillis();
            double totalSeconds = (endTime - startTime) / 1000.0;
            Log.d(TAG, "===== ÉXITO VOLUMEN: 20.100 registros en " + totalSeconds + " segundos =====");
        }).start();
    }

    /**
     * Ejecuta la prueba de sobrecarga para determinar la capacidad del campo descripción.
     * 
     * Realiza inserciones incrementales (en pasos de 1.000 caracteres) hasta forzar 
     * un error del sistema o de memoria, registrando el último tamaño estable alcanzado.
     */
    public void runStressTest() {
        Log.d(TAG, "===== INICIANDO PRUEBA DE SOBRECARGA =====");
        
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            String bloque1000 = new String(new char[1000]).replace("\0", "A");
            int iteracion = 1;
            boolean stop = false;

            try {
                while (iteracion <= 2000 && !stop) {
                    sb.append(bloque1000); 
                    int totalChars = sb.length();

                    Log.d(TAG, "Iteración " + iteracion + ": Probando con " + totalChars + " caracteres...");

                    String matriculaValida = String.format("%04d-STR", iteracion % 10000);
                    
                    Quad q = new Quad(matriculaValida, true, 10.0, sb.toString());
                    long res = mQuadRepo.insert(q);

                    if (res == -1) {
                        Log.e(TAG, "!!! LÍMITE REAL ALCANZADO !!!");
                        Log.e(TAG, "Fallo al insertar: " + totalChars + " caracteres.");
                        Log.e(TAG, "Último tamaño estable: " + (totalChars - 1000));
                        stop = true;
                    } else {
                        mQuadRepo.delete(q); 
                        iteracion++;
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "COLAPSO DE SISTEMA: " + t.getClass().getSimpleName());
                Log.e(TAG, "Tamaño en el momento del crash: " + sb.length());
            }
            Log.d(TAG, "===== FIN PRUEBA SOBRECARGA =====");
        }).start();
    }

    /**
     * Valida el resultado de una prueba específica y registra un mensaje de éxito 
     * o fallo en el log.
     * 
     * @param testName Nombre identificativo del caso de prueba.
     * @param success Booleano que indica si se ha cumplido el resultado esperado.
     */
    private void checkResult(String testName, boolean success) {
        if (success) {
            Log.d(TAG, "OK: " + testName);
        } else {
            Log.e(TAG, "FALLO: " + testName);
        }
    }

    /**
     * Solicita la eliminación de todos los registros de la tabla Quads.
     */
    public void clearQuads() {
        mQuadRepo.deleteAll();
    }

    /**
     * Solicita la eliminación de todas las reservas y sus relaciones asociadas.
     */
    public void clearReservas() {
        mResRepo.deleteAllData();
    }

    /**
     * Realiza un borrado completo de toda la información persistente en la aplicación.
     */
    public void clearAll() {
        mQuadRepo.deleteAll();
        mResRepo.deleteAllData();
    }
}