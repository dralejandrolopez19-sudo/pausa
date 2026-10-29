package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PausaUiState
import com.example.ui.components.PlantIllustration
import com.example.ui.theme.PausaCardBorder
import com.example.ui.theme.PausaCardLight
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDarkGreen
import com.example.ui.theme.PausaDeepForest
import com.example.ui.theme.PausaSageLight
import com.example.ui.theme.PausaTextPrimary
import com.example.ui.theme.PausaTextSecondary
import java.util.Locale

private data class PauseDurationOption(
    val seconds: Int,
    val chipLabel: String,
    val modeTitle: String,
    val recoveredMinutesBonus: Int,
    val plantBonusPercent: Int
)

private data class FocusCycleOption(
    val focusMinutes: Int,
    val pauseMinutes: Int,
    val label: String,
    val subtitle: String
)

private val pauseDurationPresets = listOf(
    PauseDurationOption(60, "1 min", "Pausa rápida", 15, 10),
    PauseDurationOption(300, "5 min", "Pausa consciente", 20, 14),
    PauseDurationOption(900, "15 min", "Desconexión corta", 30, 20),
    PauseDurationOption(1800, "30 min", "Enfoque profundo", 45, 28),
    PauseDurationOption(3600, "1 hora", "Inmersión sin redes", 60, 40),
    PauseDurationOption(7200, "2 horas", "Desconexión total", 120, 60)
)

private val focusCyclePresets = listOf(
    FocusCycleOption(25, 5, "25m · 5m", "Pomodoro clásico"),
    FocusCycleOption(45, 10, "45m · 10m", "Bloque profundo"),
    FocusCycleOption(60, 15, "60m · 15m", "Inmersión creativa"),
    FocusCycleOption(90, 20, "90m · 20m", "Flujo extendido")
)

