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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PausaUiState
import com.example.ui.components.CoinStackIllustration
import com.example.ui.components.Plant3DIllustration
import com.example.ui.components.PlantIllustration
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
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun TuPlantaScreen(
    uiState: PausaUiState,
    onBackClick: () -> Unit,
    onSelectPlant: (Int) -> Unit,
    onDismissAlternativeBanner: () -> Unit,
    onTopUpWallet: (Double) -> Unit,
    onNurturePlant: () -> Unit = {},
    onSaveEvolutionStage: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val activePlant = uiState.activePlant
    val dbLevel = activePlant?.level ?: 1
    val dbProgressPercent = activePlant?.progressPercent ?: 35

    // Continuous 3D growth stage (1.0f = Semilla .. 5.0f = Árbol)
    val baseStageFromDb = (dbLevel.toFloat() + (dbProgressPercent / 100f) * 0.85f).coerceIn(1f, 5f)
    var interactiveGrowthStage by remember(dbLevel, dbProgressPercent) {
        mutableFloatStateOf(baseStageFromDb)
    }
    var isPlaying3DEvolution by remember { mutableStateOf(false) }
    var showMyPlantsModal by remember { mutableStateOf(false) }
    var wateredFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // Continuous 3D evolution playback when user taps play
    LaunchedEffect(isPlaying3DEvolution) {
        if (isPlaying3DEvolution) {
            interactiveGrowthStage = 1.0f
            val totalSteps = 140
            for (step in 0..totalSteps) {
                if (!isPlaying3DEvolution) break
                interactiveGrowthStage = 1.0f + (step.toFloat() / totalSteps) * 4.0f
                delay(40L)
            }
            delay(600L)
            isPlaying3DEvolution = false
        }
    }

    val displayedLevel = floor(interactiveGrowthStage).toInt().coerceIn(1, 5)
    val displayedLevelName = when (displayedLevel) {
        1 -> "Semilla"
        2 -> "Brote"
        3 -> "En crecimiento"
        4 -> "Floreciendo"
        else -> "Árbol"
    }
    val displayedProgress = if (isPlaying3DEvolution || interactiveGrowthStage != baseStageFromDb) {
        (((interactiveGrowthStage - 1f) / 4f) * 100f).roundToInt().coerceIn(0, 100)
    } else {
        dbProgressPercent
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PausaCreamBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Top App Bar: <- Tu planta + 3D Interactivo badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = PausaTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tu planta",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PausaTextPrimary
                )
            }

            Surface(
                color = PausaSageLight,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ViewInAr,
                        contentDescription = "3D Interactivo",
                        tint = PausaDarkGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "3D Interactivo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PausaDarkGreen
                    )
                }
            }
        }

        // Optional toast banner if the user just chose a healthy alternative activity
        uiState.selectedAlternativeActivity?.let { activity ->
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = PausaSageLight,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismissAlternativeBanner() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Eco,
                        contentDescription = null,
                        tint = PausaDarkGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¡Excelente elección! ($activity). Tu planta recibió energía extra.",
                        style = MaterialTheme.typography.labelSmall,
                        color = PausaDarkGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large 3D Hero Plant Illustration inside Soft Sage Aura Circle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(236.dp),
            contentAlignment = Alignment.Center
        ) {
            Plant3DIllustration(
                targetGrowthStage = interactiveGrowthStage,
                modifier = Modifier
                    .size(215.dp)
                    .testTag("hero_plant_3d_canvas"),
                showAuraCircle = true,
                enableAutoTurntable = true,
                enableDragRotation = true
            )

            Surface(
                color = PausaCardLight.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .border(1.dp, PausaCardBorder, RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "Arrastra horizontalmente para girar en 360°",
                    style = MaterialTheme.typography.labelSmall,
                    color = PausaTextSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Level & 3D Evolution Play Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nivel $displayedLevel · $displayedLevelName",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = PausaTextPrimary
            )

            Surface(
                color = if (isPlaying3DEvolution) PausaDarkGreen else PausaSageLight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { isPlaying3DEvolution = !isPlaying3DEvolution }
                    .testTag("btn_play_3d_evolution")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPlaying3DEvolution) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = if (isPlaying3DEvolution) Color.White else PausaDarkGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPlaying3DEvolution) "Detener evolución" else "Ver evolución 3D (Niv. 1 → 5)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying3DEvolution) Color.White else PausaDarkGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(PausaCardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((displayedProgress / 100f).coerceIn(0.08f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(PausaDarkGreen)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$displayedProgress%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PausaTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Plant Care Action: "Regar y nutrir mi planta (+25% crecimiento)"
        Button(
            onClick = {
                isPlaying3DEvolution = false
                onNurturePlant()
                wateredFeedbackMessage = "¡Planta nutrida! +25% de crecimiento y +10 min guardados en tu cuenta."
            },
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PausaDarkGreen,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_regar_planta")
        ) {
            Icon(
                imageVector = Icons.Filled.Eco,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Regar y nutrir ${activePlant?.name ?: "tu planta"} (+25% crecimiento)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        wateredFeedbackMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = PausaSageLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { wateredFeedbackMessage = null }
                    .testTag("badge_planta_nutrida")
            ) {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = PausaDarkGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card: "Si rompes la pausa..." ($1.00 por cada vez)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(18.dp))
                .clickable { onTopUpWallet(5.0) }
                .testTag("card_saldo_digital_tu_planta")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Si rompes la pausa...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PausaTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinStackIllustration(size = 46.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = String.format(Locale.US, "$%.2f", uiState.stats.penaltyPerBreak),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PausaTextPrimary
                        )
                        Text(
                            text = "por cada vez",
                            style = MaterialTheme.typography.labelSmall,
                            color = PausaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "La penalidad se cobra automáticamente y se descuenta de tu saldo PayPal (disponible: $${String.format(Locale.US, "%.2f", uiState.stats.digitalWalletBalance)} USD).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PausaTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onTopUpWallet(5.0) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0070BA),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_recargar_paypal_planta")
                ) {
                    Text(
                        text = "Recargar saldo vía PayPal",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Button Row Card: "Ver mis plantas >"
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(16.dp))
                .clickable { showMyPlantsModal = true }
                .testTag("btn_ver_mis_plantas")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver mis plantas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PausaTextPrimary
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Ver mis plantas",
                    tint = PausaTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Full Card: "El sistema de plantas (Evolución 3D)" directly visible on the screen
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCardLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(20.dp))
                .testTag("card_sistema_plantas_3d")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "El sistema de plantas (Evolución 3D)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PausaTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tu progreso se ve y se siente del Nivel 1 al Nivel 5.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PausaTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Desliza el control o toca cualquier etapa para ver cómo tu planta crece en 3D desde una pequeña semilla hasta un árbol frondoso:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PausaTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = interactiveGrowthStage,
                    onValueChange = {
                        isPlaying3DEvolution = false
                        interactiveGrowthStage = it
                    },
                    onValueChangeFinished = {
                        val savedLevel = floor(interactiveGrowthStage).toInt().coerceIn(1, 5)
                        val savedPct = (((interactiveGrowthStage - 1f) / 4f) * 100f).roundToInt().coerceIn(15, 100)
                        onSaveEvolutionStage(savedLevel, savedPct)
                    },
                    valueRange = 1f..5f,
                    colors = SliderDefaults.colors(
                        thumbColor = PausaDarkGreen,
                        activeTrackColor = PausaDarkGreen,
                        inactiveTrackColor = PausaCardBorder
                    ),
                    modifier = Modifier.testTag("slider_evolucion_planta_3d")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5-Stage Visual Evolution Row (Nivel 1 Semilla -> Nivel 5 Árbol)
                val stages = listOf(
                    Triple(1, "Nivel 1", "Semilla"),
                    Triple(2, "Nivel 2", "Brote"),
                    Triple(3, "Nivel 3", "En crecimiento"),
                    Triple(4, "Nivel 4", "Floreciendo"),
                    Triple(5, "Nivel 5", "Árbol")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    stages.forEach { (lvl, nLabel, stageName) ->
                        val isSelected = displayedLevel == lvl
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PausaSageLight else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) PausaDarkGreen else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    isPlaying3DEvolution = false
                                    interactiveGrowthStage = lvl.toFloat()
                                    val matching = uiState.plants.firstOrNull { it.level == lvl }
                                    if (matching != null) {
                                        onSelectPlant(matching.id)
                                    } else {
                                        onSaveEvolutionStage(lvl, (lvl * 20).coerceIn(25, 100))
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 2.dp)
                                .testTag("stage_planta_$lvl")
                        ) {
                            PlantIllustration(
                                level = lvl,
                                modifier = Modifier.size(46.dp),
                                showAuraCircle = false
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isSelected) PausaDarkGreen else PausaTextPrimary
                            )
                            Text(
                                text = stageName,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.5.sp,
                                color = PausaTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    // Modal when tapping "Ver mis plantas"
    if (showMyPlantsModal) {
        AlertDialog(
            onDismissRequest = { showMyPlantsModal = false },
            containerColor = PausaCardLight,
            title = {
                Column {
                    Text(
                        text = "Mis plantas coleccionadas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PausaTextPrimary
                    )
                    Text(
                        text = "Selecciona cuál planta cuidar en tu jardín consciente",
                        style = MaterialTheme.typography.labelSmall,
                        color = PausaTextSecondary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.plants.forEach { plant ->
                        Surface(
                            color = if (plant.isCurrent) PausaSageLight else PausaCreamBackground,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (plant.isCurrent) PausaDarkGreen else PausaCardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    isPlaying3DEvolution = false
                                    interactiveGrowthStage = plant.level.toFloat()
                                    onSelectPlant(plant.id)
                                    showMyPlantsModal = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PlantIllustration(
                                    level = plant.level,
                                    modifier = Modifier.size(44.dp),
                                    showAuraCircle = false
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plant.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = PausaTextPrimary
                                    )
                                    Text(
                                        text = "Nivel ${plant.level} · ${plant.levelName} (${plant.progressPercent}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PausaDarkGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = plant.unlockedBenefit,
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
                Button(
                    onClick = { showMyPlantsModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PausaDarkGreen)
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
