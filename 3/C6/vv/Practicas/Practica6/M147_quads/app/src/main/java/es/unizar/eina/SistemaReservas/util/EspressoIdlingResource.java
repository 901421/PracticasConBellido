package es.unizar.eina.SistemaReservas.util;

import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

/**
 * Clase de utilidad para gestionar los IdlingResources de Espresso.
 * Permite que los tests esperen a que las tareas asíncronas (como Room) finalicen.
 */
public class EspressoIdlingResource {

    private static final String RESOURCE = "GLOBAL";

    private static final CountingIdlingResource mCountingIdlingResource =
            new CountingIdlingResource(RESOURCE);

    public static void increment() {
        mCountingIdlingResource.increment();
    }

    public static void decrement() {
        if (!mCountingIdlingResource.isIdleNow()) {
            mCountingIdlingResource.decrement();
        }
    }

    public static IdlingResource getIdlingResource() {
        return mCountingIdlingResource;
    }
}