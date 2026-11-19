package com.smartwifi.switcher.ui.screens

import android.location.Location
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.location.LocationServices
import com.smartwifi.switcher.service.NearbyWifiItem
import com.smartwifi.switcher.service.WifiHelper
import com.smartwifi.switcher.service.WifiKeychainHelper
import com.smartwifi.switcher.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun QuickAddZoneDialog(
    initialPreset: String = "Casa",
    onDismiss: () -> Unit,
    onSave: (name: String, ssid: String?, password: String?, category: String, notes: String?, lat: Double, lng: Double, radius: Float) -> Unit,
    onOpenMapPicker: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember {
        mutableStateOf(
            when (initialPreset) {
                "Casa" -> "HOME"
                "Trabajo" -> "WORK"
                "Casa Juan" -> "FRIEND"
                else -> "CUSTOM"
            }
        )
    }
    var zoneName by remember { mutableStateOf(initialPreset) }

    val detectedSsid = remember { WifiHelper.getCurrentSsid(context) }
    var ssidInput by remember { mutableStateOf(detectedSsid ?: "") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var notesInput by remember { mutableStateOf("") }

    var currentLat by remember { mutableDoubleStateOf(-17.3935) }
    var currentLng by remember { mutableDoubleStateOf(-66.1570) }
    var locationFetched by remember { mutableStateOf(false) }
    var radiusMeters by remember { mutableFloatStateOf(120f) }

    // Nearby Wi-Fi scan state
    var nearbyNetworks by remember { mutableStateOf<List<NearbyWifiItem>>(emptyList()) }
    var isScanningWifi by remember { mutableStateOf(false) }

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

        // Attempt to auto-fill password if system credentials exist
        if (!detectedSsid.isNullOrBlank()) {
            val creds = WifiKeychainHelper.extractSystemSavedCredentials()
            val savedPass = creds[detectedSsid]
            if (!savedPass.isNullOrBlank()) {
                passwordInput = savedPass
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .border(1.dp, SurfaceBorderLight, RoundedCornerShape(22.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                                .size(38.dp)
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
                                text = "Guardar Zona Segura",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ContentPrimary
                            )
                            Text(
                                text = "Apaga datos al entrar y guarda su Wi-Fi",
                                style = MaterialTheme.typography.labelSmall,
                                color = ContentTertiary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = ContentTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Presets
                Text(
                    text = "CATEGORÍA DEL LUGAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContentTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        PresetChip(
                            icon = Icons.Outlined.Home,
                            label = "Casa",
                            isSelected = selectedCategory == "HOME",
                            onClick = {
                                selectedCategory = "HOME"
                                zoneName = "Casa"
                            }
                        )
                    }
                    item {
                        PresetChip(
                            icon = Icons.Outlined.Work,
                            label = "Trabajo",
                            isSelected = selectedCategory == "WORK",
                            onClick = {
                                selectedCategory = "WORK"
                                zoneName = "Trabajo"
                            }
                        )
                    }
                    item {
                        PresetChip(
                            icon = Icons.Outlined.Person,
                            label = "Casa Juan / Amigo",
                            isSelected = selectedCategory == "FRIEND",
                            onClick = {
                                selectedCategory = "FRIEND"
                                zoneName = "Casa Juan"
                            }
                        )
                    }
                    item {
                        PresetChip(
                            icon = Icons.Outlined.School,
                            label = "Estudio / Uni",
                            isSelected = selectedCategory == "SCHOOL",
                            onClick = {
                                selectedCategory = "SCHOOL"
                                zoneName = "Universidad"
                            }
                        )
                    }
                    item {
                        PresetChip(
                            icon = Icons.Outlined.FitnessCenter,
                            label = "Gimnasio",
                            isSelected = selectedCategory == "GYM",
                            onClick = {
                                selectedCategory = "GYM"
                                zoneName = "Gimnasio"
                            }
                        )
                    }
                    item {
                        PresetChip(
                            icon = Icons.Outlined.LocalCafe,
                            label = "Cafetería",
                            isSelected = selectedCategory == "CAFE",
                            onClick = {
                                selectedCategory = "CAFE"
                                zoneName = "Cafetería"
                            }
                        )
                    }
                    item {
                        PresetChip(
                            icon = Icons.Outlined.Place,
                            label = "Otro",
                            isSelected = selectedCategory == "CUSTOM",
                            onClick = {
                                selectedCategory = "CUSTOM"
                                if (zoneName.isBlank() || zoneName == "Casa") zoneName = "Nuevo Lugar"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Name Field
                OutlinedTextField(
                    value = zoneName,
                    onValueChange = { zoneName = it },
                    label = { Text("Nombre del lugar") },
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
                    label = { Text("Nombre de red Wi-Fi (SSID)") },
                    supportingText = {
                        if (detectedSsid != null && ssidInput == detectedSsid) {
                            Text("✓ Red actual conectada detectada", color = AccentEmerald)
                        } else {
                            Text("Nombre exacto del Wi-Fi de este lugar", color = ContentTertiary)
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Wifi, contentDescription = null, tint = AccentCyan)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            isScanningWifi = true
                            nearbyNetworks = WifiKeychainHelper.getNearbyWifiNetworks(context)
                        }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Escanear Wi-Fi", tint = AccentCyan)
                        }
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

                // Nearby Wi-Fi chips if scanned
                if (isScanningWifi && nearbyNetworks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Redes detectadas al alcance:", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(nearbyNetworks) { item ->
                            Surface(
                                onClick = {
                                    ssidInput = item.ssid
                                    isScanningWifi = false
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = SurfaceCard,
                                border = BorderStroke(1.dp, SurfaceBorderLight)
                            ) {
                                Text(
                                    text = "📶 ${item.ssid}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ContentPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Wi-Fi Password Field (Keychain / Vault)
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Contraseña Wi-Fi (Opcional)") },
                    supportingText = {
                        Text("Se guardará en tu llavero para verla o compartirla por QR", color = ContentTertiary)
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Key, contentDescription = null, tint = AccentCyan)
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Ver contraseña",
                                tint = ContentSecondary
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

                Spacer(modifier = Modifier.height(10.dp))

                // Notes Field (Optional)
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notas o recordatorios (Opcional)") },
                    placeholder = { Text("Ej. Piso 3, pedir permiso a Juan") },
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
                                    text = if (locationFetched) "GPS Fijado con éxito" else "Buscando satélites...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ContentPrimary
                                )
                                Text(
                                    text = "Radio de geocerca: ${radiusMeters.roundToInt()}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ContentTertiary
                                )
                            }
                        }

                        TextButton(
                            onClick = onOpenMapPicker,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Ajustar en mapa", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
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
                                    passwordInput.trim().ifEmpty { null },
                                    selectedCategory,
                                    notesInput.trim().ifEmpty { null },
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
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else SurfaceCard,
        border = BorderStroke(1.dp, if (isSelected) AccentCyan else SurfaceBorder),
        modifier = Modifier.height(42.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
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
