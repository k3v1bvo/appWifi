package com.smartwifi.switcher.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale

data class NetworkSummary(
    val isWifiConnected: Boolean,
    val currentSsid: String?,
    val localIp: String,
    val subnetMask: String,
    val isMobileDataActive: Boolean
)

object WifiHelper {

    fun getCurrentSsid(context: Context): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return null

        val activeNetwork = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return null

        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return null
        }

        var ssid: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val wifiInfo = capabilities.transportInfo as? WifiInfo
            if (wifiInfo != null) {
                ssid = wifiInfo.ssid
            }
        }

        // Fallback for older Android or when transportInfo is null
        if (ssid == null || ssid == "<unknown ssid>") {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val info = wifiManager?.connectionInfo
            ssid = info?.ssid
        }

        if (ssid != null) {
            val cleaned = ssid.trim('\"')
            return if (cleaned != "<unknown ssid>" && cleaned.isNotBlank()) cleaned else null
        }

        return null
    }

    fun isConnectedToWifi(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun isMobileDataActive(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    fun getLocalIpAddress(context: Context): String {
        // Method 1: Check network interfaces for active wlan/eth
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in Collections.list(interfaces)) {
                if (intf.isUp && (intf.name.contains("wlan") || intf.name.contains("eth") || intf.name.contains("rmnet"))) {
                    for (addr in Collections.list(intf.inetAddresses)) {
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            val ip = addr.hostAddress
                            if (!ip.isNullOrBlank()) return ip
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // Method 2: WifiManager fallback
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val ipInt = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                return String.format(
                    Locale.US,
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            }
        } catch (_: Exception) {}

        return "192.168.1.1"
    }

    fun getSubnetMask(context: Context): String {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val dhcp = wifiManager?.dhcpInfo
            if (dhcp != null && dhcp.netmask != 0) {
                val mask = dhcp.netmask
                return String.format(
                    Locale.US,
                    "%d.%d.%d.%d",
                    mask and 0xff,
                    mask shr 8 and 0xff,
                    mask shr 16 and 0xff,
                    mask shr 24 and 0xff
                )
            }
        } catch (_: Exception) {}
        return "255.255.255.0"
    }

    fun getNetworkSummary(context: Context): NetworkSummary {
        val wifiConnected = isConnectedToWifi(context)
        val ssid = getCurrentSsid(context)
        val localIp = if (wifiConnected) getLocalIpAddress(context) else "--"
        val subnet = if (wifiConnected) getSubnetMask(context) else "--"
        val mobileActive = isMobileDataActive(context)

        return NetworkSummary(
            isWifiConnected = wifiConnected,
            currentSsid = ssid,
            localIp = localIp,
            subnetMask = subnet,
            isMobileDataActive = mobileActive
        )
    }
}
