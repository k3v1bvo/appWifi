# Política de Privacidad / Privacy Policy

**Última actualización:** 19 de Noviembre de 2026  
**Responsable:** k3v1bvo (k3v1bvo@gmail.com)

Esta Política de Privacidad describe cómo **Smart Wifi Switch** ("la Aplicación") recopila, utiliza y protege tu información al utilizar nuestro servicio.

---

### 1. Principio Fundamental: Privacidad 100% Local (Zero-Server Architecture)
Tu privacidad es nuestra máxima prioridad. **Smart Wifi Switch no recopila, no transmite, no vende y no almacena ningún dato personal en servidores externos.** Toda la información generada o configurada dentro de la aplicación reside exclusivamente en la memoria interna y en el almacenamiento aislado (sandbox) de tu propio dispositivo.

---

### 2. Información que Tratamos y Para Qué la Usamos

#### A. Ubicación en Primer Plano y Segundo Plano (`ACCESS_FINE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`)
* **Qué recopilamos:** Coordenadas de latitud y longitud satelital de los perímetros que decides guardar (ej. tu casa, trabajo o casa de amigos).
* **Finalidad única:** La aplicación requiere el permiso de ubicación en segundo plano ("Permitir todo el tiempo") **estrictamente para alternar automáticamente la conexión de datos móviles y Wi-Fi cuando cruzas la frontera de tus zonas guardadas con la pantalla apagada**.
* **Protección:** Tus coordenadas no se rastrean de forma continua en trayectos. El sistema operativo Android (mediante la API nativa de Geofencing) notifica a la aplicación únicamente cuando cruzas el perímetro configurado.

#### B. Redes Wi-Fi y Llavero de Contraseñas
* **Qué recopilamos:** El nombre de la red Wi-Fi (SSID) a la que te conectas y, opcionalmente, la contraseña o notas que decidas guardar en tu llavero.
* **Finalidad:** Identificar tus redes de confianza para apagar los megas y permitirte visualizar o compartir la clave mediante código QR con amigos o invitados.
* **Almacenamiento:** Las contraseñas quedan almacenadas en la base de datos interna SQLite de la app y solo tú tienes acceso a ellas desde la interfaz de tu teléfono.

#### C. Diagnóstico y Registro de Actividad
* La aplicación almacena un registro local de cuándo se encendieron o apagaron los datos móviles para que puedas verificar el ahorro de megas. Puedes vaciar este registro en cualquier momento con el botón *"Limpiar Registro"*.

---

### 3. Cookies y Rastreadores de Terceros
La Aplicación **NO utiliza cookies, píxeles de seguimiento, herramientas de telemetría publicitaria ni redes de analítica de terceros** (como Google Analytics, Meta Pixel o Firebase).

---

### 4. Derechos del Usuario (Acceso, Rectificación y Supresión)
Dado que toda la información se almacena localmente en tu teléfono:
* Puedes **acceder** y **rectificar** cualquier zona, red o contraseña directamente desde la pantalla principal o el mapa.
* Puedes **suprimir** de manera inmediata todos los datos eliminando las zonas individualmente, borrando el registro de actividad o desinstalando la aplicación desde los ajustes de Android.

---

### 5. Contacto
Si tienes dudas o consultas sobre esta política de privacidad, puedes contactar al desarrollador en:  
📧 **k3v1bvo@gmail.com**
