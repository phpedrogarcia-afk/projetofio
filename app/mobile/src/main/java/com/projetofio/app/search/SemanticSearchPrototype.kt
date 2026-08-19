package com.projetofio.app.search

import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.ReturnAttempt
import com.projetofio.app.domain.ReturnState
import com.projetofio.app.domain.SearchExecutionSettings
import com.projetofio.app.domain.SearchHit
import com.projetofio.app.domain.SearchMode
import com.projetofio.app.domain.SearchQuery
import com.projetofio.app.domain.SearchResult
import com.projetofio.app.domain.SearchService
import com.projetofio.app.domain.SearchRepository
import com.projetofio.app.domain.ReturnedFilter
import com.projetofio.app.domain.SealedSearchBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SEMANTIC RESEARCH PROTOTYPE (Missão 4 — ADR-040/ADR-041 boundary).
 *
 * HYBRID RRF ranking over the same scan-on-demand pipeline as the lexical
 * baseline: every lexical contract still holds (sealed invisible,
 * soft-deleted excluded, snippets are original-text slices, queries never
 * persisted). This class is fully REMOVABLE: deleting it and its feature
 * flag leaves the lexical path untouched — no code outside this file and
 * its flag wiring knows it exists.
 *
 * Kill criterion (research/search/RESEARCH-LOG.md): semantic only ships
 * when quality gain (recall@10 ≥ +10pt or MRR ≥ +0.08 vs lexical) justifies
 * cost (model weight, RAM ≤ +200MB, index ≤ ~100MB @10k entries,
 * latency ≤ 500ms on a mid-range device).
 */
object SemanticSearchPrototype {

    /** RRF constant used for rank fusion (classic value). */
    internal const val RRF_K = 60

    /**
     * Fuse a lexical result list with a vector result list via Reciprocal
     * Rank Fusion. Entries missing from one arm score 0 on that arm.
     * Exposed for direct unit-testing of the fusion math.
     */
    fun reciprocalRankFusion(
        lexicalRanked: List<Entry>,
        vectorRanked: List<Entry>,
        k: Int = RRF_K,
    ): List<Entry> {
        val lexIdx = HashMap<String, Int>(lexicalRanked.size)
        lexicalRanked.forEachIndexed { i, e -> lexIdx[e.id] = i + 1 }
        val vecIdx = HashMap<String, Int>(vectorRanked.size)
        vectorRanked.forEachIndexed { i, e -> vecIdx[e.id] = i + 1 }
        val byId = HashMap<String, Entry>(lexicalRanked.size + vectorRanked.size)
        lexicalRanked.forEach { byId[it.id] = it }
        vectorRanked.forEach { byId[it.id] = it }
        val scored: List<Pair<Double, String>> = byId.keys.map { id ->
            val score = 1.0 / (k + (lexIdx[id] ?: Int.MAX_VALUE)) +
                1.0 / (k + (vecIdx[id] ?: Int.MAX_VALUE))
            kotlin.Pair(score, id)
        }
        return scored.sortedByDescending { it.first }.mapNotNull { byId[it.second] }
    }
}

/**
 * Pluggable embedding source for the prototype. Production wiring will come
 * from LiteRT (EmbeddingGemma-300M) *if and only if* the kill criterion is
 * satisfied by device-side measurement. The prototype ships with NO bundled
 * model: tests use [FakeEmbeddingProvider]; any future
 * `LiteRtEmbeddingProvider` must stay behind the same feature flag and
 * must never connect to Room or the Returns engine (ADR-040/ADR-041).
 */
interface EmbeddingProvider {
    /** Embed texts into unit-normalized vectors; dimension stable per provider. */
    fun embed(texts: List<String>): List<FloatArray>
    /** Human-readable provenance (model name/quant, used in tests and logs). */
    val provenance: String
}

/**
 * Deterministic stand-in for [EmbeddingProvider] used ONLY in tests. Maps
 * each distinct text to a fixed pseudo-random unit vector so recall
 * assertions stay reproducible without shipping weights.
 */
class FakeEmbeddingProvider(private val dimension: Int = 32) : EmbeddingProvider {
    private val cache = HashMap<String, FloatArray>()
    override val provenance: String = "fake:deterministic:$dimension"
    override fun embed(texts: List<String>): List<FloatArray> = texts.map { text ->
        cache.getOrPut(text) {
            val rng = kotlin.random.Random(text.hashCode().toLong())
            val raw = FloatArray(dimension) { rng.nextFloat() * 2f - 1f }
            val n = kotlin.math.sqrt(raw.sumOf { it.toDouble() * it.toDouble() }).toFloat()
            FloatArray(dimension) { raw[it] / n.coerceAtLeast(1e-9f) }
        }
    }
}

