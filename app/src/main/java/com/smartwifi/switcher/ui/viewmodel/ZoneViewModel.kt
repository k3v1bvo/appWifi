package com.smartwifi.switcher.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartwifi.switcher.data.AppDatabase
import com.smartwifi.switcher.data.GeoZone
import com.smartwifi.switcher.service.GeofenceManagerHelper
import com.smartwifi.switcher.service.ShizukuManager
import com.smartwifi.switcher.service.SmartKeepAliveService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ZoneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.geoZoneDao()
    private val geofenceHelper = GeofenceManagerHelper(application)

    val allZones: StateFlow<List<GeoZone>> = dao.getAllZones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isShizukuActive = MutableStateFlow(ShizukuManager.hasPermission())
    val isShizukuActive: StateFlow<Boolean> = _isShizukuActive.asStateFlow()

    private val _isServiceActive = MutableStateFlow(true)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    init {
        refreshShizukuStatus()
        SmartKeepAliveService.start(application)
    }

    fun refreshShizukuStatus() {
        _isShizukuActive.value = ShizukuManager.hasPermission()
    }

    fun toggleService(active: Boolean) {
        _isServiceActive.value = active
        val context = getApplication<Application>()
        if (active) {
            SmartKeepAliveService.start(context)
            viewModelScope.launch {
                val activeZones = dao.getActiveZones()
                geofenceHelper.syncAllActiveZones(activeZones)
            }
        } else {
            SmartKeepAliveService.stop(context)
        }
    }

    fun saveZone(zone: GeoZone, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val id = dao.insertZone(zone)
            val savedZone = zone.copy(id = id)
            geofenceHelper.addOrUpdateGeofence(savedZone) { success, error ->
                onComplete(success, error)
            }
        }
    }

    fun toggleZoneEnabled(zone: GeoZone, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = zone.copy(isEnabled = isEnabled)
            dao.updateZone(updated)
            geofenceHelper.addOrUpdateGeofence(updated)
        }
    }

    fun deleteZone(zone: GeoZone) {
        viewModelScope.launch {
            dao.deleteZone(zone)
            geofenceHelper.removeGeofence(zone.id)
        }
    }
}
