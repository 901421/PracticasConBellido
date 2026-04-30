# 🚀 Plan de Acción: Super Auditoría Final (Práctica 4)

Este documento detalla los pasos exactos para elevar la calificación de la Práctica 4 a un **10/10 (Matrícula de Honor)**, resolviendo los riesgos detectados en la auditoría técnica senior.

---

## 1. 🛠️ Corrección Crítica: Documentación LaTeX
**Problema:** El archivo `arquitectura_p4.tex` tiene líneas de código basura al final que impiden su compilación.
**Acción:** 
- Abrir `arquitectura_p4.tex`.
- Ir al final del archivo.
- Eliminar todo el texto sobrante después de la etiqueta `\end{document}`.
- El archivo debe terminar exactamente así:
```latex
    \item \textbf{Sincronía vs Asincronía:} Orquestación híbrida para optimizar la experiencia de usuario.
\end{itemize}

\end{document}
```
- **Importante:** Volver a generar el PDF (`arquitectura_p4.pdf`) tras la limpieza para asegurar que el profesor reciba un documento profesional.

---

## 2. 📝 Nueva Guía de Despliegue (`INSTRUCCIONES.md`)
**Problema:** Falta una hoja de ruta clara para que el profesor levante el sistema sin fricciones.
**Acción:** Crear un archivo `INSTRUCCIONES.md` en la raíz con:
- Pasos para iniciar `minikube`.
- Orden de comandos `kubectl apply -f k8s/`.
- Instrucción para configurar el `/etc/hosts` con el dominio `mis-aplicaciones.com`.
- Credenciales de prueba (Header `X-API-Key: mi_clave_secreta`).

---

## 3. 🔍 Verificación Técnica de Último Minuto
**Acción:** Realizar un "Sanity Check" rápido:
1. **API Key:** Confirmar que `k8s/storage.yaml` (Secret) tiene el token `"mi_clave_secreta"` y que el Gateway lo valida correctamente.
2. **Persistencia:** Verificar que el Broker (P3) tiene su `broker-pvc` y que los mensajes sobreviven a un reinicio del pod del broker.
3. **Concurrencia:** Asegurarse de que el `inventory-service` sigue usando el bloqueo `FOR UPDATE` en PostgreSQL para evitar errores de stock ante ráfagas de pedidos.

---

## 4. 📦 Estructura de Entrega Profesional
**Acción:** Al crear el archivo `.zip` para la entrega en Moodle, seguir estrictamente esta jerarquía:
```text
Entrega_P4_Apellidos/
├── broker/                     # Código de la P3 corregido (Obligatorio)
├── api-gateway/                # Código fuente + Dockerfile
├── inventory-service/          # Código fuente + Dockerfile
├── orders-service/             # Código fuente + Dockerfile
├── notifications-service/      # Código fuente + Dockerfile
├── k8s/                        # Todos los manifiestos YAML
├── arquitectura_p4.pdf         # Memoria técnica (YA CORREGIDA)
├── arquitectura_p4.tex         # Fuente LaTeX (YA LIMPIO)
└── INSTRUCCIONES.md            # El manual creado en el Paso 2
```

---

## 💎 Puntos Fuertes a Defender (Venta del proyecto)
Si el profesor pregunta o si hay una defensa oral, destaca estos tres pilares de tu implementación:
1. **Resiliencia Transaccional:** Uso de bloqueos de fila en PostgreSQL para garantizar consistencia de stock en entornos con réplicas.
2. **Seguridad SecDevOps:** Gestión de claves mediante `Secrets` nativos de Kubernetes, evitando cualquier dato sensible en el código fuente.
3. **Robustez del Middleware:** Integración de un broker custom (P3) que soporta persistencia atómica en disco y recuperación de mensajes ante caídas de consumidores.

---
**Veredicto del Auditor:** Una vez ejecutados estos pasos, el proyecto es técnicamente impecable.
