Feature: CU-2 Eliminar Quad
  Como administrador del sistema
  Quiero dar de baja quads del sistema
  Para mantener el historial contable sin que aparezcan en operaciones nuevas

  Scenario Outline: CP-V-02 Eliminar un quad existente (Camino Feliz)
    Given que existe un quad con ID <id_quad> en el sistema
    When accedo al listado de quads
    And selecciono el quad con ID <id_quad>
    And pulso el botón eliminar
    Then el quad debería estar marcado como inactivo con éxito

    Examples:
      | id_quad |
      | 10      |

  Scenario Outline: CP-I-03 Intentar eliminar un quad inexistente (Camino de Error)
    Given que no existe un quad con ID <id_quad> en el sistema
    When intento realizar la operación de borrado sobre el ID <id_quad>
    Then debería ver un mensaje indicando que el vehículo no ha sido encontrado

    Examples:
      | id_quad |
      | 999     |
