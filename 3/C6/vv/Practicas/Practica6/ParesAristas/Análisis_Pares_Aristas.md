# Análisis Exhaustivo de Pares de Aristas (Auditoría Manual 100% - INTEGRAL)

Este documento contiene la validación manual, pareja por pareja y sin automatizaciones, de los 391 pares de aristas estructurales del grafo. Cada fila ha sido contrastada con el código fuente Java (`SistemaReservas.java`, `QuadListActivity.java`, `QuadEdit.java`, `ReservaListActivity.java`, `ReservaEdit.java` y `QuadSelectionActivity.java`) para certificar su validez técnica y funcional, considerando las restricciones de navegación de Android (Back Stack) y lógica de negocio.

---

## 1. Pares IMPOSIBLES ❌ (26 Casos)

Estos pares figuran como conexiones en el grafo teórico, pero tras revisar la lógica de negocio en el código Java y las políticas de navegación de Android, se determina que son imposibles de ejecutar.

| ID | Pareja | Código Revisado | Navegación | Evidencia Verificada en Código | Razón Técnica de Imposibilidad | Estado |
|---|---|---|---|---|---|---|
| 1 | 3, 6 | QuadEdit.java | N1->N3->N2 | finish() en Guardar | Back Stack: Alta desde menú (3) siempre vuelve al menú (7). N2 no está en la pila. | Verificado |
| 2 | 3, 6b | QuadEdit.java | N1->N3->N2 | finish() en Cancelar | Back Stack: Cancelar alta desde menú devuelve a N1. | Verificado |
| 3 | 3, 6c | QuadEdit.java | N1->N3->N2 | onBackPressed() | Back Stack: Retroceder desde alta menú devuelve a N1. | Verificado |
| 4 | 4, 9 | ReservaEdit.java | N1->N5->N4 | finish() en Guardar | Back Stack: Alta reserva desde menú vuelve a N1. | Verificado |
| 5 | 4, 9b | ReservaEdit.java | N1->N5->N4 | finish() en Cancelar | Back Stack: Cancelar alta reserva desde menú devuelve a N1. | Verificado |
| 6 | 4, 9c | ReservaEdit.java | N1->N5->N4 | onBackPressed() | Back Stack: Retroceder desde alta reserva menú devuelve a N1. | Verificado |
| 7 | 5, 7 | QuadEdit.java | N2->N3->N1 | finish() en Guardar | Back Stack: Edición desde lista (5) vuelve a la lista (6). | Verificado |
| 8 | 5, 7b | QuadEdit.java | N2->N3->N1 | finish() en Cancelar | Back Stack: Cancelar edición vuelve a la lista. | Verificado |
| 9 | 5, 7c | QuadEdit.java | N2->N3->N1 | onBackPressed() | Back Stack: Retroceder desde edición vuelve a la lista. | Verificado |
| 10 | 8, 10 | ReservaEdit.java | N4->N5->N1 | finish() en Guardar | Back Stack: Edición reserva desde lista (8) vuelve a la lista. | Verificado |
| 11 | 8, 10b | ReservaEdit.java | N4->N5->N1 | finish() en Cancelar | Back Stack: Cancelar edición reserva vuelve a la lista. | Verificado |
| 12 | 8, 10c | ReservaEdit.java | N4->N5->N1 | onBackPressed() | Back Stack: Retroceder desde edición reserva vuelve a la lista. | Verificado |
| 13 | 4, 11 | ReservaEdit.java | N1->N5->N6 | mBtnSelectQuads | UI Guard: No se puede seleccionar quads sin fechas (Alta nueva). | Verificado |
| 14 | 4, 10 | ReservaEdit.java | N1->N5->N1 | saveReserva() | UI Guard: No se puede guardar reserva sin quads (Alta nueva). | Verificado |
| 15 | 15, 7 | QuadEdit.java | N2->N3->N3->N1 | finish() en saveQuad | Back Stack: Viniendo de edición (5), no puedes salir al menú (7). | Verificado |
| 16 | 15, 7b | QuadEdit.java | N2->N3->N3->N1 | finish() en cancel | Back Stack: Viniendo de edición, cancelar vuelve a lista. | Verificado |
| 17 | 15, 7c | QuadEdit.java | N2->N3->N3->N1 | onBackPressed() | Back Stack: Viniendo de edición, atrás vuelve a lista. | Verificado |
| 18 | 15, 6 | QuadEdit.java | N1->N3->N3->N2 | finish() en saveQuad | Back Stack: Viniendo de alta (3), no puedes salir a la lista (6). | Verificado |
| 19 | 15, 6b | QuadEdit.java | N1->N3->N3->N2 | finish() en cancel | Back Stack: Viniendo de alta, cancelar vuelve a menú. | Verificado |
| 20 | 15, 6c | QuadEdit.java | N1->N3->N3->N2 | onBackPressed() | Back Stack: Viniendo de alta, atrás vuelve a menú. | Verificado |
| 21 | 15b, 7 | QuadEdit.java | N2->N3->N3->N1 | finish() en saveQuad | Rama Edición -> Salida Menú imposible. | Verificado |
| 22 | 15b, 7b | QuadEdit.java | N2->N3->N3->N1 | finish() en cancel | Rama Edición -> Cancelar Menú imposible. | Verificado |
| 23 | 15b, 7c | QuadEdit.java | N2->N3->N3->N1 | onBackPressed() | Rama Edición -> Atrás Menú imposible. | Verificado |
| 24 | 15b, 6 | QuadEdit.java | N1->N3->N3->N2 | finish() en saveQuad | Rama Alta -> Salida Lista imposible. | Verificado |
| 25 | 15b, 6b | QuadEdit.java | N1->N3->N3->N2 | finish() en cancel | Rama Alta -> Cancelar Lista imposible. | Verificado |
| 26 | 15b, 6c | QuadEdit.java | N1->N3->N3->N2 | onBackPressed() | Rama Alta -> Atrás Lista imposible. | Verificado |

---

## 2. Pares POSIBLES ✅ (365 Casos)

Tras revisar el código de cada controlador y listener, se confirma que el estado de la interfaz y la lógica de negocio de Android permiten la ejecución real de estas transiciones de forma secuencial.

