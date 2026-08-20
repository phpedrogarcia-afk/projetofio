package com.projetofio.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.annotation.DrawableRes
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projetofio.app.domain.AppLockMode
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.ImportBatch
import com.projetofio.app.domain.ImportIssueCode
import com.projetofio.app.domain.NotificationPermissionObserved
import com.projetofio.app.domain.ReturnConsentState
import com.projetofio.app.R
import com.projetofio.app.ui.theme.FioRadius
import com.projetofio.app.ui.theme.FioSpace
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

private enum class SettingsPage { OVERVIEW, PRIVACY, RETURNS, IMPORT, EXPORT, DELETED }

@Composable
internal fun SettingsScreen(
    state: FioUiState,
    viewModel: FioViewModel,
    padding: PaddingValues,
    authenticationAvailable: Boolean,
    exportMessage: String?,
    authorizeFresh: (() -> Unit) -> Unit,
    onExportMarkdown: () -> Unit,
    onExportText: () -> Unit,
    onBack: () -> Unit,
    onEnableReturns: () -> Unit,
    onOpenPendingReturn: (String) -> Unit,
    onSelectImport: () -> Unit,
) {
    var page by remember { mutableStateOf(SettingsPage.OVERVIEW) }
    BackHandler {
        if (page == SettingsPage.OVERVIEW) onBack() else page = SettingsPage.OVERVIEW
    }
    var permanentDelete by remember { mutableStateOf<Entry?>(null) }
    var confirmReturnConsent by remember { mutableStateOf(false) }
    var rollbackBatch by remember { mutableStateOf<ImportBatch?>(null) }
    if (confirmReturnConsent) {
        AlertDialog(
            onDismissRequest = { confirmReturnConsent = false },
            title = { Text("Ativar lembranças que voltam?") },
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
                }) { Text("Ativar") }
            },
            dismissButton = { TextButton(onClick = { confirmReturnConsent = false }) { Text("Agora não") } },
        )
    }
    permanentDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { permanentDelete = null },
            title = { Text("Excluir permanentemente?") },
            text = {
                Text(
                    "Esta ação não pode ser desfeita. A nota deixará de aparecer no Fio e não poderá ser recuperada pelo aplicativo. " +
                        "O armazenamento do celular ainda pode manter vestígios temporários.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { authorizeFresh { viewModel.permanentlyDelete(entry.id) }; permanentDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Excluir permanentemente") }
            },
            dismissButton = { TextButton(onClick = { permanentDelete = null }) { Text("Cancelar") } },
        )
    }

    rollbackBatch?.let { batch ->
        AlertDialog(
            onDismissRequest = { rollbackBatch = null },
            title = { Text("Desfazer esta importação?") },
            text = { Text("Entradas importadas e não editadas irão para Excluídos recentemente. Entradas editadas depois da importação serão preservadas.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rollbackImport(batch.id)
                    rollbackBatch = null
                }) { Text("Desfazer importação") }
            },
            dismissButton = { TextButton(onClick = { rollbackBatch = null }) { Text("Cancelar") } },
        )
    }

    when (page) {
        SettingsPage.OVERVIEW -> SettingsOverview(
            state = state,
            padding = padding,
            onBack = onBack,
            onOpen = { page = it },
        )
        SettingsPage.PRIVACY -> PrivacySettingsPage(
            state = state,
            padding = padding,
            authenticationAvailable = authenticationAvailable,
            authorizeFresh = authorizeFresh,
            onChoose = viewModel::setAppLockMode,
            onBack = { page = SettingsPage.OVERVIEW },
        )
        SettingsPage.RETURNS -> ReturnsSettingsPage(
            state = state,
            padding = padding,
            onAskEnable = { confirmReturnConsent = true },
            onPause = viewModel::pauseReturns,
            onResume = viewModel::resumeReturns,
            onQuietHours = viewModel::setQuietHours,
            onOpenPendingReturn = onOpenPendingReturn,
            onBack = { page = SettingsPage.OVERVIEW },
        )
        SettingsPage.IMPORT -> ImportSettingsPage(
            state = state,
            padding = padding,
            viewModel = viewModel,
            onSelectImport = onSelectImport,
            onRollback = { rollbackBatch = it },
            onBack = { page = SettingsPage.OVERVIEW },
        )
        SettingsPage.EXPORT -> ExportSettingsPage(
            padding = padding,
            exportMessage = exportMessage,
            onExportMarkdown = onExportMarkdown,
            onExportText = onExportText,
            onBack = { page = SettingsPage.OVERVIEW },
        )
        SettingsPage.DELETED -> DeletedSettingsPage(
            entries = state.deletedEntries,
            padding = padding,
            onRecover = viewModel::recoverEntry,
            onPurge = { permanentDelete = it },
            onBack = { page = SettingsPage.OVERVIEW },
        )
    }
}

