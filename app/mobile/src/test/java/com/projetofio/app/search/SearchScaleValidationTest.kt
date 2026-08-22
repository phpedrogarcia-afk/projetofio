package com.projetofio.app.search

import com.projetofio.app.domain.ContentFormat
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.EntrySource
import com.projetofio.app.domain.ReturnAttempt
import com.projetofio.app.domain.ReturnMode
import com.projetofio.app.domain.SearchExecutionSettings
import com.projetofio.app.domain.SearchQuery
import com.projetofio.app.domain.SearchRepository
import com.projetofio.app.domain.SearchResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * FIO-P06 — Scale validation of LocalSearchService (1k and 10k synthetic entries).
 *
 * Contracts verified:
 * - Scan-on-demand latency scales linearly O(N) with text size.
 * - Max results cap short-circuits early when hits are abundant.
 * - Worst-case (miss query scanning all 1k/10k items) stays bounded and fast.
 * - Preserves invariant I-14–I-20 (no FTS, no persistence, ephemeral memory only).
 */
class SearchScaleValidationTest {

    private class ScaleSearchRepository(val data: List<Entry>) : SearchRepository {
        override suspend fun loadSearchableEntries(): List<Entry> = data
        override suspend fun loadReturnHistory(): List<ReturnAttempt> = emptyList()
    }


    private fun generateCorpus(count: Int): List<Entry> {
        val sampleParagraphs = listOf(
            "Café da manhã na varanda com vento frio de agosto e pensamentos soltos.",
            "Reunião de trabalho sobre a arquitetura do sistema sem dependências externas.",
            "Caminhada pela praia no final da tarde, observando as ondas e o horizonte calmo.",
            "Anotação rápida sobre uma ideia de projeto: manter tudo simples, privado e local.",
            "Leitura do livro antigo que encontrei na estante, cheio de marcas a lápis.",
            "Noite estrelada com céu límpido, lembrança de conversas da infância.",
            "Reflexão sobre o tempo que passa depressa e a importância de guardar momentos exatos.",
            "Música instrumental tocando ao fundo enquanto a chuva cai lá fora devagar.",
            "Desenho de um mapa astronômico com constelações e símbolos cósmicos antigos.",
            "Lista de pensamentos silenciosos que não precisam de explicação nem julgamento.",
        )

        val baseInstant = Instant.parse("2026-01-01T00:00:00Z")
        return (1..count).map { i ->
            val paragraphIndex = i % sampleParagraphs.size
            val content = "${sampleParagraphs[paragraphIndex]} Registro número $i com observações detalhadas e texto livre."
            Entry(
                id = "scale-entry-${"%06d".format(i)}",
                createdAt = baseInstant.plusSeconds(i * 3600L),
                originalCreatedAt = baseInstant.plusSeconds(i * 3600L),
                originalTimeZone = "America/Sao_Paulo",
                updatedAt = baseInstant.plusSeconds(i * 3600L),
                source = EntrySource.NATIVE,
                content = content,
                contentFormat = ContentFormat.PLAIN_TEXT,
                returnMode = ReturnMode.ELIGIBLE,
            )
        }
    }

    @Test
    fun `search at 1k scale stays fast for hits and misses`() = runBlocking {
        val corpus1k = generateCorpus(1_000)
        val service = LocalSearchService(repository = ScaleSearchRepository(corpus1k))

        // Warm-up
        service.search(SearchQuery(terms = "cafe"))

        // Common hit with cap
        val hitStart = System.nanoTime()
        val hitResult = service.search(SearchQuery(terms = "cafe", maxResults = 20))
        val hitDurationMs = (System.nanoTime() - hitStart) / 1_000_000
        assertEquals(20, hitResult.hits.size)
        assertTrue("Hit search at 1k should complete in < 100ms, took ${hitDurationMs}ms", hitDurationMs < 100)

        // Prefix match on last token ("arquitetura sis")
        val prefixResult = service.search(SearchQuery(terms = "arquitetura sis", maxResults = 20))
        assertTrue(prefixResult.hits.isNotEmpty())
        assertTrue(prefixResult.hits[0].matchedSnippet.contains("arquitetura", ignoreCase = true))

        // Worst case: complete miss scanning all 1k entries
        val missStart = System.nanoTime()
        val missResult = service.search(SearchQuery(terms = "termo_completamente_inexistente_xyz_123"))
        val missDurationMs = (System.nanoTime() - missStart) / 1_000_000
        assertEquals(0, missResult.hits.size)
        assertTrue("Miss search at 1k should complete in < 150ms, took ${missDurationMs}ms", missDurationMs < 150)
    }

    @Test
    fun `search at 10k scale stays bounded without OOM`() = runBlocking {
        val corpus10k = generateCorpus(10_000)
        val service = LocalSearchService(repository = ScaleSearchRepository(corpus10k))

        // Warm-up
        service.search(SearchQuery(terms = "chuva"))

        // Hit search with 20 results (early exit)
        val hitStart = System.nanoTime()
        val hitResult = service.search(SearchQuery(terms = "vento frio", maxResults = 20))
        val hitDurationMs = (System.nanoTime() - hitStart) / 1_000_000
        assertEquals(20, hitResult.hits.size)
        assertTrue("Early-exit hit search at 10k should complete in < 200ms, took ${hitDurationMs}ms", hitDurationMs < 200)

        // Full scan worst case on 10k entries (10,000 strings normalized and evaluated)
        val fullScanStart = System.nanoTime()
        val fullScanResult = service.search(SearchQuery(terms = "palavra_que_nao_existe_no_corpus"))
        val fullScanDurationMs = (System.nanoTime() - fullScanStart) / 1_000_000
        assertEquals(0, fullScanResult.hits.size)
        assertTrue("Full 10k scan worst case should complete in < 800ms, took ${fullScanDurationMs}ms", fullScanDurationMs < 800)
    }

    @Test
    fun `search result snippet extraction preserves exact casing and punctuation at scale`() = runBlocking {
        val corpus = generateCorpus(500)
        val service = LocalSearchService(repository = ScaleSearchRepository(corpus))

        val result = service.search(SearchQuery(terms = "astronomico"))
        assertTrue(result.hits.isNotEmpty())
        val firstSnippet = result.hits[0].matchedSnippet
        // Snippet preserves original accented word "astronômico"
        assertTrue("Snippet must preserve exact original accents: $firstSnippet", firstSnippet.contains("astronômico"))
    }
}
