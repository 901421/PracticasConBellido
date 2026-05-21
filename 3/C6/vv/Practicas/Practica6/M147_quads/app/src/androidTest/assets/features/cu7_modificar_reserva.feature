Feature: CU-7 Modificar Información Reserva
  Como empleado
  Quiero actualizar los datos de una reserva existente
  Para reflejar cambios solicitados por el cliente

  Scenario Outline: CP-V-12 Modificación de reserva exitosa (Camino Feliz)
    Given que existe una reserva con ID <id_res> en el sistema
    And estoy en la pantalla de edición de esa reserva
    When modifico el cliente a <cliente>
    And modifico el móvil a <movil>
    And modifico la fecha de recogida a <f_in>
    And modifico la fecha de devolución a <f_out>
    And modifico la selección de quads libres a <quads>
    And modifico los cascos a <cascos>
    And pulso el botón guardar reserva
    Then la reserva modificada debería registrarse conservando su precio histórico inalterado

    Examples:
      | id_res | cliente | movil       | f_in       | f_out      | quads | cascos |
      | 1001   | "Pedro" | "611222333" | "25/05/25" | "26/05/25" | "1"   | "2"    |

  Scenario Outline: Modificación de reserva fallida por errores de validación (Caminos de Error)
    Given que intento acceder a la reserva con ID <id_res> para modificarla
    When introduzco el cliente <cliente>
    And introduzco el móvil <movil>
    And introduzco la fecha de recogida <f_in>
    And introduzco la fecha de devolución <f_out>
    And configuro la selección de quads como <quads_config>
    And selecciono <cascos> cascos
    And pulso el botón guardar reserva
    Then debería ver los errores correspondientes al caso de modificación de reserva <id_caso>

    Examples:
      | id_caso   | id_res | cliente | movil | f_in       | f_out      | quads_config     | cascos |
      | "CP-I-08" | 99999  | "Ana"   | "123" | "25/05/25" | "26/05/25" | "1 quad libre"   | "1"    |
      | "CP-I-09" | 1001   | ""      | ""    | ""         | ""         | "0 quads"        | "0"    |
      | "CP-I-10" | 1001   | "Ana"   | "123" | "26/05/25" | "25/05/25" | "1 quad ocupado" | "3"    |
