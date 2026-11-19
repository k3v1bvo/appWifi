# 📱 Smart Wifi Switch (Para Xiaomi Redmi Note 11 y Android 11+)

Aplicación nativa para Android que **apaga automáticamente los datos móviles (megas) y enciende el Wi-Fi** cuando entras al radio de tu casa (o cualquier zona configurada), y **enciende los datos y apaga el Wi-Fi** cuando sales.

---

## 🚀 1. Cómo compilar el APK gratis con GitHub Actions (Sin usar recursos de tu PC)

Tu proyecto ya tiene configurado el flujo automatizado en `.github/workflows/build-apk.yml`. Para generar tu instalador `.apk`:

1. **Sube este proyecto a tu repositorio de GitHub:**
   ```bash
   git init
   git add .
   git commit -m "feat: Smart Wifi Switch app lista"
   git branch -M main
   git remote add origin https://github.com/TU_USUARIO/TU_REPOSITORIO.git
   git push -u origin main
   ```
2. **Descarga tu APK compilado:**
   * Entra a tu repositorio en GitHub desde tu navegador.
   * Haz clic en la pestaña **Actions** arriba.
   * Verás el flujo **"Build Android APK"** ejecutándose.
   * En 2 a 3 minutos terminará en verde. Haz clic sobre él.
   * Al final de la página, en la sección **Artifacts**, descarga el archivo **`SmartWifiDataSaver-Debug-APK`**.
   * Pásalo a tu celular o descárgalo directamente desde el navegador de tu Xiaomi e instálalo.

---

## ⚙️ 2. Configuración obligatoria en tu Xiaomi Redmi Note 11 (MIUI / HyperOS)

Los teléfonos Xiaomi suelen cerrar las aplicaciones en segundo plano para ahorrar batería. Para que la app nunca se cierre y funcione las 24 horas:

### A. Permitir ubicación en segundo plano
1. Abre la app por primera vez y acepta los permisos de ubicación.
2. Cuando pregunte, selecciona **"Permitir todo el tiempo"** (necesario para detectar tu casa con la pantalla apagada).

### B. Inicio Automático en MIUI
1. Ve a **Ajustes** -> **Aplicaciones** -> **Permisos** -> **Inicio automático** (o usa el botón directo dentro de la app).
2. Busca **Smart Wifi Switch** y **actívalo**.

### C. Ahorro de Batería sin restricciones
1. Mantén presionado el icono de la app en la pantalla de inicio y toca **Información de la aplicación** (el icono de la "i").
2. Baja hasta **Ahorro de batería**.
3. Cámbialo de *Ahorro de batería MIUI (recomendado)* a **"Sin restricciones"**.

---

## ⚡ 3. Cómo activar el control automático de Datos Móviles (Vía Shizuku)

En Android 11 en adelante, Google no permite a ninguna app tocar los datos móviles por seguridad a menos que tenga permisos del sistema. Lo solucionamos con **Shizuku** (100% seguro, sin Root y sin cables):

1. Descarga **Shizuku** gratis desde la Google Play Store.
2. Conéctate a tu Wi-Fi.
3. En tu Xiaomi ve a **Ajustes** -> **Ajustes adicionales** -> **Opciones de desarrollador**:
   * Activa **Depuración USB**.
   * Activa **Depuración inalámbrica**.
   * Activa **Depuración USB (Ajustes de seguridad)** *(crucial en Xiaomi para permitir comandos de red)*.
4. Abre la app **Shizuku** y toca **Emparejamiento / Iniciar**.
5. Abre **Smart Wifi Switch** y toca el botón **"Activar"**: acepta el permiso emergente de Shizuku.
6. ¡Listo! A partir de ese momento, la app tiene permiso para apagar y prender tus megas y tu Wi-Fi sola.

---

## 🗺️ 4. Uso de la Aplicación

1. En la pantalla principal, toca el botón **"Nueva Zona"**.
2. Verás el mapa de OpenStreetMap (no requiere claves de API):
   * Toca el icono de ubicación para centrarte en tu posición actual.
   * Ajusta el control deslizante del **Radio** (ej. 100 o 150 metros). Verás el círculo azul cambiar de tamaño en vivo.
   * Ponle nombre (ej. *"Mi Casa"*).
3. Toca **"Guardar y Activar Zona"**.
4. ¡Ya está protegida tu conexión! Cada vez que cruces la frontera de ese radio, recibirás la notificación y la conexión cambiará automáticamente.
