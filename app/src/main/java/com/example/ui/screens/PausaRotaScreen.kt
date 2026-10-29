package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HmsIapPackage
import com.example.ui.PausaUiState
import com.example.ui.components.HuaweiIapWalletDialog
import com.example.ui.components.PlantIllustration
import com.example.ui.theme.PausaAlertRed
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDeepForest
import com.example.ui.theme.PausaTextPrimary
import java.util.Locale

private val PayPalBrandBlue = Color(0xFF003087)
private val PayPalGold = Color(0xFFFFC439)

@Composable
fun PausaRotaScreen(
    uiState: PausaUiState,
    onEntendidoClick: () -> Unit,
    onPayWithPayPal: () -> Unit = {},
    onOpenPayPalRecharge: () -> Unit = {},
    onDismissPayPalDialog: () -> Unit = {},
    onPurchasePayPalPackage: (HmsIapPackage, String, Boolean) -> Unit = { _, _, _ -> },
    onUpdatePayPalEmail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stats = uiState.stats
    val penaltyFormatted = String.format(Locale.US, "$%.2f", stats.penaltyPerBreak)
    val hasPendingPayPalCharge = stats.accumulatedDebtDollars > 0.0
    val pendingAmountFormatted = String.format(
        Locale.US,
        "$%.2f",
        if (hasPendingPayPalCharge) stats.accumulatedDebtDollars else stats.penaltyPerBreak
    )
    val paypalAccountLabel = stats.paypalEmail.ifBlank {
        stats.userEmail.ifBlank { "usuario@paypal.com" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PausaDeepForest)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "PAUSA",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE6ECE8),
            letterSpacing = 1.2.sp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Red exclamation circle with white border
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(PausaAlertRed)
                    .border(2.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "¡Pausa rota!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Has decidido continuar y se ha\naplicado una penalidad vía PayPal de:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC5D6CC),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = penaltyFormatted,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.testTag("text_penalty_amount")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // PayPal Penalty & Recharge Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3B2E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF3A6752), RoundedCornerShape(20.dp))
                    .testTag("card_paypal_pausa_rota")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PayPalBrandBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasPendingPayPalCharge) {
                                    "Pago pendiente vía PayPal ($pendingAmountFormatted)"
                                } else {
                                    "Penalidad cubierta con Saldo PayPal"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Pagador: $paypalAccountLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB5D2C1)
                            )
                            Text(
                                text = "Beneficiario: ${com.example.data.PAYPAL_MERCHANT_EMAIL}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8FD6AD),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (!hasPendingPayPalCharge) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Pagado con PayPal",
                                tint = Color(0xFF67D696),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (hasPendingPayPalCharge) {
                        Button(
                            onClick = onPayWithPayPal,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PayPalGold,
                                contentColor = PayPalBrandBlue
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_pagar_paypal_pausa_rota")
                        ) {
                            Text(
                                text = "Pagar $pendingAmountFormatted USD con PayPal",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Text(
                            text = "Recibo PayPal: ${stats.lastPayPalReceiptId.ifBlank { "PP-SALDO-OK" }} · Saldo restante: $${String.format(Locale.US, "%.2f", stats.digitalWalletBalance)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA7D4B6),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedButton(
                        onClick = onOpenPayPalRecharge,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_recargar_paypal_pausa_rota")
                    ) {
                        Text(
                            text = "Recargar saldo con PayPal (+$5.00 / +$10.00)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDCEEE2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onEntendidoClick,
                enabled = !hasPendingPayPalCharge,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PausaCreamBackground,
                    contentColor = PausaTextPrimary,
                    disabledContainerColor = Color(0xFF284839),
                    disabledContentColor = Color(0xFF8AA899)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_entendido_pausa_rota")
            ) {
                Text(
                    text = if (hasPendingPayPalCharge) {
                        "Completa el pago en PayPal para continuar"
                    } else {
                        "Entendido"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tu planta ha perdido un nivel de energía.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFB3C7BC),
                textAlign = TextAlign.Center
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PlantIllustration(
                level = 2,
                modifier = Modifier.size(84.dp),
                showAuraCircle = false,
                isWilted = true
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Saldo PayPal restante: $${String.format(Locale.US, "%.2f", stats.digitalWalletBalance)} USD",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8AA899)
            )
        }
    }

    if (uiState.showHuaweiIapSheet) {
        HuaweiIapWalletDialog(
            uiState = uiState,
            onDismiss = onDismissPayPalDialog,
            onConfirmPurchase = onPurchasePayPalPackage,
            onUpdatePayPalEmail = onUpdatePayPalEmail
        )
    }
}
