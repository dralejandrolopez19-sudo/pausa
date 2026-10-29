package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PausaDestination
import com.example.ui.theme.PausaCardBorder
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDarkGreen
import com.example.ui.theme.PausaDeepForest
import com.example.ui.theme.PausaSageLight
import com.example.ui.theme.PausaTextMuted

/**
 * Custom vector Canvas rendering of the exact PAUSA logo:
 * Rounded dark green squircle with two vertical cream pause bars and a sprouting leaf at the bottom-right.
 */
@Composable
fun PausaLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Squircle background
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF234F3B), Color(0xFF163527))
            ),
            cornerRadius = CornerRadius(w * 0.28f, h * 0.28f),
            size = Size(w, h)
        )

        val barWidth = w * 0.115f
        val barRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

        // Left pause bar
        drawRoundRect(
            color = Color(0xFFF6F4EE),
            topLeft = Offset(w * 0.31f, h * 0.26f),
            size = Size(barWidth, h * 0.48f),
            cornerRadius = barRadius
        )

        // Right pause bar
        drawRoundRect(
            color = Color(0xFFF6F4EE),
            topLeft = Offset(w * 0.54f, h * 0.26f),
            size = Size(barWidth, h * 0.42f),
            cornerRadius = barRadius
        )

        // Sprouting leaf at bottom-right
        val leafPath = Path().apply {
            moveTo(w * 0.52f, h * 0.74f)
            cubicTo(
                w * 0.52f, h * 0.58f,
                w * 0.66f, h * 0.50f,
                w * 0.80f, h * 0.52f
            )
            cubicTo(
                w * 0.80f, h * 0.68f,
                w * 0.67f, h * 0.76f,
                w * 0.52f, h * 0.74f
            )
            close()
        }
        drawPath(path = leafPath, color = Color(0xFF88C999))
    }
}

/**
 * Custom Canvas illustration for the 5 levels of the Plant System ("El sistema de plantas"):
 * Level 1: Semilla (Seed / tiny sprout)
 * Level 2: Brote (2 small leaves)
 * Level 3: En crecimiento (4-6 lush leaves, hero illustration on Screen 5)
 * Level 4: Floreciendo (Lush plant with yellow blossoms)
 * Level 5: Árbol (Full leafy tree canopy in pot)
 */
@Composable
fun PlantIllustration(
    level: Int,
    modifier: Modifier = Modifier,
    showAuraCircle: Boolean = false,
    isWilted: Boolean = false,
    continuousGrowthStage: Float? = null,
    enableDragRotation: Boolean = false
) {
    Plant3DIllustration(
        targetGrowthStage = continuousGrowthStage ?: level.coerceIn(1, 5).toFloat(),
        modifier = modifier,
        showAuraCircle = showAuraCircle,
        isWilted = isWilted,
        enableAutoTurntable = true,
        enableDragRotation = enableDragRotation
    )
}

/**
 * Custom Coins & Dollar Penalty Badge matching Screen 5 ("Si rompes la pausa... $1.00")
 */
@Composable
fun CoinStackIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Soft sage circle backdrop
        drawCircle(
            color = PausaSageLight,
            radius = w * 0.48f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Stack of 3 dark green coins
        val coinW = w * 0.52f
        val coinH = h * 0.20f
        val startX = (w - coinW) / 2f

        for (i in 2 downTo 0) {
            val yOffset = h * (0.30f + i * 0.14f)
            drawRoundRect(
                color = PausaDarkGreen,
                topLeft = Offset(startX, yOffset),
                size = Size(coinW, coinH),
                cornerRadius = CornerRadius(coinH / 2f, coinH / 2f)
            )
            drawRoundRect(
                color = Color(0xFF9AC5A9),
                topLeft = Offset(startX + coinW * 0.08f, yOffset + coinH * 0.15f),
                size = Size(coinW * 0.84f, coinH * 0.45f),
                cornerRadius = CornerRadius(coinH / 3f, coinH / 3f),
                style = Stroke(width = 2f)
            )
        }
    }
}

