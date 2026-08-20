package com.projetofio.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.projetofio.app.domain.ReturnConsentState
import com.projetofio.app.R
import com.projetofio.app.ui.theme.FioRadius
import com.projetofio.app.ui.theme.FioSpace
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// first-class candidate.
// ---------------------------------------------------------------------------
sealed class ReturnPolicy {
    data object Someday : ReturnPolicy() // organic eligibility
    data class InPeriod(val days: Int) : ReturnPolicy() // explicit request
    data class OnDate(val date: LocalDate) : ReturnPolicy() // anchored request
    data object Never : ReturnPolicy()
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    state: FioUiState,
    viewModel: FioViewModel,
    padding: PaddingValues,
    onOpenSettings: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val largeText = LocalDensity.current.fontScale >= 1.2f
    var timeSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf(false) }
    var policy by remember { mutableStateOf<ReturnPolicy>(ReturnPolicy.Someday) }

    // First-capsule copy (ADR-044): the single extended confirmation appears
    // only for the very first save of the life of this installation.
    val isFirstSave =
        state.settings.returnConsentState == ReturnConsentState.NOT_CONFIGURED &&
            state.entries.isEmpty() && state.savedNotice
    val saveCopy = if (isFirstSave) "Guardado. O tempo cuida do resto." else "Guardado."

    val screenModifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = FioSpace.s6, vertical = FioSpace.s5)
            .let { base ->
                if (largeText) base.verticalScroll(rememberScrollState()) else base
            }
    Column(modifier = screenModifier) {
        // Top row: writing keeps the strongest brand treatment; only the
        // secondary Settings action stays outside primary navigation.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Fio",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics {
                    heading()
                    contentDescription = "Tela Escrever"
                },
            )
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(FioSpace.s6))

        // The prompt — small, calm, in the body scale.
        Text(
            text = "O que está passando pela sua cabeça hoje?",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(FioSpace.s4))

        // Paper within paper: enough presence to feel intentional, without a
        // heavy card competing with the person's words.
        val interactionSource = remember { MutableInteractionSource() }
        val editorModifier = if (largeText) {
            Modifier.fillMaxWidth().heightIn(min = 280.dp)
        } else {
            Modifier.fillMaxWidth().weight(1f)
        }
        Surface(
            modifier = editorModifier
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(FioRadius.md)),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
            shape = RoundedCornerShape(FioRadius.md),
        ) {
            BasicTextField(
                value = state.draftText,
                onValueChange = viewModel::onDraftChanged,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(FioSpace.s4)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Área de escrita"
                    },
                decorationBox = { inner ->
                    Box {
                        if (state.draftText.isEmpty()) {
                            Text(
                                "Escreva quando quiser.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                            )
                        }
                        inner()
                    }
                },
            )
        }

        val policyLabel = when (val p = policy) {
            ReturnPolicy.Someday -> "Algum dia"
            is ReturnPolicy.InPeriod -> returnPolicyLabel(p.days)
            is ReturnPolicy.OnDate -> p.date.format(Formatter.ptDate)
            ReturnPolicy.Never -> "Nunca"
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = FioSpace.s3)
                .clickable(onClick = { timeSheet = true })
                .heightIn(min = 48.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(FioRadius.md))
                .padding(horizontal = FioSpace.s3, vertical = FioSpace.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_clock),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.height(18.dp).width(18.dp),
            )
            Spacer(Modifier.width(FioSpace.s2))
            Text(
                text = "Pode voltar · $policyLabel",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "›",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics {
                    contentDescription = "Escolher quando pode voltar. Escolhido: $policyLabel"
                },
            )
        }

        // Error pill (terracotta, discreet, with icon) — replaces the
        // full-width red text block for recoverable errors.
        state.recoverableError?.let {
            Row(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        RoundedCornerShape(FioRadius.md),
                    )
                    .padding(horizontal = FioSpace.s3, vertical = FioSpace.s2)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_error),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(16.dp).width(16.dp),
                )
                Spacer(Modifier.width(FioSpace.s2))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
            }
            Spacer(Modifier.height(FioSpace.s3))
        }

        // Primary action — the tallest, warmest touch target in the app.
        val pressed by interactionSource.collectIsPressedAsState()
        Button(
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                viewModel.saveEntry()
            },
            enabled = state.draftText.isNotBlank() && !state.saving,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(FioRadius.lg),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = FioSpace.s4)
                .heightIn(min = 52.dp)
                .scale(if (pressed) 0.98f else 1f)
                .alpha(if (pressed) 0.92f else 1f)
                .semantics { contentDescription = "Guardar lembrança" },
        ) {
            Text(if (state.saving) "Guardando…" else "Guardar", style = MaterialTheme.typography.labelMedium)
        }
    }

    // Floating "Guardado." pill (ADR-014: immediate, restrained, gone in 1.5s)
    AnimatedVisibility(
        visible = state.savedNotice,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 88.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(FioRadius.full),
                )
                .padding(horizontal = FioSpace.s4, vertical = FioSpace.s2)
                .semantics { liveRegion = LiveRegionMode.Polite },
            contentAlignment = Alignment.Center,
        ) {
            Text(saveCopy, style = MaterialTheme.typography.labelMedium)
        }
    }

    // Temporal picker sheet (ADR-043)
    if (timeSheet) {
        ModalBottomSheet(
            onDismissRequest = { timeSheet = false },
            shape = RoundedCornerShape(topStart = FioRadius.lg, topEnd = FioRadius.lg),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FioSpace.s4, vertical = FioSpace.s5)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(FioSpace.s2),
            ) {
                Text("Quando isso pode voltar?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(FioSpace.s2))
                TimeOption("Algum dia", policy is ReturnPolicy.Someday) {
                    policy = ReturnPolicy.Someday; timeSheet = false
                }
                TimeOption("Daqui a 1 semana", (policy as? ReturnPolicy.InPeriod)?.days == 7) {
                    policy = ReturnPolicy.InPeriod(7); timeSheet = false
                }
                TimeOption("Daqui a 1 mês", (policy as? ReturnPolicy.InPeriod)?.days == 30) {
                    policy = ReturnPolicy.InPeriod(30); timeSheet = false
                }
                TimeOption("Daqui a 3 meses", (policy as? ReturnPolicy.InPeriod)?.days == 90) {
                    policy = ReturnPolicy.InPeriod(90); timeSheet = false
                }
                TimeOption("Daqui a 1 ano", (policy as? ReturnPolicy.InPeriod)?.days == 365) {
                    policy = ReturnPolicy.InPeriod(365); timeSheet = false
                }
                TimeOption("Escolher uma data", policy is ReturnPolicy.OnDate) {
                    timeSheet = false; dateSheet = true
                }
                TimeOption("Nunca", policy is ReturnPolicy.Never) {
                    policy = ReturnPolicy.Never; timeSheet = false
                }
                Spacer(Modifier.height(FioSpace.s2))
            }
        }
    }

    // Date picker dialog (anchored request)
    if (dateSheet) {
        val datePickerState = rememberDatePickerState(
            // G1 fix: anchor the picker in the device's local zone — UTC
            // conversion shows the previous day for negative-offset zones
            // after 21:00 local time.
            initialSelectedDateMillis = (policy as? ReturnPolicy.OnDate)
                ?.date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                ?: LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { dateSheet = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // G1 fix: decode the chosen millis in the device's
                        // local zone so the touched calendar day is honored.
                        datePickerState.selectedDateMillis?.let { millis ->
                            policy = ReturnPolicy.OnDate(
                                Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate(),
                            )
                        }
                        dateSheet = false
                    },
                ) { Text("Escolher") }
            },
            dismissButton = { TextButton(onClick = { dateSheet = false }) { Text("Cancelar") } },
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        "Escolher uma data",
                        modifier = Modifier.padding(start = FioSpace.s5, top = FioSpace.s4),
                    )
                },
                headline = {
                    val selected = datePickerState.selectedDateMillis?.let { millis ->
                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    Text(
                        selected?.format(Formatter.ptDate) ?: "Escolha um dia",
                        modifier = Modifier.padding(horizontal = FioSpace.s5, vertical = FioSpace.s3),
                    )
                },
            )
        }
    }
}


@Composable
private fun TimeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp),
        shape = RoundedCornerShape(FioRadius.md),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FioSpace.s3, vertical = FioSpace.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(18.dp).width(18.dp),
                )
                Spacer(Modifier.width(FioSpace.s2))
            }
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun returnPolicyLabel(days: Int): String = when (days) {
    7 -> "Daqui a 1 semana"
    30 -> "Daqui a 1 mês"
    90 -> "Daqui a 3 meses"
    365 -> "Daqui a 1 ano"
    else -> "Daqui a $days dias"
}

// ===========================================================================
// FIND — deliberate retrieval has its own quiet surface. It never shares the
// chronological Archive list and never affects Returns.
// ===========================================================================
