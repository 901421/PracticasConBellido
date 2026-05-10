# Validación Manual de Pares de Aristas

## RESUMEN DE LA VALIDACIÓN
Tras un análisis manual detallado del código fuente de la aplicación, se ha verificado la viabilidad técnica de cada transición generada por la estructura del grafo.

- **Total de Pares Analizados:** 391
- **Pares Posibles:** 377
- **Pares Imposibles:** 14

---

## 1. PARES POSIBLES

| ID | Pareja Aristas | Navegación Verificada | Ficheros Comprobados | Descripción Técnica |
|----|-----------------|----------------------|----------------------|---------------------|
| 1 | (1,13) | N1 -> N2 -> N2 | SistemaReservas.java, QuadListActivity.java | Desde el Menú Principal (N1) se abre el Listado de Quads (N2). Una vez allí, se activa la ordenación por matrícula (A13), lo que refresca el RecyclerView. |
| 2 | (1,13b) | N1 -> N2 -> N2 | SistemaReservas.java, QuadListActivity.java | Desde N1 se navega al listado (N2). Se pulsa el botón de ordenación por tipo (A13b), actualizando la lista mediante el ViewModel. |
| 3 | (1,13c) | N1 -> N2 -> N2 | SistemaReservas.java, QuadListActivity.java | Navegación de N1 a N2. Se activa la ordenación por precio (A13c), provocando una nueva consulta al repositorio y refresco de UI. |
| 4 | (1,14) | N1 -> N2 -> N2 | SistemaReservas.java, QuadListActivity.java | Acceso al listado desde el menú (A1). Se selecciona un quad y se pulsa eliminar (A14), disparando el diálogo de confirmación y la lógica del ViewModel. |
| 5 | (1,23) | N1 -> N2 -> N1 | SistemaReservas.java, QuadListActivity.java | Flujo de ida y vuelta: se abre el listado (A1) y se pulsa el botón de retorno (A23) que finaliza la actividad volviendo al menú. |
| 6 | (1,5) | N1 -> N2 -> N3 | SistemaReservas.java, QuadListActivity.java | Apertura del listado (A1). Se selecciona un quad para editar (A5), lo que lanza QuadEdit mediante mStartForResult. |
| 7 | (10,1) | N5 -> N1 -> N2 | ReservaEdit.java, SistemaReservas.java | Se guarda una nueva reserva volviendo al menú (A10). Desde el menú, el usuario decide ir a gestionar quads (A1). |
| 8 | (10,2) | N5 -> N1 -> N4 | ReservaEdit.java, SistemaReservas.java | Tras guardar reserva y volver a N1 (A10), se abre el listado de reservas (A2) para comprobar el registro recién creado. |
| 9 | (10,3) | N5 -> N1 -> N3 | ReservaEdit.java, SistemaReservas.java | Retorno al menú tras reserva (A10). El usuario procede a crear un nuevo quad (A3) abriendo el formulario QuadEdit. |
| 10 | (10,4) | N5 -> N1 -> N5 | ReservaEdit.java, SistemaReservas.java | Guardado de reserva (A10). Inmediatamente desde el menú se inicia la creación de otra reserva (A4). |
| 11 | (10b,1) | N5 -> N1 -> N2 | ReservaEdit.java, SistemaReservas.java | Se cancela la creación de reserva volviendo a N1 (A10b). Se procede a abrir el listado de quads (A1). |
| 12 | (10b,2) | N5 -> N1 -> N4 | ReservaEdit.java, SistemaReservas.java | Cancelación en N5 (A10b). Navegación posterior al listado de reservas (A2) desde el menú principal. |
| 13 | (10b,3) | N5 -> N1 -> N3 | ReservaEdit.java, SistemaReservas.java | Tras cancelar reserva (A10b), se decide crear un quad (A3). Flujo verificado en la gestión de Intents de N1. |
| 14 | (10b,4) | N5 -> N1 -> N5 | ReservaEdit.java, SistemaReservas.java | Se aborta reserva (A10b) y se vuelve a intentar crear una nueva (A4) abriendo de nuevo el formulario N5. |
| 15 | (10c,1) | N5 -> N1 -> N2 | ReservaEdit.java, SistemaReservas.java | Salida de N5 mediante botón atrás físico (A10c). En N1 se selecciona el listado de quads (A1). |
| 16 | (10c,2) | N5 -> N1 -> N4 | ReservaEdit.java, SistemaReservas.java | Retorno a N1 por botón atrás (A10c). El usuario abre el listado de reservas (A2). |
| 17 | (10c,3) | N5 -> N1 -> N3 | ReservaEdit.java, SistemaReservas.java | Salida de N5 (A10c) y entrada inmediata a creación de quad (A3). |
| 18 | (10c,4) | N5 -> N1 -> N5 | ReservaEdit.java, SistemaReservas.java | Abandono de formulario (A10c) y re-entrada al mismo (A4) desde el menú principal. |
| 19 | (11,12) | N5 -> N6 -> N5 | ReservaEdit.java, QuadSelectionActivity.java | En la creación de reserva se abre la selección de quads (A11). Se confirman los quads elegidos (A12) volviendo al formulario con los datos. |
| 20 | (11,12b) | N5 -> N6 -> N5 | ReservaEdit.java, QuadSelectionActivity.java | Apertura de selección (A11). El usuario pulsa cancelar (A12b) y vuelve a N5 sin cambios en la lista de quads. |
| 21 | (11,12c) | N5 -> N6 -> N5 | ReservaEdit.java, QuadSelectionActivity.java | Se entra en N6 (A11) y se sale mediante el botón atrás físico (A12c), retornando al formulario de reserva N5. |
| 22 | (11,21) | N5 -> N6 -> N6 | ReservaEdit.java, QuadSelectionActivity.java | Apertura de selección (A11). En N6 se pulsa ordenar por matrícula (A21) para facilitar la localización de un vehículo. |
| 23 | (11,21b) | N5 -> N6 -> N6 | ReservaEdit.java, QuadSelectionActivity.java | Entrada a selección (A11). Uso del botón de ordenación por tipo (A21b) dentro de la actividad de selección N6. |
| 24 | (11,21c) | N5 -> N6 -> N6 | ReservaEdit.java, QuadSelectionActivity.java | Navegación a N6 (A11). Aplicación del filtro de ordenación por precio diario (A21c). |
| 25 | (11,25) | N5 -> N6 -> N6 | ReservaEdit.java, QuadSelectionActivity.java | En N6 (tras A11), el usuario pulsa en el botón de detalles (A25) de un quad para ver su descripción técnica. |
| 26 | (11,26) | N5 -> N6 -> N6 | ReservaEdit.java, QuadSelectionAdapter.java | Tras abrir N6 (A11), se selecciona un quad lo que habilita el botón de popup para elegir el número de cascos (A26). |
| 27 | (12,10) | N6 -> N5 -> N1 | QuadSelectionActivity.java, ReservaEdit.java | Confirmación de selección (A12). De vuelta en N5, se pulsa confirmar reserva (A10) volviendo al menú principal con éxito. |
| 28 | (12,10b) | N6 -> N5 -> N1 | QuadSelectionActivity.java, ReservaEdit.java | Se confirman quads (A12) pero luego se decide cancelar la reserva completa (A10b) en el formulario principal. |
| 29 | (12,10c) | N6 -> N5 -> N1 | QuadSelectionActivity.java, ReservaEdit.java | Retorno de N6 a N5 (A12). El usuario usa el botón atrás en N5 (A10c) para descartar cambios y volver al menú. |
| 30 | (12,11) | N6 -> N5 -> N6 | QuadSelectionActivity.java, ReservaEdit.java | Tras seleccionar quads (A12), el usuario decide volver a entrar en la selección (A11) para añadir o quitar vehículos. |
| 31 | (12,20) | N6 -> N5 -> N5 | QuadSelectionActivity.java, ReservaEdit.java | Vuelta a N5 con quads (A12). El usuario pulsa en el selector de fecha de recogida (A20) para modificarla antes de confirmar. |
| 32 | (12,20b) | N6 -> N5 -> N5 | QuadSelectionActivity.java, ReservaEdit.java | Confirmación en N6 (A12). En N5 se ajusta la fecha de devolución (A20b) mediante el DatePickerDialog. |
| 33 | (12,9) | N6 -> N5 -> N4 | QuadSelectionActivity.java, ReservaEdit.java | Si se estaba editando una reserva, se confirma selección (A12) y se guarda el cambio (A9) volviendo al listado N4. |
| 34 | (12,9b) | N6 -> N5 -> N4 | QuadSelectionActivity.java, ReservaEdit.java | Retorno a N5 (A12). El usuario cancela la edición (A9b) volviendo al listado de reservas N4 sin aplicar cambios. |
| 35 | (12,9c) | N6 -> N5 -> N4 | QuadSelectionActivity.java, ReservaEdit.java | Vuelta de N6 (A12). Se pulsa atrás físico en N5 (A9c) retornando al listado de reservas N4. |
| 36 | (12b,10) | N6 -> N5 -> N1 | QuadSelectionActivity.java, ReservaEdit.java | Se cancela selección (A12b) y luego se intenta guardar reserva (A10). Fallará si no había quads previos. |
| 37 | (12b,10b) | N6 -> N5 -> N1 | QuadSelectionActivity.java, ReservaEdit.java | Cancelación en N6 (A12b) y posterior cancelación total de la reserva (A10b) volviendo al menú. |
| 38 | (12b,10c) | N6 -> N5 -> N1 | QuadSelectionActivity.java, ReservaEdit.java | Abandono de selección (A12b) y salida de N5 por botón atrás (A10c) hacia el menú principal. |
| 39 | (12b,11) | N6 -> N5 -> N6 | QuadSelectionActivity.java, ReservaEdit.java | Se cancela N6 (A12b) pero se vuelve a intentar entrar (A11) inmediatamente para realizar la selección correctamente. |
| 40 | (12b,20) | N6 -> N5 -> N5 | QuadSelectionActivity.java, ReservaEdit.java | Cancelación en N6 (A12b). En N5 se reajusta la fecha de recogida (A20) para ver si hay otros quads disponibles. |
| 41 | (12b,20b) | N6 -> N5 -> N5 | QuadSelectionActivity.java, ReservaEdit.java | Tras salir de N6 (A12b), se modifica la fecha de devolución (A20b) en el formulario N5. |
| 42 | (12b,9) | N6 -> N5 -> N4 | QuadSelectionActivity.java, ReservaEdit.java | Cancelación de selección (A12b) y guardado de edición en N5 (A9), manteniendo los quads que tuviera la reserva previamente. |
| 43 | (12b,9b) | N6 -> N5 -> N4 | QuadSelectionActivity.java, ReservaEdit.java | Se aborta N6 (A12b) y también la edición de reserva (A9b) volviendo a N4. |
| 44 | (12b,9c) | N6 -> N5 -> N4 | QuadSelectionActivity.java, ReservaEdit.java | Cancelación en N6 (A12b) y salida por atrás en N5 (A9c) hacia la lista de reservas. |
| 45 | (12c,10) | N6 -> N5 -> N1 | QuadSelectionActivity.java, ReservaEdit.java | Salida de N6 por atrás (A12c). Intento de guardado en N5 (A10). |
| 46 | (12c,10b) | N6 -> N5 -> N1 | QuadSelectionActivity.java, ReservaEdit.java | Retorno de N6 (A12c). Cancelación de reserva en N5 (A10b). |
| 47 | (12c,10c) | N6 -> N5 -> N1 | QuadSelectionActivity.java, ReservaEdit.java | Doble pulsación atrás: de N6 a N5 (A12c) y de N5 a N1 (A10c). |
| 48 | (12c,11) | N6 -> N5 -> N6 | QuadSelectionActivity.java, ReservaEdit.java | Se sale de N6 (A12c) y se vuelve a entrar (A11). |
| 49 | (12c,20) | N6 -> N5 -> N5 | QuadSelectionActivity.java, ReservaEdit.java | Salida de N6 (A12c) y ajuste de fecha de recogida (A20). |
| 50 | (12c,20b) | N6 -> N5 -> N5 | QuadSelectionActivity.java, ReservaEdit.java | Retorno a N5 (A12c) y cambio de fecha de devolución (A20b). |
| 51 | (12c,9) | N6 -> N5 -> N4 | QuadSelectionActivity.java, ReservaEdit.java | Salida de N6 (A12c) y guardado de edición en N5 (A9). |
| 52 | (12c,9b) | N6 -> N5 -> N4 | QuadSelectionActivity.java, ReservaEdit.java | Salida de N6 (A12c) y cancelación de edición en N5 (A9b). |
| 53 | (12c,9c) | N6 -> N5 -> N4 | QuadSelectionActivity.java, ReservaEdit.java | Doble atrás: de N6 a N5 (A12c) y de N5 a N4 (A9c). |
| 54 | (13,13) | N2 -> N2 -> N2 | QuadListActivity.java | Re-ordenación por matrícula sobre el listado ya ordenado. Operación permitida en UI. |
| 55 | (13,13b) | N2 -> N2 -> N2 | QuadListActivity.java | Cambio de criterio de ordenación: de Matrícula (A13) a Tipo (A13b) en N2. |
| 56 | (13,13c) | N2 -> N2 -> N2 | QuadListActivity.java | Cambio de ordenación: de Matrícula (A13) a Precio (A13c) en el listado de quads. |
| 57 | (13,14) | N2 -> N2 -> N2 | QuadListActivity.java | Tras ordenar (A13), el usuario procede a eliminar un quad de la lista (A14). |
| 58 | (13,23) | N2 -> N2 -> N1 | QuadListActivity.java | Ordenación de lista (A13) y posterior salida al menú principal (A23). |
| 59 | (13,5) | N2 -> N2 -> N3 | QuadListActivity.java | Se ordena el listado (A13) para encontrar un quad y editarlo (A5). |
| 60 | (13b,13) | N2 -> N2 -> N2 | QuadListActivity.java | Cambio de ordenación: de Tipo (A13b) a Matrícula (A13). |
| 61 | (13b,13b) | N2 -> N2 -> N2 | QuadListActivity.java | Pulsación repetida del botón de ordenación por tipo en N2. |
| 62 | (13b,13c) | N2 -> N2 -> N2 | QuadListActivity.java | Transición entre filtros de ordenación: Tipo (A13b) a Precio (A13c). |
| 63 | (13b,14) | N2 -> N2 -> N2 | QuadListActivity.java | Ordenación por tipo (A13b) y eliminación de elemento (A14). |
| 64 | (13b,23) | N2 -> N2 -> N1 | QuadListActivity.java | Filtro de tipo (A13b) y retorno al menú (A23). |
| 65 | (13b,5) | N2 -> N2 -> N3 | QuadListActivity.java | Uso del filtro de tipo (A13b) para localizar quad y editar (A5). |
| 66 | (13c,13) | N2 -> N2 -> N2 | QuadListActivity.java | Cambio de Precio (A13c) a Matrícula (A13). |
| 67 | (13c,13b) | N2 -> N2 -> N2 | QuadListActivity.java | Cambio de Precio (A13c) a Tipo (A13b). |
| 68 | (13c,13c) | N2 -> N2 -> N2 | QuadListActivity.java | Re-aplicación del filtro de precio en N2. |
| 69 | (13c,14) | N2 -> N2 -> N2 | QuadListActivity.java | Ordenación por precio (A13c) y borrado de quad (A14). |
| 70 | (13c,23) | N2 -> N2 -> N1 | QuadListActivity.java | Filtro por precio (A13c) y vuelta al menú (A23). |
| 71 | (13c,5) | N2 -> N2 -> N3 | QuadListActivity.java | Localización por precio (A13c) y entrada a edición (A5). |
| 72 | (14,13) | N2 -> N2 -> N2 | QuadListActivity.java | Borrado de quad (A14) y posterior ordenación de los restantes por matrícula (A13). |
| 73 | (14,13b) | N2 -> N2 -> N2 | QuadListActivity.java | Eliminación (A14) y ordenación por tipo (A13b). |
| 74 | (14,13c) | N2 -> N2 -> N2 | QuadListActivity.java | Borrado (A14) y ordenación por precio (A13c). |
| 75 | (14,14) | N2 -> N2 -> N2 | QuadListActivity.java | Eliminación sucesiva de dos vehículos de la lista N2. |
| 76 | (14,23) | N2 -> N2 -> N1 | QuadListActivity.java | Borrado de un elemento (A14) y salida al menú (A23). |
| 77 | (14,5) | N2 -> N2 -> N3 | QuadListActivity.java | Eliminación (A14) y edición de otro quad diferente (A5). |
| 78 | (15,15) | N3 -> N3 -> N3 | QuadEdit.java | Cambio repetido de selección a Monoplaza en el formulario. |
| 79 | (15,15b) | N3 -> N3 -> N3 | QuadEdit.java | Cambio de Monoplaza (A15) a Biplaza (A15b) mediante RadioButtons/Toggles. |
| 80 | (15,6) | N3 -> N3 -> N2 | QuadEdit.java | Selección de Monoplaza (A15) y guardado (A6) volviendo al listado. |
| 81 | (15,6b) | N3 -> N3 -> N2 | QuadEdit.java | Cambio a Monoplaza (A15) pero se decide cancelar la edición (A6b). |
| 82 | (15,6c) | N3 -> N3 -> N2 | QuadEdit.java | Ajuste de tipo (A15) y salida por botón atrás (A6c) a N2. |
| 83 | (15,7) | N3 -> N3 -> N1 | QuadEdit.java | Definición como monoplaza (A15) y guardado de nuevo quad (A7) hacia N1. |
| 84 | (15,7b) | N3 -> N3 -> N1 | QuadEdit.java | Ajuste (A15) y cancelación de creación (A7b) hacia el menú. |
| 85 | (15,7c) | N3 -> N3 -> N1 | QuadEdit.java | Selección de plaza (A15) y atrás físico (A7c) volviendo a N1. |
| 86 | (15b,15) | N3 -> N3 -> N3 | QuadEdit.java | Cambio de Biplaza (A15b) a Monoplaza (A15). |
| 87 | (15b,15b) | N3 -> N3 -> N3 | QuadEdit.java | Re-selección del modo biplaza en el formulario. |
| 88 | (15b,6) | N3 -> N3 -> N2 | QuadEdit.java | Definición como biplaza (A15b) y guardado en edición (A6) hacia N2. |
| 89 | (15b,6b) | N3 -> N3 -> N2 | QuadEdit.java | Ajuste biplaza (A15b) y cancelación (A6b). |
| 90 | (15b,6c) | N3 -> N3 -> N2 | QuadEdit.java | Selección biplaza (A15b) y atrás (A6c). |
| 91 | (15b,7) | N3 -> N3 -> N1 | QuadEdit.java | Creación de quad biplaza (A15b) y guardado exitoso (A7) a N1. |
| 92 | (15b,7b) | N3 -> N3 -> N1 | QuadEdit.java | Ajuste biplaza (A15b) y descarte de creación (A7b). |
| 93 | (15b,7c) | N3 -> N3 -> N1 | QuadEdit.java | Selección biplaza (A15b) y atrás físico (A7c). |
| 94 | (16,16) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenación por cliente (A16) sobre lista ya filtrada. |
| 95 | (16,16b) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de ordenación: Cliente (A16) a Teléfono (A16b) en N4. |
| 96 | (16,16c) | N4 -> N4 -> N4 | ReservaListActivity.java | Transición de Cliente (A16) a Fecha Recogida (A16c). |
| 97 | (16,16d) | N4 -> N4 -> N4 | ReservaListActivity.java | Transición de Cliente (A16) a Fecha Devolución (A16d). |
| 98 | (16,16e) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenación por cliente (A16) y filtrado por Previstas (A16e). |
| 99 | (16,16f) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenación (A16) y filtrado por Vigentes (A16f). |
| 100 | (16,16g) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenación (A16) y filtrado por Caducadas (A16g). |
| 101 | (16,16h) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenación (A16) y quitar todos los filtros (A16h). |
| 102 | (16,17) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por cliente (A16) y ver detalles de una reserva (A17). |
| 103 | (16,18) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar (A16) e iniciar el flujo de envío de mensaje (A18). |
| 104 | (16,24) | N4 -> N4 -> N1 | ReservaListActivity.java | Ordenar por cliente (A16) y volver al menú principal (A24). |
| 105 | (16,8) | N4 -> N4 -> N5 | ReservaListActivity.java | Ordenar (A16) y seleccionar una reserva para editar (A8). |
| 106 | (16b,16) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de Teléfono (A16b) a Cliente (A16). |
| 107 | (16b,16b) | N4 -> N4 -> N4 | ReservaListActivity.java | Re-aplicación del filtro de teléfono en la lista de reservas. |
| 108 | (16b,16c) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de Teléfono (A16b) a Fecha Recogida (A16c). |
| 109 | (16b,16d) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de Teléfono (A16b) a Fecha Devolución (A16d). |
| 110 | (16b,16e) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por teléfono (A16b) y filtrar Previstas (A16e). |
| 111 | (16b,16f) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por teléfono (A16b) y filtrar Vigentes (A16f). |
| 112 | (16b,16g) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por teléfono (A16b) y filtrar Caducadas (A16g). |
| 113 | (16b,16h) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por teléfono (A16b) y mostrar todas (A16h). |
| 114 | (16b,17) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por teléfono (A16b) y abrir diálogo de detalles (A17). |
| 115 | (16b,18) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por teléfono (A16b) y abrir diálogo de envío (A18). |
| 116 | (16b,24) | N4 -> N4 -> N1 | ReservaListActivity.java | Ordenar por teléfono (A16b) y salir al menú (A24). |
| 117 | (16b,8) | N4 -> N4 -> N5 | ReservaListActivity.java | Ordenar por teléfono (A16b) y entrar a editar reserva (A8). |
| 118 | (16c,16) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de Fecha Recogida (A16c) a Cliente (A16). |
| 119 | (16c,16b) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de Fecha Recogida (A16c) a Teléfono (A16b). |
| 120 | (16c,16c) | N4 -> N4 -> N4 | ReservaListActivity.java | Re-aplicación de ordenación por fecha de recogida. |
| 121 | (16c,16d) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de Fecha Recogida (A16c) a Fecha Devolución (A16d). |
| 122 | (16c,16e) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por fecha (A16c) y filtrar Previstas (A16e). |
| 123 | (16c,16f) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por fecha (A16c) y filtrar Vigentes (A16f). |
| 124 | (16c,16g) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por fecha (A16c) y filtrar Caducadas (A16g). |
| 125 | (16c,16h) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por fecha (A16c) y ver todas (A16h). |
| 126 | (16c,17) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por fecha (A16c) y ver detalles (A17). |
| 127 | (16c,18) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar por fecha (A16c) y enviar mensaje (A18). |
| 128 | (16c,24) | N4 -> N4 -> N1 | ReservaListActivity.java | Ordenar por fecha (A16c) y volver al menú (A24). |
| 129 | (16c,8) | N4 -> N4 -> N5 | ReservaListActivity.java | Ordenar por fecha (A16c) y editar reserva (A8). |
| 130 | (16d,16) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de Fecha Devolución (A16d) a Cliente (A16). |
| 131 | (16d,16b) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de Fecha Devolución (A16d) a Teléfono (A16b). |
| 132 | (16d,16c) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de Fecha Devolución (A16d) a Fecha Recogida (A16c). |
| 133 | (16d,16d) | N4 -> N4 -> N4 | ReservaListActivity.java | Re-aplicación de orden por fecha de devolución. |
| 134 | (16d,16e) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar (A16d) y filtrar Previstas (A16e). |
| 135 | (16d,16f) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar (A16d) y filtrar Vigentes (A16f). |
| 136 | (16d,16g) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar (A16d) y filtrar Caducadas (A16g). |
| 137 | (16d,16h) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar (A16d) y ver todas (A16h). |
| 138 | (16d,17) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar (A16d) y abrir detalles (A17). |
| 139 | (16d,18) | N4 -> N4 -> N4 | ReservaListActivity.java | Ordenar (A16d) y abrir envío (A18). |
| 140 | (16d,24) | N4 -> N4 -> N1 | ReservaListActivity.java | Ordenar (A16d) y volver al menú (A24). |
| 141 | (16d,8) | N4 -> N4 -> N5 | ReservaListActivity.java | Ordenar (A16d) y editar reserva (A8). |
| 142 | (16e,16) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtrado por Previstas (A16e) y posterior ordenación por cliente (A16). |
| 143 | (16e,16b) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Previstas (A16e) y ordenación por teléfono (A16b). |
| 144 | (16e,16c) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Previstas (A16e) y ordenación por fecha recogida (A16c). |
| 145 | (16e,16d) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Previstas (A16e) y ordenación por fecha devolución (A16d). |
| 146 | (16e,16e) | N4 -> N4 -> N4 | ReservaListActivity.java | Re-aplicación del filtro de reservas previstas. |
| 147 | (16e,16f) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de filtro: de Previstas (A16e) a Vigentes (A16f). |
| 148 | (16e,16g) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de filtro: de Previstas (A16e) a Caducadas (A16g). |
| 149 | (16e,16h) | N4 -> N4 -> N4 | ReservaListActivity.java | Quitar filtro Previstas (A16e) para ver todas las reservas (A16h). |
| 150 | (16e,17) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtrar Previstas (A16e) y ver detalles de una de ellas (A17). |
| 151 | (16e,18) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtrar Previstas (A16e) y enviar mensaje (A18). |
| 152 | (16e,24) | N4 -> N4 -> N1 | ReservaListActivity.java | Filtrar Previstas (A16e) y volver al menú (A24). |
| 153 | (16e,8) | N4 -> N4 -> N5 | ReservaListActivity.java | Filtrar Previstas (A16e) y editar una de ellas (A8). |
| 154 | (16f,16) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Vigentes (A16f) y ordenación por cliente (A16). |
| 155 | (16f,16b) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Vigentes (A16f) y ordenación por teléfono (A16b). |
| 156 | (16f,16c) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Vigentes (A16f) y ordenación por fecha recogida (A16c). |
| 157 | (16f,16d) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Vigentes (A16f) y ordenación por fecha devolución (A16d). |
| 158 | (16f,16e) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de filtro: Vigentes (A16f) a Previstas (A16e). |
| 159 | (16f,16f) | N4 -> N4 -> N4 | ReservaListActivity.java | Re-aplicación del filtro de reservas vigentes. |
| 160 | (16f,16g) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de filtro: Vigentes (A16f) a Caducadas (A16g). |
| 161 | (16f,16h) | N4 -> N4 -> N4 | ReservaListActivity.java | Quitar filtro Vigentes (A16f) para ver todas (A16h). |
| 162 | (16f,17) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtrar Vigentes (A16f) y abrir detalles (A17). |
| 163 | (16f,18) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtrar Vigentes (A16f) y enviar mensaje (A18). |
| 164 | (16f,24) | N4 -> N4 -> N1 | ReservaListActivity.java | Filtrar Vigentes (A16f) y volver al menú (A24). |
| 165 | (16f,8) | N4 -> N4 -> N5 | ReservaListActivity.java | Filtrar Vigentes (A16f) y editar una (A8). |
| 166 | (16g,16) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Caducadas (A16g) y ordenación por cliente (A16). |
| 167 | (16g,16b) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Caducadas (A16g) y ordenación por teléfono (A16b). |
| 168 | (16g,16c) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Caducadas (A16g) y ordenación por fecha recogida (A16c). |
| 169 | (16g,16d) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtro Caducadas (A16g) y ordenación por fecha devolución (A16d). |
| 170 | (16g,16e) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de filtro: Caducadas (A16g) a Previstas (A16e). |
| 171 | (16g,16f) | N4 -> N4 -> N4 | ReservaListActivity.java | Cambio de filtro: Caducadas (A16g) a Vigentes (A16f). |
| 172 | (16g,16g) | N4 -> N4 -> N4 | ReservaListActivity.java | Re-aplicación del filtro de caducadas. |
| 173 | (16g,16h) | N4 -> N4 -> N4 | ReservaListActivity.java | Quitar filtro Caducadas (A16g). |
| 174 | (16g,17) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtrar Caducadas (A16g) y ver detalles (A17). |
| 175 | (16g,18) | N4 -> N4 -> N4 | ReservaListActivity.java | Filtrar Caducadas (A16g) y enviar mensaje (A18). |
| 176 | (16g,24) | N4 -> N4 -> N1 | ReservaListActivity.java | Filtrar Caducadas (A16g) y volver al menú (A24). |
| 177 | (16g,8) | N4 -> N4 -> N5 | ReservaListActivity.java | Filtrar Caducadas (A16g) y editar una (A8). |
| 178 | (16h,16) | N4 -> N4 -> N4 | ReservaListActivity.java | Mostrar todas las reservas (A16h) y ordenar por cliente (A16). |
| 179 | (16h,16b) | N4 -> N4 -> N4 | ReservaListActivity.java | Ver todas (A16h) y ordenar por teléfono (A16b). |
| 180 | (16h,16c) | N4 -> N4 -> N4 | ReservaListActivity.java | Ver todas (A16h) y ordenar por fecha recogida (A16c). |
| 181 | (16h,16d) | N4 -> N4 -> N4 | ReservaListActivity.java | Ver todas (A16h) y ordenar por fecha devolución (A16d). |
| 182 | (16h,16e) | N4 -> N4 -> N4 | ReservaListActivity.java | Ver todas (A16h) y aplicar filtro de Previstas (A16e). |
| 183 | (16h,16f) | N4 -> N4 -> N4 | ReservaListActivity.java | Ver todas (A16h) y aplicar filtro de Vigentes (A16f). |
| 184 | (16h,16g) | N4 -> N4 -> N4 | ReservaListActivity.java | Ver todas (A16h) y aplicar filtro de Caducadas (A16g). |
| 185 | (16h,16h) | N4 -> N4 -> N4 | ReservaListActivity.java | Pulsación repetida de quitar filtros en N4. |
| 186 | (16h,17) | N4 -> N4 -> N4 | ReservaListActivity.java | Ver todas (A16h) y abrir detalles de una reserva (A17). |
| 187 | (16h,18) | N4 -> N4 -> N4 | ReservaListActivity.java | Ver todas (A16h) e iniciar flujo de envío (A18). |
| 188 | (16h,24) | N4 -> N4 -> N1 | ReservaListActivity.java | Ver todas (A16h) y volver al menú principal (A24). |
| 189 | (16h,8) | N4 -> N4 -> N5 | ReservaListActivity.java | Ver todas (A16h) y entrar a editar una reserva (A8). |
| 190 | (17,16) | N4 -> N4 -> N4 | ReservaListActivity.java | Cerrar diálogo de detalles (A17) y ordenar la lista por cliente (A16). |
| 191 | (17,16b) | N4 -> N4 -> N4 | ReservaListActivity.java | Salir de detalles (A17) y ordenar por teléfono (A16b). |
| 192 | (17,16c) | N4 -> N4 -> N4 | ReservaListActivity.java | Salir de detalles (A17) y ordenar por fecha recogida (A16c). |
| 193 | (17,16d) | N4 -> N4 -> N4 | ReservaListActivity.java | Salir de detalles (A17) y ordenar por fecha devolución (A16d). |
| 194 | (17,16e) | N4 -> N4 -> N4 | ReservaListActivity.java | Salir de detalles (A17) y filtrar por Previstas (A16e). |
| 195 | (17,16f) | N4 -> N4 -> N4 | ReservaListActivity.java | Salir de detalles (A17) y filtrar por Vigentes (A16f). |
| 196 | (17,16g) | N4 -> N4 -> N4 | ReservaListActivity.java | Salir de detalles (A17) y filtrar por Caducadas (A16g). |
| 197 | (17,16h) | N4 -> N4 -> N4 | ReservaListActivity.java | Salir de detalles (A17) y mostrar todas las reservas (A16h). |
| 198 | (17,17) | N4 -> N4 -> N4 | ReservaListActivity.java | Ver detalles de una reserva (A17), cerrar y ver detalles de otra (A17). |
| 199 | (17,18) | N4 -> N4 -> N4 | ReservaListActivity.java | Ver detalles (A17) y luego iniciar flujo de envío (A18) para la misma reserva. |
| 200 | (17,24) | N4 -> N4 -> N1 | ReservaListActivity.java | Ver detalles (A17) y volver al menú principal (A24). |
| 201 | (17,8) | N4 -> N4 -> N5 | ReservaListActivity.java | Ver detalles (A17) y decidir editar la reserva (A8). |
| 202 | (18,16) | N4 -> N4 -> N4 | ReservaListActivity.java | Tras el envío de mensaje (A18), ordenar la lista por cliente (A16). |
| 203 | (18,16b) | N4 -> N4 -> N4 | ReservaListActivity.java | Finalizar envío (A18) y ordenar por teléfono (A16b). |
| 204 | (18,16c) | N4 -> N4 -> N4 | ReservaListActivity.java | Finalizar envío (A18) y ordenar por fecha recogida (A16c). |
| 205 | (18,16d) | N4 -> N4 -> N4 | ReservaListActivity.java | Finalizar envío (A18) y ordenar por fecha devolución (A16d). |
| 206 | (18,16e) | N4 -> N4 -> N4 | ReservaListActivity.java | Finalizar envío (A18) y filtrar por Previstas (A16e). |
| 207 | (18,16f) | N4 -> N4 -> N4 | ReservaListActivity.java | Finalizar envío (A18) y filtrar por Vigentes (A16f). |
| 208 | (18,16g) | N4 -> N4 -> N4 | ReservaListActivity.java | Finalizar envío (A18) y filtrar por Caducadas (A16g). |
| 209 | (18,16h) | N4 -> N4 -> N4 | ReservaListActivity.java | Finalizar envío (A18) y mostrar todas las reservas (A16h). |
| 210 | (18,17) | N4 -> N4 -> N4 | ReservaListActivity.java | Enviar mensaje (A18) y abrir detalles de la reserva (A17). |
| 211 | (18,18) | N4 -> N4 -> N4 | ReservaListActivity.java | Enviar mensaje por WhatsApp (A18) y luego por SMS (A18). |
| 212 | (18,24) | N4 -> N4 -> N1 | ReservaListActivity.java | Finalizar envío (A18) y volver al menú principal (A24). |
| 213 | (18,8) | N4 -> N4 -> N5 | ReservaListActivity.java | Enviar mensaje (A18) y entrar a editar la reserva (A8). |
| 214 | (2,16) | N1 -> N4 -> N4 | SistemaReservas.java, ReservaListActivity.java | Desde el menú principal se abre el Listado de Reservas (A2). Se ordena por cliente (A16). |
| 215 | (2,16b) | N1 -> N4 -> N4 | SistemaReservas.java, ReservaListActivity.java | Abrir listado de reservas (A2) y ordenar por teléfono (A16b). |
| 216 | (2,16c) | N1 -> N4 -> N4 | SistemaReservas.java, ReservaListActivity.java | Abrir listado (A2) y ordenar por fecha de recogida (A16c). |
| 217 | (2,16d) | N1 -> N4 -> N4 | SistemaReservas.java, ReservaListActivity.java | Abrir listado (A2) y ordenar por fecha de devolución (A16d). |
| 218 | (2,16e) | N1 -> N4 -> N4 | SistemaReservas.java, ReservaListActivity.java | Abrir listado (A2) y filtrar por reservas Previstas (A16e). |
| 219 | (2,16f) | N1 -> N4 -> N4 | SistemaReservas.java, ReservaListActivity.java | Abrir listado (A2) y filtrar por reservas Vigentes (A16f). |
| 220 | (2,16g) | N1 -> N4 -> N4 | SistemaReservas.java, ReservaListActivity.java | Abrir listado (A2) y filtrar por reservas Caducadas (A16g). |
| 221 | (2,16h) | N1 -> N4 -> N4 | SistemaReservas.java, ReservaListActivity.java | Abrir listado (A2) y asegurar que se ven todas (A16h). |
| 222 | (2,17) | N1 -> N4 -> N4 | SistemaReservas.java, ReservaListActivity.java | Abrir listado (A2) y consultar detalles de la primera reserva (A17). |
| 223 | (2,18) | N1 -> N4 -> N4 | SistemaReservas.java, ReservaListActivity.java | Abrir listado (A2) e iniciar envío de confirmación (A18). |
| 224 | (2,24) | N1 -> N4 -> N1 | SistemaReservas.java, ReservaListActivity.java | Abrir listado de reservas (A2) y volver inmediatamente al menú (A24). |
| 225 | (2,8) | N1 -> N4 -> N5 | SistemaReservas.java, ReservaListActivity.java | Abrir listado (A2) y entrar a editar una reserva existente (A8). |
| 226 | (20,10) | N5 -> N5 -> N1 | ReservaEdit.java | Se selecciona la fecha de recogida (A20) y se procede a guardar la reserva (A10) hacia el menú principal. |
| 227 | (20,10b) | N5 -> N5 -> N1 | ReservaEdit.java | Se ajusta la fecha (A20) pero el usuario decide cancelar la reserva (A10b). |
| 228 | (20,10c) | N5 -> N5 -> N1 | ReservaEdit.java | Cambio de fecha (A20) y salida por botón atrás físico (A10c) a N1. |
| 229 | (20,11) | N5 -> N5 -> N6 | ReservaEdit.java, QuadSelectionActivity.java | Se define la fecha de recogida (A20). Con las fechas listas, se abre la selección de quads (A11). |
| 230 | (20,20) | N5 -> N5 -> N5 | ReservaEdit.java | Apertura repetida del selector de fecha de recogida en el formulario. |
| 231 | (20,20b) | N5 -> N5 -> N5 | ReservaEdit.java | Se define la recogida (A20) y luego la devolución (A20b). |
| 232 | (20,9) | N5 -> N5 -> N4 | ReservaEdit.java | En edición, se cambia fecha recogida (A20) y se guarda (A9) hacia N4. |
| 233 | (20,9b) | N5 -> N5 -> N4 | ReservaEdit.java | Cambio de fecha (A20) y cancelación de edición (A9b) hacia N4. |
| 234 | (20,9c) | N5 -> N5 -> N4 | ReservaEdit.java | Cambio de fecha (A20) y atrás físico (A9c) hacia el listado N4. |
| 235 | (20b,10) | N5 -> N5 -> N1 | ReservaEdit.java | Se ajusta la fecha de devolución (A20b) y se guarda la reserva (A10). |
| 236 | (20b,10b) | N5 -> N5 -> N1 | ReservaEdit.java | Cambio de fecha devolución (A20b) y cancelación (A10b). |
| 237 | (20b,10c) | N5 -> N5 -> N1 | ReservaEdit.java | Cambio de fecha (A20b) y atrás físico (A10c). |
| 238 | (20b,11) | N5 -> N5 -> N6 | ReservaEdit.java, QuadSelectionActivity.java | Tras definir la devolución (A20b), se procede a seleccionar los vehículos (A11). |
| 239 | (20b,20) | N5 -> N5 -> N5 | ReservaEdit.java | Ajuste de devolución (A20b) y re-ajuste de recogida (A20). |
| 240 | (20b,20b) | N5 -> N5 -> N5 | ReservaEdit.java | Apertura repetida del diálogo de fecha de devolución. |
| 241 | (20b,9) | N5 -> N5 -> N4 | ReservaEdit.java | Cambio de devolución en edición (A20b) y guardado (A9) a N4. |
| 242 | (20b,9b) | N5 -> N5 -> N4 | ReservaEdit.java | Cambio de fecha (A20b) y cancelación de edición (A9b). |
| 243 | (20b,9c) | N5 -> N5 -> N4 | ReservaEdit.java | Cambio de fecha (A20b) y atrás físico (A9c). |
| 244 | (21,12) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Se ordena por matrícula en selección (A21) y se confirma la elección (A12) volviendo a N5. |
| 245 | (21,12b) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ordenación (A21) y cancelación de la selección (A12b). |
| 246 | (21,12c) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ordenación (A21) y botón atrás (A12c) hacia el formulario N5. |
| 247 | (21,21) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Re-aplicación del orden por matrícula en la pantalla de selección. |
| 248 | (21,21b) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Cambio de ordenación en N6: de Matrícula (A21) a Tipo (A21b). |
| 249 | (21,21c) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Cambio de ordenación en N6: de Matrícula (A21) a Precio (A21c). |
| 250 | (21,25) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Ordenación por matrícula (A21) y consulta de detalles de un quad (A25). |
| 251 | (21,26) | N6 -> N6 -> N6 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ordenación (A21) y apertura del popup de selección de cascos (A26). |
| 252 | (21b,12) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ordenación por tipo (A21b) y confirmación de selección (A12). |
| 253 | (21b,12b) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ordenación por tipo (A21b) y cancelación (A12b). |
| 254 | (21b,12c) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ordenación por tipo (A21b) y botón atrás (A12c). |
| 255 | (21b,21) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Cambio de ordenación en N6: de Tipo (A21b) a Matrícula (A21). |
| 256 | (21b,21b) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Re-aplicación de orden por tipo en N6. |
| 257 | (21b,21c) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Cambio de ordenación en N6: de Tipo (A21b) a Precio (A21c). |
| 258 | (21b,25) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Ordenación por tipo (A21b) y ver detalles de quad (A25). |
| 259 | (21b,26) | N6 -> N6 -> N6 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ordenación por tipo (A21b) y ajuste de cascos (A26). |
| 260 | (21c,12) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ordenación por precio (A21c) y confirmación de reserva (A12). |
| 261 | (21c,12b) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ordenación por precio (A21c) y cancelación de selección (A12b). |
| 262 | (21c,12c) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ordenación por precio (A21c) y atrás físico (A12c). |
| 263 | (21c,21) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Cambio de Precio (A21c) a Matrícula (A21). |
| 264 | (21c,21b) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Cambio de Precio (A21c) a Tipo (A21b). |
| 265 | (21c,21c) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Re-aplicación de filtro por precio en N6. |
| 266 | (21c,25) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Ordenación por precio (A21c) y consulta de detalles (A25). |
| 267 | (21c,26) | N6 -> N6 -> N6 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ordenación por precio (A21c) y selector de cascos (A26). |
| 268 | (23,1) | N2 -> N1 -> N2 | QuadListActivity.java, SistemaReservas.java | Retorno de listado de quads al menú (A23) y re-entrada al mismo (A1). |
| 269 | (23,2) | N2 -> N1 -> N4 | QuadListActivity.java, SistemaReservas.java | Salida de quads (A23) y apertura de listado de reservas (A2). |
| 270 | (23,3) | N2 -> N1 -> N3 | QuadListActivity.java, SistemaReservas.java | Salida de quads (A23) y apertura de creación de nuevo quad (A3). |
| 271 | (23,4) | N2 -> N1 -> N5 | QuadListActivity.java, SistemaReservas.java | Salida de quads (A23) y apertura de creación de nueva reserva (A4). |
| 272 | (24,1) | N4 -> N1 -> N2 | ReservaListActivity.java, SistemaReservas.java | Retorno de listado de reservas (A24) y apertura de listado de quads (A1). |
| 273 | (24,2) | N4 -> N1 -> N4 | ReservaListActivity.java, SistemaReservas.java | Salida de reservas (A24) y re-entrada al listado de reservas (A2). |
| 274 | (24,3) | N4 -> N1 -> N3 | ReservaListActivity.java, SistemaReservas.java | Salida de reservas (A24) y apertura de creación de quad (A3). |
| 275 | (24,4) | N4 -> N1 -> N5 | ReservaListActivity.java, SistemaReservas.java | Salida de reservas (A24) y apertura de creación de reserva (A4). |
| 276 | (25,12) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ver detalles de quad (A25) y confirmar la selección del mismo (A12). |
| 277 | (25,12b) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ver detalles (A25) y cancelar la selección (A12b). |
| 278 | (25,12c) | N6 -> N6 -> N5 | QuadSelectionActivity.java | Ver detalles (A25) y atrás físico (A12c). |
| 279 | (25,21) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Ver detalles (A25) y ordenar por matrícula (A21). |
| 280 | (25,21b) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Ver detalles (A25) y ordenar por tipo (A21b). |
| 281 | (25,21c) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Ver detalles (A25) y ordenar por precio (A21c). |
| 282 | (25,25) | N6 -> N6 -> N6 | QuadSelectionActivity.java | Ver detalles de un quad (A25) y luego de otro diferente (A25). |
| 283 | (25,26) | N6 -> N6 -> N6 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ver detalles (A25) y ajustar número de cascos (A26). |
| 284 | (26,12) | N6 -> N6 -> N5 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ajustar cascos (A26) y confirmar selección de vehículos (A12). |
| 285 | (26,12b) | N6 -> N6 -> N5 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ajustar cascos (A26) y cancelar selección (A12b). |
| 286 | (26,12c) | N6 -> N6 -> N5 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ajustar cascos (A26) y salir por botón atrás (A12c). |
| 287 | (26,21) | N6 -> N6 -> N6 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ajustar cascos (A26) y ordenar por matrícula (A21). |
| 288 | (26,21b) | N6 -> N6 -> N6 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ajustar cascos (A26) y ordenar por tipo (A21b). |
| 289 | (26,21c) | N6 -> N6 -> N6 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ajustar cascos (A26) y ordenar por precio (A21c). |
| 290 | (26,25) | N6 -> N6 -> N6 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ajustar cascos (A26) y ver detalles técnicos del quad (A25). |
| 291 | (26,26) | N6 -> N6 -> N6 | QuadSelectionActivity.java, QuadSelectionAdapter.java | Ajuste repetido del número de cascos para un quad seleccionado. |
| 292 | (3,15) | N1 -> N3 -> N3 | SistemaReservas.java, QuadEdit.java | Desde el menú se abre creación de quad (A3). Se marca como monoplaza (A15). |
| 293 | (3,15b) | N1 -> N3 -> N3 | SistemaReservas.java, QuadEdit.java | Se abre creación de quad (A3) y se marca como biplaza (A15b). |
| 294 | (3,7) | N1 -> N3 -> N1 | SistemaReservas.java, QuadEdit.java | Se abre creación (A3) y se guarda el nuevo registro (A7) hacia N1. |
| 295 | (3,7b) | N1 -> N3 -> N1 | SistemaReservas.java, QuadEdit.java | Se abre creación (A3) y se cancela (A7b) volviendo al menú. |
| 296 | (3,7c) | N1 -> N3 -> N1 | SistemaReservas.java, QuadEdit.java | Apertura de creación (A3) y salida por botón atrás (A7c). |
| 297 | (4,10b) | N1 -> N5 -> N1 | SistemaReservas.java, ReservaEdit.java | Inicio de reserva (A4) y cancelación inmediata (A10b) volviendo al menú. |
| 298 | (4,10c) | N1 -> N5 -> N1 | SistemaReservas.java, ReservaEdit.java | Inicio de reserva (A4) y salida por atrás físico (A10c). |
| 299 | (4,20) | N1 -> N5 -> N5 | SistemaReservas.java, ReservaEdit.java | Inicio de reserva (A4). Se pulsa selector de fecha de recogida (A20). |
| 300 | (4,20b) | N1 -> N5 -> N5 | SistemaReservas.java, ReservaEdit.java | Inicio de reserva (A4). Se pulsa selector de fecha de devolución (A20b). |
| 301 | (5,15) | N2 -> N3 -> N3 | QuadListActivity.java, QuadEdit.java | Edición de un quad existente (A5). Se cambia a monoplaza (A15). |
| 302 | (5,15b) | N2 -> N3 -> N3 | QuadListActivity.java, QuadEdit.java | Edición de quad (A5) y cambio a biplaza (A15b). |
| 303 | (5,6) | N2 -> N3 -> N2 | QuadListActivity.java, QuadEdit.java | Edición (A5) y guardado de los cambios (A6) volviendo al listado N2. |
| 304 | (5,6b) | N2 -> N3 -> N2 | QuadListActivity.java, QuadEdit.java | Edición (A5) y cancelación (A6b) volviendo al listado. |
| 305 | (5,6c) | N2 -> N3 -> N2 | QuadListActivity.java, QuadEdit.java | Edición (A5) y salida por botón atrás (A6c) hacia N2. |
| 306 | (6,13) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Guardado en edición (A6) y posterior ordenación de la lista actualizada (A13). |
| 307 | (6,13b) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Guardado (A6) y ordenación por tipo (A13b). |
| 308 | (6,13c) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Guardado (A6) y ordenación por precio (A13c). |
| 309 | (6,14) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Guardado (A6) y eliminación de otro quad (A14). |
| 310 | (6,23) | N3 -> N2 -> N1 | QuadEdit.java, QuadListActivity.java | Guardado de cambios (A6) y retorno al menú principal (A23). |
| 311 | (6,5) | N3 -> N2 -> N3 | QuadEdit.java, QuadListActivity.java | Guardado (A6) y edición inmediata de otro vehículo (A5). |
| 312 | (6b,13) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Cancelación (A6b) y ordenación de la lista por matrícula (A13). |
| 313 | (6b,13b) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Cancelación (A6b) y ordenación por tipo (A13b). |
| 314 | (6b,13c) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Cancelación (A6b) y ordenación por precio (A13c). |
| 315 | (6b,14) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Cancelación (A6b) y eliminación de un quad (A14). |
| 316 | (6b,23) | N3 -> N2 -> N1 | QuadEdit.java, QuadListActivity.java | Cancelación de edición (A6b) y retorno al menú principal (A23). |
| 317 | (6b,5) | N3 -> N2 -> N3 | QuadEdit.java, QuadListActivity.java | Cancelación (A6b) e inicio de edición de otro quad (A5). |
| 318 | (6c,13) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Salida por atrás (A6c) y ordenación por matrícula (A13). |
| 319 | (6c,13b) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Salida por atrás (A6c) y ordenación por tipo (A13b). |
| 320 | (6c,13c) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Salida por atrás (A6c) y ordenación por precio (A13c). |
| 321 | (6c,14) | N3 -> N2 -> N2 | QuadEdit.java, QuadListActivity.java | Salida por atrás (A6c) y borrado de quad (A14). |
| 322 | (6c,23) | N3 -> N2 -> N1 | QuadEdit.java, QuadListActivity.java | Salida por atrás (A6c) y retorno al menú (A23). |
| 323 | (6c,5) | N3 -> N2 -> N3 | QuadEdit.java, QuadListActivity.java | Salida por atrás (A6c) y edición de quad (A5). |
| 324 | (7,1) | N3 -> N1 -> N2 | QuadEdit.java, SistemaReservas.java | Guardado de nuevo quad (A7) y apertura del listado para verlo (A1). |
| 325 | (7,2) | N3 -> N1 -> N4 | QuadEdit.java, SistemaReservas.java | Guardado de quad (A7) y apertura de listado de reservas (A2). |
| 326 | (7,3) | N3 -> N1 -> N3 | QuadEdit.java, SistemaReservas.java | Guardado de quad (A7) e inicio de creación de otro quad (A3). |
| 327 | (7,4) | N3 -> N1 -> N5 | QuadEdit.java, SistemaReservas.java | Guardado de quad (A7) e inicio de creación de reserva (A4). |
| 328 | (7b,1) | N3 -> N1 -> N2 | QuadEdit.java, SistemaReservas.java | Cancelación de creación (A7b) y apertura de listado (A1). |
| 329 | (7b,2) | N3 -> N1 -> N4 | QuadEdit.java, SistemaReservas.java | Cancelación (A7b) y apertura de reservas (A2). |
| 330 | (7b,3) | N3 -> N1 -> N3 | QuadEdit.java, SistemaReservas.java | Cancelación (A7b) e intento de creación de nuevo (A3). |
| 331 | (7b,4) | N3 -> N1 -> N5 | QuadEdit.java, SistemaReservas.java | Cancelación (A7b) e inicio de reserva (A4). |
| 332 | (7c,1) | N3 -> N1 -> N2 | QuadEdit.java, SistemaReservas.java | Salida por atrás (A7c) y apertura de listado (A1). |
| 333 | (7c,2) | N3 -> N1 -> N4 | QuadEdit.java, SistemaReservas.java | Salida por atrás (A7c) y apertura de reservas (A2). |
| 334 | (7c,3) | N3 -> N1 -> N3 | QuadEdit.java, SistemaReservas.java | Salida por atrás (A7c) e inicio de creación (A3). |
| 335 | (7c,4) | N3 -> N1 -> N5 | QuadEdit.java, SistemaReservas.java | Salida por atrás (A7c) e inicio de reserva (A4). |
| 336 | (8,11) | N4 -> N5 -> N6 | ReservaListActivity.java, ReservaEdit.java, QuadSelectionActivity.java | Se edita una reserva (A8) y se entra en la selección de quads (A11) para modificar los vehículos asignados. |
| 337 | (8,20) | N4 -> N5 -> N5 | ReservaListActivity.java, ReservaEdit.java | Edición de reserva (A8) y ajuste de fecha de recogida (A20). |
| 338 | (8,20b) | N4 -> N5 -> N5 | ReservaListActivity.java, ReservaEdit.java | Edición de reserva (A8) y ajuste de fecha de devolución (A20b). |
| 339 | (8,9) | N4 -> N5 -> N4 | ReservaListActivity.java, ReservaEdit.java | Edición de reserva (A8) y guardado de cambios (A9) volviendo al listado N4. |
| 340 | (8,9b) | N4 -> N5 -> N4 | ReservaListActivity.java, ReservaEdit.java | Edición de reserva (A8) y cancelación de la misma (A9b). |
| 341 | (8,9c) | N4 -> N5 -> N4 | ReservaListActivity.java, ReservaEdit.java | Edición de reserva (A8) y salida por botón atrás (A9c). |
| 342 | (9,16) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Guardado de cambios (A9) y ordenación de la lista por cliente (A16). |
| 343 | (9,16b) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Guardado (A9) y ordenación por teléfono (A16b). |
| 344 | (9,16c) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Guardado (A9) y ordenación por recogida (A16c). |
| 345 | (9,16d) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Guardado (A9) y ordenación por devolución (A16d). |
| 346 | (9,16e) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Guardado (A9) y filtrado por Previstas (A16e). |
| 347 | (9,16f) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Guardado (A9) y filtrado por Vigentes (A16f). |
| 348 | (9,16g) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Guardado (A9) y filtrado por Caducadas (A16g). |
| 349 | (9,16h) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Guardado (A9) y ver todas las reservas (A16h). |
| 350 | (9,17) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Guardado (A9) y consulta de detalles de la reserva actualizada (A17). |
| 351 | (9,18) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Guardado (A9) y envío de mensaje de confirmación (A18). |
| 352 | (9,24) | N5 -> N4 -> N1 | ReservaEdit.java, ReservaListActivity.java | Guardado de edición (A9) y retorno al menú principal (A24). |
| 353 | (9,8) | N5 -> N4 -> N5 | ReservaEdit.java, ReservaListActivity.java | Guardado (A9) e inicio de edición de otra reserva diferente (A8). |
| 354 | (9b,16) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Cancelación de edición (A9b) y ordenación por cliente (A16). |
| 355 | (9b,16b) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y ordenación por teléfono (A16b). |
| 356 | (9b,16c) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y ordenación por recogida (A16c). |
| 357 | (9b,16d) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y ordenación por devolución (A16d). |
| 358 | (9b,16e) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y filtrado por Previstas (A16e). |
| 359 | (9b,16f) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y filtrado por Vigentes (A16f). |
| 360 | (9b,16g) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y filtrado por Caducadas (A16g). |
| 361 | (9b,16h) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y ver todas (A16h). |
| 362 | (9b,17) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y ver detalles (A17). |
| 363 | (9b,18) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y enviar mensaje (A18). |
| 364 | (9b,24) | N5 -> N4 -> N1 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y volver al menú (A24). |
| 365 | (9b,8) | N5 -> N4 -> N5 | ReservaEdit.java, ReservaListActivity.java | Cancelación (A9b) y editar otra reserva (A8). |
| 366 | (9c,16) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Salida por atrás (A9c) y ordenación por cliente (A16). |
| 367 | (9c,16b) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Salida por atrás (A9c) y ordenación por teléfono (A16b). |
| 368 | (9c,16c) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Salida por atrás (A9c) y ordenación por recogida (A16c). |
| 369 | (9c,16d) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Salida por atrás (A9c) y ordenación por devolución (A16d). |
| 370 | (9c,16e) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Salida por atrás (A9c) y filtrado por Previstas (A16e). |
| 371 | (9c,16f) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Salida por atrás (A9c) y filtrado por Vigentes (A16f). |
| 372 | (9c,16g) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Salida por atrás (A9c) y filtrado por Caducadas (A16g). |
| 373 | (9c,16h) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Salida por atrás (A9c) y ver todas (A16h). |
| 374 | (9c,17) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Salida por atrás (A9c) y ver detalles (A17). |
| 375 | (9c,18) | N5 -> N4 -> N4 | ReservaEdit.java, ReservaListActivity.java | Salida por atrás (A9c) y enviar mensaje (A18). |
| 376 | (9c,24) | N5 -> N4 -> N1 | ReservaEdit.java, ReservaListActivity.java | Salida de N5 por botón atrás físico (A9c) y retorno al menú principal (A24) desde el listado. |
| 377 | (9c,8) | N5 -> N4 -> N5 | ReservaEdit.java, ReservaListActivity.java | Salida de N5 por atrás (A9c) y entrada inmediata a la edición de otra reserva (A8) desde el listado. |

