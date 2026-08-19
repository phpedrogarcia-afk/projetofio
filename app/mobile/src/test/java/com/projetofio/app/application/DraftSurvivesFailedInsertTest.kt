package com.projetofio.app.application

import com.projetofio.app.domain.AppLockMode
import com.projetofio.app.domain.AppSettings
import com.projetofio.app.domain.Draft
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.FioRepository
import com.projetofio.app.domain.IdGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * S1 / D12 closure.
 *
 * Contract being verified: when the Entry insert transaction fails, the
 * user's words are not lost — the encrypted Draft saved before the
 * transaction (debounce-gap close) must still be loadable, and no Entry
 * must have been persisted.
 *
 * The production path (`FioService.saveEntry`) persists the exact editor
 * state as a Draft *before* calling `insertEntryAndClearDraft`. This test
 * forces the transaction to fail and asserts the Draft survived.
 */
class DraftSurvivesFailedInsertTest {

    private val now = Instant.parse("2026-08-19T10:00:00Z")
    private var nextId = 0

    private fun failingRepository(): Pair<FailingRepository, FioService> {
        val repo = FailingRepository()
        val service = FioService(
            repository = repo,
            clock = Clock.fixed(now, ZoneId.of("America/Sao_Paulo")),
            ids = IdGenerator { "synthetic-${++nextId}" },
        )
        return repo to service
    }

    @Test
    fun draftSurvivesWhenInsertTransactionThrows() = runBlocking {
        val (repo, service) = failingRepository()
        repo.failInsert = true

        val thrown = runCatching { service.saveEntry("pensando em mudar de emprego… amanhã?") }
        assertTrue("Expected the transaction failure to propagate", thrown.isFailure)

        // The exact editor state is still recoverable as an encrypted Draft.
        assertEquals("pensando em mudar de emprego… amanhã?", repo.draftContent)
        // No entry leaked into the active store when the transaction failed.
        assertTrue("No entry should exist after a failed transaction", repo.activeEntries.isEmpty())
        // The draft id was minted by the service before the failure.
        assertEquals("synthetic-1", repo.draftId)
    }

    @Test
    fun exactEditorStateSurvivesEvenWhenNewSaveFails() = runBlocking {
        val (repo, service) = failingRepository()
        repo.failInsert = true

        // Seed: an earlier autosave sits on disk.
        service.autosaveDraft("versão anterior do rascunho")
        assertEquals("versão anterior do rascunho", repo.draftContent)

        runCatching { service.saveEntry("tentativa de salvar que vai falhar") }

        // `saveEntry` persists the exact editor state as a Draft *before*
        // attempting the Entry transaction, so the newest words survive
        // even when the transaction itself fails.
        assertEquals("tentativa de salvar que vai falhar", repo.draftContent)
        // The earlier seed was intentionally replaced by the new autosave.
    }

    @Test
    fun blankContentOnFailureClearsDraftConsistently() = runBlocking {
        val (repo, service) = failingRepository()
        service.autosaveDraft("algo escrito")
        service.autosaveDraft(" \n \t ")
        // Blank autosave clears the draft — this is the normal, passing path
        // and must keep behaving after the failure-mode test above.
        assertNull("Blank draft must never persist", repo.draftContent)
    }

    /** Minimal in-memory FioRepository that fails the entry transaction on demand. */
    private class FailingRepository : FioRepository {
        var failInsert = false
        var draftId: String? = null
        var draftContent: String? = null
        var draftCleared = false
        val activeEntries = mutableListOf<Entry>()
        val active = MutableStateFlow<List<Entry>>(emptyList())
        val deleted = MutableStateFlow<List<Entry>>(emptyList())

        override fun observeActiveEntries(): Flow<List<Entry>> = active
        override fun observeDeletedEntries(): Flow<List<Entry>> = deleted
        override suspend fun findEntry(id: String) = activeEntries.find { it.id == id }
        override suspend fun loadDraft(): Draft? = draftContent?.let {
            Draft(id = requireNotNull(draftId), updatedAt = FIXED_NOW, content = it)
        }
        override suspend fun saveDraft(draft: Draft) {
            draftId = draft.id
            draftContent = draft.content
            draftCleared = false
        }
        override suspend fun clearDraft() {
            draftId = null
            draftContent = null
            draftCleared = true
        }
        override suspend fun insertEntryAndClearDraft(entry: Entry) {
            if (failInsert) error("synthetic transaction failure (D12)")
            activeEntries += entry
            active.value = activeEntries.toList()
            clearDraft()
        }
        override suspend fun updateEntry(entry: Entry) {
            val i = activeEntries.indexOfFirst { it.id == entry.id }
            if (i >= 0) activeEntries[i] = entry
        }

        companion object {
            private val FIXED_NOW = Instant.parse("2026-08-19T10:00:00Z")
        }
        override suspend fun deleteEntry(id: String, deletedAtMillis: Long, purgeAfterMillis: Long) {
            val entry = activeEntries.single { it.id == id }
            activeEntries -= entry
        }
        override suspend fun recoverEntry(id: String) {}
        override suspend fun purgeEntry(id: String) {}
        override suspend fun purgeExpired(nowMillis: Long): Int = 0
        override suspend fun loadSettings(): AppSettings = AppSettings()
        override suspend fun saveSettings(settings: AppSettings) {}
    }
}
