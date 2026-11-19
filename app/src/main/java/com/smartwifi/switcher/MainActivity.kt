package com.smartwifi.switcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.smartwifi.switcher.service.ShizukuManager
import com.smartwifi.switcher.ui.screens.HomeScreen
import com.smartwifi.switcher.ui.screens.MapPickerScreen
import com.smartwifi.switcher.ui.theme.SmartWifiSwitchTheme
import com.smartwifi.switcher.ui.viewmodel.ZoneViewModel
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val viewModel: ZoneViewModel by viewModels()

    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuManager.SHIZUKU_PERMISSION_REQUEST_CODE) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            if (granted) {
                Toast.makeText(this, "✅ Permiso Shizuku concedido. Control de datos activo.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "⚠️ Permiso Shizuku denegado.", Toast.LENGTH_SHORT).show()
            }
            viewModel.refreshShizukuStatus()
        }
    }

    private val shizukuBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        viewModel.refreshShizukuStatus()
    }

    private val shizukuBinderDeadListener = Shizuku.OnBinderDeadListener {
        viewModel.refreshShizukuStatus()
    }

    private val requestNotificationAndForegroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineLocationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkAndRequestBackgroundLocation()
        }
    }

    private val requestBackgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "✅ Ubicación en segundo plano concedida.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "⚠️ Se requiere 'Permitir todo el tiempo' para detectar tu casa con la pantalla apagada.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shizuku.addBinderReceivedListener(shizukuBinderReceivedListener)
        Shizuku.addBinderDeadListener(shizukuBinderDeadListener)

        checkInitialPermissions()

        setContent {
            SmartWifiSwitchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val zones by viewModel.allZones.collectAsState()
                    val recentLogs by viewModel.recentLogs.collectAsState()
                    val isServiceActive by viewModel.isServiceActive.collectAsState()
                    val isShizukuActive by viewModel.isShizukuActive.collectAsState()

                    var currentScreen by remember { mutableStateOf("home") }

                    when (currentScreen) {
                        "home" -> {
                            HomeScreen(
                                zones = zones,
                                recentLogs = recentLogs,
                                isServiceActive = isServiceActive,
                                isShizukuActive = isShizukuActive,
                                onToggleService = { viewModel.toggleService(it) },
                                onToggleZone = { zone, enabled -> viewModel.toggleZoneEnabled(zone, enabled) },
                                onDeleteZone = { viewModel.deleteZone(it) },
                                onAddZoneClick = { currentScreen = "map_picker" },
                                onRequestShizukuPermission = {
                                    if (ShizukuManager.isShizukuAvailable()) {
                                        ShizukuManager.requestPermission(shizukuPermissionListener)
                                    } else {
                                        Toast.makeText(this, "Instala y abre Shizuku con Depuración Inalámbrica primero.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onClearLogs = { viewModel.clearLogs() },
                                onTestData = { viewModel.testData(it) },
                                onTestWifi = { viewModel.testWifi(it) }
                            )
                        }
                        "map_picker" -> {
                            MapPickerScreen(
                                onNavigateBack = { currentScreen = "home" },
                                onSaveZone = { newZone ->
                                    viewModel.saveZone(newZone) { success, error ->
                                        if (success) {
                                            Toast.makeText(this, "Zona '${newZone.name}' guardada y activada", Toast.LENGTH_SHORT).show()
                                            currentScreen = "home"
                                        } else {
                                            Toast.makeText(this, "Error al activar: $error", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkInitialPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            requestNotificationAndForegroundLocationLauncher.launch(missing.toTypedArray())
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkAndRequestBackgroundLocation()
        }
    }

    private fun checkAndRequestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasBgLocation = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasBgLocation) {
                requestBackgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshShizukuStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeBinderReceivedListener(shizukuBinderReceivedListener)
        Shizuku.removeBinderDeadListener(shizukuBinderDeadListener)
    }
}
