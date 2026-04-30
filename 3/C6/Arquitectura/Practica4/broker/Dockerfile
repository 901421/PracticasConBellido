FROM python:3.9-slim

# Entorno para evitar buffering en los logs
ENV PYTHONUNBUFFERED=1

WORKDIR /app

# Copiar el código del broker
COPY . .

# Exponer el puerto del servidor TCP
EXPOSE 5555

# Ejecutar el servidor
CMD ["python", "server.py"]