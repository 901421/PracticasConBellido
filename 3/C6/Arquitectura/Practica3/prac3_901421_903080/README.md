# Broker de Mensajes (MOM) - Práctica 3

Este proyecto implementa un Broker de mensajes asíncrono con soporte para persistencia, distribución Fair Dispatch y resiliencia ante fallos.

## Requisitos
- Python 3.x instalado.

## Instrucciones de Ejecución

Siga estos pasos en terminales separadas para poner en marcha el entorno completo:

### 1. Iniciar el Servidor (Broker)
El servidor gestiona las colas, la persistencia en disco (`broker_storage.json`) y la distribución de mensajes.
```bash
python server.py
```

### 2. Iniciar el Productor
El productor declarará una cola de pruebas y comenzará a inyectar eventos de telemetría cada 2 segundos.
```bash
python productor.py
```

### 3. Iniciar el Consumidor
El consumidor se suscribirá a la cola y procesará los mensajes enviados por el productor.
```bash
python consumidor.py
```

## Ejecución de Pruebas (Tests)
Para validar el funcionamiento de todos los componentes (incluyendo concurrencia y timeouts), ejecute la suite de pruebas automatizada:
```bash
python test_main.py
```

## Estructura de Archivos Principal
- `server.py`: Núcleo del broker.
- `client.py`: Librería cliente (API) para productores y consumidores.
- `storage.py`: Gestor de persistencia atómica en JSON.
- `Memoria.pdf`: Documentación técnica del diseño y arquitectura.
