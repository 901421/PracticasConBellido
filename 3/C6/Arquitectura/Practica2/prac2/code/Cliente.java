import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Cliente {

    // ==========================================================
    // CONFIGURACIÓN DE RED 
    // ==========================================================
    private static final String IP_BROKER = "127.0.0.1";
    private static final String PUERTO_BROKER = "32000";
    private static final String NOMBRE_BROKER = "BrokerEspacial_903080";

    public static void main(String[] args) {
        // Leemos la IP de los argumentos (ej: java Cliente 155.210.154.200)
        // Si no se le pasa nada, usamos la IP por defecto de la clase.
        String ipAUsar = (args.length > 0) ? args[0] : IP_BROKER;
        String puertoAUsar = (args.length > 1) ? args[1] : PUERTO_BROKER;

        Scanner scanner = new Scanner(System.in);

        try {
            //Conectarnos al Broker
            String urlBroker = "//" + ipAUsar + ":" + puertoAUsar + "/" + NOMBRE_BROKER;
            Broker broker = (Broker) Naming.lookup(urlBroker);
            System.out.println("[OK] Conectado al Broker en: " + urlBroker);
            System.out.println("--------------------------------------------------");

            // ==========================================================
            // SELECTOR DE MODO DE EJECUCIÓN
            // Comenta y descomenta para cambiar entre Menú y Pruebas
            // ==========================================================

            System.out.println("Selecciona modo de ejecucion (1: Menú interactivo, 2: Pruebas automáticas)");
            String modo = scanner.nextLine();

            
                if ("1".equals(modo)) {
                    modoMenuInteractivo(broker, scanner);
                } else if ("2".equals(modo)) {
                    modoPruebas(broker);
                } else {
                    System.out.println("Opción no válida. Abortando misión.");
                }

        } catch (Exception e) {
            System.err.println("¡Error! No se pudo conectar al Broker en la IP: " + ipAUsar);
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }


    // ==========================================================
    // MODO 1: MENÚ INTERACTIVO
    // ==========================================================
    public static void modoMenuInteractivo(Broker broker, Scanner scanner) {
        boolean salir = false;

        System.out.println(">>> INICIANDO MODO MENÚ INTERACTIVO <<<");

        while (!salir) {
            System.out.println("\n--- MENÚ BASE ESPACIAL ---");
            System.out.println("1. Ver lista de servicios disponibles");
            System.out.println("2. Ejecutar servicio de forma SÍNCRONA (Bloqueante)");
            System.out.println("3. Ejecutar servicio de forma ASÍNCRONA (No bloqueante)");
            System.out.println("4. Recoger respuesta ASÍNCRONA");
            System.out.println("5. Salir");
            System.out.print("Elige una opción: ");
            
            String opcion = scanner.nextLine();

            try {
                switch (opcion) {
                    case "1":
                        mostrarServicios(broker);
                        break;
                    case "2":
                        ejecutarDesdeMenu(broker, scanner, false);
                        break;
                    case "3":
                        ejecutarDesdeMenu(broker, scanner, true);
                        break;
                    case "4":
                        System.out.print("Introduce el nombre del servicio que quieres recoger: ");
                        String nomServRecoger = scanner.nextLine();
                        Respuesta res = broker.obtener_respuesta_asinc(nomServRecoger);
                        imprimirRespuesta(res);
                        break;
                    case "5":
                        salir = true;
                        System.out.println("Desconectando de la base. ¡Adiós!");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (Exception e) {
                // Escarbamos en la excepción para quitar la "basura" de texto de RMI
                // y quedarnos solo con el mensaje original que mandó el Broker
                Throwable causaOriginal = e;
                while (causaOriginal.getCause() != null) {
                    causaOriginal = causaOriginal.getCause();
                }
                
                System.out.println("\nAVISO: " + causaOriginal.getMessage() + "\n");
            }
        }
    }

    // Método de apoyo para pedir los parámetros al usuario basándonos en lo que dice el Broker
    private static void ejecutarDesdeMenu(Broker broker, Scanner scanner, boolean asincrono) throws Exception {
        System.out.print("Introduce el nombre del servicio a ejecutar: ");
        String nomServicio = scanner.nextLine();

        // Buscamos el servicio en el catálogo para saber qué parámetros pide
        Servicios catalogo = broker.lista_servicios();
        InfoServicio servicioInfo = null;
        for (InfoServicio s : catalogo.getListaServicios()) {
            if (s.getNombreServicio().equals(nomServicio)) {
                servicioInfo = s;
                break;
            }
        }

        if (servicioInfo == null) {
            System.out.println("Error: El servicio no existe en el catálogo.");
            return;
        }

        // Rellenamos los parámetros preguntando al usuario
        List<Object> parametros = new ArrayList<>();
        List<Object> tiposEsperados = servicioInfo.getListaParametros();
        
        System.out.println("Este servicio requiere " + tiposEsperados.size() + " parámetros.");
        for (int i = 0; i < tiposEsperados.size(); i++) {
            String tipo = (String) tiposEsperados.get(i);
            System.out.print("Introduce valor para el parámetro " + (i+1) + " (Tipo " + tipo + "): ");
            String valorString = scanner.nextLine();
            
            // Convertimos el String al tipo que espera el servidor
            if (tipo.equals("Integer")) {
                try {
                    // Intentamos convertir el texto a número
                    parametros.add(Integer.parseInt(valorString));
                } catch (NumberFormatException e) {
                    // Si el usuario mete letras en vez de números, lanzamos una excepción 
                    throw new Exception("Formato incorrecto. Se esperaba un número entero pero escribiste: '" + valorString + "'.");
                }
            } else {
                parametros.add(valorString); // Por defecto asumimos String
            }
        }

        System.out.println("Enviando petición...");
        if (asincrono) {
            broker.ejecutar_servicio_asinc(nomServicio, parametros);
            System.out.println("¡Petición asíncrona enviada! Sigue usando el menú mientras el Rover trabaja.");
        } else {
            Respuesta res = broker.ejecutar_servicio(nomServicio, parametros);
            imprimirRespuesta(res);
        }
    }

    // ==========================================================
    // MODO 2: PRUEBAS 
    // ==========================================================
    public static void modoPruebas(Broker broker) {
        System.out.println(">>> INICIANDO MODO PRUEBAS AUTOMÁTICAS <<<");
        try {
            // Prueba 1: Mostrar el catálogo
            System.out.println("\n[PRUEBA 1] Solicitando catálogo...");
            mostrarServicios(broker);

            // Prueba 2: Ejecución Síncrona
            System.out.println("\n[PRUEBA 2] Ejecutando 'mover_rover(150)' SÍNCRONO...");
            List<Object> paramsMover = new ArrayList<>();
            paramsMover.add(150); // Metros a mover
            Respuesta resSync = broker.ejecutar_servicio("mover_rover", paramsMover);
            imprimirRespuesta(resSync);

            // Prueba 3: Ejecución Asíncrona (El análisis tarda 8 segundos)
            System.out.println("\n[PRUEBA 3] Ejecutando 'analizar_muestra(Basalto)' ASÍNCRONO...");
            List<Object> paramsRoca = new ArrayList<>();
            paramsRoca.add("Basalto Marciano");
            broker.ejecutar_servicio_asinc("analizar_muestra", paramsRoca);
            System.out.println("Petición enviada. No nos hemos bloqueado.");

            // Prueba 4: Trampa del cliente (Intentar ejecutar asíncrono otra vez sin recoger)
            System.out.println("\n[PRUEBA 4] Intentando ejecutar de nuevo sin haber recogido (Debe dar error)...");
            try {
                broker.ejecutar_servicio_asinc("analizar_muestra", paramsRoca);
            } catch (Exception e) {
                System.out.println("--> ERROR CAPTURADO CORRECTAMENTE: " + e.getMessage());
            }

            // Prueba 5: Preguntar por la respuesta inmediatamente (Estará procesando)
            System.out.println("\n[PRUEBA 5] Recogiendo respuesta rápido");
            Respuesta resRapida = broker.obtener_respuesta_asinc("analizar_muestra");
            imprimirRespuesta(resRapida);

            // Prueba 6: Esperar a que acabe el servidor y recoger
            System.out.println("\n[PRUEBA 6] Esperando 9 segundos a que el rover termine...");
            Thread.sleep(9000); // Simulamos que el cliente hace otras cosas
            
            System.out.println("Recogiendo respuesta final...");
            Respuesta resFinal = broker.obtener_respuesta_asinc("analizar_muestra");
            imprimirRespuesta(resFinal);

            // Prueba 7: Trampa del cliente (Intentar recoger algo que ya recogimos)
            System.out.println("\n[PRUEBA 7] Intentando recoger la respuesta por segunda vez (Debe dar error)...");
            Respuesta resTrampa = broker.obtener_respuesta_asinc("analizar_muestra");
            imprimirRespuesta(resTrampa);

            System.out.println("\n>>> TODAS LAS PRUEBAS COMPLETADAS CON ÉXITO <<<");

        } catch (Exception e) {
            System.err.println("Error durante las pruebas: " + e.getMessage());
        }
    }

    // ==========================================================
    // MÉTODOS DE UTILIDAD
    // ==========================================================
    
    private static void mostrarServicios(Broker broker) throws Exception {
        Servicios catalogo = broker.lista_servicios();
        System.out.println("=== CATÁLOGO DE SERVICIOS ===");
        if (catalogo.getListaServicios().isEmpty()) {
            System.out.println("No hay servidores registrados aún.");
        } else {
            for (InfoServicio s : catalogo.getListaServicios()) {
                System.out.println("- " + s.toString());
            }
        }
        System.out.println("=============================");
    }

    private static void imprimirRespuesta(Respuesta res) {
        if (res.isExito()) {
            System.out.println(" [RESULTADO]: " + res.getValorRetorno());
        } else {
            System.out.println(" [PROBLEMA]: " + res.getMensajeError());
        }
    }
}