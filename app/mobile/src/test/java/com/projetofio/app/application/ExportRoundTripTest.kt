package com.projetofio.app.application

import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.FioRepository
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.domain.ReturnMode
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Campaign 8 — Export Torture + Round-Trip (ADR-046).
 *
 * The checksum on the exported document is a durability marker, not a
 * cryptographic one. This suite verifies that the exported checksum is
 * reproducible by an independent SHA-256 implementation over the documented
 * body construction (displayed instant + raw content + exported-at ISO
 * instant, UTF-8), that any body change flips the checksum, and that hostile
 * content survives the export faithfully.
 */
class ExportRoundTripTest {

    private val exportedAt = "2026-08-16T12:00:00Z"
    private val now = Instant.parse(exportedAt)
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val ids = IdGenerator { "synthetic" }

    private fun repository(entries: List<Entry>) = object : FioRepository by NoOpRepository() {
        override fun observeActiveEntries(): Flow<List<Entry>> =
            MutableStateFlow(entries)
    }

    private fun entry(
        id: String,
        secondsAgo: Long,
        zone: String = "America/Sao_Paulo",
        content: String = "conteúdo de $id",
    ) = Entry(
        id = id,
        createdAt = now.minusSeconds(secondsAgo),
        originalCreatedAt = now.minusSeconds(secondsAgo),
        originalTimeZone = zone,
        updatedAt = now.minusSeconds(secondsAgo),
        content = content,
        returnMode = ReturnMode.ELIGIBLE,
    )

    private fun runExport(entries: List<Entry>, format: ExportFormat): String =
        runBlocking {
            FioService(repository(entries), clock, ids).export(format)
        }

    /** Independent reference implementation of the documented body construction. */
    private fun referenceBodyChecksum(entries: List<Entry>, exportedAt: String): String {
        // The export walks the snapshot oldest-first (asReversed), matching the
        // document order used when the checksum was computed.
        val body = entries.sortedBy { it.originalCreatedAt }.joinToString("") { e ->
            val z = runCatching { ZoneId.of(e.originalTimeZone ?: "UTC") }
                .getOrDefault(ZoneId.of("UTC"))
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(e.originalCreatedAt.atZone(z)) + e.content
        } + exportedAt
        return MessageDigest.getInstance("SHA-256")
            .digest(body.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun markdownChecksum(export: String): String =
        export.lines().first { it.contains("Checksum SHA-256") }
            .substringAfter("**Checksum SHA-256 (do corpo, sem este rodapé):** `").substringBefore("`")

    @Test
    fun markdownChecksumMatchesIndependentImplementation() {
        val entries = listOf(
            entry("e1", 86400),
            entry("e2", 2 * 86400, content = "segunda entrada\ncom quebras de linha"),
            entry("e3", 3 * 86400, content = "emoji 🧵✓ e acentos çã"),
        )
        assertEquals(referenceBodyChecksum(entries, exportedAt), markdownChecksum(runExport(entries, ExportFormat.MARKDOWN)))
    }

    @Test
    fun plaintextChecksumMatchesIndependentImplementation() {
        val entries = listOf(entry("p1", 86400), entry("p2", 48 * 3600))
        val export = runExport(entries, ExportFormat.PLAIN_TEXT)
        val checksum = export.lines().first { it.startsWith("Checksum SHA-256") }
            .substringAfter("Checksum SHA-256 (do corpo, sem este rodapé): ").trim()
        assertEquals(referenceBodyChecksum(entries, exportedAt), checksum)
    }

    @Test
    fun oneByteChangeFlipsTheChecksum() {
        val a = listOf(entry("x", 86400, content = "exato"))
        val b = listOf(entry("x", 86400, content = "exata"))
        assertNotEquals(markdownChecksum(runExport(a, ExportFormat.MARKDOWN)), markdownChecksum(runExport(b, ExportFormat.MARKDOWN)))
    }

    @Test
    fun exportedAtInstantIsPartOfTheBody() {
        val entries = listOf(entry("e", 86400))
        val later = Clock.fixed(Instant.parse("2026-08-16T12:00:01Z"), ZoneOffset.UTC)
        val other = runBlocking { FioService(repository(entries), later, ids).export(ExportFormat.MARKDOWN) }
        assertNotEquals(markdownChecksum(runExport(entries, ExportFormat.MARKDOWN)), markdownChecksum(other))
    }

    @Test
    fun hostileContentSurvivesExportRoundTrip() {
        // Campaign 1 payloads re-appear inside the export body, intact.
        val payloads = listOf(
            "a\uD83D\uDCA0b", // valid surrogate pair (astral)
            "c\u200B\u200Ed", // zero-width + bidi mark
            "e\u00A0f",       // non-breaking space
            "g\uD83D",        // isolated surrogate half (P0 finding)
        )
        val entries = payloads.mapIndexed { i, content -> entry("h$i", i * 86400L, content = content) }
        val export = runExport(entries, ExportFormat.MARKDOWN)
        for (payload in payloads) {
            assertTrue("payload missing in export: $payload", export.contains(payload))
        }
        // The surrogate-half payload round-trips byte-for-byte into the body,
        // and the checksum still covers it exactly (no silent replacement).
        assertEquals(referenceBodyChecksum(entries, exportedAt), markdownChecksum(export))
    }

    @Test
    fun unknownTimeZoneFallsBackToUtcAndStillChecksums() {
        val entries = listOf(entry("z", 86400, zone = "Marte/Olympus"))
        val export = runExport(entries, ExportFormat.MARKDOWN)
        assertTrue(export.contains("Fuso original: Marte/Olympus"))
        assertEquals(referenceBodyChecksum(entries, exportedAt), markdownChecksum(export))
    }

    @Test
    fun emptyJournalProducesValidExport() {
        val export = runExport(emptyList(), ExportFormat.MARKDOWN)
        assertEquals(referenceBodyChecksum(emptyList(), exportedAt), markdownChecksum(export))
        assertFalse(export.contains("## "))
    }
}

/** Minimal no-op repository for seeding active entries only. */
private open class NoOpRepository : FioRepository {
    override fun observeActiveEntries(): Flow<List<Entry>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override fun observeDeletedEntries(): Flow<List<Entry>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override suspend fun findEntry(id: String): Entry? = null
    override suspend fun loadDraft(): com.projetofio.app.domain.Draft? = null
    override suspend fun saveDraft(draft: com.projetofio.app.domain.Draft) = Unit
    override suspend fun clearDraft() = Unit
    override suspend fun insertEntryAndClearDraft(entry: Entry) = Unit
    override suspend fun updateEntry(entry: Entry) = Unit
    override suspend fun deleteEntry(id: String, deletedAtMillis: Long, purgeAfterMillis: Long) = Unit
    override suspend fun recoverEntry(id: String) = Unit
    override suspend fun purgeEntry(id: String) = Unit
    override suspend fun purgeExpired(nowMillis: Long): Int = 0
    override suspend fun loadSettings(): com.projetofio.app.domain.AppSettings =
        com.projetofio.app.domain.AppSettings()
    override suspend fun saveSettings(settings: com.projetofio.app.domain.AppSettings) = Unit
}
