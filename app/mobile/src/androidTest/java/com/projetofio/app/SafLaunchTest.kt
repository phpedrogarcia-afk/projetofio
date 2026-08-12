package com.projetofio.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
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
        composeRule.onNodeWithText("Configurações").performClick()
        composeRule.onNodeWithText("Exportar").assertIsDisplayed()
        composeRule
            .onNode(hasScrollToIndexAction())
            .performScrollToNode(hasText("Texto"))
        val appPackageName = composeRule.activity.packageName
        composeRule.onNodeWithText("Texto").performClick()

        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        composeRule.waitUntil(timeoutMillis = 15_000) {
            uiAutomation.rootInActiveWindow?.packageName?.toString()?.let { it != appPackageName } == true
        }
    }
}
