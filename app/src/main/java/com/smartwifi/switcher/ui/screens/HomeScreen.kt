package com.smartwifi.switcher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartwifi.switcher.data.GeoZone
import com.smartwifi.switcher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    zones: List<GeoZone>,
    isServiceActive: Boolean,
    isShizukuActive: Boolean,
    onToggleService: (Boolean) -> Unit,
    onToggleZone: (GeoZone, Boolean) -> Unit,
    onDeleteZone: (GeoZone) -> Unit,
    onAddZoneClick: () -> Unit,
    onRequestShizukuPermission: () -> Unit
) {
    var showXiaomiGuide by remember { mutableStateOf(false) }

    if (showXiaomiGuide) {
        XiaomiGuideDialog(onDismiss = { showXiaomiGuide = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.WifiTethering,
                            contentDescription = null,
                            tint = PrimaryCyan
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Smart Wifi Switch",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showXiaomiGuide = true }) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = "Guía Xiaomi",
                            tint = PrimaryCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextLight
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddZoneClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddLocationAlt, contentDescription = null) },
                text = { Text("Nueva Zona") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Service & Status Card
            item {
                StatusCard(
                    isServiceActive = isServiceActive,
                    isShizukuActive = isShizukuActive,
                    onToggleService = onToggleService,
                    onRequestShizuku = onRequestShizukuPermission,
                    onOpenGuide = { showXiaomiGuide = true }
                )
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Zonas Guardadas",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight
                    )
                    Text(
                        text = "${zones.size} configuradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }

            // Empty State
            if (zones.isEmpty()) {
                item {
                    EmptyZonesCard(onAddClick = onAddZoneClick)
                }
            } else {
                items(zones, key = { it.id }) { zone ->
                    ZoneItemCard(
                        zone = zone,
                        onToggle = { isEnabled -> onToggleZone(zone, isEnabled) },
                        onDelete = { onDeleteZone(zone) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun StatusCard(
    isServiceActive: Boolean,
    isShizukuActive: Boolean,
    onToggleService: (Boolean) -> Unit,
    onRequestShizuku: () -> Unit,
    onOpenGuide: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Service Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isServiceActive) "Protección Activa" else "Protección Pausada",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isServiceActive) AccentGreen else AccentRed
                    )
                    Text(
                        text = if (isServiceActive) "Detectando entradas y salidas en segundo plano" else "El monitoreo automático está apagado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = isServiceActive,
                    onCheckedChange = onToggleService,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryBlue,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = SurfaceDark)
            Spacer(modifier = Modifier.height(14.dp))

            // Shizuku Automation Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isShizukuActive) AccentGreen.copy(alpha = 0.2f) else AccentOrange.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isShizukuActive) Icons.Default.Bolt else Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = if (isShizukuActive) AccentGreen else AccentOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isShizukuActive) "Control de Megas: Automático" else "Control de Megas: Manual",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextLight
                        )
                        Text(
                            text = if (isShizukuActive) "Shizuku Conectado" else "Requiere permiso Shizuku o Root",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                if (!isShizukuActive) {
                    Button(
                        onClick = onRequestShizuku,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Activar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            TextButton(
                onClick = onOpenGuide,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ver guía para Redmi Note 11 (MIUI/HyperOS)", color = PrimaryCyan, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ZoneItemCard(
    zone: GeoZone,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = zone.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (zone.isEnabled) TextLight else TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PrimaryBlue.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Radio: ${zone.radiusMeters.roundToInt()}m",
                                color = PrimaryCyan,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = zone.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryBlue,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceDark
                        )
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = AccentRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = SurfaceDark)
            Spacer(modifier = Modifier.height(8.dp))

            // Action summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Al entrar a casa:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "📶 Wi-Fi ON • 📱 Datos OFF",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text("Al salir de casa:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "📶 Wi-Fi OFF • 📱 Datos ON",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryCyan,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (zone.lastTriggeredAt > 0L) {
                Spacer(modifier = Modifier.height(8.dp))
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(zone.lastTriggeredAt))
                Text(
                    text = "Última activación: $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun EmptyZonesCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ShareLocation,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sin zonas configuradas",
                style = MaterialTheme.typography.titleMedium,
                color = TextLight
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Agrega tu casa y define un radio en metros para que la app apague los datos móviles y prenda tu Wi-Fi automáticamente cuando llegues.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Marcar Mi Casa en el Mapa")
            }
        }
    }
}
