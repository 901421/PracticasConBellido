import java.lang.reflect.Method;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementación del servidor Broker basado en Java RMI.
 * Actúa como intermediario gestionando el registro de servidores y la 
 * invocación dinámica de servicios, soportando operaciones síncronas y asíncronas.
 */
public class BrokerImpl extends UnicastRemoteObject implements Broker {
    
    // Registro maestro de servicios disponibles con soporte para concurrencia
    private List<InfoServicio> registroMaestroServicios;
    
    // Mapeo de nombres de servidores a sus respectivas direcciones (IP:puerto)
    private Map<String, String> localizacionServidores;

    // Estructura de datos para gestionar peticiones asíncronas
    // K1: Dirección IP del cliente, K2: Nombre del servicio, V: Respuesta asociada
    private Map<String, Map<String, Respuesta>> peticionesAsincronas;

    // Pool de hilos para procesar peticiones asíncronas
    private transient ExecutorService poolAsincrono;

    public BrokerImpl() throws RemoteException {
        super();
        this.registroMaestroServicios = new CopyOnWriteArrayList<>();
        this.localizacionServidores = new ConcurrentHashMap<>();
        this.peticionesAsincronas = new ConcurrentHashMap<>();

        // Inicialización del pool de hilos con capacidad fija
        this.poolAsincrono = Executors.newFixedThreadPool(10);

        // Configuración del hook de apagado para liberar recursos adecuadamente
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[BROKER] Iniciando secuencia de apagado. Deteniendo pool asíncrono...");
            poolAsincrono.shutdown();
        }));
    }

    // ==========================================================
    // API DE SERVIDORES
    // ==========================================================
    
    @Override
    public void registrar_servidor(String nombre_servidor, String host_remoto_IP_puerto) throws RemoteException {
        if (nombre_servidor == null || nombre_servidor.trim().isEmpty()) {
            throw new RemoteException("Error de validación: El nombre del servidor no puede ser nulo o vacío.");
        }

        boolean actualizacion = localizacionServidores.containsKey(nombre_servidor);
        localizacionServidores.put(nombre_servidor, host_remoto_IP_puerto); // Actualiza o inserta la ubicación del servidor
        
        if (actualizacion) {
            System.out.println("[BROKER] Ubicación de servidor actualizada: " + nombre_servidor + " -> " + host_remoto_IP_puerto);
        } else {
            System.out.println("[BROKER] Servidor registrado: " + nombre_servidor + " en " + host_remoto_IP_puerto);
        }
    }

    @Override
    public void alta_servicio(String nombre_servidor, String nom_servicio, List<Object> lista_param, String tipo_retorno) throws RemoteException {
        // Verificación de dependencia: El servidor debe existir en el registro
        if (!localizacionServidores.containsKey(nombre_servidor)) {
            throw new RemoteException("Error de integridad: El servidor '" + nombre_servidor + "' no está registrado en el Broker.");
        }

        // Verificación de duplicidad: Garantizar unicidad del nombre del servicio en todo el sistema
        boolean servicioDuplicado = registroMaestroServicios.stream().anyMatch(s -> s.getNombreServicio().equals(nom_servicio));
            
        if (servicioDuplicado) {
            throw new RemoteException("Error de conflicto: El servicio '" + nom_servicio + "' ya se encuentra registrado.");
        }

        InfoServicio nuevoServicio = new InfoServicio(nombre_servidor, nom_servicio, lista_param, tipo_retorno);
        registroMaestroServicios.add(nuevoServicio);
        System.out.println("[BROKER] Servicio dado de alta: " + nom_servicio + " (" + nombre_servidor + ")");
    }

    @Override
    public void baja_servicio(String nombre_servidor, String nom_servicio) throws RemoteException {
        // removeIf devuelve true si al menos un elemento fue eliminado
        boolean eliminado = registroMaestroServicios.removeIf(
            s -> s.getNombreServidor().equals(nombre_servidor) && s.getNombreServicio().equals(nom_servicio)
        );
        
        if (eliminado) {
            System.out.println("[BROKER] Servicio dado de baja: " + nom_servicio + " (" + nombre_servidor + ")");
        } else {
            System.out.println("[BROKER-WARN] Intento de baja fallido para el servicio: " + nom_servicio);
            throw new RemoteException("Error de operación: El servicio no existe o no pertenece al servidor especificado.");
        }
    }

    // ==========================================================
    // API DE CLIENTES
    // ==========================================================

    @Override
    public Servicios lista_servicios() throws RemoteException {
        return new Servicios(this.registroMaestroServicios);
    }

    @Override
    public Respuesta ejecutar_servicio(String nom_servicio, List<Object> parametros) throws RemoteException {
        System.out.println("[BROKER] Procesando petición síncrona para: " + nom_servicio);
        
        try {
            // 1. Búsqueda del servicio utilizando Stream API
            InfoServicio servicioEncontrado = registroMaestroServicios.stream()
                .filter(s -> s.getNombreServicio().equals(nom_servicio))
                .findFirst()
                .orElse(null);

            if (servicioEncontrado == null) {
                return Respuesta.error("Error: Servicio '" + nom_servicio + "' no encontrado en el catálogo.");
            }

            // 2. Resolución de ubicación y validación de ruteo
            String nombreServidor = servicioEncontrado.getNombreServidor();
            String ipPuerto = localizacionServidores.get(nombreServidor);
            
            if (ipPuerto == null) {
                return Respuesta.error("Error de enrutamiento: No se localizó la dirección (IP/Puerto) para el servidor '" + nombreServidor + "'.");
            }
            
            // 3. Resolución de la referencia RMI
            Remote servidorRemoto = Naming.lookup("//" + ipPuerto + "/" + nombreServidor);

            // 4. Preparación de parámetros para Reflexión
            Class<?>[] clasesParametros = new Class<?>[parametros.size()];
            for (int i = 0; i < parametros.size(); i++) {
                Object p = parametros.get(i);
                clasesParametros[i] = (p != null) ? p.getClass() : Object.class;
            }

            Method metodo = servidorRemoto.getClass().getMethod(nom_servicio, clasesParametros);

            // 5. Invocación dinámica
            Object resultadoBruto = metodo.invoke(servidorRemoto, parametros.toArray());

            // 6. Retorno exitoso
            return Respuesta.exito(resultadoBruto);

        } catch (NoSuchMethodException e) {
            return Respuesta.error("Error de contrato: Firma de método no coincidente. Verifique el uso de clases Wrapper en la interfaz.");
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable causaReal = e.getTargetException();
            return Respuesta.error("Error en la ejecución del servicio remoto: " + causaReal.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Respuesta.error("Error interno crítico en el Broker: " + e.toString());
        }
    }

    // ==========================================================
    // GESTIÓN DE EJECUCIÓN ASÍNCRONA
    // ==========================================================

    @Override
    public void ejecutar_servicio_asinc(String nom_servicio, List<Object> parametros) throws RemoteException {
        String ipCliente;
        try {
            ipCliente = java.rmi.server.RemoteServer.getClientHost();
        } catch (java.rmi.server.ServerNotActiveException e) {
            throw new RemoteException("Error: No se pudo identificar el host de origen del cliente.");
        }

        Map<String, Respuesta> mapaCliente = peticionesAsincronas.computeIfAbsent(ipCliente, k -> new ConcurrentHashMap<>());

        // Operación atómica: Previene condiciones de carrera si el cliente envía peticiones simultáneas idénticas
        Respuesta estadoPrevio = mapaCliente.putIfAbsent(nom_servicio, Respuesta.enProceso());
        
        if (estadoPrevio != null) {
            throw new RemoteException("Error de concurrencia: Existe una petición pendiente para el servicio '" + nom_servicio + "'. Debe recuperar la respuesta previa.");
        }

        System.out.println("[BROKER] Procesando petición asíncrona de " + ipCliente + " para el servicio: " + nom_servicio);

        // Delegación de carga al pool de hilos
        poolAsincrono.submit(() -> {
            try {
                Respuesta res = ejecutar_servicio(nom_servicio, parametros);
                mapaCliente.put(nom_servicio, res);
                System.out.println("[BROKER] Tarea asíncrona finalizada (" + nom_servicio + ") para cliente " + ipCliente);
            } catch (Exception e) {
                System.err.println("[BROKER-ERROR] Excepción en hilo asíncrono: " + e.getMessage());
                mapaCliente.put(nom_servicio, Respuesta.error("Excepción en la ejecución diferida: " + e.getMessage()));
            }
        });
    }

    @Override
    public Respuesta obtener_respuesta_asinc(String nom_servicio) throws RemoteException {
        String ipCliente;
        try {
            ipCliente = java.rmi.server.RemoteServer.getClientHost();
        } catch (java.rmi.server.ServerNotActiveException e) {
            return Respuesta.error("Error de autenticación: Host no identificable.");
        }
        
        Map<String, Respuesta> mapaCliente = peticionesAsincronas.get(ipCliente);

        if (mapaCliente == null || !mapaCliente.containsKey(nom_servicio)) {
            return Respuesta.error("Error de estado: No se localizó una petición activa para el servicio '" + nom_servicio + "' asociada a su host.");
        }

        Respuesta res = mapaCliente.get(nom_servicio);

        if (res != null && res.isProcesando()) {
            return Respuesta.enProceso();
        }

        // Limpieza de estado transaccional tras entrega
        mapaCliente.remove(nom_servicio);
        
        // Prevención de fuga de memoria: Eliminación del mapa del cliente si ya no hay operaciones pendientes
        if (mapaCliente.isEmpty()) {
            peticionesAsincronas.remove(ipCliente);
        }
        
        return res;
    }
    
    // ==========================================================
    // PUNTO DE ENTRADA PRINCIPAL
    // ==========================================================
    public static void main(String args[]) {
        try {
            // 1. Configuración y validación de parámetros de red
            String ipBroker = (args.length > 0) ? args[0] : "127.0.0.1";
            int puertoBroker;
            
            try {
                puertoBroker = (args.length > 1) ? Integer.parseInt(args[1]) : 32000;
            } catch (NumberFormatException e) {
                System.err.println("[ERROR CRÍTICO] El puerto proporcionado debe ser un valor numérico válido.");
                System.exit(1);
                return;
            }
            // 2. Configuración explícita del hostname RMI
            // Esto garantiza que el stub RMI contenga la IP pública correcta y evita fallos de enrutamiento (NAT/Multi-homing)
            System.setProperty("java.rmi.server.hostname", ipBroker);
            
            // 3. Instanciación y registro del objeto remoto
            BrokerImpl implementacionBroker = new BrokerImpl();
            String uriRegistro = "//" + ipBroker + ":" + puertoBroker + "/BrokerEspacial_903080"; 
            
            Naming.rebind(uriRegistro, implementacionBroker);
            
            System.out.println("=======================================================");
            System.out.println("[INFO] Instancia del Broker desplegada y operativa.");
            System.out.println("[INFO] Interfaz de red vinculada: " + ipBroker);
            System.out.println("[INFO] URI de registro RMI: " + uriRegistro);
            System.out.println("=======================================================");
            
        } catch (java.net.MalformedURLException e) {
            System.err.println("[ERROR CRÍTICO] La URI construida para el registro RMI no es válida.");
            e.printStackTrace();
            System.exit(1);
        } catch (java.rmi.ConnectException e) {
            System.err.println("[ERROR CRÍTICO] No se pudo contactar con el rmiregistry en la dirección especificada.");
            System.err.println("Asegúrese de que el registro RMI está en ejecución en el puerto asignado.");
            System.exit(1);
        } catch (Exception ex) {
            System.err.println("[ERROR CRÍTICO] Excepción general durante el arranque del subsistema Broker.");
            ex.printStackTrace();
            System.exit(1);
        }
    }
}