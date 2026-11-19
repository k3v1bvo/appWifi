package com.smartwifi.switcher.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationServices
import com.smartwifi.switcher.data.GeoZone
import com.smartwifi.switcher.ui.theme.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    onNavigateBack: () -> Unit,
    onSaveZone: (GeoZone) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var zoneName by remember { mutableStateOf("Mi Casa") }
    var selectedLocation by remember { mutableStateOf(GeoPoint(-17.3935, -66.1570)) } // Default center
    var radiusMeters by remember { mutableFloatStateOf(150f) }

    var wifiOnEntry by remember { mutableStateOf(true) }
    var dataOffEntry by remember { mutableStateOf(true) }
    var wifiOffExit by remember { mutableStateOf(true) }
    var dataOnExit by remember { mutableStateOf(true) }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var circleOverlay by remember { mutableStateOf<Polygon?>(null) }
    var centerMarker by remember { mutableStateOf<Marker?>(null) }

    // Fetch current GPS location on first open
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

    // Update Circle & Marker whenever selectedLocation or radius changes
    fun updateMapOverlays(mapView: MapView) {
        circleOverlay?.let { mapView.overlays.remove(it) }
        centerMarker?.let { mapView.overlays.remove(it) }

        // Create Circle polygon
        val circlePoints = Polygon.pointsAsCircle(selectedLocation, radiusMeters.toDouble())
        val polygon = Polygon(mapView).apply {
            points = circlePoints
            fillPaint.color = AndroidColor.argb(70, 37, 99, 235) // PrimaryBlue with alpha
            fillPaint.style = Paint.Style.FILL
            outlinePaint.color = AndroidColor.rgb(37, 99, 235)
            outlinePaint.strokeWidth = 4f
        }
        mapView.overlays.add(polygon)
        circleOverlay = polygon

        // Create center pin marker
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

    // Handle MapView Lifecycle
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
                title = { Text("Nueva Zona con Radio") },
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
                            dataOnAtExit = dataOnExit
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
                    Text("Guardar y Activar Zona", color = Color.White)
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
            // Map container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(16.5)
                            controller.setCenter(selectedLocation)

                            // Tap on map listener to move location
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

                // Current GPS location FAB button
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

            // Controls & Settings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configuración de la Ubicación",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = zoneName,
                        onValueChange = { zoneName = it },
                        label = { Text("Nombre de la Zona") },
                        placeholder = { Text("Ej. Mi Casa, Trabajo, Universidad") },
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

                    // Radius Slider
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

                    Text(
                        text = "💡 Recomendado: 100m - 150m para que detecte justo al entrar a tu calle.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = SurfaceDark)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Acciones Automáticas",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SwitchOption(
                        title = "Al entrar: Prender Wi-Fi",
                        checked = wifiOnEntry,
                        onCheckedChange = { wifiOnEntry = it }
                    )

                    SwitchOption(
                        title = "Al entrar: Apagar Datos Móviles (Megas)",
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
                }
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
