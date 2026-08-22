package com.projetofio.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.projetofio.app.domain.Entry
import com.projetofio.app.ui.theme.FioSpace
import com.projetofio.app.ui.theme.FioRadius
import com.projetofio.app.ui.theme.FioThemeContext
import com.projetofio.app.ui.theme.cosmic.CosmicOrnament
import com.projetofio.app.ui.theme.cosmic.CosmicOrnament as CosmicOrnamentType
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.ZoneId
import java.util.Locale

@Composable
internal fun ArchiveScreen(
    state: FioUiState,
    viewModel: FioViewModel,
    padding: PaddingValues,
    onOpenSettings: () -> Unit,
) {
    var reading by remember { mutableStateOf<Entry?>(null) }
    var editing by remember { mutableStateOf<Entry?>(null) }
    var deleting by remember { mutableStateOf<Entry?>(null) }

    reading?.let { entry ->
        NoteScreen(
            entry = entry,
            onBack = { reading = null },
            onReedit = { text -> viewModel.onDraftChanged(text) },
            contentPadding = padding,
            onEdit = {
                reading = null
                editing = entry
            },
            onDelete = {
                reading = null
                deleting = entry
            },
        )
        return
    }
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
                TextButton(onClick = { viewModel.deleteEntry(entry.id); deleting = null }) {
                    Text("Mover")
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = FioSpace.s4, vertical = FioSpace.s5),
        verticalArrangement = Arrangement.spacedBy(FioSpace.s1),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Arquivo",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics {
                        heading()
                        contentDescription = "Tela Arquivo"
                    },
                )
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        "Ajustes",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Text(
                text = "Suas palavras, em ordem do tempo.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = FioSpace.s3),
            )
        }

        if (state.loading) item { CircularProgressIndicator() }
        if (state.archiveError) {
            item { Text("O Arquivo não pôde ser aberto com segurança. Nada foi apagado.", color = MaterialTheme.colorScheme.error) }
        } else if (!state.loading && state.entries.isEmpty()) {
            item {
                if (FioThemeContext.current.isCosmic) {
                    Box(modifier = Modifier.fillMaxWidth().height(190.dp).padding(vertical = FioSpace.s4)) {
                        CosmicOrnament(CosmicOrnamentType.OBSERVATORY, opacity = .20f)
                        Text(
                            "Quando quiser, suas palavras podem ficar aqui.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                } else {
                    Column(modifier = Modifier.padding(vertical = FioSpace.s7)) {
                        BotanicalMotif(firstEntryAt = state.entries.lastOrNull()?.originalCreatedAt)
                        Spacer(Modifier.height(FioSpace.s3))
                        Text(
                            "Quando quiser, suas palavras podem ficar aqui.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            val grouped = state.entries.groupBy { groupKey(it) }
            grouped.forEach { (label, entries) ->
                item {
                    Column(modifier = Modifier.padding(top = FioSpace.s4, bottom = FioSpace.s2)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (FioThemeContext.current.isCosmic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                items(entries, key = { it.id }) { entry ->
                    ArchiveRow(
                        entry = entry,
                        onOpen = { reading = entry },
                    )
                    if (!FioThemeContext.current.isCosmic) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(FioSpace.s5)) }
    }
}

private fun groupKey(entry: Entry): String {
    val zone = runCatching { ZoneId.of(entry.originalTimeZone ?: "UTC") }.getOrDefault(ZoneId.of("UTC"))
    val local = entry.originalCreatedAt.atZone(zone).toLocalDate()
    val monthLabel = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
        .format(local)
    return monthLabel.replaceFirstChar { it.uppercase(Locale.forLanguageTag("pt-BR")) }
}

private fun temporalDistance(entry: Entry): String {
    val zone = runCatching { ZoneId.of(entry.originalTimeZone ?: "UTC") }.getOrDefault(ZoneId.of("UTC"))
    val then = entry.originalCreatedAt.atZone(zone).toLocalDate()
    val now = LocalDate.now(zone)
    val days = ChronoUnit.DAYS.between(then, now)
    return when {
        days <= 0 -> "Hoje"
        days == 1L -> "Ontem"
        days < 30 -> "há $days dias"
        days < 365 -> {
            val months = (days / 30).toInt().coerceAtLeast(1)
            "há $months " + if (months == 1) "mês" else "meses"
        }
        else -> {
            val years = (days / 365).toInt().coerceAtLeast(1)
            "há $years " + if (years == 1) "ano" else "anos"
        }
    }
}

@Composable
private fun ArchiveRow(entry: Entry, onOpen: () -> Unit) {
    val cosmic = FioThemeContext.current.isCosmic
    val shape = RoundedCornerShape(FioRadius.md)
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(
            if (cosmic) {
                Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.70f), shape)
                    .border(0.6.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            } else {
                Modifier
            },
        )
        .clickable(onClick = onOpen)
        .padding(
            horizontal = if (cosmic) FioSpace.s3 else 0.dp,
            vertical = FioSpace.s3,
        )
        .semantics(mergeDescendants = true) {
            contentDescription = "Abrir lembrança de ${displayDay(entry)} para ler ou editar. ${entry.content}"
        }
    SelectionContainer {
        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        displayDay(entry),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(FioSpace.s2))
                    Text(
                        temporalDistance(entry),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = FioSpace.s1),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (cosmic) {
                Text(
                    "✦",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.70f),
                    modifier = Modifier.padding(start = FioSpace.s2),
                )
            } else {
                Text(
                    "›",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = FioSpace.s3),
                )
            }
        }
    }
}

// ===========================================================================
// NOTE — reading an entry is a letter, not a row. Serif date, calm body.
// ===========================================================================
