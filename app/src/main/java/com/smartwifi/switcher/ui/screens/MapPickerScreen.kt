package com.smartwifi.switcher.ui.screens

import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationServices
import com.smartwifi.switcher.data.GeoZone
import com.smartwifi.switcher.service.WifiHelper
import com.smartwifi.switcher.ui.theme.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MapPickerScreen(
    onNavigateBack: () -> Unit,
    onSaveZone: (GeoZone) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var zoneName by remember { mutableStateOf("Hogar") }
    var selectedLocation by remember { mutableStateOf(GeoPoint(-17.3935, -66.1570)) }
    var radiusMeters by remember { mutableFloatStateOf(150f) }

    val initialSsid = WifiHelper.getCurrentSsid(context)
    var ssidList by remember { mutableStateOf(initialSsid?.let { listOf(it) } ?: emptyList()) }
    var newSsidInput by remember { mutableStateOf("") }

    var selectedBatteryMode by remember { mutableStateOf("HYBRID") } // HYBRID, WIFI_ONLY, GEOFENCE_ONLY
    var bufferSeconds by remember { mutableFloatStateOf(20f) }
    var vibrateOnAction by remember { mutableStateOf(true) }

    var wifiOnEntry by remember { mutableStateOf(true) }
    var dataOffEntry by remember { mutableStateOf(true) }
    var wifiOffExit by remember { mutableStateOf(true) }
    var dataOnExit by remember { mutableStateOf(true) }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var circleOverlay by remember { mutableStateOf<Polygon?>(null) }
    var centerMarker by remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(Unit) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    val currentPoint = GeoPoint(loc.latitude, loc.longitude)
                    selectedLocation = currentPoint
                    mapViewRef?.controller?.setCenter(currentPoint)
                }
            }
        } catch (_: SecurityException) {}
    }

    fun updateMapOverlays(mapView: MapView) {
        circleOverlay?.let { mapView.overlays.remove(it) }
        centerMarker?.let { mapView.overlays.remove(it) }

            val circlePoints = Polygon.pointsAsCircle(selectedLocation, radiusMeters.toDouble())
        val polygon = Polygon(mapView).apply {
            points = circlePoints
            fillPaint.color = AndroidColor.argb(55, 0, 210, 255)
            fillPaint.style = Paint.Style.FILL
            outlinePaint.color = AndroidColor.rgb(0, 210, 255)
            outlinePaint.strokeWidth = 3.5f
        }
        mapView.overlays.add(polygon)
        circleOverlay = polygon

        val marker = Marker(mapView).apply {
            position = selectedLocation
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = zoneName
        }
        mapView.overlays.add(marker)
        centerMarker = marker

        mapView.invalidate()
    }

    LaunchedEffect(selectedLocation, radiusMeters) {
        mapViewRef?.let { updateMapOverlays(it) }
    }

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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración de Zona",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ContentPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Regresar",
                            tint = ContentPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBg
                )
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val newZone = GeoZone(
                            name = zoneName.ifBlank { "Hogar" },
                            latitude = selectedLocation.latitude,
                            longitude = selectedLocation.longitude,
                            radiusMeters = radiusMeters,
                            wifiOnAtEntry = wifiOnEntry,
                            dataOffAtEntry = dataOffEntry,
                            wifiOffAtExit = wifiOffExit,
                            dataOnAtExit = dataOnExit,
                            linkedWifiSsids = if (ssidList.isNotEmpty()) ssidList.joinToString(",") else null,
                            batteryMode = selectedBatteryMode,
                            disconnectBufferSeconds = bufferSeconds.roundToInt(),
                            vibrateOnAction = vibrateOnAction
                        )
                        onSaveZone(newZone)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Guardar y aplicar zona",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
                .verticalScroll(rememberScrollState())
        ) {
            // Map Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(MapTileSources.CARTO_DARK)
                            isTilesScaledToDpi = true
                            setMultiTouchControls(true)
                            controller.setZoom(16.5)
                            controller.setCenter(selectedLocation)

                            val receiver = object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    selectedLocation = p
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint): Boolean = false
                            }
                            overlays.add(0, MapEventsOverlay(receiver))

                            mapViewRef = this
                            updateMapOverlays(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Current GPS Pin button
                IconButton(
                    onClick = {
                        try {
                            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                            fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                                if (loc != null) {
                                    val currentPoint = GeoPoint(loc.latitude, loc.longitude)
                                    selectedLocation = currentPoint
                                    mapViewRef?.controller?.animateTo(currentPoint)
                                }
                            }
                        } catch (_: SecurityException) {}
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Zone Form Sections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Identification
                FormSectionCard {
                    Text(
                        text = "IDENTIFICACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = zoneName,
                        onValueChange = { zoneName = it },
                        placeholder = { Text("Nombre descriptivo (ej. Casa, Oficina)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ContentPrimary,
                            unfocusedTextColor = ContentPrimary,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        ),
                        singleLine = true
                    )
                }

                // Section 2: Linked Wi-Fi Networks
                FormSectionCard {
                    Text(
                        text = "REDES WI-FI VINCULADAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "La desconexión de estas redes activa los datos móviles en milisegundos sin consultar el GPS.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ContentSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (ssidList.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ssidList.forEach { ssid ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                            text = ssid,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontFamily = FontFamily.Monospace,
                                            color = ContentPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Quitar",
                                            tint = ContentTertiary,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable {
                                                    ssidList = ssidList.filter { it != ssid }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Add Wi-Fi row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSsidInput,
                            onValueChange = { newSsidInput = it },
                            placeholder = { Text("Nombre de SSID") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ContentPrimary,
                                unfocusedTextColor = ContentPrimary,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard
                            ),
                            singleLine = true
                        )

                        FilledTonalButton(
                            onClick = {
                                if (newSsidInput.isNotBlank()) {
                                    val trimmed = newSsidInput.trim()
                                    if (!ssidList.contains(trimmed)) {
                                        ssidList = ssidList + trimmed
                                        newSsidInput = ""
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SurfaceCard,
                                contentColor = ContentPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Text("Añadir")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            val detected = WifiHelper.getCurrentSsid(context)
                            if (detected != null) {
                                if (!ssidList.contains(detected)) {
                                    ssidList = ssidList + detected
                                    Toast.makeText(context, "Vinculado: $detected", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Conéctate a una red Wi-Fi primero", Toast.LENGTH_SHORT).show()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        Icon(
                            Icons.Outlined.AddCircleOutline,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Detectar y vincular red actual",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentCyan
                        )
                    }
                }

                // Section 3: Detection Engine & Parameters
                FormSectionCard {
                    Text(
                        text = "PARÁMETROS DE SENSIBILIDAD",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Mode Selector (Clean Segmented Style)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppBg)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ModeSegment(
                            title = "Híbrido (Óptimo)",
                            isSelected = selectedBatteryMode == "HYBRID",
                            onClick = { selectedBatteryMode = "HYBRID" },
                            modifier = Modifier.weight(1f)
                        )
                        ModeSegment(
                            title = "Solo Wi-Fi (0% GPS)",
                            isSelected = selectedBatteryMode == "WIFI_ONLY",
                            onClick = { selectedBatteryMode = "WIFI_ONLY" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buffer anti-desconexión
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tolerancia ante micro-cortes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ContentPrimary
                        )
                        Text(
                            text = "${bufferSeconds.roundToInt()} s",
                            style = MaterialTheme.typography.labelLarge,
                            color = AccentCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Espera antes de reactivar datos tras perder Wi-Fi.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                    Slider(
                        value = bufferSeconds,
                        onValueChange = { bufferSeconds = it },
                        valueRange = 10f..45f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = AccentBlue,
                            inactiveTrackColor = SurfaceBorder
                        )
                    )

                    // Radius slider (if not pure wifi only)
                    if (selectedBatteryMode != "WIFI_ONLY") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Radio geográfico",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ContentPrimary
                            )
                            Text(
                                text = "${radiusMeters.roundToInt()} m",
                                style = MaterialTheme.typography.labelLarge,
                                color = AccentCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = radiusMeters,
                            onValueChange = { radiusMeters = it },
                            valueRange = 50f..600f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = AccentBlue,
                                inactiveTrackColor = SurfaceBorder
                            )
                        )
                    }
                }

                // Section 4: Trigger Actions
                FormSectionCard {
                    Text(
                        text = "REGLAS DE AUTOMATIZACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = ContentTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ToggleRow(
                        title = "Al entrar: Activar Wi-Fi",
                        checked = wifiOnEntry,
                        onCheckedChange = { wifiOnEntry = it }
                    )
                    ToggleRow(
                        title = "Al entrar: Suspender datos móviles",
                        checked = dataOffEntry,
                        onCheckedChange = { dataOffEntry = it }
                    )
                    ToggleRow(
                        title = "Al salir: Apagar Wi-Fi",
                        checked = wifiOffExit,
                        onCheckedChange = { wifiOffExit = it }
                    )
                    ToggleRow(
                        title = "Al salir: Reactivar datos móviles",
                        checked = dataOnExit,
                        onCheckedChange = { dataOnExit = it }
                    )
                    ToggleRow(
                        title = "Respuesta háptica (Vibración discreta)",
                        checked = vibrateOnAction,
                        onCheckedChange = { vibrateOnAction = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FormSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun ModeSegment(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) SurfaceCard else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) SurfaceBorderLight else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) ContentPrimary else ContentTertiary
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = ContentPrimary
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentBlue,
                uncheckedThumbColor = ContentSecondary,
                uncheckedTrackColor = SurfaceCard,
                uncheckedBorderColor = SurfaceBorder
            )
        )
    }
}
