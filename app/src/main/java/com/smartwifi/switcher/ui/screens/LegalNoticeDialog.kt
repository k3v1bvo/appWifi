package com.smartwifi.switcher.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smartwifi.switcher.ui.theme.*

@Composable
fun LegalNoticeDialog(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Privacidad", "Licencia / EULA", "Créditos")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
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
                                .size(36.dp)
                                .background(GlowCyan, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Gavel,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Avisos Legales y Privacidad",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ContentPrimary
                            )
                            Text(
                                text = "Smart Wifi Switch v1.0.0",
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

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceCard,
                    contentColor = AccentCyan,
                    indicator = {},
                    divider = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) AccentCyan else ContentTertiary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> PrivacyContent()
                        1 -> LicenseContent()
                        2 -> ThirdPartyContent()
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = ContentPrimary),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Text("Entendido y Cerrar")
                }
            }
        }
    }
}

@Composable
private fun PrivacyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LegalSection(
            title = "🛡️ Privacidad 100% Local (Zero-Server)",
            body = "Toda la información se procesa y almacena en la memoria aislada de tu propio teléfono. No contamos con servidores externos, bases de datos remotas ni trackers de publicidad."
        )
        LegalSection(
            title = "📍 Uso de Ubicación en Segundo Plano",
            body = "El permiso de ubicación todo el tiempo es requerido exclusivamente por el sistema operativo Android para detectar la entrada o salida de tus zonas guardadas y cambiar el estado de los datos móviles automáticamente con la pantalla apagada."
        )
        LegalSection(
            title = "🔑 Llavero de Contraseñas",
            body = "Las claves Wi-Fi que guardas se almacenan localmente en tu base de datos SQLite y solo tú puedes verlas o compartirlas por código QR en tu pantalla. Jamás se transmiten a internet."
        )
        LegalSection(
            title = "✉️ Contacto del Desarrollador",
            body = "Para dudas o consultas de privacidad: k3v1bvo@gmail.com"
        )
    }
}

@Composable
private fun LicenseContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LegalSection(
            title = "🔒 Software Propietario — Todos los Derechos Reservados",
            body = "Copyright (c) 2026 k3v1bvo. Todos los derechos reservados.\n\nEste software es propiedad intelectual privada. Queda terminantemente prohibida su copia, reproducción, ingeniería inversa, modificación, distribución no autorizada o explotación comercial sin el consentimiento previo por escrito del autor."
        )
        LegalSection(
            title = "⚠️ Exención de Responsabilidad (AS IS)",
            body = "El software se entrega 'tal cual' para uso personal, sin garantías explícitas ni implícitas sobre el consumo de batería o planes de telefonía móvil derivados de desconexiones imprevistas de red."
        )
    }
}

@Composable
private fun ThirdPartyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LegalSection(
            title = "🗺️ Cartografía y Datos Geoespaciales",
            body = "Mosaicos Dark Matter provistos por CARTO.\nDatos base © OpenStreetMap contributors (ODbL / CC BY 4.0)."
        )
        LegalSection(
            title = "📦 Bibliotecas de Código Abierto (Apache 2.0)",
            body = "• Android Jetpack Compose & Room (Google LLC)\n• Google Play Services Location (Google LLC)\n• OSMDroid (OSMDroid Contributors)\n• Shizuku API (Rikka / dev.rikka.shizuku)\n• ZXing Core QR Generator (ZXing Authors)\n• KotlinX Coroutines (JetBrains s.r.o.)"
        )
    }
}

@Composable
private fun LegalSection(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AccentCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = ContentSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
