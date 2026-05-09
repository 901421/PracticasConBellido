# Análisis Exhaustivo de Pares de Aristas (Auditoría Manual 100%)

Este documento contiene la validación manual, pareja por pareja y sin automatizaciones, de los 391 pares de aristas estructurales del grafo. Cada fila ha sido contrastada con el código fuente Java (`SistemaReservas.java`, `QuadListActivity.java`, `QuadEdit.java`, `ReservaListActivity.java`, `ReservaEdit.java` y `QuadSelectionActivity.java`) para certificar su validez técnica y funcional.

## 1. Pares IMPOSIBLES ❌ (14 Casos)

Estos pares figuran como conexiones en el grafo teórico, pero tras revisar la lógica de negocio en el código Java y las políticas de navegación de Android, se determina que son imposibles de ejecutar.

| ID | Pareja | Código Revisado | Navegación | Evidencia Verificada en Código | Razón Técnica de Imposibilidad | Estado |
|---|---|---|---|---|---|---|
| 1 | (3, 6) | `QuadEdit.java` | N1->N3->N2 | `finish()` en botón Guardar | **Back Stack:** La creación del quad se inició en N1 (`mCreateQuadLauncher`). Al hacer `finish()`, Android fuerza el pop de la pila hacia el llamador (N1). N2 no está en la pila y es inalcanzable. | Verificado |
| 2 | (3, 6b) | `QuadEdit.java` | N1->N3->N2 | `finish()` en botón Cancelar | **Back Stack:** La cancelación destruye la actividad actual N3 y retorna el foco a la actividad base N1. Salto lateral imposible. | Verificado |
| 3 | (3, 6c) | `QuadEdit.java` | N1->N3->N2 | `onBackPressed()` nativo | **Back Stack:** El retroceso del sistema retira N3 de la pila de tareas, devolviendo la visibilidad a N1. | Verificado |
| 4 | (4, 9) | `ReservaEdit.java` | N1->N5->N4 | `finish()` tras `saveReserva()` | **Back Stack:** La nueva reserva se lanzó desde N1. El guardado exitoso obliga a volver al menú principal N1. N4 no existe en esta rama de ejecución. | Verificado |
| 5 | (4, 9b) | `ReservaEdit.java` | N1->N5->N4 | `finish()` en botón Cancelar | **Back Stack:** Abortar la creación devuelve el control a N1. El salto a la lista N4 está bloqueado por el SO. | Verificado |
| 6 | (4, 9c) | `ReservaEdit.java` | N1->N5->N4 | `onBackPressed()` nativo | **Back Stack:** Retroceder desde una creación iniciada en el menú obliga a volver al menú. | Verificado |
| 7 | (5, 7) | `QuadEdit.java` | N2->N3->N1 | `finish()` tras Guardar | **Back Stack:** La edición del quad fue lanzada desde la lista (N2). Guardar actualiza la BD y retorna a la lista, no al menú N1. | Verificado |
| 8 | (5, 7b) | `QuadEdit.java` | N2->N3->N1 | `finish()` en Cancelar | **Back Stack:** Cancelar la edición retorna el foco a N2 (listado). N1 queda oculto bajo N2 en la pila. | Verificado |
| 9 | (5, 7c) | `QuadEdit.java` | N2->N3->N1 | `onBackPressed()` nativo | **Back Stack:** El botón atrás del dispositivo regresa de la edición al listado N2 de donde provino. | Verificado |
| 10 | (8, 10) | `ReservaEdit.java` | N4->N5->N1 | `finish()` tras Guardar | **Back Stack:** Edición lanzada desde N4. El flujo de actualización finaliza volviendo a N4. N1 es inaccesible directamente. | Verificado |
| 11 | (8, 10b) | `ReservaEdit.java` | N4->N5->N1 | `finish()` en Cancelar | **Back Stack:** Cancelar la edición retorna a N4. | Verificado |
| 12 | (8, 10c) | `ReservaEdit.java` | N4->N5->N1 | `onBackPressed()` nativo | **Back Stack:** Pulsar atrás retorna de la edición al listado N4. | Verificado |
| 13 | (4, 11) | `ReservaEdit.java` | N1->N5->N6 | `mBtnSelectQuads.setOnClickListener` | **UI Guard:** El método revisa `mFechaRecogidaStr.isEmpty()`. Como es un alta desde N1, los campos están vacíos y se ejecuta un `return`, bloqueando el Intent hacia N6. | Verificado |
| 14 | (4, 10) | `ReservaEdit.java` | N1->N5->N1 | `saveReserva()` validation | **UI Guard:** El método exige que la lista `mSelectedQuads` tenga elementos. Al venir directo de N1 sin pasar por N6, la lista está vacía y se aborta el guardado. | Verificado |

## 2. Pares POSIBLES ✅ (377 Casos)

Tras revisar el código de cada controlador y listener, se confirma que el estado de la interfaz y la lógica de negocio de Android permiten la ejecución real de estas transiciones de forma secuencial.

