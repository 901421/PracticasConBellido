Feature: CU-8 Eliminar Reserva
  Como empleado
  Quiero dar de baja una reserva del sistema
  Para gestionar cancelaciones

  Scenario Outline: CP-V-13 Eliminar una reserva existente (Camino Feliz)
    Given que existe una reserva con ID <id_reserva> en el sistema
    When accedo al listado de reservas
    And selecciono la reserva con ID <id_reserva>
    And pulso el botón eliminar reserva
    Then la reserva debería estar marcada como inactiva con éxito

    Examples:
      | id_reserva |
      | 500        |

  Scenario Outline: CP-I-11 Intentar eliminar una reserva inexistente (Camino de Error)
    Given que no existe una reserva con ID <id_reserva> en el sistema
    When intento realizar la operación de borrado sobre la reserva ID <id_reserva>
    Then debería ver un mensaje indicando que la reserva no ha sido encontrada

    Examples:
      | id_reserva |
      | 99999      |
