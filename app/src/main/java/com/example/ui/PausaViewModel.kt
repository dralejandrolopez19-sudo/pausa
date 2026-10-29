package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppLimitEntity
import com.example.data.DailyUsageBar
import com.example.data.HmsIapPackage
import com.example.data.PausaRepository
import com.example.data.PauseLogEntity
import com.example.data.PlantEntity
import com.example.data.UserStatsPreferences
import com.example.data.WalletMode
import com.example.data.WalletTransactionEntity
import com.example.data.officialHuaweiIapPackages
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PausaDestination {
    INICIO,
    LIMITES,
    PAUSA,
    PROGRESO,
    AJUSTES,
    TU_PLANTA,
    PAUSA_ROTA
}

enum class ProgresoPeriod {
    SEMANA,
    MES,
    TOTAL
}

data class PausaUiState(
    val currentDestination: PausaDestination = PausaDestination.INICIO,
    val navigationHistory: List<PausaDestination> = listOf(PausaDestination.INICIO),
    val stats: UserStatsPreferences = UserStatsPreferences(),
    val appLimits: List<AppLimitEntity> = emptyList(),
    val plants: List<PlantEntity> = emptyList(),
    val activePlant: PlantEntity? = null,
    val pauseLogs: List<PauseLogEntity> = emptyList(),
    val walletTransactions: List<WalletTransactionEntity> = emptyList(),
    val iapPackages: List<HmsIapPackage> = officialHuaweiIapPackages,
    val countdownSeconds: Int = 900,
    val totalPauseDurationSeconds: Int = 900,
    val isPauseTimerRunning: Boolean = false,
    val triggeredAppName: String = "Instagram",
    val selectedPeriod: ProgresoPeriod = ProgresoPeriod.SEMANA,
    val usageBars: List<DailyUsageBar> = defaultWeekBars,
    val showAllPlantsSheet: Boolean = false,
    val showAlternativeActivitySheet: Boolean = false,
    val showHuaweiIapSheet: Boolean = false,
    val selectedAlternativeActivity: String? = null,
    val hasUsageStatsPermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val isAccessibilityBlockerEnabled: Boolean = false
)

private val defaultWeekBars = listOf(
    DailyUsageBar("Lun", 205),
    DailyUsageBar("Mar", 175),
    DailyUsageBar("Mié", 190),
    DailyUsageBar("Jue", 160),
    DailyUsageBar("Vie", 145),
    DailyUsageBar("Sáb", 130),
    DailyUsageBar("Dom", 150)
)

private val defaultMonthBars = listOf(
    DailyUsageBar("S1", 220),
    DailyUsageBar("S2", 195),
    DailyUsageBar("S3", 165),
    DailyUsageBar("S4", 142)
)

private val defaultTotalBars = listOf(
    DailyUsageBar("Jul", 240),
    DailyUsageBar("Ago", 215),
    DailyUsageBar("Sep", 180),
    DailyUsageBar("Oct", 142)
)