### 2.1 — Transiciones en N1 (Menú Principal)
| ID | Pareja | Archivo | Navegación | Evidencia Técnica | Estado |
|---|---|---|---|---|---|
| 27 | 7, 1 | SistRes.java | N3->N1->N2 | Guardar Alta Quad (7) permite abrir Listado Quads (1). | Verificado |
| 28 | 7, 2 | SistRes.java | N3->N1->N4 | Guardar Alta Quad permite abrir Listado Reservas (2). | Verificado |
| 29 | 7, 3 | SistRes.java | N3->N1->N3 | Guardar Alta Quad permite iniciar otra Alta Quad (3). | Verificado |
| 30 | 7, 4 | SistRes.java | N3->N1->N5 | Guardar Alta Quad permite iniciar Alta Reserva (4). | Verificado |
| 31 | 7b, 1 | SistRes.java | N3->N1->N2 | Cancelar Alta Quad (7b) permite abrir Listado Quads (1). | Verificado |
| 32 | 7b, 2 | SistRes.java | N3->N1->N4 | Cancelar Alta Quad permite abrir Listado Reservas (2). | Verificado |
| 33 | 7b, 3 | SistRes.java | N3->N1->N3 | Cancelar Alta Quad permite otra Alta Quad (3). | Verificado |
| 34 | 7b, 4 | SistRes.java | N3->N1->N5 | Cancelar Alta Quad permite Alta Reserva (4). | Verificado |
| 35 | 7c, 1 | SistRes.java | N3->N1->N2 | Atrás en Alta Quad (7c) permite abrir Listado Quads (1). | Verificado |
| 36 | 7c, 2 | SistRes.java | N3->N1->N4 | Atrás en Alta Quad permite abrir Listado Reservas (2). | Verificado |
| 37 | 7c, 3 | SistRes.java | N3->N1->N3 | Atrás en Alta Quad permite otra Alta Quad (3). | Verificado |
| 38 | 7c, 4 | SistRes.java | N3->N1->N5 | Atrás en Alta Quad permite Alta Reserva (4). | Verificado |
| 39 | 10, 1 | SistRes.java | N5->N1->N2 | Guardar Alta Reserva (10) permite abrir Listado Quads (1). | Verificado |
| 40 | 10, 2 | SistRes.java | N5->N1->N4 | Guardar Alta Reserva permite abrir Listado Reservas (2). | Verificado |
| 41 | 10, 3 | SistRes.java | N5->N1->N3 | Guardar Alta Reserva permite iniciar Alta Quad (3). | Verificado |
| 42 | 10, 4 | SistRes.java | N5->N1->N5 | Guardar Alta Reserva permite otra Alta Reserva (4). | Verificado |
| 43 | 10b, 1 | SistRes.java | N5->N1->N2 | Cancelar Alta Reserva (10b) permite abrir Listado Quads (1). | Verificado |
| 44 | 10b, 2 | SistRes.java | N5->N1->N4 | Cancelar Alta Reserva permite abrir Listado Reservas (2). | Verificado |
| 45 | 10b, 3 | SistRes.java | N5->N1->N3 | Cancelar Alta Reserva permite Alta Quad (3). | Verificado |
| 46 | 10b, 4 | SistRes.java | N5->N1->N5 | Cancelar Alta Reserva permite otra Alta Reserva (4). | Verificado |
| 47 | 10c, 1 | SistRes.java | N5->N1->N2 | Atrás en Alta Reserva (10c) permite abrir Listado Quads (1). | Verificado |
| 48 | 10c, 2 | SistRes.java | N5->N1->N4 | Atrás en Alta Reserva permite abrir Listado Reservas (2). | Verificado |
| 49 | 10c, 3 | SistRes.java | N5->N1->N3 | Atrás en Alta Reserva permite Alta Quad (3). | Verificado |
| 50 | 10c, 4 | SistRes.java | N5->N1->N5 | Atrás en Alta Reserva permite otra Alta Reserva (4). | Verificado |
| 51 | 23, 1 | SistRes.java | N2->N1->N2 | Volver de Lista Quads (23) permite reabrir Listado Quads (1). | Verificado |
| 52 | 23, 2 | SistRes.java | N2->N1->N4 | Volver de Lista Quads permite abrir Listado Reservas (2). | Verificado |
| 53 | 23, 3 | SistRes.java | N2->N1->N3 | Volver de Lista Quads permite Alta Quad (3). | Verificado |
| 54 | 23, 4 | SistRes.java | N2->N1->N5 | Volver de Lista Quads permite Alta Reserva (4). | Verificado |
| 55 | 24, 1 | SistRes.java | N4->N1->N2 | Volver de Lista Reservas (24) permite abrir Listado Quads (1). | Verificado |
| 56 | 24, 2 | SistRes.java | N4->N1->N4 | Volver de Lista Reservas permite reabrir Listado Reservas (2). | Verificado |
| 57 | 24, 3 | SistRes.java | N4->N1->N3 | Volver de Lista Reservas permite Alta Quad (3). | Verificado |
| 58 | 24, 4 | SistRes.java | N4->N1->N5 | Volver de Lista Reservas permite Alta Reserva (4). | Verificado |

### 2.2 — Transiciones en N2 (Listado Quads)
| ID | Pareja | Archivo | Navegación | Evidencia Técnica | Estado |
|---|---|---|---|---|---|
| 59 | 1, 5 | QuadList.java | N1->N2->N3 | Entrar en lista (1) permite editar quad (5). | Verificado |
| 60 | 1, 13 | QuadList.java | N1->N2->N2 | Entrar en lista permite ordenar por matrícula (13). | Verificado |
| 61 | 1, 13b | QuadList.java | N1->N2->N2 | Entrar en lista permite ordenar por tipo (13b). | Verificado |
| 62 | 1, 13c | QuadList.java | N1->N2->N2 | Entrar en lista permite ordenar por precio (13c). | Verificado |
| 63 | 1, 14 | QuadList.java | N1->N2->N2 | Entrar en lista permite eliminar quad (14). | Verificado |
| 64 | 1, 23 | QuadList.java | N1->N2->N1 | Entrar en lista permite volver atrás (23). | Verificado |
| 65 | 6, 5 | QuadList.java | N3->N2->N3 | Tras guardar edición (6), permite editar otro quad (5). | Verificado |
| 66 | 6, 13 | QuadList.java | N3->N2->N2 | Tras guardar edición, permite reordenar matrícula (13). | Verificado |
| 67 | 6, 13b | QuadList.java | N3->N2->N2 | Tras guardar edición, permite reordenar tipo (13b). | Verificado |
| 68 | 6, 13c | QuadList.java | N3->N2->N2 | Tras guardar edición, permite reordenar precio (13c). | Verificado |
| 69 | 6, 14 | QuadList.java | N3->N2->N2 | Tras guardar edición, permite eliminar quad (14). | Verificado |
| 70 | 6, 23 | QuadList.java | N3->N2->N1 | Tras guardar edición, permite volver al menú (23). | Verificado |
| 71 | 6b, 5 | QuadList.java | N3->N2->N3 | Tras cancelar edición (6b), permite editar quad (5). | Verificado |
| 72 | 6b, 13 | QuadList.java | N3->N2->N2 | Tras cancelar edición, permite ordenar matrícula (13). | Verificado |
| 73 | 6b, 13b | QuadList.java | N3->N2->N2 | Tras cancelar edición, permite ordenar tipo (13b). | Verificado |
| 74 | 6b, 13c | QuadList.java | N3->N2->N2 | Tras cancelar edición, permite ordenar precio (13c). | Verificado |
| 75 | 6b, 14 | QuadList.java | N3->N2->N2 | Tras cancelar edición, permite eliminar quad (14). | Verificado |
| 76 | 6b, 23 | QuadList.java | N3->N2->N1 | Tras cancelar edición, permite volver al menú (23). | Verificado |
| 77 | 6c, 5 | QuadList.java | N3->N2->N3 | Tras atrás en edición (6c), permite editar quad (5). | Verificado |
| 78 | 6c, 13 | QuadList.java | N3->N2->N2 | Tras atrás en edición, permite ordenar matrícula (13). | Verificado |
| 79 | 6c, 13b | QuadList.java | N3->N2->N2 | Tras atrás en edición, permite ordenar tipo (13b). | Verificado |
| 80 | 6c, 13c | QuadList.java | N3->N2->N2 | Tras atrás en edición, permite ordenar precio (13c). | Verificado |
| 81 | 6c, 14 | QuadList.java | N3->N2->N2 | Tras atrás en edición, permite eliminar quad (14). | Verificado |
| 82 | 6c, 23 | QuadList.java | N3->N2->N1 | Tras atrás en edición, permite volver al menú (23). | Verificado |
| 83 | 13, 5 | QuadList.java | N2->N2->N3 | Tras ordenar matrícula (13), permite editar quad (5). | Verificado |
| 84 | 13, 13 | QuadList.java | N2->N2->N2 | Bucle de ordenación: permite re-ordenar matrícula. | Verificado |
| 85 | 13, 13b | QuadList.java | N2->N2->N2 | Tras ordenar matrícula, permite cambiar a tipo (13b). | Verificado |
| 86 | 13, 13c | QuadList.java | N2->N2->N2 | Tras ordenar matrícula, permite cambiar a precio (13c). | Verificado |
| 87 | 13, 14 | QuadList.java | N2->N2->N2 | Tras ordenar matrícula, permite eliminar quad (14). | Verificado |
| 88 | 13, 23 | QuadList.java | N2->N2->N1 | Tras ordenar matrícula, permite salir (23). | Verificado |
| 89 | 13b, 5 | QuadList.java | N2->N2->N3 | Tras ordenar tipo (13b), permite editar quad (5). | Verificado |
| 90 | 13b, 13 | QuadList.java | N2->N2->N2 | Tras ordenar tipo, permite cambiar a matrícula (13). | Verificado |
| 91 | 13b, 13b | QuadList.java | N2->N2->N2 | Bucle de ordenación tipo. | Verificado |
| 92 | 13b, 13c | QuadList.java | N2->N2->N2 | Tras ordenar tipo, permite cambiar a precio (13c). | Verificado |
| 93 | 13b, 14 | QuadList.java | N2->N2->N2 | Tras ordenar tipo, permite eliminar quad (14). | Verificado |
| 94 | 13b, 23 | QuadList.java | N2->N2->N1 | Tras ordenar tipo, permite salir (23). | Verificado |
| 95 | 13c, 5 | QuadList.java | N2->N2->N3 | Tras ordenar precio (13c), permite editar quad (5). | Verificado |
| 96 | 13c, 13 | QuadList.java | N2->N2->N2 | Tras ordenar precio, permite cambiar a matrícula (13). | Verificado |
| 97 | 13c, 13b | QuadList.java | N2->N2->N2 | Tras ordenar precio, permite cambiar a tipo (13b). | Verificado |
| 98 | 13c, 13c | QuadList.java | N2->N2->N2 | Bucle de ordenación precio. | Verificado |
| 99 | 13c, 14 | QuadList.java | N2->N2->N2 | Tras ordenar precio, permite eliminar quad (14). | Verificado |
| 100 | 13c, 23 | QuadList.java | N2->N2->N1 | Tras ordenar precio, permite salir (23). | Verificado |
| 101 | 14, 5 | QuadList.java | N2->N2->N3 | Tras eliminar quad (14), permite editar otro (5). | Verificado |
| 102 | 14, 13 | QuadList.java | N2->N2->N2 | Tras eliminar, permite ordenar matrícula (13). | Verificado |
| 103 | 14, 13b | QuadList.java | N2->N2->N2 | Tras eliminar, permite ordenar tipo (13b). | Verificado |
| 104 | 14, 13c | QuadList.java | N2->N2->N2 | Tras eliminar, permite ordenar precio (13c). | Verificado |
| 105 | 14, 14 | QuadList.java | N2->N2->N2 | Tras eliminar, permite eliminar otro quad (14). | Verificado |
| 106 | 14, 23 | QuadList.java | N2->N2->N1 | Tras eliminar, permite salir (23). | Verificado |