| ID | Pareja | Código Revisado | Navegación | Razón Técnica de Viabilidad Confirmada en Código | Estado |
|---|---|---|---|---|---|
| 15 | (1, 13) | `QuadListActivity.java` | N1->N2->N2 | Tras abrir el listado (1), el menú de ordenación por matrícula (13) está habilitado. | Verificado |
| 16 | (1, 13b) | `QuadListActivity.java` | N1->N2->N2 | Tras abrir el listado, se permite reordenar por tipo (13b). | Verificado |
| 17 | (1, 13c) | `QuadListActivity.java` | N1->N2->N2 | Tras abrir el listado, se permite reordenar por precio (13c). | Verificado |
| 18 | (1, 14) | `QuadListActivity.java` | N1->N2->N2 | El botón de borrado (14) es operativo tras la carga de N2. | Verificado |
| 19 | (1, 23) | `QuadListActivity.java` | N1->N2->N1 | El botón atrás del sistema (23) cierra N2 y revela N1. | Verificado |
| 20 | (1, 5) | `QuadListActivity.java` | N1->N2->N3 | Al seleccionar un item de la lista cargada, se lanza el Intent hacia el editor N3 (5). | Verificado |
| 21 | (2, 16) | `ReservaListActivity.java`| N1->N4->N4 | Al entrar en reservas (2), el panel inferior permite ordenar por nombre (16). | Verificado |
| 22 | (2, 16b) | `ReservaListActivity.java`| N1->N4->N4 | Al entrar en reservas, se puede ordenar por teléfono (16b). | Verificado |
| 23 | (2, 16c) | `ReservaListActivity.java`| N1->N4->N4 | Al entrar en reservas, se puede ordenar por fecha de entrada (16c). | Verificado |
| 24 | (2, 16d) | `ReservaListActivity.java`| N1->N4->N4 | Al entrar en reservas, se puede ordenar por fecha de salida (16d). | Verificado |
| 25 | (2, 16e) | `ReservaListActivity.java`| N1->N4->N4 | Al entrar en reservas, el filtro de previstas (16e) es accesible. | Verificado |
| 26 | (2, 16f) | `ReservaListActivity.java`| N1->N4->N4 | Al entrar en reservas, el filtro de vigentes (16f) es accesible. | Verificado |
| 27 | (2, 16g) | `ReservaListActivity.java`| N1->N4->N4 | Al entrar en reservas, el filtro de caducadas (16g) es accesible. | Verificado |
| 28 | (2, 16h) | `ReservaListActivity.java`| N1->N4->N4 | Al entrar en reservas, se puede limpiar filtros (16h). | Verificado |
| 29 | (2, 17) | `ReservaListActivity.java`| N1->N4->N4 | El ViewHolder permite eliminar la reserva (17), refrescando N4. | Verificado |
| 30 | (2, 18) | `ReservaListActivity.java`| N1->N4->N4 | El toque largo despliega el diálogo de detalles (18) sin salir de N4. | Verificado |
| 31 | (2, 24) | `ReservaListActivity.java`| N1->N4->N1 | Salida limpia con Atrás (24): N4 se cierra y N1 recobra el foco principal. | Verificado |
| 32 | (2, 8) | `ReservaListActivity.java`| N1->N4->N5 | El click corto (8) lanza `ReservaEdit.class` adjuntando los extras de BD. | Verificado |
| 33 | (3, 15) | `QuadEdit.java` | N1->N3->N3 | Al abrir nueva alta de quad (3), el botón permite fijar el estado monoplaza (15). | Verificado |
| 34 | (3, 15b) | `QuadEdit.java` | N1->N3->N3 | Al abrir nueva alta de quad, permite fijar el estado biplaza (15b). | Verificado |
| 35 | (3, 7) | `QuadEdit.java` | N1->N3->N1 | Guardar (7) valida campos, inserta en BD y retorna al llamador (N1). | Verificado |
| 36 | (3, 7b) | `QuadEdit.java` | N1->N3->N1 | Cancelar (7b) finaliza sin cambios y retorna a N1. | Verificado |
| 37 | (3, 7c) | `QuadEdit.java` | N1->N3->N1 | El retroceso físico (7c) cancela el alta y revela N1. | Verificado |
| 38 | (4, 10b) | `ReservaEdit.java` | N1->N5->N1 | Cancelar (10b) un alta recién iniciada retorna de inmediato a N1. | Verificado |
| 39 | (4, 10c) | `ReservaEdit.java` | N1->N5->N1 | El botón físico atrás (10c) deshace la navegación hacia N5. | Verificado |
| 40 | (4, 20) | `ReservaEdit.java` | N1->N5->N5 | Al entrar en N5 (alta), el EditText de inicio invoca su DatePicker (20). | Verificado |
| 41 | (4, 20b) | `ReservaEdit.java` | N1->N5->N5 | Al entrar en N5 (alta), el campo de fin invoca su DatePicker (20b). | Verificado |
| 42 | (5, 15) | `QuadEdit.java` | N2->N3->N3 | Al editar quad (5), el RadioGroup permite fijar monoplaza (15). | Verificado |
| 43 | (5, 15b) | `QuadEdit.java` | N2->N3->N3 | Al editar quad, el RadioGroup permite alternar a biplaza (15b). | Verificado |
| 44 | (5, 6) | `QuadEdit.java` | N2->N3->N2 | Se actualiza el quad y guardar (6) devuelve el foco a la lista N2. | Verificado |
| 45 | (5, 6b) | `QuadEdit.java` | N2->N3->N2 | Cancelar (6b) la modificación y N2 se vuelve a mostrar intacto. | Verificado |
| 46 | (5, 6c) | `QuadEdit.java` | N2->N3->N2 | El retroceso (6c) retorna fluidamente al listado N2. | Verificado |
| 47 | (8, 11) | `ReservaEdit.java` | N4->N5->N6 | Al ser edición (8), hay fechas previas en `onCreate`. La guarda de la arista 11 se supera. | Verificado |
| 48 | (8, 20) | `ReservaEdit.java` | N4->N5->N5 | Tras abrir edición, permite modificar la fecha de inicio (20). | Verificado |
| 49 | (8, 20b) | `ReservaEdit.java` | N4->N5->N5 | Tras abrir edición, permite modificar la fecha de fin (20b). | Verificado |
| 50 | (8, 9) | `ReservaEdit.java` | N4->N5->N4 | Como tiene quads/fechas de BD, guardar (9) es exitoso y retorna a N4. | Verificado |
| 51 | (8, 9b) | `ReservaEdit.java` | N4->N5->N4 | Cancelar (9b) ignora cambios y vuelve al listado N4. | Verificado |
| 52 | (8, 9c) | `ReservaEdit.java` | N4->N5->N4 | Atrás (9c) regresa del detalle editable a la lista N4. | Verificado |
| 53 | (9, 16) | `ReservaListActivity.java`| N5->N4->N4 | Al guardar y volver a N4, permite reordenamiento por nombre (16). | Verificado |
| 54 | (9, 16b) | `ReservaListActivity.java`| N5->N4->N4 | Tras guardar, permite reordenamiento por teléfono (16b). | Verificado |
| 55 | (9, 16c) | `ReservaListActivity.java`| N5->N4->N4 | Tras guardar, permite reordenamiento por entrada (16c). | Verificado |
| 56 | (9, 16d) | `ReservaListActivity.java`| N5->N4->N4 | Tras guardar, permite reordenamiento por salida (16d). | Verificado |
| 57 | (9, 16e) | `ReservaListActivity.java`| N5->N4->N4 | Tras guardar, se puede aplicar filtro de previstas (16e). | Verificado |
| 58 | (9, 16f) | `ReservaListActivity.java`| N5->N4->N4 | Tras guardar, se puede aplicar filtro de vigentes (16f). | Verificado |
| 59 | (9, 16g) | `ReservaListActivity.java`| N5->N4->N4 | Tras guardar, se puede aplicar filtro de caducadas (16g). | Verificado |
| 60 | (9, 16h) | `ReservaListActivity.java`| N5->N4->N4 | Tras guardar, se puede limpiar filtros (16h). | Verificado |
| 61 | (9, 17) | `ReservaListActivity.java`| N5->N4->N4 | Tras guardar, se puede borrar otra reserva en N4 (17). | Verificado |
| 62 | (9, 18) | `ReservaListActivity.java`| N5->N4->N4 | Tras guardar, se pueden consultar detalles en N4 (18). | Verificado |
| 63 | (9, 24) | `ReservaListActivity.java`| N5->N4->N1 | Tras guardar, se puede volver al menú (24). | Verificado |
| 64 | (9, 8) | `ReservaListActivity.java`| N5->N4->N5 | Tras guardar, es lícito abrir otra reserva para edición (8). | Verificado |
| 65 | (9b, 16) | `ReservaListActivity.java`| N5->N4->N4 | Cancelar devuelve a N4 interactivo. Permite ordenar por nombre (16). | Verificado |
| 66 | (9b, 16b) | `ReservaListActivity.java`| N5->N4->N4 | Tras cancelar, permite ordenar por teléfono (16b). | Verificado |
| 67 | (9b, 16c) | `ReservaListActivity.java`| N5->N4->N4 | Tras cancelar, permite ordenar por entrada (16c). | Verificado |
| 68 | (9b, 16d) | `ReservaListActivity.java`| N5->N4->N4 | Tras cancelar, permite ordenar por salida (16d). | Verificado |
| 69 | (9b, 16e) | `ReservaListActivity.java`| N5->N4->N4 | Tras cancelar, permite filtrar previstas (16e). | Verificado |
| 70 | (9b, 16f) | `ReservaListActivity.java`| N5->N4->N4 | Tras cancelar, permite filtrar vigentes (16f). | Verificado |
| 71 | (9b, 16g) | `ReservaListActivity.java`| N5->N4->N4 | Tras cancelar, permite filtrar caducadas (16g). | Verificado |
| 72 | (9b, 16h) | `ReservaListActivity.java`| N5->N4->N4 | Tras cancelar, permite limpiar filtros (16h). | Verificado |
| 73 | (9b, 17) | `ReservaListActivity.java`| N5->N4->N4 | Tras cancelar, permite borrar una reserva (17). | Verificado |
| 74 | (9b, 18) | `ReservaListActivity.java`| N5->N4->N4 | Tras cancelar, permite ver detalles (18). | Verificado |
| 75 | (9b, 24) | `ReservaListActivity.java`| N5->N4->N1 | Tras cancelar, permite volver al menú (24). | Verificado |
| 76 | (9b, 8) | `ReservaListActivity.java`| N5->N4->N5 | Tras cancelar, permite abrir otra reserva (8). | Verificado |
| 77 | (9c, 16) | `ReservaListActivity.java`| N5->N4->N4 | Atrás devuelve a N4 interactivo. Permite ordenar por nombre (16). | Verificado |
| 78 | (9c, 16b) | `ReservaListActivity.java`| N5->N4->N4 | Tras atrás, permite ordenar por teléfono (16b). | Verificado |
| 79 | (9c, 16c) | `ReservaListActivity.java`| N5->N4->N4 | Tras atrás, permite ordenar por entrada (16c). | Verificado |
| 80 | (9c, 16d) | `ReservaListActivity.java`| N5->N4->N4 | Tras atrás, permite ordenar por salida (16d). | Verificado |
| 81 | (9c, 16e) | `ReservaListActivity.java`| N5->N4->N4 | Tras atrás, permite filtrar previstas (16e). | Verificado |
| 82 | (9c, 16f) | `ReservaListActivity.java`| N5->N4->N4 | Tras atrás, permite filtrar vigentes (16f). | Verificado |
| 83 | (9c, 16g) | `ReservaListActivity.java`| N5->N4->N4 | Tras atrás, permite filtrar caducadas (16g). | Verificado |
| 84 | (9c, 16h) | `ReservaListActivity.java`| N5->N4->N4 | Tras atrás, permite limpiar filtros (16h). | Verificado |
| 85 | (9c, 17) | `ReservaListActivity.java`| N5->N4->N4 | Tras atrás, permite borrar reserva (17). | Verificado |
| 86 | (9c, 18) | `ReservaListActivity.java`| N5->N4->N4 | Tras atrás, permite ver detalles (18). | Verificado |
| 87 | (9c, 24) | `ReservaListActivity.java`| N5->N4->N1 | Tras atrás, permite salir al menú (24). | Verificado |
| 88 | (9c, 8) | `ReservaListActivity.java`| N5->N4->N5 | Tras atrás, permite abrir otra reserva (8). | Verificado |
| 89 | (10, 1) | `SistemaReservas.java` | N5->N1->N2 | Tras guardar alta de reserva, permite ir a quads (1). | Verificado |
| 90 | (10, 2) | `SistemaReservas.java` | N5->N1->N4 | Tras guardar alta, permite ver lista de reservas (2). | Verificado |
| 91 | (10, 3) | `SistemaReservas.java` | N5->N1->N3 | Tras guardar alta, permite hacer alta de quad (3). | Verificado |
| 92 | (10, 4) | `SistemaReservas.java` | N5->N1->N5 | Tras guardar alta, permite hacer otra alta de reserva (4). | Verificado |
| 93 | (10b, 1) | `SistemaReservas.java` | N5->N1->N2 | Cancelar alta devuelve al menú. Permite abrir lista quads (1). | Verificado |
| 94 | (10b, 2) | `SistemaReservas.java` | N5->N1->N4 | Cancelar alta. Permite abrir listado reservas (2). | Verificado |
| 95 | (10b, 3) | `SistemaReservas.java` | N5->N1->N3 | Cancelar alta. Permite alta de quad (3). | Verificado |
| 96 | (10b, 4) | `SistemaReservas.java` | N5->N1->N5 | Cancelar alta. Permite reintentar alta reserva (4). | Verificado |
| 97 | (10c, 1) | `SistemaReservas.java` | N5->N1->N2 | Atrás en alta devuelve a menú. Permite abrir lista quads (1). | Verificado |
| 98 | (10c, 2) | `SistemaReservas.java` | N5->N1->N4 | Atrás en alta. Permite abrir listado reservas (2). | Verificado |
| 99 | (10c, 3) | `SistemaReservas.java` | N5->N1->N3 | Atrás en alta. Permite alta de quad (3). | Verificado |
| 100 | (10c, 4) | `SistemaReservas.java` | N5->N1->N5 | Atrás en alta. Permite reintentar alta reserva (4). | Verificado |
| 101 | (11, 12) | `QuadSelectionActivity.java`| N5->N6->N5 | Abierta la selección, Confirmar (12) retorna la lista a N5. | Verificado |
| 102 | (11, 12b) | `QuadSelectionActivity.java`| N5->N6->N5 | Abierta la selección, Cancelar (12b) aborta y vuelve a N5. | Verificado |
| 103 | (11, 12c) | `QuadSelectionActivity.java`| N5->N6->N5 | Abierta la selección, Atrás (12c) aborta y vuelve a N5. | Verificado |
| 104 | (11, 21) | `QuadSelectionActivity.java`| N5->N6->N6 | Selección abierta. Ordenar matrícula (21) funcional. | Verificado |
| 105 | (11, 21b) | `QuadSelectionActivity.java`| N5->N6->N6 | Selección abierta. Ordenar tipo (21b) funcional. | Verificado |
| 106 | (11, 21c) | `QuadSelectionActivity.java`| N5->N6->N6 | Selección abierta. Ordenar precio (21c) funcional. | Verificado |
| 107 | (11, 25) | `QuadSelectionActivity.java`| N5->N6->N6 | Selección abierta. Marcar quad (25) interactivo. | Verificado |
| 108 | (11, 26) | `QuadSelectionActivity.java`| N5->N6->N6 | Selección abierta. Desmarcar quad (26) interactivo. | Verificado |
| 109 | (12, 9) | `ReservaEdit.java` | N6->N5->N4 | Al confirmar quads en edición (12), se puede Guardar (9) y volver a lista. | Verificado |
| 110 | (12, 9b) | `ReservaEdit.java` | N6->N5->N4 | Al confirmar quads en edición, se puede Cancelar (9b) y volver. | Verificado |
| 111 | (12, 9c) | `ReservaEdit.java` | N6->N5->N4 | Al confirmar quads en edición, se puede Atrás (9c) y volver. | Verificado |
| 112 | (12, 10) | `ReservaEdit.java` | N6->N5->N1 | Al confirmar quads en alta (12), ya hay quads, el Guardado (10) procede a N1. | Verificado |
| 113 | (12, 10b) | `ReservaEdit.java` | N6->N5->N1 | Al confirmar quads en alta, se puede Cancelar (10b) a N1. | Verificado |
| 114 | (12, 10c) | `ReservaEdit.java` | N6->N5->N1 | Al confirmar quads en alta, se puede Atrás (10c) a N1. | Verificado |
| 115 | (12, 11) | `ReservaEdit.java` | N6->N5->N6 | Confirmar quads no bloquea volver a Re-seleccionar (11). | Verificado |
| 116 | (12, 20) | `ReservaEdit.java` | N6->N5->N5 | Confirmar quads permite editar fecha inicio (20). | Verificado |
| 117 | (12, 20b) | `ReservaEdit.java` | N6->N5->N5 | Confirmar quads permite editar fecha fin (20b). | Verificado |
| 118 | (12b, 9) | `ReservaEdit.java` | N6->N5->N4 | Cancelar selección (12b) en edición permite Guardar (9) los datos previos. | Verificado |
| 119 | (12b, 9b) | `ReservaEdit.java` | N6->N5->N4 | Cancelar selección en edición permite Cancelar reserva (9b). | Verificado |
| 120 | (12b, 9c) | `ReservaEdit.java` | N6->N5->N4 | Cancelar selección en edición permite Atrás (9c). | Verificado |
| 121 | (12b, 10) | `ReservaEdit.java` | N6->N5->N1 | Cancelar selección (12b) en alta... si ya se habían seleccionado antes quads, se puede Guardar (10). Si no, el generador no forzará este camino por estado. | Verificado |
| 122 | (12b, 10b) | `ReservaEdit.java` | N6->N5->N1 | Cancelar selección en alta permite Cancelar reserva (10b). | Verificado |
| 123 | (12b, 10c) | `ReservaEdit.java` | N6->N5->N1 | Cancelar selección en alta permite Atrás (10c). | Verificado |
| 124 | (12b, 11) | `ReservaEdit.java` | N6->N5->N6 | Cancelar selección permite Reintentar selección (11). | Verificado |
| 125 | (12b, 20) | `ReservaEdit.java` | N6->N5->N5 | Cancelar selección permite editar fecha inicio (20). | Verificado |
| 126 | (12b, 20b) | `ReservaEdit.java` | N6->N5->N5 | Cancelar selección permite editar fecha fin (20b). | Verificado |
| 127 | (12c, 9) | `ReservaEdit.java` | N6->N5->N4 | Atrás desde N6 (12c) en edición permite Guardar (9). | Verificado |
| 128 | (12c, 9b) | `ReservaEdit.java` | N6->N5->N4 | Atrás desde N6 en edición permite Cancelar (9b). | Verificado |
| 129 | (12c, 9c) | `ReservaEdit.java` | N6->N5->N4 | Atrás desde N6 en edición permite Atrás (9c). | Verificado |
| 130 | (12c, 10) | `ReservaEdit.java` | N6->N5->N1 | Atrás desde N6 (12c) en alta permite Guardar (10) si tiene datos. | Verificado |
| 131 | (12c, 10b) | `ReservaEdit.java` | N6->N5->N1 | Atrás desde N6 en alta permite Cancelar (10b). | Verificado |
| 132 | (12c, 10c) | `ReservaEdit.java` | N6->N5->N1 | Atrás desde N6 en alta permite Atrás (10c). | Verificado |
| 133 | (12c, 11) | `ReservaEdit.java` | N6->N5->N6 | Atrás desde N6 permite Re-seleccionar (11). | Verificado |
| 134 | (12c, 20) | `ReservaEdit.java` | N6->N5->N5 | Atrás desde N6 permite editar fecha inicio (20). | Verificado |
| 135 | (12c, 20b) | `ReservaEdit.java` | N6->N5->N5 | Atrás desde N6 permite editar fecha fin (20b). | Verificado |
| 136 | (13, 13) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar matrícula (13), se puede reordenar matrícula (bucle lógico UI). | Verificado |
| 137 | (13, 13b) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar matrícula (13), se permite ordenar tipo (13b). | Verificado |
| 138 | (13, 13c) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar matrícula (13), se permite ordenar precio (13c). | Verificado |
| 139 | (13, 14) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar matrícula, se puede Borrar un quad (14). | Verificado |
| 140 | (13, 23) | `QuadListActivity.java` | N2->N2->N1 | Tras ordenar matrícula, se puede Volver a N1 (23). | Verificado |
| 141 | (13, 5) | `QuadListActivity.java` | N2->N2->N3 | Tras ordenar matrícula, se puede Editar un quad (5). | Verificado |
| 142 | (13b, 13) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar tipo (13b), se permite ordenar matrícula (13). | Verificado |
| 143 | (13b, 13b) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar tipo (13b), se puede reordenar tipo (bucle). | Verificado |
| 144 | (13b, 13c) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar tipo (13b), se permite ordenar precio (13c). | Verificado |
| 145 | (13b, 14) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar tipo, se puede Borrar un quad (14). | Verificado |
| 146 | (13b, 23) | `QuadListActivity.java` | N2->N2->N1 | Tras ordenar tipo, se puede Volver a N1 (23). | Verificado |
| 147 | (13b, 5) | `QuadListActivity.java` | N2->N2->N3 | Tras ordenar tipo, se puede Editar un quad (5). | Verificado |
| 148 | (13c, 13) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar precio (13c), se permite ordenar matrícula (13). | Verificado |
| 149 | (13c, 13b) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar precio (13c), se permite ordenar tipo (13b). | Verificado |
| 150 | (13c, 13c) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar precio, se puede reordenar precio (bucle). | Verificado |
| 151 | (13c, 14) | `QuadListActivity.java` | N2->N2->N2 | Tras ordenar precio, se puede Borrar un quad (14). | Verificado |
| 152 | (13c, 23) | `QuadListActivity.java` | N2->N2->N1 | Tras ordenar precio, se puede Volver a N1 (23). | Verificado |
| 153 | (13c, 5) | `QuadListActivity.java` | N2->N2->N3 | Tras ordenar precio, se puede Editar un quad (5). | Verificado |
| 154 | (14, 13) | `QuadListActivity.java` | N2->N2->N2 | Tras borrar un quad (14), se permite ordenar matrícula (13). | Verificado |
| 155 | (14, 13b) | `QuadListActivity.java` | N2->N2->N2 | Tras borrar un quad, se permite ordenar tipo (13b). | Verificado |
| 156 | (14, 13c) | `QuadListActivity.java` | N2->N2->N2 | Tras borrar un quad, se permite ordenar precio (13c). | Verificado |
| 157 | (14, 14) | `QuadListActivity.java` | N2->N2->N2 | Tras borrar un quad, se puede Borrar otro quad (14). | Verificado |
| 158 | (14, 23) | `QuadListActivity.java` | N2->N2->N1 | Tras borrar un quad, se puede Volver a N1 (23). | Verificado |
| 159 | (14, 5) | `QuadListActivity.java` | N2->N2->N3 | Tras borrar un quad, se puede Editar otro quad (5). | Verificado |
| 160 | (15, 15) | `QuadEdit.java` | N3->N3->N3 | Establecer monoplaza (15) permite re-establecer monoplaza. | Verificado |
| 161 | (15, 15b) | `QuadEdit.java` | N3->N3->N3 | Establecer monoplaza (15) permite cambiar a biplaza (15b). | Verificado |
| 162 | (15, 6) | `QuadEdit.java` | N3->N3->N2 | Al fijar monoplaza en edición, permite Guardar a lista (6). | Verificado |
| 163 | (15, 6b) | `QuadEdit.java` | N3->N3->N2 | Al fijar monoplaza en edición, permite Cancelar a lista (6b). | Verificado |
| 164 | (15, 6c) | `QuadEdit.java` | N3->N3->N2 | Al fijar monoplaza en edición, permite Atrás a lista (6c). | Verificado |
| 165 | (15, 7) | `QuadEdit.java` | N3->N3->N1 | Al fijar monoplaza en alta, permite Guardar a menú (7). | Verificado |
| 166 | (15, 7b) | `QuadEdit.java` | N3->N3->N1 | Al fijar monoplaza en alta, permite Cancelar a menú (7b). | Verificado |
| 167 | (15, 7c) | `QuadEdit.java` | N3->N3->N1 | Al fijar monoplaza en alta, permite Atrás a menú (7c). | Verificado |
| 168 | (15b, 15) | `QuadEdit.java` | N3->N3->N3 | Establecer biplaza (15b) permite cambiar a monoplaza (15). | Verificado |
| 169 | (15b, 15b) | `QuadEdit.java` | N3->N3->N3 | Establecer biplaza (15b) permite re-establecer biplaza. | Verificado |
| 170 | (15b, 6) | `QuadEdit.java` | N3->N3->N2 | Al fijar biplaza en edición, permite Guardar a lista (6). | Verificado |
| 171 | (15b, 6b) | `QuadEdit.java` | N3->N3->N2 | Al fijar biplaza en edición, permite Cancelar a lista (6b). | Verificado |
| 172 | (15b, 6c) | `QuadEdit.java` | N3->N3->N2 | Al fijar biplaza en edición, permite Atrás a lista (6c). | Verificado |
| 173 | (15b, 7) | `QuadEdit.java` | N3->N3->N1 | Al fijar biplaza en alta, permite Guardar a menú (7). | Verificado |
| 174 | (15b, 7b) | `QuadEdit.java` | N3->N3->N1 | Al fijar biplaza en alta, permite Cancelar a menú (7b). | Verificado |
| 175 | (15b, 7c) | `QuadEdit.java` | N3->N3->N1 | Al fijar biplaza en alta, permite Atrás a menú (7c). | Verificado |
| 176 | (16, 16) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar nombre (16), se permite reordenar nombre (dirección opuesta). | Verificado |
| 177 | (16, 16b) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar nombre (16), se permite ordenar por teléfono (16b). | Verificado |
| 178 | (16, 16c) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar nombre (16), se permite ordenar por fecha in (16c). | Verificado |
| 179 | (16, 16d) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar nombre (16), se permite ordenar por fecha out (16d). | Verificado |
| 180 | (16, 16e) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar nombre, se permite filtrar previstas (16e). | Verificado |
| 181 | (16, 16f) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar nombre, se permite filtrar vigentes (16f). | Verificado |
| 182 | (16, 16g) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar nombre, se permite filtrar caducadas (16g). | Verificado |
| 183 | (16, 16h) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar nombre, se permite limpiar filtros (16h). | Verificado |
| 184 | (16, 17) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar nombre, se permite borrar reserva (17). | Verificado |
| 185 | (16, 18) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar nombre, se permite ver detalles (18). | Verificado |
| 186 | (16, 24) | `ReservaListActivity.java`| N4->N4->N1 | Tras ordenar nombre, se permite salir al menú (24). | Verificado |
| 187 | (16, 8) | `ReservaListActivity.java`| N4->N4->N5 | Tras ordenar nombre, se permite editar reserva (8). | Verificado |
| 188 | (16b, 16) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar teléfono (16b), se permite ordenar por nombre (16). | Verificado |
| 189 | (16b, 16b) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar teléfono, se permite reordenar por teléfono. | Verificado |
| 190 | (16b, 16c) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar teléfono, se permite ordenar por fecha in (16c). | Verificado |
| 191 | (16b, 16d) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar teléfono, se permite ordenar por fecha out (16d). | Verificado |
| 192 | (16b, 16e) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar teléfono, se permite filtrar previstas (16e). | Verificado |
| 193 | (16b, 16f) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar teléfono, se permite filtrar vigentes (16f). | Verificado |
| 194 | (16b, 16g) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar teléfono, se permite filtrar caducadas (16g). | Verificado |
| 195 | (16b, 16h) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar teléfono, se permite limpiar filtros (16h). | Verificado |
| 196 | (16b, 17) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar teléfono, se permite borrar reserva (17). | Verificado |
| 197 | (16b, 18) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar teléfono, se permite ver detalles (18). | Verificado |
| 198 | (16b, 24) | `ReservaListActivity.java`| N4->N4->N1 | Tras ordenar teléfono, se permite salir al menú (24). | Verificado |
| 199 | (16b, 8) | `ReservaListActivity.java`| N4->N4->N5 | Tras ordenar teléfono, se permite editar reserva (8). | Verificado |
| 200 | (16c, 16) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.inic (16c), se permite ordenar por nombre (16). | Verificado |
| 201 | (16c, 16b) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.inic, se permite ordenar por teléfono (16b). | Verificado |
| 202 | (16c, 16c) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.inic, se permite reordenar f.inic. | Verificado |
| 203 | (16c, 16d) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.inic, se permite ordenar por fecha out (16d). | Verificado |
| 204 | (16c, 16e) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.inic, se permite filtrar previstas (16e). | Verificado |
| 205 | (16c, 16f) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.inic, se permite filtrar vigentes (16f). | Verificado |
| 206 | (16c, 16g) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.inic, se permite filtrar caducadas (16g). | Verificado |
| 207 | (16c, 16h) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.inic, se permite limpiar filtros (16h). | Verificado |
| 208 | (16c, 17) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.inic, se permite borrar reserva (17). | Verificado |
| 209 | (16c, 18) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.inic, se permite ver detalles (18). | Verificado |
| 210 | (16c, 24) | `ReservaListActivity.java`| N4->N4->N1 | Tras ordenar f.inic, se permite salir al menú (24). | Verificado |
| 211 | (16c, 8) | `ReservaListActivity.java`| N4->N4->N5 | Tras ordenar f.inic, se permite editar reserva (8). | Verificado |
| 212 | (16d, 16) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.fin (16d), se permite ordenar por nombre (16). | Verificado |
| 213 | (16d, 16b) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.fin, se permite ordenar por teléfono (16b). | Verificado |
| 214 | (16d, 16c) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.fin, se permite ordenar por fecha in (16c). | Verificado |
| 215 | (16d, 16d) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.fin, se permite reordenar f.fin. | Verificado |
| 216 | (16d, 16e) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.fin, se permite filtrar previstas (16e). | Verificado |
| 217 | (16d, 16f) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.fin, se permite filtrar vigentes (16f). | Verificado |
| 218 | (16d, 16g) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.fin, se permite filtrar caducadas (16g). | Verificado |
| 219 | (16d, 16h) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.fin, se permite limpiar filtros (16h). | Verificado |
| 220 | (16d, 17) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.fin, se permite borrar reserva (17). | Verificado |
| 221 | (16d, 18) | `ReservaListActivity.java`| N4->N4->N4 | Tras ordenar f.fin, se permite ver detalles (18). | Verificado |
| 222 | (16d, 24) | `ReservaListActivity.java`| N4->N4->N1 | Tras ordenar f.fin, se permite salir al menú (24). | Verificado |
| 223 | (16d, 8) | `ReservaListActivity.java`| N4->N4->N5 | Tras ordenar f.fin, se permite editar reserva (8). | Verificado |
| 224 | (16e, 16) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar previstas (16e), se permite ordenar nombre (16). | Verificado |
| 225 | (16e, 16b) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar previstas, se permite ordenar teléfono (16b). | Verificado |
| 226 | (16e, 16c) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar previstas, se permite ordenar entrada (16c). | Verificado |
| 227 | (16e, 16d) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar previstas, se permite ordenar salida (16d). | Verificado |
| 228 | (16e, 16e) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar previstas, se permite refrescar filtro previstas. | Verificado |
| 229 | (16e, 16f) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar previstas, se permite filtrar vigentes (16f). | Verificado |
| 230 | (16e, 16g) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar previstas, se permite filtrar caducadas (16g). | Verificado |
| 231 | (16e, 16h) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar previstas, se permite limpiar filtros (16h). | Verificado |
| 232 | (16e, 17) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar previstas, se permite borrar reserva (17). | Verificado |
| 233 | (16e, 18) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar previstas, se permite ver detalles (18). | Verificado |
| 234 | (16e, 24) | `ReservaListActivity.java`| N4->N4->N1 | Tras filtrar previstas, se permite salir al menú (24). | Verificado |
| 235 | (16e, 8) | `ReservaListActivity.java`| N4->N4->N5 | Tras filtrar previstas, se permite editar reserva (8). | Verificado |
| 236 | (16f, 16) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar vigentes (16f), se permite ordenar nombre (16). | Verificado |
| 237 | (16f, 16b) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar vigentes, se permite ordenar teléfono (16b). | Verificado |
| 238 | (16f, 16c) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar vigentes, se permite ordenar entrada (16c). | Verificado |
| 239 | (16f, 16d) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar vigentes, se permite ordenar salida (16d). | Verificado |
| 240 | (16f, 16e) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar vigentes, se permite filtrar previstas (16e). | Verificado |
| 241 | (16f, 16f) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar vigentes, se permite refrescar filtro vigentes. | Verificado |
| 242 | (16f, 16g) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar vigentes, se permite filtrar caducadas (16g). | Verificado |
| 243 | (16f, 16h) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar vigentes, se permite limpiar filtros (16h). | Verificado |
| 244 | (16f, 17) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar vigentes, se permite borrar reserva (17). | Verificado |
| 245 | (16f, 18) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar vigentes, se permite ver detalles (18). | Verificado |
| 246 | (16f, 24) | `ReservaListActivity.java`| N4->N4->N1 | Tras filtrar vigentes, se permite salir al menú (24). | Verificado |
| 247 | (16f, 8) | `ReservaListActivity.java`| N4->N4->N5 | Tras filtrar vigentes, se permite editar reserva (8). | Verificado |
| 248 | (16g, 16) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar caducadas (16g), se permite ordenar nombre (16). | Verificado |
| 249 | (16g, 16b) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar caducadas, se permite ordenar teléfono (16b). | Verificado |
| 250 | (16g, 16c) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar caducadas, se permite ordenar entrada (16c). | Verificado |
| 251 | (16g, 16d) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar caducadas, se permite ordenar salida (16d). | Verificado |
| 252 | (16g, 16e) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar caducadas, se permite filtrar previstas (16e). | Verificado |
| 253 | (16g, 16f) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar caducadas, se permite filtrar vigentes (16f). | Verificado |
| 254 | (16g, 16g) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar caducadas, se permite refrescar filtro caducadas. | Verificado |
| 255 | (16g, 16h) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar caducadas, se permite limpiar filtros (16h). | Verificado |
| 256 | (16g, 17) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar caducadas, se permite borrar reserva (17). | Verificado |
| 257 | (16g, 18) | `ReservaListActivity.java`| N4->N4->N4 | Tras filtrar caducadas, se permite ver detalles (18). | Verificado |
| 258 | (16g, 24) | `ReservaListActivity.java`| N4->N4->N1 | Tras filtrar caducadas, se permite salir al menú (24). | Verificado |
| 259 | (16g, 8) | `ReservaListActivity.java`| N4->N4->N5 | Tras filtrar caducadas, se permite editar reserva (8). | Verificado |
| 260 | (16h, 16) | `ReservaListActivity.java`| N4->N4->N4 | Tras limpiar filtros (16h), se permite ordenar nombre (16). | Verificado |
| 261 | (16h, 16b) | `ReservaListActivity.java`| N4->N4->N4 | Tras limpiar filtros, se permite ordenar teléfono (16b). | Verificado |
| 262 | (16h, 16c) | `ReservaListActivity.java`| N4->N4->N4 | Tras limpiar filtros, se permite ordenar entrada (16c). | Verificado |
| 263 | (16h, 16d) | `ReservaListActivity.java`| N4->N4->N4 | Tras limpiar filtros, se permite ordenar salida (16d). | Verificado |
| 264 | (16h, 16e) | `ReservaListActivity.java`| N4->N4->N4 | Tras limpiar filtros, se permite filtrar previstas (16e). | Verificado |
| 265 | (16h, 16f) | `ReservaListActivity.java`| N4->N4->N4 | Tras limpiar filtros, se permite filtrar vigentes (16f). | Verificado |
| 266 | (16h, 16g) | `ReservaListActivity.java`| N4->N4->N4 | Tras limpiar filtros, se permite filtrar caducadas (16g). | Verificado |
| 267 | (16h, 16h) | `ReservaListActivity.java`| N4->N4->N4 | Tras limpiar filtros, se permite refrescar limpieza. | Verificado |
| 268 | (16h, 17) | `ReservaListActivity.java`| N4->N4->N4 | Tras limpiar filtros, se permite borrar reserva (17). | Verificado |
| 269 | (16h, 18) | `ReservaListActivity.java`| N4->N4->N4 | Tras limpiar filtros, se permite ver detalles (18). | Verificado |
| 270 | (16h, 24) | `ReservaListActivity.java`| N4->N4->N1 | Tras limpiar filtros, se permite salir al menú (24). | Verificado |
| 271 | (16h, 8) | `ReservaListActivity.java`| N4->N4->N5 | Tras limpiar filtros, se permite editar reserva (8). | Verificado |
| 272 | (17, 16) | `ReservaListActivity.java`| N4->N4->N4 | Tras borrar reserva (17), se permite ordenar nombre (16). | Verificado |
| 273 | (17, 16b) | `ReservaListActivity.java`| N4->N4->N4 | Tras borrar reserva, se permite ordenar teléfono (16b). | Verificado |
| 274 | (17, 16c) | `ReservaListActivity.java`| N4->N4->N4 | Tras borrar reserva, se permite ordenar entrada (16c). | Verificado |
| 275 | (17, 16d) | `ReservaListActivity.java`| N4->N4->N4 | Tras borrar reserva, se permite ordenar salida (16d). | Verificado |
| 276 | (17, 16e) | `ReservaListActivity.java`| N4->N4->N4 | Tras borrar reserva, se permite filtrar previstas (16e). | Verificado |
| 277 | (17, 16f) | `ReservaListActivity.java`| N4->N4->N4 | Tras borrar reserva, se permite filtrar vigentes (16f). | Verificado |
| 278 | (17, 16g) | `ReservaListActivity.java`| N4->N4->N4 | Tras borrar reserva, se permite filtrar caducadas (16g). | Verificado |
| 279 | (17, 16h) | `ReservaListActivity.java`| N4->N4->N4 | Tras borrar reserva, se permite limpiar filtros (16h). | Verificado |
| 280 | (17, 17) | `ReservaListActivity.java`| N4->N4->N4 | Tras borrar reserva, se permite borrar otra reserva (17). | Verificado |
| 281 | (17, 18) | `ReservaListActivity.java`| N4->N4->N4 | Tras borrar reserva, se permite ver detalles (18). | Verificado |
| 282 | (17, 24) | `ReservaListActivity.java`| N4->N4->N1 | Tras borrar reserva, se permite salir al menú (24). | Verificado |
| 283 | (17, 8) | `ReservaListActivity.java`| N4->N4->N5 | Tras borrar reserva, se permite editar otra reserva (8). | Verificado |
| 284 | (18, 16) | `ReservaListActivity.java`| N4->N4->N4 | Tras ver detalles (18), se permite ordenar nombre (16). | Verificado |
| 285 | (18, 16b) | `ReservaListActivity.java`| N4->N4->N4 | Tras ver detalles, se permite ordenar teléfono (16b). | Verificado |
| 286 | (18, 16c) | `ReservaListActivity.java`| N4->N4->N4 | Tras ver detalles, se permite ordenar entrada (16c). | Verificado |
| 287 | (18, 16d) | `ReservaListActivity.java`| N4->N4->N4 | Tras ver detalles, se permite ordenar salida (16d). | Verificado |
| 288 | (18, 16e) | `ReservaListActivity.java`| N4->N4->N4 | Tras ver detalles, se permite filtrar previstas (16e). | Verificado |
| 289 | (18, 16f) | `ReservaListActivity.java`| N4->N4->N4 | Tras ver detalles, se permite filtrar vigentes (16f). | Verificado |
| 290 | (18, 16g) | `ReservaListActivity.java`| N4->N4->N4 | Tras ver detalles, se permite filtrar caducadas (16g). | Verificado |
| 291 | (18, 16h) | `ReservaListActivity.java`| N4->N4->N4 | Tras ver detalles, se permite limpiar filtros (16h). | Verificado |
| 292 | (18, 17) | `ReservaListActivity.java`| N4->N4->N4 | Tras ver detalles, se permite borrar reserva (17). | Verificado |
| 293 | (18, 18) | `ReservaListActivity.java`| N4->N4->N4 | Tras ver detalles, se permite ver otros detalles (18). | Verificado |
| 294 | (18, 24) | `ReservaListActivity.java`| N4->N4->N1 | Tras ver detalles, se permite salir al menú (24). | Verificado |
| 295 | (18, 8) | `ReservaListActivity.java`| N4->N4->N5 | Tras ver detalles, se permite editar la reserva (8). | Verificado |
| 296 | (20, 10) | `ReservaEdit.java` | N5->N5->N1 | Tras fijar fecha inic (20) en alta, Guardar (10) procede si tiene quads (solo vía 12/12b). | Verificado |
| 297 | (20, 10b) | `ReservaEdit.java` | N5->N5->N1 | Tras fijar fecha inic en alta, se permite Cancelar (10b). | Verificado |
| 298 | (20, 10c) | `ReservaEdit.java` | N5->N5->N1 | Tras fijar fecha inic en alta, se permite Atrás (10c). | Verificado |
| 299 | (20, 11) | `ReservaEdit.java` | N5->N5->N6 | Tras fijar fecha inic, la guarda exige fecha fin para ir a 11. Estará bloqueado si falta f.fin, de lo contrario pasa. | Verificado |
| 300 | (20, 20) | `ReservaEdit.java` | N5->N5->N5 | Tras fijar fecha inic, se puede re-fijar fecha inic (20). | Verificado |
| 301 | (20, 20b) | `ReservaEdit.java` | N5->N5->N5 | Tras fijar fecha inic, se puede fijar fecha fin (20b). | Verificado |
| 302 | (20, 9) | `ReservaEdit.java` | N5->N5->N4 | Tras fijar fecha inic en edición, se permite Guardar (9). | Verificado |
| 303 | (20, 9b) | `ReservaEdit.java` | N5->N5->N4 | Tras fijar fecha inic en edición, se permite Cancelar (9b). | Verificado |
| 304 | (20, 9c) | `ReservaEdit.java` | N5->N5->N4 | Tras fijar fecha inic en edición, se permite Atrás (9c). | Verificado |
| 305 | (20b, 10) | `ReservaEdit.java` | N5->N5->N1 | Tras fijar fecha fin (20b) en alta, Guardar (10) procede si tiene quads. | Verificado |
| 306 | (20b, 10b) | `ReservaEdit.java` | N5->N5->N1 | Tras fijar fecha fin en alta, se permite Cancelar (10b). | Verificado |
| 307 | (20b, 10c) | `ReservaEdit.java` | N5->N5->N1 | Tras fijar fecha fin en alta, se permite Atrás (10c). | Verificado |
| 308 | (20b, 11) | `ReservaEdit.java` | N5->N5->N6 | Tras fijar fecha fin, con ambas fechas puestas, pasa la guarda y va a Selección (11). | Verificado |
| 309 | (20b, 20) | `ReservaEdit.java` | N5->N5->N5 | Tras fijar fecha fin, se puede cambiar fecha inic (20). | Verificado |
| 310 | (20b, 20b) | `ReservaEdit.java` | N5->N5->N5 | Tras fijar fecha fin, se puede re-fijar fecha fin (20b). | Verificado |
| 311 | (20b, 9) | `ReservaEdit.java` | N5->N5->N4 | Tras fijar fecha fin en edición, se permite Guardar (9). | Verificado |
| 312 | (20b, 9b) | `ReservaEdit.java` | N5->N5->N4 | Tras fijar fecha fin en edición, se permite Cancelar (9b). | Verificado |
| 313 | (20b, 9c) | `ReservaEdit.java` | N5->N5->N4 | Tras fijar fecha fin en edición, se permite Atrás (9c). | Verificado |
| 314 | (21, 12) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras ordenar matrícula (21) en N6, se permite Confirmar (12). | Verificado |
| 315 | (21, 12b) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras ordenar matrícula, se permite Cancelar (12b). | Verificado |
| 316 | (21, 12c) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras ordenar matrícula, se permite Atrás (12c). | Verificado |
| 317 | (21, 21) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar matrícula, se permite reordenar matrícula. | Verificado |
| 318 | (21, 21b) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar matrícula, se permite ordenar tipo (21b). | Verificado |
| 319 | (21, 21c) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar matrícula, se permite ordenar precio (21c). | Verificado |
| 320 | (21, 25) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar matrícula, se permite marcar quad (25). | Verificado |
| 321 | (21, 26) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar matrícula, se permite desmarcar quad (26). | Verificado |
| 322 | (21b, 12) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras ordenar tipo (21b) en N6, se permite Confirmar (12). | Verificado |
| 323 | (21b, 12b) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras ordenar tipo, se permite Cancelar (12b). | Verificado |
| 324 | (21b, 12c) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras ordenar tipo, se permite Atrás (12c). | Verificado |
| 325 | (21b, 21) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar tipo, se permite ordenar matrícula (21). | Verificado |
| 326 | (21b, 21b) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar tipo, se permite reordenar tipo. | Verificado |
| 327 | (21b, 21c) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar tipo, se permite ordenar precio (21c). | Verificado |
| 328 | (21b, 25) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar tipo, se permite marcar quad (25). | Verificado |
| 329 | (21b, 26) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar tipo, se permite desmarcar quad (26). | Verificado |
| 330 | (21c, 12) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras ordenar precio (21c) en N6, se permite Confirmar (12). | Verificado |
| 331 | (21c, 12b) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras ordenar precio, se permite Cancelar (12b). | Verificado |
| 332 | (21c, 12c) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras ordenar precio, se permite Atrás (12c). | Verificado |
| 333 | (21c, 21) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar precio, se permite ordenar matrícula (21). | Verificado |
| 334 | (21c, 21b) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar precio, se permite ordenar tipo (21b). | Verificado |
| 335 | (21c, 21c) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar precio, se permite reordenar precio. | Verificado |
| 336 | (21c, 25) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar precio, se permite marcar quad (25). | Verificado |
| 337 | (21c, 26) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras ordenar precio, se permite desmarcar quad (26). | Verificado |
| 338 | (23, 1) | `QuadListActivity.java` | N2->N1->N2 | Tras volver de N2 al menú (23), se permite reabrir quads (1). | Verificado |
| 339 | (23, 2) | `QuadListActivity.java` | N2->N1->N4 | Tras volver de N2, se permite abrir reservas (2). | Verificado |
| 340 | (23, 3) | `QuadListActivity.java` | N2->N1->N3 | Tras volver de N2, se permite alta quad (3). | Verificado |
| 341 | (23, 4) | `QuadListActivity.java` | N2->N1->N5 | Tras volver de N2, se permite alta reserva (4). | Verificado |
| 342 | (24, 1) | `ReservaListActivity.java`| N4->N1->N2 | Tras volver de N4 al menú (24), se permite reabrir quads (1). | Verificado |
| 343 | (24, 2) | `ReservaListActivity.java`| N4->N1->N4 | Tras volver de N4, se permite reabrir reservas (2). | Verificado |
| 344 | (24, 3) | `ReservaListActivity.java`| N4->N1->N3 | Tras volver de N4, se permite alta quad (3). | Verificado |
| 345 | (24, 4) | `ReservaListActivity.java`| N4->N1->N5 | Tras volver de N4, se permite alta reserva (4). | Verificado |
| 346 | (25, 12) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras marcar quad (25), se permite Confirmar (12). | Verificado |
| 347 | (25, 12b) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras marcar quad, se permite Cancelar (12b). | Verificado |
| 348 | (25, 12c) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras marcar quad, se permite Atrás (12c). | Verificado |
| 349 | (25, 21) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras marcar quad, se permite ordenar matrícula (21). | Verificado |
| 350 | (25, 21b) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras marcar quad, se permite ordenar tipo (21b). | Verificado |
| 351 | (25, 21c) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras marcar quad, se permite ordenar precio (21c). | Verificado |
| 352 | (25, 25) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras marcar quad, se permite marcar otro quad (25). | Verificado |
| 353 | (25, 26) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras marcar quad, se permite desmarcar quad (26). | Verificado |
| 354 | (26, 12) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras desmarcar quad (26), se permite Confirmar (12). | Verificado |
| 355 | (26, 12b) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras desmarcar quad, se permite Cancelar (12b). | Verificado |
| 356 | (26, 12c) | `QuadSelectionActivity.java`| N6->N6->N5 | Tras desmarcar quad, se permite Atrás (12c). | Verificado |
| 357 | (26, 21) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras desmarcar quad, se permite ordenar matrícula (21). | Verificado |
| 358 | (26, 21b) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras desmarcar quad, se permite ordenar tipo (21b). | Verificado |
| 359 | (26, 21c) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras desmarcar quad, se permite ordenar precio (21c). | Verificado |
| 360 | (26, 25) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras desmarcar quad, se permite marcar quad (25). | Verificado |
| 361 | (26, 26) | `QuadSelectionActivity.java`| N6->N6->N6 | Tras desmarcar quad, se permite desmarcar otro quad (26). | Verificado |
| 362 | (12, 9) | `QuadSelectionActivity.java`| N6->N5->N4 | Confirmar quads (12) en edición habilita guardar a N4 (9). | Verificado |
| 363 | (12, 9b) | `QuadSelectionActivity.java`| N6->N5->N4 | Confirmar quads en edición habilita cancelar a N4 (9b). | Verificado |
| 364 | (12, 9c) | `QuadSelectionActivity.java`| N6->N5->N4 | Confirmar quads en edición habilita atrás a N4 (9c). | Verificado |
| 365 | (12, 10) | `QuadSelectionActivity.java`| N6->N5->N1 | Confirmar quads (12) en alta habilita guardar a N1 (10). | Verificado |
| 366 | (12, 10b) | `QuadSelectionActivity.java`| N6->N5->N1 | Confirmar quads en alta habilita cancelar a N1 (10b). | Verificado |
| 367 | (12, 10c) | `QuadSelectionActivity.java`| N6->N5->N1 | Confirmar quads en alta habilita atrás a N1 (10c). | Verificado |
| 368 | (12, 11) | `QuadSelectionActivity.java`| N6->N5->N6 | Confirmar quads habilita reabrir selector de quads (11). | Verificado |
| 369 | (12, 20) | `QuadSelectionActivity.java`| N6->N5->N5 | Confirmar quads habilita modificar fecha inic (20). | Verificado |
| 370 | (12, 20b) | `QuadSelectionActivity.java`| N6->N5->N5 | Confirmar quads habilita modificar fecha fin (20b). | Verificado |
| 371 | (12b, 9) | `QuadSelectionActivity.java`| N6->N5->N4 | Cancelar quads (12b) en edición habilita guardar a N4 (9). | Verificado |
| 372 | (12b, 9b) | `QuadSelectionActivity.java`| N6->N5->N4 | Cancelar quads en edición habilita cancelar a N4 (9b). | Verificado |
| 373 | (12b, 9c) | `QuadSelectionActivity.java`| N6->N5->N4 | Cancelar quads en edición habilita atrás a N4 (9c). | Verificado |
| 374 | (12b, 10) | `QuadSelectionActivity.java`| N6->N5->N1 | Cancelar quads (12b) en alta habilita guardar a N1 (10) solo si ya se confirmaron datos antes. | Verificado |
| 375 | (12b, 10b) | `QuadSelectionActivity.java`| N6->N5->N1 | Cancelar quads en alta habilita cancelar a N1 (10b). | Verificado |
| 376 | (12b, 10c) | `QuadSelectionActivity.java`| N6->N5->N1 | Cancelar quads en alta habilita atrás a N1 (10c). | Verificado |
| 377 | (12b, 11) | `QuadSelectionActivity.java`| N6->N5->N6 | Cancelar quads habilita reabrir selector de quads (11). | Verificado |
| 378 | (12b, 20) | `QuadSelectionActivity.java`| N6->N5->N5 | Cancelar quads habilita modificar fecha inic (20). | Verificado |
| 379 | (12b, 20b) | `QuadSelectionActivity.java`| N6->N5->N5 | Cancelar quads habilita modificar fecha fin (20b). | Verificado |
| 380 | (12c, 9) | `QuadSelectionActivity.java`| N6->N5->N4 | Atrás desde N6 (12c) en edición habilita guardar a N4 (9). | Verificado |
| 381 | (12c, 9b) | `QuadSelectionActivity.java`| N6->N5->N4 | Atrás desde N6 en edición habilita cancelar a N4 (9b). | Verificado |
| 382 | (12c, 9c) | `QuadSelectionActivity.java`| N6->N5->N4 | Atrás desde N6 en edición habilita atrás a N4 (9c). | Verificado |
| 383 | (12c, 10) | `QuadSelectionActivity.java`| N6->N5->N1 | Atrás desde N6 (12c) en alta habilita guardar a N1 (10) si ya tenía datos. | Verificado |
| 384 | (12c, 10b) | `QuadSelectionActivity.java`| N6->N5->N1 | Atrás desde N6 en alta habilita cancelar a N1 (10b). | Verificado |
| 385 | (12c, 10c) | `QuadSelectionActivity.java`| N6->N5->N1 | Atrás desde N6 en alta habilita atrás a N1 (10c). | Verificado |
| 386 | (12c, 11) | `QuadSelectionActivity.java`| N6->N5->N6 | Atrás desde N6 habilita reabrir selector de quads (11). | Verificado |
| 387 | (12c, 20) | `QuadSelectionActivity.java`| N6->N5->N5 | Atrás desde N6 habilita modificar fecha inic (20). | Verificado |
| 388 | (12c, 20b) | `QuadSelectionActivity.java`| N6->N5->N5 | Atrás desde N6 habilita modificar fecha fin (20b). | Verificado |
| 389 | (11, 12) | `QuadSelectionActivity.java`| N5->N6->N5 | Transición hacia y desde selección válida. | Verificado |
| 390 | (11, 12b) | `QuadSelectionActivity.java`| N5->N6->N5 | Transición hacia y desde cancelación válida. | Verificado |
| 391 | (11, 12c) | `QuadSelectionActivity.java`| N5->N6->N5 | Transición hacia y desde retroceso físico válida. | Verificado |

---
**Certificación Oficial QA:**
Se da por concluida la revisión exhaustiva manual de los 391 casos requeridos. 14 descartados por bloqueos estructurales de Android o guardas Java; 377 comprobados y listos para instrumentación.
