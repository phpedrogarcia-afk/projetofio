package com.projetofio.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.material3.DatePickerDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.DrawableRes
import com.projetofio.app.domain.ReturnConsentState
import com.projetofio.app.domain.ReturnPolicy
import com.projetofio.app.R
import com.projetofio.app.ui.theme.FioRadius
import com.projetofio.app.ui.theme.FioSpace
import com.projetofio.app.ui.theme.FioThemeContext
import com.projetofio.app.ui.theme.cosmic.CosmicOrnament
import com.projetofio.app.ui.theme.cosmic.CosmicOrnament as CosmicOrnamentType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// ReturnPolicy lives in com.projetofio.app.domain.ReturnPolicy (FIO-P19 A1).
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
    val cosmic = FioThemeContext.current.isCosmic

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
    Box(modifier = Modifier.fillMaxSize()) {
        if (cosmic) {
            Image(
                painter = painterResource(R.drawable.cosmic_home_nebula),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(.20f),
            )
            Image(
                painter = painterResource(R.drawable.cosmic_home_constellations),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 2.dp)
                    .width(210.dp)
                    .height(175.dp)
                    .alpha(.10f),
            )
            Image(
                painter = painterResource(R.drawable.cosmic_home_astrolabe),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 2.dp, bottom = 62.dp)
                    .width(155.dp)
                    .height(155.dp)
                    .alpha(.10f),
            )
            Image(
                painter = painterResource(R.drawable.cosmic_home_observatory),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 58.dp)
                    .width(168.dp)
                    .height(168.dp)
                    .alpha(.08f),
            )
        }
        CosmicOrnament(
            ornament = CosmicOrnamentType.COMPASS,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 2.dp, bottom = 72.dp)
                .height(170.dp)
                .width(170.dp),
            opacity = .28f,
        )
        Column(modifier = screenModifier) {
        // Top row: writing keeps the strongest brand treatment; only the
        // secondary Settings action stays outside primary navigation.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Fio",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics {
                        heading()
                        contentDescription = "Tela Escrever"
                    },
                )
                if (cosmic) {
                    Text(
                        text = "SEUS PENSAMENTOS. GUARDADOS NO TEMPO.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.6.sp,
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
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

        Spacer(Modifier.height(if (cosmic) FioSpace.s5 else FioSpace.s6))

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
                .border(1.dp, if (cosmic) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(FioRadius.md)),
            color = MaterialTheme.colorScheme.surface.copy(alpha = if (cosmic) 0.65f else 0.48f),
            shape = RoundedCornerShape(FioRadius.md),
            shadowElevation = if (cosmic) 6.dp else 0.dp,
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
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = if (cosmic) .58f else 0f),
                    RoundedCornerShape(FioRadius.md),
                )
                .border(1.dp, if (cosmic) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(FioRadius.md))
                .testTag("fio-return-policy")
                .padding(horizontal = FioSpace.s3, vertical = FioSpace.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_time_return),
                contentDescription = null,
                tint = if (cosmic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = if (cosmic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
        // FIO-P19 A1: passes the chosen policy to the ViewModel so it reaches the service.
        val pressed by interactionSource.collectIsPressedAsState()
        Button(
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                viewModel.saveEntry(policy)
            },
            enabled = state.draftText.isNotBlank() && !state.saving,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(FioRadius.lg),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (cosmic) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                contentColor = if (cosmic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f),
            ),
            border = if (cosmic) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
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
    }

    // Floating "Guardado." feedback (ADR-014: immediate, restrained, gone in 1.5s)
    AnimatedVisibility(
        visible = state.savedNotice,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        if (cosmic) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(FioSpace.s5)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            RoundedCornerShape(FioRadius.lg),
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(FioRadius.lg))
                        .padding(horizontal = FioSpace.s5, vertical = FioSpace.s6),
                ) {
                    Box(modifier = Modifier.height(130.dp).width(130.dp)) {
                        CosmicOrnament(CosmicOrnamentType.MANDALA, opacity = 0.70f)
                    }
                    Spacer(Modifier.height(FioSpace.s4))
                    Text(
                        "Guardado",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(FioSpace.s2))
                    Text(
                        "Seu pensamento foi guardado no tempo com cuidado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        } else {
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
    }


    // Temporal picker sheet (ADR-043)
    if (timeSheet) {
        ModalBottomSheet(
            onDismissRequest = { timeSheet = false },
            shape = RoundedCornerShape(topStart = FioRadius.lg, topEnd = FioRadius.lg),
            containerColor = if (cosmic) Color.Transparent else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (cosmic) Brush.verticalGradient(listOf(
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = .94f),
                            MaterialTheme.colorScheme.surface.copy(alpha = .90f),
                        )) else Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)),
                        RoundedCornerShape(topStart = FioRadius.lg, topEnd = FioRadius.lg),
                    )
                    .border(1.dp, if (cosmic) MaterialTheme.colorScheme.outline else Color.Transparent, RoundedCornerShape(topStart = FioRadius.lg, topEnd = FioRadius.lg)),
            ) {
                CosmicOrnament(
                    ornament = CosmicOrnamentType.ASTROLABE,
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 10.dp).width(126.dp).height(126.dp),
                    opacity = .10f,
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = FioSpace.s4, vertical = FioSpace.s5).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(FioSpace.s2),
                ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_time_return), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(20.dp).width(20.dp))
                    Spacer(Modifier.width(FioSpace.s2))
                    Text("Quando isso pode voltar?", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(FioSpace.s2))
                TimeOption("Algum dia", R.drawable.ic_infinity, policy is ReturnPolicy.Someday) {
                    policy = ReturnPolicy.Someday; timeSheet = false
                }
                // FIO-P19 A1: labels changed from "Daqui a" to "A partir de" to reflect the
                // 7-day window semantics (opportunity, not exact delivery).
                TimeOption(
                    label = "A partir de 1 semana",
                    sublabel = "Durante os 7 dias seguintes, esta nota pode voltar.",
                    icon = R.drawable.ic_calendar_span,
                    selected = (policy as? ReturnPolicy.InPeriod)?.days == 7,
                ) { policy = ReturnPolicy.InPeriod(7); timeSheet = false }
                TimeOption(
                    label = "A partir de 1 mês",
                    sublabel = "Durante os 7 dias seguintes, esta nota pode voltar.",
                    icon = R.drawable.ic_calendar_span,
                    selected = (policy as? ReturnPolicy.InPeriod)?.days == 30,
                ) { policy = ReturnPolicy.InPeriod(30); timeSheet = false }
                TimeOption(
                    label = "A partir de 3 meses",
                    sublabel = "Durante os 7 dias seguintes, esta nota pode voltar.",
                    icon = R.drawable.ic_calendar_span,
                    selected = (policy as? ReturnPolicy.InPeriod)?.days == 90,
                ) { policy = ReturnPolicy.InPeriod(90); timeSheet = false }
                TimeOption(
                    label = "A partir de 1 ano",
                    sublabel = "Durante os 7 dias seguintes, esta nota pode voltar.",
                    icon = R.drawable.ic_calendar_span,
                    selected = (policy as? ReturnPolicy.InPeriod)?.days == 365,
                ) { policy = ReturnPolicy.InPeriod(365); timeSheet = false }
                TimeOption(
                    label = "Escolher uma data",
                    sublabel = "Durante os 7 dias a partir da data, esta nota pode voltar.",
                    icon = R.drawable.ic_calendar_date,
                    selected = policy is ReturnPolicy.OnDate,
                ) { timeSheet = false; dateSheet = true }
                TimeOption("Nunca", R.drawable.ic_never, policy is ReturnPolicy.Never) {
                    policy = ReturnPolicy.Never; timeSheet = false
                }
                Spacer(Modifier.height(FioSpace.s2))
                }
            }
        }
    }

    // Date picker dialog (anchored request)
    if (dateSheet) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (policy as? ReturnPolicy.OnDate)
                ?.date?.let(::datePickerMillis)
                ?: datePickerMillis(LocalDate.now()),
        )
        DatePickerDialog(
            onDismissRequest = { dateSheet = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            policy = ReturnPolicy.OnDate(datePickerDate(millis))
                        }
                        dateSheet = false
                    },
                ) { Text("Escolher") }
            },
            dismissButton = { TextButton(onClick = { dateSheet = false }) { Text("Cancelar") } },
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = FioSpace.s5, vertical = FioSpace.s4),
                ) {
                    Text(
                        "Escolher uma data",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val selected = datePickerState.selectedDateMillis?.let { millis ->
                        datePickerDate(millis)
                    }
                    Text(
                        selected?.format(Formatter.ptDate) ?: "Escolha um dia",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(top = FioSpace.s1)
                            .testTag("fio-date-picker-headline"),
                    )
                }
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    title = null,
                    headline = null,
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        headlineContentColor = MaterialTheme.colorScheme.onSurface,
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                        todayContentColor = MaterialTheme.colorScheme.primary,
                        todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }
    }
}


