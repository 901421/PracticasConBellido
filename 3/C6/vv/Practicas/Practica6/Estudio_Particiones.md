# Estudio de Particiones de Equivalencia y Casos de Prueba

Este documento detalla el análisis de Particiones de Equivalencia para cada caso de uso. Los casos de prueba se dividen estrictamente en dos tablas: una para combinaciones de particiones válidas (Camino Feliz) y otra para combinaciones de particiones inválidas (Caminos de Error), agrupando múltiples particiones en cada caso para optimizar el número de pruebas sin cruzar referencias entre ambas categorías.

---

## CU-1: Caso de Uso "Crear Quad"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-4, Inválidas: 5-8).

| Variable        | Particiones Válidas                    | Particiones Inválidas                               |
| :-------------- | :------------------------------------- | :-------------------------------------------------- |
| **Matrícula**   | **(1)** Formato `"1234-ABC"`           | **(5)** Vacía `""` <br> **(6)** Formato incorrecto  |
| **Precio**      | **(2)** Valor numérico > 0             | **(7)** Vacío o No numérico <br> **(8)** Valor <= 0 |
| **Descripción** | **(3)** Cualquier texto / Vacío / null | -                                                   |
| **Tipo**        | **(4)** Monoplaza / Biplaza            | -                                                   |

### 2. Tablas de Casos de Prueba

#### Casos de Prueba Válidos
| ID          | Matrícula    | Precio | Descripción   | Tipo        | Particiones Verificadas | Salida Esperada        |
| :---------- | :----------- | :----- | :------------ | :---------- | :---------------------- | :--------------------- |
| **CP-V-01** | `"1234-AAA"` | 50.0   | `"Quad Rojo"` | `Monoplaza` | **1, 2, 3, 4**          | Quad creado con éxito. |

#### Casos de Prueba Inválidos
| ID          | Matrícula | Precio | Descripción | Tipo        | Particiones Verificadas | Salida Esperada                                      |
| :---------- | :-------- | :----- | :---------- | :---------- | :---------------------- | :--------------------------------------------------- |
| **CP-I-01** | `""`      | `null` | `"Desc"`    | `Monoplaza` | **5, 7**                | Error: Matrícula obligatoria y Precio requerido.     |
| **CP-I-02** | `"1AA1"`  | -5.0   | `"Desc"`    | `Biplaza`   | **6, 8**                | Error: Formato de matrícula incorrecto y Precio > 0. |

---

## CU-2: Caso de Uso "Eliminar Quad"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1, Inválidas: 2).

| Variable       | Particiones Válidas                  | Particiones Inválidas     |
| :------------- | :----------------------------------- | :------------------------ |
| **Existencia** | **(1)** El Quad existe en el sistema | **(2)** El Quad NO existe |

### 2. Tablas de Casos de Prueba

#### Casos de Prueba Válidos
| ID          | Quad Seleccionado     | Particiones Verificadas | Salida Esperada                       |
| :---------- | :-------------------- | :---------------------- | :------------------------------------ |
| **CP-V-02** | ID Existente (ej. 10) | **1**                   | Quad marcado como inactivo con éxito. |

#### Casos de Prueba Inválidos
| ID          | Quad Seleccionado        | Particiones Verificadas | Salida Esperada                |
| :---------- | :----------------------- | :---------------------- | :----------------------------- |
| **CP-I-03** | ID Inexistente (ej. 999) | **2**                   | Error: Vehículo no encontrado. |

---

## CU-3: Caso de Uso "Modificar Información Quad"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-5, Inválidas: 6-9).

| Variable        | Particiones Válidas                    | Particiones Inválidas                               |
| :-------------- | :------------------------------------- | :-------------------------------------------------- |
| **Existencia**  | **(1)** Quad existe                    | **(6)** Quad no existe                              |
| **Matrícula**   | **(2)** Formato `"1234-ABC"`           | **(7)** Vacía / Formato incorrecto                  |
| **Precio**      | **(3)** Valor numérico > 0             | **(8)** Vacío / No numérico <br> **(9)** Valor <= 0 |
| **Descripción** | **(4)** Cualquier texto / Vacío / null | -                                                   |
| **Tipo**        | **(5)** Monoplaza / Biplaza            | -                                                   |

