Feature: CU-9 Enviar Información
  Como administrador del sistema
  Quiero enviar los detalles de la reserva al móvil del cliente
  Para que el cliente tenga el justificante de su alquiler

  Scenario: Envío exitoso a número válido
    Given que existe una reserva para un cliente con móvil "600111222"
    When pulso en el botón enviar información de la reserva
    Then el sistema debería notificar que el mensaje ha sido enviado correctamente

  Scenario: Fallo de envío por número inválido
    Given que una reserva tiene asignado el número "123"
    When intento enviar la información de la reserva
    Then debería ver un error indicando que el teléfono no es válido para el envío
