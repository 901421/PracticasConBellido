Repetir nodos?
Bucles?
All buttons?
AtrasFisico?

NODO A(INICIO)
    A(CrearQuad)B(Monoplaza)B
    A(CrearQuad)B(Biplaza)B
    A(CrearQuad)B(Cancelar)A
    A(CrearQuad)B(Confirmar)A
    A(ListarQuads)F(Editar)B
    A(ListarQuads)F(Borrar)E
    A(ListarQuads)F(Ordenar)F
    A(ListarReservas)G(Envio)J
    A(ListarReservas)G(Borrar)K
    A(ListarReservas)G(Detalles)L
    A(ListarReservas)G(Ordenar)M
    A(ListarReservas)G(Filtrar)N
    A(ListarReservas)G(Editar)C
    A(CrearReserva)C(Cancelar)A
    A(CrearReserva)C(Confirmar)B
    A(CrearReserva)C(FechaRecogida)D
    A(CrearReserva)C(FechaDevolucion)D

NODO B(Crear/ModificarQuad)
    B(Monoplaza)B(Biplaza)B
    B(Monoplaza)B(Monoplaza)B
    B(Monoplaza)B(Cancelar)A
    B(Monoplaza)B(Confirmar)A
    B(Monoplaza)B(CancelarL)F
    B(Monoplaza)B(ConfirmarL)F
    B(Biplaza)B(Biplaza)B
    B(Biplaza)B(Monoplaza)B
    B(Biplaza)B(Cancelar)A
    B(Biplaza)B(Confirmar)A
    B(Biplaza)B(CancelarL)F
    B(Biplaza)B(ConfirmarL)F
    B(Cancelar)A(CrearQuad)
    B(Cancelar)A(ListarQuads)
    B(Cancelar)A(ListarReservas)
    B(Cancelar)A(CrearReserva)
    B(Confirmar)A(CrearQuad)
    B(Confirmar)A(ListarQuads)
    B(Confirmar)A(ListarReservas)
    B(Confirmar)A(CrearReserva)
    B(ConfirmarL)F(Ordenar)F
    B(ConfirmarL)F(Borrar)E
    B(ConfirmarL)F(Editar)B
    B(CancelarL)F(Ordenar)F
    B(CancelarL)F(Borrar)E
    B(CancelarL)F(Editar)B

NODO C(Crear/ModificarReserva)
    C(Cancelar)A(CrearReserva)C
    C(Cancelar)A(CrearQuad)B
    C(Cancelar)A(ListarQuad)F
    C(Cancelar)A(ListarReserva)G
    C(Confirmar)A(CrearReserva)C
    C(Confirmar)A(CrearQuad)B
    C(Confirmar)A(ListarQuad)F
    C(Confirmar)A(ListarReserva)G
    C(CancelarL)G(Editar)C
    C(CancelarL)G(Envio)J
    C(CancelarL)G(Borrar)K
    C(CancelarL)G(Detalles)L
    C(CancelarL)G(Ordenar)M
    C(CancelarL)G(Filtrar)N
    C(ConfirmarL)G(Editar)C
    C(ConfirmarL)G(Envio)J
    C(ConfirmarL)G(Borrar)K
    C(ConfirmarL)G(Detalles)L
    C(ConfirmarL)G(Ordenar)M
    C(ConfirmarL)G(Filtrar)N
    C(FechaRecogida)D(Confirmar)C
    C(FechaRecogida)D(Cancelar)C
    C(FechaRecogida)D(SelectQuads)H
    C(FechaDevolucion)D(Confirmar)C
    C(FechaDevolucion)D(Cancelar)C
    C(FechaDevolucion)D(SelectQuads)H