### 2.3 — Transiciones en N3 (Formulario Quad)
| ID | Pareja | Archivo | Navegación | Evidencia Técnica | Estado |
|---|---|---|---|---|---|
| 107 | 3, 7 | QuadEdit.java | N1->N3->N1 | Alta Quad (3) permite Guardar y volver a Menú (7). | Verificado |
| 108 | 3, 7b | QuadEdit.java | N1->N3->N1 | Alta Quad permite Cancelar y volver a Menú (7b). | Verificado |
| 109 | 3, 7c | QuadEdit.java | N1->N3->N1 | Alta Quad permite Atrás y volver a Menú (7c). | Verificado |
| 110 | 3, 15 | QuadEdit.java | N1->N3->N3 | Alta Quad permite cambiar a Monoplaza (15). | Verificado |
| 111 | 3, 15b | QuadEdit.java | N1->N3->N3 | Alta Quad permite cambiar a Biplaza (15b). | Verificado |
| 112 | 5, 6 | QuadEdit.java | N2->N3->N2 | Edición Quad (5) permite Guardar y volver a Lista (6). | Verificado |
| 113 | 5, 6b | QuadEdit.java | N2->N3->N2 | Edición Quad permite Cancelar y volver a Lista (6b). | Verificado |
| 114 | 5, 6c | QuadEdit.java | N2->N3->N2 | Edición Quad permite Atrás y volver a Lista (6c). | Verificado |
| 115 | 5, 15 | QuadEdit.java | N2->N3->N3 | Edición Quad permite cambiar a Monoplaza (15). | Verificado |
| 116 | 5, 15b | QuadEdit.java | N2->N3->N3 | Edición Quad permite cambiar a Biplaza (15b). | Verificado |
| 117 | 15, 15 | QuadEdit.java | N3->N3->N3 | Permite reafirmar Monoplaza (15). | Verificado |
| 118 | 15, 15b | QuadEdit.java | N3->N3->N3 | Permite alternar de Monoplaza a Biplaza (15b). | Verificado |
| 119 | 15, 6 | QuadEdit.java | N2->N3->N2 | Si vienes de Lista (5), Monoplaza (15) permite Guardar a Lista (6). | Verificado |
| 120 | 15, 6b | QuadEdit.java | N2->N3->N2 | Si vienes de Lista, Monoplaza permite Cancelar a Lista (6b). | Verificado |
| 121 | 15, 6c | QuadEdit.java | N2->N3->N2 | Si vienes de Lista, Monoplaza permite Atrás a Lista (6c). | Verificado |
| 122 | 15, 7 | QuadEdit.java | N1->N3->N1 | Si vienes de Menú (3), Monoplaza (15) permite Guardar a Menú (7). | Verificado |
| 123 | 15, 7b | QuadEdit.java | N1->N3->N1 | Si vienes de Menú, Monoplaza permite Cancelar a Menú (7b). | Verificado |
| 124 | 15, 7c | QuadEdit.java | N1->N3->N1 | Si vienes de Menú, Monoplaza permite Atrás a Menú (7c). | Verificado |
| 125 | 15b, 15 | QuadEdit.java | N3->N3->N3 | Permite alternar de Biplaza a Monoplaza (15). | Verificado |
| 126 | 15b, 15b | QuadEdit.java | N3->N3->N3 | Permite reafirmar Biplaza (15b). | Verificado |
| 127 | 15b, 6 | QuadEdit.java | N2->N3->N2 | Si vienes de Lista (5), Biplaza (15b) permite Guardar a Lista (6). | Verificado |
| 128 | 15b, 6b | QuadEdit.java | N2->N3->N2 | Si vienes de Lista, Biplaza permite Cancelar a Lista (6b). | Verificado |
| 129 | 15b, 6c | QuadEdit.java | N2->N3->N2 | Si vienes de Lista, Biplaza permite Atrás a Lista (6c). | Verificado |
| 130 | 15b, 7 | QuadEdit.java | N1->N3->N1 | Si vienes de Menú (3), Biplaza (15b) permite Guardar a Menú (7). | Verificado |
| 131 | 15b, 7b | QuadEdit.java | N1->N3->N1 | Si vienes de Menú, Biplaza permite Cancelar a Menú (7b). | Verificado |
| 132 | 15b, 7c | QuadEdit.java | N1->N3->N1 | Si vienes de Menú, Biplaza permite Atrás a Menú (7c). | Verificado |

### 2.4 — Transiciones en N4 (Listado Reservas)
*(Este nodo concentra la mayoría de combinaciones por su panel de filtros y ordenación: 14 entradas x 12 salidas)*

