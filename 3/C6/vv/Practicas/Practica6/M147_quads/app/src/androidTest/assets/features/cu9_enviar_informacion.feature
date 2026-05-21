Feature: CU-9 Enviar Información
  Como empleado
  Quiero enviar la información de la reserva al cliente
  Para que tenga un comprobante por SMS o WhatsApp

  Scenario Outline: Enviar información de reserva por medios válidos (Camino Feliz)
    Given que estoy visualizando los detalles de una reserva válida
    When selecciono la opción de enviar por <metodo>
    Then la aplicación <metodo> debería abrirse con los datos de la reserva precargados correspondientes al caso <id_caso>

    Examples:
      | id_caso   | metodo     |
      | "CP-V-14" | "SMS"      |
      | "CP-V-15" | "WhatsApp" |
