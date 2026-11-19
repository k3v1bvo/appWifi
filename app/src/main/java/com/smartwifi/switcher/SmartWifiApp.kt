package com.smartwifi.switcher

import android.app.Application
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import org.osmdroid.config.Configuration
import java.io.File

class SmartWifiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize OSMDroid cache configuration with valid user agent (no API key required, zero watermarks)
        val osmConfig = Configuration.getInstance()
        osmConfig.load(this, getSharedPreferences("osmdroid_config", Context.MODE_PRIVATE))
        osmConfig.userAgentValue = "SmartWifiSwitch/1.0.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL})"
        val baseDir = File(cacheDir, "osmdroid").apply { mkdirs() }
        val tileDir = File(baseDir, "tiles").apply { mkdirs() }
        osmConfig.osmdroidBasePath = baseDir
        osmConfig.osmdroidTileCache = tileDir

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