/**
 * Branded Social App Icon for Screen 2 ("Límites") matching Instagram, TikTok, YouTube, Facebook, X, WhatsApp
 */
@Composable
fun SocialAppBadge(
    iconType: String,
    brandColorHex: Long,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(brandColorHex)
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(
                brush = if (iconType == "instagram") {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF58529),
                            Color(0xFFDD2A7B),
                            Color(0xFF8134AF)
                        )
                    )
                } else {
                    Brush.linearGradient(colors = listOf(bgColor, bgColor))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (iconType) {
            "instagram" -> {
                Canvas(modifier = Modifier.size(22.dp)) {
                    drawRoundRect(
                        color = Color.White,
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.5.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.3.dp.toPx(),
                        center = Offset(size.width * 0.76f, size.height * 0.24f)
                    )
                }
            }
            "tiktok" -> {
                Text(
                    text = "♪",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            "youtube" -> {
                Canvas(modifier = Modifier.size(20.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.28f, size.height * 0.2f)
                        lineTo(size.width * 0.82f, size.height * 0.5f)
                        lineTo(size.width * 0.28f, size.height * 0.8f)
                        close()
                    }
                    drawPath(path, color = Color.White)
                }
            }
            "facebook" -> {
                Text(
                    text = "f",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            "x" -> {
                Text(
                    text = "𝕏",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            "whatsapp" -> {
                Canvas(modifier = Modifier.size(22.dp)) {
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension * 0.44f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.35f, size.height * 0.38f),
                        end = Offset(size.width * 0.65f, size.height * 0.64f),
                        strokeWidth = 2.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            else -> {
                Text(
                    text = iconType.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 5-Tab Bottom Navigation Bar matching the exact tabs in the reference image:
 * Inicio | Límites | Pausa | Progreso | Ajustes
 * Automatically adapts to dark mode when on Modo Pausa or Pausa Rota screens.
 */
@Composable
fun PausaBottomNavigationBar(
    currentDestination: PausaDestination,
    onNavigate: (PausaDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkScreen = currentDestination == PausaDestination.PAUSA ||
        currentDestination == PausaDestination.PAUSA_ROTA

    val containerColor = if (isDarkScreen) PausaDeepForest else PausaCreamBackground
    val activeColor = if (isDarkScreen) Color(0xFF9CD3AE) else PausaDarkGreen
    val inactiveColor = if (isDarkScreen) Color(0xFF7B9B89) else PausaTextMuted
    val dividerColor = if (isDarkScreen) Color(0xFF234736) else PausaCardBorder

    val items = listOf(
        Triple(PausaDestination.INICIO, "Inicio", Pair(Icons.Filled.Home, Icons.Outlined.Home)),
        Triple(PausaDestination.LIMITES, "Límites", Pair(Icons.Filled.Schedule, Icons.Outlined.Schedule)),
        Triple(PausaDestination.PAUSA, "Pausa", Pair(Icons.Filled.Pause, Icons.Outlined.Pause)),
        Triple(PausaDestination.PROGRESO, "Progreso", Pair(Icons.Filled.BarChart, Icons.Outlined.BarChart)),
        Triple(PausaDestination.AJUSTES, "Ajustes", Pair(Icons.Filled.Settings, Icons.Outlined.Settings))
    )

    Surface(
        color = containerColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dividerColor)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (dest, label, icons) ->
                    val isSelected = currentDestination == dest ||
                        (currentDestination == PausaDestination.TU_PLANTA && dest == PausaDestination.PROGRESO)
                    val color = if (isSelected) activeColor else inactiveColor

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigate(dest) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("nav_${label.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected && isDarkScreen) Color(0xFF285741)
                                    else if (isSelected) PausaSageLight
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) icons.first else icons.second,
                                contentDescription = label,
                                tint = color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = color
                        )
                    }
                }
            }
        }
    }
}
