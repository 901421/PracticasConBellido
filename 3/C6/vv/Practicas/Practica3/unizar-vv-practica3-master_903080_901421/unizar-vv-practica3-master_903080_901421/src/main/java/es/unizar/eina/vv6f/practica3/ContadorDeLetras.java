package es.unizar.eina.vv6f.practica3;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.Normalizer;
import java.util.Scanner;

/**
 * Clase para el análisis de la frecuencia de aparición de letras del alfabeto español en un
 * fichero de texto. Los objetos de esta clase se construyen utilizando como argumento un objeto de
 * la clase File que representa el fichero de texto que se quiere analizar. La primera invocación al
 * método frecuencias() analiza el contenido del fichero de texto y, si se ha podido procesar,
 * devuelve un vector de siempre 27 componentes de tipo entero. Las primeras 26 componentes
 * almacenan el número de apariciones de las 26 letras del alfabeto inglés. La última componente
 * almacena el número de apariciones de la letra Ñ.
 *
 * No se distingue entre mayúsculas y minúsculas. En español, la letra Ñ es una letra distinta a la
 * N. El resto de apariciones de letras voladas y caracteres con diacríticos (acentos agudos,
 * graves, diéresis, cedillas), se consideran como ocurrencias de la letra correspondiente sin
 * diacríticos.
 *
 */
public class ContadorDeLetras {
    public static final int TAMANO_ALFABETO = 27;
    public static final int INDICE_ENYE = 26;

    private File fichero;
    private int[] frecuencias = null;

    /**
     * Construye un ContadorDeLetras para frecuencias la frecuencia en las que aparecen las letras
     * del fichero «fichero».
     * @param fichero
     *            fichero de texto cuyo contenido será analizado.
     */
    public ContadorDeLetras(File fichero) {
        this.fichero = fichero;
        this.frecuencias = null;
    }

    /**
     * La primera vez que este método es invocado, analiza el contenido del fichero de texto asociado a este
     * objeto en el constructor. Devuelve un vector de 27 componentes con las frecuencias
     * absolutas de aparición de cada letra del alfabeto español en el fichero.
     *
     * @return vector de 27 componentes de tipo entero. Las primeras 26 componentes almacenan el
     *         número de apariciones de las 26 letras del alfabeto inglés: la componente indexada
     *         por 0 almacena el número de apariciones de la letra A, la componente indexada por 1,
     *         el de la letra B y así sucesivamente. La última componente, almacena el número de
     *         apariciones de la letra Ñ.
     * @throws FileNotFoundException
     *             si el fichero de texto que se especificó al construir este objeto no existe o no
     *             puede abrirse.
     */


    public int[] frecuencias() throws FileNotFoundException {
        // Solo calculamos si es la primera invocación
        if (frecuencias == null) {
            // Si nos pasan un null, lanzamos la excepción correspondiente
            if (fichero == null) {
                throw new NullPointerException("La referencia al fichero no puede ser null");
            }

            frecuencias = new int[TAMANO_ALFABETO];

            // Abrimos el fichero indicando codificación UTF-8
            try (Scanner scanner = new Scanner(fichero, "UTF-8")) {
                while (scanner.hasNextLine()) {
                    // Pasamos todo a minúsculas para simplificar
                    String linea = scanner.nextLine().toLowerCase();

                    for (int i = 0; i < linea.length(); i++) {
                        char c = linea.charAt(i);

                        // Filtramos las excepciones (ñ, cedilla y voladas) ANTES de normalizar
                        if (c == 'ñ') {
                            frecuencias[INDICE_ENYE]++;
                        } else if (c == 'ç') {
                            frecuencias[2]++; // Se cuenta como 'c'
                        } else if (c == 'ª') {
                            frecuencias[0]++; // Se cuenta como 'a'
                        } else if (c == 'º') {
                            frecuencias['o' - 'a']++; // Se cuenta como 'o'
                        } else if (c >= 'a' && c <= 'z') {
                            frecuencias[c - 'a']++;
                        } else {
                            // Quitamos diacríticos (acentos, diéresis...)
                            String normalizada = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
                            char base = normalizada.charAt(0);

                            // Si tras quitar diacríticos es una letra del alfabeto...
                            if (base >= 'a' && base <= 'z') {
                                // Solo la contamos si la letra original NO tenía diacrítico (c == base)
                                // O si tenía diacrítico pero la letra base es una VOCAL (aeiou)
                                if (c == base || "aeiou".indexOf(base) != -1) {
                                    frecuencias[base - 'a']++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return frecuencias;

    }
}
