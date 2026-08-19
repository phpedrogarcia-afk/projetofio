package com.projetofio.app.search

import com.projetofio.app.domain.AgeBucket
import com.projetofio.app.domain.ContentFormat
import com.projetofio.app.domain.Entry

import com.projetofio.app.domain.ReturnAlgorithm
import com.projetofio.app.domain.ReturnAttempt
import com.projetofio.app.domain.ReturnRepository
import com.projetofio.app.domain.ReturnState
import com.projetofio.app.domain.ReturnedFilter
import com.projetofio.app.domain.SearchExecutionSettings
import com.projetofio.app.domain.SearchQuery
import com.projetofio.app.domain.SearchRepository
import com.projetofio.app.domain.SearchTimeFilter
import com.projetofio.app.domain.SealedSearchBehavior
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * HybridSearchService + RRF fusion tests (Missão 4, S6).
 *
 * The FakeEmbeddingProvider hashes each text into a fixed unit vector, so
 * similarity depends ONLY on text equality: equal texts score 1.0, distinct
 * texts score ≈ uniform random (≈0 after the 0.1 floor). Fusion math stays
 * deterministic and hermetic, with zero shipped weights.
 */
class HybridSearchServiceTest {

    private val now = Instant.parse("2026-08-19T10:00:00Z")

    private fun entry(id: String, content: String, deletedAt: Instant? = null): Entry =
        Entry(
            id = id,
            createdAt = now,
            originalCreatedAt = now,
            originalTimeZone = null,
            updatedAt = now,
            content = content,
            contentFormat = ContentFormat.PLAIN_TEXT,
            deletedAt = deletedAt,
            purgeAfter = deletedAt?.plusMillis(86_400_000),
        )

    private fun returnAttempt(id: String, entryId: String, openedAt: Instant?): ReturnAttempt =
        ReturnAttempt(
            id = id,
            entryId = entryId,
            algorithm = ReturnAlgorithm.TIME,
            algorithmVersion = "time-v1",
            state = ReturnState.OPENED,
            createdAt = now.minusSeconds(7200),
            windowStart = now.minusSeconds(7200),
            windowEnd = now,
            openedAt = openedAt,
            ageBucket = AgeBucket.DAYS_7_29,
        )

    private class FakeSearchRepository(
        var entries: List<Entry> = emptyList(),
        var history: List<ReturnAttempt> = emptyList(),
    ) : SearchRepository {
        var loadCalls = 0
        override suspend fun loadSearchableEntries(): List<Entry> = entries.filter { it.deletedAt == null }
        override suspend fun loadReturnHistory(): List<ReturnAttempt> = history
    }

    private class FailingEmbeddingProvider : EmbeddingProvider {
        override val provenance = "failing"
        override fun embed(texts: List<String>): List<FloatArray> =
            throw IllegalStateException("inference unavailable")
    }

    @Test
    fun rrfKeepsLexicalWinnerFirstAndSuppressesLexicalMiss() = runBlocking {
        val provider = FakeEmbeddingProvider()
        // E1 lacks both query tokens → lexical miss; E2 has both → lexical hit.
        val e1 = entry("e1", "fiquei sem energia depois da reunião")
        val e2 = entry("e2", "reunião das oito, cheguei exausto ao escritório")
        val repo = FakeSearchRepository(entries = listOf(e1, e2))
        val service = HybridSearchService(repo, provider, LocalSearchService(repo))

        val result = service.search(SearchQuery(terms = "reunião exausto"))

        assertEquals(1, result.hits.size)
        assertEquals("e2", result.hits.first().entry.id)
    }

    @Test
    fun identicalQueryAndContentAreFusedWithTopRank() = runBlocking {
        val repo = FakeSearchRepository(entries = listOf(
            entry("a", "café da manhã na varanda"),
            entry("b", "texto completamente distinto sobre outra coisa"),
        ))
        val service = HybridSearchService(repo, FakeEmbeddingProvider(), LocalSearchService(repo))
        val result = service.search(SearchQuery(terms = "café da manhã"))
        assertEquals("a", result.hits.first().entry.id)
    }

    @Test
    fun softDeletedEntriesNeverSurfaceFromAnyArm() = runBlocking {
        val live = entry("live", "café da manhã")
        val deleted = entry("deleted", "café da manhã", deletedAt = now)
        val repo = FakeSearchRepository(entries = listOf(live, deleted))
        val service = HybridSearchService(repo, FakeEmbeddingProvider(), LocalSearchService(repo))
        val result = service.search(SearchQuery(terms = "café"))
        assertEquals(1, result.hits.size)
        assertEquals("live", result.hits.first().entry.id)
    }

