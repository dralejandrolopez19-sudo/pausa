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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HmsIapPackage
import com.example.ui.PausaUiState
import com.example.ui.components.HuaweiIapWalletDialog
import com.example.ui.theme.PausaAlertRed
import com.example.ui.theme.PausaCardBorder
import com.example.ui.theme.PausaCardSurface
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDarkGreen
import com.example.ui.theme.PausaSageLight
import com.example.ui.theme.PausaTextPrimary
import com.example.ui.theme.PausaTextSecondary
import java.util.Locale

private val PayPalBlueBadge = Color(0xFF003087)
private val PayPalButtonAccent = Color(0xFF0070BA)

@Composable
fun AjustesScreen(
    uiState: PausaUiState,
    onOpenHuaweiWalletDialog: () -> Unit,
    onDismissHuaweiWalletDialog: () -> Unit,
    onPurchaseHuaweiPackage: (HmsIapPackage, String, Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onPayBrokenPauseWithPayPal: () -> Unit = {},
    onUpdatePayPalEmail: (String) -> Unit = {},
    onInitiatePendingWebOrder: ((HmsIapPackage, String) -> Unit)? = null,
    onConfirmPendingTransaction: (String) -> Unit = {},
    onCancelPendingTransaction: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stats = uiState.stats
    val resolvedPayPalEmail = stats.paypalEmail.ifBlank {
        stats.userEmail.ifBlank { "PayPal Checkout" }
    }
    var rechargeFeedbackMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PausaCreamBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = PausaTextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Unified minimalist card with PayPal Digital Balance, Dark Mode (Descanso visual) & Account
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(20.dp))
                .testTag("card_ajustes_minimal")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Saldo digital · PayPal section with full-width button & quick chips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PayPalBlueBadge),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "P",
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontStyle = FontStyle.Italic
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Saldo digital · PayPal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PausaTextPrimary
                                )
                                Text(
                                    text = "Cuenta: $resolvedPayPalEmail",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PayPalButtonAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            color = PausaSageLight,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", stats.digitalWalletBalance)} USD",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PausaDarkGreen,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val statusDescription = if (stats.accumulatedDebtDollars > 0.0) {
                        "Penalidad pendiente por pausa rota: -$${String.format(Locale.US, "%.2f", stats.accumulatedDebtDollars)} USD"
                    } else {
                        "Costo por romper una pausa: $${String.format(Locale.US, "%.2f", stats.penaltyPerBreak)} USD · Beneficiario: ${com.example.data.PAYPAL_MERCHANT_EMAIL}"
                    }
                    Text(
                        text = statusDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (stats.accumulatedDebtDollars > 0.0) PausaAlertRed else PausaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary full-width PayPal Recharge Button
                    Button(
                        onClick = onOpenHuaweiWalletDialog,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PayPalButtonAccent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_recargar_saldo")
                    ) {
                        Text(
                            text = "Recargar saldo vía PayPal",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick 1-tap PayPal top-up chips (+$1.00 | +$5.00 | +$10.00)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.iapPackages.forEach { pkg ->
                            val amountInt = pkg.creditsAmount.toInt()
                            OutlinedButton(
                                onClick = {
                                    onPurchaseHuaweiPackage(pkg, "PayPal", false)
                                    rechargeFeedbackMessage = "¡Recarga exitosa! +$${String.format(Locale.US, "%.2f", pkg.creditsAmount)} USD vía PayPal · Estado en BD: COMPLETADA."
                                },
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PausaCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_recarga_rapida_$amountInt")
                            ) {
                                Text(
                                    text = "+$$amountInt.00",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PayPalButtonAccent
                                )
                            }
                        }
                    }

                    uiState.walletTransactions.firstOrNull()?.let { latestTx ->
                        val latestStatus = com.example.data.TransactionStatus.fromValue(latestTx.status)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Última transacción: ${latestTx.orderReference} · Estado en BD: ${latestStatus.labelEs}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PausaTextSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("text_ultimo_estado_bd_paypal")
                        )
                    }

                    rechargeFeedbackMessage?.let { feedback ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = PausaSageLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { rechargeFeedbackMessage = null }
                                .testTag("badge_recarga_exitosa")
                        ) {
                            Text(
                                text = feedback,
                                style = MaterialTheme.typography.labelSmall,
                                color = PausaDarkGreen,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    if (stats.accumulatedDebtDollars > 0.0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onPayBrokenPauseWithPayPal()
                                rechargeFeedbackMessage = "Penalidad saldada vía PayPal."
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PausaAlertRed,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_pagar_deuda_paypal_ajustes")
                        ) {
                            Text(
                                text = "Pagar pausa rota ($${String.format(Locale.US, "%.2f", stats.accumulatedDebtDollars)} USD) vía PayPal",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = PausaCardBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )

                // 2. Modo oscuro (Material 3 Accessibility & Visual Rest Palette)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleDarkMode(!stats.isDarkMode) }
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PausaSageLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (stats.isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                contentDescription = if (stats.isDarkMode) "Modo oscuro activo" else "Modo claro activo",
                                tint = PausaDarkGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Modo oscuro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PausaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (stats.isDarkMode) {
                                    "Activo · Paleta bosque nocturno (baja luz azul)"
                                } else {
                                    "Descanso visual y accesibilidad nocturna"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = PausaTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Material 3 Color Palette swatches preview
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val swatches = if (stats.isDarkMode) {
                                    listOf(
                                        Color(0xFF121A15),
                                        Color(0xFF1B2620),
                                        Color(0xFF68A37C),
                                        Color(0xFFEAE6DF)
                                    )
                                } else {
                                    listOf(
                                        Color(0xFFF6F4EE),
                                        Color(0xFFEDEBE3),
                                        Color(0xFF1E4332),
                                        Color(0xFF172C22)
                                    )
                                }
                                swatches.forEach { swatchColor ->
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                            .border(0.75.dp, PausaCardBorder, CircleShape)
                                    )
                                }
                                Text(
                                    text = if (stats.isDarkMode) "Material 3 Nocturno" else "Material 3 Orgánico",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PausaDarkGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Switch(
                        checked = stats.isDarkMode,
                        onCheckedChange = onToggleDarkMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PausaDarkGreen,
                            uncheckedThumbColor = PausaTextSecondary,
                            uncheckedTrackColor = PausaCreamBackground,
                            uncheckedBorderColor = PausaCardBorder
                        ),
                        modifier = Modifier.testTag("switch_modo_oscuro")
                    )
                }

                HorizontalDivider(
                    color = PausaCardBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )

                // 3. Cuenta activa y botón claro de Cerrar sesión
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (stats.userName.isNotBlank()) {
                                    "Sesión: ${stats.userName}"
                                } else {
                                    "Sesión activa"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PausaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stats.userEmail.ifBlank { "Sin correo vinculado" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = PausaTextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tus datos y el progreso de tu planta quedan guardados",
                                style = MaterialTheme.typography.labelSmall,
                                color = PausaDarkGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onSignOutClick,
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PausaAlertRed.copy(alpha = 0.65f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PausaAlertRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_cerrar_sesion")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = PausaAlertRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cerrar sesión",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = PausaAlertRed
                        )
                    }
                }
            }
        }

        // Recent PayPal Transactions History (if any)
        if (uiState.walletTransactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Movimientos recientes (PayPal)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PausaTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PausaCardBorder, RoundedCornerShape(16.dp))
                    .testTag("card_historial_paypal")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    uiState.walletTransactions.take(5).forEachIndexed { index, tx ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = PausaCardBorder,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                        val txStatus = com.example.data.TransactionStatus.fromValue(tx.status)
                        val isPositive = tx.amount >= 0.0
                        val statusBadgeBg = when (txStatus) {
                            com.example.data.TransactionStatus.COMPLETED -> PausaSageLight
                            com.example.data.TransactionStatus.PENDING -> Color(0xFFFFF3CD)
                            com.example.data.TransactionStatus.CANCELLED,
                            com.example.data.TransactionStatus.FAILED -> PausaAlertRed.copy(alpha = 0.14f)
                        }
                        val statusBadgeText = when (txStatus) {
                            com.example.data.TransactionStatus.COMPLETED -> PausaDarkGreen
                            com.example.data.TransactionStatus.PENDING -> Color(0xFF7A5200)
                            com.example.data.TransactionStatus.CANCELLED,
                            com.example.data.TransactionStatus.FAILED -> PausaAlertRed
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = tx.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PausaTextPrimary
                                        )
                                        Surface(
                                            color = statusBadgeBg,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("badge_estado_tx_${tx.orderReference}")
                                        ) {
                                            Text(
                                                text = txStatus.labelEs,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusBadgeText,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${tx.subtitle} · ${tx.orderReference}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PausaTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPositive) {
                                        "+$${String.format(Locale.US, "%.2f", tx.amount)}"
                                    } else {
                                        "-$${String.format(Locale.US, "%.2f", -tx.amount)}"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        txStatus == com.example.data.TransactionStatus.CANCELLED ||
                                            txStatus == com.example.data.TransactionStatus.FAILED -> PausaTextSecondary
                                        isPositive -> PausaDarkGreen
                                        else -> PausaAlertRed
                                    }
                                )
                            }

                            if (txStatus == com.example.data.TransactionStatus.PENDING && tx.amount > 0.0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onConfirmPendingTransaction(tx.orderReference)
                                            rechargeFeedbackMessage = "¡Orden ${tx.orderReference} confirmada! Estado en BD: COMPLETADA."
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PausaDarkGreen,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_confirmar_tx_${tx.orderReference}")
                                    ) {
                                        Text(
                                            text = "Confirmar pago",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            onCancelPendingTransaction(tx.orderReference)
                                            rechargeFeedbackMessage = "Orden ${tx.orderReference} actualizada a CANCELADA."
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PausaAlertRed),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PausaAlertRed),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_cancelar_tx_${tx.orderReference}")
                                    ) {
                                        Text(
                                            text = "Cancelar",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showHuaweiIapSheet) {
        HuaweiIapWalletDialog(
            uiState = uiState,
            onDismiss = onDismissHuaweiWalletDialog,
            onConfirmPurchase = onPurchaseHuaweiPackage,
            onUpdatePayPalEmail = onUpdatePayPalEmail,
            onInitiatePendingWebOrder = onInitiatePendingWebOrder,
            onConfirmPendingTransaction = onConfirmPendingTransaction,
            onCancelPendingTransaction = onCancelPendingTransaction
        )
    }
}
