import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class NavegacionImpl extends UnicastRemoteObject implements Navegacion {

    public NavegacionImpl() throws RemoteException {
        super();
    }

    @Override
    public String calcular_ruta(Integer coordenadaX, Integer coordenadaY) throws RemoteException {
        System.out.println("[NAVEGACION] Calculando ruta hacia (" + coordenadaX + ", " + coordenadaY + ")...");
        try {
            // Simulación de latencia de comunicaciones (5 segundos)
            Thread.sleep(5000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restauración del estado de interrupción
            System.err.println("[NAVEGACION] Cálculo de ruta interrumpido.");
        }
        return "Ruta calculada con éxito hacia " + coordenadaX + "," + coordenadaY + ". Terreno despejado.";
    }

    @Override
    public String mover_rover(Integer metros) throws RemoteException {
        System.out.println("[NAVEGACION] Moviendo rover " + metros + " metros...");
        return "El rover ha avanzado " + metros + " metros en línea recta.";
    }

    // ==========================================================
    // PUNTO DE ENTRADA PRINCIPAL (BOOTSTRAP)
    // ==========================================================
    public static void main(String[] args) {
        try {
            // 1. Lectura de parámetros de configuración de red
            String miIP = (args.length > 0) ? args[0] : "155.210.154.192";
            String miPuerto = (args.length > 1) ? args[1] : "32000";
            
            String ipBroker = (args.length > 2) ? args[2] : "127.0.0.1";
            String puertoBroker = (args.length > 3) ? args[3] : "32000";

            // 2. Configuración explícita del hostname para evitar NAT/Multi-homing issues
            System.setProperty("java.rmi.server.hostname", miIP);

            // 3. Creación programática del registro RMI local
            try {
                LocateRegistry.createRegistry(Integer.parseInt(miPuerto));
                System.out.println("[INFO] Registro RMI local inicializado en el puerto " + miPuerto);
            } catch (RemoteException e) {
                System.out.println("[WARN] El registro RMI ya se encontraba activo en el puerto " + miPuerto);
            }

            // 4. Instanciación y registro de este servidor RMI
            NavegacionImpl miNavegacion = new NavegacionImpl();
            String miNombreRegistro = "ServidorNavegacion";
            String miURL = "//" + miIP + ":" + miPuerto + "/" + miNombreRegistro;
            
            Naming.rebind(miURL, miNavegacion);
            System.out.println("[OK] Servidor de Navegación ONLINE exportado en: " + miURL);

            // 5. Resolución de referencia y conexión al Broker
            String urlBroker = "//" + ipBroker + ":" + puertoBroker + "/BrokerEspacial_903080";
            Broker broker = (Broker) Naming.lookup(urlBroker);

            // 6. Registro de infraestructura y alta de catálogo de servicios
            broker.registrar_servidor(miNombreRegistro, miIP + ":" + miPuerto);                    

            broker.alta_servicio(miNombreRegistro, "calcular_ruta", Arrays.asList("Integer", "Integer"), "String");
            broker.alta_servicio(miNombreRegistro, "mover_rover", Arrays.asList("Integer"), "String");

            System.out.println("[OK] Servicios de navegación dados de alta exitosamente en el Broker.");

        } catch (NumberFormatException e) {
            System.err.println("[ERROR CRÍTICO] El puerto proporcionado debe ser un valor numérico entero.");
            System.exit(1);
        } catch (java.rmi.ConnectException e) {
            System.err.println("[ERROR CRÍTICO] Imposible contactar con el Broker en la dirección especificada.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("[ERROR CRÍTICO] Excepción general en la inicialización del Servidor de Navegación.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}