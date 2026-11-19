package com.smartwifi.switcher.ui.screens

import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.location.LocationServices
import com.smartwifi.switcher.service.WifiHelper
import com.smartwifi.switcher.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun QuickAddZoneDialog(
    initialPreset: String = "Casa",
    onDismiss: () -> Unit,
    onSave: (name: String, ssid: String?, lat: Double, lng: Double, radius: Float) -> Unit,
    onOpenMapPicker: () -> Unit
) {
    val context = LocalContext.current
    var selectedPreset by remember { mutableStateOf(initialPreset) }
    var zoneName by remember { mutableStateOf(initialPreset) }

    val detectedSsid = remember { WifiHelper.getCurrentSsid(context) }
    var ssidInput by remember { mutableStateOf(detectedSsid ?: "") }

    var currentLat by remember { mutableDoubleStateOf(-17.3935) }
    var currentLng by remember { mutableDoubleStateOf(-66.1570) }
    var locationFetched by remember { mutableStateOf(false) }
    var radiusMeters by remember { mutableFloatStateOf(120f) }

    // Auto-fetch current coordinates
    LaunchedEffect(Unit) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    currentLat = loc.latitude
                    currentLng = loc.longitude
                    locationFetched = true
                }
            }
        } catch (_: SecurityException) {}
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorderLight, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GlowCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AddLocation,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Zona de Confianza",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ContentPrimary
                            )
                            Text(
                                text = "Apaga datos al entrar y ahorra megas",
                                style = MaterialTheme.typography.labelSmall,
                                color = ContentTertiary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = ContentTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets (Casa, Trabajo, Casa Juan, Amigo)
                Text(
                    text = "SELECCIONA TIPO DE LUGAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContentTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip(
                        icon = Icons.Outlined.Home,
                        label = "Casa",
                        isSelected = selectedPreset == "Casa",
                        onClick = {
                            selectedPreset = "Casa"
                            zoneName = "Casa"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PresetChip(
                        icon = Icons.Outlined.Work,
                        label = "Trabajo",
                        isSelected = selectedPreset == "Trabajo",
                        onClick = {
                            selectedPreset = "Trabajo"
                            zoneName = "Trabajo"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PresetChip(
                        icon = Icons.Outlined.Person,
                        label = "Casa Juan",
                        isSelected = selectedPreset == "Casa Juan",
                        onClick = {
                            selectedPreset = "Casa Juan"
                            zoneName = "Casa Juan"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Name Field
                OutlinedTextField(
                    value = zoneName,
                    onValueChange = { zoneName = it },
                    label = { Text("Nombre de la ubicación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ContentPrimary,
                        unfocusedTextColor = ContentPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = ContentSecondary,
                        cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Wi-Fi SSID Field
                OutlinedTextField(
                    value = ssidInput,
                    onValueChange = { ssidInput = it },
                    label = { Text("Nombre de la red Wi-Fi (SSID)") },
                    supportingText = {
                        if (detectedSsid != null && ssidInput == detectedSsid) {
                            Text("✓ Red actual detectada automáticamente", color = AccentEmerald)
                        } else {
                            Text("Puedes poner varias separadas por coma", color = ContentTertiary)
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Wifi, contentDescription = null, tint = AccentCyan)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ContentPrimary,
                        unfocusedTextColor = ContentPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = ContentSecondary,
                        cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // GPS & Accuracy Row
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.MyLocation,
                                contentDescription = null,
                                tint = if (locationFetched) AccentEmerald else AccentAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (locationFetched) "Coordenadas fijadas" else "Buscando satélites...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ContentPrimary
                                )
                                Text(
                                    text = "Radio Geocerca: ${radiusMeters.roundToInt()}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ContentTertiary
                                )
                            }
                        }

                        TextButton(
                            onClick = onOpenMapPicker,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Ver en mapa", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text("Cancelar", color = ContentSecondary)
                    }

                    Button(
                        onClick = {
                            if (zoneName.isNotBlank()) {
                                onSave(
                                    zoneName.trim(),
                                    ssidInput.trim().ifEmpty { null },
                                    currentLat,
                                    currentLng,
                                    radiusMeters
                                )
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            "Guardar Zona",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else SurfaceCard,
        border = BorderStroke(1.dp, if (isSelected) AccentCyan else SurfaceBorder),
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) AccentCyan else ContentSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) AccentCyan else ContentPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
