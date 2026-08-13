package com.projetofio.app.application

import android.content.ContentResolver
import android.net.Uri
import com.projetofio.app.domain.CURRENT_RECORD_SCHEMA_VERSION
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.EntrySource
import com.projetofio.app.domain.FioRepository
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.domain.ImportBatch
import com.projetofio.app.domain.ImportBatchStatus
import com.projetofio.app.domain.ImportCandidate
import com.projetofio.app.domain.ImportCommit
import com.projetofio.app.domain.ImportFingerprint
import com.projetofio.app.domain.ImportIssue
import com.projetofio.app.domain.ImportRepository
import com.projetofio.app.domain.ImportRollbackResult
import com.projetofio.app.domain.ImportSource
import com.projetofio.app.domain.LocalImportParser
import com.projetofio.app.domain.RECENTLY_DELETED_RETENTION_DAYS
import java.io.ByteArrayOutputStream
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.first

data class ImportPreviewItem(
    val candidate: ImportCandidate,
    val duplicate: Boolean,
)

data class ImportPreview(
    val id: String,
    val source: ImportSource,
    val sourceFileName: String?,
    val startedAt: Instant,
    val items: List<ImportPreviewItem>,
    val issues: List<ImportIssue>,
    val parserVersion: String,
) {
    val importableCount: Int get() = items.count { !it.duplicate }
    val duplicateCount: Int get() = items.count { it.duplicate }
    val canCommit: Boolean get() = issues.isEmpty() && importableCount > 0
}

class ImportService(
    private val entries: FioRepository,
    private val imports: ImportRepository,
    private val timeReturns: ImportReturnCoordinator,
    private val parser: LocalImportParser,
    private val clock: Clock,
    private val ids: IdGenerator,
    private val engineeringEnabled: Boolean,
) {
    private val staged = ConcurrentHashMap<String, ImportPreview>()

    suspend fun preview(bytes: ByteArray, source: ImportSource, sourceFileName: String?): ImportPreview {
        require(engineeringEnabled) { "Local import engineering is disabled in this build" }
        val parsed = parser.parse(bytes, source)
        val existing = (entries.observeActiveEntries().first() + entries.observeDeletedEntries().first())
            .mapTo(mutableSetOf()) { entry ->
                entry.importFingerprint ?: ImportFingerprint.compute(
                    entry.content,
                    entry.originalCreatedAt,
                    entry.originalTimeZone ?: "Z",
                )
            }
        val seen = existing.toMutableSet()
        val items = parsed.candidates.map { candidate ->
            val duplicate = !seen.add(candidate.fingerprint)
            ImportPreviewItem(candidate, duplicate)
        }
        return ImportPreview(
            id = ids.newId(),
            source = source,
            sourceFileName = sourceFileName?.take(255),
            startedAt = clock.instant(),
            items = items,
            issues = parsed.issues,
            parserVersion = parsed.parserVersion,
        ).also { staged[it.id] = it }
    }

    suspend fun commit(previewId: String): ImportBatch {
        require(engineeringEnabled)
        val preview = requireNotNull(staged[previewId]) { "Import preview is no longer available" }
        require(preview.canCommit) { "Import preview contains blocking errors or no new entries" }
        val committedAt = clock.instant()
        val importable = preview.items.filterNot { it.duplicate }
        val batch = ImportBatch(
            id = preview.id,
            source = preview.source,
            startedAt = preview.startedAt,
            committedAt = committedAt,
            status = ImportBatchStatus.COMMITTED,
            sourceFileName = preview.sourceFileName,
            parsedCount = preview.items.size + preview.issues.size,
            importedCount = importable.size,
            duplicateCount = preview.duplicateCount,
            failedCount = preview.issues.size,
            parserVersion = preview.parserVersion,
        )
        val source = when (preview.source) {
            ImportSource.MARKDOWN -> EntrySource.IMPORT_MARKDOWN
            ImportSource.TEXT -> EntrySource.IMPORT_TEXT
        }
        val importedEntries = importable.map { item ->
            Entry(
                id = ids.newId(),
                createdAt = committedAt,
                originalCreatedAt = item.candidate.originalCreatedAt,
                originalTimeZone = item.candidate.originalTimeZone,
                updatedAt = committedAt,
                source = source,
                content = item.candidate.content,
                importBatchId = batch.id,
                importFingerprint = item.candidate.fingerprint,
            )
        }
        imports.commitImport(ImportCommit(batch, importedEntries))
        staged.remove(previewId)
        timeReturns.reconcile()
        return batch
    }

    fun cancel(previewId: String) { staged.remove(previewId) }

    suspend fun loadBatches(): List<ImportBatch> = if (engineeringEnabled) imports.loadImportBatches() else emptyList()

    suspend fun rollback(batchId: String): ImportRollbackResult {
        require(engineeringEnabled)
        val at = clock.instant()
        val result = imports.rollbackImport(
            batchId,
            at.toEpochMilli(),
            at.plus(RECENTLY_DELETED_RETENTION_DAYS, ChronoUnit.DAYS).toEpochMilli(),
        )
        timeReturns.cancelExternalReferences(result.cancelledReturnIds)
        timeReturns.reconcile()
        return result
    }
}

interface ImportReturnCoordinator {
    suspend fun reconcile()
    suspend fun cancelExternalReferences(returnIds: List<String>)
}

class AndroidImportDocumentReader(private val resolver: ContentResolver) {
    fun read(uri: Uri): ByteArray = resolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Selected document cannot be opened" }
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            output.write(buffer, 0, read)
            if (total > LocalImportParser.MAX_FILE_BYTES) break
        }
        output.toByteArray()
    }
}
