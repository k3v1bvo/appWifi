package com.smartwifi.switcher.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smartwifi.switcher.service.WifiKeychainHelper
import com.smartwifi.switcher.ui.theme.*

@Composable
fun WifiQrDialog(
    placeName: String,
    ssid: String,
    password: String?,
    notes: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }

    val qrBitmap: Bitmap? = remember(ssid, password) {
        WifiKeychainHelper.generateWifiQrBitmap(ssid, password)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorderLight, RoundedCornerShape(22.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1117)),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = placeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ContentPrimary
                        )
                        Text(
                            text = "Red: $ssid",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = ContentTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // High Contrast QR Code Canvas (White background for 100% camera readability)
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Código QR de Wi-Fi",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(color = AccentCyan)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Apunta la cámara del teléfono al código para conectarte automáticamente sin escribir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ContentTertiary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Card with Show/Hide toggle & Copy Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CONTRASEÑA WI-FI",
                                style = MaterialTheme.typography.labelSmall,
                                color = ContentTertiary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when {
                                    password.isNullOrBlank() -> "Sin contraseña guardada (abierta)"
                                    isPasswordVisible -> password
                                    else -> "••••••••••••"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (password.isNullOrBlank()) ContentTertiary else ContentPrimary,
                                fontFamily = if (!password.isNullOrBlank()) FontFamily.Monospace else FontFamily.Default
                            )
                        }

                        if (!password.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Toggle eye icon
                                IconButton(
                                    onClick = { isPasswordVisible = !isPasswordVisible },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Ver contraseña",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Copy to clipboard
                                IconButton(
                                    onClick = {
                                        WifiKeychainHelper.copyToClipboard(context, "Wi-Fi $ssid", password)
                                        Toast.makeText(context, "✅ Contraseña copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.ContentCopy,
                                        contentDescription = "Copiar contraseña",
                                        tint = ContentSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (!notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceElevated,
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = ContentTertiary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = notes, style = MaterialTheme.typography.labelSmall, color = ContentSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = ContentPrimary),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}
