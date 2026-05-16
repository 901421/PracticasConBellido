Feature: CU-3 Modificar Información Quad
  Como administrador del sistema
  Quiero actualizar los datos de un quad existente
  Para corregir errores o reflejar cambios en la flota

  Scenario Outline: Validaciones positivas de modificación de quad (Aislamiento)
    Given que existe un quad con ID <id_quad_base>
    And estoy en la pantalla de edición para ese quad
    When modifico la matrícula a <matricula>
    And modifico el precio a <precio>
    And modifico la descripción a <descripcion>
    And modifico el tipo a <tipo>
    And pulso el botón guardar
    Then no debería ver error en el campo "<campo_ok>"
    And debería ver error en el campo incorrecto "<campo_error>"

    Examples:
      | id_quad_base | matricula  | precio | descripcion | tipo         | campo_ok   | campo_error |
      | 10           | "1-A"      | -1     | "Edit"      | "Biplaza"    | ID Quad    | matrícula   |
      | 999          | "1234-BBB" | -1     | "Edit"      | "Monoplaza"  | matrícula  | ID Quad     |
      | 999          | "1-A"      | 60.0   | "Edit"      | "Monoplaza"  | precio     | ID Quad     |

  Scenario: Modificación fallida por múltiples errores (Combinada)
    Given que no existe el quad con ID 999
    When intento modificar el quad 999 con matrícula "" y precio "abc"
    Then debería ver errores de "ID inexistente, matrícula vacía y precio no numérico"
