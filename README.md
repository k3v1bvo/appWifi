# ⚡ Smart Wifi Switch (Para Xiaomi Redmi Note 11 y Android 11+)
> **Control autónomo de datos móviles, geocercas inteligentes, llavero Wi-Fi con códigos QR y mapa radar OLED sin consumo de batería ni notificaciones molestas.**

[![Build Android APK](https://github.com/k3v1bvo/appWifi/actions/workflows/build-apk.yml/badge.svg)](https://github.com/k3v1bvo/appWifi/actions/workflows/build-apk.yml)
![Platform](https://img.shields.io/badge/Plataforma-Android%2011%2B%20%7C%20MIUI%20%7C%20HyperOS-blue)
![Architecture](https://img.shields.io/badge/Arquitectura-Jetpack%20Compose%20%7C%20Room%20%7C%20Shizuku-emerald)
![License](https://img.shields.io/badge/Licencia-Privada%20%7C%20Derechos%20Reservados-red)

---

## 🎯 ¿Qué problema resuelve?

En Android, dejar los **datos móviles encendidos** mientras estás en casa, en el trabajo o en casa de amigos provoca que el teléfono consuma megas de tu plan por microcortes de Wi-Fi, descargas en segundo plano o retrasos de reconexión. 

**Smart Wifi Switch** automatiza este proceso al 100%:
* **Al entrar a una zona o conectarte a su Wi-Fi:** Apaga los datos móviles al instante (`0 MB gastados`) y mantiene el Wi-Fi activo.
* **Buffer anti-pestañeo (20s):** Si el router se reinicia o caminas a una esquina con baja señal, la app espera 20 segundos antes de tocar los megas. Si la señal vuelve, cancela el cambio y evita el consumo accidental.
* **Al salir de la zona:** Enciende los datos móviles automáticamente para que no te quedes sin internet en la calle.
* **Cero invasión:** Cero notificaciones con sonido en la barra de estado (`IMPORTANCE_MIN`). En su lugar, usa respuesta háptica táctil sutil (vibración imperceptible).

---

## 🚀 Características Principales

### 1. Estética Minimalista "Dark Matter OLED" (Inspirada en Wolow)
* **Fondo Negro Puro OLED (`#000000`):** Máximo contraste y mínimo consumo de pantalla en paneles AMOLED (como el Redmi Note 11).
* **Botón Maestro Central ("Power Ring"):** Anillo concéntrico con resplandor neón cian/esmeralda para alternar la protección global en 1 toque.
* **Telemetría en Tiempo Real ("Network Information"):**
  * Estado de megas (`APAGADO (0 MB)` / `ENCENDIDO`).
  * Red Wi-Fi conectada y nombre SSID.
  * Dirección IP local (`192.168.1.xxx`) y máscara de subred.
  * Badge dinámico: `✓ Megas Protegidos` o `⚡ Datos Móviles`.

### 2. Llavero Wi-Fi y Códigos QR Compartibles en 1-Tap
* **Contraseñas Seguras:** Guarda la clave Wi-Fi de cada lugar en tu dispositivo con botón de ojo (👁️) para verla y botón para copiarla (📋).
* **Generador de Código QR Oficial:** Cualquier invitado o amigo apunta la cámara de su celular al código QR en pantalla y se conecta de inmediato sin escribir contraseñas.
* **Escáner de Redes Cercanas (🔍):** Detecta las redes Wi-Fi al alcance con su barra de señal (📶) para vincularlas en un toque sin tener que escribir el nombre a mano.

### 3. Radar Geográfico y Mapa de Alta Precisión (Sin Marcas de Agua)
* **Cartografía Oficial CARTO Basemaps Integrada:** Mapa técnico oscuro de alta resolución con API Key de k3v1bvoLabs autenticada (`CARTO_DARK` y `CARTO_VOYAGER`), 100% libre de marcas de agua diagonales y optimizado para pantallas OLED.
* **Radar Multizona en Vivo:** Visualiza todas tus zonas ("Casa", "Trabajo", "Casa Juan", etc.) al mismo tiempo en el mapa con halos de neón.
* **Detección Dinámica:** El perímetro en el que te encuentras pulsa en verde esmeralda con la etiqueta `📍 AQUÍ AHORA`.
* **Editor Interactivo:** Ajusta el radio de detección de 50 a 500 metros con deslizador en vivo, chips de acceso rápido (50m, 100m, 180m, 350m) y controles flotantes (`🎯` Centrar GPS, zoom y selector de estilos).

### 4. Categorías de Ubicación Ilimitadas
* 🏠 **Casa / Hogar**
* 💼 **Trabajo / Oficina**
* 👤 **Casa de Amigos / Familiares (Juan, etc.)**
* 🎓 **Universidad / Estudio**
* 🏋️ **Gimnasio / Club**
* ☕ **Cafetería / Restaurante**
* 📍 **Lugares Personalizados**

---

## 📦 Cómo compilar y descargar el APK en GitHub Actions

El proyecto está configurado con integración continua (CI) en GitHub Actions, lo que permite compilar el instalador `.apk` en la nube **sin consumir memoria RAM ni procesador de tu PC**:

1. **Subir los cambios a tu repositorio:**
   ```bash
   git add .
   git commit -m "feat: actualizacion completa de app"
   git push origin main
   ```
2. **Descargar el APK:**
   * Entra a la pestaña **Actions** en tu repositorio de GitHub: `https://github.com/k3v1bvo/appWifi/actions`.
   * Abre la última ejecución en verde de **Build Android APK**.
   * En la sección **Artifacts** al pie de la página, descarga el paquete **`SmartWifiDataSaver-Debug-APK`**.
   * **Instalación limpia (primera vez v1.1.0):** Al cambiar la firma a un certificado criptográfico persistente (`app/debug.keystore`) y el paquete a `com.k3v1bvo.smartwifi`, si tenías instalada una compilación de prueba anterior, desinstálala una sola vez para eliminar el conflicto de firmas de Android. A partir de esta versión, todas las futuras actualizaciones se instalarán encima sin errores.

---

## ⚙️ Configuración Obligatoria para Xiaomi (MIUI / HyperOS)

Los teléfonos Xiaomi son agresivos cerrando procesos en segundo plano. Sigue estos 3 pasos (o pulsa el botón **"Ajustes Xiaomi"** dentro de la app para abrir los accesos directos):

### A. Permitir ubicación todo el tiempo
1. Abre la app y concede permisos de ubicación.
2. Selecciona **"Permitir todo el tiempo"** para que el sensor de geocerca funcione con la pantalla apagada.

### B. Inicio Automático
1. Ve a **Ajustes** -> **Aplicaciones** -> **Permisos** -> **Inicio automático**.
2. Activa el interruptor para **Smart Wifi Switch**.

### C. Ahorro de batería sin restricciones
1. Mantén presionado el icono de la aplicación y toca **Información de la aplicación** (icono `i`).
2. Entra a **Ahorro de batería**.
3. Selecciona **"Sin restricciones"** (evita que el optimizador de MIUI congele el servicio en reposo).

---

## ⚡ Activación de Privilegios del Sistema con Shizuku (Sin Root)

En Android 11 y versiones superiores, el sistema restringe el control de datos móviles a apps comunes. Esta app utiliza la API de **Shizuku** (depuración inalámbrica ADB) para ejecutar `svc data` y `svc wifi` sin requerir Root ni cables:

1. Instala **Shizuku** gratis desde Google Play Store.
2. Conecta tu celular a tu red Wi-Fi.
3. Ve a **Ajustes** -> **Ajustes adicionales** -> **Opciones de desarrollador**:
   * Activa **Depuración USB**.
   * Activa **Depuración inalámbrica**.
   * Activa **Depuración USB (Ajustes de seguridad)** *(imprescindible en Xiaomi para permitir comandos de red)*.
4. Abre la app **Shizuku** y toca **Emparejamiento / Iniciar**.
5. Abre **Smart Wifi Switch** y pulsa el banner **"Vincular Shizuku"**; acepta el diálogo emergente.
6. ¡Listo! El control de megas y Wi-Fi queda 100% activo y autónomo.

---

## 🏗️ Estructura del Código

```
app/src/main/java/com/smartwifi/switcher/
├── MainActivity.kt               # Orquestación de pantallas, permisos y ciclo de vida Shizuku
├── SmartWifiApp.kt               # Configuración de caché OSMDroid, canales de notificación silenciosos
├── data/
│   ├── GeoZone.kt                # Entidad Room con soporte multi-SSID, llavero de claves y categorías
│   ├── GeoZoneDao.kt             # Consultas reactivas (Flow) y operaciones CRUD
│   ├── ActivityLog.kt            # Entidad de historial de ahorro de megas
│   └── AppDatabase.kt            # Base de datos Room v4 con migración automática
├── service/
│   ├── ConnectionController.kt   # Ejecución de comandos svc data y svc wifi por ADB/Root
│   ├── GeofenceManagerHelper.kt  # Integración con Google Play Services Geofencing
│   ├── SmartKeepAliveService.kt  # Foreground service silencioso con NetworkCallback y buffer de 20s
│   ├── WifiHelper.kt             # Telemetría de red: SSID, IP local, máscara de subred y estado de datos
│   ├── WifiKeychainHelper.kt     # Generación de códigos QR con ZXing y escaneo de redes RF
│   ├── ShizukuManager.kt         # Conexión IPC con Shizuku por reflexión
│   └── VibratorHelper.kt         # Respuesta háptica suave para llegada y salida
└── ui/
    ├── screens/
    │   ├── HomeScreen.kt         # Dashboard principal estilo Wolow con Power Ring y telemetría
    │   ├── RadarMapScreen.kt     # Mapa radar OLED a pantalla completa con geocercas interactivas
    │   ├── QuickAddZoneDialog.kt # Diálogo rápido para añadir lugares, claves y escanear redes
    │   ├── WifiQrDialog.kt       # Modal de visualización de clave Wi-Fi y código QR oficial
    │   ├── TestBenchDialog.kt    # Panel de diagnóstico manual en 1 tap
    │   ├── ActivityLogDialog.kt  # Historial auditado de ahorro de megas
    │   ├── XiaomiGuideDialog.kt  # Guía de configuración con intents directos a MIUI
    │   └── MapTileSources.kt     # Servidores de mapas CartoDB Dark Matter y Voyager
    └── theme/
        ├── Color.kt              # Paleta OLED Black (#000000), neón cian y esmeralda
        ├── Theme.kt              # Configuración de MaterialTheme en modo oscuro puro
        └── Type.kt               # Tipografía técnica del sistema
```

---

## 📄 Licencia y Documentación Legal

**Copyright (c) 2026 k3v1bvo. Todos los derechos reservados.**

Este proyecto es software privado y propietario. Queda estrictamente prohibida la copia, reproducción, redistribución, modificación, venta, sublicenciamiento o uso no autorizado de este código fuente y sus binarios sin la autorización expresa por escrito de su autor (**k3v1bvo**).

Para consultar los documentos de cumplimiento legal, privacidad y atribuciones:
* 📜 [Licencia Propietaria (LICENSE)](LICENSE)
* 🔒 [Política de Privacidad (PRIVACY.md)](PRIVACY.md)
* ⚖️ [Términos de Uso y EULA (TERMS.md)](TERMS.md)
* 🛡️ [Política de Seguridad y Vulnerabilidades (SECURITY.md)](SECURITY.md)
* 📋 [Inventario de Datos y ROPA (DATA_INVENTORY.md)](DATA_INVENTORY.md)
* 🌐 [Fuentes de Datos y APIs (DATA_SOURCES.md)](DATA_SOURCES.md)
* 🤝 [Avisos de Terceros y Licencias Apache 2.0 (THIRD_PARTY_NOTICES.md)](THIRD_PARTY_NOTICES.md)
* 🎨 [Atribuciones de Diseño y Cartografía (ATTRIBUTIONS.md)](ATTRIBUTIONS.md)
