package com.projetofio.app.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "entries",
    indices = [
        Index(value = ["deleted_at", "original_created_at"]),
        Index(value = ["purge_after"]),
    ],
)
data class EntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "original_created_at") val originalCreatedAt: Long,
    @ColumnInfo(name = "original_time_zone") val originalTimeZone: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    val source: String,
    @ColumnInfo(name = "content_envelope", typeAffinity = ColumnInfo.BLOB) val contentEnvelope: ByteArray,
    @ColumnInfo(name = "content_format") val contentFormat: String,
    @ColumnInfo(name = "return_mode") val returnMode: String,
    @ColumnInfo(name = "last_returned_at") val lastReturnedAt: Long? = null,
    @ColumnInfo(name = "return_count", defaultValue = "0") val returnCount: Int = 0,
    @ColumnInfo(name = "import_batch_id") val importBatchId: String? = null,
    @ColumnInfo(name = "import_fingerprint_envelope", typeAffinity = ColumnInfo.BLOB) val importFingerprintEnvelope: ByteArray? = null,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "purge_after") val purgeAfter: Long?,
    // FIO-P19 A1: requested delivery window. NULL for organic (Someday) entries.
    // Schema 4 adds these two columns via additive MIGRATION_3_4.
    @ColumnInfo(name = "requested_window_start") val requestedWindowStart: Long? = null,
    @ColumnInfo(name = "requested_window_end") val requestedWindowEnd: Long? = null,
    @ColumnInfo(name = "schema_version") val schemaVersion: Int,
)



@Entity(tableName = "drafts")
data class DraftEntity(
    val id: String,
    @PrimaryKey @ColumnInfo(name = "singleton_slot") val singletonSlot: Int = SINGLETON_SLOT,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "content_envelope", typeAffinity = ColumnInfo.BLOB) val contentEnvelope: ByteArray,
    @ColumnInfo(name = "content_format") val contentFormat: String,
    @ColumnInfo(name = "schema_version") val schemaVersion: Int,
) {
    companion object { const val SINGLETON_SLOT = 1 }
}

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    @ColumnInfo(name = "return_consent_state") val returnConsentState: String,
    @ColumnInfo(name = "returns_paused_at") val returnsPausedAt: Long?,
    @ColumnInfo(name = "app_lock_mode") val appLockMode: String,
    @ColumnInfo(name = "privacy_cover_enabled") val privacyCoverEnabled: Boolean,
    @ColumnInfo(name = "analytics_enabled") val analyticsEnabled: Boolean,
    @ColumnInfo(name = "quiet_hours_start_minute", defaultValue = "1260") val quietHoursStartMinute: Int = 1260,
    @ColumnInfo(name = "quiet_hours_end_minute", defaultValue = "480") val quietHoursEndMinute: Int = 480,
    @ColumnInfo(name = "notification_permission_observed", defaultValue = "'UNKNOWN'") val notificationPermissionObserved: String = "UNKNOWN",
    @ColumnInfo(name = "schema_version") val schemaVersion: Int,
) {
    companion object { const val SINGLETON_ID = 1 }
}

@Entity(
    tableName = "returns",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["entry_id"]),
        Index(value = ["state", "window_end"]),
        Index(value = ["created_at"]),
    ],
)
data class ReturnEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "entry_id") val entryId: String,
    val algorithm: String,
    @ColumnInfo(name = "algorithm_version") val algorithmVersion: String,
    val state: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "window_start") val windowStart: Long,
    @ColumnInfo(name = "window_end") val windowEnd: Long,
    @ColumnInfo(name = "scheduled_at") val scheduledAt: Long?,
    @ColumnInfo(name = "notified_at") val notifiedAt: Long?,
    @ColumnInfo(name = "opened_at") val openedAt: Long?,
    @ColumnInfo(name = "dismissed_at") val dismissedAt: Long?,
    @ColumnInfo(name = "expired_at") val expiredAt: Long?,
    @ColumnInfo(name = "cancelled_at") val cancelledAt: Long?,
    @ColumnInfo(name = "cancel_reason") val cancelReason: String?,
    @ColumnInfo(name = "age_bucket") val ageBucket: String,
    @ColumnInfo(name = "schema_version") val schemaVersion: Int,
)

@Entity(
    tableName = "import_batches",
    indices = [Index(value = ["committed_at"])],
)
data class ImportBatchEntity(
    @PrimaryKey val id: String,
    val source: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "committed_at") val committedAt: Long,
    val status: String,
    @ColumnInfo(name = "source_file_name_envelope", typeAffinity = ColumnInfo.BLOB) val sourceFileNameEnvelope: ByteArray?,
    @ColumnInfo(name = "parsed_count") val parsedCount: Int,
    @ColumnInfo(name = "imported_count") val importedCount: Int,
    @ColumnInfo(name = "duplicate_count") val duplicateCount: Int,
    @ColumnInfo(name = "failed_count") val failedCount: Int,
    @ColumnInfo(name = "parser_version") val parserVersion: String,
    @ColumnInfo(name = "schema_version") val schemaVersion: Int,
)

@Entity(
    tableName = "import_batch_items",
    foreignKeys = [
        ForeignKey(
            entity = ImportBatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batch_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index(value = ["batch_id"]), Index(value = ["entry_id"])],
)
data class ImportBatchItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "batch_id") val batchId: String,
    @ColumnInfo(name = "source_index") val sourceIndex: Int,
    @ColumnInfo(name = "entry_id") val entryId: String?,
    val status: String,
    @ColumnInfo(name = "imported_updated_at") val importedUpdatedAt: Long,
    @ColumnInfo(name = "schema_version") val schemaVersion: Int,
)
