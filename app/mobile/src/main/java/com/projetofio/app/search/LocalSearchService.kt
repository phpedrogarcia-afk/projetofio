package com.projetofio.app.search

import com.projetofio.app.domain.ReturnAttempt
import com.projetofio.app.domain.ReturnState
import com.projetofio.app.domain.ReturnedFilter
import com.projetofio.app.domain.SearchExecutionSettings
import com.projetofio.app.domain.SearchHit
import com.projetofio.app.domain.SearchQuery
import com.projetofio.app.domain.SearchRepository
import com.projetofio.app.domain.SearchResult
import com.projetofio.app.domain.SearchScope
import com.projetofio.app.domain.SearchService
import com.projetofio.app.domain.SealedSearchBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lexical baseline: decrypt-and-scan on demand (Option A).
 *
 * Privacy contracts (SEARCH-PRIVACY-ARCHITECTURE.md):
 * - Decrypted content lives only in memory for the duration of this call.
 * - Nothing (queries, tokens, scores, snippets) is persisted or logged.
 * - Sealed entries never contribute content to results.
 *
 * Failure contract: if anything throws, the caller gets no partial data —
 * the repository layer stays the single source of truth, so search can
 * never silently show stale content.
 */
class LocalSearchService(
    private val repository: SearchRepository,
    /**
     * Pluggable sealed detection. Production defaults to "no sealed entries"
     * (M1 has no entry-level sealed flag); a future sealed marker/query or
     * an authorization boundary overrides this single point without touching
     * search internals (threat model contract).
     */
    private val isSealed: (id: String) -> Boolean = { false },
) : SearchService {

    override suspend fun search(
        query: SearchQuery,
        settings: SearchExecutionSettings,
    ): SearchResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val queryTokens = LexicalTokenizer.normalize(query.terms)
            .split(' ')
            .filter { it.isNotEmpty() }

        val entries = repository.loadSearchableEntries()
        val history = repository.loadReturnHistory()

        // "Já voltou" evidence: opened returns per entry.
        val returnsByEntry = history.groupBy { it.entryId }

        val hits = mutableListOf<SearchHit>()
        var sealedSkipped = 0

        for (entry in entries) {
            // Sealed entries are outside this V1 scope (threat model).
            if (isSealed(entry.id)) {
                if (settings.sealedBehavior == SealedSearchBehavior.COUNT_ONLY && matchesQuery(entry, queryTokens)) {
                    sealedSkipped++
                }
                continue
            }

            if (!matchesFilters(entry, query, returnsByEntry[entry.id])) continue
            if (!matchesQuery(entry, queryTokens)) continue

            val normalized = LexicalTokenizer.normalize(entry.content)
            hits += SearchHit(
                entry = entry,
                matchedSnippet = LexicalTokenizer.snippetAround(entry.content, normalized, queryTokens),
                matchedAtOriginalDate = entry.originalCreatedAt,
                returnedCount = (returnsByEntry[entry.id].orEmpty())
                    .count { it.state == ReturnState.OPENED },
                lastReturnedAt = (returnsByEntry[entry.id].orEmpty())
                    .mapNotNull { it.openedAt }
                    .maxOrNull(),
            )
            if (hits.size >= query.maxResults) break
        }

        SearchResult(
            query = query,
            hits = hits,
            totalMatched = hits.size,
            sealedCount = sealedSkipped,
            elapsedMillis = System.currentTimeMillis() - start,
        )
    }

    private fun matchesQuery(entry: com.projetofio.app.domain.Entry, queryTokens: List<String>): Boolean =
        LexicalTokenizer.containsQueryTokens(LexicalTokenizer.normalize(entry.content), queryTokens)

    private fun matchesFilters(
        entry: com.projetofio.app.domain.Entry,
        query: SearchQuery,
        entryReturns: List<ReturnAttempt>?,
    ): Boolean {
        val opened = entryReturns.orEmpty().filter { it.state == ReturnState.OPENED }
        if (query.returned == ReturnedFilter.EVER_RETURNED && opened.isEmpty()) return false
        if (query.returned == ReturnedFilter.NEVER_RETURNED && opened.isNotEmpty()) return false

        val when_ = entry.originalCreatedAt
        val after = query.time.after
        val before = query.time.before
        if (after != null && when_ < after) return false
        if (before != null && when_ > before) return false

        return true
    }
}

