# Estudio de Particiones de Equivalencia y Casos de Prueba

Este documento detalla el análisis de Particiones de Equivalencia para cada caso de uso.

## CU-1: Caso de Uso "Crear Quad"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-4, Inválidas: 5-8).

| Variable | Particiones Válidas | Particiones Inválidas |
| :--- | :--- | :--- |
| **Matrícula** | **(1)** Formato `"1234-ABC"` | **(5)** Vacía `""` <br> **(6)** Formato incorrecto |
| **Precio** | **(2)** Valor numérico > 0 | **(7)** Vacío o No numérico <br> **(8)** Valor <= 0 |
| **Descripción**| **(3)** Cualquier texto / Vacío / null (Siempre Válida) | - |
| **Tipo** | **(4)** Monoplaza / Biplaza (Siempre Válida) | - |

### 2. Tabla de Casos de Prueba

#### Sección: Prueba Validaciones Positivas (Aislamiento)
| ID | Matrícula | Precio | Descripción | Tipo | Particiones Verificadas | Salida Esperada |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-VP-01** | `"1234-AAA"` | -5.0 | `"Quad Rojo"` | `Monoplaza` | **1, 3, 4** | Error en Precio. Matrícula sin error. |
| **CP-VP-02** | `null` | 50.0 | null | `Biplaza` | **2, 3, 4** | Error en Matrícula. Precio sin error. |

#### Sección: Prueba de Invalidadas (Combinadas)
| ID | Matrícula | Precio | Descripción | Tipo | Particiones Verificadas | Salida Esperada |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-IN-01** | `null` | `null` | `null` | `Monoplaza` | **5, 7** | Error: Matrícula y Precio obligatorios. |
| **CP-IN-02** | `"1234 AAA"` | 0 | null | `Biplaza` | **6, 8** | Error: Formato matrícula incorrecto, El precio debe ser > 0. |

---

## CU-2: Caso de Uso "Eliminar Quad"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1, Inválidas: 2).

| Variable | Particiones Válidas | Particiones Inválidas |
| :--- | :--- | :--- |
| **Existencia** | **(1)** El Quad existe en el sistema | **(2)** El Quad NO existe |

### 2. Tabla de Casos de Prueba

#### Sección: Prueba Validaciones Positivas (Aislamiento)
| ID | Quad Seleccionado | Particiones Verificadas | Salida Esperada |
| :--- | :--- | :--- | :--- |
| **CP-VP-03** | 10 | **1** | Quad marcado como inactivo con éxito. |

#### Sección: Prueba de Invalidadas (Combinadas)
| ID | Quad Seleccionado | Particiones Verificadas | Salida Esperada |
| :--- | :--- | :--- | :--- |
| **CP-IN-03** | 999 | **2** | Error: Vehículo no encontrado. |

---

## CU-3: Caso de Uso "Modificar Información Quad"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-5, Inválidas: 6-10).

| Variable | Particiones Válidas | Particiones Inválidas |
| :--- | :--- | :--- |
| **Existencia** | **(1)** Quad existe | **(6)** Quad no existe |
| **Matrícula** | **(2)** Formato `"1234-ABC"` | **(7)** Vacía / Formato incorrecto |
| **Precio** | **(3)** Valor numérico > 0 | **(8)** Vacío / No numérico <br> **(9)** Valor <= 0 |
| **Descripción**| **(4)** Siempre válida | - |
| **Tipo** | **(5)** Siempre válida | - |

### 2. Tabla de Casos de Prueba

#### Sección: Prueba Validaciones Positivas (Aislamiento)
| ID | ID Quad | Matrícula | Precio | Descripción | Tipo | Particiones Verificadas | Salida Esperada |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-VP-04** | 10 | `"1-A"` | -1 | `"Edit"` | `Biplaza` | **1, 4, 5** | Error Matrícula/Precio. ID Quad OK. |
| **CP-VP-05** | 999 | `"1234-BBB"`| -1 | `"Edit"` | `Monoplaza` | **2, 4, 5** | Error ID/Precio. Matrícula OK. |
| **CP-VP-06** | 999 | `"1-A"` | 60.0 | `"Edit"` | `Monoplaza` | **3, 4, 5** | Error ID/Matrícula. Precio OK. |

