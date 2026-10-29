package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.blocker.PausaBlockerController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pausa_prefs")

enum class WalletMode {
    PAYPAL,          // Pago y recarga directa mediante PayPal
    HUAWEI_IAP,      // Recarga mediante Huawei In-App Purchases (HMS IAP Consumables)
    COMPROMISO_LIBRE // Saldo de compromiso personal gratuito / Deuda registrada en la app
}

data class UserStatsPreferences(
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val authProvider: String = "", // "Google" or "Correo"
    val usedMinutesToday: Int = 0,
    val dailyGoalMinutes: Int = 120, // 2 h 00 min default goal
    val streakDays: Int = 0,
    val recoveredMinutes: Int = 0,
    val savedMoneyDollars: Double = 0.00,
    val digitalWalletBalance: Double = 0.00,
    val accumulatedDebtDollars: Double = 0.00,
    val penaltyPerBreak: Double = 1.00,
    val walletMode: WalletMode = WalletMode.PAYPAL,
    val selectedPaymentMethodLabel: String = "PayPal",
    val paypalEmail: String = "",
    val lastPayPalReceiptId: String = "",
    val focusWorkMinutes: Int = 25,
    val focusPauseMinutes: Int = 5,
    val focusStartTime: String = "21:00",
    val focusEndTime: String = "07:00",
    val focusScope: String = "Todas las apps",
    val focusEnabled: Boolean = true,
    val isDarkMode: Boolean = false
)

const val PAYPAL_MERCHANT_EMAIL = "dr.alejandrolopez19@gmail.com"

fun buildPayPalMerchantCheckoutUrl(amountUsd: Double, itemDescription: String): String {
    val formattedAmount = String.format(java.util.Locale.US, "%.2f", amountUsd)
    val encodedBusiness = java.net.URLEncoder.encode(PAYPAL_MERCHANT_EMAIL, "UTF-8")
    val encodedItem = java.net.URLEncoder.encode(itemDescription, "UTF-8")
    return "https://www.paypal.com/cgi-bin/webscr?cmd=_xclick&business=$encodedBusiness&item_name=$encodedItem&amount=$formattedAmount&currency_code=USD"
}

val officialHuaweiIapPackages = listOf(
    HmsIapPackage(
        productId = "com.aistudio.pausa.paypal.100",
        title = "Pago de Pausa · $1.00",
        description = "Cubre 1 pausa rota ($1.00 USD) vía PayPal",
        priceUsd = 1.00,
        creditsAmount = 1.00,
        isRecommended = false
    ),
    HmsIapPackage(
        productId = "com.aistudio.pausa.paypal.500",
        title = "Recarga Brote · $5.00",
        description = "5 créditos de pausa consciente vía PayPal",
        priceUsd = 5.00,
        creditsAmount = 5.00,
        isRecommended = true
    ),
    HmsIapPackage(
        productId = "com.aistudio.pausa.paypal.1000",
        title = "Recarga Bosque · $10.00",
        description = "10 créditos de pausa consciente vía PayPal",
        priceUsd = 10.00,
        creditsAmount = 10.00,
        isRecommended = false
    )
)

