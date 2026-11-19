package com.smartwifi.switcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartwifi.switcher.data.ActivityLog
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
    recentLogs: List<ActivityLog>,
    isServiceActive: Boolean,
    isShizukuActive: Boolean,
    onToggleService: (Boolean) -> Unit,
    onToggleZone: (GeoZone, Boolean) -> Unit,
    onDeleteZone: (GeoZone) -> Unit,
    onAddZoneClick: () -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onClearLogs: () -> Unit,
    onTestData: (Boolean) -> Boolean,
    onTestWifi: (Boolean) -> Boolean
) {
    var showXiaomiGuide by remember { mutableStateOf(false) }
    var showTestBench by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }

    if (showXiaomiGuide) {
        XiaomiGuideDialog(onDismiss = { showXiaomiGuide = false })
    }

    if (showTestBench) {
        TestBenchDialog(
            onDismiss = { showTestBench = false },
            onTestData = onTestData,
            onTestWifi = onTestWifi
        )
    }

    if (showLogsDialog) {
        ActivityLogDialog(
            logs = recentLogs,
            onDismiss = { showLogsDialog = false },
            onClearLogs = onClearLogs
        )
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlowBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.WifiProtectedSetup,
                                contentDescription = null,
                                tint = AccentBlueGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Smart Wifi Switch",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ContentPrimary
                            )
                            Text(
                                text = "Control autónomo de datos",
                                style = MaterialTheme.typography.labelSmall,
                                color = ContentTertiary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showXiaomiGuide = true }) {
                        Icon(
                            Icons.Outlined.HelpOutline,
                            contentDescription = "Configuración Xiaomi",
                            tint = ContentSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddZoneClick,
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nueva zona", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // System State Hero
            item {
                SystemHeroCard(
                    isServiceActive = isServiceActive,
                    isShizukuActive = isShizukuActive,
                    onToggleService = onToggleService,
                    onRequestShizuku = onRequestShizukuPermission,
                    onOpenTest = { showTestBench = true },
                    onOpenLogs = { showLogsDialog = true },
                    onOpenGuide = { showXiaomiGuide = true }
                )
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ZONAS CONFIGURADAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                    Text(
                        text = "${zones.size} total",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                }
            }

            // Zone List or Empty State
            if (zones.isEmpty()) {
                item {
                    EmptyStateCard(onAddClick = onAddZoneClick)
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
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

@Composable
private fun SystemHeroCard(
    isServiceActive: Boolean,
    isShizukuActive: Boolean,
    onToggleService: (Boolean) -> Unit,
    onRequestShizuku: () -> Unit,
    onOpenTest: () -> Unit,
    onOpenLogs: () -> Unit,
    onOpenGuide: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Status Pill & Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isServiceActive) AccentEmerald else AccentRose)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isServiceActive) "SISTEMA ACTIVO" else "SISTEMA EN PAUSA",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isServiceActive) AccentEmerald else AccentRose
                    )
                }

                Switch(
                    checked = isServiceActive,
                    onCheckedChange = onToggleService,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentBlue,
                        uncheckedThumbColor = ContentSecondary,
                        uncheckedTrackColor = SurfaceCard,
                        uncheckedBorderColor = SurfaceBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isServiceActive) "Protección de datos en ejecución" else "Monitoreo automático detenido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ContentPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Alterna Wi-Fi y datos móviles por proximidad de red y geocercas en segundo plano sin drenaje de batería.",
                style = MaterialTheme.typography.bodyMedium,
                color = ContentSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = SurfaceBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Sub-status Row: Shizuku / Engine Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isShizukuActive) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = if (isShizukuActive) AccentEmerald else AccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isShizukuActive) "Control de red automático" else "Requiere enlace Shizuku",
                            style = MaterialTheme.typography.labelLarge,
                            color = ContentPrimary
                        )
                        Text(
                            text = if (isShizukuActive) "Privilegios de sistema autorizados" else "Toca para conectar el servicio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ContentTertiary
                        )
                    }
                }

                if (!isShizukuActive) {
                    FilledTonalButton(
                        onClick = onRequestShizuku,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AccentAmber.copy(alpha = 0.15f),
                            contentColor = AccentAmber
                        )
                    ) {
                        Text("Vincular", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Utility Toolbar: Diagnostic, Logs, System Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    icon = Icons.Outlined.Speed,
                    label = "Diagnóstico",
                    onClick = onOpenTest,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    icon = Icons.Outlined.FormatListBulleted,
                    label = "Registro",
                    onClick = onOpenLogs,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    icon = Icons.Outlined.SettingsSuggest,
                    label = "Ajustes Xiaomi",
                    onClick = onOpenGuide,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(8.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = ContentSecondary, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = ContentPrimary)
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
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Zone Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (zone.isEnabled) AccentEmerald else ContentTertiary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (zone.isEnabled) ContentPrimary else ContentTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Meta Tags Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetaTag(text = "${zone.radiusMeters.roundToInt()} m")
                        MetaTag(text = "Buffer ${zone.disconnectBufferSeconds}s")
                        if (zone.batteryMode == "WIFI_ONLY") {
                            MetaTag(text = "Solo Wi-Fi")
                        } else {
                            MetaTag(text = "Híbrido")
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = zone.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentBlue,
                            uncheckedThumbColor = ContentSecondary,
                            uncheckedTrackColor = SurfaceElevated,
                            uncheckedBorderColor = SurfaceBorder
                        )
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Eliminar zona",
                            tint = ContentTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Linked Wi-Fi Networks
            val ssids = zone.getSsidList()
            if (ssids.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Wifi,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ssids.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ContentSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = SurfaceBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Rules Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AL INGRESAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Wi-Fi On • Datos Off",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (zone.isEnabled) AccentEmerald else ContentTertiary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "AL SALIR",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Wi-Fi Off • Datos On",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (zone.isEnabled) AccentCyan else ContentTertiary
                    )
                }
            }

            if (zone.lastTriggeredAt > 0L) {
                Spacer(modifier = Modifier.height(8.dp))
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(zone.lastTriggeredAt))
                Text(
                    text = "Última transición: $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContentTertiary
                )
            }
        }
    }
}

@Composable
private fun MetaTag(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = SurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = ContentSecondary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun EmptyStateCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Sensors,
                    contentDescription = null,
                    tint = ContentSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin zonas configuradas",
                style = MaterialTheme.typography.titleMedium,
                color = ContentPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Define el perímetro de tu hogar y vincula tus redes Wi-Fi para iniciar la automatización sin intervención manual.",
                style = MaterialTheme.typography.bodyMedium,
                color = ContentSecondary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Definir primera zona", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
