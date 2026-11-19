package com.smartwifi.switcher.ui.screens

import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.location.Location
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationServices
import com.smartwifi.switcher.data.GeoZone
import com.smartwifi.switcher.service.WifiHelper
import com.smartwifi.switcher.ui.theme.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.math.roundToInt

enum class MapTileTheme {
    DARK_OLED,
    VOYAGER,
    STANDARD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarMapScreen(
    zones: List<GeoZone>,
    activeSsid: String?,
    onNavigateBack: () -> Unit,
    onSaveZone: (GeoZone) -> Unit,
    onDeleteZone: (GeoZone) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTileTheme by remember { mutableStateOf(MapTileTheme.DARK_OLED) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Zone creation/editing state
    var isCreatingZone by remember { mutableStateOf(false) }
    var newPinLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var newZoneName by remember { mutableStateOf("Casa") }
    var newRadiusMeters by remember { mutableFloatStateOf(120f) }
    var newSsidInput by remember { mutableStateOf(activeSsid ?: "") }

    // Selected zone inspector
    var selectedZoneForInspect by remember { mutableStateOf<GeoZone?>(null) }
    var showQrZone by remember { mutableStateOf<GeoZone?>(null) }

    if (showQrZone != null) {
        val z = showQrZone!!
        val ssid = z.getSsidList().firstOrNull() ?: z.linkedWifiSsids ?: z.name
        WifiQrDialog(
            placeName = z.name,
            ssid = ssid,
            password = z.wifiPassword,
            notes = z.notes,
            onDismiss = { showQrZone = null }
        )
    }

    // Overlays list references
    val zoneOverlays = remember { mutableListOf<Polygon>() }
    val zoneMarkers = remember { mutableListOf<Marker>() }
    var createPolygonRef by remember { mutableStateOf<Polygon?>(null) }
    var createMarkerRef by remember { mutableStateOf<Marker?>(null) }
    var userMarkerRef by remember { mutableStateOf<Marker?>(null) }

    // Fetch initial user GPS
    LaunchedEffect(Unit) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    val p = GeoPoint(loc.latitude, loc.longitude)
                    userLocation = p
                    if (!isCreatingZone) {
                        mapViewRef?.controller?.setCenter(p)
                    }
                }
            }
        } catch (_: SecurityException) {}
    }

    // Function to render all saved zones on the map
    fun renderAllZones(mapView: MapView) {
        // Clear previous overlays
        zoneOverlays.forEach { mapView.overlays.remove(it) }
        zoneOverlays.clear()
        zoneMarkers.forEach { mapView.overlays.remove(it) }
        zoneMarkers.clear()

        // Render each zone as glowing radar perimeter
        zones.forEach { zone ->
            val center = GeoPoint(zone.latitude, zone.longitude)
            val isCurrentZone = activeSsid != null && zone.matchesSsid(activeSsid)

            val circlePoints = Polygon.pointsAsCircle(center, zone.radiusMeters.toDouble())
            val polygon = Polygon(mapView).apply {
                points = circlePoints
                if (isCurrentZone) {
                    // Glowing Emerald for the currently connected zone
                    fillPaint.color = AndroidColor.argb(55, 16, 185, 129)
                    fillPaint.style = Paint.Style.FILL
                    outlinePaint.color = AndroidColor.rgb(16, 185, 129)
                    outlinePaint.strokeWidth = 4.5f
                } else {
                    // Deep Cyan/Cobalt for standby zones
                    fillPaint.color = AndroidColor.argb(40, 0, 210, 255)
                    fillPaint.style = Paint.Style.FILL
                    outlinePaint.color = AndroidColor.rgb(0, 210, 255)
                    outlinePaint.strokeWidth = 3f
                }
                title = zone.name
            }
            mapView.overlays.add(polygon)
            zoneOverlays.add(polygon)

            val marker = Marker(mapView).apply {
                position = center
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = zone.name
                subDescription = if (isCurrentZone) "📍 Conectado ahora (Megas apagados)" else "Wi-Fi: ${zone.linkedWifiSsids ?: "GPS"}"
                setOnMarkerClickListener { _, _ ->
                    selectedZoneForInspect = zone
                    mapView.controller.animateTo(center)
                    true
                }
            }
            mapView.overlays.add(marker)
            zoneMarkers.add(marker)
        }

        // Render user location beacon
        userMarkerRef?.let { mapView.overlays.remove(it) }
        userLocation?.let { loc ->
            val uMarker = Marker(mapView).apply {
                position = loc
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                title = "Tu ubicación actual"
            }
            mapView.overlays.add(uMarker)
            userMarkerRef = uMarker
        }

        mapView.invalidate()
    }

    // Function to render the new zone in creation mode
    fun renderCreationPin(mapView: MapView, point: GeoPoint, radius: Float) {
        createPolygonRef?.let { mapView.overlays.remove(it) }
        createMarkerRef?.let { mapView.overlays.remove(it) }

        val circlePoints = Polygon.pointsAsCircle(point, radius.toDouble())
        val polygon = Polygon(mapView).apply {
            points = circlePoints
            fillPaint.color = AndroidColor.argb(65, 0, 210, 255)
            fillPaint.style = Paint.Style.FILL
            outlinePaint.color = AndroidColor.rgb(0, 210, 255)
            outlinePaint.strokeWidth = 4f
        }
        mapView.overlays.add(polygon)
        createPolygonRef = polygon

        val marker = Marker(mapView).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = newZoneName
        }
        mapView.overlays.add(marker)
        createMarkerRef = marker

        mapView.invalidate()
    }

    // Lifecycle observer for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Update tile source when theme toggled
    LaunchedEffect(selectedTileTheme) {
        mapViewRef?.let { map ->
            when (selectedTileTheme) {
                MapTileTheme.DARK_OLED -> map.setTileSource(MapTileSources.CARTO_DARK)
                MapTileTheme.VOYAGER -> map.setTileSource(MapTileSources.CARTO_VOYAGER)
                MapTileTheme.STANDARD -> map.setTileSource(MapTileSources.OSM_STANDARD)
            }
            map.invalidate()
        }
    }

    // Update overlays when zones change or mode changes
    LaunchedEffect(zones, isCreatingZone, newPinLocation, newRadiusMeters) {
        mapViewRef?.let { map ->
            if (isCreatingZone && newPinLocation != null) {
                renderCreationPin(map, newPinLocation!!, newRadiusMeters)
            } else {
                createPolygonRef?.let { map.overlays.remove(it) }
                createMarkerRef?.let { map.overlays.remove(it) }
                renderAllZones(map)
            }
        }
    }

    Scaffold(
        containerColor = AppBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. FULL SCREEN MAP VIEW
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(MapTileSources.CARTO_DARK)
                        setMultiTouchControls(true)
                        isTilesScaledToDpi = true
                        controller.setZoom(16.5)

                        val initialCenter = userLocation ?: GeoPoint(-17.3935, -66.1570)
                        controller.setCenter(initialCenter)

                        val receiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                if (isCreatingZone) {
                                    newPinLocation = p
                                    renderCreationPin(this@apply, p, newRadiusMeters)
                                } else {
                                    selectedZoneForInspect = null
                                }
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint): Boolean {
                                isCreatingZone = true
                                newPinLocation = p
                                renderCreationPin(this@apply, p, newRadiusMeters)
                                return true
                            }
                        }
                        overlays.add(0, MapEventsOverlay(receiver))

                        mapViewRef = this
                        renderAllZones(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. FLOATING TOP BAR (Glassmorphic)
            FloatingMapHeader(
                zoneCount = zones.size,
                selectedTileTheme = selectedTileTheme,
                onTileThemeChange = { selectedTileTheme = it },
                onNavigateBack = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // 3. FLOATING RIGHT UTILITY DOCK (Location, Zoom In, Zoom Out)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FloatingMapToolButton(
                    icon = Icons.Filled.MyLocation,
                    contentDescription = "Mi ubicación",
                    onClick = {
                        userLocation?.let { loc ->
                            mapViewRef?.controller?.animateTo(loc)
                        } ?: run {
                            Toast.makeText(context, "Buscando coordenadas GPS...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                FloatingMapToolButton(
                    icon = Icons.Filled.Add,
                    contentDescription = "Acercar",
                    onClick = { mapViewRef?.controller?.zoomIn() }
                )

                FloatingMapToolButton(
                    icon = Icons.Filled.Remove,
                    contentDescription = "Alejar",
                    onClick = { mapViewRef?.controller?.zoomOut() }
                )
            }

            // 4. FLOATING BOTTOM CONTENT
            // Case A: Creating a new zone
            AnimatedVisibility(
                visible = isCreatingZone,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                NewZoneCreationPanel(
                    pinLocation = newPinLocation,
                    name = newZoneName,
                    onNameChange = { newZoneName = it },
                    radiusMeters = newRadiusMeters,
                    onRadiusChange = { newRadiusMeters = it },
                    ssidInput = newSsidInput,
                    onSsidChange = { newSsidInput = it },
                    activeSsid = activeSsid,
                    onCancel = {
                        isCreatingZone = false
                        newPinLocation = null
                        mapViewRef?.let { renderAllZones(it) }
                    },
                    onSave = {
                        val pin = newPinLocation ?: userLocation ?: GeoPoint(-17.3935, -66.1570)
                        val newZone = GeoZone(
                            name = newZoneName.trim().ifBlank { "Zona Segura" },
                            latitude = pin.latitude,
                            longitude = pin.longitude,
                            radiusMeters = newRadiusMeters,
                            isEnabled = true,
                            wifiOnAtEntry = true,
                            dataOffAtEntry = true,
                            wifiOffAtExit = true,
                            dataOnAtExit = true,
                            linkedWifiSsids = newSsidInput.trim().ifEmpty { null },
                            batteryMode = "HYBRID",
                            disconnectBufferSeconds = 20,
                            vibrateOnAction = true
                        )
                        onSaveZone(newZone)
                        isCreatingZone = false
                        newPinLocation = null
                    }
                )
            }

            // Case B: Inspecting a selected zone
            AnimatedVisibility(
                visible = !isCreatingZone && selectedZoneForInspect != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                selectedZoneForInspect?.let { inspectedZone ->
                    ZoneInspectorCard(
                        zone = inspectedZone,
                        isCurrent = activeSsid != null && inspectedZone.matchesSsid(activeSsid),
                        onDismiss = { selectedZoneForInspect = null },
                        onDelete = {
                            onDeleteZone(inspectedZone)
                            selectedZoneForInspect = null
                        },
                        onOpenQr = {
                            showQrZone = inspectedZone
                        }
                    )
                }
            }

            // Case C: Standard Explorer Bottom Bar (Zones Carousel + Add Button)
            AnimatedVisibility(
                visible = !isCreatingZone && selectedZoneForInspect == null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                RadarZonesCarousel(
                    zones = zones,
                    activeSsid = activeSsid,
                    onSelectZone = { zone ->
                        selectedZoneForInspect = zone
                        mapViewRef?.controller?.animateTo(GeoPoint(zone.latitude, zone.longitude))
                    },
                    onStartCreateZone = {
                        isCreatingZone = true
                        val center = userLocation ?: GeoPoint(-17.3935, -66.1570)
                        newPinLocation = center
                        mapViewRef?.let { renderCreationPin(it, center, newRadiusMeters) }
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// FLOATING HEADER
// -------------------------------------------------------------
@Composable
private fun FloatingMapHeader(
    zoneCount: Int,
    selectedTileTheme: MapTileTheme,
    onTileThemeChange: (MapTileTheme) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xDD0C0E14),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar", tint = ContentPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Radar de Zonas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ContentPrimary
                    )
                    Text(
                        text = "$zoneCount perímetros activos",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan
                    )
                }
            }

            // Theme Switcher Pills (Dark OLED vs Voyager)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ThemePill(
                    label = "OLED",
                    isSelected = selectedTileTheme == MapTileTheme.DARK_OLED,
                    onClick = { onTileThemeChange(MapTileTheme.DARK_OLED) }
                )
                ThemePill(
                    label = "Voyager",
                    isSelected = selectedTileTheme == MapTileTheme.VOYAGER,
                    onClick = { onTileThemeChange(MapTileTheme.VOYAGER) }
                )
            }
        }
    }
}

