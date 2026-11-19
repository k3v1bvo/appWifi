package com.smartwifi.switcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geo_zones")
data class GeoZone(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 150f,
    val isEnabled: Boolean = true,
    val wifiOnAtEntry: Boolean = true,
    val dataOffAtEntry: Boolean = true,
    val wifiOffAtExit: Boolean = true,
    val dataOnAtExit: Boolean = true,
    val lastTriggeredAt: Long = 0L,
    val linkedWifiSsids: String? = null, // Redes Wi-Fi separadas por comas (ej. "MiCasa,MiCasa_5G")
    val batteryMode: String = "HYBRID",   // HYBRID (Zero Batería), GEOFENCE_ONLY, WIFI_ONLY
    val disconnectBufferSeconds: Int = 20, // Buffer anti-pestañeo (15s a 30s)
    val vibrateOnAction: Boolean = true,   // Vibración sutil sin sonido ni invasión
    val wifiPassword: String? = null,     // Contraseña Wi-Fi de este lugar (llavero seguro)
    val category: String = "CUSTOM",      // HOME, WORK, FRIEND, SCHOOL, GYM, CAFE, CUSTOM
    val notes: String? = null             // Notas adicionales o recordatorios (ej. "Preguntar a Juan", "Piso 2")
) {
    // Helper to get list of configured SSIDs
    fun getSsidList(): List<String> {
        return linkedWifiSsids?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    // Helper to check if a connected SSID matches any of the zone's networks
    fun matchesSsid(ssid: String?): Boolean {
        if (ssid.isNullOrBlank()) return false
        val cleanSsid = ssid.trim('\"')
        return getSsidList().any { it.equals(cleanSsid, ignoreCase = true) }
    }
}
