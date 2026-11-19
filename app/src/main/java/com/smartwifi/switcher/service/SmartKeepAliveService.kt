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
import com.smartwifi.switcher.data.AppDatabase
import kotlinx.coroutines.*

class SmartKeepAliveService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var currentConnectedSsid: String? = null

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

        val notification = createNotification("Smart Wifi Switch Activo", "Modo inteligente activo (Consumo mínimo de batería)")
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
                // Short delay to let Android obtain WifiInfo
                serviceScope.launch {
                    delay(1200)
                    val ssid = WifiHelper.getCurrentSsid(this@SmartKeepAliveService)
                    currentConnectedSsid = ssid
                    Log.d(TAG, "Wi-Fi conectado a: $ssid")

                    if (ssid != null) {
                        handleWifiConnected(ssid)
                    }
                }
            }

            override fun onLost(network: Network) {
                val previousSsid = currentConnectedSsid
                currentConnectedSsid = null
                Log.d(TAG, "Wi-Fi desconectado (estaba en: $previousSsid)")

                if (previousSsid != null) {
                    handleWifiDisconnected(previousSsid)
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

        // Match by linked Wi-Fi SSID
        val matchedZone = activeZones.find { zone ->
            zone.linkedWifiSsid?.equals(ssid, ignoreCase = true) == true
        }

        if (matchedZone != null && matchedZone.isEnabled) {
            Log.d(TAG, "Red de casa detectada (${matchedZone.name}): Apagando datos móviles...")
            if (matchedZone.dataOffAtEntry) {
                ConnectionController.setMobileDataState(this, false)
            }
            updateNotification(
                "En ${matchedZone.name} ($ssid)",
                "✅ Wi-Fi Conectado • Megas apagados (0% consumo de batería)"
            )
        }
    }

    private suspend fun handleWifiDisconnected(previousSsid: String) {
        val db = AppDatabase.getDatabase(this)
        val activeZones = db.geoZoneDao().getActiveZones()

        val matchedZone = activeZones.find { zone ->
            zone.linkedWifiSsid?.equals(previousSsid, ignoreCase = true) == true
        }

        if (matchedZone != null && matchedZone.isEnabled) {
            Log.d(TAG, "Saliste de la cobertura de ${matchedZone.name}: Encendiendo megas...")
            if (matchedZone.dataOnAtExit) {
                ConnectionController.setMobileDataState(this, true)
            }
            updateNotification(
                "Fuera de ${matchedZone.name}",
                "📶 Wi-Fi desconectado • Megas encendidos para seguir conectado"
            )
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SmartWifiApp.CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, createNotification(title, content))
    }

    override fun onDestroy() {
        super.onDestroy()
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
