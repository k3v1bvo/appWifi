package com.smartwifi.switcher.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartwifi.switcher.ui.theme.*

@Composable
fun XiaomiGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configuración Xiaomi / MIUI",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryCyan
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "En tu Redmi Note 11 (MIUI / HyperOS), debes hacer estos 3 pasos para que el sistema no apague la app en segundo plano y pueda alternar los datos:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextLight
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Step 1: Autostart
                GuideStepItem(
                    stepNumber = "1",
                    title = "Inicio Automático",
                    description = "Permite que la app despierte sola cuando reinicies o salgas de casa.",
                    buttonText = "Abrir Inicio Automático",
                    icon = Icons.Default.PlayArrow,
                    onClick = { openMiuiAutostart(context) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Step 2: Battery Saver
                GuideStepItem(
                    stepNumber = "2",
                    title = "Batería: Sin Restricciones",
                    description = "Evita que MIUI cierre la app al bloquear la pantalla.",
                    buttonText = "Ajustar Batería",
                    icon = Icons.Default.BatteryChargingFull,
                    onClick = { openBatteryOptimization(context) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Step 3: Shizuku / Wireless Debugging
                GuideStepItem(
                    stepNumber = "3",
                    title = "Activar Shizuku (Para Datos)",
                    description = "Permite apagar los megas sin Root mediante Depuración Inalámbrica.",
                    buttonText = "Abrir Shizuku / Play Store",
                    icon = Icons.Default.Bolt,
                    onClick = { openShizukuOrStore(context) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                GuideStepItem(
                    stepNumber = "4",
                    title = "Opciones de Desarrollador",
                    description = "Activa 'Depuración USB (Ajustes de seguridad)' en MIUI.",
                    buttonText = "Opciones Desarrollador",
                    icon = Icons.Default.DeveloperMode,
                    onClick = { openDeveloperOptions(context) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Entendido, todo listo", color = Color.White)
            }
        }
    )
}

@Composable
private fun GuideStepItem(
    stepNumber: String,
    title: String,
    description: String,
    buttonText: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(stepNumber, color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextLight)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryCyan)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(buttonText)
            }
        }
    }
}

private fun openMiuiAutostart(context: Context) {
    try {
        val intent = Intent().apply {
            component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        openAppInfo(context)
    }
}

private fun openBatteryOptimization(context: Context) {
    try {
        val intent = Intent().apply {
            component = ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")
            putExtra("package_name", context.packageName)
            putExtra("package_label", "Smart Wifi Switch")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppInfo(context)
        }
    }
}

private fun openDeveloperOptions(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Abre Ajustes -> Ajustes adicionales -> Opciones de desarrollador", Toast.LENGTH_LONG).show()
    }
}

private fun openShizukuOrStore(context: Context) {
    val pm = context.packageManager
    val launchIntent = pm.getLaunchIntentForPackage("moe.shizuku.privileged.api")
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    } else {
        try {
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=moe.shizuku.privileged.api"))
            context.startActivity(playStoreIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api"))
            context.startActivity(webIntent)
        }
    }
}

private fun openAppInfo(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
