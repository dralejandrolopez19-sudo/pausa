package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val pin6Digits: String,
    val createdAt: Long,
    val usedMinutesToday: Int = 0,
    val dailyGoalMinutes: Int = 120,
    val streakDays: Int = 0,
    val recoveredMinutes: Int = 0,
    val savedMoneyDollars: Double = 0.0,
    val digitalWalletBalance: Double = 0.0,
    val accumulatedDebtDollars: Double = 0.0,
    val paypalEmail: String = "",
    val activePlantId: Int = 1,
    val activePlantLevel: Int = 1,
    val activePlantProgress: Int = 35,
    val focusWorkMinutes: Int = 25,
    val focusPauseMinutes: Int = 5,
    val focusStartTime: String = "21:00",
    val focusEndTime: String = "07:00",
    val focusScope: String = "Todas las apps",
    val focusEnabled: Boolean = true,
    val isDarkMode: Boolean = false
)

@Entity(tableName = "app_limits")
data class AppLimitEntity(
    @PrimaryKey val id: String,
    val appName: String,
    val limitMinutes: Int, // 0 means "Sin límite"
    val usedMinutesToday: Int,
    val isEnabled: Boolean,
    val brandColorHex: Long,
    val iconType: String // "instagram", "tiktok", "youtube", "facebook", "x", "whatsapp", "custom"
)

@Entity(tableName = "pause_logs")
data class PauseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val appName: String,
    val outcome: String, // "RESPECTED" or "BROKEN"
    val penaltyAmount: Double,
    val minutesSaved: Int
)

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val level: Int, // 1: Semilla, 2: Brote, 3: En crecimiento, 4: Floreciendo, 5: Árbol
    val levelName: String,
    val progressPercent: Int,
    val isCurrent: Boolean,
    val unlockedBenefit: String
)

enum class TransactionStatus(val labelEs: String) {
    PENDING("PENDIENTE"),
    COMPLETED("COMPLETADA"),
    FAILED("FALLIDA"),
    CANCELLED("CANCELADA");

    companion object {
        fun fromValue(value: String): TransactionStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: COMPLETED
        }
    }
}

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val updatedAt: Long = timestamp,
    val type: String, // "TOP_UP_PAYPAL", "TOP_UP_PAYPAL_WEB", "PAYPAL_PENALTY_PAYMENT", "PENALTY_PAYPAL_BALANCE", "PENALTY_PAYPAL_PENDING"
    val status: String = TransactionStatus.COMPLETED.name, // "PENDING", "COMPLETED", "FAILED", "CANCELLED"
    val title: String,
    val subtitle: String,
    val amount: Double, // Positive for top-ups, negative for penalties
    val orderReference: String,
    val payerEmail: String = "",
    val merchantEmail: String = "dr.alejandrolopez19@gmail.com",
    val skuId: String = "",
    val balanceAfter: Double = 0.0
)

data class DailyUsageBar(
    val dayLabel: String, // Lun, Mar, Mié, Jue, Vie, Sáb, Dom
    val minutes: Int
)

data class HmsIapPackage(
    val productId: String,
    val title: String,
    val description: String,
    val priceUsd: Double,
    val creditsAmount: Double,
    val isRecommended: Boolean = false
)
