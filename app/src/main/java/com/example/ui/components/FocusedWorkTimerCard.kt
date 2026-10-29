package com.example.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blocker.PausaBlockerController
import com.example.ui.theme.PausaAccentGreen
import com.example.ui.theme.PausaCardBorder
import com.example.ui.theme.PausaCardLight
import com.example.ui.theme.PausaCardSurface
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDarkGreen
import com.example.ui.theme.PausaSageLight
import com.example.ui.theme.PausaTextPrimary
import com.example.ui.theme.PausaTextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

enum class AttentionTimerPhase {
    FOCUS_WORK,
    CONSCIOUS_PAUSE
}

private val focusMinutePresets = listOf(15, 25, 45, 60)
private val pauseMinutePresets = listOf(5, 10, 15, 20)

/**
 * FocusedWorkTimerCard: Core attention-management Compose component that allows users
 * to define custom Start (Focus Work) and Pause (Conscious Rest) intervals, run multi-cycle
 * countdown sessions, and synchronize social media blocking while focused.
 */
@Composable
fun FocusedWorkTimerCard(
    initialFocusMinutes: Int = 25,
    initialPauseMinutes: Int = 5,
    onIntervalsChanged: (Int, Int) -> Unit = { _, _ -> },
    onIntervalCompleted: (AttentionTimerPhase, Int) -> Unit = { _, _ -> },
    onLaunchFullScreenPause: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var focusMinutes by rememberSaveable { mutableIntStateOf(initialFocusMinutes.coerceIn(5, 120)) }
    var pauseMinutes by rememberSaveable { mutableIntStateOf(initialPauseMinutes.coerceIn(1, 60)) }
    val totalCycles = 4
    var currentCycle by rememberSaveable { mutableIntStateOf(1) }
    var currentPhase by rememberSaveable { mutableStateOf(AttentionTimerPhase.FOCUS_WORK) }
    var isRunning by rememberSaveable { mutableStateOf(false) }

    val activePhaseTotalSeconds = if (currentPhase == AttentionTimerPhase.FOCUS_WORK) {
        focusMinutes * 60
    } else {
        pauseMinutes * 60
    }

    var remainingSeconds by rememberSaveable { mutableIntStateOf(focusMinutes * 60) }

    LaunchedEffect(initialFocusMinutes, initialPauseMinutes) {
        val safeFocus = initialFocusMinutes.coerceIn(5, 120)
        val safePause = initialPauseMinutes.coerceIn(1, 60)
        if (!isRunning) {
            if (focusMinutes != safeFocus) {
                focusMinutes = safeFocus
                if (currentPhase == AttentionTimerPhase.FOCUS_WORK) {
                    remainingSeconds = safeFocus * 60
                }
            }
            if (pauseMinutes != safePause) {
                pauseMinutes = safePause
                if (currentPhase == AttentionTimerPhase.CONSCIOUS_PAUSE) {
                    remainingSeconds = safePause * 60
                }
            }
        }
    }

    // Synchronize countdown loop and social media blocker state
    LaunchedEffect(isRunning, currentPhase) {
        if (isRunning) {
            PausaBlockerController.setPauseTimerActiveState(
                context = context,
                isRunning = true,
                remainingSeconds = remainingSeconds
            )
            while (remainingSeconds > 0 && isRunning) {
                delay(1000L)
                remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
            }
            if (remainingSeconds == 0 && isRunning) {
                val finishedPhase = currentPhase
                val completedMinutes = if (finishedPhase == AttentionTimerPhase.FOCUS_WORK) {
                    focusMinutes
                } else {
                    pauseMinutes
                }
                onIntervalCompleted(finishedPhase, completedMinutes)

                if (finishedPhase == AttentionTimerPhase.FOCUS_WORK) {
                    currentPhase = AttentionTimerPhase.CONSCIOUS_PAUSE
                    remainingSeconds = pauseMinutes * 60
                } else {
                    if (currentCycle < totalCycles) {
                        currentCycle += 1
                        currentPhase = AttentionTimerPhase.FOCUS_WORK
                        remainingSeconds = focusMinutes * 60
                    } else {
                        isRunning = false
                        currentCycle = 1
                        currentPhase = AttentionTimerPhase.FOCUS_WORK
                        remainingSeconds = focusMinutes * 60
                        PausaBlockerController.setPauseTimerActiveState(
                            context = context,
                            isRunning = false,
                            remainingSeconds = 0
                        )
                    }
                }
            }
        } else {
            PausaBlockerController.setPauseTimerActiveState(
                context = context,
                isRunning = false,
                remainingSeconds = 0
            )
        }
    }

    val pulseScale = if (isRunning) {
        val infiniteTransition = rememberInfiniteTransition(label = "focus_timer_pulse")
        val animatedScale by infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
        animatedScale
    } else {
        1f
    }

    val progress = (remainingSeconds.toFloat() / activePhaseTotalSeconds.coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)
    val isFocusPhase = currentPhase == AttentionTimerPhase.FOCUS_WORK
    val activeAccentColor = if (isFocusPhase) PausaDarkGreen else PausaAccentGreen

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PausaCardBorder, RoundedCornerShape(22.dp))
            .testTag("card_temporizador_enfoque")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Header: Title + Cycle / Phase Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PausaSageLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFocusPhase) Icons.Filled.Timer else Icons.Filled.SelfImprovement,
                            contentDescription = "Temporizador de enfoque",
                            tint = PausaDarkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Temporizador de Enfoque",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PausaTextPrimary
                        )
                        Text(
                            text = "Define tus intervalos de inicio y pausa",
                            style = MaterialTheme.typography.labelSmall,
                            color = PausaTextSecondary
                        )
                    }
                }

                Surface(
                    color = PausaSageLight,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Ciclo $currentCycle/$totalCycles",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PausaDarkGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Center Timer Ring + Live Phase Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PausaCreamBackground)
                    .border(1.dp, PausaCardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .testTag("ring_temporizador_enfoque"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 9.dp.toPx()
                        val effectiveScale = if (isRunning) pulseScale else 1f
                        val radius = (size.minDimension - stroke) / 2f * effectiveScale

                        drawCircle(
                            color = PausaCardBorder,
                            radius = radius,
                            style = Stroke(width = stroke)
                        )
                        drawArc(
                            color = activeAccentColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatIntervalSeconds(remainingSeconds),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = PausaTextPrimary,
                            modifier = Modifier.testTag("text_tiempo_intervalo")
                        )
                        Text(
                            text = if (isFocusPhase) "ENFOQUE" else "PAUSA",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeAccentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isFocusPhase) {
                            "Intervalo de Trabajo ($focusMinutes min)"
                        } else {
                            "Intervalo de Pausa ($pauseMinutes min)"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PausaTextPrimary
                    )
                    Text(
                        text = if (isRunning) {
                            if (isFocusPhase) {
                                "Redes sociales bloqueadas durante tu bloque de atención."
                            } else {
                                "Descansa la vista y respira antes del siguiente bloque."
                            }
                        } else {
                            "Configura abajo la duración de inicio (trabajo) y pausa."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = PausaTextSecondary
                    )

                    // Play / Pause, Skip Phase, and Reset Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isRunning = !isRunning },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeAccentColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_iniciar_pausar_intervalo")
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isRunning) "Pausar temporizador" else "Iniciar temporizador",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRunning) "Pausar" else "Iniciar",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                isRunning = false
                                if (currentPhase == AttentionTimerPhase.FOCUS_WORK) {
                                    currentPhase = AttentionTimerPhase.CONSCIOUS_PAUSE
                                    remainingSeconds = pauseMinutes * 60
                                } else {
                                    currentPhase = AttentionTimerPhase.FOCUS_WORK
                                    remainingSeconds = focusMinutes * 60
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PausaCardLight)
                                .border(1.dp, PausaCardBorder, CircleShape)
                                .testTag("btn_cambiar_fase_intervalo")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Cambiar entre enfoque y pausa",
                                tint = PausaTextPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                isRunning = false
                                remainingSeconds = if (currentPhase == AttentionTimerPhase.FOCUS_WORK) {
                                    focusMinutes * 60
                                } else {
                                    pauseMinutes * 60
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PausaCardLight)
                                .border(1.dp, PausaCardBorder, CircleShape)
                                .testTag("btn_reiniciar_intervalo")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reiniciar intervalo",
                                tint = PausaTextPrimary
                            )
                        }
                    }
                }
            }

            // Interval Configuration 1: Focus / Start Interval Selector
            IntervalSelectorSection(
                label = "1. Intervalo de Inicio (Enfoque)",
                currentMinutes = focusMinutes,
                presets = focusMinutePresets,
                stepMinutes = 5,
                minMinutes = 5,
                maxMinutes = 120,
                tagPrefix = "focus",
                onMinutesChanged = { newMins ->
                    focusMinutes = newMins
                    if (!isRunning && currentPhase == AttentionTimerPhase.FOCUS_WORK) {
                        remainingSeconds = newMins * 60
                    }
                    onIntervalsChanged(newMins, pauseMinutes)
                }
            )

            // Interval Configuration 2: Pause / Rest Interval Selector
            IntervalSelectorSection(
                label = "2. Intervalo de Pausa (Descanso)",
                currentMinutes = pauseMinutes,
                presets = pauseMinutePresets,
                stepMinutes = 1,
                minMinutes = 1,
                maxMinutes = 60,
                tagPrefix = "pause",
                onMinutesChanged = { newMins ->
                    pauseMinutes = newMins
                    if (!isRunning && currentPhase == AttentionTimerPhase.CONSCIOUS_PAUSE) {
                        remainingSeconds = newMins * 60
                    }
                    onIntervalsChanged(focusMinutes, newMins)
                }
            )

            // Full-Screen Immersive Mode CTA
            OutlinedButton(
                onClick = { onLaunchFullScreenPause(focusMinutes * 60) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_abrir_pausa_inmersiva")
            ) {
                Icon(
                    imageVector = Icons.Filled.SelfImprovement,
                    contentDescription = null,
                    tint = PausaDarkGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Iniciar bloqueo inmersivo ($focusMinutes min enfoque / $pauseMinutes min pausa)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PausaDarkGreen
                )
            }
        }
    }
}

