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

    @Query("SELECT * FROM returns ORDER BY created_at ASC, id ASC")
    abstract suspend fun loadReturnHistory(): List<ReturnEntity>

    @Query("SELECT * FROM returns WHERE id = :id LIMIT 1")
    abstract suspend fun findReturn(id: String): ReturnEntity?

    @Query("SELECT COUNT(*) FROM returns WHERE state IN ('SELECTED', 'SCHEDULED', 'NOTIFIED')")
    protected abstract suspend fun pendingReturnCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertReturn(attempt: ReturnEntity)

    @Transaction
    open suspend fun insertReturnIfNoPending(attempt: ReturnEntity): Boolean {
        if (pendingReturnCount() != 0) return false
        insertReturn(attempt)
        return true
    }

    @Update
    abstract suspend fun updateReturn(attempt: ReturnEntity): Int

    @Query("UPDATE returns SET state = 'CANCELLED', cancelled_at = :at, cancel_reason = :reason WHERE entry_id = :entryId AND state IN ('SELECTED', 'SCHEDULED', 'NOTIFIED')")
    abstract suspend fun cancelPendingReturnsForEntry(entryId: String, at: Long, reason: String): Int

    @Query("UPDATE returns SET state = 'CANCELLED', cancelled_at = :at, cancel_reason = :reason WHERE state IN ('SELECTED', 'SCHEDULED', 'NOTIFIED')")
    abstract suspend fun cancelAllPendingReturns(at: Long, reason: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertImportBatch(batch: ImportBatchEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertImportedEntries(entries: List<EntryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertImportItems(items: List<ImportBatchItemEntity>)

    @Transaction
    open suspend fun commitImport(
        batch: ImportBatchEntity,
        entries: List<EntryEntity>,
        items: List<ImportBatchItemEntity>,
    ) {
        insertImportBatch(batch)
        insertImportedEntries(entries)
        insertImportItems(items)
    }

    @Query("SELECT * FROM import_batches ORDER BY committed_at DESC, id DESC")
    abstract suspend fun loadImportBatches(): List<ImportBatchEntity>

    @Query("SELECT * FROM import_batches WHERE id = :id LIMIT 1")
    protected abstract suspend fun findImportBatch(id: String): ImportBatchEntity?

    @Query("SELECT * FROM import_batch_items WHERE batch_id = :batchId ORDER BY source_index ASC")
    protected abstract suspend fun loadImportItems(batchId: String): List<ImportBatchItemEntity>

    @Query("UPDATE import_batch_items SET status = :status WHERE id = :id")
    protected abstract suspend fun updateImportItemStatus(id: String, status: String): Int

    @Query("UPDATE import_batches SET status = 'ROLLED_BACK' WHERE id = :id AND status = 'COMMITTED'")
    protected abstract suspend fun markImportBatchRolledBack(id: String): Int

    @Query("SELECT id FROM returns WHERE entry_id = :entryId AND state IN ('SELECTED', 'SCHEDULED', 'NOTIFIED')")
    protected abstract suspend fun pendingReturnIdsForEntry(entryId: String): List<String>

    data class RollbackRows(
        val entryIds: List<String>,
        val returnIds: List<String>,
        val editedExcludedCount: Int,
    )

    @Transaction
    open suspend fun rollbackImport(batchId: String, at: Long, purgeAfter: Long): RollbackRows {
        val batch = requireNotNull(findImportBatch(batchId)) { "Import batch not found" }
        require(batch.status == "COMMITTED") { "Only a committed batch can be rolled back" }
        val rolledBack = mutableListOf<String>()
        val returnIds = mutableListOf<String>()
        var edited = 0
        loadImportItems(batchId).forEach { item ->
            val entryId = item.entryId
            val entry = entryId?.let { findEntry(it) }
            if (entry != null && entry.deletedAt == null && entry.updatedAt == item.importedUpdatedAt) {
                returnIds += pendingReturnIdsForEntry(entry.id)
                cancelPendingReturnsForEntry(entry.id, at, "IMPORT_ROLLBACK")
                check(softDelete(entry.id, at, purgeAfter) == 1)
                check(updateImportItemStatus(item.id, "ROLLED_BACK") == 1)
                rolledBack += entry.id
            } else {
                check(updateImportItemStatus(item.id, "EDITED_EXCLUDED") == 1)
                edited++
            }
        }
        check(markImportBatchRolledBack(batchId) == 1)
        return RollbackRows(rolledBack, returnIds.distinct(), edited)
    }

    @Query("SELECT (SELECT COUNT(*) FROM entries) + (SELECT COUNT(*) FROM drafts) + (SELECT COUNT(*) FROM import_batches WHERE source_file_name_envelope IS NOT NULL)")
    abstract fun encryptedRecordCount(): Int
}