### 2. Tablas de Casos de Prueba

#### Casos de Prueba Válidos
| ID          | ID Quad | Matrícula    | Precio | Descripción | Tipo      | Particiones Verificadas | Salida Esperada            |
| :---------- | :------ | :----------- | :----- | :---------- | :-------- | :---------------------- | :------------------------- |
| **CP-V-03** | 10      | `"5555-BBB"` | 60.0   | `"Editado"` | `Biplaza` | **1, 2, 3, 4, 5**       | Quad modificado con éxito. |

#### Casos de Prueba Inválidos
| ID          | ID Quad | Matrícula | Precio | Descripción | Tipo        | Particiones Verificadas | Salida Esperada                                                   |
| :---------- | :------ | :-------- | :----- | :---------- | :---------- | :---------------------- | :---------------------------------------------------------------- |
| **CP-I-04** | 999     | `""`      | `null` | `"Edit"`    | `Monoplaza` | **6, 7, 8**             | Errores: Quad no existe, Matrícula obligatoria, Precio requerido. |
| **CP-I-05** | 10      | `"1-A"`   | -1.0   | `"Edit"`    | `Monoplaza` | **7, 9**                | Errores: Formato de matrícula incorrecto, Precio > 0.             |

---

## CU-4: Caso de Uso "Consultar Listado Quads"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-3). No aplican particiones inválidas dado que la entrada es a través de controles cerrados de UI.

| Variable           | Particiones Válidas                                                 | Particiones Inválidas |
| :----------------- | :------------------------------------------------------------------ | :-------------------- |
| **Criterio Orden** | **(1)** `"Matrícula"` <br> **(2)** `"Tipo"` <br> **(3)** `"Precio"` | -                     |

### 2. Tablas de Casos de Prueba

#### Casos de Prueba Válidos
| ID          | Criterio Seleccionado | Particiones Verificadas | Salida Esperada                        |
| :---------- | :-------------------- | :---------------------- | :------------------------------------- |
| **CP-V-04** | `"Matrícula"`         | **1**                   | Lista mostrada ordenada por matrícula. |
| **CP-V-05** | `"Tipo"`              | **2**                   | Lista mostrada ordenada por tipo.      |
| **CP-V-06** | `"Precio"`            | **3**                   | Lista mostrada ordenada por precio.    |

---

## CU-5: Caso de Uso "Consultar Listado Reservas"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-8). No aplican particiones inválidas (controles cerrados).

| Variable           | Particiones Válidas                                                                     | Particiones Inválidas |
| :----------------- | :-------------------------------------------------------------------------------------- | :-------------------- |
| **Criterio Orden** | **(1)** `"Cliente"`, **(2)** `"Móvil"`, **(3)** `"Recogida"`, **(4)** `"Devolución"`    | -                     |
| **Filtro Estado**  | **(5)** `"Previstas"`, **(6)** `"Vigentes"`, **(7)** `"Caducadas"`, **(8)** `"Totales"` | -                     |

### 2. Tablas de Casos de Prueba

#### Casos de Prueba Válidos
| ID          | Orden          | Filtro        | Particiones Verificadas | Salida Esperada                              |
| :---------- | :------------- | :------------ | :---------------------- | :------------------------------------------- |
| **CP-V-07** | `"Cliente"`    | `"Totales"`   | **1, 8**                | Lista total ordenada por cliente.            |
| **CP-V-08** | `"Móvil"`      | `"Previstas"` | **2, 5**                | Reservas previstas ordenadas por móvil.      |
| **CP-V-09** | `"Recogida"`   | `"Vigentes"`  | **3, 6**                | Reservas vigentes ordenadas por recogida.    |
| **CP-V-10** | `"Devolución"` | `"Caducadas"` | **4, 7**                | Reservas caducadas ordenadas por devolución. |

---

## CU-6: Caso de Uso "Crear Reserva"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-8, Inválidas: 9-17).

