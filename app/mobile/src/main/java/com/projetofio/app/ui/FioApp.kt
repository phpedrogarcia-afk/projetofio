package com.projetofio.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projetofio.app.domain.AppLockMode
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.ImportBatch
import com.projetofio.app.domain.ImportIssueCode
import com.projetofio.app.domain.NotificationPermissionObserved
import com.projetofio.app.domain.ReturnConsentState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private enum class MainSurface(val label: String) {
    WRITE("Escrever"),
    ARCHIVE("Arquivo"),
    SETTINGS("Configurações"),
}

@Composable
fun FioApp(
    viewModel: FioViewModel,
    authorizeFresh: (() -> Unit) -> Unit,
    authenticationAvailable: Boolean,
    exportMessage: String?,
    onExportMarkdown: () -> Unit,
    onExportText: () -> Unit,
    onEnableReturns: () -> Unit,
    onOpenPendingReturn: (String) -> Unit,
    onSelectImport: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var surface by remember { mutableStateOf(MainSurface.WRITE) }

    state.openedReturn?.let { opened ->
        ReturnScreen(
            entry = opened.entry,
            onClose = viewModel::closeReturn,
            onNeverReturn = viewModel::neverReturnOpened,
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainSurface.entries.forEach { item ->
                    NavigationBarItem(
                        selected = surface == item,
                        onClick = { surface = item },
                        icon = {},
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        when (surface) {
            MainSurface.WRITE -> WriteScreen(state, viewModel, padding)
            MainSurface.ARCHIVE -> ArchiveScreen(state, viewModel, padding)
            MainSurface.SETTINGS -> SettingsScreen(
                state = state,
                viewModel = viewModel,
                padding = padding,
                authenticationAvailable = authenticationAvailable,
                exportMessage = exportMessage,
                authorizeFresh = authorizeFresh,
                onExportMarkdown = onExportMarkdown,
                onExportText = onExportText,
                onEnableReturns = onEnableReturns,
                onOpenPendingReturn = onOpenPendingReturn,
                onSelectImport = onSelectImport,
            )
        }
    }
}

@Composable
private fun WriteScreen(state: FioUiState, viewModel: FioViewModel, padding: PaddingValues) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Text(
            text = "Fio",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = "O que está passando pela sua cabeça hoje?",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 28.dp),
        )
        state.recoverableError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        if (state.savedNotice) {
            Text(
                text = "Guardado.",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        OutlinedTextField(
            value = state.draftText,
            onValueChange = viewModel::onDraftChanged,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 18.dp),
            placeholder = { Text("Escreva quando quiser.") },
            enabled = !state.saving,
        )
        Button(
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                viewModel.saveEntry()
            },
            enabled = state.draftText.isNotBlank() && !state.saving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .heightIn(min = 48.dp),
        ) {
            Text(if (state.saving) "Guardando…" else "Guardar")
        }
    }
}

@Composable
private fun ArchiveScreen(
    state: FioUiState,
    viewModel: FioViewModel,
    padding: PaddingValues,
) {
    var editing by remember { mutableStateOf<Entry?>(null) }
    var deleting by remember { mutableStateOf<Entry?>(null) }
    if (editing != null) {
        EditEntryDialog(
            entry = checkNotNull(editing),
            onDismiss = { editing = null },
            onSave = { content ->
                viewModel.editEntry(checkNotNull(editing).id, content)
                editing = null
            },
        )
    }
    deleting?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Mover para Excluídos recentemente?") },
            text = { Text("Você poderá recuperar esta entrada por 30 dias.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry.id)
                    deleting = null
                }) { Text("Mover") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Arquivo", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        }
        if (state.loading) item { CircularProgressIndicator() }
        if (state.archiveError) {
            item { Text("O Arquivo não pôde ser aberto com segurança. Nada foi apagado.", color = MaterialTheme.colorScheme.error) }
        } else if (!state.loading && state.entries.isEmpty()) {
            item { Text("Quando quiser, suas palavras podem ficar aqui.") }
        }
        items(state.entries, key = { it.id }) { entry ->
            EntryCard(entry, onEdit = { editing = entry }, onDelete = { deleting = entry })
        }
    }
}

@Composable
private fun EntryCard(entry: Entry, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = displayDate(entry),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            SelectionContainer {
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 10.dp),
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Excluir") }
            }
        }
    }
}

