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
import com.projetofio.app.domain.ReturnConsentState
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
) : FioRepository {
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
        schemaVersion = schemaVersion,
    )

    private fun AppSettings.toEntity() = AppSettingsEntity(
        returnConsentState = returnConsentState.name,
        returnsPausedAt = returnsPausedAt?.toEpochMilli(),
        appLockMode = appLockMode.name,
        privacyCoverEnabled = privacyCoverEnabled,
        analyticsEnabled = false,
        schemaVersion = schemaVersion,
    )

    private suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) { block() }
}
