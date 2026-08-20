package com.projetofio.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrustedLocalFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private var syntheticContent: String? = null

    @After
    fun removeSyntheticEntry() = runBlocking {
        val content = syntheticContent ?: return@runBlocking
        val application = ApplicationProvider.getApplicationContext<FioApplication>()
        val service = application.graph.service
        service.observeActiveEntries().first()
            .filter { it.content == content }
            .forEach { entry ->
                service.moveToRecentlyDeleted(entry.id)
                service.permanentlyDelete(entry.id)
            }
        service.autosaveDraft(" ")
    }

    @Test
    fun exactEntrySurvivesActivityRecreationAndRemainsInArchive() {
        val content = "Fluxo sintético local — çã ☕ ${System.nanoTime()}"
        syntheticContent = content

        composeRule.onNode(hasSetTextAction()).performTextInput(content)
        composeRule.onNodeWithContentDescription("Guardar lembrança").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Guardado.").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Guardado.").assertExists()

        composeRule.onNodeWithContentDescription("Abrir Arquivo").performClick()
        composeRule.onNodeWithText(content).assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithContentDescription("Abrir Arquivo").performClick()
        composeRule.onNodeWithText(content).assertIsDisplayed()
    }
}
