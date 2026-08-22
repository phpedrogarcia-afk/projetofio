package com.projetofio.app.domain

import java.time.Instant
import java.time.LocalDate

const val CURRENT_RECORD_SCHEMA_VERSION = 4
const val RECENTLY_DELETED_RETENTION_DAYS = 30L

/**
 * The user's explicit return intent expressed at save time (FIO-P19 A1).
 * Lives in the domain so the engine and service can reason about it without
 * depending on UI classes.
 */
sealed class ReturnPolicy {
    /** Organic eligibility — the engine picks the moment. */
    data object Someday : ReturnPolicy()
    /** Requested window starting N days from now (7-day delivery window). */
    data class InPeriod(val days: Int) : ReturnPolicy()
    /** Requested window anchored to a specific calendar date (7-day delivery window). */
    data class OnDate(val date: LocalDate) : ReturnPolicy()
    /** Never return this entry. */
    data object Never : ReturnPolicy()
}

enum class ContentFormat { PLAIN_TEXT }

enum class EntrySource { NATIVE, IMPORT_MARKDOWN, IMPORT_TEXT }

enum class ImportSource { MARKDOWN, TEXT }

enum class ImportBatchStatus { COMMITTED, ROLLED_BACK }

enum class ImportItemStatus { IMPORTED, ROLLED_BACK, EDITED_EXCLUDED }

enum class ReturnMode { ELIGIBLE, NEVER }

enum class ReturnConsentState { NOT_CONFIGURED, ENABLED, PAUSED }

enum class NotificationPermissionObserved { UNKNOWN, GRANTED, DENIED }

enum class ReturnAlgorithm { TIME }

enum class ReturnState { SELECTED, SCHEDULED, NOTIFIED, OPENED, DISMISSED, EXPIRED, CANCELLED }

enum class ReturnCancelReason { PAUSED, ENTRY_DELETED, ENTRY_NEVER, SUPERSEDED, INELIGIBLE, IMPORT_ROLLBACK }

enum class AgeBucket {
    DAYS_7_29,
    DAYS_30_89,
    DAYS_90_179,
    DAYS_180_364,
    DAYS_365_729,
    DAYS_730_PLUS,
}

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
    val lastReturnedAt: Instant? = null,
    val returnCount: Int = 0,
    val importBatchId: String? = null,
    val importFingerprint: String? = null,
    val deletedAt: Instant? = null,
    val purgeAfter: Instant? = null,
    // FIO-P19 A1: requested delivery window. Both are null for organic entries.
    // When set, the engine treats this entry as a priority candidate within the window.
    // If the window expires without a safe delivery, both fields are cleared and the
    // entry returns to the organic ELIGIBLE pool — nothing is delivered by force.
    val requestedWindowStart: Instant? = null,
    val requestedWindowEnd: Instant? = null,
    val schemaVersion: Int = CURRENT_RECORD_SCHEMA_VERSION,
) {
    init {
        require(id.isNotBlank())
        require(content.isNotBlank())
        require((deletedAt == null) == (purgeAfter == null))
        require(returnCount >= 0)
        require((source == EntrySource.NATIVE) == (importBatchId == null && importFingerprint == null))
        require((requestedWindowStart == null) == (requestedWindowEnd == null)) {
            "requestedWindowStart and requestedWindowEnd must both be null or both be set"
        }
        if (requestedWindowStart != null && requestedWindowEnd != null) {
            require(requestedWindowStart < requestedWindowEnd) {
                "requestedWindowStart must be before requestedWindowEnd"
            }
        }
    }
}


data class ImportBatch(
    val id: String,
    val source: ImportSource,
    val startedAt: Instant,
    val committedAt: Instant,
    val status: ImportBatchStatus,
    val sourceFileName: String?,
    val parsedCount: Int,
    val importedCount: Int,
    val duplicateCount: Int,
    val failedCount: Int,
    val parserVersion: String,
    val schemaVersion: Int = CURRENT_RECORD_SCHEMA_VERSION,
)

data class ImportCommit(
    val batch: ImportBatch,
    val entries: List<Entry>,
)

