package com.smartwifi.switcher.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartwifi.switcher.ui.theme.*

@Composable
fun TestBenchDialog(
    onDismiss: () -> Unit,
    onTestData: (Boolean) -> Boolean,
    onTestWifi: (Boolean) -> Boolean
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
                        Icons.Outlined.Speed,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Diagnóstico de hardware",
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ejecuta comandos de prueba manuales para validar que las órdenes de sistema se apliquen sin errores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ContentSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Mobile Data Test Row
                Text(
                    text = "DATOS MÓVILES",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContentTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val ok = onTestData(false)
                            Toast.makeText(context, if (ok) "Datos móviles suspendidos" else "Fallo al ejecutar orden", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Suspender", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val ok = onTestData(true)
                            Toast.makeText(context, if (ok) "Datos móviles reactivados" else "Fallo al ejecutar orden", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reactivar", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wi-Fi Test Row
                Text(
                    text = "INTERFAZ WI-FI",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContentTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val ok = onTestWifi(false)
                            Toast.makeText(context, if (ok) "Wi-Fi desactivado" else "Fallo al ejecutar", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text("Apagar", style = MaterialTheme.typography.labelMedium, color = ContentPrimary)
                    }

                    OutlinedButton(
                        onClick = {
                            val ok = onTestWifi(true)
                            Toast.makeText(context, if (ok) "Wi-Fi activado" else "Fallo al ejecutar", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text("Encender", style = MaterialTheme.typography.labelMedium, color = AccentCyan)
                    }
                }
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
                Text("Finalizar")
            }
        }
    )
}
