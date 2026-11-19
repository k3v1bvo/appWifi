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
    val linkedWifiSsid: String? = null, // Red Wi-Fi vinculada (ej. Wi-Fi de casa)
    val batteryMode: String = "HYBRID"   // HYBRID (Zero Batería), GEOFENCE_ONLY, WIFI_ONLY
)