---

## 2. PARES IMPOSIBLES

| ID | Pareja Aristas | Navegación Verificada | Ficheros Comprobados | Descripción Técnica |
|----|-----------------|----------------------|----------------------|---------------------|
| 1 | (3,6) | N1 -> N3 -> N2 | QuadEdit.java | **Imposible por Stack:** Si se entra desde N1 (Crear), QuadEdit debe volver a N1 mediante setResult(RESULT_OK/CANCELED), no puede alcanzar N2 (Listado) directamente. |
| 2 | (3,6b) | N1 -> N3 -> N2 | QuadEdit.java | **Imposible por Stack:** Cancelar en un formulario iniciado desde el menú principal (N1) retorna obligatoriamente a N1. |
| 3 | (3,6c) | N1 -> N3 -> N2 | QuadEdit.java | **Imposible por Stack:** El botón atrás físico en una actividad lanzada desde N1 siempre retorna a N1. |
| 4 | (4,10) | N1 -> N5 -> N1 | ReservaEdit.java | **Imposible por Validación:** No se puede guardar una reserva nueva (A10) sin haber pasado por la selección de quads (A11), el código bloquea el flujo si la lista está vacía. |
| 5 | (4,11) | N1 -> N5 -> N6 | ReservaEdit.java | **Imposible por Validación:** El botón de selección de quads (A11) está condicionado a que las fechas (A20, A20b) no estén vacías. |
| 6 | (4,9) | N1 -> N5 -> N4 | ReservaEdit.java | **Imposible por Stack:** Una reserva creada desde el menú principal (N1) vuelve al menú al finalizar, no al listado de reservas (N4). |
| 7 | (4,9b) | N1 -> N5 -> N4 | ReservaEdit.java | **Imposible por Stack:** Cancelar creación desde N1 vuelve a N1. |
| 8 | (4,9c) | N1 -> N5 -> N4 | ReservaEdit.java | **Imposible por Stack:** Botón atrás desde N5 iniciado por N1 vuelve a N1. |
| 9 | (5,7) | N2 -> N3 -> N1 | QuadEdit.java | **Imposible por Stack:** Si se entra desde N2 (Editar), al finalizar la actividad se vuelve a N2, nunca directamente a N1. |
| 10 | (5,7b) | N2 -> N3 -> N1 | QuadEdit.java | **Imposible por Stack:** Cancelar una edición iniciada desde el listado (N2) retorna a dicho listado. |
| 11 | (5,7c) | N2 -> N3 -> N1 | QuadEdit.java | **Imposible por Stack:** El botón atrás físico desde una edición vuelve al listado N2. |
| 12 | (8,10) | N4 -> N5 -> N1 | ReservaEdit.java | **Imposible por Stack:** Una edición iniciada desde el listado (N4) retorna al listado, no al menú principal (N1). |
| 13 | (8,10b) | N4 -> N5 -> N1 | ReservaEdit.java | **Imposible por Stack:** Cancelar edición desde N4 vuelve a N4. |
| 14 | (8,10c) | N4 -> N5 -> N1 | ReservaEdit.java | **Imposible por Stack:** Botón atrás desde N5 iniciado por N4 vuelve a N4. |
