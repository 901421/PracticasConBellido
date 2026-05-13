Feature: Gestión de reservas por el usuario
  
  Scenario: Crear una reserva nueva correctamente
    Given que estoy en la pantalla principal de la aplicación
    And que existe un quad con matricula "1111-AAA" y precio "40.0"
    When pulso el botón de añadir reserva
    And relleno los datos del cliente "Juan Perez" y telefono "611222333"
    And selecciono las fechas de recogida y devolución
    And selecciono el primer quad disponible
    And confirmo la reserva
    Then debo ver la reserva de "Juan Perez" en el listado de reservas
