package com.smartwifi.switcher.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.smartwifi.switcher.MainActivity
import com.smartwifi.switcher.R
import com.smartwifi.switcher.SmartWifiApp
import com.smartwifi.switcher.data.ActivityLog
import com.smartwifi.switcher.data.AppDatabase
import kotlinx.coroutines.*

class SmartKeepAliveService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var currentConnectedSsid: String? = null
    private var pendingDisconnectJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        setupSmartNetworkMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createSilentNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    private fun setupSmartNetworkMonitoring() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // If there was a pending disconnect buffer running, CANCEL IT!
                // This means the router blinked or signal dropped momentarily inside the house!
                if (pendingDisconnectJob?.isActive == true) {
                    pendingDisconnectJob?.cancel()
                    pendingDisconnectJob = null
                    Log.d(TAG, "⚡ Pestañeo de Wi-Fi evitado: la señal volvió antes de que se encendieran los megas.")
                    logEvent("🛡️ Pestañeo de Wi-Fi evitado", "La conexión volvió a tiempo. No se consumieron megas por error.", "WIFI_RESTORED")
                }

                serviceScope.launch {
                    delay(1200) // Allow Android to associate WifiInfo
                    val ssid = WifiHelper.getCurrentSsid(this@SmartKeepAliveService)
                    currentConnectedSsid = ssid
                    Log.d(TAG, "Wi-Fi conectado a: $ssid")

                    if (ssid != null) {
                        handleWifiConnected(ssid)
                    }
                }
            }

            override fun onLost(network: Network) {
                val lostSsid = currentConnectedSsid
                currentConnectedSsid = null
                Log.d(TAG, "Wi-Fi desconectado (estaba en: $lostSsid)")

                if (lostSsid != null) {
                    handleWifiDisconnectedWithBuffer(lostSsid)
                }
            }
        }

        try {
            connectivityManager?.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando NetworkCallback: ${e.message}")
        }
    }

    private suspend fun handleWifiConnected(ssid: String) {
        val db = AppDatabase.getDatabase(this)
        val activeZones = db.geoZoneDao().getActiveZones()

        // Multi-SSID matching (supports 2.4G, 5G, or repeaters)
        val matchedZone = activeZones.find { zone ->
            zone.matchesSsid(ssid)
        }

        if (matchedZone != null && matchedZone.isEnabled) {
            Log.d(TAG, "Red de casa detectada (${matchedZone.name}): Apagando datos móviles...")
            if (matchedZone.dataOffAtEntry) {
                ConnectionController.setMobileDataState(this, false)
            }
            if (matchedZone.vibrateOnAction) {
                VibratorHelper.vibrateArrival(this)
            }
            logEvent("📍 Llegaste a ${matchedZone.name}", "Conectado a $ssid • Megas apagados para ahorrar", "ARRIVED")
        }
    }

    // Grace Period: Anti-Flapping Buffer (Wait 15s-20s before turning on mobile data)
    private fun handleWifiDisconnectedWithBuffer(lostSsid: String) {
        pendingDisconnectJob?.cancel()
        pendingDisconnectJob = serviceScope.launch {
            val db = AppDatabase.getDatabase(this@SmartKeepAliveService)
            val activeZones = db.geoZoneDao().getActiveZones()

            val matchedZone = activeZones.find { zone ->
                zone.matchesSsid(lostSsid)
            } ?: return@launch

            if (!matchedZone.isEnabled) return@launch

            val bufferSeconds = matchedZone.disconnectBufferSeconds.coerceAtLeast(5)
            Log.d(TAG, "Wi-Fi de casa perdido. Esperando buffer anti-pestañeo de $bufferSeconds segundos...")

            delay(bufferSeconds * 1000L)

            // If still disconnected after buffer: Confirm departure and enable mobile data
            Log.d(TAG, "Buffer expirado. Salida confirmada de ${matchedZone.name}: Encendiendo megas...")
            if (matchedZone.dataOnAtExit) {
                ConnectionController.setMobileDataState(this@SmartKeepAliveService, true)
            }
            if (matchedZone.vibrateOnAction) {
                VibratorHelper.vibrateDeparture(this@SmartKeepAliveService)
            }
            logEvent("🚶 Saliste de ${matchedZone.name}", "Wi-Fi desconectado • Megas encendidos para seguir con internet", "DEPARTED")
        }
    }

    private fun logEvent(title: String, desc: String, type: String) {
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@SmartKeepAliveService)
                db.activityLogDao().insertLog(ActivityLog(title = title, description = desc, type = type))
            } catch (e: Exception) {
                Log.e(TAG, "Error saving log: ${e.message}")
            }
        }
    }

    // Completely Silent, Minimized Notification (No popups, no sound, no status bar invasion)
    private fun createSilentNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SmartWifiApp.CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Smart Wifi Switch")
            .setContentText("Ahorro inteligente activo en segundo plano")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setShowWhen(false)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        pendingDisconnectJob?.cancel()
        serviceScope.cancel()
        networkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (_: Exception) {}
        }
    }

    companion object {
        const val TAG = "SmartKeepAliveService"
        const val NOTIFICATION_ID = 9001
        const val ACTION_STOP_SERVICE = "com.smartwifi.ACTION_STOP_SERVICE"

        fun start(context: Context) {
            val intent = Intent(context, SmartKeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SmartKeepAliveService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
