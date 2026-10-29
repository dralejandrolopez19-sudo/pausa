package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blocker.PausaBlockerController
import com.example.data.PausaDatabase
import com.example.data.PausaRepository
import com.example.ui.PausaDestination
import com.example.ui.PausaViewModel
import com.example.ui.components.PausaBottomNavigationBar
import com.example.ui.screens.AjustesScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.InicioScreen
import com.example.ui.screens.LimitesScreen
import com.example.ui.screens.ModoPausaScreen
import com.example.ui.screens.PausaRotaScreen
import com.example.ui.screens.ProgresoScreen
import com.example.ui.screens.TuPlantaScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDeepForest
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val interceptedAppFlow = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        extractInterceptedAppFromIntent(intent)

        val database = PausaDatabase.getDatabase(applicationContext)
        val repository = PausaRepository(applicationContext, database.pausaDao())
        val factory = PausaViewModel.Factory(repository)

        setContent {
            val viewModel: PausaViewModel = viewModel(factory = factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val interceptedApp by interceptedAppFlow.collectAsStateWithLifecycle()

            LaunchedEffect(interceptedApp) {
                val appName = interceptedApp
                if (!appName.isNullOrBlank()) {
                    viewModel.onSocialAppIntercepted(appName)
                    interceptedAppFlow.value = null
                }
            }

            MyApplicationTheme(darkTheme = uiState.stats.isDarkMode) {
                PausaApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractInterceptedAppFromIntent(intent)
    }

    private fun extractInterceptedAppFromIntent(intent: Intent?) {
        val blockedApp = intent?.getStringExtra(PausaBlockerController.EXTRA_INTERCEPTED_APP_NAME)
        if (!blockedApp.isNullOrBlank()) {
            interceptedAppFlow.value = blockedApp
            intent.removeExtra(PausaBlockerController.EXTRA_INTERCEPTED_APP_NAME)
        }
    }
}

@Composable
fun PausaApp(
    viewModel: PausaViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.refreshUsagePermissionState(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshUsagePermissionState(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!uiState.stats.isLoggedIn) {
        Scaffold(
            containerColor = PausaCreamBackground,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                AuthScreen(
                    onEmailPinSubmit = viewModel::signInOrRegisterEmailPin,
                    modifier = Modifier.widthIn(max = 600.dp)
                )
            }
        }
        return
    }

    // BackHandler: if on PAUSA or PAUSA_ROTA with unpaid debt, prevent bypassing the pause without paying
    BackHandler(enabled = uiState.currentDestination != PausaDestination.INICIO) {
        when (uiState.currentDestination) {
            PausaDestination.PAUSA -> {
                if (uiState.isPauseTimerRunning && uiState.countdownSeconds > 0) {
                    viewModel.onContinueAnywayBreakPause()
                } else {
                    viewModel.navigateBack()
                }
            }
            PausaDestination.PAUSA_ROTA -> {
                if (uiState.stats.accumulatedDebtDollars <= 0.0) {
                    viewModel.navigateBack()
                }
            }
            else -> viewModel.navigateBack()
        }
    }

    val isDarkFullScreen = uiState.currentDestination == PausaDestination.PAUSA ||
        uiState.currentDestination == PausaDestination.PAUSA_ROTA

    Scaffold(
        containerColor = if (isDarkFullScreen) PausaDeepForest else PausaCreamBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!isDarkFullScreen) {
                PausaBottomNavigationBar(
                    currentDestination = uiState.currentDestination,
                    onNavigate = { dest -> viewModel.navigateTo(dest) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                Crossfade(
                    targetState = uiState.currentDestination,
                    animationSpec = tween(durationMillis = 220),
                    label = "pausa_screen_transition"
                ) { destination ->
                    when (destination) {
                        PausaDestination.INICIO -> {
                            InicioScreen(
                                uiState = uiState,
                                onNeedPauseClick = {
                                    viewModel.navigateTo(PausaDestination.PAUSA, triggeredApp = "Pausa Voluntaria")
                                },
                                onPlantCardClick = {
                                    viewModel.navigateTo(PausaDestination.TU_PLANTA)
                                },
                                onMetricClick = { target ->
                                    viewModel.navigateTo(target)
                                }
                            )
                        }

                        PausaDestination.LIMITES -> {
                            LimitesScreen(
                                uiState = uiState,
                                onToggleAppLimit = viewModel::toggleAppLimit,
                                onUpdateAppLimitMinutes = viewModel::updateAppLimitMinutes,
                                onAddCustomApp = viewModel::addCustomApp,
                                onUpdateFocusSchedule = viewModel::updateFocusSchedule,
                                onTriggerPauseForApp = { appName ->
                                    viewModel.navigateTo(PausaDestination.PAUSA, triggeredApp = appName)
                                },
                                onRelockAllNow = {
                                    viewModel.relockAllSocialAppsNow(context)
                                }
                            )
                        }

                        PausaDestination.PAUSA -> {
                            ModoPausaScreen(
                                uiState = uiState,
                                onContinueWhatIWasDoing = viewModel::onContinueAnywayBreakPause,
                                onCloseApplication = viewModel::onCloseAppRespectPause,
                                onOpenAlternativeActivities = viewModel::onOpenAlternativeActivities,
                                onDismissAlternativeActivities = viewModel::onDismissAlternativeActivities,
                                onSelectAlternativeActivity = viewModel::onChooseAlternativeActivity,
                                onRestartTimer = viewModel::startPauseCountdown,
                                onSelectPauseDuration = viewModel::selectPauseDuration,
                                onExtendPauseTime = viewModel::addMorePauseTime,
                                onUpdateFocusIntervals = viewModel::updateFocusIntervals
                            )
                            if (uiState.showHuaweiIapSheet) {
                                com.example.ui.components.HuaweiIapWalletDialog(
                                    uiState = uiState,
                                    onDismiss = { viewModel.setShowHuaweiIapSheet(false) },
                                    onConfirmPurchase = viewModel::purchaseHuaweiIapPackage,
                                    onUpdatePayPalEmail = viewModel::updatePayPalEmail,
                                    onInitiatePendingWebOrder = viewModel::initiatePendingPayPalWebRecharge,
                                    onConfirmPendingTransaction = viewModel::confirmPendingPayPalTransaction,
                                    onCancelPendingTransaction = viewModel::cancelPendingPayPalTransaction
                                )
                            }
                        }

                        PausaDestination.PROGRESO -> {
                            ProgresoScreen(
                                uiState = uiState,
                                onSelectPeriod = viewModel::setProgresoPeriod,
                                onOpenTuPlantaClick = {
                                    viewModel.navigateTo(PausaDestination.TU_PLANTA)
                                },
                                onUpdateDailyGoal = viewModel::updateDailyGoal,
                                onAdvanceStreakDay = viewModel::advanceStreakDay
                            )
                        }

                        PausaDestination.TU_PLANTA -> {
                            TuPlantaScreen(
                                uiState = uiState,
                                onBackClick = { viewModel.navigateBack() },
                                onSelectPlant = viewModel::selectPlant,
                                onDismissAlternativeBanner = viewModel::clearSelectedAlternativeBanner,
                                onTopUpWallet = { _ -> viewModel.setShowHuaweiIapSheet(true) },
                                onNurturePlant = { viewModel.nurturePlant() },
                                onSaveEvolutionStage = viewModel::savePlantEvolutionStage
                            )
                            if (uiState.showHuaweiIapSheet) {
                                com.example.ui.components.HuaweiIapWalletDialog(
                                    uiState = uiState,
                                    onDismiss = { viewModel.setShowHuaweiIapSheet(false) },
                                    onConfirmPurchase = viewModel::purchaseHuaweiIapPackage,
                                    onUpdatePayPalEmail = viewModel::updatePayPalEmail,
                                    onInitiatePendingWebOrder = viewModel::initiatePendingPayPalWebRecharge,
                                    onConfirmPendingTransaction = viewModel::confirmPendingPayPalTransaction,
                                    onCancelPendingTransaction = viewModel::cancelPendingPayPalTransaction
                                )
                            }
                        }

                        PausaDestination.PAUSA_ROTA -> {
                            PausaRotaScreen(
                                uiState = uiState,
                                onEntendidoClick = {
                                    viewModel.navigateTo(PausaDestination.TU_PLANTA)
                                },
                                onPayWithPayPal = {
                                    viewModel.payBrokenPausePenaltyViaPayPal()
                                },
                                onOpenPayPalRecharge = {
                                    viewModel.setShowHuaweiIapSheet(true)
                                },
                                onDismissPayPalDialog = {
                                    viewModel.setShowHuaweiIapSheet(false)
                                },
                                onPurchasePayPalPackage = viewModel::purchaseHuaweiIapPackage,
                                onUpdatePayPalEmail = viewModel::updatePayPalEmail
                            )
                        }

                        PausaDestination.AJUSTES -> {
                            AjustesScreen(
                                uiState = uiState,
                                onOpenHuaweiWalletDialog = { viewModel.setShowHuaweiIapSheet(true) },
                                onDismissHuaweiWalletDialog = { viewModel.setShowHuaweiIapSheet(false) },
                                onPurchaseHuaweiPackage = viewModel::purchaseHuaweiIapPackage,
                                onToggleDarkMode = viewModel::toggleDarkMode,
                                onSignOutClick = viewModel::signOut,
                                onPayBrokenPauseWithPayPal = { viewModel.payBrokenPausePenaltyViaPayPal() },
                                onUpdatePayPalEmail = viewModel::updatePayPalEmail,
                                onInitiatePendingWebOrder = viewModel::initiatePendingPayPalWebRecharge,
                                onConfirmPendingTransaction = viewModel::confirmPendingPayPalTransaction,
                                onCancelPendingTransaction = viewModel::cancelPendingPayPalTransaction
                            )
                        }
                    }
                }
            }
        }
    }
}

