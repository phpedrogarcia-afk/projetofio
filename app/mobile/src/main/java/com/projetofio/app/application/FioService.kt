package com.projetofio.app.application

import com.projetofio.app.domain.AppLockMode
import com.projetofio.app.domain.AppSettings
import com.projetofio.app.domain.Draft
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.FioRepository
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.domain.RECENTLY_DELETED_RETENTION_DAYS
import com.projetofio.app.domain.ReturnMode
import com.projetofio.app.domain.ReturnPolicy
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first


class FioService(
    private val repository: FioRepository,
    private val clock: Clock,
    private val ids: IdGenerator,
) {
    fun observeActiveEntries(): Flow<List<Entry>> = repository.observeActiveEntries()

    fun observeDeletedEntries(): Flow<List<Entry>> = repository.observeDeletedEntries()

    suspend fun loadDraft(): Draft? = repository.loadDraft()

    suspend fun autosaveDraft(content: String) {
        if (content.isBlank()) {
            repository.clearDraft()
            return
        }
        val previous = repository.loadDraft()
        repository.saveDraft(
            Draft(
                id = previous?.id ?: ids.newId(),
                updatedAt = clock.instant(),
                content = content,
            ),
        )
    }

    suspend fun clearDraft() = repository.clearDraft()

    suspend fun saveEntry(content: String, policy: ReturnPolicy = ReturnPolicy.Someday): Entry {
        require(content.isNotBlank()) { "Blank entries are not persisted" }
        // Close the debounce gap: the exact editor state becomes a recoverable,
        // encrypted Draft before the Entry transaction is attempted.
        autosaveDraft(content)
        val now = clock.instant()
        val zone = clock.zone

        // FIO-P19 A1: map the user's explicit return policy to domain fields.
        val (returnMode, windowStart, windowEnd) = mapPolicy(policy, now, zone)

        val entry = Entry(
            id = ids.newId(),
            createdAt = now,
            originalCreatedAt = now,
            originalTimeZone = zone.id,
            updatedAt = now,
            content = content,
            returnMode = returnMode,
            requestedWindowStart = windowStart,
            requestedWindowEnd = windowEnd,
        )
        repository.insertEntryAndClearDraft(entry)
        return entry
    }

    /**
     * Maps a [ReturnPolicy] chosen at save time to the three domain fields that
     * persist the intent: [ReturnMode], an optional window start, and an optional
     * window end. The 7-day window is a delivery opportunity, never a guarantee.
     */
    private fun mapPolicy(
        policy: ReturnPolicy,
        now: Instant,
        zone: java.time.ZoneId,
    ): Triple<ReturnMode, Instant?, Instant?> = when (policy) {
        is ReturnPolicy.Someday -> Triple(ReturnMode.ELIGIBLE, null, null)
        is ReturnPolicy.Never   -> Triple(ReturnMode.NEVER, null, null)
        is ReturnPolicy.InPeriod -> {
            val start = now.plus(policy.days.toLong(), ChronoUnit.DAYS)
            Triple(ReturnMode.ELIGIBLE, start, start.plus(7, ChronoUnit.DAYS))
        }
        is ReturnPolicy.OnDate -> {
            // Anchor to start-of-day in local time; same UTC-midnight contract as DatePicker.
            val start = policy.date
                .atStartOfDay(zone)
                .toInstant()
            Triple(ReturnMode.ELIGIBLE, start, start.plus(7, ChronoUnit.DAYS))
        }
    }


    suspend fun editEntry(id: String, content: String) {
        require(content.isNotBlank()) { "Blank entries are not persisted" }
        val current = requireNotNull(repository.findEntry(id))
        repository.updateEntry(current.copy(content = content, updatedAt = clock.instant()))
    }

    suspend fun moveToRecentlyDeleted(id: String) {
        val deletedAt = clock.instant()
        val purgeAfter = deletedAt.plus(RECENTLY_DELETED_RETENTION_DAYS, ChronoUnit.DAYS)
        repository.deleteEntry(id, deletedAt.toEpochMilli(), purgeAfter.toEpochMilli())
    }

    suspend fun recoverEntry(id: String) = repository.recoverEntry(id)

    suspend fun permanentlyDelete(id: String) = repository.purgeEntry(id)

    suspend fun purgeExpired(): Int = repository.purgeExpired(clock.millis())

    suspend fun loadSettings(): AppSettings = repository.loadSettings()

    suspend fun setAppLockMode(mode: AppLockMode) {
        repository.saveSettings(repository.loadSettings().copy(appLockMode = mode))
    }

    suspend fun export(format: ExportFormat): String {
        val entries = repositorySnapshotOldestFirst()
        val exportedAt = DateTimeFormatter.ISO_INSTANT.format(clock.instant())
        return when (format) {
            ExportFormat.MARKDOWN -> markdownExport(entries, exportedAt)
            ExportFormat.PLAIN_TEXT -> textExport(entries, exportedAt)
        }
    }

    private suspend fun repositorySnapshotOldestFirst(): List<Entry> =
        repository.observeActiveEntries().first().asReversed()

    private fun markdownExport(entries: List<Entry>, exportedAt: String): String = buildString {
        appendLine("# Fio — exportação local")
        appendLine()
        appendLine("- Formato: fio-export-v1")
        appendLine("- Exportado em: $exportedAt")
        for (entry in entries) {
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## ${displayInstant(entry.originalCreatedAt, entry.originalTimeZone)}")
            appendLine()
            appendLine("ID: `${entry.id}`")
            appendLine("Fuso original: ${entry.originalTimeZone ?: "UTC"}")
            appendLine("Tamanho UTF-8: ${entry.content.toByteArray(Charsets.UTF_8).size} bytes")
            appendLine()
            append(entry.content)
            if (!entry.content.endsWith('\n')) appendLine()
        }
        appendLine()
        appendLine("---")
        appendLine()
        appendLine("**Checksum SHA-256 (do corpo, sem este rodapé):** `${bodyChecksum(entries, exportedAt)}`")
        appendLine("Formato de export definido em docs/export-format.md v1.0 — arquivo projetado para sobreviver ao próprio app.")
    }

    private fun textExport(entries: List<Entry>, exportedAt: String): String = buildString {
        appendLine("FIO — EXPORTAÇÃO LOCAL")
        appendLine("Formato: fio-export-v1")
        appendLine("Exportado em: $exportedAt")
        for (entry in entries) {
            appendLine()
            appendLine("========== FIO: INÍCIO DA ENTRADA ==========")
            appendLine("ID: ${entry.id}")
            appendLine("Data original: ${displayInstant(entry.originalCreatedAt, entry.originalTimeZone)}")
            appendLine("Fuso original: ${entry.originalTimeZone ?: "UTC"}")
            appendLine("Tamanho UTF-8: ${entry.content.toByteArray(Charsets.UTF_8).size} bytes")
            appendLine("Conteúdo:")
            append(entry.content)
            if (!entry.content.endsWith('\n')) appendLine()
            appendLine("========== FIO: FIM DA ENTRADA =============")
        }
        appendLine()
        appendLine("Checksum SHA-256 (do corpo, sem este rodapé): ${bodyChecksum(entries, exportedAt)}")
        appendLine("Formato de export definido em docs/export-format.md v1.0 — arquivo projetado para sobreviver ao próprio app.")
    }

    // Export v1.0 (ADR-046): checksum of the document body, so the file can be
    // verified years later — a durability marker, not a security one.
    private fun bodyChecksum(entries: List<Entry>, exportedAt: String): String = runCatching {
        val body = buildString {
            for (entry in entries) {
                append(displayInstant(entry.originalCreatedAt, entry.originalTimeZone))
                append(entry.content)
            }
            append(exportedAt)
        }
        MessageDigest.getInstance("SHA-256")
            .digest(body.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }.getOrElse { "indisponível" }

    private fun displayInstant(instant: Instant, zoneName: String?): String {
        val zone = runCatching { ZoneId.of(zoneName ?: "UTC") }.getOrDefault(ZoneId.of("UTC"))
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(instant.atZone(zone))
    }
}

enum class ExportFormat(val mimeType: String, val extension: String) {
    MARKDOWN("text/markdown", "md"),
    PLAIN_TEXT("text/plain", "txt"),
}