@Composable
private fun ThemePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else Color(0xFF161820),
        border = BorderStroke(1.dp, if (isSelected) AccentCyan else SurfaceBorder)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AccentCyan else ContentSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

// -------------------------------------------------------------
// FLOATING TOOL BUTTON
// -------------------------------------------------------------
@Composable
private fun FloatingMapToolButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xDD12141C),
        border = BorderStroke(1.dp, SurfaceBorderLight),
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = AccentCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// ZONES CAROUSEL (EXPLORER MODE)
// -------------------------------------------------------------
@Composable
private fun RadarZonesCarousel(
    zones: List<GeoZone>,
    activeSsid: String?,
    onSelectZone: (GeoZone) -> Unit,
    onStartCreateZone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (zones.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(zones, key = { it.id }) { zone ->
                    val isHere = activeSsid != null && zone.matchesSsid(activeSsid)
                    Surface(
                        onClick = { onSelectZone(zone) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xEE12141A),
                        border = BorderStroke(1.dp, if (isHere) AccentEmerald else SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isHere) AccentEmerald else AccentCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = zone.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ContentPrimary
                                )
                                Text(
                                    text = "${zone.radiusMeters.roundToInt()}m • ${zone.linkedWifiSsids ?: "GPS"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ContentTertiary
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Add Zone Primary Button
        Button(
            onClick = onStartCreateZone,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.AddLocation, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir zona en este mapa", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// -------------------------------------------------------------
// NEW ZONE CREATION PANEL
// -------------------------------------------------------------
@Composable
private fun NewZoneCreationPanel(
    pinLocation: GeoPoint?,
    name: String,
    onNameChange: (String) -> Unit,
    radiusMeters: Float,
    onRadiusChange: (Float) -> Unit,
    ssidInput: String,
    onSsidChange: (String) -> Unit,
    activeSsid: String?,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA101217)),
        border = BorderStroke(1.dp, SurfaceBorderLight),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nueva Geocerca Radar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ContentPrimary
                    )
                    Text(
                        text = "Toca cualquier punto del mapa para mover el pin",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan
                    )
                }
                IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancelar", tint = ContentTertiary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Presets: Casa / Trabajo / Juan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetMiniChip(label = "🏠 Casa", isSelected = name == "Casa", onClick = { onNameChange("Casa") })
                PresetMiniChip(label = "💼 Trabajo", isSelected = name == "Trabajo", onClick = { onNameChange("Trabajo") })
                PresetMiniChip(label = "👤 Juan", isSelected = name == "Casa Juan", onClick = { onNameChange("Casa Juan") })
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Name & SSID Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ContentPrimary,
                        unfocusedTextColor = ContentPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = ssidInput,
                    onValueChange = onSsidChange,
                    label = { Text("Wi-Fi SSID") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ContentPrimary,
                        unfocusedTextColor = ContentPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Radius Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Radio de detección",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContentTertiary
                )
                Text(
                    text = "${radiusMeters.roundToInt()} metros",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )
            }
            Slider(
                value = radiusMeters,
                onValueChange = onRadiusChange,
                valueRange = 50f..500f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = AccentCyan,
                    activeTrackColor = AccentCyan,
                    inactiveTrackColor = SurfaceBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Text("Cancelar", color = ContentSecondary)
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Fijar y Guardar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PresetMiniChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else Color(0xFF161820),
        border = BorderStroke(1.dp, if (isSelected) AccentCyan else SurfaceBorder)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AccentCyan else ContentPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

// -------------------------------------------------------------
// ZONE INSPECTOR CARD
// -------------------------------------------------------------
@Composable
private fun ZoneInspectorCard(
    zone: GeoZone,
    isCurrent: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onOpenQr: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA101217)),
        border = BorderStroke(1.dp, if (isCurrent) AccentEmerald else SurfaceBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                            .background(if (isCurrent) GlowEmerald else GlowCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isCurrent) Icons.Filled.CheckCircle else Icons.Filled.Place,
                            contentDescription = null,
                            tint = if (isCurrent) AccentEmerald else AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ContentPrimary
                        )
                        Text(
                            text = if (isCurrent) "Conectado ahora • Megas apagados" else "Radio ${zone.radiusMeters.roundToInt()}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) AccentEmerald else ContentTertiary
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = ContentTertiary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details
            val ssids = zone.getSsidList()
            if (ssids.isNotEmpty()) {
                Text(
                    text = "Redes vinculadas: ${ssids.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ContentSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "Regla: Al ingresar apaga datos móviles • Buffer ${zone.disconnectBufferSeconds}s al salir",
                style = MaterialTheme.typography.bodySmall,
                color = ContentTertiary
            )

            if (!zone.wifiPassword.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Key, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Clave guardada: ••••••••",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions row: QR / Clave & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onOpenQr,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AccentCyan.copy(alpha = 0.15f),
                        contentColor = AccentCyan
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver QR / Clave", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentRose)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Eliminar zona", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
