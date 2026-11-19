package com.smartwifi.switcher.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartwifi.switcher.ui.theme.*

@Composable
fun XiaomiGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceElevated,
        modifier = Modifier.border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.SettingsSuggest,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Configuración MIUI / HyperOS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ContentPrimary
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = ContentTertiary)
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
                    text = "Ajustes esenciales para garantizar que el gestor de memoria de Xiaomi mantenga el servicio activo:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ContentSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                GuideStepItem(
                    stepNumber = "1",
                    title = "Inicio automático",
                    description = "Permite la reactivación del servicio tras reinicios del terminal.",
                    buttonText = "Configurar inicio",
                    icon = Icons.Outlined.PowerSettingsNew,
                    onClick = { openMiuiAutostart(context) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideStepItem(
                    stepNumber = "2",
                    title = "Batería sin restricciones",
                    description = "Exime la app del cierre forzoso al bloquear el dispositivo.",
                    buttonText = "Eximir de ahorro",
                    icon = Icons.Outlined.BatterySaver,
                    onClick = { openBatteryOptimization(context) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideStepItem(
                    stepNumber = "3",
                    title = "Permisos de sistema (Shizuku)",
                    description = "Concede acceso a la ejecución de órdenes de red mediante depuración inalámbrica.",
                    buttonText = "Abrir Shizuku",
                    icon = Icons.Outlined.Terminal,
                    onClick = { openShizukuOrStore(context) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideStepItem(
                    stepNumber = "4",
                    title = "Opciones de desarrollo",
                    description = "Habilita 'Depuración USB (Ajustes de seguridad)' en MIUI.",
                    buttonText = "Opciones de desarrollador",
                    icon = Icons.Outlined.DeveloperMode,
                    onClick = { openDeveloperOptions(context) }
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = SurfaceCard,
                    contentColor = ContentPrimary
                )
            ) {
                Text("Entendido")
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentBlue.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f)),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(stepNumber, color = AccentBlueGlow, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = ContentPrimary)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = ContentSecondary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText, style = MaterialTheme.typography.labelMedium, color = ContentPrimary)
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
