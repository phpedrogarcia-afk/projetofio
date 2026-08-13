package com.projetofio.app.application

import com.projetofio.app.domain.AppSettings
import com.projetofio.app.domain.Draft
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.FioRepository
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.domain.ImportBatch
import com.projetofio.app.domain.ImportCommit
import com.projetofio.app.domain.ImportRepository
import com.projetofio.app.domain.ImportRollbackResult
import com.projetofio.app.domain.ImportSource
import com.projetofio.app.domain.LocalImportParser
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportServiceTest {
    private val now = Instant.parse("2026-08-13T15:00:00Z")

    @Test
    fun previewDeduplicatesActiveDeletedAndSameBatchExactly() = runBlocking {
        val repository = MemoryImportRepository()
        repository.active.value = listOf(entry("active", "igual", "2020-01-01T10:00:00Z"))
        repository.deleted.value = listOf(entry("deleted", "apagada", "2020-01-02T10:00:00Z"))
        val fixture = fixture(repository)

        val preview = fixture.service.preview(
            genericDocument(
                "igual" to "2020-01-01T10:00:00Z",
                "apagada" to "2020-01-02T10:00:00Z",
                "nova" to "2020-01-03T10:00:00Z",
                "nova" to "2020-01-03T10:00:00Z",
                "igual" to "2020-01-04T10:00:00Z",
            ),
            ImportSource.TEXT,
            "synthetic.txt",
        )

        assertEquals(2, preview.importableCount)
        assertEquals(3, preview.duplicateCount)
        assertTrue(preview.canCommit)
    }

    @Test
    fun commitIsOneBatchAndPreservesExactWordsAndDates() = runBlocking {
        val repository = MemoryImportRepository()
        val fixture = fixture(repository)
        val preview = fixture.service.preview(
            genericDocument("palavras 🌿\n" to "2020-01-01T10:00:00-03:00"),
            ImportSource.MARKDOWN,
            "history.md",
        )

        val batch = fixture.service.commit(preview.id)

        val imported = repository.commits.single().entries.single()
        assertEquals("palavras 🌿\n", imported.content)
        assertEquals(Instant.parse("2020-01-01T13:00:00Z"), imported.originalCreatedAt)
        assertEquals(batch.id, imported.importBatchId)
        assertEquals(1, batch.importedCount)
        assertEquals(1, fixture.returns.reconcileCalls)
    }

    @Test
    fun repositoryFailureLeavesCanonicalEntriesUnchangedAndPreviewRetryable() = runBlocking {
        val repository = MemoryImportRepository(failCommit = true)
        val fixture = fixture(repository)
        val preview = fixture.service.preview(genericDocument("nova" to "2020-01-01T10:00:00Z"), ImportSource.TEXT, null)

        assertTrue(runCatching { fixture.service.commit(preview.id) }.isFailure)
        assertTrue(repository.active.value.isEmpty())
        assertTrue(runCatching { fixture.service.commit(preview.id) }.isFailure)
    }

    @Test
    fun rollbackCancelsOnlyReportedReturnReferences() = runBlocking {
        val repository = MemoryImportRepository()
        val fixture = fixture(repository)
        repository.rollbackResult = ImportRollbackResult(listOf("imported"), listOf("opaque-return"), 1)

        val result = fixture.service.rollback("batch")

        assertEquals(1, result.editedExcludedCount)
        assertEquals(listOf("opaque-return"), fixture.returns.cancelled)
        assertEquals(1, fixture.returns.reconcileCalls)
    }

    @Test
    fun disabledBuildCannotReadOrCommitImport() = runBlocking {
        val service = fixture(MemoryImportRepository(), enabled = false).service
        assertFalse(runCatching {
            service.preview(genericDocument("x" to "2020-01-01T10:00:00Z"), ImportSource.TEXT, null)
        }.isSuccess)
        assertTrue(service.loadBatches().isEmpty())
    }

    @Test
    fun currentFioExportRoundTripsNamedZoneAndIsRecognizedAsDuplicate() = runBlocking {
        val repository = MemoryImportRepository()
        val original = Entry(
            id = "original",
            createdAt = Instant.parse("2020-03-08T04:30:00Z"),
            originalCreatedAt = Instant.parse("2020-03-08T04:30:00Z"),
            originalTimeZone = "America/Sao_Paulo",
            updatedAt = Instant.parse("2020-03-08T04:30:00Z"),
            content = "palavras exatas 🌿",
        )
        repository.active.value = listOf(original)
        val export = FioService(repository, Clock.fixed(now, ZoneOffset.UTC), IdGenerator { "unused" })
            .export(ExportFormat.PLAIN_TEXT)
        val preview = fixture(repository).service.preview(export.toByteArray(), ImportSource.TEXT, "fio-export.txt")

        assertEquals("America/Sao_Paulo", preview.items.single().candidate.originalTimeZone)
        assertEquals(original.content, preview.items.single().candidate.content)
        assertTrue(preview.items.single().duplicate)
    }

    private fun fixture(repository: MemoryImportRepository, enabled: Boolean = true): Fixture {
        var sequence = 0
        val returns = RecordingReturns()
        val service = ImportService(
            entries = repository,
            imports = repository,
            timeReturns = returns,
            parser = LocalImportParser(),
            clock = Clock.fixed(now, ZoneOffset.UTC),
            ids = IdGenerator { "id-${++sequence}" },
            engineeringEnabled = enabled,
        )
        return Fixture(service, returns)
    }

    private fun genericDocument(vararg values: Pair<String, String>): ByteArray = buildString {
        values.forEach { (content, date) ->
            appendLine("--- FIO ENTRY ---")
            appendLine("Date: $date")
            appendLine("Bytes: ${content.toByteArray().size}")
            appendLine()
            append(content)
            if (!content.endsWith('\n')) appendLine()
            appendLine("--- FIO END ---")
        }
    }.toByteArray()

    private fun entry(id: String, content: String, date: String): Entry {
        val instant = Instant.parse(date)
        return Entry(id, instant, instant, "Z", instant, content = content)
    }

    private data class Fixture(val service: ImportService, val returns: RecordingReturns)

    private class RecordingReturns : ImportReturnCoordinator {
        var reconcileCalls = 0
        var cancelled = emptyList<String>()
        override suspend fun reconcile() { reconcileCalls++ }
        override suspend fun cancelExternalReferences(returnIds: List<String>) { cancelled = returnIds }
    }

    private class MemoryImportRepository(private val failCommit: Boolean = false) : FioRepository, ImportRepository {
        val active = MutableStateFlow<List<Entry>>(emptyList())
        val deleted = MutableStateFlow<List<Entry>>(emptyList())
        val commits = mutableListOf<ImportCommit>()
        var rollbackResult = ImportRollbackResult(emptyList(), emptyList(), 0)
        override fun observeActiveEntries(): Flow<List<Entry>> = active
        override fun observeDeletedEntries(): Flow<List<Entry>> = deleted
        override suspend fun findEntry(id: String) = (active.value + deleted.value).find { it.id == id }
        override suspend fun loadDraft(): Draft? = null
        override suspend fun saveDraft(draft: Draft) = Unit
        override suspend fun clearDraft() = Unit
        override suspend fun insertEntryAndClearDraft(entry: Entry) = Unit
        override suspend fun updateEntry(entry: Entry) = Unit
        override suspend fun deleteEntry(id: String, deletedAtMillis: Long, purgeAfterMillis: Long) = Unit
        override suspend fun recoverEntry(id: String) = Unit
        override suspend fun purgeEntry(id: String) = Unit
        override suspend fun purgeExpired(nowMillis: Long) = 0
        override suspend fun loadSettings() = AppSettings()
        override suspend fun saveSettings(settings: AppSettings) = Unit
        override suspend fun commitImport(commit: ImportCommit) {
            if (failCommit) error("synthetic full database")
            commits += commit
            active.value += commit.entries
        }
        override suspend fun loadImportBatches(): List<ImportBatch> = commits.map { it.batch }
        override suspend fun rollbackImport(batchId: String, atMillis: Long, purgeAfterMillis: Long) = rollbackResult
    }
}
