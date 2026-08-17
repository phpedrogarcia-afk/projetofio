package com.projetofio.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projetofio.app.application.ExportFormat
import com.projetofio.app.application.FioService
import com.projetofio.app.domain.AppLockMode
import com.projetofio.app.domain.AppSettings
import com.projetofio.app.domain.Entry
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
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class FioViewModel(
    private val service: FioService,
) : ViewModel() {
    private val mutableState = MutableStateFlow(FioUiState())
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
    }

    fun onDraftChanged(content: String) {
        mutableState.update { it.copy(draftText = content, savedNotice = false, recoverableError = null) }
        draftChanges.value = content
    }

    /** ADR-014: the save confirmation is restrained — hide it after it was
     *  observed (the UI auto-dismisses after 1.5s). */
    fun acknowledgeSaved() {
        mutableState.update { it.copy(savedNotice = false) }
    }

    fun saveEntry() {
        val content = state.value.draftText
        if (content.isBlank() || !saveInProgress.compareAndSet(false, true)) return
        mutableState.update { it.copy(saving = true, savedNotice = false, recoverableError = null) }
        viewModelScope.launch {
            try {
                service.saveEntry(content)
                draftChanges.value = ""
                mutableState.update { it.copy(draftText = "", saving = false, savedNotice = true) }
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

    class Factory(private val service: FioService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(FioViewModel::class.java))
            return FioViewModel(service) as T
        }
    }
}
