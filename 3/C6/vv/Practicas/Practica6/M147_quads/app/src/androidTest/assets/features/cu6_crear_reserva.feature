Feature: CU-6 Crear Reserva
  Como administrador del sistema
  Quiero registrar una nueva reserva de quads para un cliente
  Para gestionar el alquiler de los vehículos

  Scenario Outline: Validaciones positivas de creación de reserva (Aislamiento)
    Given que estoy en la pantalla de nueva reserva
    When introduzco el nombre del cliente <cliente>
    And introduzco el móvil <movil>
    And selecciono la fecha de recogida <f_in>
    And selecciono la fecha de devolución <f_out>
    And selecciono <quads> quads con <cascos> cascos
    And pulso confirmar reserva
    Then no debería ver error en el campo "<campo_ok>"
    And debería ver errores en el resto de campos obligatorios

    Examples:
      | cliente  | movil         | f_in         | f_out        | quads    | cascos | campo_ok    |
      | "Juan"   | "123"         | null         | null         | 0        | 0      | Cliente     |
      | ""       | "600111222"   | null         | null         | 0        | 0      | Móvil       |
      | ""       | "123"         | "20-05-2025" | null         | 0        | 0      | F. Recogida |
      | ""       | "123"         | null         | "22-05-2025" | 0        | 0      | F. Devolución|
      | ""       | "123"         | "20-05-2025" | "20-05-2025" | 0        | 0      | Coherencia  |
      | ""       | "123"         | null         | null         | "1 Mono" | 1      | Selección   |

  Scenario: Creación fallida por errores combinados (Negativa)
    Given que estoy en la pantalla de nueva reserva
    When dejo todos los campos vacíos y pulso confirmar
    Then debería ver los errores de campos obligatorios para Cliente, Móvil, Fechas y Quads

  Scenario: Error por solape de fechas o exceso de cascos (Negativa)
    Given que el quad "1234-ABC" ya está reservado para el "20-05-2025"
    When intento reservar el quad "1234-ABC" para la misma fecha
    And selecciono 3 cascos para ese quad monoplaza
    Then debería ver un error de "Quad no disponible" y "Exceso de cascos"
