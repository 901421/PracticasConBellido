# Instrucciones de Despliegue - Práctica 4 (Arquitectura Software)

Este documento detalla el procedimiento para desplegar la arquitectura de microservicios en un clúster de Kubernetes local (Minikube).

## 1. Prerrequisitos
- **Minikube** instalado y en ejecución.
- **kubectl** configurado.
- Addon de Ingress activo: 
  ```bash
  minikube addons enable ingress
  ```

## 2. Preparación de Imágenes
Dado que se utilizan imágenes locales (`imagePullPolicy: Never`), es necesario cargarlas en el nodo de Minikube antes de desplegar:

```bash
minikube image load api-gateway:latest 
minikube image load broker-mom:latest 
minikube image load inventory-service:latest 
minikube image load notifications-service:latest 
minikube image load orders-service:latest
```

## 3. Despliegue de la Infraestructura
Desde la raíz del proyecto, aplique todos los manifiestos YAML:

```bash
kubectl apply -f k8s/
```

Esto desplegará:
- **Bases de Datos:** MariaDB (Pedidos) y PostgreSQL (Inventario).
- **Middleware:** Broker MOM (P3).
- **Microservicios:** API Gateway, Pedidos, Inventario y Notificaciones.
- **Configuraciones:** Secrets, ConfigMaps y PersistentVolumeClaims.

## 4. Configuración del Dominio
1. Obtenga la IP asignada al Ingress:
   ```bash
   kubectl get ingress
   ```
2. Añada la siguiente línea a su archivo de hosts (`/etc/hosts` en Linux/Mac o `C:\Windows\System32\drivers\etc\hosts` en Windows):
   ```text
   [IP_OBTENIDA] mis-aplicaciones.com
   ```

## 5. Pruebas de Funcionamiento
Para interactuar con el sistema, realice peticiones al API Gateway incluyendo la cabecera de seguridad:

- **Endpoint de Pedidos:** `POST http://mis-aplicaciones.com/orders`
- **Cabecera Obligatoria:** `X-API-Key: mi_clave_secreta`
- **Cuerpo (JSON):**
  ```json
  {
    "product_id": 1,
    "quantity": 2,
    "customer_name": "Auditor Senior"
  }
  ```

---
*Nota: El sistema utiliza persistencia física mediante PVCs. Los datos de stock y pedidos sobrevivirán a los reinicios de los pods.*
