Feature: CU-1 Crear Quad
  Como administrador del sistema
  Quiero registrar nuevos quads en el catálogo
  Para poder ofrecerlos en alquiler

  Scenario Outline: CP-V-01 Creación de quad exitosa (Camino Feliz)
    Given que estoy en la pantalla de edición de quad
    When introduzco la matrícula <matricula>
    And introduzco el precio <precio>
    And introduzco la descripción <descripcion>
    And selecciono el tipo <tipo>
    And pulso el botón guardar
    Then el quad debería quedar registrado correctamente en el sistema

    Examples:
      | matricula  | precio | descripcion | tipo        |
      | "1234-AAA" | "50.0" | "Quad Rojo" | "Monoplaza" |

  Scenario Outline: Creación de quad fallida por errores de validación (Caminos de Error)
    Given que estoy en la pantalla de edición de quad
    When introduzco la matrícula <matricula>
    And introduzco el precio <precio>
    And introduzco la descripción <descripcion>
    And selecciono el tipo <tipo>
    And pulso el botón guardar
    Then debería ver los errores correspondientes al caso <id_caso>

    Examples:
      | id_caso   | matricula  | precio | descripcion | tipo        |
      | "CP-I-01" | ""         | ""     | "Desc"      | "Monoplaza" |
      | "CP-I-02" | "1AA1"     | "-5.0" | "Desc"      | "Biplaza"   |