| Variable             | Particiones Válidas                                             | Particiones Inválidas                                                   |
| :------------------- | :-------------------------------------------------------------- | :---------------------------------------------------------------------- |
| **Nombre Cliente**   | **(1)** Texto no vacío                                          | **(9)** Vacío `""`                                                      |
| **Número Móvil**     | **(2)** 9 dígitos numéricos                                     | **(10)** Vacío `""` <br> **(11)** Formato incorrecto / longitud != 9    |
| **Fecha Recogida**   | **(3)** Fecha seleccionada                                      | **(12)** No seleccionada                                                |
| **Fecha Devolución** | **(4)** Fecha seleccionada <br> **(5)** Fecha >= Recogida       | **(13)** No seleccionada <br> **(14)** Fecha < Recogida                 |
| **Selección Quads**  | **(6)** Selección > 0 <br> **(7)** Quad disponible (sin solape) | **(15)** 0 Quads seleccionados <br> **(16)** Quad ya reservado (Solape) |
| **Cascos**           | **(8)** Según capacidad (Mono: 0-1, Bi: 0-2)                    | **(17)** Excede capacidad del Quad                                      |

### 2. Tablas de Casos de Prueba

#### Casos de Prueba Válidos
| ID          | Cliente  | Móvil         | F. In      | F. Out     | Quads     | Cascos | Particiones Verificadas    | Salida Esperada              |
| :---------- | :------- | :------------ | :--------- | :--------- | :-------- | :----- | :------------------------- | :--------------------------- |
| **CP-V-11** | `"Juan"` | `"600111222"` | `20/05/25` | `22/05/25` | 1 (Libre) | 1      | **1, 2, 3, 4, 5, 6, 7, 8** | Reserva creada exitosamente. |

#### Casos de Prueba Inválidos
| ID          | Cliente | Móvil   | F. In      | F. Out     | Quads    | Cascos | Particiones Verificadas | Salida Esperada                                                             |
| :---------- | :------ | :------ | :--------- | :--------- | :------- | :----- | :---------------------- | :-------------------------------------------------------------------------- |
| **CP-I-06** | `""`    | `""`    | `null`     | `null`     | 0        | 0      | **9, 10, 12, 13, 15**   | Múltiples errores: campos obligatorios faltantes y 0 quads.                 |
| **CP-I-07** | `"Ana"` | `"123"` | `22/05/25` | `20/05/25` | 1 (Ocup) | 3      | **11, 14, 16, 17**      | Múltiples errores: formato móvil, fechas invertidas, solape, exceso cascos. |

---

## CU-7: Caso de Uso "Modificar Información Reserva"

### 1. Identificación de Particiones
IDs incrementales. Se listan explícitamente todas las variables involucradas en la modificación.

| Variable             | Particiones Válidas                                             | Particiones Inválidas                                                   |
| :------------------- | :-------------------------------------------------------------- | :---------------------------------------------------------------------- |
| **Existencia**       | **(1)** Reserva existe                                          | **(10)** Reserva no existe                                              |
| **RF12 Mantenim.**   | **(2)** Precio histórico se mantiene inalterado                 | -                                                                       |
| **Nombre Cliente**   | **(3)** Texto no vacío                                          | **(11)** Vacío `""`                                                     |
| **Número Móvil**     | **(4)** 9 dígitos numéricos                                     | **(12)** Vacío `""` <br> **(13)** Formato incorrecto / longitud != 9    |
| **Fecha Recogida**   | **(5)** Fecha seleccionada                                      | **(14)** No seleccionada                                                |
| **Fecha Devolución** | **(6)** Fecha seleccionada <br> **(7)** Fecha >= Recogida       | **(15)** No seleccionada <br> **(16)** Fecha < Recogida                 |
| **Selección Quads**  | **(8)** Selección > 0 <br> **(9)** Quad disponible (sin solape) | **(17)** 0 Quads seleccionados <br> **(18)** Quad ya reservado (Solape) |
| **Cascos**           | **(10)** Según capacidad (Mono: 0-1, Bi: 0-2)                   | **(19)** Excede capacidad del Quad                                      |

