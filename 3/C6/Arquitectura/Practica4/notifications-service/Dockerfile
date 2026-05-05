FROM python:3.9-slim

# Entorno para asegurar que los logs se emitan sin búfer (Crucial para K8s logs)
ENV PYTHONUNBUFFERED=1

WORKDIR /app

# Aunque esté vacío, mantenemos la estructura por consistencia
COPY notifications-service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia de la lógica del microservicio y el cliente MOM
COPY notifications-service/ .
COPY broker/client.py .

# El servicio de notificaciones es un consumidor asíncrono, no expone puertos HTTP
CMD ["python", "main.py"]
