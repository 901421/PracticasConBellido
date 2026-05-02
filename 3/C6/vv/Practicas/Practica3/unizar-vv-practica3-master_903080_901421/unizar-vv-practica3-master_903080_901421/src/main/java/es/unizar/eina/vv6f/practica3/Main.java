package es.unizar.eina.vv6f.practica3;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static es.unizar.eina.vv6f.practica3.ContadorDeLetras.INDICE_ENYE;
import static es.unizar.eina.vv6f.practica3.ContadorDeLetras.TAMANO_ALFABETO;

/**
 * Programa Java que, al iniciar su ejecución, solicita al usuario el nombre de un fichero de texto.
 * A continuación, si el fichero existe y se puede leer, muestra en la salida estándar una lista de
 * las letras del alfabeto español y el número de veces que dicha letra aparece en el fichero
 * introducido. En caso contrario, escribe en la salida estándar un mensaje de error de la forma
 * «El fichero 'f' no existe.», donde 'f' es el nombre de fichero introducido por el usuario.
 * 
 * No se distingue entre mayúsculas y minúsculas. La letra Ñ es una letra en español. El resto de
 * apariciones de letras voladas y caracteres con diacríticos (acentos agudos, graves, diéresis y
 * cedillas), se consideran como ocurrencias de la letra correspondiente sin diacríticos.
 */
public class Main {

    private static final String FORMATO_SALIDA_FRECUENCIAS = "%c: %7d%n";

    /**
     * Método que, al iniciar su ejecución, solicita al usuario el nombre de un fichero de texto.
     * A continuación, si el fichero existe y se puede leer, muestra en la salida estándar una lista
     * de las letras del alfabeto español y el número de veces que dicha letra aparece en el fichero
     * introducido. En caso contrario, escribe en la salida estándar un mensaje de error de la forma
     * «El fichero 'f' no existe.», donde 'f' es el nombre de fichero introducido por el usuario.
     *
     * No se distingue entre mayúsculas y minúsculas. La letra Ñ es una letra en español. El resto
     * de apariciones de letras voladas y caracteres con diacríticos (acentos agudos, graves,
     * diéresis y cedillas), se consideran como ocurrencias de la letra correspondiente sin
     * diacríticos.
     *
     * @param args
     *            no utilizado.
     */
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        System.out.print("Nombre de un fichero de texto: ");
        String nombreFichero = teclado.nextLine();

        File f = new File(nombreFichero);

        // Verificamos si existe y si tenemos permisos de lectura
        if (!f.exists() || !f.isFile() || !f.canRead()) {
            System.out.printf("El fichero '%s' no existe.%n", nombreFichero);
            return;
        }

        try {
            ContadorDeLetras contador = new ContadorDeLetras(f);
            int[] frecs = contador.frecuencias();

            // Imprimimos la A-N (índices 0 al 13)
            for (int i = 0; i <= 13; i++) {
                System.out.printf(FORMATO_SALIDA_FRECUENCIAS, (char) ('A' + i), frecs[i]);
            }

            // Imprimimos la Ñ (posición 26)
            System.out.printf(FORMATO_SALIDA_FRECUENCIAS, 'Ñ', frecs[INDICE_ENYE]);

            // Imprimimos la O-Z (índices 14 al 25)
            for (int i = 14; i < INDICE_ENYE; i++) {
                System.out.printf(FORMATO_SALIDA_FRECUENCIAS, (char) ('A' + i), frecs[i]);
            }

        } catch (FileNotFoundException e) {
            System.out.printf("El fichero '%s' no existe.%n", nombreFichero);
        }
    }
}
