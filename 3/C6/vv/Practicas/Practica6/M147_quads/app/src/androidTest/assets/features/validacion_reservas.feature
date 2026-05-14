Feature: Validación de datos en reservas (Caja Negra)
  Como administrador del sistema de reservas
  Quiero asegurar que solo se creen reservas con datos válidos
  Para mantener la integridad de la base de datos y el servicio

  Scenario Outline: Validar campos obligatorios y formatos (Particiones de Equivalencia)
    Given que estoy en la pantalla principal de la aplicación
    When pulso el botón de añadir reserva
    And relleno los datos del cliente "<cliente>" y telefono "<telefono>"
    And confirmo la reserva
    Then debería ver un error en el campo "<campo_error>" con el mensaje "<mensaje>"

    Examples:
      | cliente         | telefono   | campo_error | mensaje                     |
      |                 | 600111222  | cliente     | El nombre es obligatorio     |
      | Juan Perez      |            | telefono    | El teléfono es obligatorio   |
      |                 |            | cliente     | El nombre es obligatorio     |
      |                 | 600111222  | cliente     | El nombre es obligatorio     |
      | Juan Perez      |            | telefono    | El teléfono es obligatorio   |

  Scenario: Validar que no se puede confirmar sin seleccionar vehículos
    Given que estoy en la pantalla principal de la aplicación
    When pulso el botón de añadir reserva
    And relleno los datos del cliente "Usuario Test" y telefono "666777888"
    And selecciono las fechas de recogida y devolución
    And confirmo la reserva
    Then debería ver un error en el botón de selección de quads con el mensaje "Debes seleccionar al menos un vehículo"

  Scenario: Validar coherencia de fechas (Devolución anterior a Recogida)
    Given que estoy en la pantalla principal de la aplicación
    When pulso el botón de añadir reserva
    And selecciono una fecha de recogida posterior a la de devolución
    And intento seleccionar quads
    Then debería ver un aviso de "La fecha de devolución debe ser posterior"
    And el botón de fecha de devolución debería mostrar el error "Fecha inválida"