data class ImportRollbackResult(
    val rolledBackEntryIds: List<String>,
    val cancelledReturnIds: List<String>,
    val editedExcludedCount: Int,
)

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
    val quietHoursStartMinute: Int = 21 * 60,
    val quietHoursEndMinute: Int = 8 * 60,
    val notificationPermissionObserved: NotificationPermissionObserved = NotificationPermissionObserved.UNKNOWN,
    val schemaVersion: Int = CURRENT_RECORD_SCHEMA_VERSION,
) {
    init {
        require(!analyticsEnabled) { "M1 does not enable analytics" }
        require(returnConsentState != ReturnConsentState.PAUSED || returnsPausedAt != null)
        require(quietHoursStartMinute in 0 until 24 * 60)
        require(quietHoursEndMinute in 0 until 24 * 60)
        require(quietHoursStartMinute != quietHoursEndMinute)
    }
}

data class ReturnAttempt(
    val id: String,
    val entryId: String,
    val algorithm: ReturnAlgorithm = ReturnAlgorithm.TIME,
    val algorithmVersion: String = "time-v1",
    val state: ReturnState,
    val createdAt: Instant,
    val windowStart: Instant,
    val windowEnd: Instant,
    val scheduledAt: Instant? = null,
    val notifiedAt: Instant? = null,
    val openedAt: Instant? = null,
    val dismissedAt: Instant? = null,
    val expiredAt: Instant? = null,
    val cancelledAt: Instant? = null,
    val cancelReason: ReturnCancelReason? = null,
    val ageBucket: AgeBucket,
    val schemaVersion: Int = CURRENT_RECORD_SCHEMA_VERSION,
) {
    init {
        require(id.isNotBlank())
        require(entryId.isNotBlank())
        require(windowStart < windowEnd)
        require((cancelledAt == null) == (cancelReason == null))
    }
}

// ---------- Search (canonical: Encontrar — search retrieves, it does not interpret)

enum class SearchMode { LEXICAL }

enum class SearchScope { ALL, EXCLUDE_SEALED, SEALED_ONLY }

/** Time window filter over the entry's original (autobiographical) date. */
data class SearchTimeFilter(
    val after: Instant? = null,
    val before: Instant? = null,
) {
    init {
        if (after != null && before != null) require(before >= after) { "Invalid time filter range" }
    }
}

enum class ReturnedFilter { ANY, EVER_RETURNED, NEVER_RETURNED }

data class SearchQuery(
    val terms: String,
    val mode: SearchMode = SearchMode.LEXICAL,
    val scope: SearchScope = SearchScope.ALL,
    val time: SearchTimeFilter = SearchTimeFilter(),
    val returned: ReturnedFilter = ReturnedFilter.ANY,
    val maxResults: Int = DEFAULT_MAX_SEARCH_RESULTS,
) {
    init {
        require(terms.isNotBlank()) { "Search terms must not be blank" }
        require(maxResults in 1..MAX_SEARCH_RESULTS_CAP) { "maxResults out of range" }
    }

    companion object {
        const val DEFAULT_MAX_SEARCH_RESULTS = 50
        const val MAX_SEARCH_RESULTS_CAP = 200
    }
}

/**
 * Evidence returned by search: the entry's original words plus factual
 * context (matched snippet, dates, return evidence). Search never returns
 * generated text, interpretations, or summaries of the person's life.
 */
data class SearchHit(
    val entry: Entry,
    val matchedSnippet: String,
    val matchedAtOriginalDate: Instant,
    val returnedCount: Int,
    val lastReturnedAt: Instant?,
)

data class SearchResult(
    val query: SearchQuery,
    val hits: List<SearchHit>,
    val totalMatched: Int,
    val sealedCount: Int,
    val elapsedMillis: Long,
)

/**
 * How sealed entries interact with search. V1 keeps sealed notes invisible
 * to content search (SEARCH-SEALED-THREAT-MODEL.md): COUNT_ONLY exposes an
 * opaque match count and nothing else; HIDDEN exposes nothing.
 */
enum class SealedSearchBehavior { HIDDEN, COUNT_ONLY }
