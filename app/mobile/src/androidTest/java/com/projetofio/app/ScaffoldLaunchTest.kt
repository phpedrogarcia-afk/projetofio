package com.projetofio.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.Lifecycle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScaffoldLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun quietWritePromptIsVisible() {
        // MainActivity starts behind the neutral privacy cover while encrypted
        // settings are opened. That coroutine is outside Compose's idling resource.
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        val prompt = "O que está passando pela sua cabeça hoje?"
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithText(prompt).assertIsDisplayed()
            }.isSuccess
        }
        composeRule.onNodeWithText(prompt).assertIsDisplayed()
    }

    @Test
    fun transientPauseDoesNotLeavePrivacyCoverStuck() {
        val prompt = "O que está passando pela sua cabeça hoje?"
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithText(prompt).assertIsDisplayed()
            }.isSuccess
        }

        // STARTED invokes onPause without onStop, matching a transient Android
        // permission surface. Returning to RESUMED must uncover Fio again.
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithText(prompt).assertIsDisplayed()
            }.isSuccess
        }
        composeRule.onNodeWithText(prompt).assertIsDisplayed()
    }

    @Test
    fun stoppedActivityResolvesAccessGateAgainBeforeShowingContent() {
        val prompt = "O que está passando pela sua cabeça hoje?"
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithText(prompt).assertIsDisplayed()
            }.isSuccess
        }

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithText(prompt).assertIsDisplayed()
            }.isSuccess
        }
        composeRule.onNodeWithText(prompt).assertIsDisplayed()
    }
}
