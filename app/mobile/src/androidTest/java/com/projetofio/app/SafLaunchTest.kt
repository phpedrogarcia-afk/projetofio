package com.projetofio.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SafLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun textExportPickerLaunchesWithoutCrashing() {
        composeRule.onNodeWithText("Ajustes").performClick()
        composeRule.onNodeWithText("Exportar uma cópia").performClick()
        composeRule.onNodeWithText("Exportar uma cópia").assertIsDisplayed()
        val appPackageName = composeRule.activity.packageName
        composeRule.onNodeWithText("Criar arquivo de texto (.txt)").performClick()

        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        composeRule.waitUntil(timeoutMillis = 15_000) {
            uiAutomation.rootInActiveWindow?.packageName?.toString()?.let { it != appPackageName } == true
        }
    }
}
