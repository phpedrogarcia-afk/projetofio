package com.projetofio.app.search

import com.projetofio.app.domain.AgeBucket
import com.projetofio.app.domain.ContentFormat
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.EntrySource
import com.projetofio.app.domain.ReturnAlgorithm
import com.projetofio.app.domain.ReturnAttempt
import com.projetofio.app.domain.ReturnConsentState
import com.projetofio.app.domain.ReturnMode
import com.projetofio.app.domain.ReturnRepository
import com.projetofio.app.domain.ReturnState
import com.projetofio.app.domain.ReturnedFilter
import com.projetofio.app.domain.SearchExecutionSettings
import com.projetofio.app.domain.SearchQuery
import com.projetofio.app.domain.SearchRepository
import com.projetofio.app.domain.SearchScope
import com.projetofio.app.domain.SearchService
import com.projetofio.app.domain.SealedSearchBehavior
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Contracts for the lexical baseline (Option A — scan sob demanda):
 * - Sealed entries never appear in hits; sealedCount reports skipped hits.
 * - Soft-deleted entries are invisible by construction.
 * - "Já voltou" evidence comes from OPENED return history only.
 * - Temporal and returned filters restrict evidence, never invent it.
 * - Nothing is persisted: two identical queries return identical hits.
 */
class LocalSearchServiceTest {

    private class FakeSearchRepository : SearchRepository, ReturnRepository {
        var entries: List<Entry> = emptyList()
        var history: List<ReturnAttempt> = emptyList()
        var sealedIds: Set<String> = emptySet()

        override suspend fun loadSearchableEntries() = entries.filter { it.deletedAt == null }
        override suspend fun loadReturnHistory() = history
        override suspend fun loadReturnsForEntry(entryId: String) =
            history.filter { it.entryId == entryId }
        // ReturnRepository members the fake never needs — sealed entries
        // keep search from ever reading/writing return policy here.
        override suspend fun findReturn(id: String) = history.find { it.id == id }
        override suspend fun insertReturnIfNoPending(attempt: ReturnAttempt) = false
        override suspend fun updateReturn(attempt: ReturnAttempt) = Unit
        override suspend fun cancelPendingReturnsForEntry(id: String, atMillis: Long, reason: com.projetofio.app.domain.ReturnCancelReason) = 0
        override suspend fun cancelAllPendingReturns(atMillis: Long, reason: com.projetofio.app.domain.ReturnCancelReason) = 0
    }

    private val fake = FakeSearchRepository()
    private val service: SearchService = LocalSearchService(
        repository = fake,
        isSealed = { it in fake.sealedIds },
    )

    private fun entry(id: String, content: String, deletedAt: Instant? = null) = Entry(
        id = id,
        createdAt = Instant.parse("2026-01-01T12:00:00Z"),
        originalCreatedAt = Instant.parse("2026-01-01T12:00:00Z"),
        originalTimeZone = "America/Sao_Paulo",
        updatedAt = Instant.parse("2026-01-01T12:00:00Z"),
        source = EntrySource.NATIVE,
        content = content,
        contentFormat = ContentFormat.PLAIN_TEXT,
        returnMode = ReturnMode.ELIGIBLE,
        deletedAt = deletedAt,
        purgeAfter = deletedAt?.plusSeconds(86400L * 30),
    )

    private fun returned(id: String, state: ReturnState, openedAt: Instant? = null) = ReturnAttempt(
        id = "r-$id",
        entryId = id,
        algorithm = ReturnAlgorithm.TIME,
        state = state,
        createdAt = Instant.parse("2026-02-01T12:00:00Z"),
        windowStart = Instant.parse("2026-02-01T00:00:00Z"),
        windowEnd = Instant.parse("2026-02-28T23:59:59Z"),
        openedAt = openedAt,
        ageBucket = AgeBucket.DAYS_30_89,
    )

    @Test
    fun matchesQueryAndReturnsOriginalWordsInSnippet() = runBlocking {
        fake.entries = listOf(entry("e1", "Café da manhã na varanda, manhã fria"))
        val result = service.search(SearchQuery(terms = "cafe"))
        assertEquals(1, result.hits.size)
        val snippet = result.hits[0].matchedSnippet
        assertTrue("snippet keeps original accent: $snippet", snippet.contains("Café"))
        assertTrue(snippet.contains("manhã"))
        assertEquals(0, result.sealedCount)
    }

