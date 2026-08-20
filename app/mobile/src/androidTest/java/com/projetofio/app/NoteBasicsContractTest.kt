package com.projetofio.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteBasicsContractTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val createdContents = mutableSetOf<String>()

    @After
    fun removeSyntheticEntries() = runBlocking {
        val application = ApplicationProvider.getApplicationContext<FioApplication>()
        val service = application.graph.service
        service.observeActiveEntries().first()
            .filter { it.content in createdContents }
            .forEach { entry ->
                service.moveToRecentlyDeleted(entry.id)
                service.permanentlyDelete(entry.id)
            }
        service.observeDeletedEntries().first()
            .filter { it.content in createdContents }
            .forEach { service.permanentlyDelete(it.id) }
        service.autosaveDraft(" ")
    }

    @Test
    fun userCanDiscoverEditDeleteAndRecoverFromTheInterface() {
        val original = "Nota básica original ${System.nanoTime()}"
        val edited = "Nota básica editada ${System.nanoTime()}"
        createdContents += original
        createdContents += edited

        composeRule.onNode(hasSetTextAction()).performTextInput(original)
        composeRule.onNodeWithContentDescription("Guardar lembrança").performClick()
        composeRule.onNodeWithContentDescription("Abrir Arquivo").performClick()

        composeRule
            .onNodeWithContentDescription(original, substring = true)
            .performClick()
        composeRule.onNodeWithText("Editar").assertIsDisplayed()
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNodeWithContentDescription("Tela Arquivo").assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(original, substring = true)
            .performClick()
        composeRule.onNodeWithText("Editar").assertIsDisplayed().performClick()
        composeRule.onNode(hasSetTextAction()).performTextReplacement(edited)
        composeRule.onNodeWithText("Guardar alterações").performClick()

        composeRule
            .onNodeWithContentDescription(edited, substring = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText("Excluir").performClick()
        composeRule.onNodeWithText("Mover").performClick()

        composeRule.onNodeWithText(edited).assertDoesNotExist()
        composeRule.onNodeWithText("Ajustes").performClick()
        composeRule
            .onNodeWithContentDescription("Excluídos recentemente", substring = true)
            .performClick()
        composeRule.onNodeWithText(edited).assertIsDisplayed()
        composeRule.onNodeWithText("Recuperar").performClick()

        composeRule.onNodeWithText(edited).assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Voltar aos Ajustes").performClick()
        composeRule.onNodeWithContentDescription("Voltar").performClick()
        composeRule.onNodeWithContentDescription("Abrir Arquivo").performClick()
        composeRule
            .onNodeWithContentDescription(edited, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun settingsOverviewUsesPlainLanguageAndFocusedDestinations() {
        composeRule.onNodeWithText("Ajustes").performClick()
        composeRule.onNodeWithContentDescription("Tela Ajustes").assertIsDisplayed()
        composeRule.onNodeWithText("M1", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("validação", substring = true, ignoreCase = true).assertDoesNotExist()
        composeRule.onNodeWithText("lote", substring = true, ignoreCase = true).assertDoesNotExist()

        composeRule
            .onNodeWithContentDescription("Proteção ao abrir", substring = true)
            .performClick()
        composeRule.onNodeWithContentDescription("Tela Proteção ao abrir").assertIsDisplayed()
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNodeWithContentDescription("Tela Ajustes").assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription("Exportar uma cópia", substring = true)
            .performClick()
        composeRule.onNodeWithContentDescription("Tela Exportar uma cópia").assertIsDisplayed()
        composeRule.onNodeWithText("Se estiver em dúvida", substring = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Voltar aos Ajustes").performClick()

        composeRule
            .onNodeWithContentDescription("Excluídos recentemente", substring = true)
            .performClick()
        composeRule.onNodeWithContentDescription("Tela Excluídos recentemente").assertIsDisplayed()
    }

    @Test
    fun longRealisticNoteKeepsWordsReadableAndActionsReachable() {
        val longNote = """
            Hoje eu percebi que algumas mudanças chegam devagar. Não houve uma grande resposta, apenas a sensação de que eu podia respirar um pouco melhor antes de decidir o próximo passo.

            Caminhei sem pressa e reparei nas árvores, no barulho distante da rua e na maneira como a luz atravessava as folhas. Pensei nas pessoas que estiveram comigo em épocas diferentes e no que cada encontro deixou.

            Ainda existem perguntas sem resposta. Algumas pedem coragem; outras talvez só precisem de tempo. Quero me lembrar de que nem todo silêncio é vazio e de que descansar também pode ser uma forma de continuar.

            Se eu reencontrar estas palavras no futuro, espero não procurar uma conclusão perfeita. Basta reconhecer que, neste dia, eu estava tentando cuidar do que importava sem apagar o que senti.

            No fim, quero guardar isto: eu não precisava resolver a vida inteira hoje. Precisava apenas estar presente o bastante para dar o próximo passo com gentileza.
        """.trimIndent()
        createdContents += longNote

        composeRule.onNode(hasSetTextAction()).performTextInput(longNote)
        composeRule.onNodeWithContentDescription("Guardar lembrança").performClick()
        composeRule.onNodeWithContentDescription("Abrir Arquivo").performClick()
        composeRule
            .onNodeWithContentDescription("Hoje eu percebi", substring = true)
            .performClick()

        composeRule.onNodeWithText("Hoje eu percebi", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Editar").assertIsDisplayed()
        composeRule.onNodeWithText("Excluir").performScrollTo().assertIsDisplayed()
    }
}
