package com.projetofio.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projetofio.app.domain.Entry
import com.projetofio.app.ui.theme.FioSpace

@Composable
internal fun SearchScreen(
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
    editing?.let { entry ->
        EditEntryDialog(
            entry = entry,
            onDismiss = { editing = null },
            onSave = { content ->
                viewModel.editEntry(entry.id, content)
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
                    "Encontrar",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics {
                        heading()
                        contentDescription = "Tela Encontrar"
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
                "Procure uma palavra ou frase que você escreveu.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = FioSpace.s3),
            )
        }
        item { ArchiveSearchField(terms = state.searchTerms, onChanged = viewModel::onSearchChanged) }

        if (state.searchTerms.isBlank()) {
            item {
                Column(modifier = Modifier.padding(top = FioSpace.s6)) {
                    BotanicalMotif(firstEntryAt = state.entries.lastOrNull()?.originalCreatedAt)
                    Spacer(Modifier.height(FioSpace.s3))
                    Text(
                        "O Fio encontra suas palavras como foram guardadas.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Ele não responde por você nem interpreta o que viveu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = FioSpace.s1),
                    )
                }
            }
        } else {
            when {
                state.searchLoading -> item {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = FioSpace.s5))
                }
                state.searchError != null -> item {
                    Text(
                        state.searchError,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = FioSpace.s3),
                    )
                }
                state.searchResult == null -> Unit
                else -> {
                    val result = checkNotNull(state.searchResult)
                    item {
                        Text(
                            text = when (result.hits.size) {
                                0 -> "Nenhum resultado"
                                1 -> "1 resultado"
                                else -> "${result.hits.size} resultados"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(vertical = FioSpace.s2)
                                .semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                    if (result.sealedCount > 0) {
                        item {
                            Text(
                                text = "${result.sealedCount} nota(s) selada(s) permaneceram ocultas.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = FioSpace.s2),
                            )
                        }
                    }
                    if (result.hits.isEmpty()) {
                        item {
                            Text(
                                text = "Nada no Arquivo contém essas palavras.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = FioSpace.s3),
                            )
                        }
                    }
                    items(result.hits, key = { it.entry.id }) { hit ->
                        ArchiveSearchHitRow(hit = hit, onOpen = { reading = hit.entry })
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(FioSpace.s5)) }
    }
}

@Composable
private fun ArchiveSearchField(terms: String, onChanged: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = terms,
        onValueChange = onChanged,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics(mergeDescendants = true) {},
        label = { Text("Buscar nas suas palavras") },
        placeholder = { Text("Uma palavra ou frase", style = MaterialTheme.typography.bodyLarge) },
        singleLine = true,
        trailingIcon = {
            if (terms.isNotBlank()) {
                TextButton(
                    onClick = {
                        onChanged("")
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Limpar busca" },
                ) { Text("✕", fontSize = 13.sp) }
            }
        },
    )
    Spacer(Modifier.height(FioSpace.s2))
}

@Composable
private fun ArchiveSearchHitRow(hit: com.projetofio.app.domain.SearchHit, onOpen: () -> Unit) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(vertical = FioSpace.s3)
                .semantics(mergeDescendants = true) {
                    contentDescription = buildString {
                        append(displayDate(hit.entry))
                        append(". ")
                        append(hit.matchedSnippet)
                        if (hit.returnedCount > 0) {
                            append(". ")
                            append("Já voltou ${hit.returnedCount} vez(es)")
                        }
                    }
                    liveRegion = LiveRegionMode.Polite
                },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    displayDate(hit.entry),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (hit.returnedCount > 0) {
                    Spacer(Modifier.width(FioSpace.s2))
                    Text(
                        text = "Já voltou ${hit.returnedCount} vez(es)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            Text(
                text = hit.matchedSnippet,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = FioSpace.s1),
            )
        }
    }
}

// ===========================================================================
// ARCHIVE — typographic grouping by month, temporal distance as secondary
// line, no cards (the system's rule: cards only for elevated objects).
// ===========================================================================