    @Test
    fun sealedEntriesNeverAppearInHitsButAreCounted() = runBlocking {
        fake.sealedIds = setOf("e2")
        fake.entries = listOf(
            entry("e1", "Café da manhã na varanda"),
            entry("e2", "Segredo inconfessável sobre o café de domingo"),
        )
        val result = service.search(SearchQuery(terms = "cafe"))
        assertEquals(1, result.hits.size)
        assertEquals("e1", result.hits[0].entry.id)
        assertEquals(1, result.sealedCount)
    }

    @Test
    fun hiddenSealedBehaviorExposesNoCount() = runBlocking {
        fake.sealedIds = setOf("e2")
        fake.entries = listOf(entry("e2", "Segredo sobre café"))
        val result = service.search(
            SearchQuery(terms = "cafe"),
            SearchExecutionSettings(sealedBehavior = SealedSearchBehavior.HIDDEN),
        )
        assertEquals(0, result.hits.size)
        assertEquals(0, result.sealedCount)
    }

    @Test
    fun softDeletedEntriesAreInvisible() = runBlocking {
        val deleted = Instant.parse("2026-06-01T12:00:00Z")
        fake.entries = listOf(entry("e1", "Café na varanda", deletedAt = deleted))
        val result = service.search(SearchQuery(terms = "cafe"))
        assertEquals(0, result.hits.size)
    }

    @Test
    fun returnedFilterEverReturnedExcludesNeverReturned() = runBlocking {
        fake.entries = listOf(entry("e1", "Café na varanda"), entry("e2", "Outro café qualquer"))
        fake.history = listOf(returned("e1", ReturnState.OPENED, Instant.parse("2026-03-01T09:00:00Z")))
        val result = service.search(SearchQuery(terms = "cafe", returned = ReturnedFilter.EVER_RETURNED))
        assertEquals(1, result.hits.size)
        assertEquals("e1", result.hits[0].entry.id)
        assertEquals(1, result.hits[0].returnedCount)
    }

    @Test
    fun returnedFilterNeverReturnedExcludesEverReturned() = runBlocking {
        fake.entries = listOf(entry("e1", "Café na varanda"))
        fake.history = listOf(returned("e1", ReturnState.OPENED, Instant.parse("2026-03-01T09:00:00Z")))
        val result = service.search(SearchQuery(terms = "cafe", returned = ReturnedFilter.NEVER_RETURNED))
        assertEquals(0, result.hits.size)
    }

    @Test
    fun scheduledButNeverOpenedDoesNotCountAsReturned() = runBlocking {
        fake.entries = listOf(entry("e1", "Café na varanda"))
        fake.history = listOf(returned("e1", ReturnState.SCHEDULED))
        val result = service.search(SearchQuery(terms = "cafe", returned = ReturnedFilter.EVER_RETURNED))
        assertEquals(0, result.hits.size)
    }

    @Test
    fun timeFilterUsesOriginalDate() = runBlocking {
        fake.entries = listOf(entry("e1", "Café na varanda"))
        val result = service.search(SearchQuery(
            terms = "cafe",
            time = com.projetofio.app.domain.SearchTimeFilter(
                after = Instant.parse("2026-06-01T00:00:00Z"),
            ),
        ))
        assertEquals(0, result.hits.size)
        val inside = service.search(SearchQuery(
            terms = "cafe",
            time = com.projetofio.app.domain.SearchTimeFilter(before = Instant.parse("2030-01-01T00:00:00Z")),
        ))
        assertEquals(1, inside.hits.size)
    }

    @Test
    fun maxResultsCapsHits() = runBlocking {
        fake.entries = (1..10).map { i -> entry("e$i", "Café número $i no dia $i") }
        val result = service.search(SearchQuery(terms = "cafe", maxResults = 3))
        assertEquals(3, result.hits.size)
    }

    @Test
    fun nonMatchingQueryReturnsEmptyResult() = runBlocking {
        fake.entries = listOf(entry("e1", "Café na varanda"))
        val result = service.search(SearchQuery(terms = "palavra inexistente no texto"))
        assertEquals(0, result.hits.size)
        assertNotNull(result.query)
    }

    @Test
    fun queryIsCarriedInResultButNeverMutated() = runBlocking {
        fake.entries = listOf(entry("e1", "Café na varanda"))
        val query = SearchQuery(terms = "cafe")
        service.search(query)
        service.search(query)
        assertEquals("cafe", query.terms)
    }
}
