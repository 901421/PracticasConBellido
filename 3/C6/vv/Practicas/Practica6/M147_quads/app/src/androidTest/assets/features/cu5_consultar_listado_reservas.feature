Feature: CU-5 Consultar Listado Reservas
  Como administrador del sistema
  Quiero visualizar el listado de reservas con filtros y ordenación
  Para controlar el estado de los alquileres previstos, vigentes y caducados

  Scenario Outline: Aplicar filtros de estado al listado de reservas
    Given que existen reservas de todos los estados (previstas, vigentes, caducadas)
    When accedo al listado de reservas
    And aplico el filtro de estado "<filtro>"
    Then solo deberían mostrarse las reservas que coinciden con el estado "<filtro>"

    Examples:
      | filtro      |
      | Previstas   |
      | Vigentes    |
      | Caducadas   |
      | Totales     |

  Scenario Outline: Ordenar el listado de reservas
    Given que estoy visualizando el listado de reservas
    When selecciono ordenar por "<criterio>"
    Then la lista de reservas debería mostrarse ordenada por "<criterio>"

    Examples:
      | criterio             |
      | Nombre de Cliente    |
      | Número de Móvil      |
      | Fecha de Recogida    |
      | Fecha de Devolución  |
