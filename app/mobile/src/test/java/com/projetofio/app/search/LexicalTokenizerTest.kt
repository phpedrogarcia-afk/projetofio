package com.projetofio.app.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PT-BR lexical baseline contracts:
 * - Normalization folds diacritics and case; snippet always comes from the
 *   original text (never the normalized form) — search retrieves the
 *   person's exact words.
 * - Queries match whole normalized tokens, in order; only the LAST token
 *   may match a prefix (mid-typing).
 */
class LexicalTokenizerTest {

    @Test
    fun normalizesDiacriticsAndCase() {
        assertEquals("nao me lembro", LexicalTokenizer.normalize("Não me lembro"))
        assertEquals("coracao", LexicalTokenizer.normalize("Coração"))
        assertEquals("sao paulo", LexicalTokenizer.normalize("São Paulo"))
        assertEquals("excecao", LexicalTokenizer.normalize("Exceção"))
        assertEquals("dia ensolarado", LexicalTokenizer.normalize("Dia ensolarado"))
    }

    @Test
    fun stripsPunctuationAndLigatures() {
        assertEquals("oi tudo bem", LexicalTokenizer.normalize("Oi, tudo bem?"))
        assertEquals("reuniao as 14h", LexicalTokenizer.normalize("Reunião às 14h"))
    }

    @Test
    fun blankTextNormalizesToEmpty() {
        assertEquals("", LexicalTokenizer.normalize("   "))
        assertEquals("", LexicalTokenizer.normalize("— … ,"))
        assertEquals(emptyList<LexicalTokenizer.NormalizedToken>(), LexicalTokenizer.tokenize("   "))
    }

    @Test
    fun tokensPreservePositionsInNormalizedText() {
        val tokens = LexicalTokenizer.tokenize("O café da manhã")
        assertEquals(4, tokens.size)
        assertEquals(LexicalTokenizer.NormalizedToken("o", 0), tokens[0])
        assertEquals(LexicalTokenizer.NormalizedToken("cafe", 2), tokens[1])
        assertEquals(LexicalTokenizer.NormalizedToken("da", 7), tokens[2])
        assertEquals(LexicalTokenizer.NormalizedToken("manha", 10), tokens[3])
    }

    @Test
    fun containsQueryMatchesWholeTokensInOrder() {
        val doc = LexicalTokenizer.normalize("Hoje fui ao mercado e comprei pão")
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, listOf("mercado")))
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, listOf("fui", "mercado")))
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, listOf("hoje", "fui", "mercado")))
        // Out of order must not match.
        assertFalse(LexicalTokenizer.containsQueryTokens(doc, listOf("mercado", "fui")))
    }

    @Test
    fun lastQueryTokenMayMatchPrefix() {
        val doc = LexicalTokenizer.normalize("Reunião sobre o lançamento do produto")
        // Single-token prefix match (mid-typing) is the core contract.
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, listOf("lanc")))
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, listOf("prod")))
        // Two tokens: the FIRST must be a whole token match; the LAST may be
        // a prefix (whole "reuniao", then partial "sobr" → "sobre").
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, listOf("reuniao", "sobr")))
        // Whole whole always works.
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, listOf("reuniao", "sobre")))
        // A partial FIRST token must NOT match (only the last token may be partial).
        assertFalse(LexicalTokenizer.containsQueryTokens(doc, listOf("reunia", "sobre")))
        // Order follows the document: "sobre" precedes "lancamento", so a query
        // asking for "produto" first can never be satisfied by this entry.
        assertFalse(LexicalTokenizer.containsQueryTokens(doc, listOf("produto", "lanc")))
        // And "prod" (partial "produto") comes after "lancamento" in the doc,
        // so the earlier token cannot be revisited once "prod" consumed it.
        assertFalse(LexicalTokenizer.containsQueryTokens(doc, listOf("prod", "lancamento")))
    }

    @Test
    fun diacriticFoldingMakesAccentedQueriesMatchPlainWords() {
        val doc = LexicalTokenizer.normalize("Vi a cidade de São Paulo")
        // Query tokens arrive already normalized (the service normalizes the
        // person's query the same way it normalizes the entry content).
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, listOf("sao")))
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, LexicalTokenizer.tokenize("são").map { it.text }))
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, LexicalTokenizer.tokenize("SÃO").map { it.text }))
    }

    @Test
    fun emptyQueryMatchesAnything() {
        val doc = LexicalTokenizer.normalize("Qualquer texto aqui")
        assertTrue(LexicalTokenizer.containsQueryTokens(doc, emptyList()))
    }

    @Test
    fun snippetIsASliceOfTheOriginalText() {
        val original = "Hoje pensei em escrever sobre o café que tomei na esquina"
        val normalized = LexicalTokenizer.normalize(original)
        val snippet = LexicalTokenizer.snippetAround(original, normalized, listOf("cafe"))
        assertTrue("snippet must preserve original wording and accent: $snippet",
            snippet.contains("café"))
        // Never shows the normalized (accent-stripped) form alone.
        assertFalse(snippet.contains("cafe ") && !snippet.contains("café"))
    }

    @Test
    fun snippetShowsEllipsisWhenTruncated() {
        val original = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
        val snippet = LexicalTokenizer.snippetAround(original, LexicalTokenizer.normalize(original), listOf("tempor"))
        assertTrue("left truncation expected: $snippet", snippet.startsWith("…"))
        assertTrue("right truncation expected: $snippet", snippet.endsWith("…"))
    }
}