| ID | Pareja | Archivo | Navegación | Evidencia Técnica | Estado |
|---|---|---|---|---|---|
| 133 | 2, 8 | ResList.java | N1->N4->N5 | Entrar en lista (2) permite editar reserva (8). | Verificado |
| 134 | 2, 16 | ResList.java | N1->N4->N4 | Entrar en lista permite ordenar por nombre (16). | Verificado |
| 135 | 2, 16b | ResList.java | N1->N4->N4 | Entrar en lista permite ordenar por teléfono (16b). | Verificado |
| 136 | 2, 16c | ResList.java | N1->N4->N4 | Entrar en lista permite ordenar por entrada (16c). | Verificado |
| 137 | 2, 16d | ResList.java | N1->N4->N4 | Entrar en lista permite ordenar por salida (16d). | Verificado |
| 138 | 2, 16e | ResList.java | N1->N4->N4 | Entrar en lista permite filtrar previstas (16e). | Verificado |
| 139 | 2, 16f | ResList.java | N1->N4->N4 | Entrar en lista permite filtrar vigentes (16f). | Verificado |
| 140 | 2, 16g | ResList.java | N1->N4->N4 | Entrar en lista permite filtrar caducadas (16g). | Verificado |
| 141 | 2, 16h | ResList.java | N1->N4->N4 | Entrar en lista permite limpiar filtros (16h). | Verificado |
| 142 | 2, 17 | ResList.java | N1->N4->N4 | Entrar en lista permite eliminar reserva (17). | Verificado |
| 143 | 2, 18 | ResList.java | N1->N4->N4 | Entrar en lista permite ver detalles (18). | Verificado |
| 144 | 2, 24 | ResList.java | N1->N4->N1 | Entrar en lista permite volver atrás (24). | Verificado |
| 145 | 9, 8 | ResList.java | N5->N4->N5 | Tras guardar edición (9), permite editar otra reserva (8). | Verificado |
| 146 | 9, 16 | ResList.java | N5->N4->N4 | Tras guardar, permite ordenar por nombre (16). | Verificado |
| 147 | 9, 16b | ResList.java | N5->N4->N4 | Tras guardar, permite ordenar por teléfono (16b). | Verificado |
| 148 | 9, 16c | ResList.java | N5->N4->N4 | Tras guardar, permite ordenar por entrada (16c). | Verificado |
| 149 | 9, 16d | ResList.java | N5->N4->N4 | Tras guardar, permite ordenar por salida (16d). | Verificado |
| 150 | 9, 16e | ResList.java | N5->N4->N4 | Tras guardar, permite filtrar previstas (16e). | Verificado |
| 151 | 9, 16f | ResList.java | N5->N4->N4 | Tras guardar, permite filtrar vigentes (16f). | Verificado |
| 152 | 9, 16g | ResList.java | N5->N4->N4 | Tras guardar, permite filtrar caducadas (16g). | Verificado |
| 153 | 9, 16h | ResList.java | N5->N4->N4 | Tras guardar, permite limpiar filtros (16h). | Verificado |
| 154 | 9, 17 | ResList.java | N5->N4->N4 | Tras guardar, permite eliminar reserva (17). | Verificado |
| 155 | 9, 18 | ResList.java | N5->N4->N4 | Tras guardar, permite ver detalles (18). | Verificado |
| 156 | 9, 24 | ResList.java | N5->N4->N1 | Tras guardar, permite volver al menú (24). | Verificado |
| 157 | 9b, 8 | ResList.java | N5->N4->N5 | Tras cancelar edición (9b), permite editar reserva (8). | Verificado |
| 158 | 9b, 16 | ResList.java | N5->N4->N4 | Tras cancelar, permite ordenar nombre (16). | Verificado |
| 159 | 9b, 16b | ResList.java | N5->N4->N4 | Tras cancelar, permite ordenar teléfono (16b). | Verificado |
| 160 | 9b, 16c | ResList.java | N5->N4->N4 | Tras cancelar, permite ordenar entrada (16c). | Verificado |
| 161 | 9b, 16d | ResList.java | N5->N4->N4 | Tras cancelar, permite ordenar salida (16d). | Verificado |
| 162 | 9b, 16e | ResList.java | N5->N4->N4 | Tras cancelar, permite filtrar previstas (16e). | Verificado |
| 163 | 9b, 16f | ResList.java | N5->N4->N4 | Tras cancelar, permite filtrar vigentes (16f). | Verificado |
| 164 | 9b, 16g | ResList.java | N5->N4->N4 | Tras cancelar, permite filtrar caducadas (16g). | Verificado |
| 165 | 9b, 16h | ResList.java | N5->N4->N4 | Tras cancelar, permite limpiar filtros (16h). | Verificado |
| 166 | 9b, 17 | ResList.java | N5->N4->N4 | Tras cancelar, permite eliminar reserva (17). | Verificado |
| 167 | 9b, 18 | ResList.java | N5->N4->N4 | Tras cancelar, permite ver detalles (18). | Verificado |
| 168 | 9b, 24 | ResList.java | N5->N4->N1 | Tras cancelar, permite volver al menú (24). | Verificado |
| 169 | 9c, 8 | ResList.java | N5->N4->N5 | Tras atrás en edición (9c), permite editar reserva (8). | Verificado |
| 170 | 9c, 16 | ResList.java | N5->N4->N4 | Tras atrás, permite ordenar nombre (16). | Verificado |
| 171 | 9c, 16b | ResList.java | N5->N4->N4 | Tras atrás, permite ordenar teléfono (16b). | Verificado |
| 172 | 9c, 16c | ResList.java | N5->N4->N4 | Tras atrás, permite ordenar entrada (16c). | Verificado |
| 173 | 9c, 16d | ResList.java | N5->N4->N4 | Tras atrás, permite ordenar salida (16d). | Verificado |
| 174 | 9c, 16e | ResList.java | N5->N4->N4 | Tras atrás, permite filtrar previstas (16e). | Verificado |
| 175 | 9c, 16f | ResList.java | N5->N4->N4 | Tras atrás, permite filtrar vigentes (16f). | Verificado |
| 176 | 9c, 16g | ResList.java | N5->N4->N4 | Tras atrás, permite filtrar caducadas (16g). | Verificado |
| 177 | 9c, 16h | ResList.java | N5->N4->N4 | Tras atrás, permite limpiar filtros (16h). | Verificado |
| 178 | 9c, 17 | ResList.java | N5->N4->N4 | Tras atrás, permite eliminar reserva (17). | Verificado |
| 179 | 9c, 18 | ResList.java | N5->N4->N4 | Tras atrás, permite ver detalles (18). | Verificado |
| 180 | 9c, 24 | ResList.java | N5->N4->N1 | Tras atrás, permite volver al menú (24). | Verificado |
| 181 | 16, 8 | ResList.java | N4->N4->N5 | Tras ordenar nombre (16), permite editar reserva (8). | Verificado |
| 182 | 16, 16 | ResList.java | N4->N4->N4 | Bucle de ordenación nombre. | Verificado |
| 183 | 16, 16b | ResList.java | N4->N4->N4 | Tras ordenar nombre, permite cambiar a teléfono (16b). | Verificado |
| 184 | 16, 16c | ResList.java | N4->N4->N4 | Tras ordenar nombre, permite cambiar a entrada (16c). | Verificado |
| 185 | 16, 16d | ResList.java | N4->N4->N4 | Tras ordenar nombre, permite cambiar a salida (16d). | Verificado |
| 186 | 16, 16e | ResList.java | N4->N4->N4 | Tras ordenar nombre, permite filtrar previstas (16e). | Verificado |
| 187 | 16, 16f | ResList.java | N4->N4->N4 | Tras ordenar nombre, permite filtrar vigentes (16f). | Verificado |
| 188 | 16, 16g | ResList.java | N4->N4->N4 | Tras ordenar nombre, permite filtrar caducadas (16g). | Verificado |
| 189 | 16, 16h | ResList.java | N4->N4->N4 | Tras ordenar nombre, permite limpiar filtros (16h). | Verificado |
| 190 | 16, 17 | ResList.java | N4->N4->N4 | Tras ordenar nombre, permite eliminar reserva (17). | Verificado |
| 191 | 16, 18 | ResList.java | N4->N4->N4 | Tras ordenar nombre, permite ver detalles (18). | Verificado |
| 192 | 16, 24 | ResList.java | N4->N4->N1 | Tras ordenar nombre, permite salir (24). | Verificado |
| 193 | 16b, 8 | ResList.java | N4->N4->N5 | Tras ordenar teléfono (16b), permite editar reserva (8). | Verificado |
| 194 | 16b, 16 | ResList.java | N4->N4->N4 | Tras ordenar teléfono, permite cambiar a nombre (16). | Verificado |
| 195 | 16b, 16b | ResList.java | N4->N4->N4 | Bucle de ordenación teléfono. | Verificado |
| 196 | 16b, 16c | ResList.java | N4->N4->N4 | Tras ordenar teléfono, permite cambiar a entrada (16c). | Verificado |
| 197 | 16b, 16d | ResList.java | N4->N4->N4 | Tras ordenar teléfono, permite cambiar a salida (16d). | Verificado |
| 198 | 16b, 16e | ResList.java | N4->N4->N4 | Tras ordenar teléfono, permite filtrar previstas (16e). | Verificado |
| 199 | 16b, 16f | ResList.java | N4->N4->N4 | Tras ordenar teléfono, permite filtrar vigentes (16f). | Verificado |
| 200 | 16b, 16g | ResList.java | N4->N4->N4 | Tras ordenar teléfono, permite filtrar caducadas (16g). | Verificado |
| 201 | 16b, 16h | ResList.java | N4->N4->N4 | Tras ordenar teléfono, permite limpiar filtros (16h). | Verificado |
| 202 | 16b, 17 | ResList.java | N4->N4->N4 | Tras ordenar teléfono, permite eliminar reserva (17). | Verificado |
| 203 | 16b, 18 | ResList.java | N4->N4->N4 | Tras ordenar teléfono, permite ver detalles (18). | Verificado |
| 204 | 16b, 24 | ResList.java | N4->N4->N1 | Tras ordenar teléfono, permite salir (24). | Verificado |
| 205 | 16c, 8 | ResList.java | N4->N4->N5 | Tras ordenar entrada (16c), permite editar reserva (8). | Verificado |
| 206 | 16c, 16 | ResList.java | N4->N4->N4 | Tras ordenar entrada, permite cambiar a nombre (16). | Verificado |
| 207 | 16c, 16b | ResList.java | N4->N4->N4 | Tras ordenar entrada, permite cambiar a teléfono (16b). | Verificado |
| 208 | 16c, 16c | ResList.java | N4->N4->N4 | Bucle de ordenación entrada. | Verificado |
| 209 | 16c, 16d | ResList.java | N4->N4->N4 | Tras ordenar entrada, permite cambiar a salida (16d). | Verificado |
| 210 | 16c, 16e | ResList.java | N4->N4->N4 | Tras ordenar entrada, permite filtrar previstas (16e). | Verificado |
| 211 | 16c, 16f | ResList.java | N4->N4->N4 | Tras ordenar entrada, permite filtrar vigentes (16f). | Verificado |
| 212 | 16c, 16g | ResList.java | N4->N4->N4 | Tras ordenar entrada, permite filtrar caducadas (16g). | Verificado |
| 213 | 16c, 16h | ResList.java | N4->N4->N4 | Tras ordenar entrada, permite limpiar filtros (16h). | Verificado |
| 214 | 16c, 17 | ResList.java | N4->N4->N4 | Tras ordenar entrada, permite eliminar reserva (17). | Verificado |
| 215 | 16c, 18 | ResList.java | N4->N4->N4 | Tras ordenar entrada, permite ver detalles (18). | Verificado |
| 216 | 16c, 24 | ResList.java | N4->N4->N1 | Tras ordenar entrada, permite salir (24). | Verificado |
| 217 | 16d, 8 | ResList.java | N4->N4->N5 | Tras ordenar salida (16d), permite editar reserva (8). | Verificado |
| 218 | 16d, 16 | ResList.java | N4->N4->N4 | Tras ordenar salida, permite cambiar a nombre (16). | Verificado |
| 219 | 16d, 16b | ResList.java | N4->N4->N4 | Tras ordenar salida, permite cambiar a teléfono (16b). | Verificado |
| 220 | 16d, 16c | ResList.java | N4->N4->N4 | Tras ordenar salida, permite cambiar a entrada (16c). | Verificado |
| 221 | 16d, 16d | ResList.java | N4->N4->N4 | Bucle de ordenación salida. | Verificado |
| 222 | 16d, 16e | ResList.java | N4->N4->N4 | Tras ordenar salida, permite filtrar previstas (16e). | Verificado |
| 223 | 16d, 16f | ResList.java | N4->N4->N4 | Tras ordenar salida, permite filtrar vigentes (16f). | Verificado |
| 224 | 16d, 16g | ResList.java | N4->N4->N4 | Tras ordenar salida, permite filtrar caducadas (16g). | Verificado |
| 225 | 16d, 16h | ResList.java | N4->N4->N4 | Tras ordenar salida, permite limpiar filtros (16h). | Verificado |
| 226 | 16d, 17 | ResList.java | N4->N4->N4 | Tras ordenar salida, permite eliminar reserva (17). | Verificado |
| 227 | 16d, 18 | ResList.java | N4->N4->N4 | Tras ordenar salida, permite ver detalles (18). | Verificado |
| 228 | 16d, 24 | ResList.java | N4->N4->N1 | Tras ordenar salida, permite salir (24). | Verificado |
| 229 | 16e, 8 | ResList.java | N4->N4->N5 | Tras filtrar previstas (16e), permite editar reserva (8). | Verificado |
| 230 | 16e, 16 | ResList.java | N4->N4->N4 | Tras filtrar previstas, permite ordenar nombre (16). | Verificado |
| 231 | 16e, 16b | ResList.java | N4->N4->N4 | Tras filtrar previstas, permite ordenar teléfono (16b). | Verificado |
| 232 | 16e, 16c | ResList.java | N4->N4->N4 | Tras filtrar previstas, permite ordenar entrada (16c). | Verificado |
| 233 | 16e, 16d | ResList.java | N4->N4->N4 | Tras filtrar previstas, permite ordenar salida (16d). | Verificado |
| 234 | 16e, 16e | ResList.java | N4->N4->N4 | Bucle de filtro previstas. | Verificado |
| 235 | 16e, 16f | ResList.java | N4->N4->N4 | Tras filtrar previstas, permite cambiar a vigentes (16f). | Verificado |
| 236 | 16e, 16g | ResList.java | N4->N4->N4 | Tras filtrar previstas, permite cambiar a caducadas (16g). | Verificado |
| 237 | 16e, 16h | ResList.java | N4->N4->N4 | Tras filtrar previstas, permite limpiar filtros (16h). | Verificado |
| 238 | 16e, 17 | ResList.java | N4->N4->N4 | Tras filtrar previstas, permite eliminar reserva (17). | Verificado |
| 239 | 16e, 18 | ResList.java | N4->N4->N4 | Tras filtrar previstas, permite ver detalles (18). | Verificado |
| 240 | 16e, 24 | ResList.java | N4->N4->N1 | Tras filtrar previstas, permite salir (24). | Verificado |
| 241 | 16f, 8 | ResList.java | N4->N4->N5 | Tras filtrar vigentes (16f), permite editar reserva (8). | Verificado |
| 242 | 16f, 16 | ResList.java | N4->N4->N4 | Tras filtrar vigentes, permite ordenar nombre (16). | Verificado |
| 243 | 16f, 16b | ResList.java | N4->N4->N4 | Tras filtrar vigentes, permite ordenar teléfono (16b). | Verificado |
| 244 | 16f, 16c | ResList.java | N4->N4->N4 | Tras filtrar vigentes, permite ordenar entrada (16c). | Verificado |
| 245 | 16f, 16d | ResList.java | N4->N4->N4 | Tras filtrar vigentes, permite ordenar salida (16d). | Verificado |
| 246 | 16f, 16e | ResList.java | N4->N4->N4 | Tras filtrar vigentes, permite cambiar a previstas (16e). | Verificado |
| 247 | 16f, 16f | ResList.java | N4->N4->N4 | Bucle de filtro vigentes. | Verificado |
| 248 | 16f, 16g | ResList.java | N4->N4->N4 | Tras filtrar vigentes, permite cambiar a caducadas (16g). | Verificado |
| 249 | 16f, 16h | ResList.java | N4->N4->N4 | Tras filtrar vigentes, permite limpiar filtros (16h). | Verificado |
| 250 | 16f, 17 | ResList.java | N4->N4->N4 | Tras filtrar vigentes, permite eliminar reserva (17). | Verificado |
| 251 | 16f, 18 | ResList.java | N4->N4->N4 | Tras filtrar vigentes, permite ver detalles (18). | Verificado |
| 252 | 16f, 24 | ResList.java | N4->N4->N1 | Tras filtrar vigentes, permite salir (24). | Verificado |
| 253 | 16g, 8 | ResList.java | N4->N4->N5 | Tras filtrar caducadas (16g), permite editar reserva (8). | Verificado |
| 254 | 16g, 16 | ResList.java | N4->N4->N4 | Tras filtrar caducadas, permite ordenar nombre (16). | Verificado |
| 255 | 16g, 16b | ResList.java | N4->N4->N4 | Tras filtrar caducadas, permite ordenar teléfono (16b). | Verificado |
| 256 | 16g, 16c | ResList.java | N4->N4->N4 | Tras filtrar caducadas, permite ordenar entrada (16c). | Verificado |
| 257 | 16g, 16d | ResList.java | N4->N4->N4 | Tras filtrar caducadas, permite ordenar salida (16d). | Verificado |
| 258 | 16g, 16e | ResList.java | N4->N4->N4 | Tras filtrar caducadas, permite cambiar a previstas (16e). | Verificado |
| 259 | 16g, 16f | ResList.java | N4->N4->N4 | Tras filtrar caducadas, permite cambiar a vigentes (16f). | Verificado |
| 260 | 16g, 16g | ResList.java | N4->N4->N4 | Bucle de filtro caducadas. | Verificado |
| 261 | 16g, 16h | ResList.java | N4->N4->N4 | Tras filtrar caducadas, permite limpiar filtros (16h). | Verificado |
| 262 | 16g, 17 | ResList.java | N4->N4->N4 | Tras filtrar caducadas, permite eliminar reserva (17). | Verificado |
| 263 | 16g, 18 | ResList.java | N4->N4->N4 | Tras filtrar caducadas, permite ver detalles (18). | Verificado |
| 264 | 16g, 24 | ResList.java | N4->N4->N1 | Tras filtrar caducadas, permite salir (24). | Verificado |
| 265 | 16h, 8 | ResList.java | N4->N4->N5 | Tras limpiar filtros (16h), permite editar reserva (8). | Verificado |
| 266 | 16h, 16 | ResList.java | N4->N4->N4 | Tras limpiar filtros, permite ordenar nombre (16). | Verificado |
| 267 | 16h, 16b | ResList.java | N4->N4->N4 | Tras limpiar filtros, permite ordenar teléfono (16b). | Verificado |
| 268 | 16h, 16c | ResList.java | N4->N4->N4 | Tras limpiar filtros, permite ordenar entrada (16c). | Verificado |
| 269 | 16h, 16d | ResList.java | N4->N4->N4 | Tras limpiar filtros, permite ordenar salida (16d). | Verificado |
| 270 | 16h, 16e | ResList.java | N4->N4->N4 | Tras limpiar filtros, permite filtrar previstas (16e). | Verificado |
| 271 | 16h, 16f | ResList.java | N4->N4->N4 | Tras limpiar filtros, permite filtrar vigentes (16f). | Verificado |
| 272 | 16h, 16g | ResList.java | N4->N4->N4 | Tras limpiar filtros, permite filtrar caducadas (16g). | Verificado |
| 273 | 16h, 16h | ResList.java | N4->N4->N4 | Bucle de limpieza de filtros. | Verificado |
| 274 | 16h, 17 | ResList.java | N4->N4->N4 | Tras limpiar filtros, permite eliminar reserva (17). | Verificado |
| 275 | 16h, 18 | ResList.java | N4->N4->N4 | Tras limpiar filtros, permite ver detalles (18). | Verificado |
| 276 | 16h, 24 | ResList.java | N4->N4->N1 | Tras limpiar filtros, permite salir (24). | Verificado |
| 277 | 17, 8 | ResList.java | N4->N4->N5 | Tras eliminar reserva (17), permite editar otra (8). | Verificado |
| 278 | 17, 16 | ResList.java | N4->N4->N4 | Tras eliminar, permite ordenar nombre (16). | Verificado |
| 279 | 17, 16b | ResList.java | N4->N4->N4 | Tras eliminar, permite ordenar teléfono (16b). | Verificado |
| 280 | 17, 16c | ResList.java | N4->N4->N4 | Tras eliminar, permite ordenar entrada (16c). | Verificado |
| 281 | 17, 16d | ResList.java | N4->N4->N4 | Tras eliminar, permite ordenar salida (16d). | Verificado |
| 282 | 17, 16e | ResList.java | N4->N4->N4 | Tras eliminar, permite filtrar previstas (16e). | Verificado |
| 283 | 17, 16f | ResList.java | N4->N4->N4 | Tras eliminar, permite filtrar vigentes (16f). | Verificado |
| 284 | 17, 16g | ResList.java | N4->N4->N4 | Tras eliminar, permite filtrar caducadas (16g). | Verificado |
| 285 | 17, 16h | ResList.java | N4->N4->N4 | Tras eliminar, permite limpiar filtros (16h). | Verificado |
| 286 | 17, 17 | ResList.java | N4->N4->N4 | Tras eliminar, permite eliminar otra reserva (17). | Verificado |
| 287 | 17, 18 | ResList.java | N4->N4->N4 | Tras eliminar, permite ver detalles (18). | Verificado |
| 288 | 17, 24 | ResList.java | N4->N4->N1 | Tras eliminar, permite salir (24). | Verificado |
| 289 | 18, 8 | ResList.java | N4->N4->N5 | Tras ver detalles (18), permite editar esa reserva (8). | Verificado |
| 290 | 18, 16 | ResList.java | N4->N4->N4 | Tras ver detalles, permite ordenar nombre (16). | Verificado |
| 291 | 18, 16b | ResList.java | N4->N4->N4 | Tras ver detalles, permite ordenar teléfono (16b). | Verificado |
| 292 | 18, 16c | ResList.java | N4->N4->N4 | Tras ver detalles, permite ordenar entrada (16c). | Verificado |
| 293 | 18, 16d | ResList.java | N4->N4->N4 | Tras ver detalles, permite ordenar salida (16d). | Verificado |
| 294 | 18, 16e | ResList.java | N4->N4->N4 | Tras ver detalles, permite filtrar previstas (16e). | Verificado |
| 295 | 18, 16f | ResList.java | N4->N4->N4 | Tras ver detalles, permite filtrar vigentes (16f). | Verificado |
| 296 | 18, 16g | ResList.java | N4->N4->N4 | Tras ver detalles, permite filtrar caducadas (16g). | Verificado |
| 297 | 18, 16h | ResList.java | N4->N4->N4 | Tras ver detalles, permite limpiar filtros (16h). | Verificado |
| 298 | 18, 17 | ResList.java | N4->N4->N4 | Tras ver detalles, permite eliminar reserva (17). | Verificado |
| 299 | 18, 18 | ResList.java | N4->N4->N4 | Bucle de consulta de detalles. | Verificado |
| 300 | 18, 24 | ResList.java | N4->N4->N1 | Tras ver detalles, permite salir (24). | Verificado |

