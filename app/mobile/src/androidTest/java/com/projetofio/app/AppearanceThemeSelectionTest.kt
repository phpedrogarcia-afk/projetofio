package com.projetofio.app

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import com.projetofio.app.ui.theme.AppearanceThemeStore
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppearanceThemeSelectionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @After
    fun clearPreference() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences(AppearanceThemeStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun settingsSelectsTheOnlyTwoApprovedThemes() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithText("Ajustes").assertIsDisplayed()
            }.isSuccess
        }
        composeRule.onNodeWithText("Ajustes").performClick()
        composeRule
            .onNodeWithContentDescription("Tema Sereno. Visual claro, orgânico e discreto.", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription("Tema Céu Noturno. Atmosfera cósmica, vidro e símbolos do tempo.", substring = true)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription("Tema Céu Noturno. Atmosfera cósmica, vidro e símbolos do tempo. Selecionado.")
            .assertIsDisplayed()
    }
}
