package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Reactive theme flag synced by MyApplicationTheme(darkTheme) so all Pausa* color tokens
 * (including inside Canvas DrawScope) recompose immediately between Light Mode and
 * "Bosque Nocturno" Dark Mode.
 */
var isPausaDarkThemeActive by mutableStateOf(false)

// Adaptive Semantic Tokens (Warm Organic Light Mode / Bosque Nocturno Soft Forest Dark Mode)
val PausaCreamBackground: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF121A15) else Color(0xFFF6F4EE)

val PausaCardSurface: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF1B2620) else Color(0xFFEDEBE3)

val PausaCardLight: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF223029) else Color(0xFFFBF9F5)

val PausaCardBorder: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF2C3E34) else Color(0xFFE0DDD3)

val PausaDarkGreen: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF68A37C) else Color(0xFF1E4332)

val PausaDeepForest: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF0D1510) else Color(0xFF143023)

val PausaMediumGreen: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF7BB38F) else Color(0xFF2D5E46)

val PausaAccentGreen: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF85B896) else Color(0xFF569A73)

val PausaSageLight: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF24382D) else Color(0xFFDCE7DF)

val PausaPlantAura: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF1D3025) else Color(0xFFE6ECE0)

val PausaTextPrimary: Color
    get() = if (isPausaDarkThemeActive) Color(0xFFEAE6DF) else Color(0xFF172C22)

val PausaTextSecondary: Color
    get() = if (isPausaDarkThemeActive) Color(0xFFAAB8B0) else Color(0xFF52635A)

val PausaTextMuted: Color
    get() = if (isPausaDarkThemeActive) Color(0xFF76887E) else Color(0xFF7B8A81)

val PausaAlertRed: Color
    get() = if (isPausaDarkThemeActive) Color(0xFFE57373) else Color(0xFFD94A4A)

val PausaFlameOrange = Color(0xFFE87A3E)
val PausaSunYellow = Color(0xFFF2C94C)
