FROM python:3.9-slim

WORKDIR /app

COPY server.py storage.py ./

# Crear el directorio para la persistencia
RUN mkdir -p /app/storage

EXPOSE 5555

CMD ["python", "server.py"]