class PausaViewModel(
    private val repository: PausaRepository
) : ViewModel() {

    private val _currentDestination = MutableStateFlow(PausaDestination.INICIO)
    private val _navHistory = MutableStateFlow(listOf(PausaDestination.INICIO))
    private val _countdownSeconds = MutableStateFlow(900)
    private val _totalPauseDurationSeconds = MutableStateFlow(900)
    private val _isPauseTimerRunning = MutableStateFlow(false)
    private val _triggeredAppName = MutableStateFlow("Instagram")
    private val _selectedPeriod = MutableStateFlow(ProgresoPeriod.SEMANA)
    private val _showAllPlantsSheet = MutableStateFlow(false)
    private val _showAlternativeSheet = MutableStateFlow(false)
    private val _showHuaweiIapSheet = MutableStateFlow(false)
    private val _selectedAlternative = MutableStateFlow<String?>(null)
    private val _hasUsagePermission = MutableStateFlow(false)
    private val _hasOverlayPermission = MutableStateFlow(false)
    private val _isAccessibilityEnabled = MutableStateFlow(false)

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private data class LocalUiOverlay(
        val destination: PausaDestination,
        val history: List<PausaDestination>,
        val countdown: Int,
        val totalDuration: Int,
        val timerRunning: Boolean,
        val triggeredApp: String,
        val period: ProgresoPeriod,
        val showPlantsSheet: Boolean,
        val showAltSheet: Boolean,
        val showIapSheet: Boolean,
        val selectedAlt: String?,
        val hasPerm: Boolean,
        val hasOverlay: Boolean,
        val hasAccessibility: Boolean
    )

    private val localOverlayFlow = combine(
        combine(_currentDestination, _navHistory, _countdownSeconds, _totalPauseDurationSeconds) { dest, hist, count, total ->
            listOf(dest, hist, count, total)
        },
        combine(_isPauseTimerRunning, _triggeredAppName, _selectedPeriod) { running, app, period ->
            Triple(running, app, period)
        },
        combine(
            _showAllPlantsSheet,
            _showAlternativeSheet,
            _showHuaweiIapSheet,
            _selectedAlternative,
            combine(_hasUsagePermission, _hasOverlayPermission, _isAccessibilityEnabled) { u, o, a -> Triple(u, o, a) }
        ) { plantsSheet, altSheet, iapSheet, alt, perms ->
            listOf(plantsSheet, altSheet, iapSheet, alt, perms.first, perms.second, perms.third)
        }
    ) { t1, t2, list ->
        @Suppress("UNCHECKED_CAST")
        LocalUiOverlay(
            destination = t1[0] as PausaDestination,
            history = t1[1] as List<PausaDestination>,
            countdown = t1[2] as Int,
            totalDuration = t1[3] as Int,
            timerRunning = t2.first,
            triggeredApp = t2.second,
            period = t2.third,
            showPlantsSheet = list[0] as Boolean,
            showAltSheet = list[1] as Boolean,
            showIapSheet = list[2] as Boolean,
            selectedAlt = list[3] as String?,
            hasPerm = list[4] as Boolean,
            hasOverlay = list[5] as Boolean,
            hasAccessibility = list[6] as Boolean
        )
    }

    val uiState: StateFlow<PausaUiState> = combine(
        combine(repository.userPreferences, repository.appLimits, repository.plants) { prefs, limits, plants ->
            Triple(prefs, limits, plants)
        },
        combine(repository.pauseLogs, repository.walletTransactions) { logs, txs ->
            Pair(logs, txs)
        },
        localOverlayFlow
    ) { (prefs, limits, plants), (logs, txs), overlay ->
        com.example.blocker.PausaBlockerController.syncAppLimitsToBlocker(repository.appContext, limits)
        val currentPlant = plants.firstOrNull { it.isCurrent } ?: plants.firstOrNull()
        val baseBars = when (overlay.period) {
            ProgresoPeriod.SEMANA -> defaultWeekBars
            ProgresoPeriod.MES -> defaultMonthBars
            ProgresoPeriod.TOTAL -> defaultTotalBars
        }
        val bars = if (prefs.usedMinutesToday > 0 && baseBars.isNotEmpty()) {
            baseBars.mapIndexed { index, bar ->
                if (index == baseBars.lastIndex) bar.copy(minutes = prefs.usedMinutesToday) else bar
            }
        } else {
            baseBars
        }
        PausaUiState(
            currentDestination = overlay.destination,
            navigationHistory = overlay.history,
            stats = prefs,
            appLimits = limits,
            plants = plants,
            activePlant = currentPlant,
            pauseLogs = logs,
            walletTransactions = txs,
            iapPackages = officialHuaweiIapPackages,
            countdownSeconds = overlay.countdown,
            totalPauseDurationSeconds = overlay.totalDuration,
            isPauseTimerRunning = overlay.timerRunning,
            triggeredAppName = overlay.triggeredApp,
            selectedPeriod = overlay.period,
            usageBars = bars,
            showAllPlantsSheet = overlay.showPlantsSheet,
            showAlternativeActivitySheet = overlay.showAltSheet,
            showHuaweiIapSheet = overlay.showIapSheet,
            selectedAlternativeActivity = overlay.selectedAlt,
            hasUsageStatsPermission = overlay.hasPerm,
            hasOverlayPermission = overlay.hasOverlay,
            isAccessibilityBlockerEnabled = overlay.hasAccessibility
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PausaUiState()
    )

    fun refreshUsagePermissionState(context: Context) {
        val hasUsage = com.example.blocker.PausaBlockerController.hasUsageStatsPermission(context)
        val hasOverlay = com.example.blocker.PausaBlockerController.hasOverlayPermission(context)
        val hasAccessibility = com.example.blocker.PausaBlockerController.isAccessibilityServiceEnabled(context)

        _hasUsagePermission.value = hasUsage
        _hasOverlayPermission.value = hasOverlay
        _isAccessibilityEnabled.value = hasAccessibility

        com.example.blocker.PausaBlockerController.syncAppLimitsToBlocker(context, uiState.value.appLimits)
        if (hasUsage) {
            viewModelScope.launch {
                repository.syncRealUsageStatsToDatabase()
            }
        }
        if (hasUsage || hasOverlay || hasAccessibility) {
            com.example.blocker.PausaBlockerController.startGuardianMonitorService(context)
        }
    }

    fun onSocialAppIntercepted(appName: String) {
        _triggeredAppName.value = appName
        if (_currentDestination.value != PausaDestination.PAUSA) {
            startPauseCountdown(_totalPauseDurationSeconds.value)
            _currentDestination.value = PausaDestination.PAUSA
            val currentHist = _navHistory.value.toMutableList()
            if (currentHist.lastOrNull() != PausaDestination.PAUSA) {
                currentHist.add(PausaDestination.PAUSA)
                _navHistory.value = currentHist
            }
        }
    }

    fun relockAllSocialAppsNow(context: Context) {
        com.example.blocker.PausaBlockerController.revokeAllUnlockPasses(context)
        com.example.blocker.PausaBlockerController.syncAppLimitsToBlocker(context, uiState.value.appLimits)
        com.example.blocker.PausaBlockerController.startGuardianMonitorService(context)
    }

    fun navigateTo(destination: PausaDestination, triggeredApp: String? = null) {
        if (triggeredApp != null) {
            _triggeredAppName.value = triggeredApp
        }
        if (destination == PausaDestination.PAUSA) {
            startPauseCountdown(_totalPauseDurationSeconds.value)
        } else {
            stopPauseCountdown()
        }
        _currentDestination.value = destination
        val currentHist = _navHistory.value.toMutableList()
        if (currentHist.lastOrNull() != destination) {
            currentHist.add(destination)
            _navHistory.value = currentHist
        }
    }

    fun navigateBack(): Boolean {
        val currentHist = _navHistory.value.toMutableList()
        if (currentHist.size > 1) {
            currentHist.removeAt(currentHist.lastIndex)
            val prev = currentHist.last()
            _navHistory.value = currentHist
            _currentDestination.value = prev
            if (prev == PausaDestination.PAUSA) {
                startPauseCountdown(_totalPauseDurationSeconds.value)
            } else {
                stopPauseCountdown()
            }
            return true
        }
        if (_currentDestination.value != PausaDestination.INICIO) {
            _currentDestination.value = PausaDestination.INICIO
            _navHistory.value = listOf(PausaDestination.INICIO)
            stopPauseCountdown()
            return true
        }
        return false
    }

    fun startPauseCountdown() {
        startPauseCountdown(_totalPauseDurationSeconds.value)
    }

    fun startPauseCountdown(durationSeconds: Int) {
        timerJob?.cancel()
        val validSeconds = durationSeconds.coerceIn(60, 14400)
        _totalPauseDurationSeconds.value = validSeconds
        _countdownSeconds.value = validSeconds
        _isPauseTimerRunning.value = true
        com.example.blocker.PausaBlockerController.setPauseTimerActiveState(
            repository.appContext,
            isRunning = true,
            remainingSeconds = validSeconds
        )
        timerJob = viewModelScope.launch {
            while (_countdownSeconds.value > 0 && _isPauseTimerRunning.value) {
                delay(1000L)
                _countdownSeconds.value = (_countdownSeconds.value - 1).coerceAtLeast(0)
            }
            _isPauseTimerRunning.value = false
            com.example.blocker.PausaBlockerController.setPauseTimerActiveState(
                repository.appContext,
                isRunning = false,
                remainingSeconds = 0
            )
            if (_countdownSeconds.value == 0) {
                val (minutesReward, plantBonus) = computePauseRewards()
                repository.recordPauseRespected(
                    appName = _triggeredAppName.value,
                    activePlant = uiState.value.activePlant,
                    minutesSaved = minutesReward,
                    plantBonusPercent = plantBonus
                )
                com.example.blocker.PausaBlockerController.grantPaidUnlockPass(
                    repository.appContext,
                    _triggeredAppName.value,
                    durationMinutes = 15
                )
            }
        }
    }

    fun selectPauseDuration(durationSeconds: Int) {
        startPauseCountdown(durationSeconds)
    }

    fun launchCustomFocusPause(durationSeconds: Int) {
        _triggeredAppName.value = "Sesión de Enfoque"
        startPauseCountdown(durationSeconds)
        _currentDestination.value = PausaDestination.PAUSA
        val currentHist = _navHistory.value.toMutableList()
        if (currentHist.lastOrNull() != PausaDestination.PAUSA) {
            currentHist.add(PausaDestination.PAUSA)
            _navHistory.value = currentHist
        }
    }

    fun onFocusedWorkIntervalCompleted(completedMinutes: Int) {
        val activePlant = uiState.value.activePlant
        val bonusPercent = (completedMinutes / 2).coerceIn(5, 35)
        viewModelScope.launch {
            repository.recordPauseRespected(
                appName = "Temporizador de Enfoque",
                activePlant = activePlant,
                minutesSaved = completedMinutes.coerceAtLeast(1),
                plantBonusPercent = bonusPercent
            )
        }
    }

    fun addMorePauseTime(extraSeconds: Int = 900) {
        val newRemaining = (_countdownSeconds.value + extraSeconds).coerceAtMost(14400)
        val newTotal = maxOf(_totalPauseDurationSeconds.value + extraSeconds, newRemaining).coerceAtMost(14400)
        _totalPauseDurationSeconds.value = newTotal
        _countdownSeconds.value = newRemaining
        com.example.blocker.PausaBlockerController.setPauseTimerActiveState(
            repository.appContext,
            isRunning = true,
            remainingSeconds = newRemaining
        )
        if (!_isPauseTimerRunning.value) {
            _isPauseTimerRunning.value = true
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (_countdownSeconds.value > 0 && _isPauseTimerRunning.value) {
                    delay(1000L)
                    _countdownSeconds.value = (_countdownSeconds.value - 1).coerceAtLeast(0)
                }
                _isPauseTimerRunning.value = false
                com.example.blocker.PausaBlockerController.setPauseTimerActiveState(
                    repository.appContext,
                    isRunning = false,
                    remainingSeconds = 0
                )
            }
        }
    }

    private fun stopPauseCountdown() {
        timerJob?.cancel()
        _isPauseTimerRunning.value = false
        com.example.blocker.PausaBlockerController.setPauseTimerActiveState(
            repository.appContext,
            isRunning = false,
            remainingSeconds = 0
        )
    }

    private fun computePauseRewards(): Pair<Int, Int> {
        val mins = (_totalPauseDurationSeconds.value / 60).coerceAtLeast(1)
        return when {
            mins >= 120 -> 120 to 60
            mins >= 60 -> 60 to 40
            mins >= 30 -> 45 to 28
            mins >= 15 -> 30 to 20
            mins >= 5 -> 20 to 14
            else -> 15 to 10
        }
    }

    fun onContinueAnywayBreakPause() {
        val currentStats = uiState.value.stats
        val hasEnoughPrepaidBalance =
            currentStats.digitalWalletBalance >= currentStats.penaltyPerBreak &&
                currentStats.accumulatedDebtDollars <= 0.0

        if (!hasEnoughPrepaidBalance) {
            // Mandatory payment gate: user cannot break the pause without paying via PayPal first
            _showHuaweiIapSheet.value = true
            return
        }

        stopPauseCountdown()
        val appName = _triggeredAppName.value
        val activePlant = uiState.value.activePlant
        viewModelScope.launch {
            repository.recordPauseBroken(appName, activePlant)
            navigateTo(PausaDestination.PAUSA_ROTA)
        }
    }

    fun onCloseAppRespectPause() {
        stopPauseCountdown()
        val appName = _triggeredAppName.value
        val activePlant = uiState.value.activePlant
        val (minutesReward, plantBonus) = computePauseRewards()
        viewModelScope.launch {
            repository.recordPauseRespected(
                appName = appName,
                activePlant = activePlant,
                minutesSaved = minutesReward,
                plantBonusPercent = plantBonus
            )
            navigateTo(PausaDestination.TU_PLANTA)
        }
    }

    fun onOpenAlternativeActivities() {
        _showAlternativeSheet.value = true
    }

    fun onDismissAlternativeActivities() {
        _showAlternativeSheet.value = false
    }

    fun onChooseAlternativeActivity(activityTitle: String) {
        _selectedAlternative.value = activityTitle
        _showAlternativeSheet.value = false
        stopPauseCountdown()
        val appName = _triggeredAppName.value
        val activePlant = uiState.value.activePlant
        val (minutesReward, plantBonus) = computePauseRewards()
        viewModelScope.launch {
            repository.recordPauseRespected(
                appName = appName,
                activePlant = activePlant,
                minutesSaved = minutesReward,
                plantBonusPercent = plantBonus
            )
            navigateTo(PausaDestination.TU_PLANTA)
        }
    }

    fun clearSelectedAlternativeBanner() {
        _selectedAlternative.value = null
    }

    fun toggleAppLimit(limit: AppLimitEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAppLimit(limit, enabled)
        }
    }

    fun updateAppLimitMinutes(limit: AppLimitEntity, minutes: Int) {
        viewModelScope.launch {
            repository.updateAppLimitMinutes(limit, minutes)
        }
    }

    fun addCustomApp(name: String, minutes: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCustomAppLimit(name.trim(), minutes)
        }
    }

    fun updateFocusSchedule(start: String, end: String, scope: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateFocusSchedule(start, end, scope, enabled)
        }
    }

    fun setProgresoPeriod(period: ProgresoPeriod) {
        _selectedPeriod.value = period
    }

    fun setShowAllPlantsSheet(show: Boolean) {
        _showAllPlantsSheet.value = show
    }

    fun setShowHuaweiIapSheet(show: Boolean) {
        _showHuaweiIapSheet.value = show
    }

    fun selectPlant(plantId: Int) {
        viewModelScope.launch {
            repository.selectActivePlant(plantId)
        }
    }

    fun nurturePlant(bonusPercent: Int = 25, bonusRecoveredMinutes: Int = 10) {
        viewModelScope.launch {
            repository.nurtureActivePlant(bonusPercent = bonusPercent, bonusRecoveredMinutes = bonusRecoveredMinutes)
        }
    }

    fun savePlantEvolutionStage(targetLevel: Int, targetProgressPercent: Int) {
        viewModelScope.launch {
            repository.saveActivePlantEvolutionStage(
                targetLevel = targetLevel,
                targetProgressPercent = targetProgressPercent
            )
        }
    }

    fun updateFocusIntervals(focusMinutes: Int, pauseMinutes: Int) {
        viewModelScope.launch {
            repository.updateFocusIntervals(focusMinutes, pauseMinutes)
        }
    }

    fun updateDailyGoal(minutes: Int) {
        viewModelScope.launch {
            repository.updateDailyGoal(minutes)
        }
    }

    fun advanceStreakDay(daysToAdd: Int = 1) {
        viewModelScope.launch {
            repository.advanceStreakDay(daysToAdd)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }

    fun topUpWallet(amount: Double) {
        viewModelScope.launch {
            repository.topUpWallet(amount)
        }
    }

    fun purchaseHuaweiIapPackage(
        pkg: HmsIapPackage,
        paymentMethodLabel: String,
        isFreeCommitment: Boolean
    ) {
        val isBreakingActivePause = _currentDestination.value == PausaDestination.PAUSA
        val appName = _triggeredAppName.value
        val activePlant = uiState.value.activePlant
        val currentPayPalEmail = uiState.value.stats.paypalEmail.ifBlank {
            uiState.value.stats.userEmail
        }
        viewModelScope.launch {
            if (isBreakingActivePause) {
                stopPauseCountdown()
                repository.payAndBreakPauseWithPayPal(
                    appName = appName,
                    activePlant = activePlant,
                    packageAmount = pkg.creditsAmount,
                    skuId = pkg.productId,
                    methodLabel = paymentMethodLabel,
                    paypalAccount = currentPayPalEmail
                )
                _showHuaweiIapSheet.value = false
                navigateTo(PausaDestination.PAUSA_ROTA)
            } else {
                repository.processCompliantTopUp(
                    amount = pkg.creditsAmount,
                    skuId = pkg.productId,
                    methodLabel = paymentMethodLabel,
                    isFreeCommitment = isFreeCommitment,
                    paypalAccount = currentPayPalEmail
                )
                _showHuaweiIapSheet.value = false
            }
        }
    }

    fun initiatePendingPayPalWebRecharge(
        pkg: HmsIapPackage,
        paypalAccount: String = ""
    ) {
        val resolvedAccount = paypalAccount.trim().ifBlank {
            uiState.value.stats.paypalEmail.ifBlank { uiState.value.stats.userEmail }
        }
        viewModelScope.launch {
            repository.createPendingPayPalRecharge(
                amount = pkg.creditsAmount,
                skuId = pkg.productId,
                paypalAccount = resolvedAccount,
                methodLabel = "PayPal Checkout Web"
            )
        }
    }

    fun confirmPendingPayPalTransaction(orderReference: String) {
        viewModelScope.launch {
            repository.confirmPendingPayPalTransaction(orderReference)
        }
    }

    fun cancelPendingPayPalTransaction(orderReference: String) {
        viewModelScope.launch {
            repository.cancelPendingPayPalTransaction(orderReference)
        }
    }

    fun payBrokenPausePenaltyViaPayPal(paypalAccount: String = "") {
        val appName = _triggeredAppName.value
        viewModelScope.launch {
            repository.payBrokenPauseWithPayPal(paypalAccount = paypalAccount)
            com.example.blocker.PausaBlockerController.grantPaidUnlockPass(
                repository.appContext,
                appName,
                durationMinutes = 15
            )
        }
    }

    fun updatePayPalEmail(email: String) {
        viewModelScope.launch {
            repository.updatePayPalEmail(email)
        }
    }

    fun setWalletMode(mode: WalletMode, paymentLabel: String) {
        viewModelScope.launch {
            repository.setWalletMode(mode, paymentLabel)
        }
    }

    fun signInOrRegisterEmailPin(
        email: String,
        pin6Digits: String,
        name: String = "",
        isNewRegistration: Boolean = true
    ) {
        viewModelScope.launch {
            repository.signInOrRegisterWithEmailPin(
                email = email,
                pin6Digits = pin6Digits,
                name = name,
                isNewRegistration = isNewRegistration
            )
            _currentDestination.value = PausaDestination.INICIO
        }
    }

    fun signOut() {
        stopPauseCountdown()
        _showAllPlantsSheet.value = false
        _showAlternativeSheet.value = false
        _showHuaweiIapSheet.value = false
        viewModelScope.launch {
            repository.signOutAccount()
            _currentDestination.value = PausaDestination.INICIO
            _navHistory.value = listOf(PausaDestination.INICIO)
        }
    }

    class Factory(private val repository: PausaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PausaViewModel(repository) as T
        }
    }
}
