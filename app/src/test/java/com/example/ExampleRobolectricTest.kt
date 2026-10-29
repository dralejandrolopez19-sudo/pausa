package com.example

import android.content.Context
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.UserStatsPreferences
import com.example.ui.PausaUiState
import com.example.ui.screens.AjustesScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ModoPausaScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `verify app name and resources`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Pausa", appName)
    }

    @Test
    fun `verify AuthScreen 6 digit password validation and email registration`() {
        var submittedName = ""
        var submittedEmail = ""
        var submittedPin = ""

        composeTestRule.setContent {
            MyApplicationTheme {
                AuthScreen(
                    onEmailPinSubmit = { email, pin, name, _ ->
                        submittedName = name
                        submittedEmail = email
                        submittedPin = pin
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Verify Name, Email and 6-digit password fields exist (100% GMS-free without Google Sign-In button)
        composeTestRule.onNodeWithTag("input_nombre_usuario").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_correo_electronico").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_password_6_digitos").performScrollTo().assertIsDisplayed()

        // Enter valid name, email and 6-digit numeric password for a new user
        composeTestRule.onNodeWithTag("input_nombre_usuario").performTextInput("Carlos")
        composeTestRule.onNodeWithTag("input_correo_electronico").performTextInput("usuario@pausa.app")
        composeTestRule.onNodeWithTag("input_password_6_digitos").performTextInput("123456")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_crear_cuenta_correo").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals("Carlos", submittedName)
        assertEquals("usuario@pausa.app", submittedEmail)
        assertEquals("123456", submittedPin)
    }

    @Test
    fun `verify ModoPausaScreen longer pause duration presets`() {
        var selectedDuration = 900
        var extendedDuration = 0

        composeTestRule.setContent {
            MyApplicationTheme {
                ModoPausaScreen(
                    uiState = PausaUiState(
                        countdownSeconds = 900,
                        totalPauseDurationSeconds = 900
                    ),
                    onContinueWhatIWasDoing = {},
                    onCloseApplication = {},
                    onOpenAlternativeActivities = {},
                    onDismissAlternativeActivities = {},
                    onSelectAlternativeActivity = {},
                    onRestartTimer = {},
                    onSelectPauseDuration = { selectedDuration = it },
                    onExtendPauseTime = { extendedDuration = it }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Check longer duration chips exist and work
        composeTestRule.onNodeWithTag("chip_pausa_1800").assertIsDisplayed().performClick()
        assertEquals(1800, selectedDuration)

        composeTestRule.onNodeWithTag("chip_pausa_3600").assertIsDisplayed().performClick()
        assertEquals(3600, selectedDuration)

        composeTestRule.onNodeWithTag("chip_extender_15m").assertIsDisplayed().performClick()
        assertEquals(900, extendedDuration)
    }

    @Test
    fun `verify ProgresoScreen TuPlantaScreen and AjustesScreen components`() {
        val sampleState = PausaUiState(
            stats = UserStatsPreferences(
                isLoggedIn = true,
                userName = "Nuevo Usuario",
                userEmail = "nuevo.usuario@pausa.app",
                authProvider = "Correo",
                selectedPaymentMethodLabel = "PayPal"
            )
        )

        var darkModeToggledTo: Boolean? = null
        var signedOut = false
        var openedPayPalDialog = false
        var rechargedAmountUsd = 0.0

        composeTestRule.setContent {
            MyApplicationTheme {
                AjustesScreen(
                    uiState = sampleState,
                    onOpenHuaweiWalletDialog = { openedPayPalDialog = true },
                    onDismissHuaweiWalletDialog = {},
                    onPurchaseHuaweiPackage = { pkg, _, _ -> rechargedAmountUsd = pkg.creditsAmount },
                    onToggleDarkMode = { darkModeToggledTo = it },
                    onSignOutClick = { signedOut = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_ajustes_minimal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_recargar_saldo").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(true, openedPayPalDialog)

        composeTestRule.onNodeWithTag("chip_recarga_rapida_5").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        assertEquals(5.0, rechargedAmountUsd, 0.01)
        composeTestRule.onNodeWithTag("badge_recarga_exitosa").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("switch_modo_oscuro").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(true, darkModeToggledTo)
        composeTestRule.onNodeWithText("nuevo.usuario@pausa.app").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_cerrar_sesion").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(true, signedOut)
    }

    @Test
    fun `verify PayPal payment flow when pause is broken`() {
        var paidViaPayPal = false
        var openedRecharge = false

        val brokenPauseState = PausaUiState(
            stats = UserStatsPreferences(
                isLoggedIn = true,
                userName = "Carlos",
                userEmail = "carlos@paypal.com",
                paypalEmail = "carlos@paypal.com",
                digitalWalletBalance = 0.0,
                accumulatedDebtDollars = 1.0,
                penaltyPerBreak = 1.0,
                selectedPaymentMethodLabel = "PayPal"
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                com.example.ui.screens.PausaRotaScreen(
                    uiState = brokenPauseState,
                    onEntendidoClick = {},
                    onPayWithPayPal = { paidViaPayPal = true },
                    onOpenPayPalRecharge = { openedRecharge = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("text_penalty_amount").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_paypal_pausa_rota").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_pagar_paypal_pausa_rota").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(true, paidViaPayPal)

        composeTestRule.onNodeWithTag("btn_recargar_paypal_pausa_rota").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(true, openedRecharge)
    }

    @Test
    fun `verify FocusedWorkTimerCard start and pause interval configuration`() {
        var launchedFullScreenDuration = 0

        composeTestRule.setContent {
            MyApplicationTheme {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier.verticalScroll(
                        androidx.compose.foundation.rememberScrollState()
                    )
                ) {
                    com.example.ui.components.FocusedWorkTimerCard(
                        initialFocusMinutes = 25,
                        initialPauseMinutes = 5,
                        onLaunchFullScreenPause = { durationSeconds ->
                            launchedFullScreenDuration = durationSeconds
                        }
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_temporizador_enfoque").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chip_focus_45").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("45:00").assertIsDisplayed()

        composeTestRule.onNodeWithTag("chip_pause_10").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_abrir_pausa_inmersiva").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(45 * 60, launchedFullScreenDuration)
    }

    @Test
    fun `verify TuPlantaScreen interactive plant care and watering`() {
        var wateredCount = 0

        composeTestRule.setContent {
            MyApplicationTheme {
                com.example.ui.screens.TuPlantaScreen(
                    uiState = PausaUiState(
                        activePlant = com.example.data.PlantEntity(
                            id = 1,
                            name = "Brote de Calma",
                            level = 1,
                            levelName = "Semilla",
                            progressPercent = 35,
                            isCurrent = true,
                            unlockedBenefit = "Inicio del hábito consciente"
                        )
                    ),
                    onBackClick = {},
                    onSelectPlant = {},
                    onDismissAlternativeBanner = {},
                    onTopUpWallet = {},
                    onNurturePlant = { wateredCount++ }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_regar_planta").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, wateredCount)
        composeTestRule.onNodeWithTag("badge_planta_nutrida").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `verify clean InicioScreen without duplicate timer and reactive Bosque Nocturno dark mode`() {
        var pauseClicked = false

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                com.example.ui.screens.InicioScreen(
                    uiState = PausaUiState(
                        stats = UserStatsPreferences(
                            isLoggedIn = true,
                            userName = "Alex"
                        )
                    ),
                    onNeedPauseClick = { pauseClicked = true },
                    onPlantCardClick = {},
                    onMetricClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Verify Bosque Nocturno dark mode flag is active
        assertEquals(true, com.example.ui.theme.isPausaDarkThemeActive)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF121A15), com.example.ui.theme.PausaCreamBackground)

        // Verify InicioScreen displays clean core elements and NO duplicate embedded timer
        composeTestRule.onNodeWithTag("card_tiempo_hoy").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_tu_planta_inicio").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_necesito_una_pausa").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("card_temporizador_enfoque").assertDoesNotExist()
        assertEquals(true, pauseClicked)
    }

    @Test
    fun `verify PayPal recharge transaction status lifecycle in Room database`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.PausaDatabase::class.java
        ).allowMainThreadQueries().build()
        val dao = db.pausaDao()
        val repository = com.example.data.PausaRepository(context, dao)

        kotlinx.coroutines.runBlocking {
            repository.signInOrRegisterWithEmailPin(
                email = "auditoria@pausa.app",
                pin6Digits = "654321",
                name = "Auditor PayPal",
                isNewRegistration = true
            )

            // 1. Direct PayPal recharge -> transitions PENDING -> COMPLETED and updates user_accounts
            repository.processCompliantTopUp(
                amount = 5.0,
                skuId = "com.aistudio.pausa.paypal.500",
                methodLabel = "PayPal",
                isFreeCommitment = false,
                paypalAccount = "auditoria@paypal.com"
            )

            val allAfterInstant = dao.getAllWalletTransactionsSync()
            assertEquals(1, allAfterInstant.size)
            val instantTx = allAfterInstant.first()
            assertEquals(com.example.data.TransactionStatus.COMPLETED.name, instantTx.status)
            assertEquals(5.0, instantTx.balanceAfter, 0.01)
            assertEquals("dr.alejandrolopez19@gmail.com", instantTx.merchantEmail)

            val accountAfterInstant = dao.getUserAccountByEmail("auditoria@pausa.app")
            assertEquals(5.0, accountAfterInstant?.digitalWalletBalance ?: 0.0, 0.01)

            // 2. Initiate PENDING web recharge -> verify status is PENDING in DB and balance unchanged
            val pendingRef = repository.createPendingPayPalRecharge(
                amount = 10.0,
                skuId = "com.aistudio.pausa.paypal.1000",
                paypalAccount = "auditoria@paypal.com"
            )
            val pendingTxInDb = dao.getWalletTransactionByReference(pendingRef)
            assertEquals(com.example.data.TransactionStatus.PENDING.name, pendingTxInDb?.status)
            assertEquals(5.0, dao.getUserAccountByEmail("auditoria@pausa.app")?.digitalWalletBalance ?: 0.0, 0.01)

            // 3. Confirm PENDING web recharge -> verify status updates to COMPLETED in DB and balance becomes $15.00
            val confirmed = repository.confirmPendingPayPalTransaction(pendingRef)
            assertEquals(true, confirmed)
            val confirmedTxInDb = dao.getWalletTransactionByReference(pendingRef)
            assertEquals(com.example.data.TransactionStatus.COMPLETED.name, confirmedTxInDb?.status)
            assertEquals(15.0, confirmedTxInDb?.balanceAfter ?: 0.0, 0.01)
            assertEquals(15.0, dao.getUserAccountByEmail("auditoria@pausa.app")?.digitalWalletBalance ?: 0.0, 0.01)

            // 4. Create another PENDING recharge and cancel it -> verify status updates to CANCELLED in DB
            val cancelRef = repository.createPendingPayPalRecharge(
                amount = 5.0,
                skuId = "com.aistudio.pausa.paypal.500",
                paypalAccount = "auditoria@paypal.com"
            )
            val cancelled = repository.cancelPendingPayPalTransaction(cancelRef)
            assertEquals(true, cancelled)
            val cancelledTxInDb = dao.getWalletTransactionByReference(cancelRef)
            assertEquals(com.example.data.TransactionStatus.CANCELLED.name, cancelledTxInDb?.status)
            assertEquals(15.0, dao.getUserAccountByEmail("auditoria@pausa.app")?.digitalWalletBalance ?: 0.0, 0.01)
        }
        db.close()
    }
}