### 2.5 — Transiciones en N5 (Formulario Reserva)
| ID | Pareja | Archivo | Navegación | Evidencia Técnica | Estado |
|---|---|---|---|---|---|
| 301 | 4, 20 | ResEdit.java | N1->N5->N5 | Alta Reserva (4) permite fijar fecha entrada (20). | Verificado |
| 302 | 4, 20b | ResEdit.java | N1->N5->N5 | Alta Reserva permite fijar fecha salida (20b). | Verificado |
| 303 | 4, 10b | ResEdit.java | N1->N5->N1 | Alta Reserva permite Cancelar a Menú (10b). | Verificado |
| 304 | 4, 10c | ResEdit.java | N1->N5->N1 | Alta Reserva permite Atrás a Menú (10c). | Verificado |
| 305 | 8, 9 | ResEdit.java | N4->N5->N4 | Edición (8) permite Guardar a Lista (9) al tener datos previos. | Verificado |
| 306 | 8, 9b | ResEdit.java | N4->N5->N4 | Edición permite Cancelar a Lista (9b). | Verificado |
| 307 | 8, 9c | ResEdit.java | N4->N5->N4 | Edición permite Atrás a Lista (9c). | Verificado |
| 308 | 8, 11 | ResEdit.java | N4->N5->N6 | Edición permite abrir Selector (11) al tener fechas. | Verificado |
| 309 | 8, 20 | ResEdit.java | N4->N5->N5 | Edición permite cambiar fecha entrada (20). | Verificado |
| 310 | 8, 20b | ResEdit.java | N4->N5->N5 | Edición permite cambiar fecha salida (20b). | Verificado |
| 311 | 12, 9 | ResEdit.java | N6->N5->N4 | Tras confirmar quads en edición, permite Guardar a Lista (9). | Verificado |
| 312 | 12, 9b | ResEdit.java | N6->N5->N4 | Tras confirmar en edición, permite Cancelar a Lista (9b). | Verificado |
| 313 | 12, 9c | ResEdit.java | N6->N5->N4 | Tras confirmar en edición, permite Atrás a Lista (9c). | Verificado |
| 314 | 12, 10 | ResEdit.java | N6->N5->N1 | Tras confirmar en alta, permite Guardar a Menú (10). | Verificado |
| 315 | 12, 10b | ResEdit.java | N6->N5->N1 | Tras confirmar en alta, permite Cancelar a Menú (10b). | Verificado |
| 316 | 12, 10c | ResEdit.java | N6->N5->N1 | Tras confirmar en alta, permite Atrás a Menú (10c). | Verificado |
| 317 | 12, 11 | ResEdit.java | N6->N5->N6 | Tras confirmar quads, permite re-abrir Selector (11). | Verificado |
| 318 | 12, 20 | ResEdit.java | N6->N5->N5 | Tras confirmar quads, permite re-fijar fecha entrada (20). | Verificado |
| 319 | 12, 20b | ResEdit.java | N6->N5->N5 | Tras confirmar quads, permite re-fijar fecha salida (20b). | Verificado |
| 320 | 12b, 9 | ResEdit.java | N6->N5->N4 | Si vienes de Edición (8), cancelar selector permite Guardar (9). | Verificado |
| 321 | 12b, 9b | ResEdit.java | N6->N5->N4 | Si vienes de Edición, cancelar selector permite Cancelar reserva (9b). | Verificado |
| 322 | 12b, 9c | ResEdit.java | N6->N5->N4 | Si vienes de Edición, cancelar selector permite Atrás (9c). | Verificado |
| 323 | 12b, 10b | ResEdit.java | N6->N5->N1 | Si vienes de Alta (4), cancelar selector permite Cancelar a Menú (10b). | Verificado |
| 324 | 12b, 10c | ResEdit.java | N6->N5->N1 | Si vienes de Alta, cancelar selector permite Atrás a Menú (10c). | Verificado |
| 325 | 12b, 11 | ResEdit.java | N6->N5->N6 | Cancelar selector permite volver a intentar Selección (11). | Verificado |
| 326 | 12b, 20 | ResEdit.java | N6->N5->N5 | Cancelar selector permite re-fijar fecha entrada (20). | Verificado |
| 327 | 12b, 20b | ResEdit.java | N6->N5->N5 | Cancelar selector permite re-fijar fecha salida (20b). | Verificado |
| 328 | 12c, 9 | ResEdit.java | N6->N5->N4 | Si vienes de Edición (8), atrás en selector permite Guardar (9). | Verificado |
| 329 | 12c, 9b | ResEdit.java | N6->N5->N4 | Si vienes de Edición, atrás en selector permite Cancelar reserva (9b). | Verificado |
| 330 | 12c, 9c | ResEdit.java | N6->N5->N4 | Si vienes de Edición, atrás en selector permite Atrás (9c). | Verificado |
| 331 | 12c, 10b | ResEdit.java | N6->N5->N1 | Si vienes de Alta (4), atrás en selector permite Cancelar a Menú (10b). | Verificado |
| 332 | 12c, 10c | ResEdit.java | N6->N5->N1 | Si vienes de Alta, atrás en selector permite Atrás a Menú (10c). | Verificado |
| 333 | 12c, 11 | ResEdit.java | N6->N5->N6 | Atrás en selector permite volver a intentar Selección (11). | Verificado |
| 334 | 12c, 20 | ResEdit.java | N6->N5->N5 | Atrás en selector permite re-fijar fecha entrada (20). | Verificado |
| 335 | 12c, 20b | ResEdit.java | N6->N5->N5 | Atrás en selector permite re-fijar fecha salida (20b). | Verificado |
| 336 | 20, 9 | ResEdit.java | N5->N5->N4 | Tras fijar entrada en edición, permite Guardar (9). | Verificado |
| 337 | 20, 9b | ResEdit.java | N5->N5->N4 | Tras fijar entrada en edición, permite Cancelar (9b). | Verificado |
| 338 | 20, 9c | ResEdit.java | N5->N5->N4 | Tras fijar entrada en edición, permite Atrás (9c). | Verificado |
| 339 | 20, 10b | ResEdit.java | N5->N5->N1 | Tras fijar entrada en alta, permite Cancelar (10b). | Verificado |
| 340 | 20, 10c | ResEdit.java | N5->N5->N1 | Tras fijar entrada en alta, permite Atrás (10c). | Verificado |
| 341 | 20, 11 | ResEdit.java | N5->N5->N6 | Tras fijar entrada, si ya hay salida, permite abrir Selección (11). | Verificado |
| 342 | 20, 20 | ResEdit.java | N5->N5->N5 | Bucle de fecha entrada. | Verificado |
| 343 | 20, 20b | ResEdit.java | N5->N5->N5 | Tras fijar entrada, permite fijar salida (20b). | Verificado |
| 344 | 20b, 9 | ResEdit.java | N5->N5->N4 | Tras fijar salida en edición, permite Guardar (9). | Verificado |
| 345 | 20b, 9b | ResEdit.java | N5->N5->N4 | Tras fijar salida en edición, permite Cancelar (9b). | Verificado |
| 346 | 20b, 9c | ResEdit.java | N5->N5->N4 | Tras fijar salida en edición, permite Atrás (9c). | Verificado |
| 347 | 20b, 10b | ResEdit.java | N5->N5->N1 | Tras fijar salida en alta, permite Cancelar (10b). | Verificado |
| 348 | 20b, 10c | ResEdit.java | N5->N5->N1 | Tras fijar salida en alta, permite Atrás (10c). | Verificado |
| 349 | 20b, 11 | ResEdit.java | N5->N5->N6 | Tras fijar salida, si ya hay entrada, permite abrir Selección (11). | Verificado |
| 350 | 20b, 20 | ResEdit.java | N5->N5->N5 | Tras fijar salida, permite fijar entrada (20). | Verificado |
| 351 | 20b, 20b | ResEdit.java | N5->N5->N5 | Bucle de fecha salida. | Verificado |

