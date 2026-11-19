package com.smartwifi.switcher

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import org.osmdroid.config.Configuration
import java.io.File

class SmartWifiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize OSMDroid cache configuration
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = packageName
        osmConfig.osmdroidBasePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidTileCache = File(cacheDir, "osmdroid/tiles")

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // IMPORTANCE_MIN: Zero invasion. No status bar icon, completely silent, minimized at the bottom.
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Servicio en Segundo Plano (Silencioso)",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Mantiene el servicio activo en segundo plano sin sonidos ni alertas molestas"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Registro de Eventos (Silencioso)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones no invasivas sin sonido"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
            manager?.createNotificationChannel(alertsChannel)
        }
    }

    companion object {
        const val CHANNEL_SERVICE = "channel_smart_wifi_service"
        const val CHANNEL_ALERTS = "channel_smart_wifi_alerts"
        lateinit var instance: SmartWifiApp
            private set
    }
}
