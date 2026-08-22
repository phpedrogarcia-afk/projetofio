package com.projetofio.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projetofio.app.application.ExportFormat
import com.projetofio.app.application.FioService
import com.projetofio.app.application.OpenedReturn
import com.projetofio.app.application.TimeReturnsService
import com.projetofio.app.application.ImportPreview
import com.projetofio.app.application.ImportService
import com.projetofio.app.domain.AppLockMode
import com.projetofio.app.domain.AppSettings
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.NotificationPermissionObserved
import com.projetofio.app.domain.ReturnConsentState
import com.projetofio.app.domain.ReturnPolicy
import com.projetofio.app.domain.ImportBatch
import com.projetofio.app.domain.ImportSource
import com.projetofio.app.domain.SearchQuery
import com.projetofio.app.domain.SearchResult
import com.projetofio.app.domain.SearchService

import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FioUiState(
    val draftText: String = "",
    val entries: List<Entry> = emptyList(),
    val deletedEntries: List<Entry> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val savedNotice: Boolean = false,
    val recoverableError: String? = null,
    val archiveError: Boolean = false,
    val m2EngineeringEnabled: Boolean = false,
    val pendingReturnId: String? = null,
    val openedReturn: OpenedReturn? = null,
    val returnError: String? = null,
    val m3EngineeringEnabled: Boolean = false,
    val importPreview: ImportPreview? = null,
    val importBatches: List<ImportBatch> = emptyList(),
    val importMessage: String? = null,
    val importing: Boolean = false,
    // Search (canonical: Encontrar — search retrieves, it does not interpret):
    // the active query, its evidence, and whether a search is in flight.
    // Nothing here is persisted; queries are pure runtime state.
    val searchTerms: String = "",
    val searchResult: SearchResult? = null,
    val searchLoading: Boolean = false,
    val searchError: String? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class FioViewModel(
    private val service: FioService,
    private val timeReturns: TimeReturnsService,
    private val localImport: ImportService,
    private val m2EngineeringEnabled: Boolean,
    private val m3EngineeringEnabled: Boolean,
    /**
     * Nullable by design: tests that predate the search layer keep working,
     * and the search lens can be removed entirely without touching the rest
     * of the UI layer (SEARCH-ARCHITECTURE.md — the semantic path, should
     * it ever exist, must be removable behind its own flag).
     */
    private val search: SearchService? = null,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        FioUiState(m2EngineeringEnabled = m2EngineeringEnabled, m3EngineeringEnabled = m3EngineeringEnabled),
    )
    private val searchTermsFlow = MutableStateFlow("")
    val state: StateFlow<FioUiState> = mutableState.asStateFlow()
    private val draftChanges = MutableStateFlow("")
    private val saveInProgress = AtomicBoolean(false)

    init {
        viewModelScope.launch {
            runCatching {
                service.purgeExpired()
                val draft = service.loadDraft()
                val settings = service.loadSettings()
                val text = draft?.content.orEmpty()
                draftChanges.value = text
                mutableState.update { it.copy(draftText = text, settings = settings, loading = false) }
                if (m2EngineeringEnabled) {
                    timeReturns.reconcile()
                    refreshPendingReturn()
                }
                if (m3EngineeringEnabled) refreshImportBatches()
            }.onFailure(::showDataFailure)
        }
        viewModelScope.launch {
            service.observeActiveEntries()
                .catch { showDataFailure(it, archive = true) }
                .collect { entries -> mutableState.update { it.copy(entries = entries, archiveError = false) } }
        }
        viewModelScope.launch {
            service.observeDeletedEntries()
                .catch { showDataFailure(it, archive = true) }
                .collect { entries -> mutableState.update { it.copy(deletedEntries = entries) } }
        }
        viewModelScope.launch {
            draftChanges.drop(1).debounce(700).distinctUntilChanged().collect { content ->
                runCatching { service.autosaveDraft(content) }
                    .onFailure { showDataFailure(it) }
            }
        }
        viewModelScope.launch {
            searchTermsFlow.drop(1).debounce(300).distinctUntilChanged().collect { terms ->
                if (search == null) {
                    mutableState.update { it.copy(searchTerms = terms) }
                    return@collect
                }
                mutableState.update { it.copy(searchTerms = terms, searchError = null) }
                if (terms.isBlank()) {
                    mutableState.update { it.copy(searchResult = null, searchLoading = false) }
                    return@collect
                }
                mutableState.update { it.copy(searchLoading = true) }
                runCatching { search.search(SearchQuery(terms = terms.trim())) }
                    .onSuccess { result ->
                        mutableState.update {
                            it.copy(searchResult = result, searchLoading = false, searchError = null)
                        }
                    }
                    .onFailure {
                        mutableState.update {
                            it.copy(
                                searchLoading = false,
                                searchResult = null,
                                searchError = "A busca não pôde ser concluída com segurança. Nada foi apagado.",
                            )
                        }
                    }
            }
        }
    }

    fun onDraftChanged(content: String) {
        mutableState.update { it.copy(draftText = content, savedNotice = false, recoverableError = null) }
        draftChanges.value = content
    }

    /** Search contract: the query is runtime-only — never persisted, logged, or sent anywhere. */
    fun onSearchChanged(terms: String) {
        mutableState.update { it.copy(searchTerms = terms, searchResult = null, searchLoading = terms.isNotBlank(), searchError = null) }
        searchTermsFlow.value = terms
    }

    /** ADR-014: the save confirmation is restrained — hide it after it was
     *  observed (the UI auto-dismisses after 1.5s). */
    fun acknowledgeSaved() {
        mutableState.update { it.copy(savedNotice = false) }
    }

    fun saveEntry(policy: ReturnPolicy = ReturnPolicy.Someday) {
        val content = state.value.draftText
        if (content.isBlank() || !saveInProgress.compareAndSet(false, true)) return
        mutableState.update { it.copy(saving = true, savedNotice = false, recoverableError = null) }
        viewModelScope.launch {
            try {
                service.saveEntry(content, policy)
                draftChanges.value = ""
                mutableState.update { it.copy(draftText = "", saving = false, savedNotice = true) }
                if (m2EngineeringEnabled) {
                    timeReturns.reconcile()
                    refreshPendingReturn()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update {
                    it.copy(
                        saving = false,
                        savedNotice = false,
                        recoverableError = "Não foi possível guardar agora. O texto continua no editor; tente novamente antes de fechar o Fio.",
                    )
                }
            } finally {
                saveInProgress.set(false)
            }
        }
    }


    fun editEntry(id: String, content: String) = launchAction {
        service.editEntry(id, content)
    }

    fun deleteEntry(id: String) = launchAction {
        service.moveToRecentlyDeleted(id)
        if (m2EngineeringEnabled) {
            timeReturns.reconcile()
            refreshPendingReturn()
        }
    }

    fun recoverEntry(id: String) = launchAction {
        service.recoverEntry(id)
    }

    fun permanentlyDelete(id: String) = launchAction {
        service.permanentlyDelete(id)
    }

    fun setAppLockMode(mode: AppLockMode) = launchAction {
        service.setAppLockMode(mode)
        mutableState.update { it.copy(settings = service.loadSettings()) }
    }

    suspend fun buildExport(format: ExportFormat): String = service.export(format)

    fun previewImport(bytes: ByteArray, source: ImportSource, sourceFileName: String?) {
        if (!m3EngineeringEnabled) return
        mutableState.update { it.copy(importing = true, importMessage = null, importPreview = null) }
        viewModelScope.launch {
            runCatching { localImport.preview(bytes, source, sourceFileName) }
                .onSuccess { preview ->
                    mutableState.update { it.copy(importing = false, importPreview = preview) }
                }
                .onFailure {
                    mutableState.update {
                        it.copy(importing = false, importMessage = "Não foi possível preparar esse arquivo. Nada foi importado.")
                    }
                }
        }
    }

    fun importReadFailed() {
        mutableState.update {
            it.copy(importing = false, importPreview = null, importMessage = "Não foi possível abrir esse arquivo. Nada foi importado.")
        }
    }

    fun cancelImportPreview() {
        state.value.importPreview?.let { localImport.cancel(it.id) }
        mutableState.update { it.copy(importPreview = null, importMessage = null) }
    }

    fun commitImport() {
        val preview = state.value.importPreview ?: return
        mutableState.update { it.copy(importing = true, importMessage = null) }
        viewModelScope.launch {
            runCatching { localImport.commit(preview.id) }
                .onSuccess { batch ->
                    mutableState.update {
                        it.copy(
                            importing = false,
                            importPreview = null,
                            importMessage = "${batch.importedCount} entradas importadas.",
                        )
                    }
                    refreshImportBatches()
                    if (m2EngineeringEnabled) refreshPendingReturn()
                }
                .onFailure {
                    mutableState.update {
                        it.copy(importing = false, importMessage = "A importação falhou. O Arquivo não foi alterado.")
                    }
                }
        }
    }

    fun rollbackImport(batchId: String) {
        mutableState.update { it.copy(importing = true, importMessage = null) }
        viewModelScope.launch {
            runCatching { localImport.rollback(batchId) }
                .onSuccess { result ->
                    mutableState.update {
                        it.copy(
                            importing = false,
                            importMessage = "${result.rolledBackEntryIds.size} entradas movidas para Excluídos recentemente; ${result.editedExcludedCount} editadas foram preservadas.",
                        )
                    }
                    refreshImportBatches()
                    if (m2EngineeringEnabled) refreshPendingReturn()
                }
                .onFailure {
                    mutableState.update { it.copy(importing = false, importMessage = "Não foi possível desfazer esse lote. Nada foi apagado.") }
                }
        }
    }

    private suspend fun refreshImportBatches() {
        mutableState.update { it.copy(importBatches = localImport.loadBatches()) }
    }

    fun enableReturns(afterConsentStored: () -> Unit) = launchAction {
        timeReturns.enableReturns()
        mutableState.update { it.copy(settings = service.loadSettings()) }
        refreshPendingReturn()
        afterConsentStored()
    }

    fun pauseReturns() = launchAction {
        timeReturns.pauseReturns()
        mutableState.update { it.copy(settings = service.loadSettings(), pendingReturnId = null) }
    }

    fun resumeReturns() = launchAction {
        timeReturns.resumeReturns()
        mutableState.update { it.copy(settings = service.loadSettings()) }
        refreshPendingReturn()
    }

    fun setQuietHours(startMinute: Int, endMinute: Int) = launchAction {
        timeReturns.setQuietHours(startMinute, endMinute)
        mutableState.update { it.copy(settings = service.loadSettings()) }
        refreshPendingReturn()
    }

    fun observeNotificationPermission(observed: NotificationPermissionObserved) = launchAction {
        timeReturns.observeNotificationPermission(observed)
        mutableState.update { it.copy(settings = service.loadSettings()) }
        timeReturns.reconcile()
        refreshPendingReturn()
    }

    fun reconcileReturns() = launchAction {
        timeReturns.reconcile()
        refreshPendingReturn()
    }

    fun openReturn(id: String) = launchAction {
        val opened = timeReturns.openReturn(id)
        mutableState.update {
            it.copy(
                openedReturn = opened,
                pendingReturnId = if (opened == null) null else it.pendingReturnId,
                returnError = if (opened == null) "Esta devolução não está mais disponível." else null,
            )
        }
    }

    fun closeReturn() = launchAction {
        val opened = state.value.openedReturn ?: return@launchAction
        timeReturns.dismissReturn(opened.attemptId)
        mutableState.update { it.copy(openedReturn = null, pendingReturnId = null, returnError = null) }
        refreshPendingReturn()
    }

    fun neverReturnOpened() = launchAction {
        val opened = state.value.openedReturn ?: return@launchAction
        timeReturns.neverReturn(opened.attemptId)
        mutableState.update { it.copy(openedReturn = null, pendingReturnId = null, returnError = null) }
        refreshPendingReturn()
    }

    private suspend fun refreshPendingReturn() {
        mutableState.update { it.copy(pendingReturnId = timeReturns.pendingReturnId()) }
    }

    private fun launchAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { action() }.onFailure(::showDataFailure)
        }
    }

    private fun showDataFailure(error: Throwable, archive: Boolean = false) {
        if (error is CancellationException) throw error
        mutableState.update {
            it.copy(
                loading = false,
                archiveError = it.archiveError || archive,
                recoverableError = "Os dados locais não puderam ser abertos com segurança. Nada foi apagado.",
            )
        }
    }

    class Factory(
        private val service: FioService,
        private val timeReturns: TimeReturnsService,
        private val localImport: ImportService,
        private val m2EngineeringEnabled: Boolean,
        private val m3EngineeringEnabled: Boolean,
        private val search: SearchService? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(FioViewModel::class.java))
            return FioViewModel(service, timeReturns, localImport, m2EngineeringEnabled, m3EngineeringEnabled, search) as T
        }
    }
}
