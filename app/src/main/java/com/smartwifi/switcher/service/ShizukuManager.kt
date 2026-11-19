package com.smartwifi.switcher.service

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuManager {

    const val SHIZUKU_PERMISSION_REQUEST_CODE = 8001

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return if (isShizukuAvailable()) {
            try {
                if (Shizuku.isPreV11()) {
                    false
                } else {
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                }
            } catch (e: Throwable) {
                false
            }
        } else {
            false
        }
    }

    fun requestPermission(listener: Shizuku.OnRequestPermissionResultListener) {
        if (isShizukuAvailable() && !hasPermission()) {
            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
        }
    }

    fun executeShell(command: String): Boolean {
        if (!hasPermission()) return false
        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                proc.waitFor() == 0
            } catch (_: Exception) {
                false
            }
        }
    }
}
