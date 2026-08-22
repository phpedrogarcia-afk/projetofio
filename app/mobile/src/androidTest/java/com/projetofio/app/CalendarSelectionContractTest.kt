package com.projetofio.app

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarSelectionContractTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun selectedCellHeadlineAndConfirmedPolicyKeepTheSameCivilDate() {
        val today = LocalDate.now()
        val chosen = if (today.dayOfMonth < today.lengthOfMonth()) today.plusDays(1) else today.minusDays(1)
        val label = chosen.format(
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR")),
        )

        composeRule.onNodeWithTag("fio-return-policy").performClick()
        composeRule
            .onNodeWithContentDescription("Definir retorno: Escolher uma data")
            .performScrollTo()
            .performClick()

        val chosenCell = hasText(chosen.dayOfMonth.toString(), substring = true) or
            hasContentDescription(chosen.dayOfMonth.toString(), substring = true)
        composeRule
            .onAllNodes(chosenCell, useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.onNodeWithTag("fio-date-picker-headline").assertTextContains(label)
        composeRule.onNodeWithText("Escolher").performClick()
        composeRule.onNodeWithContentDescription(label, substring = true).assertExists()
    }
}

