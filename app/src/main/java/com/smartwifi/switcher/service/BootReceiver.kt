package com.smartwifi.switcher.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smartwifi.switcher.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d(TAG, "Dispositivo reiniciado. Sincronizando geocercas activas...")
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val activeZones = db.geoZoneDao().getActiveZones()
                    val geofenceHelper = GeofenceManagerHelper(context)
                    geofenceHelper.syncAllActiveZones(activeZones)
                    Log.d(TAG, "${activeZones.size} zonas sincronizadas exitosamente.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al restaurar geofences post-reinicio: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