*Nota: La partición 10 ahora corresponde a Cascos válidos, por lo que las inválidas van del 11 al 19.*

### 2. Tablas de Casos de Prueba

#### Casos de Prueba Válidos
| ID          | ID Res | Cliente   | Móvil         | F. In      | F. Out     | Quads     | Cascos | Particiones Verificadas           | Salida Esperada                                  |
| :---------- | :----- | :-------- | :------------ | :--------- | :--------- | :-------- | :----- | :-------------------------------- | :----------------------------------------------- |
| **CP-V-12** | 1001   | `"Pedro"` | `"611222333"` | `25/05/25` | `26/05/25` | 1 (Libre) | 2 (Bi) | **1, 2, 3, 4, 5, 6, 7, 8, 9, 10** | Reserva modificada. Precio histórico inalterado. |

#### Casos de Prueba Inválidos
| ID          | ID Res | Cliente | Móvil   | F. In      | F. Out     | Quads    | Cascos | Particiones Verificadas | Salida Esperada                                                             |
| :---------- | :----- | :------ | :------ | :--------- | :--------- | :------- | :----- | :---------------------- | :-------------------------------------------------------------------------- |
| **CP-I-08** | 99999  | `"Ana"` | `"123"` | `25/05/25` | `26/05/25` | 1        | 1      | **10, 13**              | Errores: Reserva no existe, Formato móvil incorrecto.                       |
| **CP-I-09** | 1001   | `""`    | `""`    | `null`     | `null`     | 0        | 0      | **11, 12, 14, 15, 17**  | Múltiples errores: campos obligatorios faltantes y 0 quads.                 |
| **CP-I-10** | 1001   | `"Ana"` | `"123"` | `26/05/25` | `25/05/25` | 1 (Ocup) | 3      | **13, 16, 18, 19**      | Múltiples errores: formato móvil, fechas invertidas, solape, exceso cascos. |

---

## CU-8: Caso de Uso "Eliminar Reserva"

### 1. Identificación de Particiones
| Variable       | Particiones Válidas    | Particiones Inválidas     |
| :------------- | :--------------------- | :------------------------ |
| **Existencia** | **(1)** Reserva existe | **(2)** Reserva no existe |

### 2. Tablas de Casos de Prueba

#### Casos de Prueba Válidos
| ID          | ID Reserva | Particiones Verificadas | Salida Esperada                         |
| :---------- | :--------- | :---------------------- | :-------------------------------------- |
| **CP-V-13** | 500        | **1**                   | Reserva borrada lógicamente (inactiva). |

#### Casos de Prueba Inválidos
| ID          | ID Reserva | Particiones Verificadas | Salida Esperada               |
| :---------- | :--------- | :---------------------- | :---------------------------- |
| **CP-I-11** | 99999      | **2**                   | Error: Reserva no encontrada. |

---

## CU-9: Caso de Uso "Enviar Información"

### 1. Identificación de Particiones
Dado que el número de móvil proviene de una reserva ya validada en el sistema, las variables no dependen de la entrada de texto manual del usuario, sino de la selección de una opción predefinida en la interfaz para una reserva existente.

| Variable         | Particiones Válidas                                     | Particiones Inválidas |
| :--------------- | :------------------------------------------------------ | :-------------------- |
| **Método Envío** | **(1)** Enviar por SMS <br> **(2)** Enviar por WhatsApp | -                     |

*Nota: No existen particiones inválidas de entrada de datos, ya que el sistema solo permite seleccionar opciones predefinidas sobre datos (teléfono) previamente validados en los CU-6 y CU-7.*

### 2. Tablas de Casos de Prueba

#### Casos de Prueba Válidos
| ID          | Método Seleccionado | Particiones Verificadas | Salida Esperada                                                      |
| :---------- | :------------------ | :---------------------- | :------------------------------------------------------------------- |
| **CP-V-14** | `SMS`               | **1**                   | Aplicación SMS abierta con los datos de la reserva precargados.      |
| **CP-V-15** | `WhatsApp`          | **2**                   | Aplicación WhatsApp abierta con los datos de la reserva precargados. |
