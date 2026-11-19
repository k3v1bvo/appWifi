package com.smartwifi.switcher.service

import android.content.Context
import android.util.Log
import com.smartwifi.switcher.data.ActivityLog
import com.smartwifi.switcher.data.AppDatabase
import com.smartwifi.switcher.data.GeoZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataOutputStream

object ConnectionController {

    private const val TAG = "ConnectionController"

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
        
        if (ShizukuManager.hasPermission()) {
            val success = ShizukuManager.executeShell(command)
            if (success) return true
        }

        if (isDeviceRooted()) {
            val success = executeRootCommand(command)
            if (success) return true
        }

        return false
    }

    fun setMobileDataState(context: Context, enable: Boolean): Boolean {
        val command = if (enable) "svc data enable" else "svc data disable"

        if (ShizukuManager.hasPermission()) {
            val success = ShizukuManager.executeShell(command)
            if (success) return true
        }

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

        if (zone.vibrateOnAction) {
            VibratorHelper.vibrateArrival(context)
        }

        // Save event to internal Activity History (No annoying notification popups)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.activityLogDao().insertLog(
                    ActivityLog(
                        title = "📍 Llegada a ${zone.name}",
                        description = if (dataChanged) "Wi-Fi Encendido • Megas Apagados automáticamente" else "Wi-Fi Encendido",
                        type = "ARRIVED"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error logging event: ${e.message}")
            }
        }
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

        if (zone.vibrateOnAction) {
            VibratorHelper.vibrateDeparture(context)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.activityLogDao().insertLog(
                    ActivityLog(
                        title = "🚶 Salida de ${zone.name}",
                        description = if (dataChanged) "Wi-Fi Apagado • Megas Encendidos para seguir con internet" else "Wi-Fi Apagado",
                        type = "DEPARTED"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error logging exit: ${e.message}")
            }
        }
    }
}
