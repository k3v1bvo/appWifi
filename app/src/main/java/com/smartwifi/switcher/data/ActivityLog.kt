package com.smartwifi.switcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val type: String // "ARRIVED", "DEPARTED", "WIFI_RESTORED", "TEST"
)
