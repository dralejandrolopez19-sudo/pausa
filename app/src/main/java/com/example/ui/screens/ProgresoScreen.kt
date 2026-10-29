package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyUsageBar
import com.example.ui.PausaUiState
import com.example.ui.ProgresoPeriod
import com.example.ui.theme.PausaAccentGreen
import com.example.ui.theme.PausaCardBorder
import com.example.ui.theme.PausaCardLight
import com.example.ui.theme.PausaCardSurface
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDarkGreen
import com.example.ui.theme.PausaFlameOrange
import com.example.ui.theme.PausaMediumGreen
import com.example.ui.theme.PausaSageLight
import com.example.ui.theme.PausaTextMuted
import com.example.ui.theme.PausaTextPrimary
import com.example.ui.theme.PausaTextSecondary
import java.util.Locale
import kotlin.math.roundToInt

data class PausaStreakBadge(
    val requiredDays: Int,
    val title: String,
    val subtitle: String,
    val rewardDescription: String,
    val icon: ImageVector,
    val accentColor: Color
)

private val streakBadgesCatalog = listOf(
    PausaStreakBadge(
        requiredDays = 1,
        title = "Primer Paso",
        subtitle = "1 día",
        rewardDescription = "Completaste tu primer día usando menos las redes y respetando tus pausas.",
        icon = Icons.Filled.Eco,
        accentColor = Color(0xFF4C8C64)
    ),
    PausaStreakBadge(
        requiredDays = 3,
        title = "Brote Firme",
        subtitle = "3 días",
        rewardDescription = "3 días seguidos reduciendo el scroll impulsivo. Tu atención empieza a fortalecerse.",
        icon = Icons.Filled.Spa,
        accentColor = Color(0xFF2D5E46)
    ),
    PausaStreakBadge(
        requiredDays = 5,
        title = "Racha Consciente",
        subtitle = "5 días",
        rewardDescription = "5 días cumpliendo tu meta diaria en redes. ¡Tu planta crece con energía!",
        icon = Icons.Filled.LocalFireDepartment,
        accentColor = PausaFlameOrange
    ),
    PausaStreakBadge(
        requiredDays = 14,
        title = "Mente Clara",
        subtitle = "14 días",
        rewardDescription = "Dos semanas de uso consciente y equilibrio digital sostenido.",
        icon = Icons.Filled.WbSunny,
        accentColor = Color(0xFFD99B26)
    ),
    PausaStreakBadge(
        requiredDays = 30,
        title = "Maestro Pausa",
        subtitle = "30 días",
        rewardDescription = "Un mes entero dominando tu tiempo y convirtiendo el scroll en vida real.",
        icon = Icons.Filled.EmojiEvents,
        accentColor = Color(0xFF1E4332)
    )
)

private val last7DaysTrend = listOf(
    DailyUsageBar("Lun", 195),
    DailyUsageBar("Mar", 168),
    DailyUsageBar("Mié", 142),
    DailyUsageBar("Jue", 134),
    DailyUsageBar("Vie", 122),
    DailyUsageBar("Sáb", 110),
    DailyUsageBar("Dom", 102)
)

