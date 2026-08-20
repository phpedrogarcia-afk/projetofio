package com.projetofio.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.projetofio.app.domain.Entry
import com.projetofio.app.ui.theme.FioDisplayDate
import com.projetofio.app.ui.theme.FioRadius
import com.projetofio.app.ui.theme.FioSpace

@Composable
internal fun ReturnScreen(entry: Entry, onClose: () -> Unit, onNeverReturn: () -> Unit) {
    var confirmNeverReturn by remember(entry.id) { mutableStateOf(false) }
    BackHandler(onBack = onClose)
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = FioSpace.s6, vertical = FioSpace.s8),
            verticalArrangement = Arrangement.spacedBy(FioSpace.s4),
        ) {
            Text("Uma palavra sua voltou", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            Text(displayDate(entry), style = FioDisplayDate, color = MaterialTheme.colorScheme.primary)
            SelectionContainer { Text(entry.content, style = MaterialTheme.typography.bodyLarge) }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(FioRadius.lg),
            ) { Text("Fechar") }
            TextButton(
                onClick = { confirmNeverReturn = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text("Não devolver esta nota novamente") }
        }
    }
    if (confirmNeverReturn) {
        AlertDialog(
            onDismissRequest = { confirmNeverReturn = false },
            title = { Text("Não devolver esta nota novamente?") },
            text = {
                Text(
                    "A nota continuará no Arquivo e poderá ser lida, editada ou excluída. " +
                        "O Fio apenas deixará de escolhê-la para voltar. Esta opção não pode ser desfeita nesta versão.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmNeverReturn = false
                        onNeverReturn()
                    },
                ) { Text("Não devolver") }
            },
            dismissButton = {
                TextButton(onClick = { confirmNeverReturn = false }) { Text("Cancelar") }
            },
        )
    }
}
