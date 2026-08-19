package com.projetofio.app.search

/**
 * PT-BR lexical normalization and tokenization for the Encontrar baseline.
 *
 * Principles (SEARCH-PRIVACY-ARCHITECTURE.md):
 * - Normalization is lossy by design on the *matching* side only: the
 *   returned snippet always comes from the original text, never from a
 *   normalized version. Search shows the user's exact original words.
 * - Case folding, diacritic folding (PT-BR: ã, á, ç, é, í, ó, ú, etc.),
 *   punctuation stripping, and ligature handling.
 * - Tokenization is Unicode-word-boundary based; no stemming in V1 —
 *   stemming discards original word forms (the product keeps the person's
 *   exact words). Prefix-based substring matching inside words is NOT
 *   performed: queries match whole normalized tokens, with the exception
 *   of a leading-truncated last token to support mid-typing queries.
 * - Sealed entries never pass through here; exclusion happens upstream.
 */
object LexicalTokenizer {

    /** Unicode normalization form used for diacritic folding (NFD decomposes accents). */
    private val NFD_FORM = java.text.Normalizer.Form.NFD

    /**
     * Category Mn (Mark, Nonspacing) — covers ALL combining marks after NFD,
     * including cedilla U+0327 which lives outside the 0300–036F block used
     * by \p{InCombiningDiacriticalMarks}.
     */
    private val DIACRITICS_REGEX = Regex("\\p{Mn}")

    /**
     * Fold a piece of text for matching: lowercase, strip diacritics,
     * replace common PT-BR digraph quirks, drop non-alphanumerics.
     */
    fun normalize(text: String): String {
        val decomposed = java.text.Normalizer.normalize(text, NFD_FORM)
        val folded = DIACRITICS_REGEX.replace(decomposed, "")
        val lowered = folded.lowercase()
        return NON_ALNUM_REGEX.replace(lowered, " ").trim()
    }

    /** Tokens of a normalized text, preserving original token positions. */
    fun tokenize(text: String): List<NormalizedToken> {
        val normalized = normalize(text)
        if (normalized.isEmpty()) return emptyList()
        val tokens = mutableListOf<NormalizedToken>()
        var start = 0
        var inToken = false
        for (i in normalized.indices) {
            val ch = normalized[i]
            if (ch != ' ') {
                if (!inToken) { start = i; inToken = true }
            } else if (inToken) {
                tokens += NormalizedToken(normalized.substring(start, i), start)
                inToken = false
            }
        }
        if (inToken) tokens += NormalizedToken(normalized.substring(start), start)
        return tokens
    }

    /**
     * Whether [document] (already normalized) contains all query tokens in
     * the same relative order. The last query token may match only a prefix
     * to keep mid-typing responsive; all earlier tokens must match whole.
     * Returns true when there are no query tokens (empty normalized query).
     */
    fun containsQueryTokens(document: String, queryTokens: List<String>): Boolean {
        if (queryTokens.isEmpty()) return true
        val docTokens = tokenize(document)
        var q = 0
        var lastMatch = -1
        for ((index, token) in docTokens.withIndex()) {
            if (q >= queryTokens.size) break
            val wanted = queryTokens[q]
            val last = q == queryTokens.size - 1
            val matches = if (last) token.text.startsWith(wanted) else token.text == wanted
            if (matches && index > lastMatch) {
                lastMatch = index
                q++
            }
        }
        return q == queryTokens.size
    }

    /**
     * Build a factual snippet around the first matched token span of
     * [normalizedDoc] in the [originalText]. The snippet is always a slice
     * of the original text — never generated, never normalized output.
     */
    fun snippetAround(originalText: String, normalizedDoc: String, queryTokens: List<String>): String {
        if (queryTokens.isEmpty()) return truncated(originalText)
        val docTokens = tokenize(normalizedDoc)
        val first = docTokens.indexOfFirst { token -> token.text.startsWith(queryTokens.first()) }
        if (first < 0) return truncated(originalText)
        // Map the normalized token index back to an original-character offset:
        // both share the same whitespace layout after normalization.
        val targetStart = normalizedCharOffsetToOriginal(normalizedDoc, originalText, docTokens[first].offset)
        val windowStart = maxOf(0, targetStart - SNIPPET_CONTEXT_HALF)
        val windowEnd = minOf(originalText.length, targetStart + queryTokens.sumOf { it.length } + SNIPPET_CONTEXT_HALF)
        val raw = originalText.substring(windowStart, windowEnd)
        val needsLeftEllipsis = windowStart > 0
        val needsRightEllipsis = windowEnd < originalText.length
        return buildString {
            if (needsLeftEllipsis) append("… ")
            append(raw.trim())
            if (needsRightEllipsis) append(" …")
        }
    }

    private fun truncated(text: String): String =
        if (text.length > SNIPPET_MAX_LENGTH) text.take(SNIPPET_MAX_LENGTH).trimEnd() + "…" else text

    /**
     * Approximate the original-text character offset for a normalized offset.
     * Normalization can shrink (multi-byte decomposed glyphs collapse to one
     * code point), so we advance both cursors in lockstep.
     */
    private fun normalizedCharOffsetToOriginal(normalized: String, original: String, target: Int): Int {
        var normIdx = 0
        var origIdx = 0
        while (normIdx < target && origIdx < original.length) {
            val normCh = normalized[normIdx]
            val origChar = original[origIdx]
            val origFolded = normalize(origChar.toString())
            // Skip original code points that folded into nothing or were consumed.
            origIdx++
            if (origFolded.isNotEmpty()) {
                // Advance the normalized cursor for each code point the
                // original character contributed after folding.
                var consumed = 0
                while (consumed < origFolded.length && normIdx < normalized.length) {
                    if (normalized[normIdx] == origFolded[consumed]) consumed++
                    normIdx++
                }
            }
        }
        return minOf(origIdx, original.length)
    }

    private val NON_ALNUM_REGEX = Regex("[^\\p{L}\\p{N}]+")

    private const val SNIPPET_CONTEXT_HALF = 60
    private const val SNIPPET_MAX_LENGTH = 160

    /** A normalized token together with its char offset in the normalized text. */
    data class NormalizedToken(val text: String, val offset: Int)
}
