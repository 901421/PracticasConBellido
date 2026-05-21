Feature: CU-3 Modificar Información Quad
  Como administrador del sistema
  Quiero actualizar los datos de un quad existente
  Para corregir errores o reflejar cambios en la flota

  Scenario Outline: CP-V-03 Modificación de quad exitosa (Camino Feliz)
    Given que existe un quad con ID <id_quad>
    And estoy en la pantalla de edición para ese quad
    When introduzco la matrícula <matricula>
    And introduzco el precio <precio>
    And introduzco la descripción <descripcion>
    And selecciono el tipo <tipo>
    And pulso el botón guardar
    Then el quad modificado debería quedar registrado correctamente en el sistema

    Examples:
      | id_quad | matricula  | precio | descripcion | tipo      |
      | 10      | "5555-BBB" | "60.0" | "Editado"   | "Biplaza" |

  Scenario Outline: Modificación de quad fallida por errores de validación (Caminos de Error)
    Given que estoy intentando modificar el quad con ID <id_quad>
    And estoy en la pantalla de edición
    When introduzco la matrícula <matricula>
    And introduzco el precio <precio>
    And introduzco la descripción <descripcion>
    And selecciono el tipo <tipo>
    And pulso el botón guardar
    Then debería ver los errores correspondientes al caso de modificación <id_caso>

    Examples:
      | id_caso   | id_quad | matricula  | precio | descripcion | tipo        |
      | "CP-I-04" | 999     | ""         | ""     | "Edit"      | "Monoplaza" |
      | "CP-I-05" | 10      | "1-A"      | "-1.0" | "Edit"      | "Monoplaza" |
