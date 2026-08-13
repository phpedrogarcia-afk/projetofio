package com.projetofio.app.application

import com.projetofio.app.domain.AppSettings
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.FioRepository
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.domain.NotificationPermissionObserved
import com.projetofio.app.domain.ReturnAttempt
import com.projetofio.app.domain.ReturnCancelReason
import com.projetofio.app.domain.ReturnConsentState
import com.projetofio.app.domain.ReturnMode
import com.projetofio.app.domain.ReturnRepository
import com.projetofio.app.domain.ReturnState
import com.projetofio.app.domain.SilenceReason
import com.projetofio.app.domain.TimeReturnDecision
import com.projetofio.app.domain.TimeReturnEngine
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.first

interface ReturnOpportunityScheduler {
    suspend fun schedule(at: Instant)
    suspend fun cancel()
}

interface ReturnNotificationGateway {
    fun canPostNotifications(): Boolean
    fun createChannel()
    fun post(returnId: String)
    fun cancel(returnId: String)
}

data class OpenedReturn(val attemptId: String, val entry: Entry)

class TimeReturnsService(
    private val entries: FioRepository,
    private val returns: ReturnRepository,
    private val engine: TimeReturnEngine,
    private val scheduler: ReturnOpportunityScheduler,
    private val notifications: ReturnNotificationGateway,
    private val clock: Clock,
    private val ids: IdGenerator,
    private val engineeringEnabled: Boolean,
) : ImportReturnCoordinator {
    override suspend fun reconcile() {
        if (!engineeringEnabled) {
            scheduler.cancel()
            return
        }
        val settings = entries.loadSettings()
        if (settings.returnConsentState != ReturnConsentState.ENABLED) {
            scheduler.cancel()
            return
        }
        val now = clock.instant()
        val history = returns.loadReturnHistory()
        val pending = history.firstOrNull { it.state in PENDING_STATES }
        if (pending != null) {
            reconcilePending(pending, settings, now)
            return
        }
        val activeEntries = entries.observeActiveEntries().first()
        when (val decision = engine.evaluate(now, clock.zone, settings, activeEntries, history)) {
            is TimeReturnDecision.Selected -> {
                val attempt = ReturnAttempt(
                    id = ids.newId(),
                    entryId = decision.entryId,
                    state = ReturnState.SCHEDULED,
                    createdAt = now,
                    windowStart = decision.deliveryAt,
                    windowEnd = decision.windowEnd,
                    scheduledAt = decision.deliveryAt,
                    ageBucket = decision.ageBucket,
                )
                if (returns.insertReturnIfNoPending(attempt)) scheduler.schedule(decision.deliveryAt)
            }
            is TimeReturnDecision.Silent -> scheduleAfterSilence(decision.reason, now, settings, activeEntries, history)
        }
    }

    suspend fun enableReturns() {
        require(engineeringEnabled) { "Time Returns engineering is disabled in this build" }
        val current = entries.loadSettings()
        if (current.returnConsentState == ReturnConsentState.NOT_CONFIGURED) {
            entries.saveSettings(current.copy(returnConsentState = ReturnConsentState.ENABLED, returnsPausedAt = null))
        }
        notifications.createChannel()
        reconcile()
    }

    suspend fun pauseReturns() {
        require(engineeringEnabled)
        val now = clock.instant()
        val pendingIds = returns.loadReturnHistory()
            .filter { it.state in PENDING_STATES }
            .map { it.id }
        entries.saveSettings(entries.loadSettings().copy(returnConsentState = ReturnConsentState.PAUSED, returnsPausedAt = now))
        returns.cancelAllPendingReturns(now.toEpochMilli(), ReturnCancelReason.PAUSED)
        pendingIds.forEach(notifications::cancel)
        scheduler.cancel()
    }

    suspend fun resumeReturns() {
        require(engineeringEnabled)
        val current = entries.loadSettings()
        require(current.returnConsentState == ReturnConsentState.PAUSED)
        entries.saveSettings(current.copy(returnConsentState = ReturnConsentState.ENABLED, returnsPausedAt = null))
        reconcile()
    }

    suspend fun observeNotificationPermission(observed: NotificationPermissionObserved) {
        val current = entries.loadSettings()
        entries.saveSettings(current.copy(notificationPermissionObserved = observed))
    }

    suspend fun setQuietHours(startMinute: Int, endMinute: Int) {
        require(startMinute in 0 until MINUTES_PER_DAY)
        require(endMinute in 0 until MINUTES_PER_DAY)
        val current = entries.loadSettings()
        if (current.quietHoursStartMinute == startMinute && current.quietHoursEndMinute == endMinute) return
        val now = clock.instant()
        val pendingIds = returns.loadReturnHistory()
            .filter { it.state in PENDING_STATES }
            .map { it.id }
        entries.saveSettings(current.copy(quietHoursStartMinute = startMinute, quietHoursEndMinute = endMinute))
        returns.cancelAllPendingReturns(now.toEpochMilli(), ReturnCancelReason.SUPERSEDED)
        pendingIds.forEach(notifications::cancel)
        scheduler.cancel()
        reconcile()
    }

    suspend fun pendingReturnId(): String? = if (!engineeringEnabled) null else
        returns.loadReturnHistory().firstOrNull { it.state in PENDING_STATES }?.id

    override suspend fun cancelExternalReferences(returnIds: List<String>) {
        returnIds.forEach(notifications::cancel)
        if (returnIds.isNotEmpty()) scheduler.cancel()
    }

    suspend fun openReturn(id: String): OpenedReturn? {
        if (!engineeringEnabled) return null
        val now = clock.instant()
        val settings = entries.loadSettings()
        val attempt = returns.findReturn(id) ?: return null
        val entry = entries.findEntry(attempt.entryId)
        if (
            settings.returnConsentState != ReturnConsentState.ENABLED ||
            attempt.state !in PENDING_STATES ||
            attempt.windowEnd <= now ||
            entry == null || entry.deletedAt != null || entry.returnMode != ReturnMode.ELIGIBLE
        ) {
            cancelIneligible(attempt, now)
            return null
        }
        val opened = attempt.copy(state = ReturnState.OPENED, openedAt = now)
        returns.updateReturn(opened)
        entries.updateEntry(entry.copy(lastReturnedAt = now, returnCount = entry.returnCount + 1, updatedAt = now))
        notifications.cancel(id)
        scheduler.cancel()
        return OpenedReturn(id, entry)
    }

    suspend fun dismissReturn(id: String) {
        val attempt = returns.findReturn(id) ?: return
        if (attempt.state != ReturnState.OPENED) return
        returns.updateReturn(attempt.copy(state = ReturnState.DISMISSED, dismissedAt = clock.instant()))
        reconcile()
    }

    suspend fun neverReturn(id: String) {
        val attempt = returns.findReturn(id) ?: return
        val entry = entries.findEntry(attempt.entryId) ?: return
        val now = clock.instant()
        entries.updateEntry(entry.copy(returnMode = ReturnMode.NEVER, updatedAt = now))
        if (attempt.state == ReturnState.OPENED) {
            returns.updateReturn(attempt.copy(state = ReturnState.DISMISSED, dismissedAt = now))
        }
        returns.cancelPendingReturnsForEntry(entry.id, now.toEpochMilli(), ReturnCancelReason.ENTRY_NEVER)
        notifications.cancel(id)
        scheduler.cancel()
        reconcile()
    }

    private suspend fun reconcilePending(attempt: ReturnAttempt, settings: AppSettings, now: Instant) {
        if (attempt.windowEnd <= now) {
            returns.updateReturn(attempt.copy(state = ReturnState.EXPIRED, expiredAt = now))
            notifications.cancel(attempt.id)
            scheduler.cancel()
            reconcile()
            return
        }
        val entry = entries.findEntry(attempt.entryId)
        if (entry == null || entry.deletedAt != null || entry.returnMode != ReturnMode.ELIGIBLE) {
            cancelIneligible(attempt, now)
            reconcile()
            return
        }
        val due = attempt.scheduledAt ?: attempt.windowStart
        if (due > now) {
            scheduler.schedule(due)
            return
        }
        if (attempt.state != ReturnState.NOTIFIED && notifications.canPostNotifications()) {
            notifications.post(attempt.id)
            returns.updateReturn(attempt.copy(state = ReturnState.NOTIFIED, notifiedAt = now))
        }
        // Whether posted or permission-denied, retain one quiet local pending Return until expiry.
        scheduler.schedule(attempt.windowEnd)
    }

    private suspend fun cancelIneligible(attempt: ReturnAttempt, now: Instant) {
        if (attempt.state in PENDING_STATES) {
            returns.updateReturn(
                attempt.copy(
                    state = ReturnState.CANCELLED,
                    cancelledAt = now,
                    cancelReason = ReturnCancelReason.INELIGIBLE,
                ),
            )
        }
        notifications.cancel(attempt.id)
        scheduler.cancel()
    }

    private suspend fun scheduleAfterSilence(
        reason: SilenceReason,
        now: Instant,
        settings: AppSettings,
        activeEntries: List<Entry>,
        history: List<ReturnAttempt>,
    ) {
        val retryAt = when (reason) {
            SilenceReason.BOOTSTRAP_WAIT -> engine.nextBootstrapOpportunity(now, clock.zone, activeEntries, settings)
            SilenceReason.FREQUENCY_CAP -> history
                .filter { it.state != ReturnState.CANCELLED }
                .maxOfOrNull { it.createdAt }
                ?.plus(7, ChronoUnit.DAYS)
            else -> null
        }
        if (retryAt != null && retryAt > now) scheduler.schedule(retryAt) else scheduler.cancel()
    }

    companion object {
        private const val MINUTES_PER_DAY = 24 * 60
        private val PENDING_STATES = setOf(ReturnState.SELECTED, ReturnState.SCHEDULED, ReturnState.NOTIFIED)
    }
}