@Composable
private fun EditEntryDialog(entry: Entry, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember(entry.id) { mutableStateOf(entry.content) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar entrada") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
            )
        },
        confirmButton = { TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) { Text("Guardar alterações") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun SettingsScreen(
    state: FioUiState,
    viewModel: FioViewModel,
    padding: PaddingValues,
    authenticationAvailable: Boolean,
    exportMessage: String?,
    authorizeFresh: (() -> Unit) -> Unit,
    onExportMarkdown: () -> Unit,
    onExportText: () -> Unit,
    onEnableReturns: () -> Unit,
    onOpenPendingReturn: (String) -> Unit,
    onSelectImport: () -> Unit,
) {
    var permanentDelete by remember { mutableStateOf<Entry?>(null) }
    var confirmReturnConsent by remember { mutableStateOf(false) }
    var rollbackBatch by remember { mutableStateOf<ImportBatch?>(null) }
    if (confirmReturnConsent) {
        AlertDialog(
            onDismissRequest = { confirmReturnConsent = false },
            title = { Text("Ativar devoluções?") },
            text = {
                Text(
                    "O Fio poderá, de vez em quando, mostrar uma notificação discreta quando uma entrada antiga estiver disponível. " +
                        "A notificação nunca mostra o texto da entrada. Você poderá pausar quando quiser.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmReturnConsent = false
                    onEnableReturns()
                }) { Text("Ativar devoluções") }
            },
            dismissButton = { TextButton(onClick = { confirmReturnConsent = false }) { Text("Agora não") } },
        )
    }
    permanentDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { permanentDelete = null },
            title = { Text("Excluir permanentemente?") },
            text = { Text("Esta ação não pode ser desfeita. O Fio não promete apagamento físico seguro do armazenamento.") },
            confirmButton = {
                TextButton(onClick = {
                    authorizeFresh { viewModel.permanentlyDelete(entry.id) }
                    permanentDelete = null
                }) { Text("Excluir permanentemente") }
            },
            dismissButton = { TextButton(onClick = { permanentDelete = null }) { Text("Cancelar") } },
        )
    }
    rollbackBatch?.let { batch ->
        AlertDialog(
            onDismissRequest = { rollbackBatch = null },
            title = { Text("Desfazer este lote?") },
            text = { Text("Entradas importadas e não editadas irão para Excluídos recentemente. Entradas editadas depois da importação serão preservadas.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rollbackImport(batch.id)
                    rollbackBatch = null
                }) { Text("Desfazer lote") }
            },
            dismissButton = { TextButton(onClick = { rollbackBatch = null }) { Text("Cancelar") } },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Configurações", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() }) }
        item { SectionTitle("Privacidade") }
        item { Text("O conteúdo é ocultado nas capturas e na tela de aplicativos recentes. O bloqueio é opcional e usa a proteção do seu aparelho.") }
        item {
            if (!authenticationAvailable) {
                Text("Configure uma tela de bloqueio compatível no Android para usar o bloqueio do Fio.")
            }
            AppLockChoices(
                selected = state.settings.appLockMode,
                enabled = authenticationAvailable,
                onChoose = { mode -> authorizeFresh { viewModel.setAppLockMode(mode) } },
            )
        }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        if (state.m2EngineeringEnabled) {
            item { SectionTitle("Devoluções — validação") }
            item {
                Text("Recurso isolado para testes sintéticos. Nenhuma palavra é colocada na notificação.")
            }
            when (state.settings.returnConsentState) {
                ReturnConsentState.NOT_CONFIGURED -> item {
                    OutlinedButton(
                        onClick = { confirmReturnConsent = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text("Conhecer e ativar") }
                }
                ReturnConsentState.ENABLED -> {
                    item {
                        OutlinedButton(
                            onClick = viewModel::pauseReturns,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) { Text("Pausar devoluções") }
                    }
                    item { Text("Horário silencioso atual: ${quietHoursLabel(state.settings.quietHoursStartMinute, state.settings.quietHoursEndMinute)}") }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.setQuietHours(21 * 60, 8 * 60) }) { Text("21h–8h") }
                            OutlinedButton(onClick = { viewModel.setQuietHours(22 * 60, 9 * 60) }) { Text("22h–9h") }
                        }
                    }
                }
                ReturnConsentState.PAUSED -> item {
                    OutlinedButton(
                        onClick = viewModel::resumeReturns,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text("Retomar devoluções") }
                }
            }
            if (state.settings.notificationPermissionObserved == NotificationPermissionObserved.DENIED) {
                item { Text("Notificações estão desativadas. O Fio continuará funcionando normalmente e não pedirá novamente aqui.") }
            }
            state.pendingReturnId?.let { id ->
                item {
                    OutlinedButton(
                        onClick = { onOpenPendingReturn(id) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text("Abrir devolução disponível") }
                }
            }
            state.returnError?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        }
        if (state.m3EngineeringEnabled) {
            item { SectionTitle("Importar — validação") }
            item { Text("Escolha um TXT ou Markdown UTF-8 com blocos e datas explícitos. A prévia não altera o Arquivo.") }
            item {
                OutlinedButton(
                    onClick = onSelectImport,
                    enabled = !state.importing,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text(if (state.importing) "Preparando…" else "Escolher arquivo") }
            }
            state.importPreview?.let { preview ->
                item {
                    Text(
                        "Prévia: ${preview.importableCount} novas, ${preview.duplicateCount} duplicadas, ${preview.issues.size} erros.",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(preview.items, key = { "preview-${preview.id}-${it.candidate.sourceIndex}" }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(displayImportDate(item.candidate.originalCreatedAt, item.candidate.originalTimeZone))
                            Text(item.candidate.content, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            if (item.duplicate) Text("Duplicada — não será importada.", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                items(preview.issues, key = { "issue-${preview.id}-${it.sourceIndex}-${it.code}" }) { issue ->
                    Text(importIssueLabel(issue.code), color = MaterialTheme.colorScheme.error)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = viewModel::commitImport,
                            enabled = preview.canCommit && !state.importing,
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) { Text("Importar ${preview.importableCount}") }
                        TextButton(onClick = viewModel::cancelImportPreview) { Text("Cancelar") }
                    }
                }
            }
            state.importMessage?.let { message ->
                item { Text(message, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
            }
            items(state.importBatches, key = { "batch-${it.id}" }) { batch ->
                if (batch.status.name == "COMMITTED") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Lote: ${batch.importedCount} entradas", fontWeight = FontWeight.SemiBold)
                            Text(batch.sourceFileName ?: "Nome do arquivo não mantido")
                            TextButton(onClick = { rollbackBatch = batch }) { Text("Desfazer este lote") }
                        }
                    }
                }
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        }
        item { SectionTitle("Exportar") }
        item { Text("O arquivo escolhido ficará fora da proteção do Fio. A exportação é local e não envia uma cópia para o Fio.") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onExportMarkdown) { Text("Markdown") }
                OutlinedButton(onClick = onExportText) { Text("Texto") }
            }
        }
        exportMessage?.let { message -> item { Text(message, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) } }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        item { SectionTitle("Excluídos recentemente") }
        if (state.deletedEntries.isEmpty()) {
            item { Text("Nenhuma entrada excluída.") }
        }
        items(state.deletedEntries, key = { "deleted-${it.id}" }) { entry ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(displayDate(entry), fontWeight = FontWeight.SemiBold)
                    Text(entry.content, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { viewModel.recoverEntry(entry.id) }) { Text("Recuperar") }
                        TextButton(onClick = { permanentDelete = entry }) { Text("Excluir para sempre") }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
        item {
            Text(
                if (state.m3EngineeringEnabled) "M3 em validação local: importação sintética, sem conta, sincronização, analytics ou piloto."
                else if (state.m2EngineeringEnabled) "M2 em validação local: sem conta, sincronização, analytics ou conteúdo real de teste."
                else "M1 local: sem conta, sincronização, analytics ou devoluções ativas.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun displayImportDate(instant: java.time.Instant, zone: String): String {
    val zoneId = runCatching { ZoneId.of(zone) }.getOrDefault(ZoneId.of("UTC"))
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.forLanguageTag("pt-BR"))
        .format(instant.atZone(zoneId))
}

private fun importIssueLabel(code: ImportIssueCode): String = when (code) {
    ImportIssueCode.FILE_TOO_LARGE -> "O arquivo ultrapassa 5 MiB."
    ImportIssueCode.TOO_MANY_LINES -> "O arquivo ultrapassa 20.000 linhas."
    ImportIssueCode.TOO_MANY_ENTRIES -> "O arquivo ultrapassa 2.000 entradas."
    ImportIssueCode.ENTRY_TOO_LARGE -> "Uma entrada ultrapassa 256 KiB."
    ImportIssueCode.MALFORMED_UTF8 -> "O arquivo não é UTF-8 válido."
    ImportIssueCode.UNSUPPORTED_CONTAINER -> "Arquivos compactados não são aceitos."
    ImportIssueCode.ACTIVE_CONTENT -> "Conteúdo HTML ou executável não é aceito."
    ImportIssueCode.CONTROL_CONTENT -> "O arquivo contém controles não suportados."
    ImportIssueCode.UNSUPPORTED_STRUCTURE -> "A estrutura de entradas não foi reconhecida."
    ImportIssueCode.MISSING_DATE -> "Uma entrada não possui data original explícita."
    ImportIssueCode.INVALID_DATE -> "Uma data original é inválida."
    ImportIssueCode.INVALID_SIZE -> "O tamanho declarado de uma entrada é inválido."
    ImportIssueCode.TIME_LIMIT -> "A leitura ultrapassou o limite de segurança."
}

@Composable
private fun ReturnScreen(entry: Entry, onClose: () -> Unit, onNeverReturn: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Text(
                    "Uma palavra sua voltou",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
            }
            item { Text(displayDate(entry), color = MaterialTheme.colorScheme.primary) }
            item { SelectionContainer { Text(entry.content, style = MaterialTheme.typography.bodyLarge) } }
            item {
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Fechar") }
            }
            item {
                TextButton(
                    onClick = onNeverReturn,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Não mostrar novamente") }
            }
        }
    }
}

@Composable
private fun AppLockChoices(selected: AppLockMode, enabled: Boolean, onChoose: (AppLockMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 8.dp)) {
        listOf(
            AppLockMode.OFF to "Desativado",
            AppLockMode.IMMEDIATE to "Imediato",
            AppLockMode.ONE_MINUTE to "Após 1 minuto",
            AppLockMode.FIVE_MINUTES to "Após 5 minutos",
        ).forEach { (mode, label) ->
            OutlinedButton(
                onClick = { onChoose(mode) },
                enabled = enabled && mode != selected,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (mode == selected) "$label — atual" else label) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
}

@Composable
fun PrivacyCover() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.Center) {
            Text("Fio", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun LockedScreen(
    authenticationAvailable: Boolean,
    onUnlock: () -> Unit,
    onDisableUnavailableLock: () -> Unit,
) {
    var confirmDisable by remember { mutableStateOf(false) }
    if (confirmDisable) {
        AlertDialog(
            onDismissRequest = { confirmDisable = false },
            title = { Text("Desativar o bloqueio do Fio?") },
            text = { Text("A autenticação do Android não está disponível. O conteúdo continuará cifrado no armazenamento, mas o Fio abrirá sem esta barreira adicional.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDisable = false
                    onDisableUnavailableLock()
                }) { Text("Desativar e continuar") }
            },
            dismissButton = { TextButton(onClick = { confirmDisable = false }) { Text("Manter bloqueado") } },
        )
    }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Fio", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                if (authenticationAvailable) "Protegido pelo bloqueio do seu aparelho." else "A autenticação do aparelho não está disponível.",
                modifier = Modifier.padding(top = 18.dp),
            )
            if (authenticationAvailable) {
                Button(onClick = onUnlock, modifier = Modifier.padding(top = 20.dp)) { Text("Abrir") }
            } else {
                OutlinedButton(
                    onClick = { confirmDisable = true },
                    modifier = Modifier.padding(top = 20.dp),
                ) { Text("Rever bloqueio") }
            }
        }
    }
}

@Composable
fun SafeOpenFailure() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Fio", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                "Os dados locais não puderam ser abertos com segurança. Nada foi apagado. Feche o Fio e tente novamente.",
                modifier = Modifier.padding(top = 18.dp),
            )
        }
    }
}

private fun displayDate(entry: Entry): String {
    val zone = runCatching { ZoneId.of(entry.originalTimeZone ?: "UTC") }.getOrDefault(ZoneId.of("UTC"))
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale.forLanguageTag("pt-BR"))
    return formatter.format(entry.originalCreatedAt.atZone(zone))
}

private fun quietHoursLabel(startMinute: Int, endMinute: Int): String =
    "%02dh%02d–%02dh%02d".format(
        startMinute / 60,
        startMinute % 60,
        endMinute / 60,
        endMinute % 60,
    )
