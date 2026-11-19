package com.smartwifi.switcher.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.smartwifi.switcher.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = "Error en Geofencing: código ${geofencingEvent.errorCode}"
            Log.e(TAG, errorMessage)
            return
        }

        val transitionType = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                for (geofence in triggeringGeofences) {
                    val zoneId = geofence.requestId.toLongOrNull() ?: continue
                    val zone = db.geoZoneDao().getZoneById(zoneId) ?: continue

                    if (!zone.isEnabled) continue

                    db.geoZoneDao().updateLastTriggered(zoneId, System.currentTimeMillis())

                    when (transitionType) {
                        Geofence.GEOFENCE_TRANSITION_ENTER -> {
                            Log.d(TAG, "GEOFENCE_TRANSITION_ENTER: ${zone.name}")
                            ConnectionController.handleZoneEnter(context, zone)
                        }
                        Geofence.GEOFENCE_TRANSITION_EXIT -> {
                            Log.d(TAG, "GEOFENCE_TRANSITION_EXIT: ${zone.name}")
                            ConnectionController.handleZoneExit(context, zone)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error procesando evento geofence: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}
