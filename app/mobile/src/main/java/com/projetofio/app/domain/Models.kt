package com.projetofio.app.domain

import java.time.Instant

const val CURRENT_RECORD_SCHEMA_VERSION = 1
const val RECENTLY_DELETED_RETENTION_DAYS = 30L

enum class ContentFormat { PLAIN_TEXT }

enum class EntrySource { NATIVE }

enum class ReturnMode { ELIGIBLE, NEVER }

enum class ReturnConsentState { NOT_CONFIGURED, ENABLED, PAUSED }

enum class AppLockMode { OFF, IMMEDIATE, ONE_MINUTE, FIVE_MINUTES }

data class Entry(
    val id: String,
    val createdAt: Instant,
    val originalCreatedAt: Instant,
    val originalTimeZone: String?,
    val updatedAt: Instant,
    val source: EntrySource = EntrySource.NATIVE,
    val content: String,
    val contentFormat: ContentFormat = ContentFormat.PLAIN_TEXT,
    val returnMode: ReturnMode = ReturnMode.ELIGIBLE,
    val deletedAt: Instant? = null,
    val purgeAfter: Instant? = null,
    val schemaVersion: Int = CURRENT_RECORD_SCHEMA_VERSION,
) {
    init {
        require(id.isNotBlank())
        require(content.isNotBlank())
        require((deletedAt == null) == (purgeAfter == null))
    }
}

data class Draft(
    val id: String,
    val updatedAt: Instant,
    val content: String,
    val contentFormat: ContentFormat = ContentFormat.PLAIN_TEXT,
    val schemaVersion: Int = CURRENT_RECORD_SCHEMA_VERSION,
) {
    init {
        require(id.isNotBlank())
        require(content.isNotBlank())
    }
}

data class AppSettings(
    val returnConsentState: ReturnConsentState = ReturnConsentState.NOT_CONFIGURED,
    val returnsPausedAt: Instant? = null,
    val appLockMode: AppLockMode = AppLockMode.OFF,
    val privacyCoverEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val schemaVersion: Int = CURRENT_RECORD_SCHEMA_VERSION,
) {
    init {
        require(!analyticsEnabled) { "M1 does not enable analytics" }
        require(returnConsentState != ReturnConsentState.PAUSED || returnsPausedAt != null)
    }
}
