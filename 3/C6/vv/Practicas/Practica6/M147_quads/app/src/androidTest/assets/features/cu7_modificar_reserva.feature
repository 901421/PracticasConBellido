Feature: CU-7 Modificar Información Reserva
  Como administrador del sistema
  Quiero editar los datos de una reserva existente
  Para ajustarla a los cambios solicitados por el cliente

  Scenario: Mantenimiento del precio histórico al modificar otros datos (RF12)
    Given que existe una reserva con ID 1001 y un precio total de 50.0
    And el quad asociado tiene un precio actual de 45.0
    When accedo a modificar la reserva 1001
    And cambio el nombre del cliente a "Cliente VIP"
    And pulso guardar cambios
    Then el precio total de la reserva 1001 debería seguir siendo 50.0

  Scenario: Validar existencia al intentar modificar
    Given que no existe la reserva con ID 99999
    When intento acceder a la edición de la reserva 99999
    Then debería ver un mensaje de error indicando que la reserva no existe