class PausaRepository(
    private val context: Context,
    private val dao: PausaDao
) {
    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val AUTH_PROVIDER = stringPreferencesKey("auth_provider")
        val SAVED_PIN = stringPreferencesKey("saved_pin_6")
        val USED_MINUTES = intPreferencesKey("used_minutes_today")
        val GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val RECOVERED_MINUTES = intPreferencesKey("recovered_minutes")
        val SAVED_MONEY = doublePreferencesKey("saved_money")
        val WALLET_BALANCE = doublePreferencesKey("wallet_balance")
        val ACCUMULATED_DEBT = doublePreferencesKey("accumulated_debt")
        val PENALTY_AMOUNT = doublePreferencesKey("penalty_amount")
        val WALLET_MODE = stringPreferencesKey("wallet_mode")
        val PAYMENT_METHOD_LABEL = stringPreferencesKey("payment_method_label")
        val PAYPAL_EMAIL = stringPreferencesKey("paypal_email")
        val LAST_PAYPAL_RECEIPT = stringPreferencesKey("last_paypal_receipt")
        val FOCUS_WORK_MINUTES = intPreferencesKey("focus_work_minutes")
        val FOCUS_PAUSE_MINUTES = intPreferencesKey("focus_pause_minutes")
        val FOCUS_START = stringPreferencesKey("focus_start")
        val FOCUS_END = stringPreferencesKey("focus_end")
        val FOCUS_SCOPE = stringPreferencesKey("focus_scope")
        val FOCUS_ENABLED = booleanPreferencesKey("focus_enabled")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    val appLimits: Flow<List<AppLimitEntity>> = dao.getAllAppLimits()
    val pauseLogs: Flow<List<PauseLogEntity>> = dao.getAllPauseLogs()
    val plants: Flow<List<PlantEntity>> = dao.getAllPlants()
    val walletTransactions: Flow<List<WalletTransactionEntity>> = dao.getAllWalletTransactions()

    val userPreferences: Flow<UserStatsPreferences> = context.dataStore.data.map { prefs ->
        val modeStr = prefs[Keys.WALLET_MODE] ?: WalletMode.PAYPAL.name
        val mode = try {
            WalletMode.valueOf(modeStr)
        } catch (_: Exception) {
            WalletMode.PAYPAL
        }
        val userEmail = prefs[Keys.USER_EMAIL] ?: ""
        UserStatsPreferences(
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            userName = prefs[Keys.USER_NAME] ?: "",
            userEmail = userEmail,
            authProvider = prefs[Keys.AUTH_PROVIDER] ?: "",
            usedMinutesToday = prefs[Keys.USED_MINUTES] ?: 0,
            dailyGoalMinutes = prefs[Keys.GOAL_MINUTES] ?: 120,
            streakDays = prefs[Keys.STREAK_DAYS] ?: 0,
            recoveredMinutes = prefs[Keys.RECOVERED_MINUTES] ?: 0,
            savedMoneyDollars = prefs[Keys.SAVED_MONEY] ?: 0.00,
            digitalWalletBalance = prefs[Keys.WALLET_BALANCE] ?: 0.00,
            accumulatedDebtDollars = prefs[Keys.ACCUMULATED_DEBT] ?: 0.00,
            penaltyPerBreak = prefs[Keys.PENALTY_AMOUNT] ?: 1.00,
            walletMode = mode,
            selectedPaymentMethodLabel = prefs[Keys.PAYMENT_METHOD_LABEL] ?: "PayPal",
            paypalEmail = prefs[Keys.PAYPAL_EMAIL] ?: userEmail,
            lastPayPalReceiptId = prefs[Keys.LAST_PAYPAL_RECEIPT] ?: "",
            focusWorkMinutes = prefs[Keys.FOCUS_WORK_MINUTES] ?: 25,
            focusPauseMinutes = prefs[Keys.FOCUS_PAUSE_MINUTES] ?: 5,
            focusStartTime = prefs[Keys.FOCUS_START] ?: "21:00",
            focusEndTime = prefs[Keys.FOCUS_END] ?: "07:00",
            focusScope = prefs[Keys.FOCUS_SCOPE] ?: "Todas las apps",
            focusEnabled = prefs[Keys.FOCUS_ENABLED] ?: true,
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: false
        )
    }

    private fun defaultAppLimitsCatalog(): List<AppLimitEntity> = listOf(
        AppLimitEntity("instagram", "Instagram", 45, 0, true, 0xFFE1306C, "instagram"),
        AppLimitEntity("tiktok", "TikTok", 45, 0, true, 0xFF111111, "tiktok"),
        AppLimitEntity("youtube", "YouTube", 60, 0, true, 0xFFFF0000, "youtube"),
        AppLimitEntity("facebook", "Facebook", 30, 0, false, 0xFF1877F2, "facebook"),
        AppLimitEntity("twitter", "X (Twitter)", 30, 0, false, 0xFF14171A, "x"),
        AppLimitEntity("whatsapp", "WhatsApp", 0, 0, false, 0xFF25D366, "whatsapp")
    )

    private fun defaultPlantsCatalog(
        activePlantId: Int = 1,
        activePlantLevel: Int = 1,
        activePlantProgress: Int = 35
    ): List<PlantEntity> {
        val base = listOf(
            PlantEntity(1, "Brote de Calma", 1, "Semilla", 35, true, "Inicio del hábito consciente"),
            PlantEntity(2, "Helecho Sereno", 2, "Brote", 40, false, "Respiración guiada desbloqueada"),
            PlantEntity(3, "Ficus de Atención", 3, "En crecimiento", 50, false, "Simboliza tu atención y energía recuperada"),
            PlantEntity(4, "Jazmín en Flor", 4, "Floreciendo", 65, false, "Modo concentración profunda"),
            PlantEntity(5, "Roble Centenario", 5, "Árbol", 80, false, "Maestría total sin scroll impulsivo")
        )
        return base.map { plant ->
            if (plant.id == activePlantId) {
                val safeLevel = activePlantLevel.coerceIn(1, 5)
                plant.copy(
                    isCurrent = true,
                    level = safeLevel,
                    levelName = levelNameFor(safeLevel),
                    progressPercent = activePlantProgress.coerceIn(10, 100),
                    unlockedBenefit = unlockedBenefitFor(safeLevel)
                )
            } else {
                plant.copy(isCurrent = false)
            }
        }
    }

    private suspend fun seedDefaultTablesIfNeeded(
        activePlantId: Int = 1,
        activePlantLevel: Int = 1,
        activePlantProgress: Int = 35,
        forceReset: Boolean = false
    ) {
        if (forceReset) {
            dao.clearPauseLogs()
            dao.clearWalletTransactions()
            dao.clearAppLimits()
            dao.clearPlants()
        }
        if (dao.getAppLimitsCount() == 0) {
            dao.insertAppLimits(defaultAppLimitsCatalog())
        }
        if (dao.getPlantsCount() == 0) {
            dao.insertPlants(
                defaultPlantsCatalog(
                    activePlantId = activePlantId,
                    activePlantLevel = activePlantLevel,
                    activePlantProgress = activePlantProgress
                )
            )
        }
    }

    private suspend fun buildCurrentUserAccountEntity(): UserAccountEntity? {
        val prefs = context.dataStore.data.first()
        val email = (prefs[Keys.USER_EMAIL] ?: "").trim().lowercase()
        if (email.isBlank()) return null

        val existing = dao.getUserAccountByEmail(email)
        val activePlant = dao.getActivePlantSync() ?: dao.getAllPlantsSync().firstOrNull()

        return UserAccountEntity(
            email = email,
            fullName = (prefs[Keys.USER_NAME] ?: existing?.fullName ?: email.substringBefore("@")).ifBlank {
                email.substringBefore("@")
            },
            pin6Digits = prefs[Keys.SAVED_PIN] ?: existing?.pin6Digits ?: "000000",
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            usedMinutesToday = prefs[Keys.USED_MINUTES] ?: 0,
            dailyGoalMinutes = prefs[Keys.GOAL_MINUTES] ?: 120,
            streakDays = prefs[Keys.STREAK_DAYS] ?: 0,
            recoveredMinutes = prefs[Keys.RECOVERED_MINUTES] ?: 0,
            savedMoneyDollars = prefs[Keys.SAVED_MONEY] ?: 0.0,
            digitalWalletBalance = prefs[Keys.WALLET_BALANCE] ?: 0.0,
            accumulatedDebtDollars = prefs[Keys.ACCUMULATED_DEBT] ?: 0.0,
            paypalEmail = prefs[Keys.PAYPAL_EMAIL] ?: email,
            activePlantId = activePlant?.id ?: existing?.activePlantId ?: 1,
            activePlantLevel = activePlant?.level ?: existing?.activePlantLevel ?: 1,
            activePlantProgress = activePlant?.progressPercent ?: existing?.activePlantProgress ?: 35,
            focusWorkMinutes = prefs[Keys.FOCUS_WORK_MINUTES] ?: 25,
            focusPauseMinutes = prefs[Keys.FOCUS_PAUSE_MINUTES] ?: 5,
            focusStartTime = prefs[Keys.FOCUS_START] ?: "21:00",
            focusEndTime = prefs[Keys.FOCUS_END] ?: "07:00",
            focusScope = prefs[Keys.FOCUS_SCOPE] ?: "Todas las apps",
            focusEnabled = prefs[Keys.FOCUS_ENABLED] ?: true,
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: false
        )
    }

    /**
     * Synchronizes the currently logged-in user's full stats and active plant state
     * into the `user_accounts` Room table so their progress is permanently saved.
     */
    private suspend fun syncCurrentUserSnapshotToRoom() {
        val updatedAccount = buildCurrentUserAccountEntity() ?: return
        dao.insertUserAccount(updatedAccount)
    }

    suspend fun signInOrRegisterWithEmailPin(
        email: String,
        pin6Digits: String,
        name: String = "",
        isNewRegistration: Boolean = true
    ) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return

        val existingAccount = dao.getUserAccountByEmail(cleanEmail)
        val resolvedName = name.trim()
            .ifBlank { existingAccount?.fullName ?: "" }
            .ifBlank { cleanEmail.substringBefore("@") }

        if (existingAccount != null && !isNewRegistration) {
            // Returning user login: restore their saved profile and plant state from Room
            seedDefaultTablesIfNeeded(
                activePlantId = existingAccount.activePlantId,
                activePlantLevel = existingAccount.activePlantLevel,
                activePlantProgress = existingAccount.activePlantProgress,
                forceReset = false
            )
            dao.setActivePlant(existingAccount.activePlantId)
            val currentActive = dao.getActivePlantSync()
            if (currentActive != null) {
                val safeLevel = existingAccount.activePlantLevel.coerceIn(1, 5)
                dao.updatePlant(
                    currentActive.copy(
                        level = safeLevel,
                        levelName = levelNameFor(safeLevel),
                        progressPercent = existingAccount.activePlantProgress.coerceIn(10, 100),
                        unlockedBenefit = unlockedBenefitFor(safeLevel),
                        isCurrent = true
                    )
                )
            }

            context.dataStore.edit { prefs ->
                prefs[Keys.IS_LOGGED_IN] = true
                prefs[Keys.USER_NAME] = resolvedName
                prefs[Keys.USER_EMAIL] = cleanEmail
                prefs[Keys.SAVED_PIN] = pin6Digits
                prefs[Keys.AUTH_PROVIDER] = "Correo + Clave 6 dígitos"
                prefs[Keys.USED_MINUTES] = existingAccount.usedMinutesToday
                prefs[Keys.GOAL_MINUTES] = existingAccount.dailyGoalMinutes
                prefs[Keys.STREAK_DAYS] = existingAccount.streakDays
                prefs[Keys.RECOVERED_MINUTES] = existingAccount.recoveredMinutes
                prefs[Keys.SAVED_MONEY] = existingAccount.savedMoneyDollars
                prefs[Keys.WALLET_BALANCE] = existingAccount.digitalWalletBalance
                prefs[Keys.ACCUMULATED_DEBT] = existingAccount.accumulatedDebtDollars
                prefs[Keys.WALLET_MODE] = WalletMode.PAYPAL.name
                prefs[Keys.PAYMENT_METHOD_LABEL] = "PayPal"
                prefs[Keys.PAYPAL_EMAIL] = existingAccount.paypalEmail.ifBlank { cleanEmail }
                prefs[Keys.FOCUS_WORK_MINUTES] = existingAccount.focusWorkMinutes
                prefs[Keys.FOCUS_PAUSE_MINUTES] = existingAccount.focusPauseMinutes
                prefs[Keys.FOCUS_START] = existingAccount.focusStartTime
                prefs[Keys.FOCUS_END] = existingAccount.focusEndTime
                prefs[Keys.FOCUS_SCOPE] = existingAccount.focusScope
                prefs[Keys.FOCUS_ENABLED] = existingAccount.focusEnabled
                prefs[Keys.IS_DARK_MODE] = existingAccount.isDarkMode
            }
            syncCurrentUserSnapshotToRoom()
        } else if (existingAccount != null) {
            // User tapped "Crear cuenta" with an email that already exists in Room -> preserve their saved data!
            seedDefaultTablesIfNeeded(
                activePlantId = existingAccount.activePlantId,
                activePlantLevel = existingAccount.activePlantLevel,
                activePlantProgress = existingAccount.activePlantProgress,
                forceReset = false
            )
            dao.setActivePlant(existingAccount.activePlantId)
            val currentActive = dao.getActivePlantSync()
            if (currentActive != null) {
                val safeLevel = existingAccount.activePlantLevel.coerceIn(1, 5)
                dao.updatePlant(
                    currentActive.copy(
                        level = safeLevel,
                        levelName = levelNameFor(safeLevel),
                        progressPercent = existingAccount.activePlantProgress.coerceIn(10, 100),
                        unlockedBenefit = unlockedBenefitFor(safeLevel),
                        isCurrent = true
                    )
                )
            }
            context.dataStore.edit { prefs ->
                prefs[Keys.IS_LOGGED_IN] = true
                prefs[Keys.USER_NAME] = resolvedName
                prefs[Keys.USER_EMAIL] = cleanEmail
                prefs[Keys.SAVED_PIN] = pin6Digits
                prefs[Keys.AUTH_PROVIDER] = "Correo + Clave 6 dígitos"
                prefs[Keys.USED_MINUTES] = existingAccount.usedMinutesToday
                prefs[Keys.GOAL_MINUTES] = existingAccount.dailyGoalMinutes
                prefs[Keys.STREAK_DAYS] = existingAccount.streakDays
                prefs[Keys.RECOVERED_MINUTES] = existingAccount.recoveredMinutes
                prefs[Keys.SAVED_MONEY] = existingAccount.savedMoneyDollars
                prefs[Keys.WALLET_BALANCE] = existingAccount.digitalWalletBalance
                prefs[Keys.ACCUMULATED_DEBT] = existingAccount.accumulatedDebtDollars
                prefs[Keys.WALLET_MODE] = WalletMode.PAYPAL.name
                prefs[Keys.PAYMENT_METHOD_LABEL] = "PayPal"
                prefs[Keys.PAYPAL_EMAIL] = existingAccount.paypalEmail.ifBlank { cleanEmail }
                prefs[Keys.FOCUS_WORK_MINUTES] = existingAccount.focusWorkMinutes
                prefs[Keys.FOCUS_PAUSE_MINUTES] = existingAccount.focusPauseMinutes
                prefs[Keys.FOCUS_START] = existingAccount.focusStartTime
                prefs[Keys.FOCUS_END] = existingAccount.focusEndTime
                prefs[Keys.FOCUS_SCOPE] = existingAccount.focusScope
                prefs[Keys.FOCUS_ENABLED] = existingAccount.focusEnabled
                prefs[Keys.IS_DARK_MODE] = existingAccount.isDarkMode
            }
            syncCurrentUserSnapshotToRoom()
        } else {
            // Brand new user registration
            seedDefaultTablesIfNeeded(
                activePlantId = 1,
                activePlantLevel = 1,
                activePlantProgress = 35,
                forceReset = dao.getPlantsCount() == 0
            )
            dao.setActivePlant(1)
            val starterPlant = dao.getActivePlantSync()
            if (starterPlant != null && starterPlant.progressPercent < 35) {
                dao.updatePlant(
                    starterPlant.copy(
                        level = 1,
                        levelName = "Semilla",
                        progressPercent = 35,
                        isCurrent = true
                    )
                )
            }

            context.dataStore.edit { prefs ->
                prefs[Keys.IS_LOGGED_IN] = true
                prefs[Keys.USER_NAME] = resolvedName
                prefs[Keys.USER_EMAIL] = cleanEmail
                prefs[Keys.SAVED_PIN] = pin6Digits
                prefs[Keys.AUTH_PROVIDER] = "Correo + Clave 6 dígitos"
                prefs[Keys.USED_MINUTES] = 0
                prefs[Keys.GOAL_MINUTES] = 120
                prefs[Keys.STREAK_DAYS] = 1
                prefs[Keys.RECOVERED_MINUTES] = 0
                prefs[Keys.SAVED_MONEY] = 0.00
                prefs[Keys.WALLET_BALANCE] = 0.00
                prefs[Keys.ACCUMULATED_DEBT] = 0.00
                prefs[Keys.WALLET_MODE] = WalletMode.PAYPAL.name
                prefs[Keys.PAYMENT_METHOD_LABEL] = "PayPal"
                prefs[Keys.PAYPAL_EMAIL] = cleanEmail
                prefs[Keys.LAST_PAYPAL_RECEIPT] = ""
                prefs[Keys.FOCUS_WORK_MINUTES] = 25
                prefs[Keys.FOCUS_PAUSE_MINUTES] = 5
            }
            syncCurrentUserSnapshotToRoom()
        }
    }

    suspend fun signOutAccount() {
        // Save the current user's snapshot to Room BEFORE logging out so no data is ever lost
        syncCurrentUserSnapshotToRoom()
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
        }
    }

    suspend fun ensureSeeded() {
        seedDefaultTablesIfNeeded(forceReset = false)
        val allPlants = dao.getAllPlantsSync()
        if (allPlants.isNotEmpty() && allPlants.none { it.isCurrent }) {
            dao.setActivePlant(allPlants.first().id)
        }
        val currentActive = dao.getActivePlantSync()
        if (currentActive != null && currentActive.progressPercent <= 0) {
            dao.updatePlant(currentActive.copy(progressPercent = 35))
        }
    }

    /**
     * Reads real foreground app usage from UsageStatsManager (when permission is granted)
     * and persists the updated minutes into Room (`app_limits`) and DataStore (`USED_MINUTES`).
     */
    suspend fun syncRealUsageStatsToDatabase() {
        if (!PausaBlockerController.hasUsageStatsPermission(context)) return
        val limits = dao.getAllAppLimitsSync()
        if (limits.isEmpty()) return

        var totalSocialMinutesToday = 0
        for (limit in limits) {
            val packages = PausaBlockerController.resolvePackagesForLimit(limit)
            if (packages.isNotEmpty()) {
                val realMinutes = PausaBlockerController.queryTodayUsageMinutesForPackages(context, packages)
                val updatedUsed = maxOf(limit.usedMinutesToday, realMinutes)
                if (updatedUsed != limit.usedMinutesToday) {
                    dao.updateAppLimit(limit.copy(usedMinutesToday = updatedUsed))
                }
                totalSocialMinutesToday += updatedUsed
            }
        }

        if (totalSocialMinutesToday > 0) {
            context.dataStore.edit { prefs ->
                val currentTotal = prefs[Keys.USED_MINUTES] ?: 0
                if (totalSocialMinutesToday > currentTotal) {
                    prefs[Keys.USED_MINUTES] = totalSocialMinutesToday
                }
            }
            syncCurrentUserSnapshotToRoom()
        }
    }

    suspend fun toggleAppLimit(limit: AppLimitEntity, enabled: Boolean) {
        dao.updateAppLimit(limit.copy(isEnabled = enabled))
    }

    suspend fun updateAppLimitMinutes(limit: AppLimitEntity, newMinutes: Int) {
        dao.updateAppLimit(limit.copy(limitMinutes = newMinutes, isEnabled = newMinutes > 0 || limit.isEnabled))
    }

    suspend fun addCustomAppLimit(appName: String, limitMinutes: Int) {
        val id = appName.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis()
        dao.insertAppLimit(
            AppLimitEntity(
                id = id,
                appName = appName,
                limitMinutes = limitMinutes,
                usedMinutesToday = 0,
                isEnabled = true,
                brandColorHex = 0xFF2D5E46,
                iconType = "custom"
            )
        )
    }

    suspend fun updateFocusIntervals(focusMinutes: Int, pauseMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FOCUS_WORK_MINUTES] = focusMinutes.coerceIn(5, 120)
            prefs[Keys.FOCUS_PAUSE_MINUTES] = pauseMinutes.coerceIn(1, 60)
        }
        syncCurrentUserSnapshotToRoom()
    }

    suspend fun updateFocusSchedule(start: String, end: String, scope: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FOCUS_START] = start
            prefs[Keys.FOCUS_END] = end
            prefs[Keys.FOCUS_SCOPE] = scope
            prefs[Keys.FOCUS_ENABLED] = enabled
        }
        syncCurrentUserSnapshotToRoom()
    }

    suspend fun updateDailyGoal(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GOAL_MINUTES] = minutes.coerceIn(30, 480)
        }
        syncCurrentUserSnapshotToRoom()
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_MODE] = enabled
        }
        syncCurrentUserSnapshotToRoom()
    }

    suspend fun advanceStreakDay(daysToAdd: Int = 1) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.STREAK_DAYS] ?: 0
            val next = if (current >= 30 && daysToAdd > 0) 1 else (current + daysToAdd).coerceIn(0, 60)
            prefs[Keys.STREAK_DAYS] = next
        }
        syncCurrentUserSnapshotToRoom()
    }

    suspend fun setWalletMode(mode: WalletMode, paymentLabel: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WALLET_MODE] = mode.name
            prefs[Keys.PAYMENT_METHOD_LABEL] = paymentLabel
        }
        syncCurrentUserSnapshotToRoom()
    }

    suspend fun updatePayPalEmail(email: String) {
        val clean = email.trim()
        if (clean.isBlank()) return
        context.dataStore.edit { prefs ->
            prefs[Keys.PAYPAL_EMAIL] = clean
            prefs[Keys.PAYMENT_METHOD_LABEL] = "PayPal"
            prefs[Keys.WALLET_MODE] = WalletMode.PAYPAL.name
        }
        syncCurrentUserSnapshotToRoom()
    }

    private var txSequenceCounter = 0

    private fun nextOrderReference(prefix: String): String {
        txSequenceCounter = (txSequenceCounter + 1) % 1000
        val timePart = System.currentTimeMillis() % 1000000
        return if (txSequenceCounter == 1) {
            "$prefix-$timePart"
        } else {
            "$prefix-$timePart-$txSequenceCounter"
        }
    }

    /**
     * Initiates a PENDING PayPal recharge order in the Room database (e.g., when opening PayPal Web Checkout
     * before final confirmation, or when staging a recharge order).
     * Returns the generated `orderReference` so it can be confirmed (`COMPLETED`) or cancelled (`CANCELLED`).
     */
    suspend fun createPendingPayPalRecharge(
        amount: Double,
        skuId: String,
        paypalAccount: String = "",
        methodLabel: String = "PayPal Checkout Web"
    ): String {
        val prefsSnapshot = context.dataStore.data.first()
        val currentBalance = prefsSnapshot[Keys.WALLET_BALANCE] ?: 0.00
        val existingEmail = prefsSnapshot[Keys.PAYPAL_EMAIL] ?: prefsSnapshot[Keys.USER_EMAIL] ?: "usuario@paypal.com"
        val resolvedPayPalAccount = paypalAccount.trim().ifBlank {
            existingEmail.ifBlank { "usuario@paypal.com" }
        }
        val refCode = nextOrderReference("PP")
        val now = System.currentTimeMillis()

        if (amount <= 0.0) {
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    timestamp = now,
                    updatedAt = now,
                    type = "TOP_UP_PAYPAL",
                    status = TransactionStatus.FAILED.name,
                    title = "Recarga rechazada vía $methodLabel",
                    subtitle = "Monto inválido ($${String.format(java.util.Locale.US, "%.2f", amount)} USD)",
                    amount = amount,
                    orderReference = refCode,
                    payerEmail = resolvedPayPalAccount,
                    merchantEmail = PAYPAL_MERCHANT_EMAIL,
                    skuId = skuId,
                    balanceAfter = currentBalance
                )
            )
            return refCode
        }

        context.dataStore.edit { prefs ->
            prefs[Keys.PAYPAL_EMAIL] = resolvedPayPalAccount
            prefs[Keys.PAYMENT_METHOD_LABEL] = methodLabel
            prefs[Keys.WALLET_MODE] = WalletMode.PAYPAL.name
            prefs[Keys.LAST_PAYPAL_RECEIPT] = refCode
        }

        dao.insertWalletTransaction(
            WalletTransactionEntity(
                timestamp = now,
                updatedAt = now,
                type = "TOP_UP_PAYPAL_WEB",
                status = TransactionStatus.PENDING.name,
                title = "Recarga en proceso vía $methodLabel",
                subtitle = "Pendiente de confirmación PayPal ($resolvedPayPalAccount → $PAYPAL_MERCHANT_EMAIL)",
                amount = amount,
                orderReference = refCode,
                payerEmail = resolvedPayPalAccount,
                merchantEmail = PAYPAL_MERCHANT_EMAIL,
                skuId = skuId,
                balanceAfter = currentBalance
            )
        )
        syncCurrentUserSnapshotToRoom()
        return refCode
    }

    /**
     * Confirms a `PENDING` PayPal transaction in the Room database, transitioning its status
     * atomically from `PENDING` -> `COMPLETED`, crediting the user's digital wallet balance
     * (and settling any unpaid broken-pause penalty first), and updating `user_accounts`.
     */
    suspend fun confirmPendingPayPalTransaction(orderReference: String): Boolean {
        val existingTx = dao.getWalletTransactionByReference(orderReference) ?: return false
        if (existingTx.status != TransactionStatus.PENDING.name) return false

        val amount = existingTx.amount
        if (amount <= 0.0) {
            dao.updateWalletTransactionStatus(
                orderReference = orderReference,
                status = TransactionStatus.FAILED.name,
                subtitle = "Monto inválido al confirmar orden $orderReference",
                balanceAfter = existingTx.balanceAfter,
                updatedAt = System.currentTimeMillis()
            )
            return false
        }

        var settledDebt = 0.0
        var finalBalance = 0.0
        var resolvedPayPalAccount = existingTx.payerEmail

        context.dataStore.edit { prefs ->
            val currentBalance = prefs[Keys.WALLET_BALANCE] ?: 0.00
            val currentDebt = prefs[Keys.ACCUMULATED_DEBT] ?: 0.00
            val existingEmail = prefs[Keys.PAYPAL_EMAIL] ?: prefs[Keys.USER_EMAIL] ?: "usuario@paypal.com"
            if (resolvedPayPalAccount.isBlank()) {
                resolvedPayPalAccount = existingEmail.ifBlank { "usuario@paypal.com" }
            } else {
                prefs[Keys.PAYPAL_EMAIL] = resolvedPayPalAccount
            }

            if (currentDebt > 0.0) {
                if (amount >= currentDebt) {
                    settledDebt = currentDebt
                    val remaining = amount - currentDebt
                    finalBalance = currentBalance + remaining
                    prefs[Keys.ACCUMULATED_DEBT] = 0.0
                    prefs[Keys.WALLET_BALANCE] = finalBalance
                } else {
                    settledDebt = amount
                    finalBalance = currentBalance
                    prefs[Keys.ACCUMULATED_DEBT] = currentDebt - amount
                }
            } else {
                finalBalance = currentBalance + amount
                prefs[Keys.WALLET_BALANCE] = finalBalance
            }
            prefs[Keys.PAYMENT_METHOD_LABEL] = "PayPal"
            prefs[Keys.WALLET_MODE] = WalletMode.PAYPAL.name
            prefs[Keys.LAST_PAYPAL_RECEIPT] = orderReference
        }

        val completedSubtitle = if (settledDebt > 0.0) {
            "Confirmada en PayPal ($resolvedPayPalAccount → $PAYPAL_MERCHANT_EMAIL) · Saldó penalidad (-$${String.format(java.util.Locale.US, "%.2f", settledDebt)}) · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}"
        } else {
            "Confirmada en PayPal ($resolvedPayPalAccount → $PAYPAL_MERCHANT_EMAIL) · Recibo $orderReference · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}"
        }

        val updatedUserAccount = buildCurrentUserAccountEntity()
        dao.completeTransactionAndSyncAccount(
            orderReference = orderReference,
            completedSubtitle = completedSubtitle,
            balanceAfter = finalBalance,
            updatedAt = System.currentTimeMillis(),
            settledPenaltyDebt = settledDebt > 0.0,
            updatedAccount = updatedUserAccount
        )
        return true
    }

    /**
     * Cancels a `PENDING` PayPal transaction in the Room database, updating its status
     * to `CANCELLED` without altering the user's digital wallet balance.
     */
    suspend fun cancelPendingPayPalTransaction(orderReference: String): Boolean {
        val existingTx = dao.getWalletTransactionByReference(orderReference) ?: return false
        if (existingTx.status != TransactionStatus.PENDING.name) return false

        val currentBalance = context.dataStore.data.first()[Keys.WALLET_BALANCE] ?: existingTx.balanceAfter
        dao.updateWalletTransactionStatus(
            orderReference = orderReference,
            status = TransactionStatus.CANCELLED.name,
            subtitle = "Recarga cancelada por el usuario ($orderReference)",
            balanceAfter = currentBalance,
            updatedAt = System.currentTimeMillis()
        )
        return true
    }

    /**
     * Processes a PayPal balance recharge or pause-break penalty payment with full database state tracking:
     * 1. Records the transaction initially in `PENDING` state in `wallet_transactions`.
     * 2. Computes wallet balance and settles any unpaid pause-break penalty (`accumulatedDebtDollars`).
     * 3. Atomically updates the transaction status in Room to `COMPLETED` (`COMPLETADA`) along with `balanceAfter`,
     *    settles any pending penalty transactions, and syncs `user_accounts`.
     */
    suspend fun processCompliantTopUp(
        amount: Double,
        skuId: String,
        methodLabel: String,
        isFreeCommitment: Boolean,
        paypalAccount: String = ""
    ) {
        val isPayPal = methodLabel.contains("PayPal", ignoreCase = true)
        val refCode = when {
            isFreeCommitment -> nextOrderReference("FREE")
            isPayPal -> nextOrderReference("PP")
            else -> nextOrderReference("HMS")
        }
        val now = System.currentTimeMillis()
        val initialPrefs = context.dataStore.data.first()
        val initialBalance = initialPrefs[Keys.WALLET_BALANCE] ?: 0.00
        val fallbackEmail = initialPrefs[Keys.PAYPAL_EMAIL] ?: initialPrefs[Keys.USER_EMAIL] ?: "usuario@paypal.com"
        var resolvedPayPalAccount = paypalAccount.trim().ifBlank {
            fallbackEmail.ifBlank { "usuario@paypal.com" }
        }

        if (amount <= 0.0) {
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    timestamp = now,
                    updatedAt = now,
                    type = if (isPayPal) "TOP_UP_PAYPAL" else "TOP_UP_HMS",
                    status = TransactionStatus.FAILED.name,
                    title = "Recarga fallida vía $methodLabel",
                    subtitle = "Monto inválido ($${String.format(java.util.Locale.US, "%.2f", amount)} USD)",
                    amount = amount,
                    orderReference = refCode,
                    payerEmail = resolvedPayPalAccount,
                    merchantEmail = PAYPAL_MERCHANT_EMAIL,
                    skuId = skuId,
                    balanceAfter = initialBalance
                )
            )
            return
        }

        val txType = when {
            isFreeCommitment -> "TOP_UP_FREE"
            isPayPal -> "TOP_UP_PAYPAL"
            else -> "TOP_UP_HMS"
        }

        // Step 1: Insert transaction in PENDING state in Room before balance mutation
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                timestamp = now,
                updatedAt = now,
                type = txType,
                status = TransactionStatus.PENDING.name,
                title = if (isFreeCommitment) {
                    "Saldo de Compromiso Personal"
                } else {
                    "Recarga vía $methodLabel"
                },
                subtitle = "Procesando pago ($resolvedPayPalAccount → $PAYPAL_MERCHANT_EMAIL)...",
                amount = amount,
                orderReference = refCode,
                payerEmail = resolvedPayPalAccount,
                merchantEmail = PAYPAL_MERCHANT_EMAIL,
                skuId = skuId,
                balanceAfter = initialBalance
            )
        )

        var settledDebt = 0.0
        var finalBalance = initialBalance

        // Step 2: Update DataStore preferences (balance & debt settlement)
        context.dataStore.edit { prefs ->
            val currentBalance = prefs[Keys.WALLET_BALANCE] ?: 0.00
            val currentDebt = prefs[Keys.ACCUMULATED_DEBT] ?: 0.00
            val existingEmail = prefs[Keys.PAYPAL_EMAIL] ?: prefs[Keys.USER_EMAIL] ?: "usuario@paypal.com"
            if (resolvedPayPalAccount.isBlank()) {
                resolvedPayPalAccount = existingEmail.ifBlank { "usuario@paypal.com" }
            } else {
                prefs[Keys.PAYPAL_EMAIL] = resolvedPayPalAccount
            }

            if (currentDebt > 0.0) {
                if (amount >= currentDebt) {
                    settledDebt = currentDebt
                    val remaining = amount - currentDebt
                    finalBalance = currentBalance + remaining
                    prefs[Keys.ACCUMULATED_DEBT] = 0.0
                    prefs[Keys.WALLET_BALANCE] = finalBalance
                } else {
                    settledDebt = amount
                    finalBalance = currentBalance
                    prefs[Keys.ACCUMULATED_DEBT] = currentDebt - amount
                }
            } else {
                finalBalance = currentBalance + amount
                prefs[Keys.WALLET_BALANCE] = finalBalance
            }
            prefs[Keys.PAYMENT_METHOD_LABEL] = methodLabel
            prefs[Keys.WALLET_MODE] = when {
                isFreeCommitment -> WalletMode.COMPROMISO_LIBRE.name
                isPayPal -> WalletMode.PAYPAL.name
                else -> WalletMode.HUAWEI_IAP.name
            }
            prefs[Keys.LAST_PAYPAL_RECEIPT] = refCode
        }

        val completedSubtitle = when {
            isFreeCommitment -> "Saldo de compromiso personal ($skuId) · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}"
            settledDebt > 0.0 -> "PayPal ($resolvedPayPalAccount → $PAYPAL_MERCHANT_EMAIL) · Saldó penalidad (-$${String.format(java.util.Locale.US, "%.2f", settledDebt)}) · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}"
            isPayPal -> "PayPal ($resolvedPayPalAccount → $PAYPAL_MERCHANT_EMAIL) · Recibo $refCode · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}"
            else -> "SKU: $skuId · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}"
        }

        // Step 3: Atomically transition transaction status to COMPLETED and sync UserAccountEntity in Room
        val updatedUserAccount = buildCurrentUserAccountEntity()
        dao.completeTransactionAndSyncAccount(
            orderReference = refCode,
            completedSubtitle = completedSubtitle,
            balanceAfter = finalBalance,
            updatedAt = System.currentTimeMillis(),
            settledPenaltyDebt = settledDebt > 0.0,
            updatedAccount = updatedUserAccount
        )
    }

    /**
     * Pays the pending broken-pause penalty ($1.00 USD or accumulatedDebtDollars) directly via PayPal
     * to the merchant account (dr.alejandrolopez19@gmail.com) and updates both the new payment
     * transaction and any previously PENDING penalty transactions to COMPLETED in Room.
     */
    suspend fun payBrokenPauseWithPayPal(paypalAccount: String = "") {
        var amountPaid = 1.00
        var finalBalance = 0.0
        var resolvedAccount = paypalAccount.trim()
        val refCode = nextOrderReference("PP-PAUSA")
        val now = System.currentTimeMillis()

        val initialPrefs = context.dataStore.data.first()
        val initialBalance = initialPrefs[Keys.WALLET_BALANCE] ?: 0.00
        val initialDebt = initialPrefs[Keys.ACCUMULATED_DEBT] ?: 0.00
        val fallbackEmail = initialPrefs[Keys.PAYPAL_EMAIL] ?: initialPrefs[Keys.USER_EMAIL] ?: "usuario@paypal.com"
        if (resolvedAccount.isBlank()) {
            resolvedAccount = fallbackEmail.ifBlank { "usuario@paypal.com" }
        }

        // Step 1: Insert transaction in PENDING state
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                timestamp = now,
                updatedAt = now,
                type = "PAYPAL_PENALTY_PAYMENT",
                status = TransactionStatus.PENDING.name,
                title = "Pago PayPal · Pausa rota",
                subtitle = "Procesando pago de penalidad ($resolvedAccount → $PAYPAL_MERCHANT_EMAIL)...",
                amount = if (initialDebt > 0.0) initialDebt else (initialPrefs[Keys.PENALTY_AMOUNT] ?: 1.00),
                orderReference = refCode,
                payerEmail = resolvedAccount,
                merchantEmail = PAYPAL_MERCHANT_EMAIL,
                skuId = "com.aistudio.pausa.paypal.100",
                balanceAfter = initialBalance
            )
        )

        // Step 2: Update DataStore
        context.dataStore.edit { prefs ->
            val currentDebt = prefs[Keys.ACCUMULATED_DEBT] ?: 0.00
            val penalty = prefs[Keys.PENALTY_AMOUNT] ?: 1.00
            val currentBalance = prefs[Keys.WALLET_BALANCE] ?: 0.00
            val existingEmail = prefs[Keys.PAYPAL_EMAIL] ?: prefs[Keys.USER_EMAIL] ?: "usuario@paypal.com"

            if (resolvedAccount.isBlank()) {
                resolvedAccount = existingEmail.ifBlank { "usuario@paypal.com" }
            } else {
                prefs[Keys.PAYPAL_EMAIL] = resolvedAccount
            }

            if (currentDebt > 0.0) {
                amountPaid = currentDebt
                finalBalance = currentBalance
                prefs[Keys.ACCUMULATED_DEBT] = 0.0
            } else {
                amountPaid = penalty
                finalBalance = currentBalance + penalty
                prefs[Keys.WALLET_BALANCE] = finalBalance
            }
            prefs[Keys.PAYMENT_METHOD_LABEL] = "PayPal"
            prefs[Keys.WALLET_MODE] = WalletMode.PAYPAL.name
            prefs[Keys.LAST_PAYPAL_RECEIPT] = refCode
        }

        // Step 3: Atomically mark payment COMPLETED, settle pending penalty records, and sync UserAccountEntity
        val updatedUserAccount = buildCurrentUserAccountEntity()
        dao.completeTransactionAndSyncAccount(
            orderReference = refCode,
            completedSubtitle = "Penalidad pagada ($resolvedAccount → $PAYPAL_MERCHANT_EMAIL) · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}",
            balanceAfter = finalBalance,
            updatedAt = System.currentTimeMillis(),
            settledPenaltyDebt = true,
            updatedAccount = updatedUserAccount
        )
    }

    /**
     * Mandatory Pay-to-Break-Pause flow:
     * Charges the selected PayPal package (minimum $1.00 USD) to the merchant account (dr.alejandrolopez19@gmail.com),
     * deducts the broken pause penalty ($1.00 USD) immediately, credits any remaining amount to wallet balance,
     * and records the pause break with COMPLETED status in Room.
     */
    suspend fun payAndBreakPauseWithPayPal(
        appName: String,
        activePlant: PlantEntity?,
        packageAmount: Double,
        skuId: String,
        methodLabel: String,
        paypalAccount: String = ""
    ) {
        var appliedPenalty = 1.00
        var finalBalance = 0.0
        var resolvedAccount = paypalAccount.trim()
        val topUpReceiptCode = nextOrderReference("PP-TOP")
        val breakReceiptCode = nextOrderReference("PP-BRK")
        val now = System.currentTimeMillis()

        context.dataStore.edit { prefs ->
            val penalty = prefs[Keys.PENALTY_AMOUNT] ?: 1.00
            appliedPenalty = penalty
            val balance = prefs[Keys.WALLET_BALANCE] ?: 0.00
            val debt = prefs[Keys.ACCUMULATED_DEBT] ?: 0.00
            val used = prefs[Keys.USED_MINUTES] ?: 0
            val existingEmail = prefs[Keys.PAYPAL_EMAIL] ?: prefs[Keys.USER_EMAIL] ?: "usuario@paypal.com"

            if (resolvedAccount.isBlank()) {
                resolvedAccount = existingEmail.ifBlank { "usuario@paypal.com" }
            } else {
                prefs[Keys.PAYPAL_EMAIL] = resolvedAccount
            }

            val totalAvailable = balance + packageAmount
            val totalRequired = penalty + debt
            if (totalAvailable >= totalRequired) {
                finalBalance = totalAvailable - totalRequired
                prefs[Keys.WALLET_BALANCE] = finalBalance
                prefs[Keys.ACCUMULATED_DEBT] = 0.0
            } else {
                finalBalance = 0.0
                prefs[Keys.WALLET_BALANCE] = 0.0
                prefs[Keys.ACCUMULATED_DEBT] = (totalRequired - totalAvailable).coerceAtLeast(0.0)
            }

            prefs[Keys.USED_MINUTES] = used + 10
            prefs[Keys.PAYMENT_METHOD_LABEL] = methodLabel
            prefs[Keys.WALLET_MODE] = WalletMode.PAYPAL.name
            prefs[Keys.LAST_PAYPAL_RECEIPT] = breakReceiptCode
        }

        if (packageAmount > appliedPenalty) {
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    timestamp = now,
                    updatedAt = now,
                    type = "TOP_UP_PAYPAL",
                    status = TransactionStatus.COMPLETED.name,
                    title = "Recarga y Desbloqueo vía PayPal ($skuId)",
                    subtitle = "Pago a $PAYPAL_MERCHANT_EMAIL desde $resolvedAccount · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}",
                    amount = packageAmount,
                    orderReference = topUpReceiptCode,
                    payerEmail = resolvedAccount,
                    merchantEmail = PAYPAL_MERCHANT_EMAIL,
                    skuId = skuId,
                    balanceAfter = finalBalance
                )
            )
        }

        dao.insertPauseLog(
            PauseLogEntity(
                timestamp = now,
                appName = appName,
                outcome = "BROKEN",
                penaltyAmount = appliedPenalty,
                minutesSaved = 0
            )
        )

        dao.insertWalletTransaction(
            WalletTransactionEntity(
                timestamp = now + 1,
                updatedAt = now + 1,
                type = "PAYPAL_PENALTY_PAYMENT",
                status = TransactionStatus.COMPLETED.name,
                title = "Desbloqueo de pausa ($appName)",
                subtitle = "Pagado vía PayPal ($resolvedAccount → $PAYPAL_MERCHANT_EMAIL) · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}",
                amount = -appliedPenalty,
                orderReference = breakReceiptCode,
                payerEmail = resolvedAccount,
                merchantEmail = PAYPAL_MERCHANT_EMAIL,
                skuId = skuId,
                balanceAfter = finalBalance
            )
        )

        val targetPlant = activePlant ?: dao.getActivePlantSync()
        if (targetPlant != null) {
            val reducedProgress = (targetPlant.progressPercent - 18).coerceAtLeast(10)
            dao.updatePlant(
                targetPlant.copy(progressPercent = reducedProgress)
            )
        }
        PausaBlockerController.grantPaidUnlockPass(context, appName, durationMinutes = 15)
        val updatedUserAccount = buildCurrentUserAccountEntity()
        dao.completeTransactionAndSyncAccount(
            orderReference = breakReceiptCode,
            completedSubtitle = "Pagado vía PayPal ($resolvedAccount → $PAYPAL_MERCHANT_EMAIL) · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}",
            balanceAfter = finalBalance,
            updatedAt = System.currentTimeMillis(),
            settledPenaltyDebt = true,
            updatedAccount = updatedUserAccount
        )
    }

    suspend fun topUpWallet(amount: Double) {
        processCompliantTopUp(
            amount = amount,
            skuId = "com.aistudio.pausa.paypal.500",
            methodLabel = "PayPal",
            isFreeCommitment = false
        )
    }

    suspend fun selectActivePlant(plantId: Int) {
        dao.setActivePlant(plantId)
        syncCurrentUserSnapshotToRoom()
    }

    /**
     * Directly nurtures/waters the active plant (+bonusPercent growth, +bonusRecoveredMinutes),
     * leveling up the plant automatically if progress reaches 100%, and saving to Room.
     */
    suspend fun nurtureActivePlant(
        bonusPercent: Int = 25,
        bonusRecoveredMinutes: Int = 10
    ) {
        val currentPlant = dao.getActivePlantSync() ?: dao.getAllPlantsSync().firstOrNull()
        recordPauseRespected(
            appName = "Cuidado de tu planta",
            activePlant = currentPlant,
            minutesSaved = bonusRecoveredMinutes,
            plantBonusPercent = bonusPercent
        )
    }

    /**
     * Saves a chosen evolution stage (level 1..5 and progress 10..100%) onto the active plant in Room.
     */
    suspend fun saveActivePlantEvolutionStage(targetLevel: Int, targetProgressPercent: Int) {
        val currentPlant = dao.getActivePlantSync() ?: dao.getAllPlantsSync().firstOrNull() ?: return
        val safeLevel = targetLevel.coerceIn(1, 5)
        val safeProgress = targetProgressPercent.coerceIn(10, 100)
        dao.updatePlant(
            currentPlant.copy(
                level = safeLevel,
                levelName = levelNameFor(safeLevel),
                progressPercent = safeProgress,
                unlockedBenefit = unlockedBenefitFor(safeLevel),
                isCurrent = true
            )
        )
        syncCurrentUserSnapshotToRoom()
    }

    suspend fun recordPauseRespected(
        appName: String,
        activePlant: PlantEntity?,
        minutesSaved: Int = 15,
        plantBonusPercent: Int = 15
    ) {
        context.dataStore.edit { prefs ->
            val currentRecovered = prefs[Keys.RECOVERED_MINUTES] ?: 0
            val currentSaved = prefs[Keys.SAVED_MONEY] ?: 0.00
            val penalty = prefs[Keys.PENALTY_AMOUNT] ?: 1.00
            val currentStreak = prefs[Keys.STREAK_DAYS] ?: 0
            prefs[Keys.RECOVERED_MINUTES] = currentRecovered + minutesSaved
            prefs[Keys.SAVED_MONEY] = currentSaved + penalty
            if (currentStreak == 0) {
                prefs[Keys.STREAK_DAYS] = 1
            }
        }
        dao.insertPauseLog(
            PauseLogEntity(
                timestamp = System.currentTimeMillis(),
                appName = appName,
                outcome = "RESPECTED",
                penaltyAmount = 0.0,
                minutesSaved = minutesSaved
            )
        )
        val targetPlant = activePlant ?: dao.getActivePlantSync() ?: dao.getAllPlantsSync().firstOrNull()
        if (targetPlant != null) {
            val rawTotal = targetPlant.progressPercent + plantBonusPercent
            val leveledUp = rawTotal >= 100 && targetPlant.level < 5
            val nextLevel = if (leveledUp) (targetPlant.level + 1).coerceAtMost(5) else targetPlant.level
            val nextLevelName = levelNameFor(nextLevel)
            val nextProgress = when {
                leveledUp -> (rawTotal - 100).coerceIn(25, 95)
                nextLevel >= 5 && rawTotal >= 100 -> 100
                else -> rawTotal.coerceIn(10, 99)
            }
            dao.updatePlant(
                targetPlant.copy(
                    level = nextLevel,
                    levelName = nextLevelName,
                    progressPercent = nextProgress,
                    unlockedBenefit = unlockedBenefitFor(nextLevel),
                    isCurrent = true
                )
            )
        }
        syncCurrentUserSnapshotToRoom()
    }

    /**
     * Applies the $1.00 penalty when a pause is broken using existing prepaid PayPal balance.
     * Records status = COMPLETED if covered by prepaid balance, or status = PENDING if debt is accrued.
     */
    suspend fun recordPauseBroken(appName: String, activePlant: PlantEntity?) {
        var appliedPenalty = 1.00
        var coveredByPayPalBalance = false
        var finalBalance = 0.0
        var paypalAccount = "PayPal"
        val receiptCode = nextOrderReference("PP-BRK")
        val now = System.currentTimeMillis()

        context.dataStore.edit { prefs ->
            val penalty = prefs[Keys.PENALTY_AMOUNT] ?: 1.00
            appliedPenalty = penalty
            val balance = prefs[Keys.WALLET_BALANCE] ?: 0.00
            val debt = prefs[Keys.ACCUMULATED_DEBT] ?: 0.00
            val used = prefs[Keys.USED_MINUTES] ?: 0
            paypalAccount = (prefs[Keys.PAYPAL_EMAIL] ?: prefs[Keys.USER_EMAIL] ?: "").ifBlank { "PayPal" }

            if (balance >= penalty) {
                finalBalance = balance - penalty
                prefs[Keys.WALLET_BALANCE] = finalBalance
                coveredByPayPalBalance = true
            } else {
                val shortfall = penalty - balance
                finalBalance = 0.0
                prefs[Keys.WALLET_BALANCE] = 0.0
                prefs[Keys.ACCUMULATED_DEBT] = debt + shortfall
                coveredByPayPalBalance = false
            }
            prefs[Keys.USED_MINUTES] = used + 10
            prefs[Keys.PAYMENT_METHOD_LABEL] = "PayPal"
            prefs[Keys.WALLET_MODE] = WalletMode.PAYPAL.name
            prefs[Keys.LAST_PAYPAL_RECEIPT] = receiptCode
        }

        if (coveredByPayPalBalance) {
            PausaBlockerController.grantPaidUnlockPass(context, appName, durationMinutes = 15)
        }

        dao.insertPauseLog(
            PauseLogEntity(
                timestamp = now,
                appName = appName,
                outcome = "BROKEN",
                penaltyAmount = appliedPenalty,
                minutesSaved = 0
            )
        )

        dao.insertWalletTransaction(
            WalletTransactionEntity(
                timestamp = now,
                updatedAt = now,
                type = if (coveredByPayPalBalance) "PENALTY_PAYPAL_BALANCE" else "PENALTY_PAYPAL_PENDING",
                status = if (coveredByPayPalBalance) TransactionStatus.COMPLETED.name else TransactionStatus.PENDING.name,
                title = "Penalidad pausa rota ($appName)",
                subtitle = if (coveredByPayPalBalance) {
                    "Descontada de saldo PayPal ($paypalAccount → $PAYPAL_MERCHANT_EMAIL) · Saldo: $${String.format(java.util.Locale.US, "%.2f", finalBalance)}"
                } else {
                    "Cobro pendiente vía PayPal ($paypalAccount → $PAYPAL_MERCHANT_EMAIL)"
                },
                amount = -appliedPenalty,
                orderReference = receiptCode,
                payerEmail = paypalAccount,
                merchantEmail = PAYPAL_MERCHANT_EMAIL,
                skuId = "com.aistudio.pausa.paypal.100",
                balanceAfter = finalBalance
            )
        )

        val targetPlant = activePlant ?: dao.getActivePlantSync()
        if (targetPlant != null) {
            val reducedProgress = (targetPlant.progressPercent - 18).coerceAtLeast(10)
            dao.updatePlant(
                targetPlant.copy(progressPercent = reducedProgress)
            )
        }
        syncCurrentUserSnapshotToRoom()
    }

    val appContext: Context
        get() = context.applicationContext

    private fun levelNameFor(level: Int): String = when (level) {
        1 -> "Semilla"
        2 -> "Brote"
        3 -> "En crecimiento"
        4 -> "Floreciendo"
        else -> "Árbol"
    }

    private fun unlockedBenefitFor(level: Int): String = when (level) {
        1 -> "Inicio del hábito consciente"
        2 -> "Respiración guiada desbloqueada"
        3 -> "Simboliza tu atención y energía recuperada"
        4 -> "Modo concentración profunda"
        else -> "Maestría total sin scroll impulsivo"
    }
}
