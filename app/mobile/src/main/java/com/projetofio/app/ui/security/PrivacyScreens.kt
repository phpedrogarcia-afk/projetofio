package com.projetofio.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import com.projetofio.app.ui.theme.FioRadius
import com.projetofio.app.ui.theme.FioSpace

@Composable
fun PrivacyCover() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.Center) {
            Text("Fio", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
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
    TextButton(
                    onClick = { confirmDisable = false; onDisableUnavailableLock() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Desativar e continuar") }
            },
            dismissButton = { TextButton(onClick = { confirmDisable = false }) { Text("Manter bloqueado") } },
        )
    }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(FioSpace.s6),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Fio", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                if (authenticationAvailable) "Protegido pelo bloqueio do seu aparelho." else "A autenticação do aparelho não está disponível.",
                modifier = Modifier.padding(top = FioSpace.s4),
            )
            if (authenticationAvailable) {
                Button(
                    onClick = onUnlock,
                    modifier = Modifier.padding(top = FioSpace.s4),
                    shape = RoundedCornerShape(FioRadius.lg),
                ) { Text("Abrir") }
            } else {
                OutlinedButton(
                    onClick = { confirmDisable = true },
                    modifier = Modifier.padding(top = FioSpace.s4),
                    shape = RoundedCornerShape(FioRadius.lg),
                ) { Text("Rever bloqueio") }
            }
        }
    }
}

@Composable
fun SafeOpenFailure() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(FioSpace.s6), verticalArrangement = Arrangement.Center) {
            Text("Fio", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                "Os dados locais não puderam ser abertos com segurança. Nada foi apagado. Feche o Fio e tente novamente.",
                modifier = Modifier.padding(top = FioSpace.s4),
            )
        }
    }
}
