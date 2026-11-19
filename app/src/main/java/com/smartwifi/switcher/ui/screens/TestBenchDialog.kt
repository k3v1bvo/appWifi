package com.smartwifi.switcher.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🧪 Panel de Diagnóstico Rápido",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryCyan
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Prueba que las órdenes de sistema se ejecuten correctamente sin tener que salir de casa:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextLight
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mobile Data Tests
                Text("Datos Móviles (Megas):", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val ok = onTestData(false)
                            Toast.makeText(context, if (ok) "✅ Megas apagados con éxito" else "❌ Error: Verifica Shizuku", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apagar Megas", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val ok = onTestData(true)
                            Toast.makeText(context, if (ok) "✅ Megas encendidos con éxito" else "❌ Error: Verifica Shizuku", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Prender Megas", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wi-Fi Tests
                Text("Conexión Wi-Fi:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val ok = onTestWifi(false)
                            Toast.makeText(context, if (ok) "✅ Wi-Fi apagado" else "❌ Error en Wi-Fi", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLight)
                    ) {
                        Text("Apagar Wi-Fi")
                    }

                    OutlinedButton(
                        onClick = {
                            val ok = onTestWifi(true)
                            Toast.makeText(context, if (ok) "✅ Wi-Fi encendido" else "❌ Error en Wi-Fi", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryCyan)
                    ) {
                        Text("Prender Wi-Fi")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Listo")
            }
        }
    )
}
