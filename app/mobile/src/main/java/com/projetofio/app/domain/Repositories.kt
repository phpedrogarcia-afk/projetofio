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

fun interface IdGenerator {
    fun newId(): String
}
