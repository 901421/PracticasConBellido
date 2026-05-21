Feature: CU-5 Consultar Listado Reservas
  Como administrador del sistema
  Quiero ver el listado de reservas con opciones de filtrado y ordenación
  Para gestionar la carga de trabajo diaria

  Scenario Outline: Consultar listado de reservas combinando filtros y ordenación (Camino Feliz)
    Given que estoy en la pantalla principal de reservas
    When selecciono el filtro de estado <filtro>
    And selecciono el criterio de ordenación <orden>
    Then la lista mostrada debería reflejar el resultado del caso <id_caso>

    Examples:
      | id_caso   | orden        | filtro      |
      | "CP-V-07" | "Cliente"    | "Totales"   |
      | "CP-V-08" | "Móvil"      | "Previstas" |
      | "CP-V-09" | "Recogida"   | "Vigentes"  |
      | "CP-V-10" | "Devolución" | "Caducadas" |
