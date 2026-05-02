package es.unizar.eina.vv6f.practica3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ContadorDeLetrasTest {

    // Vector esperado para El Quijote (Caso 1)
    private static final int[] VECTOR_QUIJOTE = {
            200495, 24147, 59436, 87237, 229189, 7581, 17225, 19920, 90075,
            10530, 0, 89141, 44658, 108441, 162514, 35465, 32483,
            100954, 125727, 61749, 79559, 17855, 2, 377, 25115, 6491, 4241
    };

    @TempDir
    Path directorioTemporal;

    // CASO 1: Fichero existente y muy grande (VLS)
    @Test
    void caso01_frecuencias_ficheroValidoYGrande_devuelveVectorCorrecto() throws FileNotFoundException {
        File ficheroQuijote = java.nio.file.Path.of("src", "main", "res", "quijote.txt").toFile();

        ContadorDeLetras contador = new ContadorDeLetras(ficheroQuijote);
        assertArrayEquals(VECTOR_QUIJOTE, contador.frecuencias());
    }

    // CASO 2: Fichero que no existe
    @Test
    void caso02_frecuencias_ficheroNoExiste_lanzaFileNotFoundException() {
        File ficheroFalso = new File("fichero_que_no_existe.txt");

        ContadorDeLetras contador = new ContadorDeLetras(ficheroFalso);
        assertThrows(FileNotFoundException.class, contador::frecuencias);
    }

    // CASO 3: Ruta a un directorio en lugar de un fichero
    @Test
    void caso03_frecuencias_rutaEsDirectorio_lanzaFileNotFoundException() {
        File directorio = Path.of("src", "main", "res").toFile();
        ContadorDeLetras contador = new ContadorDeLetras(directorio);
        assertThrows(FileNotFoundException.class, contador::frecuencias);
    }

    // CASO 4: Referencia nula al construir
    @Test
    void caso04_frecuencias_ficheroNulo_lanzaNullPointerException() {
        ContadorDeLetras contador = new ContadorDeLetras(null);
        assertThrows(NullPointerException.class, contador::frecuencias);
    }

    // CASO 5: Fichero vacío (VLI)
    @Test
    void caso05_frecuencias_ficheroVacio_devuelveVectorCeros() throws IOException {
        File vacioTxt = Path.of("src", "test", "res", "vacio.txt").toFile();
        ContadorDeLetras contador = new ContadorDeLetras(vacioTxt);

        int[] esperado = new int[ContadorDeLetras.TAMANO_ALFABETO]; // Todo a 0
        assertArrayEquals(esperado, contador.frecuencias());
    }

    // CASO 6: Caracteres con diacríticos, cedilla y eñe
    @Test
    void caso06_frecuencias_caracteresDiacriticos() throws IOException {
        File diacriticosTxt = Path.of("src", "test", "res", "diacriticos.txt").toFile();
        ContadorDeLetras contador = new ContadorDeLetras(diacriticosTxt);
        int[] resultado = contador.frecuencias();

        int[] esperado = new int[ContadorDeLetras.TAMANO_ALFABETO];
        esperado[0] = 5;  // A
        esperado[4] = 5;  // E
        esperado[8] = 5;  // I
        esperado[14] = 5; // O
        esperado[20] = 5; // U
        esperado[2] = 1;  // C (ç)
        esperado[ContadorDeLetras.INDICE_ENYE] = 1; // Ñ

        assertArrayEquals(esperado, resultado, "El vector no coincide con los valores esperados para diacríticos, cedilla y ñ.");
    }

    // CASO 7: Caracteres no válidos ignorados
    @Test
    void caso07_frecuencias_caracteresIgnorados_devuelveVectorCeros() throws IOException {
        File ignoradosTxt = Path.of("src", "test", "res", "ignorados.txt").toFile();
        ContadorDeLetras contador = new ContadorDeLetras(ignoradosTxt);

        int[] esperado = new int[ContadorDeLetras.TAMANO_ALFABETO];
        assertArrayEquals(esperado, contador.frecuencias());
    }

    // CASO 8: Invocaciones sucesivas no recalculan (mismo objeto que el caso 7)
    @Test
    void caso08_frecuencias_invocacionSucesiva_devuelveMismoVector() throws IOException {
        File ignoradosTxt = Path.of("src", "test", "res", "ignorados.txt").toFile();
        ContadorDeLetras contador = new ContadorDeLetras(ignoradosTxt);

        int[] primeraInvocacion = contador.frecuencias();
        int[] segundaInvocacion = contador.frecuencias();

        assertSame(primeraInvocacion, segundaInvocacion, "Debería devolver exactamente la misma referencia en memoria sin recalcular.");
    }

    // CASO 9: Un solo carácter (VLI + 1)
    @Test
    void caso09_frecuencias_unCaracter_cuentaSoloEseCaracter() throws IOException {
        File unCaracterTxt = Path.of("src", "test", "res", "un_caracter.txt").toFile();
        ContadorDeLetras contador = new ContadorDeLetras(unCaracterTxt);
        int[] resultado = contador.frecuencias();

        int[] esperado = new int[ContadorDeLetras.TAMANO_ALFABETO];
        esperado[0] = 1; // 1 sola letra 'A'

        assertArrayEquals(esperado, resultado);
    }

    // CASO 10: Fronteras del alfabeto
    @Test
    void caso10_frecuencias_fronterasAlfabeto() throws IOException {
        File fronterasTxt = Path.of("src", "test", "res", "fronteras.txt").toFile();
        ContadorDeLetras contador = new ContadorDeLetras(fronterasTxt);
        int[] resultado = contador.frecuencias();

        int[] esperado = new int[ContadorDeLetras.TAMANO_ALFABETO];
        esperado[0] = 2;  // A y a
        esperado[25] = 2; // Z y z

        assertArrayEquals(esperado, resultado);
    }
}