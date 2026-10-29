package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PausaDestination
import com.example.ui.PausaUiState
import com.example.ui.components.PausaLogoBadge
import com.example.ui.components.PlantIllustration
import com.example.ui.theme.PausaAlertRed
import com.example.ui.theme.PausaCardBorder
import com.example.ui.theme.PausaCardLight
import com.example.ui.theme.PausaCardSurface
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDarkGreen
import com.example.ui.theme.PausaFlameOrange
import com.example.ui.theme.PausaSageLight
import com.example.ui.theme.PausaTextMuted
import com.example.ui.theme.PausaTextPrimary
import com.example.ui.theme.PausaTextSecondary
import com.example.ui.theme.isPausaDarkThemeActive
import java.util.Locale

@Composable
fun InicioScreen(
    uiState: PausaUiState,
    onNeedPauseClick: () -> Unit,
    onPlantCardClick: () -> Unit,
    onMetricClick: (PausaDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = uiState.stats
    val usedHours = stats.usedMinutesToday / 60
    val usedRemMin = stats.usedMinutesToday % 60
    val goalHours = stats.dailyGoalMinutes / 60
    val goalRemMin = stats.dailyGoalMinutes % 60
    val progressFraction = (stats.usedMinutesToday.toFloat() / stats.dailyGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)

    val recoveredH = stats.recoveredMinutes / 60
    val recoveredM = stats.recoveredMinutes % 60
    val activePlant = uiState.activePlant

    val isBlockerActive = uiState.isAccessibilityBlockerEnabled ||
        (uiState.hasUsageStatsPermission && uiState.hasOverlayPermission)
    val enabledAppsCount = uiState.appLimits.count { it.isEnabled }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PausaCreamBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 1. Minimalist Top Header: PAUSA Logo + Plant Level Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PausaLogoBadge(size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PAUSA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = PausaDarkGreen,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Menos scroll. Más vida.",
                        style = MaterialTheme.typography.labelSmall,
                        color = PausaTextSecondary
                    )
                }
            }

            Surface(
                color = PausaSageLight,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onPlantCardClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Eco,
                        contentDescription = "Nivel de planta",
                        tint = PausaDarkGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Nivel ${activePlant?.level ?: 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PausaDarkGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 2. Calm Greeting + Subtle Single-Line Shield Pill
        Text(
            text = if (stats.userName.isNotBlank()) "Hola, ${stats.userName}" else "Hola",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = PausaTextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Hoy es un buen día para estar presente",
            style = MaterialTheme.typography.bodyMedium,
            color = PausaTextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Compact single-line shield status pill (no multi-line visual noise)
        val alertSurfaceColor = if (isPausaDarkThemeActive) Color(0xFF331C1C) else Color(0xFFFCE8E6)
        Surface(
            color = if (isBlockerActive) PausaSageLight else alertSurfaceColor,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isBlockerActive) PausaCardBorder else PausaAlertRed.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable { onMetricClick(PausaDestination.LIMITES) }
                .testTag("banner_estado_bloqueador_inicio")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isBlockerActive) PausaDarkGreen else PausaAlertRed)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBlockerActive) {
                            "Escudo activo · $enabledAppsCount redes protegidas"
                        } else {
                            "Activar permiso de bloqueo en Límites"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isBlockerActive) PausaTextPrimary else PausaAlertRed
                    )
                }
                Icon(
                    imageVector = if (isBlockerActive) Icons.Filled.Shield else Icons.Filled.ChevronRight,
                    contentDescription = "Estado del bloqueo",
                    tint = if (isBlockerActive) PausaDarkGreen else PausaAlertRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Primary Card: "Tiempo hoy"
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(22.dp))
                .clickable { onMetricClick(PausaDestination.LIMITES) }
                .testTag("card_tiempo_hoy")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tiempo hoy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PausaTextSecondary
                    )
                    Text(
                        text = "Meta: $goalHours h ${String.format(Locale.US, "%02d", goalRemMin)} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = PausaTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$usedHours h ${String.format(Locale.US, "%02d", usedRemMin)} min",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = PausaTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(PausaCardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(PausaDarkGreen)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Clean 3-Metric Summary Row: Racha | Tiempo recuperado | Ahorro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Racha
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(104.dp)
                    .border(1.dp, PausaCardBorder, RoundedCornerShape(18.dp))
                    .clickable { onMetricClick(PausaDestination.PROGRESO) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Racha",
                        style = MaterialTheme.typography.labelSmall,
                        color = PausaTextSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Racha",
                            tint = PausaFlameOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stats.streakDays} días",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PausaTextPrimary
                        )
                    }
                    Text(
                        text = "constante",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = PausaTextMuted
                    )
                }
            }

            // Card 2: Recuperado
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .height(104.dp)
                    .border(1.dp, PausaCardBorder, RoundedCornerShape(18.dp))
                    .clickable { onMetricClick(PausaDestination.PROGRESO) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recuperado",
                        style = MaterialTheme.typography.labelSmall,
                        color = PausaTextSecondary
                    )
                    Text(
                        text = "${recoveredH}h ${recoveredM}m",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PausaTextPrimary
                    )
                    Text(
                        text = "sin pantallas",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = PausaTextMuted
                    )
                }
            }

            // Card 3: Ahorro
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(104.dp)
                    .border(1.dp, PausaCardBorder, RoundedCornerShape(18.dp))
                    .clickable { onPlantCardClick() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ahorro",
                        style = MaterialTheme.typography.labelSmall,
                        color = PausaTextSecondary
                    )
                    Text(
                        text = "$ ${String.format(Locale.US, "%.2f", stats.savedMoneyDollars)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PausaTextPrimary
                    )
                    Text(
                        text = "protegido",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = PausaTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. "Tu planta" Card: Serene preview & direct access to the 3D garden
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(20.dp))
                .clickable { onPlantCardClick() }
                .testTag("card_tu_planta_inicio")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PausaCardLight),
                    contentAlignment = Alignment.Center
                ) {
                    PlantIllustration(
                        level = activePlant?.level ?: 1,
                        modifier = Modifier.size(50.dp),
                        showAuraCircle = false
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val plantLevel = activePlant?.level ?: 1
                    val plantProgress = activePlant?.progressPercent ?: 35
                    Text(
                        text = activePlant?.name ?: "Tu planta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PausaTextPrimary
                    )
                    Text(
                        text = "Nivel $plantLevel · ${activePlant?.levelName ?: "Semilla"} ($plantProgress%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PausaDarkGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(PausaCardBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((plantProgress / 100f).coerceIn(0.10f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(PausaDarkGreen)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Ver tu planta",
                    tint = PausaTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 6. Single Unified Pause Action Button ("Necesito una pausa")
        Button(
            onClick = onNeedPauseClick,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PausaDarkGreen,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("btn_necesito_una_pausa")
        ) {
            Icon(
                imageVector = Icons.Filled.Pause,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Necesito una pausa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