@Composable
private fun TimeOption(
    label: String,
    @DrawableRes icon: Int,
    selected: Boolean,
    sublabel: String? = null,
    onClick: () -> Unit,
) {
    val cosmic = FioThemeContext.current.isCosmic
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 54.dp)
            .semantics { contentDescription = "Definir retorno: $label" },
        shape = RoundedCornerShape(FioRadius.md),
        color = when {
            cosmic && selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = .50f)
            cosmic -> MaterialTheme.colorScheme.surface.copy(alpha = .42f)
            selected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        border = if (cosmic) BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FioSpace.s3, vertical = FioSpace.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.height(20.dp).width(20.dp),
            )
            Spacer(Modifier.width(FioSpace.s3))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                // FIO-P19 A1: window explanation shown only for scheduled options.
                if (sublabel != null) {
                    Text(
                        sublabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    )
                }
            }
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(18.dp).width(18.dp),
                )
            }
        }
    }
}

// FIO-P19 A1: labels reflect delivery-window semantics ("A partir de") instead of exact dates.
private fun returnPolicyLabel(days: Int): String = when (days) {
    7 -> "A partir de 1 semana"
    30 -> "A partir de 1 mês"
    90 -> "A partir de 3 meses"
    365 -> "A partir de 1 ano"
    else -> "A partir de $days dias"
}

/**
 * Material DatePicker represents calendar dates as midnight UTC. Keeping the
 * conversion in that calendar domain prevents the header, selected cell and
 * confirmed value from drifting with the device time zone.
 */
internal fun datePickerMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun datePickerDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

// ===========================================================================
// FIND — deliberate retrieval has its own quiet surface. It never shares the
// chronological Archive list and never affects Returns.
// ===========================================================================
