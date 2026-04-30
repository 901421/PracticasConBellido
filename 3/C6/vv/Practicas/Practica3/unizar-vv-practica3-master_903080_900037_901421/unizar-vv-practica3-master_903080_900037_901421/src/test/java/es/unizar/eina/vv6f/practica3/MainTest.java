package es.unizar.eina.vv6f.practica3;

import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    private static InputStream standardIn;
    private static PrintStream standardOut;

    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeAll
    static void salvaguardarEntradaYSalidaEstandar() {
        standardIn = System.in;
        standardOut = System.out;
    }

    @AfterAll
    static void restaurarEntradaYSalidaEstandar() {
        System.setIn(standardIn);
        System.setOut(standardOut);
    }

    @BeforeEach
    void redirigirEntradaYSalidaParaCadaPrueba() {
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void limpiarRedireccion() {
        // En JUnit no es estrictamente necesario vaciarlo aquí si creamos uno nuevo en BeforeEach,
        // pero es una buena práctica de limpieza.
        outputStreamCaptor.reset();
    }

    private void simularEntradaUsuario(String entrada) {
        // En lugar de \n, usamos el separador de línea nativo del sistema operativo
        String entradaConIntro = entrada + System.lineSeparator();
        ByteArrayInputStream in = new ByteArrayInputStream(entradaConIntro.getBytes());
        System.setIn(in);
    }

    // CASO 11: Ruta a un fichero existente
    @Test
    void caso11_main_ficheroValido_imprimeFrecuencias() throws java.io.IOException {
        String rutaQuijote = java.nio.file.Path.of("src", "main", "res", "quijote.txt").toString();
        simularEntradaUsuario(rutaQuijote);

        Main.main(new String[]{});

        String salidaTerminal = outputStreamCaptor.toString();

        java.nio.file.Path rutaSalidaEsperada = java.nio.file.Path.of("src", "test", "res", "salida-quijote.txt");
        String salidaEsperada = java.nio.file.Files.readString(rutaSalidaEsperada, java.nio.charset.StandardCharsets.UTF_8);

        // 1. Quitamos el BOM (Byte Order Mark) de UTF-8 si el fichero lo tuviera
        salidaEsperada = salidaEsperada.replace("\uFEFF", "");

        // 2. Unificamos todos los saltos de línea al formato estándar \n
        salidaTerminal = salidaTerminal.replace("\r\n", "\n");
        salidaEsperada = salidaEsperada.replace("\r\n", "\n");

        // 3. Simulamos el "Intro" que JUnit no pinta en la terminal
        salidaTerminal = salidaTerminal.replace("Nombre de un fichero de texto: A:", "Nombre de un fichero de texto: \nA:");

        // 4. Eliminamos espacios en blanco/tabulaciones que se hayan colado justo antes de un salto de línea
        salidaTerminal = salidaTerminal.replaceAll("[ \\t]+\\n", "\n");
        salidaEsperada = salidaEsperada.replaceAll("[ \\t]+\\n", "\n");

        // 5. El .trim() final elimina cualquier línea en blanco suelta al principio o final del documento
        org.junit.jupiter.api.Assertions.assertEquals(salidaEsperada.trim(), salidaTerminal.trim(),
                "La salida del programa no coincide exactamente con el fichero salida-quijote.txt");
    }

    // CASO 12: Ruta a un fichero que no existe
    @Test
    void caso12_main_ficheroNoExiste_imprimeMensajeError() {
        simularEntradaUsuario("ruta_inventada_falsa.txt");

        Main.main(new String[]{});

        String salidaTerminal = outputStreamCaptor.toString();
        assertTrue(salidaTerminal.contains("El fichero 'ruta_inventada_falsa.txt' no existe."));
    }

    // CASO 13: Ruta a un directorio
    @Test
    void caso13_main_rutaEsDirectorio_imprimeMensajeError() {
        String rutaDirectorio = java.nio.file.Path.of("src", "main", "res").toString();
        simularEntradaUsuario(rutaDirectorio);

        Main.main(new String[]{});

        String salidaTerminal = outputStreamCaptor.toString();
        // Usamos String.format para inyectar la ruta correcta generada por el SO
        String mensajeEsperado = String.format("El fichero '%s' no existe.", rutaDirectorio);
        assertTrue(salidaTerminal.contains(mensajeEsperado));
    }

    // CASO 14: Entrada vacía
    @Test
    void caso14_main_entradaVacia_imprimeMensajeError() {
        simularEntradaUsuario("");

        Main.main(new String[]{});

        String salidaTerminal = outputStreamCaptor.toString();
        assertTrue(salidaTerminal.contains("El fichero '' no existe."));
    }
}