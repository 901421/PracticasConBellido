Feature: CU-4 Consultar Listado Quads
  Como administrador del sistema
  Quiero ver la lista de todos los quads
  Para poder gestionar la flota y revisar sus precios

  Scenario Outline: Ordenar el listado de quads por diferentes criterios
    Given que existen quads registrados en el sistema
    When accedo al listado de quads
    And selecciono ordenar por "<criterio>"
    Then la lista de quads debería mostrarse ordenada según "<criterio>" de forma ascendente

    Examples:
      | criterio   |
      | Matrícula  |
      | Tipo       |
      | Precio     |
