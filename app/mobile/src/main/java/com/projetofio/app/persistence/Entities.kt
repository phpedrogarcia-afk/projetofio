package com.projetofio.app.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "purge_after") val purgeAfter: Long?,
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
    @ColumnInfo(name = "schema_version") val schemaVersion: Int,
) {
    companion object { const val SINGLETON_ID = 1 }
}
