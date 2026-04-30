import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class InstrumentosImpl extends UnicastRemoteObject implements Instrumentos {

    public InstrumentosImpl() throws RemoteException {
        super();
    }

    @Override
    public String analizar_muestra(String tipo_roca) throws RemoteException {
        System.out.println("[INSTRUMENTOS] Iniciando análisis químico de roca tipo: " + tipo_roca);
        try {
            // Analizar rocas lleva tiempo (simulamos 8 segundos)
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción del hilo
            System.err.println("[INSTRUMENTOS] Análisis interrumpido.");
        }
        return "Análisis completado: La roca '" + tipo_roca + "' contiene altos niveles de hierro y trazas de agua helada.";
    }

    @Override
    public String tomar_foto(String camara) throws RemoteException {
        System.out.println("[INSTRUMENTOS] Tomando foto con la cámara: " + camara);
        return "¡Click! Foto panorámica capturada con éxito usando la " + camara + ".";
    }

    // ==========================================================
    // PUNTO DE ENTRADA PRINCIPAL (BOOTSTRAP)
    // ==========================================================
    public static void main(String[] args) {
        try {
            // 1. Configuración de parámetros de red
            String miIP = (args.length > 0) ? args[0] : "155.210.154.193";
            String miPuerto = (args.length > 1) ? args[1] : "32000";
            
            String ipBroker = (args.length > 2) ? args[2] : "127.0.0.1";
            String puertoBroker = (args.length > 3) ? args[3] : "32000";

            // 2. Configuración explícita del hostname RMI para evitar errores de ruteo
            System.setProperty("java.rmi.server.hostname", miIP);

            // 3. Creación programática del registro RMI local
            try {
                LocateRegistry.createRegistry(Integer.parseInt(miPuerto));
                System.out.println("[INFO] Registro RMI local inicializado en el puerto " + miPuerto);
            } catch (RemoteException e) {
                System.out.println("[WARN] El registro RMI ya estaba activo en el puerto " + miPuerto);
            }

            // 4. Instanciación y registro del objeto remoto local
            InstrumentosImpl misInstrumentos = new InstrumentosImpl();
            String miNombreRegistro = "ServidorInstrumentos";
            String miURL = "//" + miIP + ":" + miPuerto + "/" + miNombreRegistro;
            
            Naming.rebind(miURL, misInstrumentos);
            System.out.println("[OK] Servidor de Instrumentos ONLINE exportado en: " + miURL);

            // 5. Resolución de referencia y conexión al Broker
            String urlBroker = "//" + ipBroker + ":" + puertoBroker + "/BrokerEspacial_903080";
            Broker broker = (Broker) Naming.lookup(urlBroker);

            // 6. Registro de infraestructura y publicación de catálogo de servicios
            broker.registrar_servidor(miNombreRegistro, miIP + ":" + miPuerto);

            broker.alta_servicio(miNombreRegistro, "analizar_muestra", Arrays.asList("String"), "String");
            broker.alta_servicio(miNombreRegistro, "tomar_foto", Arrays.asList("String"), "String");

            System.out.println("[OK] Servicios de instrumentos dados de alta exitosamente en el Broker.");

        } catch (NumberFormatException e) {
            System.err.println("[ERROR CRÍTICO] El puerto proporcionado debe ser un valor numérico entero.");
            System.exit(1);
        } catch (java.rmi.ConnectException e) {
            System.err.println("[ERROR CRÍTICO] Imposible contactar con el Broker en la dirección especificada.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("[ERROR CRÍTICO] Excepción general en la inicialización del Servidor de Instrumentos.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}