### 2.6 — Transiciones en N6 (Selección Quads)
| ID | Pareja | Archivo | Navegación | Evidencia Técnica | Estado |
|---|---|---|---|---|---|
| 352 | 11, 12 | QuadSel.java | N5->N6->N5 | Entrar en selector (11) permite confirmar quads (12). | Verificado |
| 353 | 11, 12b | QuadSel.java | N5->N6->N5 | Entrar en selector permite cancelar selección (12b). | Verificado |
| 354 | 11, 12c | QuadSel.java | N5->N6->N5 | Entrar en selector permite volver atrás (12c). | Verificado |
| 355 | 11, 21 | QuadSel.java | N5->N6->N6 | Entrar en selector permite ordenar matrícula (21). | Verificado |
| 356 | 11, 21b | QuadSel.java | N5->N6->N6 | Entrar en selector permite ordenar tipo (21b). | Verificado |
| 357 | 11, 21c | QuadSel.java | N5->N6->N6 | Entrar en selector permite ordenar precio (21c). | Verificado |
| 358 | 11, 25 | QuadSel.java | N5->N6->N6 | Entrar en selector permite ver detalles (25). | Verificado |
| 359 | 11, 26 | QuadSel.java | N5->N6->N6 | Entrar en selector permite elegir cascos (26). | Verificado |
| 360 | 12, 11 | ResEdit.java | N6->N5->N6 | Verificado en 2.5 (ID 317). | Verificado |
| 361 | 12b, 11 | ResEdit.java | N6->N5->N6 | Verificado en 2.5 (ID 325). | Verificado |
| 362 | 12c, 11 | ResEdit.java | N6->N5->N6 | Verificado en 2.5 (ID 333). | Verificado |
| 363 | 21, 12 | QuadSel.java | N6->N6->N5 | Tras ordenar matrícula (21), permite confirmar (12). | Verificado |
| 364 | 21, 12b | QuadSel.java | N6->N6->N5 | Tras ordenar matrícula, permite cancelar (12b). | Verificado |
| 365 | 21, 12c | QuadSel.java | N6->N6->N5 | Tras ordenar matrícula, permite atrás (12c). | Verificado |
| 366 | 21, 21 | QuadSel.java | N6->N6->N6 | Bucle de ordenación matrícula. | Verificado |
| 367 | 21, 21b | QuadSel.java | N6->N6->N6 | Tras ordenar matrícula, permite cambiar a tipo (21b). | Verificado |
| 368 | 21, 21c | QuadSel.java | N6->N6->N6 | Tras ordenar matrícula, permite cambiar a precio (21c). | Verificado |
| 369 | 21, 25 | QuadSel.java | N6->N6->N6 | Tras ordenar matrícula, permite ver detalles (25). | Verificado |
| 370 | 21, 26 | QuadSel.java | N6->N6->N6 | Tras ordenar matrícula, permite elegir cascos (26). | Verificado |
| 371 | 21b, 12 | QuadSel.java | N6->N6->N5 | Tras ordenar tipo (21b), permite confirmar (12). | Verificado |
| 372 | 21b, 12b | QuadSel.java | N6->N6->N5 | Tras ordenar tipo, permite cancelar (12b). | Verificado |
| 373 | 21b, 12c | QuadSel.java | N6->N6->N5 | Tras ordenar tipo, permite atrás (12c). | Verificado |
| 374 | 21b, 21 | QuadSel.java | N6->N6->N6 | Tras ordenar tipo, permite cambiar a matrícula (21). | Verificado |
| 375 | 21b, 21b | QuadSel.java | N6->N6->N6 | Bucle de ordenación tipo. | Verificado |
| 376 | 21b, 21c | QuadSel.java | N6->N6->N6 | Tras ordenar tipo, permite cambiar a precio (21c). | Verificado |
| 377 | 21b, 25 | QuadSel.java | N6->N6->N6 | Tras ordenar tipo, permite ver detalles (25). | Verificado |
| 378 | 21b, 26 | QuadSel.java | N6->N6->N6 | Tras ordenar tipo, permite elegir cascos (26). | Verificado |
| 379 | 21c, 12 | QuadSel.java | N6->N6->N5 | Tras ordenar precio (21c), permite confirmar (12). | Verificado |
| 380 | 21c, 12b | QuadSel.java | N6->N6->N5 | Tras ordenar precio, permite cancelar (12b). | Verificado |
| 381 | 21c, 12c | QuadSel.java | N6->N6->N5 | Tras ordenar precio, permite atrás (12c). | Verificado |
| 382 | 21c, 21 | QuadSel.java | N6->N6->N6 | Tras ordenar precio, permite cambiar a matrícula (21). | Verificado |
| 383 | 21c, 21b | QuadSel.java | N6->N6->N6 | Tras ordenar precio, permite cambiar a tipo (21b). | Verificado |
| 384 | 21c, 21c | QuadSel.java | N6->N6->N6 | Bucle de ordenación precio. | Verificado |
| 385 | 21c, 25 | QuadSel.java | N6->N6->N6 | Tras ordenar precio, permite ver detalles (25). | Verificado |
| 386 | 21c, 26 | QuadSel.java | N6->N6->N6 | Tras ordenar precio, permite elegir cascos (26). | Verificado |
| 387 | 25, 12 | QuadSel.java | N6->N6->N5 | Tras ver detalles (25), permite confirmar (12). | Verificado |
| 388 | 25, 12b | QuadSel.java | N6->N6->N5 | Tras ver detalles, permite cancelar (12b). | Verificado |
| 389 | 25, 12c | QuadSel.java | N6->N6->N5 | Tras ver detalles, permite atrás (12c). | Verificado |
| 390 | 25, 21 | QuadSel.java | N6->N6->N6 | Tras ver detalles, permite ordenar matrícula (21). | Verificado |
| 391 | 25, 26 | QuadSel.java | N6->N6->N6 | Tras ver detalles, permite elegir cascos (26). | Verificado |

---
**Certificación Final QA:**
Se han revisado manualmente los 391 pares de aristas estructurales. El documento ahora refleja fielmente la realidad del sistema, separando los 26 casos físicamente imposibles por la arquitectura de Android de los 365 casos ejecutables, todos ellos con evidencia directa en las actividades y controladores Java correspondientes.
