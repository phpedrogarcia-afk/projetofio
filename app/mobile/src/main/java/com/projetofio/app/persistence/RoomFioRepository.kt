package com.projetofio.app.persistence

import com.projetofio.app.crypto.ContentCipher
import com.projetofio.app.crypto.RecordKind
import com.projetofio.app.domain.AppLockMode
import com.projetofio.app.domain.AppSettings
import com.projetofio.app.domain.ContentFormat
import com.projetofio.app.domain.Draft
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.EntrySource
import com.projetofio.app.domain.FioRepository
import com.projetofio.app.domain.ImportBatch
import com.projetofio.app.domain.ImportBatchStatus
import com.projetofio.app.domain.ImportCommit
import com.projetofio.app.domain.ImportItemStatus
import com.projetofio.app.domain.ImportRepository
import com.projetofio.app.domain.ImportRollbackResult
import com.projetofio.app.domain.ImportSource
import com.projetofio.app.domain.ReturnConsentState
import com.projetofio.app.domain.NotificationPermissionObserved
import com.projetofio.app.domain.AgeBucket
import com.projetofio.app.domain.ReturnAlgorithm
import com.projetofio.app.domain.ReturnAttempt
import com.projetofio.app.domain.ReturnCancelReason
import com.projetofio.app.domain.ReturnRepository
import com.projetofio.app.domain.ReturnState
import com.projetofio.app.domain.ReturnMode
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomFioRepository(
    private val dao: FioDao,
    private val cipher: ContentCipher,
    private val clock: Clock,
) : FioRepository, ReturnRepository, ImportRepository {
    override fun observeActiveEntries(): Flow<List<Entry>> = dao.observeActiveEntries()
        .map { rows -> rows.map(::decryptEntry) }
        .flowOn(Dispatchers.IO)

    override fun observeDeletedEntries(): Flow<List<Entry>> = dao.observeDeletedEntries()
        .map { rows -> rows.map(::decryptEntry) }
        .flowOn(Dispatchers.IO)

    override suspend fun findEntry(id: String): Entry? = io { dao.findEntry(id)?.let(::decryptEntry) }

    override suspend fun loadDraft(): Draft? = io {
        dao.loadDraft()?.let { row ->
            Draft(
                id = row.id,
                updatedAt = Instant.ofEpochMilli(row.updatedAt),
                content = cipher.open(row.contentEnvelope, RecordKind.DRAFT, row.id, row.schemaVersion),
                contentFormat = ContentFormat.valueOf(row.contentFormat),
                schemaVersion = row.schemaVersion,
            )
        }
    }

    override suspend fun saveDraft(draft: Draft) = io {
        dao.upsertDraft(
            DraftEntity(
                id = draft.id,
                updatedAt = draft.updatedAt.toEpochMilli(),
                contentEnvelope = cipher.seal(draft.content, RecordKind.DRAFT, draft.id, draft.schemaVersion),
                contentFormat = draft.contentFormat.name,
                schemaVersion = draft.schemaVersion,
            ),
        )
    }

    override suspend fun clearDraft() = io { dao.clearDraft() }

    override suspend fun insertEntryAndClearDraft(entry: Entry) = io {
        dao.insertEntryAndClearDraft(encryptEntry(entry))
    }

    override suspend fun updateEntry(entry: Entry) = io {
        check(dao.updateEntry(encryptEntry(entry)) == 1) { "Entry update did not affect exactly one record" }
    }

    override suspend fun deleteEntry(id: String, deletedAtMillis: Long, purgeAfterMillis: Long) = io {
        check(dao.softDelete(id, deletedAtMillis, purgeAfterMillis) == 1) { "Entry is unavailable for deletion" }
    }

    override suspend fun recoverEntry(id: String) = io {
        check(dao.recover(id, clock.millis()) == 1) { "Entry is unavailable for recovery" }
    }

    override suspend fun purgeEntry(id: String) = io {
        check(dao.purge(id) == 1) { "Only a deleted entry can be permanently purged" }
    }

    override suspend fun purgeExpired(nowMillis: Long): Int = io { dao.purgeExpired(nowMillis) }

    override suspend fun loadSettings(): AppSettings = io {
        val existing = dao.loadSettings()
        if (existing != null) return@io existing.toDomain()
        AppSettings().also { dao.upsertSettings(it.toEntity()) }
    }

    override suspend fun saveSettings(settings: AppSettings) = io { dao.upsertSettings(settings.toEntity()) }

    private fun encryptEntry(entry: Entry): EntryEntity = EntryEntity(
        id = entry.id,
        createdAt = entry.createdAt.toEpochMilli(),
        originalCreatedAt = entry.originalCreatedAt.toEpochMilli(),
        originalTimeZone = entry.originalTimeZone,
        updatedAt = entry.updatedAt.toEpochMilli(),
        source = entry.source.name,
        contentEnvelope = cipher.seal(entry.content, RecordKind.ENTRY, entry.id, entry.schemaVersion),
        contentFormat = entry.contentFormat.name,
        returnMode = entry.returnMode.name,
        lastReturnedAt = entry.lastReturnedAt?.toEpochMilli(),
        returnCount = entry.returnCount,
        importBatchId = entry.importBatchId,
        importFingerprintEnvelope = entry.importFingerprint?.let {
            cipher.seal(it, RecordKind.IMPORT_FINGERPRINT, entry.id, entry.schemaVersion)
        },
        deletedAt = entry.deletedAt?.toEpochMilli(),
        purgeAfter = entry.purgeAfter?.toEpochMilli(),
        schemaVersion = entry.schemaVersion,
    )

    private fun decryptEntry(row: EntryEntity): Entry = Entry(
        id = row.id,
        createdAt = Instant.ofEpochMilli(row.createdAt),
        originalCreatedAt = Instant.ofEpochMilli(row.originalCreatedAt),
        originalTimeZone = row.originalTimeZone,
        updatedAt = Instant.ofEpochMilli(row.updatedAt),
        source = EntrySource.valueOf(row.source),
        content = cipher.open(row.contentEnvelope, RecordKind.ENTRY, row.id, row.schemaVersion),
        contentFormat = ContentFormat.valueOf(row.contentFormat),
        returnMode = ReturnMode.valueOf(row.returnMode),
        lastReturnedAt = row.lastReturnedAt?.let(Instant::ofEpochMilli),
        returnCount = row.returnCount,
        importBatchId = row.importBatchId,
        importFingerprint = row.importFingerprintEnvelope?.let {
            cipher.open(it, RecordKind.IMPORT_FINGERPRINT, row.id, row.schemaVersion)
        },
        deletedAt = row.deletedAt?.let(Instant::ofEpochMilli),
        purgeAfter = row.purgeAfter?.let(Instant::ofEpochMilli),
        schemaVersion = row.schemaVersion,
    )

    private fun AppSettingsEntity.toDomain() = AppSettings(
        returnConsentState = ReturnConsentState.valueOf(returnConsentState),
        returnsPausedAt = returnsPausedAt?.let(Instant::ofEpochMilli),
        appLockMode = AppLockMode.valueOf(appLockMode),
        privacyCoverEnabled = privacyCoverEnabled,
        analyticsEnabled = analyticsEnabled,
        quietHoursStartMinute = quietHoursStartMinute,
        quietHoursEndMinute = quietHoursEndMinute,
        notificationPermissionObserved = NotificationPermissionObserved.valueOf(notificationPermissionObserved),
        schemaVersion = schemaVersion,
    )

    private fun AppSettings.toEntity() = AppSettingsEntity(
        returnConsentState = returnConsentState.name,
        returnsPausedAt = returnsPausedAt?.toEpochMilli(),
        appLockMode = appLockMode.name,
        privacyCoverEnabled = privacyCoverEnabled,
        analyticsEnabled = false,
        quietHoursStartMinute = quietHoursStartMinute,
        quietHoursEndMinute = quietHoursEndMinute,
        notificationPermissionObserved = notificationPermissionObserved.name,
        schemaVersion = schemaVersion,
    )

    override suspend fun loadReturnHistory(): List<ReturnAttempt> = io {
        dao.loadReturnHistory().map { it.toDomain() }
    }

    override suspend fun findReturn(id: String): ReturnAttempt? = io {
        dao.findReturn(id)?.toDomain()
    }

    override suspend fun insertReturnIfNoPending(attempt: ReturnAttempt): Boolean = io {
        dao.insertReturnIfNoPending(attempt.toEntity())
    }

    override suspend fun updateReturn(attempt: ReturnAttempt) = io {
        check(dao.updateReturn(attempt.toEntity()) == 1) { "Return update did not affect exactly one record" }
    }

    override suspend fun cancelPendingReturnsForEntry(
        id: String,
        atMillis: Long,
        reason: ReturnCancelReason,
    ): Int = io { dao.cancelPendingReturnsForEntry(id, atMillis, reason.name) }

    override suspend fun cancelAllPendingReturns(
        atMillis: Long,
        reason: ReturnCancelReason,
    ): Int = io { dao.cancelAllPendingReturns(atMillis, reason.name) }

    private fun ReturnAttempt.toEntity() = ReturnEntity(
        id = id,
        entryId = entryId,
        algorithm = algorithm.name,
        algorithmVersion = algorithmVersion,
        state = state.name,
        createdAt = createdAt.toEpochMilli(),
        windowStart = windowStart.toEpochMilli(),
        windowEnd = windowEnd.toEpochMilli(),
        scheduledAt = scheduledAt?.toEpochMilli(),
        notifiedAt = notifiedAt?.toEpochMilli(),
        openedAt = openedAt?.toEpochMilli(),
        dismissedAt = dismissedAt?.toEpochMilli(),
        expiredAt = expiredAt?.toEpochMilli(),
        cancelledAt = cancelledAt?.toEpochMilli(),
        cancelReason = cancelReason?.name,
        ageBucket = ageBucket.name,
        schemaVersion = schemaVersion,
    )

    private fun ReturnEntity.toDomain() = ReturnAttempt(
        id = id,
        entryId = entryId,
        algorithm = ReturnAlgorithm.valueOf(algorithm),
        algorithmVersion = algorithmVersion,
        state = ReturnState.valueOf(state),
        createdAt = Instant.ofEpochMilli(createdAt),
        windowStart = Instant.ofEpochMilli(windowStart),
        windowEnd = Instant.ofEpochMilli(windowEnd),
        scheduledAt = scheduledAt?.let(Instant::ofEpochMilli),
        notifiedAt = notifiedAt?.let(Instant::ofEpochMilli),
        openedAt = openedAt?.let(Instant::ofEpochMilli),
        dismissedAt = dismissedAt?.let(Instant::ofEpochMilli),
        expiredAt = expiredAt?.let(Instant::ofEpochMilli),
        cancelledAt = cancelledAt?.let(Instant::ofEpochMilli),
        cancelReason = cancelReason?.let(ReturnCancelReason::valueOf),
        ageBucket = AgeBucket.valueOf(ageBucket),
        schemaVersion = schemaVersion,
    )

    override suspend fun commitImport(commit: ImportCommit) = io {
        val batch = commit.batch
        val batchEntity = ImportBatchEntity(
            id = batch.id,
            source = batch.source.name,
            startedAt = batch.startedAt.toEpochMilli(),
            committedAt = batch.committedAt.toEpochMilli(),
            status = batch.status.name,
            sourceFileNameEnvelope = batch.sourceFileName?.let {
                cipher.seal(it, RecordKind.IMPORT_SOURCE_FILE, batch.id, batch.schemaVersion)
            },
            parsedCount = batch.parsedCount,
            importedCount = batch.importedCount,
            duplicateCount = batch.duplicateCount,
            failedCount = batch.failedCount,
            parserVersion = batch.parserVersion,
            schemaVersion = batch.schemaVersion,
        )
        val entries = commit.entries.map(::encryptEntry)
        val items = commit.entries.mapIndexed { index, entry ->
            ImportBatchItemEntity(
                id = "${batch.id}:$index",
                batchId = batch.id,
                sourceIndex = index,
                entryId = entry.id,
                status = ImportItemStatus.IMPORTED.name,
                importedUpdatedAt = entry.updatedAt.toEpochMilli(),
                schemaVersion = batch.schemaVersion,
            )
        }
        dao.commitImport(batchEntity, entries, items)
    }

    override suspend fun loadImportBatches(): List<ImportBatch> = io {
        dao.loadImportBatches().map { row ->
            ImportBatch(
                id = row.id,
                source = ImportSource.valueOf(row.source),
                startedAt = Instant.ofEpochMilli(row.startedAt),
                committedAt = Instant.ofEpochMilli(row.committedAt),
                status = ImportBatchStatus.valueOf(row.status),
                sourceFileName = row.sourceFileNameEnvelope?.let {
                    cipher.open(it, RecordKind.IMPORT_SOURCE_FILE, row.id, row.schemaVersion)
                },
                parsedCount = row.parsedCount,
                importedCount = row.importedCount,
                duplicateCount = row.duplicateCount,
                failedCount = row.failedCount,
                parserVersion = row.parserVersion,
                schemaVersion = row.schemaVersion,
            )
        }
    }

    override suspend fun rollbackImport(
        batchId: String,
        atMillis: Long,
        purgeAfterMillis: Long,
    ): ImportRollbackResult = io {
        val result = dao.rollbackImport(batchId, atMillis, purgeAfterMillis)
        ImportRollbackResult(result.entryIds, result.returnIds, result.editedExcludedCount)
    }

    private suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) { block() }
}
