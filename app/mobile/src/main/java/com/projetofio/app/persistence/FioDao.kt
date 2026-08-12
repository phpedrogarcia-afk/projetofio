package com.projetofio.app.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FioDao {
    @Query("SELECT * FROM entries WHERE deleted_at IS NULL ORDER BY original_created_at DESC, id DESC")
    abstract fun observeActiveEntries(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC, id DESC")
    abstract fun observeDeletedEntries(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    abstract suspend fun findEntry(id: String): EntryEntity?

    @Query("SELECT * FROM drafts WHERE singleton_slot = 1 LIMIT 1")
    abstract suspend fun loadDraft(): DraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertDraft(draft: DraftEntity)

    @Query("DELETE FROM drafts")
    abstract suspend fun clearDraft()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertEntry(entry: EntryEntity)

    @Transaction
    open suspend fun insertEntryAndClearDraft(entry: EntryEntity) {
        insertEntry(entry)
        clearDraft()
    }

    @Update
    abstract suspend fun updateEntry(entry: EntryEntity): Int

    @Query("UPDATE entries SET deleted_at = :deletedAt, purge_after = :purgeAfter, updated_at = :deletedAt WHERE id = :id AND deleted_at IS NULL")
    abstract suspend fun softDelete(id: String, deletedAt: Long, purgeAfter: Long): Int

    @Query("UPDATE entries SET deleted_at = NULL, purge_after = NULL, updated_at = :recoveredAt WHERE id = :id AND deleted_at IS NOT NULL")
    abstract suspend fun recover(id: String, recoveredAt: Long): Int

    @Query("DELETE FROM entries WHERE id = :id AND deleted_at IS NOT NULL")
    abstract suspend fun purge(id: String): Int

    @Query("DELETE FROM entries WHERE deleted_at IS NOT NULL AND purge_after <= :now")
    abstract suspend fun purgeExpired(now: Long): Int

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    abstract suspend fun loadSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertSettings(settings: AppSettingsEntity)

    @Query("SELECT (SELECT COUNT(*) FROM entries) + (SELECT COUNT(*) FROM drafts)")
    abstract fun encryptedRecordCount(): Int
}
