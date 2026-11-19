package com.smartwifi.switcher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartwifi.switcher.data.ActivityLog
import com.smartwifi.switcher.data.GeoZone
import com.smartwifi.switcher.service.NetworkSummary
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
    networkSummary: NetworkSummary,
    isServiceActive: Boolean,
    isShizukuActive: Boolean,
    onToggleService: (Boolean) -> Unit,
    onToggleZone: (GeoZone, Boolean) -> Unit,
    onDeleteZone: (GeoZone) -> Unit,
    onUpdateZone: (GeoZone) -> Unit = {},
    onQuickAddZone: (name: String, ssid: String?, password: String?, category: String, notes: String?, lat: Double, lng: Double, radius: Float) -> Unit,
    onOpenMapPicker: () -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onClearLogs: () -> Unit,
    onTestData: (Boolean) -> Boolean,
    onTestWifi: (Boolean) -> Boolean
) {
    var showXiaomiGuide by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf(false) }
    var showTestBench by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var quickAddInitialPreset by remember { mutableStateOf("Casa") }
    var selectedZoneForQr by remember { mutableStateOf<GeoZone?>(null) }
    var editingZone by remember { mutableStateOf<GeoZone?>(null) }

    // Dialogs
    if (editingZone != null) {
        EditZoneDialog(
            zone = editingZone!!,
            onDismiss = { editingZone = null },
            onSave = { updated ->
                onUpdateZone(updated)
                editingZone = null
            },
            onDelete = { toDelete ->
                onDeleteZone(toDelete)
                editingZone = null
            }
        )
    }

    if (showXiaomiGuide) {
        XiaomiGuideDialog(onDismiss = { showXiaomiGuide = false })
    }

    if (showLegalDialog) {
        LegalNoticeDialog(onDismiss = { showLegalDialog = false })
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

    if (selectedZoneForQr != null) {
        val z = selectedZoneForQr!!
        val ssid = z.getSsidList().firstOrNull() ?: z.linkedWifiSsids ?: z.name
        WifiQrDialog(
            placeName = z.name,
            ssid = ssid,
            password = z.wifiPassword,
            notes = z.notes,
            onDismiss = { selectedZoneForQr = null }
        )
    }

    if (showQuickAddDialog) {
        QuickAddZoneDialog(
            initialPreset = quickAddInitialPreset,
            onDismiss = { showQuickAddDialog = false },
            onSave = { name, ssid, password, category, notes, lat, lng, radius ->
                onQuickAddZone(name, ssid, password, category, notes, lat, lng, radius)
                showQuickAddDialog = false
            },
            onOpenMapPicker = {
                showQuickAddDialog = false
                onOpenMapPicker()
            }
        )
    }

    // Active zone detection based on current SSID
    val matchedZone = remember(networkSummary.currentSsid, zones) {
        if (!networkSummary.currentSsid.isNullOrBlank()) {
            zones.find { it.matchesSsid(networkSummary.currentSsid) }
        } else {
            null
        }
    }

    val isUnregisteredSsid = remember(networkSummary.currentSsid, zones) {
        val ssid = networkSummary.currentSsid
        !ssid.isNullOrBlank() && zones.none { it.matchesSsid(ssid) }
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Smart Wifi Switch",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ContentPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLegalDialog = true }) {
                        Icon(
                            Icons.Outlined.Gavel,
                            contentDescription = "Avisos Legales",
                            tint = ContentSecondary
                        )
                    }
                    IconButton(onClick = { showXiaomiGuide = true }) {
                        Icon(
                            Icons.Outlined.HelpOutline,
                            contentDescription = "Guía Xiaomi",
                            tint = ContentSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        },
        bottomBar = {
            WolowBottomNav(
                onOpenZones = { /* already in home */ },
                onOpenDiagnostic = { showTestBench = true },
                onOpenRadarMap = onOpenMapPicker,
                onOpenLogs = { showLogsDialog = true }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Central Hero Power Button (Wolow Aesthetic)
            item {
                WolowPowerRingHero(
                    isActive = isServiceActive,
                    isShizukuActive = isShizukuActive,
                    onToggle = onToggleService,
                    onRequestShizuku = onRequestShizukuPermission
                )
            }

            // 2. Selected Zone / Location Card (Wolow Selected Device Style)
            item {
                ActiveLocationCard(
                    matchedZone = matchedZone,
                    networkSummary = networkSummary,
                    isUnregisteredSsid = isUnregisteredSsid,
                    onQuickAddCurrentSsid = {
                        quickAddInitialPreset = "Casa Juan"
                        showQuickAddDialog = true
                    }
                )
            }

            // 3. Network Information Card (Wolow 4-quadrant layout with status badge)
            item {
                WolowNetworkInfoCard(
                    summary = networkSummary,
                    matchedZone = matchedZone
                )
            }

            // 3b. Radar Geográfico Map Launcher Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlowCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Map,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Radar de Zonas en Vivo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ContentPrimary
                                )
                                Text(
                                    text = "Mapa Dark Matter • ${zones.size} perímetros activos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ContentTertiary
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onOpenMapPicker,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AccentCyan.copy(alpha = 0.15f),
                                contentColor = AccentCyan
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Abrir Radar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Quick Add Presets Bar ("Casa", "Trabajo", "Casa Juan", "Mapa")
            item {
                QuickPresetsBar(
                    onSelectPreset = { preset ->
                        quickAddInitialPreset = preset
                        showQuickAddDialog = true
                    },
                    onOpenMap = onOpenMapPicker
                )
            }

            // 4b. Interactive How-It-Works Guide Card
            item {
                HowItWorksGuideCard(onOpenMap = onOpenMapPicker)
            }

            // 5. Configured Zones Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ZONAS CONFIGURADAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${zones.size} guardadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                }
            }

            // 6. Zone List
            if (zones.isEmpty()) {
                item {
                    WolowEmptyStateCard(
                        onAddClick = {
                            quickAddInitialPreset = "Casa"
                            showQuickAddDialog = true
                        }
                    )
                }
            } else {
                items(zones, key = { it.id }) { zone ->
                    val isCurrentlyHere = matchedZone?.id == zone.id
                    ZoneItemCard(
                        zone = zone,
                        isCurrentlyHere = isCurrentlyHere,
                        onToggle = { isEnabled -> onToggleZone(zone, isEnabled) },
                        onEdit = { editingZone = zone },
                        onDelete = { onDeleteZone(zone) },
                        onOpenQr = { selectedZoneForQr = zone }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// 1. WOLOW POWER RING HERO
// -------------------------------------------------------------
@Composable
private fun WolowPowerRingHero(
    isActive: Boolean,
    isShizukuActive: Boolean,
    onToggle: (Boolean) -> Unit,
    onRequestShizuku: () -> Unit
) {
    val ringColor = if (isActive) AccentCyan else ContentTertiary
    val glowColor = if (isActive) GlowCyan else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Concentric Ring Power Button
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color(0xFF0D0E12))
                .border(2.dp, Color(0xFF1E2028), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Outer subtle glow halo
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .clip(CircleShape)
                    .background(glowColor)
                    .border(
                        BorderStroke(
                            2.5.dp,
                            Brush.sweepGradient(
                                listOf(
                                    if (isActive) AccentCyan else ContentDisabled,
                                    if (isActive) AccentBlue else ContentTertiary,
                                    if (isActive) AccentCyan else ContentDisabled
                                )
                            )
                        ),
                        CircleShape
                    )
                    .clickable { onToggle(!isActive) },
                contentAlignment = Alignment.Center
            ) {
                // Inner button surface
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF090A0D)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PowerSettingsNew,
                        contentDescription = "Encendido/Pausa",
                        tint = if (isActive) AccentCyan else ContentTertiary,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status text below ring
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) AccentEmerald else AccentRose)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isActive) "ESCUDO DE MEGAS ACTIVO" else "PROTECCIÓN EN PAUSA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isActive) AccentEmerald else AccentRose,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isActive) "Apagando megas al entrar a tus Wi-Fi conocidos" else "Toca el botón central para reanudar el ahorro automático",
            style = MaterialTheme.typography.bodySmall,
            color = ContentTertiary
        )

        // Shizuku warning banner if not active
        if (!isShizukuActive) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                onClick = onRequestShizuku,
                shape = RoundedCornerShape(10.dp),
                color = AmberPillBg,
                border = BorderStroke(1.dp, AmberPillBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = AmberPillText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Requiere vincular Shizuku para apagar datos",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberPillText
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. ACTIVE LOCATION / DEVICE CARD (Wolow Selected Device Style)
// -------------------------------------------------------------
@Composable
private fun ActiveLocationCard(
    matchedZone: GeoZone?,
    networkSummary: NetworkSummary,
    isUnregisteredSsid: Boolean,
    onQuickAddCurrentSsid: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (matchedZone != null) GlowEmerald else SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (matchedZone != null) Icons.Outlined.LocationOn else Icons.Outlined.Sensors,
                        contentDescription = null,
                        tint = if (matchedZone != null) AccentEmerald else ContentSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "UBICACIÓN DETECTADA",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            matchedZone != null -> "${matchedZone.name} (Protegido)"
                            isUnregisteredSsid -> "Red nueva: ${networkSummary.currentSsid}"
                            networkSummary.isWifiConnected -> "Wi-Fi no registrado"
                            networkSummary.isMobileDataActive -> "En la calle (Datos móviles)"
                            else -> "Sin conexión activa"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ContentPrimary
                    )
                }
            }

            if (isUnregisteredSsid) {
                FilledTonalButton(
                    onClick = onQuickAddCurrentSsid,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AccentCyan.copy(alpha = 0.15f),
                        contentColor = AccentCyan
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("+ Guardar", style = MaterialTheme.typography.labelSmall)
                }
            } else if (matchedZone != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GreenPillBg,
                    border = BorderStroke(1.dp, GreenPillBorder)
                ) {
                    Text(
                        text = "EN ZONA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenPillText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. WOLOW NETWORK INFORMATION CARD
// -------------------------------------------------------------
@Composable
private fun WolowNetworkInfoCard(
    summary: NetworkSummary,
    matchedZone: GeoZone?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Network info label & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Hub,
                        contentDescription = null,
                        tint = ContentSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Network information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ContentPrimary
                    )
                }

                // Green/Amber status pill (identical to Wolow "Domain resolves")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (matchedZone != null || summary.isWifiConnected) GreenPillBg else AmberPillBg,
                    border = BorderStroke(1.dp, if (matchedZone != null || summary.isWifiConnected) GreenPillBorder else AmberPillBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (matchedZone != null || summary.isWifiConnected) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (matchedZone != null || summary.isWifiConnected) GreenPillText else AmberPillText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (matchedZone != null) "Megas Protegidos" else if (summary.isWifiConnected) "Wi-Fi Conectado" else "Datos Móviles",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (matchedZone != null || summary.isWifiConnected) GreenPillText else AmberPillText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2x2 Grid of Network Metrics (Wolow style: Source IP, Target address, Subnet, Status)
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Red Wi-Fi", style = MaterialTheme.typography.labelSmall, color = ContentTertiary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = summary.currentSsid ?: "Desconectado",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = ContentPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Datos Móviles", style = MaterialTheme.typography.labelSmall, color = ContentTertiary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (matchedZone != null || !summary.isMobileDataActive) "APAGADO (0 MB)" else "ENCENDIDO",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (matchedZone != null || !summary.isMobileDataActive) AccentEmerald else AccentAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Source IP", style = MaterialTheme.typography.labelSmall, color = ContentTertiary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = summary.localIp,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = ContentPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Subnet mask", style = MaterialTheme.typography.labelSmall, color = ContentTertiary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = summary.subnetMask,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = ContentPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. QUICK PRESETS BAR (Casa, Trabajo, Casa Juan, Mapa)
// -------------------------------------------------------------
@Composable
private fun QuickPresetsBar(
    onSelectPreset: (String) -> Unit,
    onOpenMap: () -> Unit
) {
    Column {
        Text(
            text = "AGREGAR ZONA RÁPIDA",
            style = MaterialTheme.typography.labelSmall,
            color = ContentTertiary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickPresetButton(
                icon = Icons.Outlined.Home,
                label = "+ Casa",
                onClick = { onSelectPreset("Casa") },
                modifier = Modifier.weight(1f)
            )
            QuickPresetButton(
                icon = Icons.Outlined.Work,
                label = "+ Trabajo",
                onClick = { onSelectPreset("Trabajo") },
                modifier = Modifier.weight(1f)
            )
            QuickPresetButton(
                icon = Icons.Outlined.Person,
                label = "+ Juan",
                onClick = { onSelectPreset("Casa Juan") },
                modifier = Modifier.weight(1f)
            )
            QuickPresetButton(
                icon = Icons.Outlined.Map,
                label = "Mapa",
                onClick = onOpenMap,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickPresetButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceElevated,
        border = BorderStroke(1.dp, SurfaceBorder),
        modifier = modifier.height(42.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = ContentPrimary
            )
        }
    }
}

// -------------------------------------------------------------
// 5. ZONE ITEM CARD
// -------------------------------------------------------------
@Composable
private fun ZoneItemCard(
    zone: GeoZone,
    isCurrentlyHere: Boolean,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenQr: () -> Unit
) {
    val zoneIcon = when (zone.category) {
        "HOME" -> Icons.Outlined.Home
        "WORK" -> Icons.Outlined.Work
        "FRIEND" -> Icons.Outlined.Person
        "SCHOOL" -> Icons.Outlined.School
        "GYM" -> Icons.Outlined.FitnessCenter
        "CAFE" -> Icons.Outlined.LocalCafe
        else -> when {
            zone.name.contains("casa", ignoreCase = true) && !zone.name.contains("juan", ignoreCase = true) -> Icons.Outlined.Home
            zone.name.contains("trabajo", ignoreCase = true) || zone.name.contains("oficina", ignoreCase = true) -> Icons.Outlined.Work
            zone.name.contains("juan", ignoreCase = true) || zone.name.contains("amigo", ignoreCase = true) -> Icons.Outlined.Person
            else -> Icons.Outlined.Place
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isCurrentlyHere) AccentEmerald.copy(alpha = 0.5f) else SurfaceBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyHere) Color(0xFF101915) else SurfaceCard
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Zone Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurrentlyHere) GlowEmerald else SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            zoneIcon,
                            contentDescription = null,
                            tint = if (isCurrentlyHere) AccentEmerald else AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = zone.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (zone.isEnabled) ContentPrimary else ContentTertiary
                            )
                            if (isCurrentlyHere) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GreenPillBg,
                                    border = BorderStroke(1.dp, GreenPillBorder)
                                ) {
                                    Text(
                                        text = "AQUÍ AHORA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPillText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Radio ${zone.radiusMeters.roundToInt()}m • Buffer anti-pestañeo ${zone.disconnectBufferSeconds}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = ContentTertiary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = zone.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentCyan,
                            uncheckedThumbColor = ContentSecondary,
                            uncheckedTrackColor = SurfaceElevated,
                            uncheckedBorderColor = SurfaceBorder
                        )
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = ContentTertiary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Linked SSIDs
            val ssids = zone.getSsidList()
            if (ssids.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Wifi, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ssids.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ContentSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = SurfaceBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Rules Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("AL INGRESAR", style = MaterialTheme.typography.labelSmall, color = ContentTertiary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Wi-Fi On • Megas Off", style = MaterialTheme.typography.labelMedium, color = AccentEmerald)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("AL SALIR", style = MaterialTheme.typography.labelSmall, color = ContentTertiary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Wi-Fi Off • Megas On", style = MaterialTheme.typography.labelMedium, color = AccentCyan)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = SurfaceBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Password and QR Sharing Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Key,
                        contentDescription = null,
                        tint = if (!zone.wifiPassword.isNullOrBlank()) AccentEmerald else ContentTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (!zone.wifiPassword.isNullOrBlank()) "Clave: ••••••••" else "Sin clave guardada",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (!zone.wifiPassword.isNullOrBlank()) ContentPrimary else ContentTertiary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1B2230),
                            contentColor = AccentCyan
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = onOpenQr,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AccentCyan.copy(alpha = 0.15f),
                            contentColor = AccentCyan
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("QR / Clave", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. EMPTY STATE CARD
// -------------------------------------------------------------
@Composable
private fun WolowEmptyStateCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
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
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Sensors,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin zonas configuradas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ContentPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Agrega tu casa, trabajo o casa de Juan para que tus datos móviles se apaguen solos y nunca gastes megas por descuido.",
                style = MaterialTheme.typography.bodyMedium,
                color = ContentSecondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear primera zona", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 7. BOTTOM NAVIGATION (Wolow style: Menu / Shield / Guide)
// -------------------------------------------------------------
@Composable
private fun WolowBottomNav(
    onOpenZones: () -> Unit,
    onOpenDiagnostic: () -> Unit,
    onOpenRadarMap: () -> Unit,
    onOpenLogs: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF090A0D),
        border = BorderStroke(1.dp, Color(0xFF1A1C24))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Outlined.Menu,
                label = "Diagnóstico",
                onClick = onOpenDiagnostic
            )
            BottomNavItem(
                icon = Icons.Filled.PowerSettingsNew,
                label = "Escudo",
                isSelected = true,
                onClick = onOpenZones
            )
            BottomNavItem(
                icon = Icons.Filled.Map,
                label = "Radar Mapa",
                onClick = onOpenRadarMap
            )
            BottomNavItem(
                icon = Icons.Outlined.FormatListBulleted,
                label = "Registro",
                onClick = onOpenLogs
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) AccentCyan else ContentTertiary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) AccentCyan else ContentTertiary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// -------------------------------------------------------------
// HOW IT WORKS GUIDE CARD
// -------------------------------------------------------------
@Composable
private fun HowItWorksGuideCard(onOpenMap: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1218)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GlowCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.TipsAndUpdates, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "¿Cómo funciona el cambio automático?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ContentPrimary
                        )
                        Text(
                            text = if (isExpanded) "Toca para plegar" else "Aprende a configurar nombres, redes y cercos",
                            style = MaterialTheme.typography.labelSmall,
                            color = ContentTertiary
                        )
                    }
                }

                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = ContentSecondary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Divider(color = SurfaceBorder, thickness = 1.dp)

                    // Step 1
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            shape = CircleShape,
                            color = AccentCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("1", color = AccentCyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Cambiar Nombres y Categorías", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ContentPrimary)
                            Text("Toca cualquier tarjeta o el botón 'Editar' para cambiar el nombre ('Casa', 'Trabajo', 'Casa Juan') y su icono.", style = MaterialTheme.typography.bodySmall, color = ContentSecondary)
                        }
                    }

                    // Step 2
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            shape = CircleShape,
                            color = AccentCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("2", color = AccentCyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Vincular Redes Wi-Fi (SSIDs)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ContentPrimary)
                            Text("Asocia el nombre del Wi-Fi de ese lugar (o escanea las redes cercanas). Puedes guardar la contraseña para compartirla por código QR.", style = MaterialTheme.typography.bodySmall, color = ContentSecondary)
                        }
                    }

                    // Step 3
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            shape = CircleShape,
                            color = AccentCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("3", color = AccentCyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Cercos Geográficos en el Mapa Radar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ContentPrimary)
                            Text("Ajusta el radio (50m a 500m). Al entrar al cerco o al Wi-Fi, la app apaga tus datos móviles (0 megas gastados). Al salir, los enciende automáticamente tras 20 segundos.", style = MaterialTheme.typography.bodySmall, color = ContentSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = onOpenMap,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Map, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver Zonas en el Radar de Mapas", color = AccentCyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
