package com.smartwifi.switcher.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.smartwifi.switcher.data.GeoZone

class GeofenceManagerHelper(private val context: Context) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    @SuppressLint("MissingPermission")
    fun addOrUpdateGeofence(zone: GeoZone, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        if (!zone.isEnabled || zone.batteryMode == "WIFI_ONLY") {
            removeGeofence(zone.id)
            onResult(true, "Zona desactivada o en modo Solo Wi-Fi (Sin consumo de GPS)")
            return
        }

        // Ultra-efficient battery tuning:
        // Responsiveness of 30s allows Android to batch location checks with cell towers and Wi-Fi scans
        val geofence = Geofence.Builder()
            .setRequestId(zone.id.toString())
            .setCircularRegion(zone.latitude, zone.longitude, zone.radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setNotificationResponsiveness(30000) // 30s = Máximo ahorro de batería
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        try {
            geofencingClient.addGeofences(request, geofencePendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Geofence optimizada registrada: ${zone.name} (${zone.batteryMode})")
                    onResult(true, null)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error al registrar geofence: ${exception.message}")
                    onResult(false, exception.message)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permisos insuficientes para geofence: ${e.message}")
            onResult(false, "Faltan permisos de ubicación todo el tiempo")
        }
    }

    fun removeGeofence(zoneId: Long) {
        geofencingClient.removeGeofences(listOf(zoneId.toString()))
            .addOnSuccessListener {
                Log.d(TAG, "Geofence eliminada: $zoneId")
            }
            .addOnFailureListener {
                Log.e(TAG, "Fallo al remover geofence $zoneId: ${it.message}")
            }
    }

    @SuppressLint("MissingPermission")
    fun syncAllActiveZones(activeZones: List<GeoZone>) {
        geofencingClient.removeGeofences(geofencePendingIntent).addOnCompleteListener {
            // Only add zones that use geofencing (HYBRID or GEOFENCE_ONLY)
            val eligibleZones = activeZones.filter { it.batteryMode != "WIFI_ONLY" }
            if (eligibleZones.isEmpty()) return@addOnCompleteListener

            val geofenceList = eligibleZones.map { zone ->
                Geofence.Builder()
                    .setRequestId(zone.id.toString())
                    .setCircularRegion(zone.latitude, zone.longitude, zone.radiusMeters)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setNotificationResponsiveness(30000)
                    .build()
            }

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build()

            try {
                geofencingClient.addGeofences(request, geofencePendingIntent)
            } catch (e: SecurityException) {
                Log.e(TAG, "Error sync geofences: ${e.message}")
            }
        }
    }

    companion object {
        const val TAG = "GeofenceManagerHelper"
        const val ACTION_GEOFENCE_EVENT = "com.smartwifi.ACTION_GEOFENCE_EVENT"
    }
}
