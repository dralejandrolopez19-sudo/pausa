package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val PausaLightColorScheme = lightColorScheme(
    primary = Color(0xFF1E4332),
    onPrimary = Color(0xFFF6F4EE),
    primaryContainer = Color(0xFFDCE7DF),
    onPrimaryContainer = Color(0xFF1E4332),
    secondary = Color(0xFF2D5E46),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDEBE3),
    onSecondaryContainer = Color(0xFF172C22),
    tertiary = Color(0xFF569A73),
    onTertiary = Color.White,
    background = Color(0xFFF6F4EE),
    onBackground = Color(0xFF172C22),
    surface = Color(0xFFF6F4EE),
    onSurface = Color(0xFF172C22),
    surfaceVariant = Color(0xFFEDEBE3),
    onSurfaceVariant = Color(0xFF52635A),
    outline = Color(0xFFE0DDD3),
    error = Color(0xFFD94A4A),
    onError = Color.White
)

val PausaDarkColorScheme = darkColorScheme(
    primary = Color(0xFF68A37C),
    onPrimary = Color(0xFF0D1510),
    primaryContainer = Color(0xFF24382D),
    onPrimaryContainer = Color(0xFFEAE6DF),
    secondary = Color(0xFF7BB38F),
    onSecondary = Color(0xFF0D1510),
    secondaryContainer = Color(0xFF1B2620),
    onSecondaryContainer = Color(0xFFEAE6DF),
    tertiary = Color(0xFF85B896),
    onTertiary = Color(0xFF0D1510),
    background = Color(0xFF121A15),
    onBackground = Color(0xFFEAE6DF),
    surface = Color(0xFF121A15),
    onSurface = Color(0xFFEAE6DF),
    surfaceVariant = Color(0xFF1B2620),
    onSurfaceVariant = Color(0xFFAAB8B0),
    outline = Color(0xFF2C3E34),
    error = Color(0xFFE57373),
    onError = Color(0xFF0D1510)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    isPausaDarkThemeActive = darkTheme
    val colorScheme = if (darkTheme) PausaDarkColorScheme else PausaLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
