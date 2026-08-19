package com.projetofio.app.domain

// SearchExecutionSettings lives in this package by design.

/**
 * Read-only source of entries and return evidence for the search layer.
 *
 * Search is canonical **Encontrar**: it must never mutate entries or return
 * history through this boundary. Write operations remain exclusively in
 * `FioRepository` / `ReturnRepository` so the search surface stays a pure
 * retrieval lens (SEARCH-ARCHITECTURE.md).
 */
interface SearchRepository {
    /**
     * Current active entries with decrypted content. Ordering is irrelevant
     * for search (it re-ranks lexically) but implementations may return a
     * stable order for deterministic pagination tests.
     */
    suspend fun loadSearchableEntries(): List<Entry>

    /**
     * Full return history; the search layer filters per-entry (state OPENED
     * drives "já voltou" evidence). Never written by search code paths.
     */
    suspend fun loadReturnHistory(): List<ReturnAttempt>
}

interface SearchService {
    /**
     * Execute an Encontra query. Plaintext exists only in memory for the
     * duration of this call (Opção A — scan sob demanda).
     *
     * Contracts:
     * - Never returns sealed entry content; sealedCount reports how many
     *   sealed entries were skipped (opaque — threat model decision).
     * - Snippets are slices of the original text, never generated.
     * - The index is the live decrypted store: edits/deletes/purges are
     *   visible on the very next query (no stale index).
     * - Never persists queries, tokens, scores, or history.
     */
    suspend fun search(query: SearchQuery, settings: SearchExecutionSettings = SearchExecutionSettings()): SearchResult
}

/**
 * Execution-time knobs that the caller (UI/feature-flag layer) supplies.
 * Kept out of [SearchQuery] on purpose: query shape belongs to the person's
 * intent; operational settings belong to the app's runtime configuration.
 */
data class SearchExecutionSettings(
    val sealedBehavior: SealedSearchBehavior = SealedSearchBehavior.COUNT_ONLY,
)