@Composable
fun ModoPausaScreen(
    uiState: PausaUiState,
    onContinueWhatIWasDoing: () -> Unit,
    onCloseApplication: () -> Unit,
    onOpenAlternativeActivities: () -> Unit,
    onDismissAlternativeActivities: () -> Unit,
    onSelectAlternativeActivity: (String) -> Unit,
    onRestartTimer: () -> Unit,
    onSelectPauseDuration: (Int) -> Unit = {},
    onExtendPauseTime: (Int) -> Unit = {},
    onUpdateFocusIntervals: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var isFocusCycleMode by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "breathing_ring")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    val totalSeconds = uiState.totalPauseDurationSeconds.coerceAtLeast(60)
    val sweepProgress = (uiState.countdownSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    val activePreset = pauseDurationPresets.lastOrNull { totalSeconds >= it.seconds }
        ?: pauseDurationPresets.first()

    val formattedTime = formatPauseTime(uiState.countdownSeconds)
    val timeUnitSubtitle = when {
        isFocusCycleMode -> "enfoque ${uiState.stats.focusWorkMinutes}m · pausa ${uiState.stats.focusPauseMinutes}m"
        uiState.countdownSeconds >= 3600 -> "horas de pausa"
        uiState.countdownSeconds > 60 -> "minutos restantes"
        else -> "segundos"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PausaDeepForest)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Modo Pausa",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFD8E6DD),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Unified Mode Switcher: "Pausa consciente" vs "Ciclo de enfoque"
        Surface(
            color = Color(0xFF1E3B2E),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.border(1.dp, Color(0xFF345E4B), RoundedCornerShape(22.dp))
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (!isFocusCycleMode) Color(0xFF9AC5A9) else Color.Transparent)
                        .clickable { isFocusCycleMode = false }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("chip_modo_pausa_consciente"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pausa consciente",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (!isFocusCycleMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isFocusCycleMode) Color(0xFF10261C) else Color(0xFFC8DDD0)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isFocusCycleMode) Color(0xFF9AC5A9) else Color.Transparent)
                        .clickable { isFocusCycleMode = true }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("chip_modo_ciclo_enfoque"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ciclo de enfoque",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isFocusCycleMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (isFocusCycleMode) Color(0xFF10261C) else Color(0xFFC8DDD0)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active mode badge
        Surface(
            color = Color(0xFF284D3C),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, Color(0xFF3E6E58), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    tint = Color(0xFFA7D4B6),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isFocusCycleMode) {
                        "Bloque ${uiState.stats.focusWorkMinutes} min + ${uiState.stats.focusPauseMinutes} min pausa"
                    } else {
                        "${activePreset.modeTitle} · +${activePreset.recoveredMinutesBonus} min recuperados"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFDCEEE2),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Circular Countdown Ring with Leaf Icon at Top
        Box(
            modifier = Modifier
                .size(214.dp)
                .clickable { onRestartTimer() }
                .testTag("timer_ring_60s"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f * breathScale

                // Background ring track
                drawCircle(
                    color = Color(0xFF2B5240),
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                // Active countdown arc
                drawArc(
                    color = Color(0xFF9AC5A9),
                    startAngle = -90f,
                    sweepAngle = 360f * sweepProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = "Respira",
                    tint = Color(0xFFB5DAC1),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = if (formattedTime.length > 5) 36.sp else if (formattedTime.length > 2) 44.sp else 54.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF6F4EE)
                )
                Text(
                    text = timeUnitSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC2D6C9)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFocusCycleMode) "Elige tu ciclo de enfoque y descanso" else "Elige el tiempo de tu pausa",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFB8D0C1),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (!isFocusCycleMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pauseDurationPresets.forEach { preset ->
                    val isSelected = uiState.totalPauseDurationSeconds == preset.seconds
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) Color(0xFF9AC5A9) else Color(0xFF254939)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFC5E5D0) else Color(0xFF3A6752),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onSelectPauseDuration(preset.seconds) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("chip_pausa_${preset.seconds}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.chipLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF10261C) else Color(0xFFE4EFE8)
                        )
                    }
                }

                // Quick +15 min extension chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF2E5B47))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF6CA486),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { onExtendPauseTime(900) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("chip_extender_15m"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color(0xFFD6EDE0),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "15 min",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD6EDE0)
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                focusCyclePresets.forEach { cycle ->
                    val isSelected = uiState.stats.focusWorkMinutes == cycle.focusMinutes &&
                        uiState.stats.focusPauseMinutes == cycle.pauseMinutes
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) Color(0xFF9AC5A9) else Color(0xFF254939)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFC5E5D0) else Color(0xFF3A6752),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                onUpdateFocusIntervals(cycle.focusMinutes, cycle.pauseMinutes)
                                onSelectPauseDuration(cycle.focusMinutes * 60)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("chip_ciclo_${cycle.focusMinutes}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cycle.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF10261C) else Color(0xFFE4EFE8)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Respira. Piensa. Decide.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFF6F4EE),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Card: "¿Qué necesitas hacer ahora?" with 3 exact options from Screen 3
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCreamBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header row with potted plant + "¿Qué necesitas hacer ahora?"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlantIllustration(
                        level = uiState.activePlant?.level ?: 3,
                        modifier = Modifier.size(48.dp),
                        showAuraCircle = false
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "¿Qué necesitas hacer\nahora?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PausaTextPrimary
                        )
                        Text(
                            text = "App pausada: ${uiState.triggeredAppName} · Saldo PayPal: $${String.format(Locale.US, "%.2f", uiState.stats.digitalWalletBalance)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PausaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PausaCardBorder)
                )

                // Option 1: Continuar con lo que iba (Requires mandatory $1.00 PayPal payment to break pause)
                val breakSubtitle = if (uiState.stats.digitalWalletBalance >= uiState.stats.penaltyPerBreak && uiState.stats.accumulatedDebtDollars <= 0.0) {
                    "Romper pausa pagando -$1.00 USD de tu saldo PayPal"
                } else {
                    "Solo pagando $1.00 USD vía PayPal puedes romper la pausa"
                }
                PauseDecisionRow(
                    icon = Icons.Filled.CheckBox,
                    title = "Continuar con lo que iba",
                    subtitle = breakSubtitle,
                    onClick = onContinueWhatIWasDoing,
                    testTag = "btn_continuar_romper_pausa"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PausaCardBorder)
                )

                // Option 2: Cerrar la aplicación (Saves minutes & grows plant proportionally)
                PauseDecisionRow(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Cerrar la aplicación",
                    subtitle = "Recuperar +${activePreset.recoveredMinutesBonus} min y +${activePreset.plantBonusPercent}% para tu planta",
                    onClick = onCloseApplication,
                    testTag = "btn_cerrar_aplicacion"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PausaCardBorder)
                )

                // Option 3: Elegir otra actividad (Opens healthy habits selector)
                PauseDecisionRow(
                    icon = Icons.Filled.FavoriteBorder,
                    title = "Elegir otra actividad",
                    subtitle = "Cambiar scroll por un hábito saludable (+${activePreset.recoveredMinutesBonus} min)",
                    onClick = onOpenAlternativeActivities,
                    testTag = "btn_elegir_otra_actividad"
                )
            }
        }
    }

    // Dialog for "Elegir otra actividad"
    if (uiState.showAlternativeActivitySheet) {
        val activities = listOf(
            Triple("Respiración consciente (2 min)", "Oxigena tu mente y reduce el impulso", Icons.Filled.SelfImprovement),
            Triple("Leer 5 páginas de un libro", "Reemplaza el scroll por lectura profunda", Icons.AutoMirrored.Filled.MenuBook),
            Triple("Hidratarse y estirar el cuerpo", "Levántate por un vaso de agua fresca", Icons.Filled.WaterDrop),
            Triple("Caminar 5 minutos sin pantalla", "Observa tu entorno y descansa la vista", Icons.Filled.Spa)
        )
        AlertDialog(
            onDismissRequest = onDismissAlternativeActivities,
            containerColor = PausaCardLight,
            title = {
                Text(
                    text = "Elegir otra actividad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PausaTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Convierte este momento en algo que te haga bien (+15 min recuperados):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PausaTextSecondary
                    )
                    activities.forEach { (title, desc, icon) ->
                        Surface(
                            color = PausaCreamBackground,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectAlternativeActivity(title) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PausaSageLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = PausaDarkGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = PausaTextPrimary
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PausaTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissAlternativeActivities) {
                    Text("Volver", color = PausaDarkGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun PauseDecisionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 13.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PausaDarkGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = PausaTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = PausaTextSecondary
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = PausaTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun formatPauseTime(seconds: Int): String {
    if (seconds <= 60) {
        return seconds.toString()
    }
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }
}