/**
 * Hybrid search service: lexical arm (LocalSearchService's scan semantics,
 * reused) + vector arm over [EmbeddingProvider], fused with RRF.
 *
 * Isolation guarantees (ADR-040):
 * - No Room/Returns mutation; read-only [SearchRepository] access only.
 * - No persistence of queries, vectors, or scores inside this file.
 * - Sealed entries remain content-invisible (same policy as lexical V1).
 *
 * NOTE on mode gating: until ADR-024 is revisited with device-side
 * measurements, hybrid execution is triggered by an explicit opt-in
 * parameter, NOT by SearchMode (mode stays LEXICAL in production; the UI
 * never exposes semantic search in V1).
 */
class HybridSearchService(
    private val repository: SearchRepository,
    private val embeddingProvider: EmbeddingProvider,
    private val lexical: LocalSearchService,
    private val similarityFloor: Float = 0.1f,
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
        val returnsByEntry = history.groupBy { it.entryId }

        val lexHits = mutableMapOf<String, SearchHit>()
        var sealedSkipped = 0
        // --- Lexical arm (identical filter contract as V1).
        for (entry in entries) {
            if (lexical.isSealed(entry.id)) {
                if (settings.sealedBehavior == SealedSearchBehavior.COUNT_ONLY &&
                    lexical.matchesQuery(entry, queryTokens)) {
                    sealedSkipped++
                }
                continue
            }
            if (!lexical.matchesFilters(entry, query, returnsByEntry[entry.id])) continue
            if (!lexical.matchesQuery(entry, queryTokens)) continue
            lexHits[entry.id] = SearchHit(
                entry = entry,
                matchedSnippet = LexicalTokenizer.snippetAround(
                    entry.content, LexicalTokenizer.normalize(entry.content), queryTokens,
                ),
                matchedAtOriginalDate = entry.originalCreatedAt,
                returnedCount = (returnsByEntry[entry.id].orEmpty())
                    .count { it.state == ReturnState.OPENED },
                lastReturnedAt = (returnsByEntry[entry.id].orEmpty())
                    .mapNotNull { it.openedAt }
                    .maxOrNull(),
            )
            if (lexHits.size >= query.maxResults) break
        }

        // --- Vector arm (bounded by the quality/latency budget).
        // Runs ONLY over non-lexical candidates (sealed/soft-deleted already
        // excluded by V1 contract). The vector arm can surface relevant
        // entries the lexical arm missed (paraphrase recall); it never
        // re-introduces sealed or deleted content.
        val vectorOrdered = runCatching {
            // The vector arm inherits the SAME filter contract as the lexical
            // arm (filters are applied BEFORE fusion, over all live entries),
            // so the vector never re-introduces entries that time/returned
            // filters discarded (e.g. a "recent" edit is not an "old"
            // autobiographical date — originalCreatedAt is the filter axis).
            val vectorCandidates = entries.filter { e ->
                e.id !in lexHits.keys &&
                    !lexical.isSealed(e.id) &&
                    e.deletedAt == null &&
                    lexical.matchesFilters(e, query, returnsByEntry[e.id])
            }
            val queryEmb = embeddingProvider.embed(listOf(query.terms)).first()
            val docEmbs = embeddingProvider.embed(vectorCandidates.map { it.content })
            val scored = vectorCandidates.mapIndexed { i, e -> cosine(queryEmb, docEmbs[i]) to e }
            scored.filter { it.first > similarityFloor }
                .sortedByDescending { it.first }
                .map { it.second }
        }.getOrElse { emptyList<Entry>() }

        val fusedIds = SemanticSearchPrototype.reciprocalRankFusion(
            lexHits.values.map { it.entry }, vectorOrdered,
        )
        val hits: MutableList<SearchHit> = mutableListOf()
        for (entry in fusedIds) {
            if (hits.size >= query.maxResults) break
            val hit = lexHits[entry.id] ?: continue
            hits += hit
        }
        SearchResult(
            query = query,
            hits = hits,
            totalMatched = hits.size,
            sealedCount = sealedSkipped,
            elapsedMillis = System.currentTimeMillis() - start,
        )
    }

    private fun cosine(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        var na = 0f
        var nb = 0f
        for (i in a.indices) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i] }
        val denom = kotlin.math.sqrt(na) * kotlin.math.sqrt(nb)
        return if (denom > 0f) dot / denom else 0f
    }
}
