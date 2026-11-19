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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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

    var zoneName by remember { mutableStateOf("Mi Casa") }
    var selectedLocation by remember { mutableStateOf(GeoPoint(-17.3935, -66.1570)) }
    var radiusMeters by remember { mutableFloatStateOf(150f) }

    // Multi-SSID List
    val initialSsid = WifiHelper.getCurrentSsid(context)
    var ssidList by remember { mutableStateOf(initialSsid?.let { listOf(it) } ?: emptyList()) }
    var manualSsidInput by remember { mutableStateOf("") }

    var selectedBatteryMode by remember { mutableStateOf("HYBRID") } // HYBRID, WIFI_ONLY, GEOFENCE_ONLY
    var bufferSeconds by remember { mutableFloatStateOf(20f) } // Grace period
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
            fillPaint.color = AndroidColor.argb(70, 37, 99, 235)
            fillPaint.style = Paint.Style.FILL
            outlinePaint.color = AndroidColor.rgb(37, 99, 235)
            outlinePaint.strokeWidth = 4f
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
        topBar = {
            TopAppBar(
                title = { Text("Configurar Zona Inteligente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextLight,
                    navigationIconContentColor = TextLight
                )
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val newZone = GeoZone(
                            name = zoneName.ifBlank { "Mi Zona" },
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
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar y Activar Zona", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBackground)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
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
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación", tint = PrimaryCyan)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Datos de la Zona",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = zoneName,
                        onValueChange = { zoneName = it },
                        label = { Text("Nombre de la Zona") },
                        placeholder = { Text("Ej. Mi Casa") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = TextMuted,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextMuted
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Multi-SSID Card (2.4G + 5G + Repeaters)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Wifi, contentDescription = null, tint = PrimaryCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Redes Wi-Fi de Casa (Multi-Red)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextLight
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Puedes agregar tu red de 2.4GHz, 5GHz y repetidores. Si te conectas a cualquiera de ellas, se apagarán los megas sin gastar batería.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Chip List
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ssidList.forEach { ssid ->
                                    InputChip(
                                        selected = true,
                                        onClick = {},
                                        label = { Text(ssid, color = Color.White) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Quitar",
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        ssidList = ssidList.filter { it != ssid }
                                                    }
                                            )
                                        },
                                        colors = InputChipDefaults.inputChipColors(selectedContainerColor = PrimaryBlue)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        val detected = WifiHelper.getCurrentSsid(context)
                                        if (detected != null) {
                                            if (!ssidList.contains(detected)) {
                                                ssidList = ssidList + detected
                                                Toast.makeText(context, "Agregado: $detected", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Ya está en la lista", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Conéctate a tu Wi-Fi para agregarlo", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Agregar Wi-Fi Actual", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buffer anti-pestañeo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Buffer Anti-Pestañeo:", color = TextLight, fontWeight = FontWeight.SemiBold)
                                Text("${bufferSeconds.roundToInt()} segundos", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Si el router se reinicia o estás en una esquina con poca señal, la app esperará este tiempo antes de prender los datos por error.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Slider(
                                value = bufferSeconds,
                                onValueChange = { bufferSeconds = it },
                                valueRange = 10f..45f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryCyan,
                                    activeTrackColor = PrimaryBlue,
                                    inactiveTrackColor = SurfaceDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Battery Mode selector
                    Text(
                        text = "Modo de Detección",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    BatteryModeOption(
                        title = "Híbrido Inteligente (Recomendado)",
                        subtitle = "Usa antenas celulares al llegar y corte de Wi-Fi al salir. Batería < 1% al día.",
                        isSelected = selectedBatteryMode == "HYBRID",
                        onClick = { selectedBatteryMode = "HYBRID" }
                    )

                    BatteryModeOption(
                        title = "Solo Wi-Fi (Zero GPS)",
                        subtitle = "No usa GPS en lo absoluto. Detecta únicamente tus redes de casa. Consumo 0%.",
                        isSelected = selectedBatteryMode == "WIFI_ONLY",
                        onClick = { selectedBatteryMode = "WIFI_ONLY" }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Radius slider
                    if (selectedBatteryMode != "WIFI_ONLY") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Radio de Detección:", color = TextLight)
                            Text(
                                text = "${radiusMeters.roundToInt()} metros",
                                color = PrimaryCyan,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Slider(
                            value = radiusMeters,
                            onValueChange = { radiusMeters = it },
                            valueRange = 50f..800f,
                            steps = 14,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryCyan,
                                activeTrackColor = PrimaryBlue,
                                inactiveTrackColor = SurfaceDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = SurfaceDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Acciones Automáticas y Sensaciones",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SwitchOption(
                        title = "Al llegar: Prender Wi-Fi",
                        checked = wifiOnEntry,
                        onCheckedChange = { wifiOnEntry = it }
                    )

                    SwitchOption(
                        title = "Al llegar: Apagar Datos Móviles (Megas)",
                        checked = dataOffEntry,
                        onCheckedChange = { dataOffEntry = it }
                    )

                    SwitchOption(
                        title = "Al salir: Apagar Wi-Fi",
                        checked = wifiOffExit,
                        onCheckedChange = { wifiOffExit = it }
                    )

                    SwitchOption(
                        title = "Al salir: Prender Datos Móviles",
                        checked = dataOnExit,
                        onCheckedChange = { dataOnExit = it }
                    )

                    SwitchOption(
                        title = "Vibración táctil sutil (Doble toque al llegar)",
                        checked = vibrateOnAction,
                        onCheckedChange = { vibrateOnAction = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryModeOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryCyan else SurfaceDark,
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) SurfaceDark else DarkBackground),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryCyan)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, color = TextLight, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text(subtitle, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SwitchOption(
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
        Text(title, color = TextLight, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceDark
            )
        )
    }
}