@Composable
fun ProgresoScreen(
    uiState: PausaUiState,
    onSelectPeriod: (ProgresoPeriod) -> Unit,
    onOpenTuPlantaClick: () -> Unit,
    onUpdateDailyGoal: (Int) -> Unit = {},
    onAdvanceStreakDay: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stats = uiState.stats
    val recoveredH = stats.recoveredMinutes / 60
    val recoveredM = stats.recoveredMinutes % 60

    var goalSlider by remember(stats.dailyGoalMinutes) {
        mutableFloatStateOf(stats.dailyGoalMinutes.toFloat())
    }
    var selectedBadgeDetail by remember { mutableStateOf<PausaStreakBadge?>(null) }

    val unlockedCount = streakBadgesCatalog.count { stats.streakDays >= it.requiredDays }
    val nextBadge = streakBadgesCatalog.firstOrNull { stats.streakDays < it.requiredDays }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PausaCreamBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Progreso",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = PausaTextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented Pill Tabs: Semana | Mes | Total
        Surface(
            color = PausaCardSurface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val periods = listOf(
                    ProgresoPeriod.SEMANA to "Semana",
                    ProgresoPeriod.MES to "Mes",
                    ProgresoPeriod.TOTAL to "Total"
                )
                periods.forEach { (period, label) ->
                    val selected = uiState.selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) PausaDarkGreen else Color.Transparent)
                            .clickable { onSelectPeriod(period) }
                            .padding(vertical = 8.dp)
                            .testTag("tab_progreso_${label.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else PausaTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Single Clean Unified Progress Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(20.dp))
                .testTag("card_progreso_minimal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // =========================================================================
                // 1. Tiempo en redes + Interactive Bar Chart with Goal Line & Tooltip
                // =========================================================================
                UsageBarChart(
                    bars = uiState.usageBars,
                    dailyGoalMinutes = goalSlider.roundToInt(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PausaCardBorder)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // =========================================================================
                // 2. Minutos recuperados | Dinero ahorrado
                // =========================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Minutos recuperados",
                            style = MaterialTheme.typography.labelSmall,
                            color = PausaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$recoveredH h $recoveredM min",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PausaTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(PausaCardBorder)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dinero ahorrado",
                            style = MaterialTheme.typography.labelSmall,
                            color = PausaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$ ${String.format(Locale.US, "%.2f", stats.savedMoneyDollars)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PausaDarkGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PausaCardBorder)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // =========================================================================
                // 3. Meta diaria de uso en redes
                // =========================================================================
                val gH = goalSlider.roundToInt() / 60
                val gM = goalSlider.roundToInt() % 60

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Meta diaria en redes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PausaTextPrimary
                    )
                    Text(
                        text = "$gH h ${String.format(Locale.US, "%02d", gM)} min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PausaDarkGreen
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = goalSlider,
                    onValueChange = { goalSlider = it },
                    onValueChangeFinished = { onUpdateDailyGoal(goalSlider.roundToInt()) },
                    valueRange = 60f..300f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = PausaDarkGreen,
                        activeTrackColor = PausaDarkGreen,
                        inactiveTrackColor = PausaCardBorder
                    ),
                    modifier = Modifier.testTag("slider_meta_diaria")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PausaCardBorder)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // =========================================================================
                // 4. Sistema de Insignias + Curva de Tendencia de los últimos 7 días
                // =========================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Insignias de uso consciente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PausaTextPrimary
                        )
                        Text(
                            text = "${stats.streakDays} días logrando usar menos las redes · $unlockedCount/${streakBadgesCatalog.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PausaTextSecondary
                        )
                    }

                    Surface(
                        color = PausaSageLight,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clickable { onAdvanceStreakDay(1) }
                            .testTag("btn_sumar_dia_racha")
                    ) {
                        Text(
                            text = "+1 día",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PausaDarkGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5 Minimalist Badges Row (1d, 3d, 5d, 14d, 30d)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    streakBadgesCatalog.forEach { badge ->
                        val isUnlocked = stats.streakDays >= badge.requiredDays
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedBadgeDetail = badge }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .testTag("badge_${badge.requiredDays}d")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isUnlocked) PausaDarkGreen else Color(0xFFE2DFD5)
                                    )
                                    .border(
                                        width = if (isUnlocked) 2.dp else 1.dp,
                                        color = if (isUnlocked) badge.accentColor else PausaCardBorder,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isUnlocked) badge.icon else Icons.Filled.Lock,
                                    contentDescription = badge.title,
                                    tint = if (isUnlocked) Color.White else PausaTextMuted,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = badge.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.5.sp,
                                lineHeight = 11.sp,
                                fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Medium,
                                color = if (isUnlocked) PausaTextPrimary else PausaTextMuted,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = badge.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (isUnlocked) PausaDarkGreen else PausaTextMuted,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Bezier Area Sparkline Chart for Last 7 Days Trend
                SevenDayScreenTimeTrendWidget(
                    bars = last7DaysTrend,
                    dailyGoalMinutes = goalSlider.roundToInt(),
                    streakDays = stats.streakDays,
                    nextBadge = nextBadge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PausaCardBorder)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // =========================================================================
                // 5. Link to Tu planta 3D
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenTuPlantaClick() }
                        .padding(vertical = 4.dp)
                        .testTag("card_logro_racha"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PausaDarkGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Racha de ${stats.streakDays} días",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PausaTextPrimary
                        )
                        Text(
                            text = "Ver evolución 3D de tu planta",
                            style = MaterialTheme.typography.labelSmall,
                            color = PausaTextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Ver tu planta",
                        tint = PausaTextSecondary
                    )
                }
            }
        }
    }

    // Detail dialog when user taps any badge
    selectedBadgeDetail?.let { badge ->
        val isUnlocked = stats.streakDays >= badge.requiredDays
        AlertDialog(
            onDismissRequest = { selectedBadgeDetail = null },
            containerColor = PausaCardLight,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isUnlocked) PausaDarkGreen else Color(0xFFE2DFD5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badge.icon,
                            contentDescription = null,
                            tint = if (isUnlocked) Color.White else PausaTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = badge.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PausaTextPrimary
                        )
                        Text(
                            text = if (isUnlocked) "Desbloqueada · ${badge.subtitle}" else "Bloqueada · Requiere ${badge.subtitle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUnlocked) PausaDarkGreen else PausaTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            text = {
                Text(
                    text = badge.rewardDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PausaTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedBadgeDetail = null }) {
                    Text("Cerrar", color = PausaDarkGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Interactive & Animated Bar Chart for "Tiempo en redes":
 * - Smooth entry growth animation
 * - Tappable bars to inspect exact time per day/week/month
 * - Dashed Daily Goal reference line
 * - Capsule background tracks + vertical gradient bars
 */
@Composable
private fun UsageBarChart(
    bars: List<DailyUsageBar>,
    dailyGoalMinutes: Int,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(bars) { mutableIntStateOf(bars.lastIndex.coerceAtLeast(0)) }
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(bars) {
        triggered = false
        triggered = true
    }
    val animProgress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "bar_chart_entry"
    )

    val selectedBar = bars.getOrNull(selectedIndex) ?: bars.lastOrNull()
    val selHours = (selectedBar?.minutes ?: 0) / 60
    val selMins = (selectedBar?.minutes ?: 0) % 60
    val isSelectedUnderGoal = (selectedBar?.minutes ?: 0) <= dailyGoalMinutes

    Column(modifier = modifier) {
        // Header + Dynamic Selected Bar Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tiempo en redes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PausaTextPrimary
                )
                if (selectedBar != null) {
                    Text(
                        text = "${selectedBar.dayLabel}: ${selHours}h ${String.format(Locale.US, "%02d", selMins)}m ${if (isSelectedUnderGoal) "· Bajo tu meta" else "· Sobre tu meta"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelectedUnderGoal) PausaDarkGreen else PausaFlameOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(
                color = PausaSageLight,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = PausaDarkGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "37% vs. ant.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PausaDarkGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Y-Axis Labels (4h, 2h, 0)
            Column(
                modifier = Modifier
                    .height(132.dp)
                    .padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text("4h", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = PausaTextMuted)
                Text("2h", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = PausaTextMuted)
                Text("0", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = PausaTextMuted)
            }

            // Interactive Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(132.dp)
                    .pointerInput(bars) {
                        detectTapGestures { offset ->
                            val count = bars.size.coerceAtLeast(1)
                            val slotW = size.width.toFloat() / count
                            val tappedIdx = (offset.x / slotW).toInt().coerceIn(0, count - 1)
                            selectedIndex = tappedIdx
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val h = size.height
                    val w = size.width
                    val maxMin = 240f // 4h

                    // Subtle horizontal grid lines
                    val gridColor = Color(0xFFDCD9CF)
                    listOf(0.06f, 0.50f, 0.94f).forEach { frac ->
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, h * frac),
                            end = Offset(w, h * frac),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Dashed Daily Goal Reference Line
                    val goalRatio = (dailyGoalMinutes / maxMin).coerceIn(0.15f, 0.95f)
                    val goalY = h * 0.94f - (h * 0.88f * goalRatio)
                    drawLine(
                        color = PausaAccentGreen,
                        start = Offset(0f, goalY),
                        end = Offset(w, goalY),
                        strokeWidth = 1.6.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                    )

                    val count = bars.size.coerceAtLeast(1)
                    val slotWidth = w / count
                    val barWidth = (slotWidth * 0.44f).coerceAtMost(22.dp.toPx())
                    val chartBottom = h * 0.94f
                    val chartTop = h * 0.06f
                    val maxBarH = chartBottom - chartTop

                    bars.forEachIndexed { idx, bar ->
                        val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                        val isSelected = idx == selectedIndex
                        val isUnderGoal = bar.minutes <= dailyGoalMinutes

                        // Full-height subtle capsule track behind each bar
                        drawRoundRect(
                            color = if (isSelected) Color(0xFFD5E1D8) else Color(0xFFE3E0D6),
                            topLeft = Offset(left, chartTop),
                            size = Size(barWidth, maxBarH),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )

                        // Animated active bar
                        val ratio = ((bar.minutes / maxMin).coerceIn(0.08f, 1f)) * animProgress
                        val barH = maxBarH * ratio
                        val top = chartBottom - barH

                        val barBrush = when {
                            isSelected && isUnderGoal -> Brush.verticalGradient(
                                colors = listOf(PausaAccentGreen, PausaDarkGreen),
                                startY = top,
                                endY = chartBottom
                            )
                            isSelected && !isUnderGoal -> Brush.verticalGradient(
                                colors = listOf(PausaFlameOrange, Color(0xFFB85624)),
                                startY = top,
                                endY = chartBottom
                            )
                            isUnderGoal -> Brush.verticalGradient(
                                colors = listOf(PausaMediumGreen, PausaDarkGreen),
                                startY = top,
                                endY = chartBottom
                            )
                            else -> Brush.verticalGradient(
                                colors = listOf(Color(0xFF9CA89F), Color(0xFF7B8A81)),
                                startY = top,
                                endY = chartBottom
                            )
                        }

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )

                        // Small glowing dot indicator on top of the selected bar
                        if (isSelected) {
                            drawCircle(
                                color = if (isUnderGoal) PausaDarkGreen else PausaFlameOrange,
                                radius = 3.5.dp.toPx(),
                                center = Offset(left + barWidth / 2f, (top - 6.dp.toPx()).coerceAtLeast(4.dp.toPx()))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis Day Labels (Tappable & Highlighted when selected)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            bars.forEachIndexed { idx, bar ->
                val isSelected = idx == selectedIndex
                Text(
                    text = bar.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) PausaDarkGreen else PausaTextSecondary,
                    fontSize = 10.5.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedIndex = idx }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Smooth Cubic Bezier Area Sparkline Chart for the Last 7 Days Reduced Screen Time Trend:
 * - Renders a continuous organic downward curve with a soft sage-green gradient area fill underneath
 * - Interactive data points (hollow/filled nodes) that show each day's exact minutes when tapped
 * - Dashed Daily Goal threshold line
 * - Segmented progress bar toward unlocking the next streak badge
 */
@Composable
private fun SevenDayScreenTimeTrendWidget(
    bars: List<DailyUsageBar>,
    dailyGoalMinutes: Int,
    streakDays: Int,
    nextBadge: PausaStreakBadge?
) {
    var selectedPointIdx by remember { mutableIntStateOf(bars.lastIndex.coerceAtLeast(0)) }
    val daysUnderGoal = bars.count { it.minutes <= dailyGoalMinutes }
    val selectedPoint = bars.getOrNull(selectedPointIdx) ?: bars.last()
    val ptH = selectedPoint.minutes / 60
    val ptM = selectedPoint.minutes % 60

    Surface(
        color = PausaCreamBackground,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PausaCardBorder, RoundedCornerShape(16.dp))
            .testTag("chart_tendencia_7_dias")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            // Top Row: Title + Selected Day Callout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = PausaDarkGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Curva últimos 7 días",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = PausaTextPrimary
                    )
                }

                Text(
                    text = "${selectedPoint.dayLabel}: ${ptH}h ${String.format(Locale.US, "%02d", ptM)}m ($daysUnderGoal/7 bajo meta)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PausaDarkGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smooth Bezier Area Line Chart with Interactive Nodes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .pointerInput(bars) {
                        detectTapGestures { offset ->
                            val count = bars.size.coerceAtLeast(1)
                            val slotW = size.width.toFloat() / count
                            selectedPointIdx = (offset.x / slotW).toInt().coerceIn(0, count - 1)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val maxMin = 220f
                    val minMin = 70f
                    val topPad = 10.dp.toPx()
                    val bottomPad = 8.dp.toPx()
                    val usableH = (h - topPad - bottomPad).coerceAtLeast(1f)

                    // Dashed Daily Goal Line
                    val goalNorm = ((dailyGoalMinutes - minMin) / (maxMin - minMin)).coerceIn(0.1f, 0.95f)
                    val goalY = (h - bottomPad) - usableH * goalNorm
                    drawLine(
                        color = PausaAccentGreen.copy(alpha = 0.7f),
                        start = Offset(0f, goalY),
                        end = Offset(w, goalY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 6f), 0f)
                    )

                    val count = bars.size.coerceAtLeast(2)
                    val slotW = w / count
                    val points = bars.mapIndexed { idx, item ->
                        val x = idx * slotW + slotW / 2f
                        val norm = ((item.minutes - minMin) / (maxMin - minMin)).coerceIn(0.05f, 1f)
                        val y = (h - bottomPad) - usableH * norm
                        Offset(x, y)
                    }

                    // Smooth cubic bezier curve path
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.lastIndex) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val ctrlX = (p0.x + p1.x) / 2f
                            cubicTo(ctrlX, p0.y, ctrlX, p1.y, p1.x, p1.y)
                        }
                    }

                    // Area fill path under the bezier curve
                    val areaPath = Path().apply {
                        addPath(linePath)
                        lineTo(points.last().x, h)
                        lineTo(points.first().x, h)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PausaAccentGreen.copy(alpha = 0.35f),
                                PausaSageLight.copy(alpha = 0.05f)
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw main curve stroke
                    drawPath(
                        path = linePath,
                        color = PausaDarkGreen,
                        style = Stroke(
                            width = 2.6.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Vertical guide on selected node + Data nodes
                    points.forEachIndexed { idx, pt ->
                        val isSelected = idx == selectedPointIdx
                        val isUnderGoal = bars[idx].minutes <= dailyGoalMinutes

                        if (isSelected) {
                            drawLine(
                                color = PausaDarkGreen.copy(alpha = 0.28f),
                                start = Offset(pt.x, pt.y),
                                end = Offset(pt.x, h),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawCircle(
                                color = PausaSageLight,
                                radius = 7.dp.toPx(),
                                center = pt
                            )
                        }

                        drawCircle(
                            color = if (isUnderGoal) PausaDarkGreen else PausaFlameOrange,
                            radius = if (isSelected) 4.8.dp.toPx() else 3.4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = PausaCreamBackground,
                            radius = if (isSelected) 2.2.dp.toPx() else 1.5.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // X-Axis Day Labels (Lun..Dom)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                bars.forEachIndexed { idx, item ->
                    val isSelected = idx == selectedPointIdx
                    val isUnderGoal = item.minutes <= dailyGoalMinutes
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.5.sp,
                        fontWeight = if (isSelected || isUnderGoal) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) PausaDarkGreen else if (isUnderGoal) PausaTextPrimary else PausaTextMuted,
                        modifier = Modifier.clickable { selectedPointIdx = idx }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Next Badge Milestone Progress Bar
            val targetDays = nextBadge?.requiredDays ?: 30
            val progressFraction = (streakDays.toFloat() / targetDays.coerceAtLeast(1)).coerceIn(0.06f, 1f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (nextBadge != null) {
                        "Próxima insignia: ${nextBadge.title}"
                    } else {
                        "¡Todas las insignias desbloqueadas!"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = PausaTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$streakDays / $targetDays días",
                    style = MaterialTheme.typography.labelSmall,
                    color = PausaDarkGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFDCD9CF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PausaMediumGreen, PausaDarkGreen)
                            )
                        )
                )
            }
        }
    }
}