#### Sección: Prueba de Invalidadas (Combinadas)
| ID | ID Quad | Matrícula | Precio | Particiones Verificadas | Salida Esperada |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-IN-04** | 999 | `null` | `"abc"` | **6, 7, 8** | Error: Quad no existe, Matrícula vacía, Precio no numérico. |

---

## CU-4: Caso de Uso "Consultar Listado Quads"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-3).

| Variable | Particiones Válidas | Particiones Inválidas |
| :--- | :--- | :--- |
| **Criterio Orden** | **(1)** `"Matrícula"` <br> **(2)** `"Tipo"` <br> **(3)** `"Precio"` | - |

### 2. Tabla de Casos de Prueba

#### Sección: Prueba Validaciones Positivas (Aislamiento)
| ID | Criterio Seleccionado | Particiones Verificadas | Salida Esperada |
| :--- | :--- | :--- | :--- |
| **CP-VP-07** | `"Matrícula"` | **1** | Lista mostrada ordenada por matrícula. |
| **CP-VP-08** | `"Tipo"` | **2** | Lista mostrada ordenada por tipo. |
| **CP-VP-09** | `"Precio"` | **3** | Lista mostrada ordenada por precio. |

---

## CU-5: Caso de Uso "Consultar Listado Reservas"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-8).

| Variable | Particiones Válidas | Particiones Inválidas |
| :--- | :--- | :--- |
| **Criterio Orden** | **(1)** `"Cliente"`, **(2)** `"Móvil"`, **(3)** `"Recogida"`, **(4)** `"Devolución"` | - |
| **Filtro Estado** | **(5)** `"Previstas"`, **(6)** `"Vigentes"`, **(7)** `"Caducadas"`, **(8)** `"Totales"` | - |

### 2. Tabla de Casos de Prueba

#### Sección: Prueba Validaciones Positivas (Aislamiento)
| ID | Orden | Filtro | Particiones Verificadas | Salida Esperada |
| :--- | :--- | :--- | :--- | :--- |
| **CP-VP-10** | `"Cliente"` | `"Totales"` | **1, 8** | Lista total ordenada por cliente. |
| **CP-VP-11** | `"Recogida"`| `"Vigentes"`| **3, 6** | Reservas vigentes ordenadas por recogida. |

---

## CU-6: Caso de Uso "Crear Reserva"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-8, Inválidas: 9-16).

| Variable | Particiones Válidas | Particiones Inválidas |
| :--- | :--- | :--- |
| **Nombre Cliente**| **(1)** Texto no vacío | **(9)** Vacío `""` |
| **Número Móvil** | **(2)** 9 dígitos numéricos | **(10)** Vacío `""` <br> **(11)** Formato incorrecto / longitud != 9 |
| **Fecha Recogida** | **(3)** Fecha seleccionada | **(12)** No seleccionada |
| **Fecha Devolución**| **(4)** Fecha seleccionada <br> **(5)** Fecha >= Recogida | **(13)** No seleccionada <br> **(14)** Fecha < Recogida |
| **Selección Quads** | **(6)** Selección > 0 <br> **(7)** Quad disponible (sin solape) | **(15)** 0 Quads seleccionados <br> **(16)** Quad ya reservado (Solape) |
| **Cascos** | **(8)** Según capacidad (Mono: 0-1, Bi: 0-2) | **(17)** Excede capacidad del Quad |

### 2. Tabla de Casos de Prueba

#### Sección: Prueba Validaciones Positivas (Aislamiento)
| ID | Cliente | Móvil | F. In | F. Out | Quads | Cascos | Particiones | Salida |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-VP-12** | `"Juan"` | `"123"` | null | null | 0 | 0 | **1** | Errores resto. Cliente OK. |
| **CP-VP-13** | `null` | `"600111222"` | null | null | 0 | 0 | **2** | Errores resto. Móvil OK. |
| **CP-VP-14** | `null` | `"123"` | `"20-05-2025"`| null | 0 | 0 | **3** | Errores resto. Recogida OK. |
| **CP-VP-15** | `null` | `"123"` | null | `"22-05-2025"`| 0 | 0 | **4** | Errores resto. Devolución OK. |
| **CP-VP-16** | `null` | `"123"` | `"20-05-2025"`| `"20-05-2025"`| 0 | 0 | **5** | Errores resto. Coherencia OK. |
| **CP-VP-17** | `null` | `"123"` | null | null | 1 | 1 | **6, 7, 8** | Errores resto. Quads/Cascos OK. |

