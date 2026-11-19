# 📱 Guía Especializada de Configuración para Xiaomi Redmi Note 11 (MIUI 13/14 y HyperOS)

Los teléfonos Xiaomi incorporan capas agresivas de optimización de batería y limpieza de memoria RAM (Security Center / Optimizador MIUI) que por defecto suspenden o cierran las aplicaciones en segundo plano cuando la pantalla se apaga.

Sigue esta guía paso a paso para garantizar que **Smart Wifi Switch** funcione de forma 100% autónoma las 24 horas del día.

---

## 1. Permitir Ubicación en Segundo Plano ("Todo el tiempo")

Android requiere autorización expresa para que el sensor de geocercas satelital funcione con la pantalla apagada:

1. Ve a **Ajustes** -> **Aplicaciones** -> **Administrar aplicaciones**.
2. Busca y selecciona **Smart Wifi Switch**.
3. Toca en **Permisos de la aplicación** -> **Ubicación**.
4. Selecciona la opción: **"Permitir todo el tiempo"** (no elijas "Solo mientras la app está en uso").

---

## 2. Activar "Inicio Automático" en MIUI / HyperOS

Esto permite que la aplicación se reinicie sola si reinicias el teléfono o si el sistema libera memoria:

1. Ve a **Ajustes** -> **Aplicaciones** -> **Permisos** -> **Inicio automático** (o pulsa el botón directo *"Ajustes Xiaomi"* dentro de la app).
2. Busca **Smart Wifi Switch** en la lista y **activa el interruptor**.
3. *(Opcional en MIUI 14 / HyperOS)*: Toca sobre la app en esa lista y asegúrate de que esté habilitado **"Permitir que otras aplicaciones inicien esta aplicación"**.

---

## 3. Desactivar el Ahorro de Batería Restrictivo de MIUI

Por defecto, MIUI aplica el perfil *"Ahorro de batería MIUI (recomendado)"*, el cual congela los hilos de red y servicios de fondo tras 10 minutos de inactividad:

1. Mantén presionado el icono de **Smart Wifi Switch** en la pantalla de inicio y toca el icono **( i )** (*Información de la aplicación*).
2. Desplázate hacia abajo hasta encontrar **Ahorro de batería**.
3. Cámbialo a: **"Sin restricciones"** (el icono verde).
   > *Nota: Gracias a nuestro modelo híbrido, cuando estás conectado al Wi-Fi de tu casa el GPS pasa a reposo absoluto (0% de consumo), por lo que esta opción no drenará tu batería.*

---

## 4. Bloquear la Aplicación en la Vista de Recientes (Candado de Memoria)

Para evitar que el limpiador de RAM cierre la app cuando presionas la "X" para limpiar aplicaciones:

1. Abre **Smart Wifi Switch**.
2. Ve a la vista de aplicaciones recientes (desliza hacia arriba desde abajo y mantén presionado).
3. Mantén presionada la ventana previa de **Smart Wifi Switch** hasta que aparezca el menú flotante.
4. Toca el icono del **Candado** (🔒) para bloquearla. Verás un candado pequeño en la esquina superior de la ventana.

---

## 5. Habilitar Shizuku con Depuración Inalámbrica (Control de Datos Móviles)

Para que la app pueda apagar y encender tus datos móviles sin cables y sin root:

1. Instala **Shizuku** desde Google Play Store.
2. Conecta tu teléfono a tu Wi-Fi.
3. Activa las **Opciones de desarrollador**:
   * Ve a **Ajustes** -> **Acerca del teléfono**.
   * Pulsa 7 veces seguidas sobre **Versión de MIUI** (o versión de SO) hasta que diga *"¡Ya eres desarrollador!"*.
4. Ve a **Ajustes** -> **Ajustes adicionales** -> **Opciones de desarrollador**:
   * Activa **Depuración USB**.
   * Activa **Depuración inalámbrica** (acepta la ventana emergente).
   * **MUY IMPORTANTE EN XIAOMI:** Baja un poco más y activa **"Depuración USB (Ajustes de seguridad)"**. *(MIUI te pedirá insertar una cuenta Mi o pasar 3 avisos de 5 segundos; esto es obligatorio para permitir que Shizuku ejecute los comandos de red `svc data`)*.
5. Abre la app **Shizuku** y toca **Emparejar con depuración inalámbrica**.
6. En la notificación que aparece, introduce el código de 6 dígitos que te da Xiaomi.
7. Toca **Iniciar** en Shizuku. Verás: *"Shizuku está en ejecución"*.
8. Abre **Smart Wifi Switch** y toca **"Vincular Shizuku"**; acepta la solicitud de permiso que saldrá en pantalla.

¡Listo! A partir de ese momento, la app operará en segundo plano de forma silenciosa, apagando tus megas cada vez que llegues a casa o a la de un amigo y encendiéndolos cuando salgas.
