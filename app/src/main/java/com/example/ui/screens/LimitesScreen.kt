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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.data.AppLimitEntity
import com.example.ui.PausaUiState
import com.example.ui.components.SocialAppBadge
import com.example.ui.theme.PausaAlertRed
import com.example.ui.theme.PausaCardBorder
import com.example.ui.theme.PausaCardLight
import com.example.ui.theme.PausaCardSurface
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDarkGreen
import com.example.ui.theme.PausaSageLight
import com.example.ui.theme.PausaTextPrimary
import com.example.ui.theme.PausaTextSecondary
import kotlin.math.roundToInt

@Composable
fun LimitesScreen(
    uiState: PausaUiState,
    onToggleAppLimit: (AppLimitEntity, Boolean) -> Unit,
    onUpdateAppLimitMinutes: (AppLimitEntity, Int) -> Unit,
    onAddCustomApp: (String, Int) -> Unit,
    onUpdateFocusSchedule: (String, String, String, Boolean) -> Unit,
    onTriggerPauseForApp: (String) -> Unit,
    onRelockAllNow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editingApp by remember { mutableStateOf<AppLimitEntity?>(null) }
    var showAddAppDialog by remember { mutableStateOf(false) }
    var showEditFocusDialog by remember { mutableStateOf(false) }

    val isBlockerReady = uiState.isAccessibilityBlockerEnabled ||
        (uiState.hasUsageStatsPermission && uiState.hasOverlayPermission)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PausaCreamBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Límites",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PausaTextPrimary
            )
            TextButton(
                onClick = { showAddAppDialog = true },
                modifier = Modifier.testTag("btn_add_app_limit")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Añadir app",
                    tint = PausaDarkGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Añadir app",
                    color = PausaDarkGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Real System Blocker Shield Card (Accessibility + Overlay + Usage Stats)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isBlockerReady) PausaSageLight else PausaCardSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = if (isBlockerReady) PausaDarkGreen else PausaAlertRed,
                    shape = RoundedCornerShape(20.dp)
                )
                .testTag("card_escudo_bloqueo_real")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isBlockerReady) PausaDarkGreen else PausaAlertRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBlockerReady) Icons.Filled.Security else Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBlockerReady) {
                                    "Escudo de Bloqueo Activo"
                                } else {
                                    "Activa los permisos de bloqueo"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PausaTextPrimary
                            )
                            Text(
                                text = if (isBlockerReady) {
                                    "Al abrir una red social marcada abajo, PAUSA la bloqueará de inmediato."
                                } else {
                                    "Para que Android/Huawei bloquee Instagram, TikTok o YouTube fuera de la app, activa estos permisos:"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = PausaTextSecondary
                            )
                        }
                    }
                }

                // Step 1: Accessibility Service (Instant interception)
                BlockerPermissionStepRow(
                    title = "1. Servicio de Accesibilidad PAUSA",
                    subtitle = "Detecta al instante cuando abres Instagram, TikTok o YouTube",
                    isGranted = uiState.isAccessibilityBlockerEnabled,
                    buttonText = "Activar",
                    onClick = {
                        runCatching {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                // Step 2: Overlay Permission (Draw over apps)
                BlockerPermissionStepRow(
                    title = "2. Mostrar sobre otras aplicaciones",
                    subtitle = "Permite cubrir la red social con la pantalla de Modo Pausa",
                    isGranted = uiState.hasOverlayPermission,
                    buttonText = "Permitir",
                    onClick = {
                        runCatching {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri()
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                // Step 3: Usage Access Permission
                BlockerPermissionStepRow(
                    title = "3. Acceso a datos de uso",
                    subtitle = "Monitorea el tiempo de uso en segundo plano",
                    isGranted = uiState.hasUsageStatsPermission,
                    buttonText = "Conceder",
                    onClick = {
                        runCatching {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                OutlinedButton(
                    onClick = onRelockAllNow,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_reactivar_bloqueo_ahora")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = PausaDarkGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bloquear redes seleccionadas ahora mismo",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = PausaDarkGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // App Limits Container Card matching Screen 2
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                uiState.appLimits.forEachIndexed { index, appLimit ->
                    AppLimitRow(
                        limit = appLimit,
                        onToggle = { enabled -> onToggleAppLimit(appLimit, enabled) },
                        onClickEdit = { editingApp = appLimit }
                    )
                    if (index < uiState.appLimits.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(PausaCardBorder)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Horario de enfoque section matching Screen 2
        Text(
            text = "Horario de enfoque",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PausaTextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(20.dp))
                .clickable { showEditFocusDialog = true }
                .testTag("card_horario_enfoque")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = PausaDarkGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "De ${uiState.stats.focusStartTime} a ${uiState.stats.focusEndTime}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PausaTextPrimary
                        )
                    }
                    Text(
                        text = uiState.stats.focusScope,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PausaTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PausaCardBorder)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsPaused,
                            contentDescription = null,
                            tint = PausaDarkGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bloquear notificaciones",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PausaTextPrimary
                        )
                    }
                    Switch(
                        checked = uiState.stats.focusEnabled,
                        onCheckedChange = { checked ->
                            onUpdateFocusSchedule(
                                uiState.stats.focusStartTime,
                                uiState.stats.focusEndTime,
                                uiState.stats.focusScope,
                                checked
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PausaDarkGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = PausaCardBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Toca cualquier aplicación para ajustar su límite diario o probar la pantalla de pausa consciente.",
            style = MaterialTheme.typography.labelSmall,
            color = PausaTextSecondary
        )
    }

    // Dialog to edit an app's daily time limit or test its pause trigger
    editingApp?.let { app ->
        var sliderMinutes by remember(app) { mutableFloatStateOf(app.limitMinutes.toFloat()) }
        AlertDialog(
            onDismissRequest = { editingApp = null },
            containerColor = PausaCardLight,
            title = {
                Text(
                    text = "Límite para ${app.appName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    val label = if (sliderMinutes.roundToInt() == 0) {
                        "Sin límite diario"
                    } else {
                        "${sliderMinutes.roundToInt()} min / día"
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = PausaDarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = sliderMinutes,
                        onValueChange = { sliderMinutes = it },
                        valueRange = 0f..180f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = PausaDarkGreen,
                            activeTrackColor = PausaDarkGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            val name = app.appName
                            editingApp = null
                            onTriggerPauseForApp(name)
                        }
                    ) {
                        Text(
                            text = "Probar pausa consciente para ${app.appName} →",
                            color = PausaDarkGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateAppLimitMinutes(app, sliderMinutes.roundToInt())
                        editingApp = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PausaDarkGreen)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingApp = null }) {
                    Text("Cancelar", color = PausaTextSecondary)
                }
            }
        )
    }

    // Dialog to add a new app limit
    if (showAddAppDialog) {
        var newAppName by remember { mutableStateOf("") }
        var newAppMinutes by remember { mutableFloatStateOf(30f) }
        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            containerColor = PausaCardLight,
            title = { Text("Añadir app a Límites", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newAppName,
                        onValueChange = { newAppName = it },
                        label = { Text("Nombre de la app (ej. Reddit)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Límite diario: ${newAppMinutes.roundToInt()} min / día",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = newAppMinutes,
                        onValueChange = { newAppMinutes = it },
                        valueRange = 15f..120f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = PausaDarkGreen,
                            activeTrackColor = PausaDarkGreen
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAppName.isNotBlank()) {
                            onAddCustomApp(newAppName, newAppMinutes.roundToInt())
                            showAddAppDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PausaDarkGreen)
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAppDialog = false }) {
                    Text("Cancelar", color = PausaTextSecondary)
                }
            }
        )
    }

    // Dialog to edit focus schedule
    if (showEditFocusDialog) {
        var start by remember { mutableStateOf(uiState.stats.focusStartTime) }
        var end by remember { mutableStateOf(uiState.stats.focusEndTime) }
        var scope by remember { mutableStateOf(uiState.stats.focusScope) }
        AlertDialog(
            onDismissRequest = { showEditFocusDialog = false },
            containerColor = PausaCardLight,
            title = { Text("Configurar horario de enfoque", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = { Text("Hora de inicio (ej. 22:00)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = { Text("Hora de fin (ej. 07:00)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = scope,
                        onValueChange = { scope = it },
                        label = { Text("Alcance") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateFocusSchedule(start, end, scope, uiState.stats.focusEnabled)
                        showEditFocusDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PausaDarkGreen)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditFocusDialog = false }) {
                    Text("Cancelar", color = PausaTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun BlockerPermissionStepRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {
    Surface(
        color = PausaCreamBackground,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isGranted) PausaDarkGreen else PausaCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PausaTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = PausaTextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isGranted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Permiso concedido",
                        tint = PausaDarkGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Activo",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PausaDarkGreen
                    )
                }
            } else {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PausaDarkGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AppLimitRow(
    limit: AppLimitEntity,
    onToggle: (Boolean) -> Unit,
    onClickEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickEdit() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SocialAppBadge(
            iconType = limit.iconType,
            brandColorHex = limit.brandColorHex
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = limit.appName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PausaTextPrimary
            )
            Text(
                text = when {
                    limit.isEnabled -> "Bloqueo activo · Desbloqueo solo pagando $1.00 PayPal"
                    limit.limitMinutes <= 0 -> "Sin límite"
                    else -> "${limit.limitMinutes} min / día (Desactivado)"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (limit.isEnabled) PausaDarkGreen else PausaTextSecondary,
                fontWeight = if (limit.isEnabled) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        Switch(
            checked = limit.isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PausaDarkGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = PausaCardBorder
            ),
            modifier = Modifier.testTag("switch_${limit.id}")
        )
    }
}
