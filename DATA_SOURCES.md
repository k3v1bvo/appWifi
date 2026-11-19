# Fuentes de Datos / Data Sources

Documento descriptivo de todas las fuentes de datos y sensores consumidos por la aplicación:

---

### 1. Sensores de Ubicación del Dispositivo
* **Sensor:** GPS, Wi-Fi Ranging y Torres Celulares mediante `com.google.android.gms.location.FusedLocationProviderClient`.
* **Propósito:** Detección de entrada y salida del perímetro geográfico configurado por el usuario.
* **Procesamiento:** 100% en el dispositivo (Edge Computing). Ninguna coordenada es enviada a servidores externos.

### 2. Capa de Red y Conectividad
* **API del Sistema:** `android.net.ConnectivityManager` y `android.net.wifi.WifiManager`.
* **Datos Leídos:** Nombre de la red Wi-Fi activa (SSID), nivel de señal (RSSI), dirección IP local asignada y máscara de subred.
* **Propósito:** Determinar si el dispositivo está conectado a una red de confianza para apagar los datos móviles de forma automática.

### 3. Proveedor de Mosaicos Cartográficos
* **Servicio:** Servidores CDN públicos de CartoDB (`https://*.basemaps.cartocdn.com/rastertiles/`).
* **Propósito:** Mostrar el mapa interactivo oscuro para que el usuario seleccione el radio y la ubicación de sus zonas.
* **Privacidad de Consulta:** Las peticiones HTTP solicitan únicamente números de teselas `{z}/{x}/{y}.png`. No se envía ningún identificador de usuario ni ubicación exacta asociada a una identidad.
