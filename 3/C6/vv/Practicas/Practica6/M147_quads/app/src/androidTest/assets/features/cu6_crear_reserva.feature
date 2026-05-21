Feature: CU-6 Crear Reserva
  Como empleado
  Quiero registrar una nueva reserva en el sistema
  Para gestionar los alquileres de los clientes

  Scenario Outline: CP-V-11 Creación de reserva exitosa (Camino Feliz)
    Given que estoy en la pantalla de creación de reserva
    When introduzco el cliente <cliente>
    And introduzco el móvil <movil>
    And introduzco la fecha de recogida <f_in>
    And introduzco la fecha de devolución <f_out>
    And selecciono <quads> quads disponibles
    And selecciono <cascos> cascos
    And pulso el botón guardar reserva
    Then la reserva debería quedar registrada exitosamente en el sistema

    Examples:
      | cliente | movil       | f_in       | f_out      | quads | cascos |
      | "Juan"  | "600111222" | "20/05/25" | "22/05/25" | "1"   | "1"    |

  Scenario Outline: Creación de reserva fallida por errores de validación (Caminos de Error)
    Given que estoy en la pantalla de creación de reserva
    When introduzco el cliente <cliente>
    And introduzco el móvil <movil>
    And introduzco la fecha de recogida <f_in>
    And introduzco la fecha de devolución <f_out>
    And configuro la selección de quads como <quads_config>
    And selecciono <cascos> cascos
    And pulso el botón guardar reserva
    Then debería ver los errores correspondientes al caso de creación de reserva <id_caso>

    Examples:
      | id_caso   | cliente | movil | f_in       | f_out      | quads_config     | cascos |
      | "CP-I-06" | ""      | ""    | ""         | ""         | "0 quads"        | "0"    |
      | "CP-I-07" | "Ana"   | "123" | "22/05/25" | "20/05/25" | "1 quad ocupado" | "3"    |