NODO D(DatePicker)
    D(Confirmar)C(Cancelar)A
    D(Confirmar)C(Confirmar)A
    D(Confirmar)C(CancelarL)G
    D(Confirmar)C(ConfirmarL)G
    D(Confirmar)C(FechaRecogida)D
    D(Confirmar)C(FechaDevolucion)D
    D(Cancelar)C(Cancelar)A
    D(Cancelar)C(Confirmar)A
    D(Cancelar)C(CancelarL)G
    D(Cancelar)C(ConfirmarL)G
    D(Cancelar)C(FechaRecogida)D
    D(Cancelar)C(FechaDevolucion)D
    D(SelectQuads)H(Confirmar)D
    D(SelectQuads)H(Cancelar)D
    D(SelectQuads)H(CuadroQuad)H
    D(SelectQuads)H(Cascos)I
    D(SelectQuads)H(Ordenar)H
    D(SelectQuads)H(Ver)O

NODO E(ConfirmarBorradoQuad)
    E(Cancelar)F(Borrar)E
    E(Cancelar)F(Editar)B
    E(Cancelar)F(OrdenarF)
    E(Confirmar)F(Borrar)E
    E(Confirmar)F(Editar)B
    E(Confirmar)F(OrdenarF)

NODO F(ListaQuads)
    F(Borrar)E(Cancelar)F
    F(Borrar)E(Confirmar)F
    F(Editar)B(CancelarL)F
    F(Editar)B(ConfirmarL)F
    F(Editar)B(Monoplaza)B
    F(Editar)B(Biplaza)B
    F(Ordenar)F(ordenar)F
    F(Ordenar)F(Borrar)E
    F(Ordenar)F(Editar)B

NODO G(ListarReservas)
    G(Envio)J(SMS)G
    G(Envio)J(Whatsapp)G
    G(Borrar)K(Confirmar)G
    G(Borrar)K(Cancelar)G
    G(Detalles)L(Cerrar)G
    G(Ordenar)M(Nombre)G
    G(Ordenar)M(Telefono)G
    G(Ordenar)M(FechaRecogida)G
    G(Ordenar)M(FechaDevolucion)G
    G(Filtrar)N(Caducados)G
    G(Filtrar)N(Previstos)G
    G(Filtrar)N(Vigentes)G
    G(Filtrar)N(Todos)G
    G(Editar)C(CrearReserva)A
    G(Editar)C(ConfirmarL)G
    G(Editar)C(CancelarL)G
    G(Editar)C(FechaRecogida)D
    G(Editar)C(FechaDevolucion)D

NODO H(SelectQuads)
    H(CuadroQuad)H(CuadroQuad)H
    H(CuadroQuad)H(Confirmar)D
    H(CuadroQuad)H(Cancelar)D
    H(CuadroQuad)H(Cascos)I
    H(CuadroQuad)H(Ordenar)H
    H(CuadroQuad)H(Ver)O
    H(Confirmar)D(SelectQuads)H
    H(Confirmar)D(Confirmar)C
    H(Confirmar)D(Cancelar)C
    H(Cancelar)D(SelectQuads)H
    H(Cancelar)D(Confirmar)C
    H(Cancelar)D(Cancelar)C
    H(Cascos)I(NumeroCascos)H
    H(Ordenar)H(CuadroQuad)H
    H(Ordenar)H(Confirmar)D
    H(Ordenar)H(Cancelar)D
    H(Ordenar)H(Cascos)I
    H(Ordenar)H(Ordenar)H
    H(Ordenar)H(Ver)O
    H(Ver)O(Cerrar)H

NODO I(SelectorCascos)
    I(NumeroCascos)H(Cascos)I
    I(NumeroCascos)H(CuadroQuad)H
    I(NumeroCascos)H(Confirmar)D
    I(NumeroCascos)H(Cancelar)D
    I(NumeroCascos)H(Ordenar)H
    I(NumeroCascos)H(Ver)O

