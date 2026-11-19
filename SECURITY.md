# Política de Seguridad / Security Policy

En **Smart Wifi Switch** nos tomamos en serio la seguridad de tu dispositivo, red y credenciales.

---

## 1. Versiones Compatibles y Soporte

| Versión | ¿Recibe Parches de Seguridad? |
| :--- | :--- |
| `1.0.x` (Rama `main`) | ✅ Activo |
| Versiones anteriores | ❌ No soportado |

---

## 2. Medidas de Seguridad Implementadas

1. **Aislamiento en Sandbox de Android:** Toda la base de datos de contraseñas y perímetros se almacena en el directorio interno protegido `/data/user/0/com.smartwifi.switcher/databases/`, inaccesible para otras aplicaciones no autorizadas.
2. **IPC con Shizuku Verificado:** Las llamadas a través de Shizuku no exponen puertos TCP abiertos a la red local. Se canalizan únicamente a través del Binder seguro del sistema operativo Android.
3. **Generación Local de QR:** Los códigos QR de Wi-Fi se renderizan mediante la biblioteca embebida ZXing directamente en la memoria RAM del teléfono, sin consultar APIs web de generación de imágenes externas.

---

## 3. Notificación de Vulnerabilidades

Si descubres una posible vulnerabilidad de seguridad en esta aplicación:
1. **NO** abras un Issue público en GitHub.
2. Envía un correo con los detalles técnicos y pasos para reproducir el fallo a:  
   📧 **k3v1bvo@gmail.com**
3. Responderemos dentro de un plazo de 48 horas para coordinar la verificación y publicación del parche correctivo.
