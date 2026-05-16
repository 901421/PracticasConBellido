Feature: CU-10 Calcular Precio
  Como sistema de gestión
  Quiero determinar automáticamente el importe de los alquileres
  Para agilizar el proceso de reserva y evitar errores manuales

  Scenario Outline: Cálculo correcto del precio según días y quads
    Given que estoy creando una reserva
    When selecciono un rango de <dias> días
    And selecciono quads con un precio total por día de <precio_dia>
    Then el campo de precio total de la reserva debería mostrar <total_esperado>

    Examples:
      | dias | precio_dia | total_esperado |
      | 3    | 50.0       | 150.0          |
      | 1    | 70.0       | 70.0           |

  Scenario: El precio total se actualiza al sumar varios quads
    Given que tengo seleccionado un quad de 30.0
    When añado otro quad de 40.0 a la selección para 1 día
    Then el importe total mostrado debería actualizarse a 70.0
