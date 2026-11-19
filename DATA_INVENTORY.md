# Inventario de Datos Personales (ROPA)

Registro de Actividades de Tratamiento conforme a los estándares de GDPR (Reglamento General de Protección de Datos), LGPD y leyes de privacidad locales.

---

## 1. Identificación del Responsable

* **Titular / Desarrollador:** k3v1bvo
* **Contacto:** k3v1bvo@gmail.com
* **Naturaleza de la App:** Aplicación móvil de utilidad de sistema (ejecución local).

---

## 2. Inventario de Datos Tratados

| Categoría de Dato | Datos Específicos | Finalidad Principal | Base Legal | ¿Almacenamiento Remoto? | Retención |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Ubicación GPS en Primer Plano** | Latitud, Longitud, Radio en metros | Permitir al usuario definir el centro de su casa/oficina en el mapa. | Consentimiento explícito del usuario. | **NO (0%)** — Solo en base de datos local SQLite (Room). | Hasta que el usuario edite o elimine la zona. |
| **Ubicación en Segundo Plano** | Eventos de Geocerca (Entrada / Salida) | Detectar cuándo el teléfono entra o sale de la zona para alternar los datos móviles sin abrir la app. | Consentimiento explícito ("Permitir todo el tiempo"). | **NO (0%)** — Solo en el hardware local. | Transitorio (se procesa y descarta de inmediato). |
| **Datos de Red Wi-Fi** | SSID (nombre de red), BSSID, RSSI | Comprobar si el Wi-Fi conectado coincide con la zona guardada. | Ejecución de la funcionalidad solicitada. | **NO (0%)** — Solo en base de datos local SQLite. | Hasta que el usuario elimine la red. |
| **Llavero de Contraseñas** | Clave Wi-Fi introducida por el usuario | Permitir ver la clave y generar el código QR de conexión para invitados. | Consentimiento explícito del usuario. | **NO (0%)** — Encriptada en el almacenamiento privado de la app. | Hasta que el usuario borre la clave. |
| **Historial de Actividad** | Timestamp, título de acción, tipo de evento | Auditoría local para que el usuario verifique cuándo se ahorraron megas. | Interés legítimo (transparencia para el usuario). | **NO (0%)** — Base de datos local `activity_logs`. | Editable / Borrable con el botón "Limpiar historial". |

---

## 3. Destinatarios y Transferencias Internacionales

* **Destinatarios Externos:** Ninguno.
* **Terceros / Analíticas:** Cero (0). No se utiliza Google Analytics, Firebase Analytics, Crashlytics, trackers de publicidad ni SDKs de monetización.
* **Transferencias Internacionales de Datos:** No aplican. Los datos jamás salen del almacenamiento físico del dispositivo móvil.
