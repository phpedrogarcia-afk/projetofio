package com.projetofio.app.application

import com.projetofio.app.domain.AppSettings
import com.projetofio.app.domain.Draft
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.FioRepository
import com.projetofio.app.domain.IdGenerator
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FioServiceTest {
    private val now = Instant.parse("2026-08-10T12:30:00Z")
    private val repository = MemoryRepository()
    private var nextId = 0
    private val service = FioService(
        repository,
        Clock.fixed(now, ZoneId.of("America/Sao_Paulo")),
        IdGenerator { "synthetic-${++nextId}" },
    )

    @Test
    fun blankDraftIsNeverPersisted() = runBlocking {
        service.autosaveDraft("texto de teste")
        assertNotNull(repository.draft)
        service.autosaveDraft(" \n ")
        assertNull(repository.draft)
    }

    @Test
    fun savingEntryAndRemovingDraftUsesOneRepositoryBoundary() = runBlocking {
        service.autosaveDraft("conteúdo sintético")
        val entry = service.saveEntry("conteúdo sintético")
        assertEquals("conteúdo sintético", entry.content)
        assertNull(repository.draft)
        assertEquals(1, repository.active.value.size)
        assertEquals(1, repository.transactionCalls)
    }

    @Test
    fun failedEntryTransactionKeepsRecoverableDraft() = runBlocking {
        service.autosaveDraft("conteúdo sintético")
        repository.failTransaction = true
        runCatching { service.saveEntry("conteúdo sintético") }
        assertNotNull(repository.draft)
        assertTrue(repository.active.value.isEmpty())
    }

    @Test
    fun saveBeforeDebounceProtectsExactEditorStateBeforeTransaction() = runBlocking {
        repository.failTransaction = true
        val latest = "texto mais recente — çã\n  espaços finais  "

        runCatching { service.saveEntry(latest) }

        assertEquals(latest, repository.draft?.content)
        assertTrue(repository.active.value.isEmpty())
        assertEquals(1, repository.transactionCalls)
    }

    @Test
    fun recentlyDeletedBoundaryIsThirtyDays() = runBlocking {
        val entry = service.saveEntry("conteúdo sintético")
        service.moveToRecentlyDeleted(entry.id)
        val deleted = repository.deleted.value.single()
        assertEquals(now, deleted.deletedAt)
        assertEquals(now.plusSeconds(30L * 24L * 60L * 60L), deleted.purgeAfter)
    }

    @Test
    fun returnPolicySomedayMapsToEligibleWithoutWindow() = runBlocking {
        val entry = service.saveEntry("someday entry", com.projetofio.app.domain.ReturnPolicy.Someday)
        assertEquals(com.projetofio.app.domain.ReturnMode.ELIGIBLE, entry.returnMode)
        assertNull(entry.requestedWindowStart)
        assertNull(entry.requestedWindowEnd)
    }

    @Test
    fun returnPolicyNeverMapsToNeverWithoutWindow() = runBlocking {
        val entry = service.saveEntry("never entry", com.projetofio.app.domain.ReturnPolicy.Never)
        assertEquals(com.projetofio.app.domain.ReturnMode.NEVER, entry.returnMode)
        assertNull(entry.requestedWindowStart)
        assertNull(entry.requestedWindowEnd)
    }

    @Test
    fun returnPolicyInPeriodMapsToSevenDayWindowFromTargetOffset() = runBlocking {
        val entry = service.saveEntry("in 30 days entry", com.projetofio.app.domain.ReturnPolicy.InPeriod(30))
        assertEquals(com.projetofio.app.domain.ReturnMode.ELIGIBLE, entry.returnMode)
        val expectedStart = now.plus(30, java.time.temporal.ChronoUnit.DAYS)
        val expectedEnd = expectedStart.plus(7, java.time.temporal.ChronoUnit.DAYS)
        assertEquals(expectedStart, entry.requestedWindowStart)
        assertEquals(expectedEnd, entry.requestedWindowEnd)
    }

    @Test
    fun returnPolicyOnDateMapsToSevenDayWindowFromLocalDateStartOfDay() = runBlocking {
        val targetDate = java.time.LocalDate.of(2026, 9, 1)
        val entry = service.saveEntry("on date entry", com.projetofio.app.domain.ReturnPolicy.OnDate(targetDate))
        assertEquals(com.projetofio.app.domain.ReturnMode.ELIGIBLE, entry.returnMode)
        val expectedStart = targetDate.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant()
        val expectedEnd = expectedStart.plus(7, java.time.temporal.ChronoUnit.DAYS)
        assertEquals(expectedStart, entry.requestedWindowStart)
        assertEquals(expectedEnd, entry.requestedWindowEnd)
    }

    private class MemoryRepository : FioRepository {
        val active = MutableStateFlow<List<Entry>>(emptyList())
        val deleted = MutableStateFlow<List<Entry>>(emptyList())
        var draft: Draft? = null
        var settings = AppSettings()
        var failTransaction = false
        var transactionCalls = 0

        override fun observeActiveEntries(): Flow<List<Entry>> = active
        override fun observeDeletedEntries(): Flow<List<Entry>> = deleted
        override suspend fun findEntry(id: String) = (active.value + deleted.value).find { it.id == id }
        override suspend fun loadDraft() = draft
        override suspend fun saveDraft(draft: Draft) { this.draft = draft }
        override suspend fun clearDraft() { draft = null }
        override suspend fun insertEntryAndClearDraft(entry: Entry) {
            transactionCalls++
            if (failTransaction) error("synthetic failure")
            active.value = active.value + entry
            draft = null
        }
        override suspend fun updateEntry(entry: Entry) {
            active.value = active.value.map { if (it.id == entry.id) entry else it }
        }
        override suspend fun deleteEntry(id: String, deletedAtMillis: Long, purgeAfterMillis: Long) {
            val entry = active.value.single { it.id == id }.copy(
                deletedAt = Instant.ofEpochMilli(deletedAtMillis),
                purgeAfter = Instant.ofEpochMilli(purgeAfterMillis),
            )
            active.value = active.value.filterNot { it.id == id }
            deleted.value = deleted.value + entry
        }
        override suspend fun recoverEntry(id: String) {
            val entry = deleted.value.single { it.id == id }.copy(deletedAt = null, purgeAfter = null)
            deleted.value = deleted.value.filterNot { it.id == id }
            active.value = active.value + entry
        }
        override suspend fun purgeEntry(id: String) { deleted.value = deleted.value.filterNot { it.id == id } }
        override suspend fun purgeExpired(nowMillis: Long): Int = 0
        override suspend fun loadSettings() = settings
        override suspend fun saveSettings(settings: AppSettings) { this.settings = settings }
    }
}
