Feature: CU-1 Crear Quad
  Como administrador del sistema
  Quiero registrar nuevos quads en el catálogo
  Para poder ofrecerlos en alquiler

  Scenario Outline: Validaciones positivas de creación de quad (Aislamiento)
    Given que estoy en la pantalla de edición de quad
    When introduzco la matrícula "<matricula>"
    And introduzco el precio "<precio>"
    And introduzco la descripción "<descripcion>"
    And selecciono el tipo "<tipo>"
    And pulso el botón guardar
    Then no debería ver un mensaje de error en el campo "<campo_ok>"
    And debería ver un mensaje de error en los campos obligatorios incorrectos

    Examples:
      | matricula  | precio | descripcion  | tipo         | campo_ok   |
      | "1234-AAA" | -5.0   | "Quad Rojo"  | "Monoplaza"  | matrícula  |
      | ""         | 50.0   | null         | "Biplaza"    | precio     |

  Scenario: Crear quad con descripción y tipo válidos (Particiones siempre válidas)
    Given que estoy en la pantalla de edición de quad
    When introduzco la matrícula "1234-BBB"
    And introduzco el precio "45.0"
    And introduzco la descripción "Info de prueba"
    And selecciono el tipo "Monoplaza"
    And pulso el botón guardar
    Then el quad debería quedar registrado correctamente en el sistema

  Scenario Outline: Validaciones negativas de creación de quad (Combinadas)
    Given que estoy en la pantalla de edición de quad
    When introduzco la matrícula <matricula>
    And introduzco el precio <precio>
    And introduzco la descripción <descripcion>
    And selecciono el tipo <tipo>
    And pulso el botón guardar
    Then debería ver los errores "<mensajes_error>"

    Examples:
      | matricula   | precio | descripcion | tipo         | mensajes_error                               |
      | ""          | ""     | ""          | "Monoplaza"  | "Matrícula obligatoria, Precio obligatorio" |
      | "1234 AAA"  | 0      | null        | "Biplaza"    | "Formato matrícula, Precio mayor que 0"     |
      | "ABC-123"   | "abc"  | "Info"      | "Monoplaza"  | "Formato matrícula, Precio numérico"        |
