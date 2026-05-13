Feature: Mantenimiento de precios en reservas
  Como gerente del negocio
  Quiero que el precio acordado en una reserva se mantenga inalterado
  Para evitar malentendidos con los clientes si los precios del catálogo suben

  Scenario: El precio de una reserva no debe cambiar si el precio del quad se actualiza después
    Given que estoy en la pantalla principal de la aplicación
    And que existe un quad con matricula "1234-ABC" y precio "55.0"
    And hago una reserva para el quad "1234-ABC" con cliente "Cliente Histórico" y telefono "600000000"
    When cambio el precio del quad con matricula "1234-ABC" al nuevo precio "99.0"
    Then la reserva del cliente "Cliente Histórico" debe mantener el precio de "55.0" en sus detalles
