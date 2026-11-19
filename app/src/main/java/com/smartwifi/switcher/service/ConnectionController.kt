package com.smartwifi.switcher.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.smartwifi.switcher.MainActivity
import com.smartwifi.switcher.R
import com.smartwifi.switcher.SmartWifiApp
import com.smartwifi.switcher.data.GeoZone
import java.io.DataOutputStream

object ConnectionController {

    private fun isDeviceRooted(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun executeRootCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setWifiState(context: Context, enable: Boolean): Boolean {
        val command = if (enable) "svc wifi enable" else "svc wifi disable"
        
        // 1. Try Shizuku
        if (ShizukuManager.hasPermission()) {
            val success = ShizukuManager.executeShell(command)
            if (success) return true
        }

        // 2. Try Root
        if (isDeviceRooted()) {
            val success = executeRootCommand(command)
            if (success) return true
        }

        return false
    }

    fun setMobileDataState(context: Context, enable: Boolean): Boolean {
        val command = if (enable) "svc data enable" else "svc data disable"

        // 1. Try Shizuku
        if (ShizukuManager.hasPermission()) {
            val success = ShizukuManager.executeShell(command)
            if (success) return true
        }

        // 2. Try Root
        if (isDeviceRooted()) {
            val success = executeRootCommand(command)
            if (success) return true
        }

        return false
    }

    fun handleZoneEnter(context: Context, zone: GeoZone) {
        var wifiChanged = false
        var dataChanged = false

        if (zone.wifiOnAtEntry) {
            wifiChanged = setWifiState(context, true)
        }
        if (zone.dataOffAtEntry) {
            dataChanged = setMobileDataState(context, false)
        }

        val title = "📍 Llegaste a ${zone.name}"
        val message = when {
            wifiChanged && dataChanged -> "✅ Wi-Fi Encendido y Megas Apagados automáticamente."
            wifiChanged -> "✅ Wi-Fi Encendido."
            dataChanged -> "✅ Megas Apagados para ahorrar."
            else -> "⚠️ Llegaste a ${zone.name}. Toca para abrir ajustes y apagar datos (activa Shizuku para automatizarlo)."
        }

        notifyAction(context, title, message, !wifiChanged && !dataChanged)
    }

    fun handleZoneExit(context: Context, zone: GeoZone) {
        var wifiChanged = false
        var dataChanged = false

        if (zone.wifiOffAtExit) {
            wifiChanged = setWifiState(context, false)
        }
        if (zone.dataOnAtExit) {
            dataChanged = setMobileDataState(context, true)
        }

        val title = "🚶 Saliste de ${zone.name}"
        val message = when {
            wifiChanged && dataChanged -> "📶 Wi-Fi Apagado y Megas Encendidos."
            dataChanged -> "📶 Megas Encendidos para seguir conectado."
            wifiChanged -> "📶 Wi-Fi Apagado."
            else -> "⚠️ Saliste de ${zone.name}. Toca para encender tus megas."
        }

        notifyAction(context, title, message, !wifiChanged && !dataChanged)
    }

    private fun notifyAction(context: Context, title: String, message: String, needsManualAction: Boolean) {
        val intent = if (needsManualAction) {
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        } else {
            Intent(context, MainActivity::class.java)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SmartWifiApp.CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