@Composable
private fun SettingsOverview(
    state: FioUiState,
    padding: PaddingValues,
    onBack: () -> Unit,
    onOpen: (SettingsPage) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = FioSpace.s4, vertical = FioSpace.s5),
        verticalArrangement = Arrangement.spacedBy(FioSpace.s2),
    ) {
        item { SettingsHeader("Ajustes", onBack, "Voltar") }
        item {
            Text(
                "Escolha o que você quer controlar. Cada item explica o efeito antes de mudar alguma coisa.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = FioSpace.s3),
            )
        }
        if (state.m2EngineeringEnabled) {
            item { SectionTitle("Lembranças") }
            item {
                SettingsNavigationRow(
                    title = "Lembranças que voltam",
                    summary = returnsSummary(state.settings.returnConsentState),
                    icon = R.drawable.ic_time_return,
                    onClick = { onOpen(SettingsPage.RETURNS) },
                )
            }
        }
        item { SectionTitle("Privacidade") }
        item {
            SettingsNavigationRow(
                title = "Proteção ao abrir",
                summary = appLockSummary(state.settings.appLockMode),
                icon = R.drawable.ic_privacy,
                onClick = { onOpen(SettingsPage.PRIVACY) },
            )
        }
        item { SectionTitle("Seus dados") }
        if (state.m3EngineeringEnabled) {
            item {
                SettingsNavigationRow(
                    title = "Importar notas",
                    summary = "Trazer textos e datas de um arquivo TXT ou Markdown.",
                    icon = R.drawable.ic_import,
                    onClick = { onOpen(SettingsPage.IMPORT) },
                )
            }
        }
        item {
            SettingsNavigationRow(
                title = "Exportar uma cópia",
                summary = "Guardar todas as suas notas em um arquivo legível.",
                icon = R.drawable.ic_export,
                onClick = { onOpen(SettingsPage.EXPORT) },
            )
        }
        item {
            SettingsNavigationRow(
                title = "Excluídos recentemente",
                summary = when (state.deletedEntries.size) {
                    0 -> "Nenhuma nota esperando exclusão definitiva."
                    1 -> "1 nota pode ser recuperada por até 30 dias."
                    else -> "${state.deletedEntries.size} notas podem ser recuperadas por até 30 dias."
                },
                icon = R.drawable.ic_deleted,
                onClick = { onOpen(SettingsPage.DELETED) },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = FioSpace.s5),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    buildString {
                        append("Suas notas ficam neste aparelho. Não há conta, sincronização ou análise das suas palavras.")
                        if (!state.m2EngineeringEnabled || !state.m3EngineeringEnabled) {
                            append(" Nesta instalação, alguns recursos ainda não estão disponíveis.")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(FioSpace.s4))
                BotanicalMotif(firstEntryAt = state.entries.lastOrNull()?.originalCreatedAt)
            }
        }
    }
}