NODO J(SelectorEnvio)
    J(SMS)G(Envio)J
    J(SMS)G(Borrar)K
    J(SMS)G(Detalles)L
    J(SMS)G(Ordenar)M
    J(SMS)G(Filtrar)N
    J(SMS)G(Editar)C
    J(Whatsapp)G(Envio)J
    J(Whatsapp)G(Borrar)K
    J(Whatsapp)G(Detalles)L
    J(Whatsapp)G(Ordenar)M
    J(Whatsapp)G(Filtrar)N
    J(Whatsapp)G(Editar)C

NODO K(ConfirmarBorradoReserva)
    K(Confirmar)G(Envio)J
    K(Confirmar)G(Borrar)K
    K(Confirmar)G(Detalles)L
    K(Confirmar)G(Ordenar)M
    K(Confirmar)G(Filtrar)N
    K(Confirmar)G(Editar)C
    K(Cancelar)G(Envio)J
    K(Cancelar)G(Borrar)K
    K(Cancelar)G(Detalles)L
    K(Cancelar)G(Ordenar)M
    K(Cancelar)G(Filtrar)N
    K(Cancelar)G(Editar)C

NODO L(DetallesReservas)
    L(Cerrar)G(Envio)J
    L(Cerrar)G(Borrar)K
    L(Cerrar)G(Detalles)L
    L(Cerrar)G(Ordenar)M
    L(Cerrar)G(Filtrar)N
    L(Cerrar)G(Editar)C
    
NODO M(Ordenar)
    M(Telefono)G(Envio)J
    M(Telefono)G(Borrar)K
    M(Telefono)G(Detalles)L
    M(Telefono)G(Ordenar)M
    M(Telefono)G(Filtrar)N
    M(Telefono)G(Editar)C
    M(Nombre)G(Envio)J
    M(Nombre)G(Borrar)K
    M(Nombre)G(Detalles)L
    M(Nombre)G(Ordenar)M
    M(Nombre)G(Filtrar)N
    M(Nombre)G(Editar)C
    M(FechaRecogida)G(Envio)J
    M(FechaRecogida)G(Borrar)K
    M(FechaRecogida)G(Detalles)L
    M(FechaRecogida)G(Ordenar)M
    M(FechaRecogida)G(Filtrar)N
    M(FechaRecogida)G(Editar)C
    M(FechaDevolucion)G(Envio)J
    M(FechaDevolucion)G(Borrar)K
    M(FechaDevolucion)G(Detalles)L
    M(FechaDevolucion)G(Ordenar)M
    M(FechaDevolucion)G(Filtrar)N
    M(FechaDevolucion)G(Editar)C

NODO N(Filtrar)
    N(Caducadas)G(Envio)J
    N(Caducadas)G(Borrar)K
    N(Caducadas)G(Detalles)L
    N(Caducadas)G(Ordenar)M
    N(Caducadas)G(Filtrar)N
    N(Caducadas)G(Editar)C
    N(Previstas)G(Envio)J
    N(Previstas)G(Borrar)K
    N(Previstas)G(Detalles)L
    N(Previstas)G(Ordenar)M
    N(Previstas)G(Filtrar)N
    N(Previstas)G(Editar)C
    N(Vigentes)G(Envio)J
    N(Vigentes)G(Borrar)K
    N(Vigentes)G(Detalles)L
    N(Vigentes)G(Ordenar)M
    N(Vigentes)G(Filtrar)N
    N(Vigentes)G(Editar)C
    N(Todas)G(Envio)J
    N(Todas)G(Borrar)K
    N(Todas)G(Detalles)L
    N(Todas)G(Ordenar)M
    N(Todas)G(Filtrar)N
    N(Todas)G(Editar)C

NODO O(DetallesQuad)
    O(Cerrar)H(CuadroQuad)H
    O(Cerrar)H(Confirmar)D
    O(Cerrar)H(Cancelar)D
    O(Cerrar)H(Cascos)I
    O(Cerrar)H(Ordenar)H
    O(Cerrar)H(Ver)O

    
    



