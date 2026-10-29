package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.data.HmsIapPackage
import com.example.ui.PausaUiState
import com.example.ui.theme.PausaAlertRed
import com.example.ui.theme.PausaCardBorder
import com.example.ui.theme.PausaCardLight
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDarkGreen
import com.example.ui.theme.PausaSageLight
import com.example.ui.theme.PausaTextPrimary
import com.example.ui.theme.PausaTextSecondary
import java.util.Locale

private val PayPalBlue = Color(0xFF003087)
private val PayPalAccentBlue = Color(0xFF0070BA)

@Composable
fun HuaweiIapWalletDialog(
    uiState: PausaUiState,
    onDismiss: () -> Unit,
    onConfirmPurchase: (HmsIapPackage, String, Boolean) -> Unit,
    onUpdatePayPalEmail: (String) -> Unit = {},
    onInitiatePendingWebOrder: ((HmsIapPackage, String) -> Unit)? = null,
    onConfirmPendingTransaction: (String) -> Unit = {},
    onCancelPendingTransaction: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val packages = uiState.iapPackages
    val hasBrokenPauseDebt = uiState.stats.accumulatedDebtDollars > 0.0
    val isBreakingActivePause = uiState.currentDestination == com.example.ui.PausaDestination.PAUSA

    var selectedPkg by remember {
        val defaultPkg = if (isBreakingActivePause || (hasBrokenPauseDebt && uiState.stats.accumulatedDebtDollars <= 1.0)) {
            packages.firstOrNull { it.creditsAmount == 1.0 }
        } else {
            packages.firstOrNull { it.isRecommended }
        }
        mutableStateOf(defaultPkg ?: packages.first())
    }

    var paypalAccountInput by remember(uiState.stats.paypalEmail, uiState.stats.userEmail) {
        mutableStateOf(
            uiState.stats.paypalEmail.ifBlank {
                uiState.stats.userEmail.ifBlank { "usuario@paypal.com" }
            }
        )
    }

    val pendingWebTransactions = uiState.walletTransactions.filter {
        it.status == com.example.data.TransactionStatus.PENDING.name && it.amount > 0.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PausaCardLight,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PayPalBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = when {
                            isBreakingActivePause -> "Pagar para Romper Pausa"
                            hasBrokenPauseDebt -> "Saldar y Recargar · PayPal"
                            else -> "Recargar Saldo · PayPal"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PausaTextPrimary
                    )
                    Text(
                        text = if (isBreakingActivePause) {
                            "Pago obligatorio para desbloquear ${uiState.triggeredAppName}"
                        } else {
                            "Beneficiario: ${com.example.data.PAYPAL_MERCHANT_EMAIL}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = PayPalAccentBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current Balance & Broken Pause Penalty Status Pill
                Surface(
                    color = PausaSageLight,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Saldo actual en Pausa",
                                style = MaterialTheme.typography.labelSmall,
                                color = PausaTextSecondary
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", uiState.stats.digitalWalletBalance)} USD",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = PausaDarkGreen
                            )
                        }
                        if (isBreakingActivePause || hasBrokenPauseDebt) {
                            val requiredUsd = if (hasBrokenPauseDebt) {
                                uiState.stats.accumulatedDebtDollars
                            } else {
                                uiState.stats.penaltyPerBreak
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isBreakingActivePause) "Costo desbloqueo" else "Deuda pendiente",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PausaAlertRed
                                )
                                Text(
                                    text = "-$${String.format(Locale.US, "%.2f", requiredUsd)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PausaAlertRed
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    tint = PausaDarkGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sin deudas",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PausaDarkGreen
                                )
                            }
                        }
                    }
                }

                // Pending Web Transactions Banner (if the user initiated a web checkout awaiting confirmation)
                pendingWebTransactions.firstOrNull()?.let { pendingTx ->
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE0A800), RoundedCornerShape(14.dp))
                            .testTag("card_orden_pendiente_dialog")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Orden ${pendingTx.orderReference} · PENDIENTE",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF7A5200)
                                )
                                Text(
                                    text = "+$${String.format(Locale.US, "%.2f", pendingTx.amount)} USD",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7A5200)
                                )
                            }
                            Text(
                                text = "Confirma el pago web para actualizar el estado a COMPLETADA en la base de datos y acreditar tu saldo.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6B4C00)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onConfirmPendingTransaction(pendingTx.orderReference) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PausaDarkGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_confirmar_orden_pendiente")
                                ) {
                                    Text(
                                        text = "Confirmar pago",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onCancelPendingTransaction(pendingTx.orderReference) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PausaAlertRed),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PausaAlertRed),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_cancelar_orden_pendiente")
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

                if (isBreakingActivePause || hasBrokenPauseDebt) {
                    Surface(
                        color = PausaAlertRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PausaAlertRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = if (isBreakingActivePause) {
                                "Solo pagando puedes romper la pausa de ${uiState.triggeredAppName}. El importe ($${String.format(Locale.US, "%.2f", uiState.stats.penaltyPerBreak)} USD) se descuenta y el resto queda en tu saldo."
                            } else {
                                "Se descontarán los $${String.format(Locale.US, "%.2f", uiState.stats.accumulatedDebtDollars)} USD pendientes y el resto se sumará a tu saldo disponible."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = PausaAlertRed,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Cuenta PayPal vinculada del usuario que paga
                OutlinedTextField(
                    value = paypalAccountInput,
                    onValueChange = { paypalAccountInput = it },
                    label = { Text("Tu correo de PayPal") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PayPalAccentBlue,
                        unfocusedBorderColor = PausaCardBorder,
                        focusedLabelColor = PayPalAccentBlue,
                        unfocusedLabelColor = PausaTextSecondary,
                        focusedTextColor = PausaTextPrimary,
                        unfocusedTextColor = PausaTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_paypal_account")
                )

                // 1. Select Amount
                Text(
                    text = "Selecciona el monto a recargar",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = PausaTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    packages.forEach { pkg ->
                        val isSelected = selectedPkg.productId == pkg.productId
                        Surface(
                            color = if (isSelected) PausaDarkGreen else PausaCreamBackground,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PausaDarkGreen else PausaCardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedPkg = pkg }
                                .testTag("pkg_${pkg.creditsAmount.toInt()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "+$${pkg.creditsAmount.toInt()}.00",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else PausaTextPrimary
                                )
                                Text(
                                    text = if (pkg.creditsAmount == 1.0) "1 pausa" else "${pkg.creditsAmount.toInt()} pausas",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color(0xFFD5E6DC) else PausaTextSecondary
                                )
                            }
                        }
                    }
                }

                // Direct Action 1: Instant PayPal Balance Recharge in App (PENDING -> COMPLETED in Room)
                Button(
                    onClick = {
                        val cleanEmail = paypalAccountInput.trim().ifBlank { "usuario@paypal.com" }
                        onUpdatePayPalEmail(cleanEmail)
                        onConfirmPurchase(selectedPkg, "PayPal", false)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PayPalAccentBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_confirmar_recarga_hms")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBreakingActivePause) {
                            "Pagar $${String.format(Locale.US, "%.2f", selectedPkg.creditsAmount)} y Romper Pausa"
                        } else {
                            "Recargar +$${String.format(Locale.US, "%.2f", selectedPkg.creditsAmount)} USD con PayPal"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Direct Action 2: Open Official PayPal Web Checkout (creates PENDING order in DB if handler provided, or completes)
                OutlinedButton(
                    onClick = {
                        val cleanEmail = paypalAccountInput.trim().ifBlank { "usuario@paypal.com" }
                        onUpdatePayPalEmail(cleanEmail)
                        runCatching {
                            val checkoutUrl = com.example.data.buildPayPalMerchantCheckoutUrl(
                                amountUsd = selectedPkg.creditsAmount,
                                itemDescription = if (isBreakingActivePause) {
                                    "PAUSA - Desbloqueo de pausa (${uiState.triggeredAppName})"
                                } else {
                                    "PAUSA App - ${selectedPkg.title}"
                                }
                            )
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                checkoutUrl.toUri()
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                        if (onInitiatePendingWebOrder != null && !isBreakingActivePause) {
                            onInitiatePendingWebOrder(selectedPkg, cleanEmail)
                        } else {
                            onConfirmPurchase(selectedPkg, "PayPal Checkout Web", false)
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PayPalAccentBlue),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PayPalAccentBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_abrir_paypal_web")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = PayPalAccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Abrir pasarela web de PayPal ($${String.format(Locale.US, "%.2f", selectedPkg.creditsAmount)})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = PausaTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Cada recarga registra su estado (PENDIENTE → COMPLETADA) en la base de datos local.",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = PausaTextSecondary
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isBreakingActivePause) "Seguir en pausa" else "Cerrar",
                    color = PausaTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
