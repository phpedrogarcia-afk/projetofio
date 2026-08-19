package com.projetofio.app.domain

import kotlinx.coroutines.flow.Flow

interface FioRepository {
    fun observeActiveEntries(): Flow<List<Entry>>
    fun observeDeletedEntries(): Flow<List<Entry>>
    suspend fun findEntry(id: String): Entry?
    suspend fun loadDraft(): Draft?
    suspend fun saveDraft(draft: Draft)
    suspend fun clearDraft()
    suspend fun insertEntryAndClearDraft(entry: Entry)
    suspend fun updateEntry(entry: Entry)
    suspend fun deleteEntry(id: String, deletedAtMillis: Long, purgeAfterMillis: Long)
    suspend fun recoverEntry(id: String)
    suspend fun purgeEntry(id: String)
    suspend fun purgeExpired(nowMillis: Long): Int
    suspend fun loadSettings(): AppSettings
    suspend fun saveSettings(settings: AppSettings)
}

interface ReturnRepository {
    suspend fun loadReturnHistory(): List<ReturnAttempt>

    /**
     * Return history for one entry, ordered by creation. This is the read-only
     * lens the search UI uses to show factual "já voltou" evidence.
     * Search code paths must never write return history through it.
     */
    suspend fun loadReturnsForEntry(entryId: String): List<ReturnAttempt>

    suspend fun findReturn(id: String): ReturnAttempt?
    suspend fun insertReturnIfNoPending(attempt: ReturnAttempt): Boolean
    suspend fun updateReturn(attempt: ReturnAttempt)
    suspend fun cancelPendingReturnsForEntry(id: String, atMillis: Long, reason: ReturnCancelReason): Int
    suspend fun cancelAllPendingReturns(atMillis: Long, reason: ReturnCancelReason): Int
}

interface ImportRepository {
    suspend fun commitImport(commit: ImportCommit)
    suspend fun loadImportBatches(): List<ImportBatch>
    suspend fun rollbackImport(batchId: String, atMillis: Long, purgeAfterMillis: Long): ImportRollbackResult
}

fun interface IdGenerator {
    fun newId(): String
}