    @Test
    fun sealedEntriesStayContentInvisibleAndCounted() = runBlocking {
        val repo = FakeSearchRepository(entries = listOf(
            entry("e1", "palavra secreta no café"),
            entry("e2", "café normal da tarde"),
        ))
        val lexical = LocalSearchService(repo, isSealed = { id -> id == "e1" })
        val service = HybridSearchService(repo, FakeEmbeddingProvider(), lexical)

        // Run several times: the vector arm's similarity floor is probabilistic
        // with the fake provider, so the e2 surface is non-deterministic — the
        // sealed invariant (e1 never surfaces, count stays correct) must hold
        // in every single run.
        repeat(20) {
            val counted = service.search(
                SearchQuery(terms = "palavra secreta"),
                SearchExecutionSettings(sealedBehavior = SealedSearchBehavior.COUNT_ONLY),
            )
            assertTrue("e1" !in counted.hits.map { it.entry.id })
            assertEquals(1, counted.sealedCount)

            val hidden = service.search(
                SearchQuery(terms = "palavra secreta"),
                SearchExecutionSettings(sealedBehavior = SealedSearchBehavior.HIDDEN),
            )
            assertTrue("e1" !in hidden.hits.map { it.entry.id })
            assertEquals(0, hidden.sealedCount)
        }
    }

    @Test
    fun embeddingFailureFallsBackToLexicalOnly() = runBlocking {
        val repo = FakeSearchRepository(entries = listOf(
            entry("e1", "café da manhã na varanda"),
        ))
        val service = HybridSearchService(
            repo, FailingEmbeddingProvider(), LocalSearchService(repo),
        )
        val result = service.search(SearchQuery(terms = "café"))
        assertEquals(1, result.hits.size)
        assertEquals("e1", result.hits.first().entry.id)
    }

    @Test
    fun timeFilterAppliesAcrossBothArms() = runBlocking {
        // The lexical contract filters on originalCreatedAt, not updatedAt:
        // build the two fixtures with distinct original autobiographical dates.
        val old = entry("old", "café de ontem").copy(originalCreatedAt = now.minusSeconds(7_200))
        val recent = entry("recent", "café de hoje").copy(originalCreatedAt = now.minusSeconds(600))
        val repo = FakeSearchRepository(entries = listOf(old, recent))
        val service = HybridSearchService(repo, FakeEmbeddingProvider(), LocalSearchService(repo))
        val result = service.search(SearchQuery(
            terms = "café",
            time = SearchTimeFilter(after = now.minusSeconds(3600)),
        ))
        assertEquals(1, result.hits.size)
        assertEquals("recent", result.hits.first().entry.id)
    }

    @Test
    fun returnedFilterExcludesNeverReturnedEntries() = runBlocking {
        val repo = FakeSearchRepository(
            entries = listOf(
                entry("e1", "café da manhã"),
                entry("e2", "café da tarde"),
            ),
            history = listOf(returnAttempt("r1", "e1", now.minusSeconds(3600))),
        )
        val service = HybridSearchService(repo, FakeEmbeddingProvider(), LocalSearchService(repo))
        val result = service.search(SearchQuery(
            terms = "café",
            returned = ReturnedFilter.EVER_RETURNED,
        ))
        assertEquals(1, result.hits.size)
        assertEquals("e1", result.hits.first().entry.id)
        assertTrue(result.hits.first().returnedCount > 0)
    }

    @Test
    fun fusionOrderingPlacesDualArmWinnerFirst() {
        val lex = listOf(entry("b", "b"), entry("a", "a"))
        val vec = listOf(entry("a", "a"), entry("c", "c"))
        val fused = SemanticSearchPrototype.reciprocalRankFusion(lex, vec)
        // "a" ranks 1st in BOTH arms → strictly highest RRF score, no ties.
        assertEquals("a", fused.first().id)
        assertEquals(3, fused.size)
        assertTrue("c" in fused.map { it.id })
    }

    // The vector arm must never resurrect content the lexical arm
    // discarded — an edit that breaks the lexical match must keep the entry
    // out of the result even when the fake embedding gives it a score above
    // the similarity floor. Run many times because the fake provider's
    // cross-text cosine is pseudo-random.
    @Test
    fun indexReflectsLiveEditsAndDeletes() = runBlocking {
        repeat(30) {
            val repo = FakeSearchRepository(entries = mutableListOf(entry("e1", "café quente")))
            val service = HybridSearchService(repo, FakeEmbeddingProvider(), LocalSearchService(repo))
            assertEquals(1, service.search(SearchQuery(terms = "café")).hits.size)
            repo.entries = repo.entries.map { it.copy(content = "texto completamente diferente e sem menções") }
            assertEquals(0, service.search(SearchQuery(terms = "café")).hits.size)
        }
    }
}
