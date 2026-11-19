package com.smartwifi.switcher

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Servicio de Monitoreo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene activo el monitoreo de zonas para no cerrar la app en Xiaomi"
                setShowBadge(false)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Alertas de Conexión",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando cambias de zona y se alternan datos/Wi-Fi"
                enableVibration(true)
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
