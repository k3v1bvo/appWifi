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
import com.smartwifi.switcher.data.GeoZone
import com.smartwifi.switcher.service.NearbyWifiItem
import com.smartwifi.switcher.service.WifiHelper
import com.smartwifi.switcher.service.WifiKeychainHelper
import com.smartwifi.switcher.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun EditZoneDialog(
    zone: GeoZone,
    onDismiss: () -> Unit,
    onSave: (GeoZone) -> Unit,
    onDelete: (GeoZone) -> Unit,
    onOpenMapPicker: ((GeoZone) -> Unit)? = null
) {
    val context = LocalContext.current

    var zoneName by remember { mutableStateOf(zone.name) }
    var selectedCategory by remember { mutableStateOf(zone.category) }
    var ssidInput by remember { mutableStateOf(zone.linkedWifiSsids ?: "") }
    var passwordInput by remember { mutableStateOf(zone.wifiPassword ?: "") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var notesInput by remember { mutableStateOf(zone.notes ?: "") }

    var lat by remember { mutableDoubleStateOf(zone.latitude) }
    var lng by remember { mutableDoubleStateOf(zone.longitude) }
    var radiusMeters by remember { mutableFloatStateOf(zone.radiusMeters) }

    // Rules
    var dataOffAtEntry by remember { mutableStateOf(zone.dataOffAtEntry) }
    var wifiOnAtEntry by remember { mutableStateOf(zone.wifiOnAtEntry) }
    var dataOnAtExit by remember { mutableStateOf(zone.dataOnAtExit) }
    var wifiOffAtExit by remember { mutableStateOf(zone.wifiOffAtExit) }
    var bufferSeconds by remember { mutableIntStateOf(zone.disconnectBufferSeconds) }

    // Nearby Wi-Fi scanner
    var nearbyNetworks by remember { mutableStateOf<List<NearbyWifiItem>>(emptyList()) }
    var isScanningWifi by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val categories = listOf(
        Triple("HOME", "Casa", Icons.Outlined.Home),
        Triple("WORK", "Trabajo", Icons.Outlined.Work),
        Triple("FRIEND", "Juan / Amigos", Icons.Outlined.Person),
        Triple("SCHOOL", "Estudio", Icons.Outlined.School),
        Triple("GYM", "Gimnasio", Icons.Outlined.FitnessCenter),
        Triple("CAFE", "Café", Icons.Outlined.LocalCafe),
        Triple("CUSTOM", "Otro", Icons.Outlined.Place)
    )

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
                            Icon(Icons.Filled.EditLocation, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Configurar Zona",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ContentPrimary
                            )
                            Text(
                                text = "Ajusta nombres, redes y cerco geográfico",
                                style = MaterialTheme.typography.labelSmall,
                                color = ContentTertiary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = ContentTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. NOMBRE DEL LUGAR Y CATEGORÍA
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "1. NOMBRE DEL LUGAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = zoneName,
                                onValueChange = { zoneName = it },
                                label = { Text("Nombre (Ej: Casa, Trabajo, Casa Juan)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = ContentPrimary,
                                    unfocusedTextColor = ContentPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Categoría del icono:", style = MaterialTheme.typography.labelSmall, color = ContentTertiary)
                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(categories) { (catKey, catLabel, catIcon) ->
                                    val isSelected = selectedCategory == catKey
                                    Surface(
                                        onClick = { selectedCategory = catKey },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else Color(0xFF161820),
                                        border = BorderStroke(1.dp, if (isSelected) AccentCyan else SurfaceBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(catIcon, contentDescription = null, tint = if (isSelected) AccentCyan else ContentSecondary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = catLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) AccentCyan else ContentPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. REDES WI-FI VINCULADAS (SSID)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2. RED WI-FI ASOCIADA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                // Scan nearby networks button
                                TextButton(
                                    onClick = {
                                        isScanningWifi = true
                                        val scan = WifiKeychainHelper.scanNearbyWifi(context)
                                        nearbyNetworks = scan
                                        isScanningWifi = false
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Filled.WifiFind, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Escanear Wi-Fi", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = ssidInput,
                                onValueChange = { ssidInput = it },
                                label = { Text("Nombre de red Wi-Fi (SSID)") },
                                placeholder = { Text("Ej: MiFibra_5G, Casa_2.4G") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    val current = WifiHelper.getCurrentSsid(context)
                                    if (!current.isNullOrBlank()) {
                                        IconButton(onClick = {
                                            if (ssidInput.isBlank()) {
                                                ssidInput = current
                                            } else if (!ssidInput.contains(current)) {
                                                ssidInput = "$ssidInput, $current"
                                            }
                                        }) {
                                            Icon(Icons.Filled.Sync, contentDescription = "Copiar actual", tint = AccentEmerald)
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = ContentPrimary,
                                    unfocusedTextColor = ContentPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Text(
                                text = "💡 Tip: Puedes poner varias redes separadas por coma (ej: Casa_5G, Casa_2.4G). Al conectarte a cualquiera, se apagarán los megas.",
                                style = MaterialTheme.typography.labelSmall,
                                color = ContentTertiary,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            // Scanned Wi-Fi list dropdown
                            if (nearbyNetworks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Redes detectadas al alcance (toca para asignar):", style = MaterialTheme.typography.labelSmall, color = AccentEmerald)
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    nearbyNetworks.take(4).forEach { net ->
                                        Surface(
                                            onClick = {
                                                if (ssidInput.isBlank()) {
                                                    ssidInput = net.ssid
                                                } else if (!ssidInput.contains(net.ssid)) {
                                                    ssidInput = "$ssidInput, ${net.ssid}"
                                                }
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF141A22),
                                            border = BorderStroke(1.dp, SurfaceBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(net.ssid, style = MaterialTheme.typography.labelMedium, color = ContentPrimary, fontFamily = FontFamily.Monospace)
                                                Text("${net.signalStrengthPercent}% señal", style = MaterialTheme.typography.labelSmall, color = ContentSecondary)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Contraseña Wi-Fi (Para verla o generar QR)") },
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = "Mostrar contraseña",
                                            tint = ContentSecondary
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = ContentPrimary,
                                    unfocusedTextColor = ContentPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // 3. CERCO GEOGRÁFICO Y RADIO EN METROS
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "3. CERCO GEOGRÁFICO (GEOFENCE)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${radiusMeters.roundToInt()} metros",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Distancia a la redonda donde se considera que estás en este lugar.",
                                style = MaterialTheme.typography.labelSmall,
                                color = ContentTertiary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Slider(
                                value = radiusMeters,
                                onValueChange = { radiusMeters = it },
                                valueRange = 50f..500f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentCyan,
                                    activeTrackColor = AccentCyan,
                                    inactiveTrackColor = SurfaceBorder
                                )
                            )

                            // Quick radius chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    50f to "50m (Depa)",
                                    100f to "100m (Casa)",
                                    180f to "180m (Oficina)",
                                    350f to "350m (Lote)"
                                ).forEach { (rad, radLabel) ->
                                    val isRadSelected = (radiusMeters - rad).let { kotlin.math.abs(it) < 20 }
                                    Surface(
                                        onClick = { radiusMeters = rad },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isRadSelected) AccentCyan.copy(alpha = 0.2f) else Color(0xFF161820),
                                        border = BorderStroke(1.dp, if (isRadSelected) AccentCyan else SurfaceBorder)
                                    ) {
                                        Text(
                                            text = radLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isRadSelected) AccentCyan else ContentSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // GPS Coordinates Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "GPS: %.5f, %.5f".format(lat, lng),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = ContentSecondary
                                    )
                                }

                                FilledTonalButton(
                                    onClick = {
                                        try {
                                            val fused = LocationServices.getFusedLocationProviderClient(context)
                                            fused.lastLocation.addOnSuccessListener { loc: Location? ->
                                                if (loc != null) {
                                                    lat = loc.latitude
                                                    lng = loc.longitude
                                                }
                                            }
                                        } catch (_: SecurityException) {}
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = GlowEmerald,
                                        contentColor = AccentEmerald
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("GPS Actual", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 4. REGLAS DE CONMUTACIÓN AUTOMÁTICA
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "4. REGLAS AL ENTRAR Y SALIR",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Enter rules
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Al entrar: Apagar datos móviles", style = MaterialTheme.typography.bodyMedium, color = ContentPrimary)
                                    Text("0 megas gastados mientras estés en casa", style = MaterialTheme.typography.labelSmall, color = ContentTertiary)
                                }
                                Switch(
                                    checked = dataOffAtEntry,
                                    onCheckedChange = { dataOffAtEntry = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = AccentEmerald)
                                )
                            }

                            Divider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 8.dp))

                            // Exit rules
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Al salir: Encender datos móviles", style = MaterialTheme.typography.bodyMedium, color = ContentPrimary)
                                    Text("Recupera tus megas en la calle automáticamente", style = MaterialTheme.typography.labelSmall, color = ContentTertiary)
                                }
                                Switch(
                                    checked = dataOnAtExit,
                                    onCheckedChange = { dataOnAtExit = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = AccentCyan)
                                )
                            }

                            Divider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 8.dp))

                            // Buffer anti-pestañeo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Buffer anti-pestañeo: ${bufferSeconds}s", style = MaterialTheme.typography.bodyMedium, color = ContentPrimary)
                                    Text("Espera ${bufferSeconds}s si el router se apaga antes de gastar megas", style = MaterialTheme.typography.labelSmall, color = ContentTertiary)
                                }
                            }
                            Slider(
                                value = bufferSeconds.toFloat(),
                                onValueChange = { bufferSeconds = it.roundToInt() },
                                valueRange = 5f..60f,
                                steps = 11,
                                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                            )
                        }
                    }

                    // 5. NOTAS ADICIONALES
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Notas (Opcional: piso, detalles del router, etc.)") },
                        singleLine = false,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = ContentPrimary,
                            unfocusedTextColor = ContentPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose),
                        border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }

                    Button(
                        onClick = {
                            val updated = zone.copy(
                                name = zoneName.trim().ifBlank { "Mi Lugar" },
                                category = selectedCategory,
                                linkedWifiSsids = ssidInput.trim().ifEmpty { null },
                                wifiPassword = passwordInput.trim().ifEmpty { null },
                                notes = notesInput.trim().ifEmpty { null },
                                latitude = lat,
                                longitude = lng,
                                radiusMeters = radiusMeters,
                                dataOffAtEntry = dataOffAtEntry,
                                wifiOnAtEntry = wifiOnAtEntry,
                                dataOnAtExit = dataOnAtExit,
                                wifiOffAtExit = wifiOffAtExit,
                                disconnectBufferSeconds = bufferSeconds
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
                        modifier = Modifier.weight(1.6f)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar Cambios", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar zona?", color = ContentPrimary) },
            text = { Text("Se desvinculará el cerco de '${zone.name}' y dejará de conmutar automáticamente.", color = ContentSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete(zone)
                    onDismiss()
                }) {
                    Text("Eliminar definitivamente", color = AccentRose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = ContentSecondary)
                }
            },
            containerColor = SurfaceElevated
        )
    }
}