@Composable
private fun IntervalSelectorSection(
    label: String,
    currentMinutes: Int,
    presets: List<Int>,
    stepMinutes: Int,
    minMinutes: Int,
    maxMinutes: Int,
    tagPrefix: String,
    onMinutesChanged: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = PausaTextPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        onMinutesChanged((currentMinutes - stepMinutes).coerceAtLeast(minMinutes))
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_decrement_${tagPrefix}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PausaCardLight)
                            .border(1.dp, PausaCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Disminuir minutos",
                            tint = PausaTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "$currentMinutes min",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PausaDarkGreen,
                    modifier = Modifier.testTag("text_${tagPrefix}_minutes")
                )

                IconButton(
                    onClick = {
                        onMinutesChanged((currentMinutes + stepMinutes).coerceAtMost(maxMinutes))
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_increment_${tagPrefix}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PausaCardLight)
                            .border(1.dp, PausaCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Aumentar minutos",
                            tint = PausaTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { presetMin ->
                val selected = currentMinutes == presetMin
                Surface(
                    color = if (selected) PausaDarkGreen else PausaCreamBackground,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (selected) PausaDarkGreen else PausaCardBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onMinutesChanged(presetMin) }
                        .testTag("chip_${tagPrefix}_$presetMin")
                ) {
                    Text(
                        text = "$presetMin min",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else PausaTextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

private fun formatIntervalSeconds(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