#### Sección: Prueba de Invalidadas (Combinadas)
| ID | Cliente | Móvil | F. In | F. Out | Quads | Cascos | Particiones | Salida |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-IN-05** | `null` | `null` | null | null | 0 | 0 | **9, 10, 12, 13, 15** | Error: Múltiples obligatorios faltantes. |
| **CP-IN-06** | `"Ana"` | `"abc"` | `"22-05-2025"`| `"20-05-2025"`| 1 | 0 | **11, 14** | Error: Formato móvil y orden fechas. |
| **CP-IN-07** | `"Test"`| `"600"` | `"20-05-2025"`| `"21-05-2025"`| 1 | 3 | **16, 17** | Error: Quad ocupado y exceso cascos. |

---

## CU-7: Caso de Uso "Modificar Información Reserva"

### 1. Identificación de Particiones
IDs incrementales (Válidas: 1-9, Inválidas: 10-18).

| Variable | Particiones Válidas | Particiones Inválidas |
| :--- | :--- | :--- |
| **Existencia** | **(1)** Reserva existe (ID <= 20000) | **(10)** Reserva no existe (ID > 20000) |
| **RF12 Mantenim.** | **(2)** Precio histórico se mantiene | - |
| **Campos (RF5/6)**| **(3..9)** Igual que CU-6 (V1..V8) | **(11..18)** Igual que CU-6 (I9..I17) |

### 2. Tabla de Casos de Prueba (Validación Positiva Aislamiento)
| ID | ID Res | Acción | Particiones | Salida Esperada |
| :--- | :--- | :--- | :--- | :--- |
| **CP-VP-18** | 1001 | Cambiar precio quad en catálogo | **1, 2** | La reserva mantiene el precio original calculado. |

---

## CU-8: Caso de Uso "Eliminar Reserva"

### 1. Identificación de Particiones
| Variable | Particiones Válidas (1) | Particiones Inválidas (2) |
| :--- | :--- | :--- |
| **Existencia** | **(1)** Reserva existe (ID <= 20000) | **(2)** Reserva no existe |

### 2. Tabla de Casos de Prueba
| ID | ID Reserva | Particiones | Salida Esperada |
| :--- | :--- | :--- | :--- |
| **CP-VP-19** | 500 | **1** | Reserva borrada lógicamente (inactiva). |
| **CP-IN-08** | 99999 | **2** | Error: Reserva no encontrada. |

---

## CU-9: Caso de Uso "Enviar Información"

### 1. Identificación de Particiones
| Variable | Particiones Válidas (1) | Particiones Inválidas (2) |
| :--- | :--- | :--- |
| **Móvil Cliente**| **(1)** 9 dígitos numéricos | **(2)** Vacío o Formato incorrecto |

### 2. Tabla de Casos de Prueba
| ID | Teléfono Destino | Particiones | Salida Esperada |
| :--- | :--- | :--- | :--- |
| **CP-VP-20** | `"600111222"` | **1** | Mensaje enviado con éxito. |
| **CP-IN-09** | `"123"` | **2** | Error: Teléfono de cliente no válido para envío. |

---

## CU-10: Caso de Uso "Calcular Precio" (Include)

### 1. Identificación de Particiones
| Variable | Particiones Válidas (1..3) | Particiones Inválidas (4..5) |
| :--- | :--- | :--- |
| **Días** | **(1)** Diferencia (Out - In) | **(4)** Fechas no seleccionadas |
| **Cálculo** | **(2)** Multiplicación (Días * Precio) | **(5)** Precio quad es 0 o null |
| **Acumulado** | **(3)** Suma de todos los quads sel. | - |

### 2. Tabla de Casos de Prueba
| ID | Días | Precio Quad | Total Esperado | Particiones |
| :--- | :--- | :--- | :--- | :--- |
| **CP-VP-21** | 3 | 50.0 | 150.0 | **1, 2** |
| **CP-VP-22** | 1 | 70.0 | 70.0 | **3** |
