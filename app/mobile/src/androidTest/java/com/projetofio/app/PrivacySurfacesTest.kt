package com.projetofio.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.view.WindowManager
import com.projetofio.app.ui.LockedScreen
import com.projetofio.app.ui.SafeOpenFailure
import com.projetofio.app.ui.theme.FioTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrivacySurfacesTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun unavailableAuthenticationOffersExplicitPrivateFallback() {
        composeRule.setContent {
            FioTheme(darkTheme = false) {
                LockedScreen(
                    authenticationAvailable = false,
                    onUnlock = {},
                    onDisableUnavailableLock = {},
                )
            }
        }
        composeRule.onNodeWithText("A autenticação do aparelho não está disponível.").assertIsDisplayed()
        composeRule.onNodeWithText("Rever bloqueio").performClick()
        composeRule.onNodeWithText("Manter bloqueado").assertIsDisplayed()
        composeRule.onNodeWithText("Desativar e continuar").assertIsDisplayed()
    }

    @Test
    fun safeOpenFailureNeverPretendsArchiveIsEmpty() {
        composeRule.setContent {
            FioTheme(darkTheme = false) { SafeOpenFailure() }
        }
        composeRule
            .onNodeWithText("Os dados locais não puderam ser abertos com segurança. Nada foi apagado. Feche o Fio e tente novamente.")
            .assertIsDisplayed()
    }
}

@RunWith(AndroidJUnit4::class)
class ActivityPrivacyFlagTest {
    @get:Rule
    val activityRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun contentWindowAlwaysHasSecureFlag() {
        val flags = activityRule.activity.window.attributes.flags
        org.junit.Assert.assertTrue(flags and WindowManager.LayoutParams.FLAG_SECURE != 0)
    }
}
