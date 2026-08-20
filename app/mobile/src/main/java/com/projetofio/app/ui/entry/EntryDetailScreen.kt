package com.projetofio.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.projetofio.app.domain.Entry
import com.projetofio.app.ui.theme.FioDisplayDate
import com.projetofio.app.ui.theme.FioSpace
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.ZoneId
import java.util.Locale

@Composable
internal fun NoteScreen(
    entry: Entry,
    onBack: () -> Unit,
    onReedit: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    BackHandler(onBack = onBack)
    var returning by remember { mutableStateOf(false) }
    var returned by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.padding(horizontal = FioSpace.s4, vertical = FioSpace.s5)) {
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
                        .semantics { contentDescription = "Voltar" },
                )
            }
            Spacer(Modifier.height(FioSpace.s3))
            SelectionContainer {
                Text(
                    text = noteDate(entry),
                    style = FioDisplayDate,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = FioSpace.s5),
                )
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(FioSpace.s5))
            if (onEdit != null || onDelete != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(FioSpace.s2),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    onEdit?.let { edit ->
                        TextButton(
                            onClick = edit,
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text("Editar")
                        }
                    }
                    onDelete?.let { delete ->
                        TextButton(
                            onClick = delete,
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text("Excluir", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(Modifier.height(FioSpace.s2))
            }
            Row {
                Text(
                    text = if (returned) "Reescrever esta nota?" else "Devolver para agora", // G2: factual — a antiga interpretativa foi neutralizada
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .clickable(onClick = {
                            if (returned) {
                                onReedit(entry.content)
                                onBack()
                            } else {
                                returning = true
                            }
                        })
                        .heightIn(min = 48.dp)
                        .padding(vertical = FioSpace.s2)
                        .semantics { contentDescription = if (returned) "Reescrever esta nota agora" else "Devolver esta nota agora" },
                )
            }
            Spacer(Modifier.height(FioSpace.s6))
            BotanicalMotif()
            Spacer(Modifier.height(FioSpace.s4))
            if (returned) {
                Text(
                    // G2 fix: factual, sem interpretar o que a volta significa.
                    text = "Esta nota voltou. Reescrever se quiser — ou apenas deixe seguir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }
    if (returning) {
        AlertDialog(
            onDismissRequest = { returning = false },
            title = { Text("Devolver agora?") },
            text = {
                Text(
                    "O Fio devolve o que está pronto. Você pode trazê-la de volta agora — ela volta como uma carta, na íntegra, com a data de quando foi escrita.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { returning = false; returned = true },
                ) { Text("Devolver") }
            },
            dismissButton = { TextButton(onClick = { returning = false }) { Text("Cancelar") } },
        )
    }
}

private fun noteDate(entry: Entry): String {
    val zone = runCatching { ZoneId.of(entry.originalTimeZone ?: "UTC") }.getOrDefault(ZoneId.of("UTC"))
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.forLanguageTag("pt-BR"))
    return formatter.format(entry.originalCreatedAt.atZone(zone).toLocalDate())
}

@Composable
internal fun EditEntryDialog(entry: Entry, onDismiss: () -> Unit, onSave: (String) -> Unit) {
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
        confirmButton = {
            TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) {
                Text("Guardar alterações")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

// ===========================================================================
// SETTINGS — a short map first, one understandable subject at a time.
// ===========================================================================
