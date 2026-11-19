package com.smartwifi.switcher.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.wifi.WifiManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.regex.Pattern

data class NearbyWifiItem(
    val ssid: String,
    val level: Int, // dBm
    val isSecure: Boolean
)

object WifiKeychainHelper {

    /**
     * Generates an official standard Wi-Fi QR Code Bitmap compatible with Android, iOS, MIUI, and HyperOS camera scanners.
     * Format: WIFI:T:WPA;S:MyNetwork;P:MyPassword;;
     */
    fun generateWifiQrBitmap(ssid: String, password: String?, isHidden: Boolean = false, size: Int = 512): Bitmap? {
        return try {
            val cleanSsid = ssid.trim('\"')
            val authType = if (password.isNullOrBlank()) "nopass" else "WPA"
            val cleanPass = password ?: ""
            val hiddenFlag = if (isHidden) "true" else "false"
            val qrText = "WIFI:T:$authType;S:$cleanSsid;P:$cleanPass;H:$hiddenFlag;;"

            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(qrText, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copies a string (like Wi-Fi password or SSID) directly to the system clipboard.
     */
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
    }

    /**
     * Scans nearby Wi-Fi networks in physical range of the phone.
     */
    fun getNearbyWifiNetworks(context: Context): List<NearbyWifiItem> {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return emptyList()

        return try {
            val results = wifiManager.scanResults ?: emptyList()
            results.mapNotNull { scanResult ->
                val ssid = scanResult.SSID?.trim('\"')
                if (!ssid.isNullOrBlank() && ssid != "<unknown ssid>") {
                    val isSecure = scanResult.capabilities.contains("WPA") || scanResult.capabilities.contains("WEP")
                    NearbyWifiItem(ssid = ssid, level = scanResult.level, isSecure = isSecure)
                } else {
                    null
                }
            }.distinctBy { it.ssid }
                .sortedByDescending { it.level }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Attempts to extract saved Wi-Fi networks and passwords from Android's WifiConfigStore.xml
     * using elevated privileges (Shizuku wireless ADB or root fallback).
     */
    fun extractSystemSavedCredentials(): Map<String, String> {
        val paths = listOf(
            "/data/misc/apexdata/com.android.wifi/WifiConfigStore.xml",
            "/data/misc/wifi/WifiConfigStore.xml"
        )

        val resultMap = mutableMapOf<String, String>()

        for (path in paths) {
            val output = ShizukuManager.executeShellWithOutput("cat $path 2>/dev/null")
            if (!output.isNullOrBlank() && output.contains("WifiConfiguration")) {
                parseWifiConfigStoreXml(output, resultMap)
                if (resultMap.isNotEmpty()) break
            }
        }

        return resultMap
    }

    private fun parseWifiConfigStoreXml(xmlContent: String, outputMap: MutableMap<String, String>) {
        try {
            // Match blocks of WifiConfiguration
            val networkBlocks = xmlContent.split("<Network>")
            val ssidPattern = Pattern.compile("<string name=\"SSID\">&quot;(.*?)&quot;</string>")
            val pskPattern = Pattern.compile("<string name=\"PreSharedKey\">&quot;(.*?)&quot;</string>")

            for (block in networkBlocks) {
                val ssidMatcher = ssidPattern.matcher(block)
                val pskMatcher = pskPattern.matcher(block)

                if (ssidMatcher.find()) {
                    val ssid = ssidMatcher.group(1)
                    val psk = if (pskMatcher.find()) pskMatcher.group(1) else ""
                    if (!ssid.isNullOrBlank()) {
                        outputMap[ssid] = psk
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
