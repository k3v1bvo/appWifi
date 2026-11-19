package com.smartwifi.switcher.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlueGlow,
    secondary = AccentCyan,
    background = AppBg,
    surface = SurfaceElevated,
    surfaceVariant = SurfaceCard,
    outline = SurfaceBorder,
    onPrimary = ContentPrimary,
    onBackground = ContentPrimary,
    onSurface = ContentPrimary,
    onSurfaceVariant = ContentSecondary
)

@Composable
fun SmartWifiSwitchTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AppBg.toArgb()
            window.navigationBarColor = AppBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