@Composable
private fun PrivacySettingsPage(
    state: FioUiState,
    padding: PaddingValues,
    authenticationAvailable: Boolean,
    authorizeFresh: (() -> Unit) -> Unit,
    onChoose: (AppLockMode) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = FioSpace.s4, vertical = FioSpace.s5),
        verticalArrangement = Arrangement.spacedBy(FioSpace.s3),
    ) {
        item { SettingsHeader("Proteção ao abrir", onBack, "Voltar aos Ajustes") }
        item {
            Text(
                "Escolha quando o Fio deve pedir a impressão digital, o rosto ou o código configurado no seu Android.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        item {
            Text(
                "O conteúdo também fica oculto na tela de aplicativos recentes. Isso não substitui o bloqueio do próprio aparelho.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!authenticationAvailable) {
            item {
                Text(
                    "Para ativar esta proteção, primeiro configure um bloqueio de tela no Android.",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        item {
            AppLockChoices(
                selected = state.settings.appLockMode,
                enabled = authenticationAvailable,
                onChoose = { mode -> authorizeFresh { onChoose(mode) } },
            )
        }
    }
}

@Composable
private fun ReturnsSettingsPage(
    state: FioUiState,
    padding: PaddingValues,
    onAskEnable: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onQuietHours: (Int, Int) -> Unit,
    onOpenPendingReturn: (String) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = FioSpace.s4, vertical = FioSpace.s5),
        verticalArrangement = Arrangement.spacedBy(FioSpace.s3),
    ) {
        item { SettingsHeader("Lembranças que voltam", onBack, "Voltar aos Ajustes") }
        item {
            Text(
                "Quando uma nota antiga estiver disponível, o Fio pode mostrar uma notificação discreta: “Algo seu voltou.” A notificação nunca mostra suas palavras.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        item {
            Text(
                "Estado atual: ${returnsSummary(state.settings.returnConsentState)}",
                fontWeight = FontWeight.SemiBold,
            )
        }
        when (state.settings.returnConsentState) {
            ReturnConsentState.NOT_CONFIGURED -> item {
                Button(
                    onClick = onAskEnable,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Ativar") }
            }
            ReturnConsentState.ENABLED -> {
                item {
                    OutlinedButton(
                        onClick = onPause,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text("Pausar lembranças") }
                }
                item { SectionTitle("Horário sem notificações") }
                item {
                    Text(
                        "Atual: ${quietHoursSentence(state.settings.quietHoursStartMinute, state.settings.quietHoursEndMinute)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(FioSpace.s2)) {
                        OutlinedButton(
                            onClick = { onQuietHours(21 * 60, 8 * 60) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) { Text("Não avisar das 21h às 8h") }
                        OutlinedButton(
                            onClick = { onQuietHours(22 * 60, 9 * 60) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) { Text("Não avisar das 22h às 9h") }
                    }
                }
            }
            ReturnConsentState.PAUSED -> item {
                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Retomar lembranças") }
            }
        }
        if (state.settings.notificationPermissionObserved == NotificationPermissionObserved.DENIED) {
            item {
                Text(
                    "As notificações do Fio estão desligadas no Android. Suas notas continuam guardadas e você pode usar o app normalmente.",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        state.pendingReturnId?.let { id ->
            item {
                OutlinedButton(
                    onClick = { onOpenPendingReturn(id) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Abrir lembrança disponível") }
            }
        }
        state.returnError?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
    }
}

@Composable
private fun ImportSettingsPage(
    state: FioUiState,
    padding: PaddingValues,
    viewModel: FioViewModel,
    onSelectImport: () -> Unit,
    onRollback: (ImportBatch) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = FioSpace.s4, vertical = FioSpace.s5),
        verticalArrangement = Arrangement.spacedBy(FioSpace.s3),
    ) {
        item { SettingsHeader("Importar notas", onBack, "Voltar aos Ajustes") }
        item {
            Text(
                "Traga notas de um arquivo TXT ou Markdown. O Fio mostra uma prévia antes de guardar qualquer coisa.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        item {
            Text(
                "O arquivo precisa ter datas explícitas. Notas repetidas são identificadas e não são importadas novamente.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Button(
                onClick = onSelectImport,
                enabled = !state.importing,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(if (state.importing) "Preparando prévia…" else "Escolher arquivo") }
        }
        state.importPreview?.let { preview ->
            item {
                Text(
                    "Prévia: ${preview.importableCount} novas, ${preview.duplicateCount} repetidas, ${preview.issues.size} com problema.",
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(preview.items, key = { "preview-${preview.id}-${it.candidate.sourceIndex}" }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(displayImportDate(item.candidate.originalCreatedAt, item.candidate.originalTimeZone))
                        Text(item.candidate.content, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        if (item.duplicate) Text("Repetida — não será importada.", color = MaterialTheme.colorScheme.primary)
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
        if (state.importBatches.any { it.status.name == "COMMITTED" }) {
            item { SectionTitle("Importações anteriores") }
        }
        items(state.importBatches, key = { "batch-${it.id}" }) { batch ->
            if (batch.status.name == "COMMITTED") {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${batch.importedCount} notas importadas", fontWeight = FontWeight.SemiBold)
                        Text(batch.sourceFileName ?: "O nome do arquivo não foi guardado")
                        TextButton(onClick = { onRollback(batch) }) { Text("Desfazer esta importação") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportSettingsPage(
    padding: PaddingValues,
    exportMessage: String?,
    onExportMarkdown: () -> Unit,
    onExportText: () -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = FioSpace.s4, vertical = FioSpace.s5),
        verticalArrangement = Arrangement.spacedBy(FioSpace.s3),
    ) {
        item { SettingsHeader("Exportar uma cópia", onBack, "Voltar aos Ajustes") }
        item {
            Text(
                "Crie um único arquivo com todas as suas notas, em ordem de data. Você poderá abrir esse arquivo sem o Fio.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        item {
            Text(
                "A cópia ficará no local que você escolher e não terá a proteção do Fio. O conteúdo não é enviado para nós.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Text(
                "Se estiver em dúvida, escolha Texto (.txt).",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(FioSpace.s2)) {
                OutlinedButton(
                    onClick = onExportText,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("Texto (.txt)", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Mais simples. Abre em praticamente qualquer lugar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                OutlinedButton(
                    onClick = onExportMarkdown,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("Markdown (.md)", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Preserva melhor a estrutura para aplicativos compatíveis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        exportMessage?.let { message ->
            item { Text(message, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        }
    }
}

@Composable
private fun DeletedSettingsPage(
    entries: List<Entry>,
    padding: PaddingValues,
    onRecover: (String) -> Unit,
    onPurge: (Entry) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = FioSpace.s4, vertical = FioSpace.s5),
        verticalArrangement = Arrangement.spacedBy(FioSpace.s2),
    ) {
        item { SettingsHeader("Excluídos recentemente", onBack, "Voltar aos Ajustes") }
        item {
            Text(
                "Notas excluídas podem ser recuperadas por 30 dias. Depois desse prazo, são removidas automaticamente.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = FioSpace.s3),
            )
        }
        if (entries.isEmpty()) {
            item { Text("Nenhuma nota excluída.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(entries, key = { "deleted-${it.id}" }) { entry ->
            DeletedRow(
                entry = entry,
                onRecover = { onRecover(entry.id) },
                onPurge = { onPurge(entry) },
            )
        }
    }
}

@Composable
private fun SettingsHeader(title: String, onBack: () -> Unit, backDescription: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "← ",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(onClick = onBack)
                .heightIn(min = 48.dp)
                .widthIn(min = 48.dp)
                .padding(12.dp)
                .semantics { contentDescription = backDescription },
        )
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics {
                heading()
                contentDescription = "Tela $title"
            },
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    summary: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 72.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(FioRadius.md))
            .semantics(mergeDescendants = true) { contentDescription = "$title. $summary" },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(FioRadius.md),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FioSpace.s3, vertical = FioSpace.s3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(22.dp),
            )
            Spacer(Modifier.width(FioSpace.s3))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = FioSpace.s1),
                )
            }
            Spacer(Modifier.width(FioSpace.s2))
            Text("›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun appLockSummary(mode: AppLockMode): String = when (mode) {
    AppLockMode.OFF -> "O Fio não pede desbloqueio ao abrir."
    AppLockMode.IMMEDIATE -> "O Fio pede desbloqueio sempre que é aberto."
    AppLockMode.ONE_MINUTE -> "O Fio pede após 1 minuto fora do app."
    AppLockMode.FIVE_MINUTES -> "O Fio pede após 5 minutos fora do app."
}

private fun returnsSummary(state: ReturnConsentState): String = when (state) {
    ReturnConsentState.NOT_CONFIGURED -> "Desativadas — nenhuma notificação será enviada."
    ReturnConsentState.ENABLED -> "Ativas — uma lembrança antiga pode voltar de vez em quando."
    ReturnConsentState.PAUSED -> "Pausadas — nenhuma nova lembrança será avisada."
}

@Composable
private fun DeletedRow(entry: Entry, onRecover: () -> Unit, onPurge: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FioSpace.s3),
    ) {
        Text(displayDate(entry), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(entry.content, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = FioSpace.s1))
        Row(horizontalArrangement = Arrangement.spacedBy(FioSpace.s2)) {
            TextButton(onClick = onRecover, modifier = Modifier.heightIn(min = 40.dp)) { Text("Recuperar", fontSize = 13.sp) }
            TextButton(onClick = onPurge, modifier = Modifier.heightIn(min = 40.dp)) {
                Text("Excluir para sempre", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


private fun quietHoursSentence(startMinute: Int, endMinute: Int): String {
    fun fmt(m: Int): String = "${(m / 60) % 24}h${(m % 60).let { if (it == 0) "00" else String.format(Locale.ROOT, "%02d", it) }}"
    return "sem avisos das ${fmt(startMinute)} às ${fmt(endMinute)}"
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



// Returns screen — the engine's delivery contract (codex PR #1), adapted to the
// Verde-Sálvia design: Fraunces display date, quiet wording, primary close.
@Composable
private fun AppLockChoices(selected: AppLockMode, enabled: Boolean, onChoose: (AppLockMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(FioSpace.s1), modifier = Modifier.padding(top = FioSpace.s2)) {
        listOf(
            AppLockMode.OFF to "Não pedir desbloqueio",
            AppLockMode.IMMEDIATE to "Pedir sempre que abrir",
            AppLockMode.ONE_MINUTE to "Pedir após 1 minuto fora",
            AppLockMode.FIVE_MINUTES to "Pedir após 5 minutos fora",
        ).forEach { (mode, label) ->
            val isSelected = mode == selected
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled && !isSelected) { onChoose(mode) }
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(FioRadius.md),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = FioSpace.s3, vertical = FioSpace.s2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                    if (isSelected) {
                        Spacer(Modifier.width(FioSpace.s2))
                        Text(
                            "— atual",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = FioSpace.s2).semantics { heading() })
}
