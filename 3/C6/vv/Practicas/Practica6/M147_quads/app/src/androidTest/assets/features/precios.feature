Feature: Mantenimiento de precios en reservas
  
  Scenario: El precio de una reserva no debe cambiar si el precio del quad se actualiza después
    Given que existe un quad llamado "Raptor-TST" con precio "50.0"
    And hago una reserva para el quad "Raptor-TST" con cliente "Usuario Test" y telefono "600111222"
    When cambio el precio del quad "Raptor-TST" a "75.0"
    Then la reserva del cliente "Usuario Test" debe mantener el precio de "50.0" en sus detalles
