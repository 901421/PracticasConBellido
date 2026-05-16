Feature: CU-8 Eliminar Reserva
  Como administrador del sistema
  Quiero dar de baja una reserva
  Para liberar los quads o cancelar el servicio

  Scenario: Eliminar una reserva existente (Validación Positiva)
    Given que existe una reserva con ID 500
    When busco la reserva 500 en el listado
    And pulso el botón eliminar reserva
    Then la reserva 500 debería quedar marcada como inactiva en el sistema

  Scenario: Intentar eliminar reserva inexistente (Validación Negativa)
    Given que no existe la reserva con ID 99999
    When intento borrar la reserva 99999
    Then debería ver un aviso indicando que la reserva no existe
