# Plan de Acción "Pasito a Pasito": Práctica 6 - Automatización de Pruebas

Este documento detalla cada acción técnica necesaria para completar la Práctica 6 sobre el proyecto `M147_quads`.

---

## Fase 1: Configuración Quirúrgica del Proyecto (1h)

### Paso 1.1: Editar `M147_quads/app/build.gradle`
Localiza el bloque `dependencies` y añade estas líneas exactamente:
```gradle
dependencies {
    // ... existentes ...

    // 1. Soporte para JUnit 4 (requerido por las reglas de Espresso)
    androidTestImplementation 'androidx.test.ext:junit:1.2.1'
    
    // 2. Espresso Core para interactuar con la UI
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.1'
    
    // 3. Espresso Intents para validar transiciones entre pantallas
    androidTestImplementation 'androidx.test.espresso:espresso-intents:3.6.1'
    
    // 4. Cucumber para Android
    androidTestImplementation 'io.cucumber:cucumber-android:7.14.0'
    
    // 5. Inyección de dependencias para los Steps de Cucumber
    androidTestImplementation 'io.cucumber:cucumber-picocontainer:7.17.0'
}
```

### Paso 1.2: Cambiar el Test Runner
En el mismo `build.gradle`, busca `testInstrumentationRunner` dentro de `defaultConfig` y cámbialo por:
```gradle
testInstrumentationRunner "io.cucumber.android.runner.CucumberAndroidJUnitRunner"
```

### Paso 1.3: Configurar `gradle.properties`
Abre `M147_quads/gradle.properties` y añade al final:
```properties
# Evita que Android desinstale la app después de cada test (ahorra mucho tiempo)
android.injected.androidTest.leaveApksInstalledAfterRun=true
```

### Paso 1.4: Desactivar Animaciones (¡CRÍTICO!)
Para evitar fallos falsos en Espresso, desactiva las animaciones en el emulador o dispositivo:
1.  Ajustes -> Opciones de desarrollador.
2.  Desactiva: **Escala de animación de ventana**, **Escala de transición-animación** y **Escala de duración de animador**.

---

## Fase 2: Automatización de Navegación con Espresso (2-3h)

### Paso 2.1: Crear la clase de test
Crea el archivo: `M147_quads/app/src/androidTest/java/es/unizar/eina/SistemaReservas/NavegacionEspressoTest.java`

### Paso 2.2: Implementar el código siguiendo tu Grafo
Basado en los nodos de tu grafo, escribe el test. Ejemplo de estructura:
1.  **Lanzar App:** Usar `@Rule public ActivityScenarioRule<ReservaListActivity> activityRule`.
2.  **Transición 1 (Nudo A -> B):** `onView(withId(R.id.add_reserva_button)).perform(click());`.
3.  **Validación:** `onView(withId(R.id.reserva_edit_layout)).check(matches(isDisplayed()));`.
4.  **Repetir:** Seguir cada flecha de tu grafo hasta completar el camino más largo.

### Paso 2.3: Validación de Intents Externos (Mock Intents)
Si tu app permite enviar confirmaciones (SMS/WhatsApp), añade un test usando `Intents.intended(...)` para verificar que se lanza el intent correcto al pulsar "Enviar".

---

## Fase 3: Infraestructura de Cucumber (1h)

### Paso 3.1: Crear carpetas de Assets
Debes crear manualmente esta ruta de directorios:
`M147_quads/app/src/androidTest/assets/features/`

### Paso 3.2: Crear el Runner de Cucumber
Crea el archivo: `M147_quads/app/src/androidTest/java/es/unizar/eina/SistemaReservas/test/RunCukesTest.java`
Contenido:
```java
package es.unizar.eina.SistemaReservas.test;

import io.cucumber.junit.CucumberOptions;

@CucumberOptions(
    features = "features",
    glue = "es.unizar.eina.SistemaReservas.test.steps",
    plugin = {"pretty"}
)
public class RunCukesTest {
    // Esta clase se deja vacía. Es solo el punto de entrada.
}
```

---

## Fase 4: Tests de Aceptación y Caja Negra (4-5h)

### Paso 4.1: Definir el escenario de "Mantenimiento de Precio"
Crea el archivo: `M147_quads/app/src/androidTest/assets/features/precios.feature`
Contenido (Gherkin):
```gherkin
Feature: Mantenimiento de precios en reservas
  
  Scenario: El precio de una reserva no debe cambiar si el precio del quad se actualiza después
    Given que existe un quad llamado "Raptor" con precio 50.0
    And hago una reserva para el quad "Raptor"
    When cambio el precio del quad "Raptor" a 75.0
    Then la reserva del quad "Raptor" debe mantener el precio de 50.0
```

### Paso 4.2: Implementar los Steps
Crea la carpeta: `M147_quads/app/src/androidTest/java/es/unizar/eina/SistemaReservas/test/steps/`
Crea el archivo: `M147_quads/app/src/androidTest/java/es/unizar/eina/SistemaReservas/test/steps/PreciosSteps.java`

Dentro de esta clase deberás:
1.  **Given:** Usar Espresso para ir a la lista de Quads, añadir uno con precio 50.
2.  **And:** Ir a la lista de Reservas, seleccionar ese Quad y guardar.
3.  **When:** Volver a la lista de Quads, editar el "Raptor", poner 75 y guardar.
4.  **Then:** Ir a la lista de Reservas, abrir la reserva realizada y verificar con un `check(matches(withText(containsString("50.0"))))` que el precio no ha subido a 75.

### Paso 4.3: Escenario de Aceptación (Scenario Testing)
Crea un archivo `M147_quads/app/src/androidTest/assets/features/reserva_flujo.feature` para cubrir un requisito de usuario representativo:
```gherkin
Feature: Gestión de reservas por el usuario
  
  Scenario: Crear una reserva nueva correctamente
    Given que estoy en la pantalla principal de reservas
    When pulso el botón de añadir reserva
    And relleno los datos de la reserva y guardo
    Then debo ver la nueva reserva en el listado principal
```
Implementa los steps correspondientes en una nueva clase `ReservaSteps.java`.

---

## Fase 5: Pruebas de Estrés y Pulido (1h)

### Paso 5.1: Monkey Test
1.  Conecta un emulador.
2.  Ejecuta en tu terminal:
    ```bash
    adb shell monkey -p es.unizar.eina.SistemaReservas --throttle 200 -v 1000
    ```
    *Nota: `--throttle 200` añade 200ms entre eventos para que no colapse la UI de golpe.*

### Paso 5.2: Verificación de IDs
Si al escribir los pasos de Espresso notas que un botón no tiene ID o es genérico, ve al archivo XML en `app/src/main/res/layout/` y añádelo:
`android:id="@+id/mi_boton_especifico"`
Luego haz un "Rebuild Project" en Android Studio.

---

## Checklist de éxito final:
- [ ] ¿Compila el proyecto con el nuevo Runner?
- [ ] ¿El test de Espresso recorre el grafo sin fallar?
- [ ] ¿Cucumber encuentra los archivos `.feature` en assets?
- [ ] ¿El test de mantenimiento de precio falla si "rompes" la lógica a propósito? (Prueba de oro).
