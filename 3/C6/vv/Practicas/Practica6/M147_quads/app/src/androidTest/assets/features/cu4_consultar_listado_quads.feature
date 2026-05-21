Feature: CU-4 Consultar Listado Quads
  Como administrador del sistema
  Quiero ver el listado de quads
  Para conocer el estado del catálogo

  Scenario Outline: Consultar listado de quads con diferentes criterios (Camino Feliz)
    Given que estoy en la pantalla principal de quads
    When abro las opciones de ordenación
    And selecciono ordenar por <criterio>
    Then la lista mostrada debería estar ordenada por el criterio <id_caso>

    Examples:
      | id_caso   | criterio    |
      | "CP-V-04" | "Matrícula" |
      | "CP-V-05" | "Tipo"      |
      | "CP-V-06" | "Precio"    |
