Feature: CU-2 Eliminar Quad
  Como administrador del sistema
  Quiero dar de baja quads del sistema
  Para mantener el historial contable sin que aparezcan en operaciones nuevas

  Scenario: Eliminar un quad existente (Validación Positiva)
    Given que existe un quad con ID 10 en el sistema
    When accedo al listado de quads
    And selecciono el quad con ID 10
    And pulso el botón eliminar
    Then el quad con ID 10 debería estar marcado como inactivo
    And no debería aparecer en la selección de nuevas reservas

  Scenario: Intentar eliminar un quad inexistente (Validación Negativa)
    Given que no existe un quad con ID 999 en el sistema
    When intento realizar la operación de borrado sobre el ID 999
    Then debería ver un mensaje de error indicando que el vehículo no ha sido encontrado
