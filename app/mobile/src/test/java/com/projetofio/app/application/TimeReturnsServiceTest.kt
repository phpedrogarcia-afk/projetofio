package com.projetofio.app.application

import com.projetofio.app.domain.AppSettings
import com.projetofio.app.domain.Draft
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.FioRepository
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.domain.ReturnAttempt
import com.projetofio.app.domain.ReturnCancelReason
import com.projetofio.app.domain.ReturnConsentState
import com.projetofio.app.domain.ReturnMode
import com.projetofio.app.domain.ReturnRandom
import com.projetofio.app.domain.ReturnRepository
import com.projetofio.app.domain.ReturnState
import com.projetofio.app.domain.TimeReturnEngine
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeReturnsServiceTest {
    private val now = Instant.parse("2026-08-13T15:00:00Z")
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun disabledBuildCannotActivateAndCancelsWork() = runBlocking {
        val fixture = fixture(enabled = false)
        assertTrue(runCatching { fixture.service.enableReturns() }.isFailure)
        fixture.service.reconcile()
        assertEquals(1, fixture.scheduler.cancelCalls)
        assertTrue(fixture.repository.history.isEmpty())
    }

    @Test
    fun explicitConsentCreatesOneIdempotentPendingReturn() = runBlocking {
        val fixture = fixture()
        fixture.repository.active.value = listOf(oldEntry())

        fixture.service.enableReturns()
        fixture.service.reconcile()
        fixture.service.reconcile()

        assertEquals(ReturnConsentState.ENABLED, fixture.repository.settings.returnConsentState)
        assertEquals(1, fixture.repository.history.size)
        assertEquals(ReturnState.NOTIFIED, fixture.repository.history.single().state)
        assertEquals(listOf(fixture.repository.history.single().id), fixture.notifications.posted)
        assertNotNull(fixture.service.pendingReturnId())
    }

    @Test
    fun deniedNotificationKeepsOneQuietLocalPendingReturn() = runBlocking {
        val fixture = fixture(canNotify = false)
        fixture.repository.active.value = listOf(oldEntry())

        fixture.service.enableReturns()
        fixture.service.reconcile()

        assertEquals(ReturnState.SCHEDULED, fixture.repository.history.single().state)
        assertTrue(fixture.notifications.posted.isEmpty())
        assertNotNull(fixture.service.pendingReturnId())
    }

    @Test
    fun openedReturnCanBecomeNeverAndCannotBeSelectedAgain() = runBlocking {
        val fixture = fixture()
        fixture.repository.active.value = listOf(oldEntry())
        fixture.service.enableReturns()
        fixture.service.reconcile()
        val id = checkNotNull(fixture.service.pendingReturnId())

        val opened = fixture.service.openReturn(id)
        assertNotNull(opened)
        fixture.service.neverReturn(id)

        assertEquals(ReturnMode.NEVER, fixture.repository.active.value.single().returnMode)
        assertEquals(1, fixture.repository.active.value.single().returnCount)
        assertEquals(ReturnState.DISMISSED, fixture.repository.history.single().state)
        assertNull(fixture.service.pendingReturnId())
        assertTrue(id in fixture.notifications.cancelled)
    }

    @Test
    fun pauseCancelsPendingAndCoreEntriesRemainAvailable() = runBlocking {
        val fixture = fixture(canNotify = false)
        fixture.repository.active.value = listOf(oldEntry())
        fixture.service.enableReturns()
        fixture.service.pauseReturns()

        assertEquals(ReturnConsentState.PAUSED, fixture.repository.settings.returnConsentState)
        assertEquals(ReturnState.CANCELLED, fixture.repository.history.single().state)
        assertEquals("texto sintético antigo", fixture.repository.active.value.single().content)
        assertTrue(fixture.repository.history.single().id in fixture.notifications.cancelled)
        assertNull(fixture.service.pendingReturnId())
    }

    @Test
    fun quietHourChangeSupersedesPendingAndCancelsItsNotification() = runBlocking {
        val fixture = fixture(canNotify = false)
        fixture.repository.active.value = listOf(oldEntry())
        fixture.service.enableReturns()
        val oldId = checkNotNull(fixture.service.pendingReturnId())

        fixture.service.setQuietHours(startMinute = 22 * 60, endMinute = 9 * 60)

        val superseded = fixture.repository.history.first { it.id == oldId }
        assertEquals(ReturnState.CANCELLED, superseded.state)
        assertEquals(ReturnCancelReason.SUPERSEDED, superseded.cancelReason)
        assertTrue(oldId in fixture.notifications.cancelled)
    }

    private fun fixture(enabled: Boolean = true, canNotify: Boolean = true): Fixture {
        val repository = MemoryRepositories()
        val scheduler = RecordingScheduler()
        val notifications = RecordingNotifications(canNotify)
        var nextId = 0
        val service = TimeReturnsService(
            entries = repository,
            returns = repository,
            engine = TimeReturnEngine(ReturnRandom { 0 }),
            scheduler = scheduler,
            notifications = notifications,
            clock = Clock.fixed(now, zone),
            ids = IdGenerator { "synthetic-return-${++nextId}" },
            engineeringEnabled = enabled,
        )
        return Fixture(service, repository, scheduler, notifications)
    }

    private fun oldEntry() = Entry(
        id = "synthetic-old-entry",
        createdAt = now.minusSeconds(40L * 86_400),
        originalCreatedAt = now.minusSeconds(40L * 86_400),
        originalTimeZone = zone.id,
        updatedAt = now.minusSeconds(40L * 86_400),
        content = "texto sintético antigo",
    )

    private data class Fixture(
        val service: TimeReturnsService,
        val repository: MemoryRepositories,
        val scheduler: RecordingScheduler,
        val notifications: RecordingNotifications,
    )

    private class RecordingScheduler : ReturnOpportunityScheduler {
        val scheduled = mutableListOf<Instant>()
        var cancelCalls = 0
        override suspend fun schedule(at: Instant) { scheduled += at }
        override suspend fun cancel() { cancelCalls++ }
    }

    private class RecordingNotifications(private val allowed: Boolean) : ReturnNotificationGateway {
        val posted = mutableListOf<String>()
        val cancelled = mutableListOf<String>()
        override fun canPostNotifications() = allowed
        override fun createChannel() = Unit
        override fun post(returnId: String) { posted += returnId }
        override fun cancel(returnId: String) { cancelled += returnId }
    }

    private class MemoryRepositories : FioRepository, ReturnRepository {
        val active = MutableStateFlow<List<Entry>>(emptyList())
        private val deleted = MutableStateFlow<List<Entry>>(emptyList())
        val history = mutableListOf<ReturnAttempt>()
        var settings = AppSettings()

        override fun observeActiveEntries(): Flow<List<Entry>> = active
        override fun observeDeletedEntries(): Flow<List<Entry>> = deleted
        override suspend fun findEntry(id: String) = (active.value + deleted.value).find { it.id == id }
        override suspend fun loadDraft(): Draft? = null
        override suspend fun saveDraft(draft: Draft) = Unit
        override suspend fun clearDraft() = Unit
        override suspend fun insertEntryAndClearDraft(entry: Entry) { active.value += entry }
        override suspend fun updateEntry(entry: Entry) {
            active.value = active.value.map { if (it.id == entry.id) entry else it }
        }
        override suspend fun deleteEntry(id: String, deletedAtMillis: Long, purgeAfterMillis: Long) = Unit
        override suspend fun recoverEntry(id: String) = Unit
        override suspend fun purgeEntry(id: String) = Unit
        override suspend fun purgeExpired(nowMillis: Long) = 0
        override suspend fun loadSettings() = settings
        override suspend fun saveSettings(settings: AppSettings) { this.settings = settings }
        override suspend fun loadReturnHistory() = history.toList()
        override suspend fun loadReturnsForEntry(entryId: String) = history.filter { it.entryId == entryId }
        override suspend fun findReturn(id: String) = history.find { it.id == id }
        override suspend fun insertReturnIfNoPending(attempt: ReturnAttempt): Boolean {
            if (history.any { it.state in setOf(ReturnState.SELECTED, ReturnState.SCHEDULED, ReturnState.NOTIFIED) }) return false
            history += attempt
            return true
        }
        override suspend fun updateReturn(attempt: ReturnAttempt) {
            val index = history.indexOfFirst { it.id == attempt.id }
            check(index >= 0)
            history[index] = attempt
        }
        override suspend fun cancelPendingReturnsForEntry(id: String, atMillis: Long, reason: ReturnCancelReason): Int =
            cancelMatching(atMillis, reason) { it.entryId == id }
        override suspend fun cancelAllPendingReturns(atMillis: Long, reason: ReturnCancelReason): Int =
            cancelMatching(atMillis, reason) { true }

        private fun cancelMatching(atMillis: Long, reason: ReturnCancelReason, matches: (ReturnAttempt) -> Boolean): Int {
            var count = 0
            history.indices.forEach { index ->
                val item = history[index]
                if (matches(item) && item.state in setOf(ReturnState.SELECTED, ReturnState.SCHEDULED, ReturnState.NOTIFIED)) {
                    history[index] = item.copy(
                        state = ReturnState.CANCELLED,
                        cancelledAt = Instant.ofEpochMilli(atMillis),
                        cancelReason = reason,
                    )
                    count++
                }
            }
            return count
        }
    }
}
