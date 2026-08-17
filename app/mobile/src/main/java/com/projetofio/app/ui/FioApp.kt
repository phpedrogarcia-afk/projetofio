package com.projetofio.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projetofio.app.R
import com.projetofio.app.domain.AppLockMode
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.ReturnMode
import com.projetofio.app.domain.ImportBatch
import com.projetofio.app.domain.ImportIssueCode
import com.projetofio.app.domain.NotificationPermissionObserved
import com.projetofio.app.domain.ReturnConsentState
import com.projetofio.app.ui.theme.FioDisplayDate
import com.projetofio.app.ui.theme.FioRadius
import com.projetofio.app.ui.theme.FioSpace
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Return policy chosen per entry in the write flow. ADR-043: "Algum dia"
// keeps the entry organically eligible; a period/date becomes an explicit
// return request ("Escolher uma data" / period); "Nunca" is never-return.
// In this V0 build the engine is not yet in main; the policy is written
// into the Entry record so the future engine (PR #1) honours it as a
// first-class candidate.
// ---------------------------------------------------------------------------
sealed class ReturnPolicy {
    data object Someday : ReturnPolicy() // organic eligibility
    data class InPeriod(val days: Int) : ReturnPolicy() // explicit request
    data class OnDate(val date: LocalDate) : ReturnPolicy() // anchored request
    data object Never : ReturnPolicy()
}

private enum class MainSurface { HOME, ARCHIVE, SETTINGS }

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
    var surface by remember { mutableStateOf(MainSurface.HOME) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isSaved = state.savedNotice
    LaunchedEffect(isSaved) {
        if (isSaved) {
            delay(1500)
            focusManager.clearFocus()
            keyboardController?.hide()
            viewModel.acknowledgeSaved()
        }
    }

    state.openedReturn?.let { opened ->
        ReturnScreen(
            entry = opened.entry,
            onClose = viewModel::closeReturn,
            onNeverReturn = viewModel::neverReturnOpened,
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (surface) {
            MainSurface.HOME -> HomeScreen(
                state = state,
                viewModel = viewModel,
                padding = padding,
                onOpenArchive = { surface = MainSurface.ARCHIVE },
                onOpenSettings = { surface = MainSurface.SETTINGS },
            )
            MainSurface.ARCHIVE -> ArchiveScreen(
                state = state,
                viewModel = viewModel,
                padding = padding,
                onBack = { surface = MainSurface.HOME },
            )
            MainSurface.SETTINGS -> SettingsScreen(
                state = state,
                viewModel = viewModel,
                padding = padding,
                authenticationAvailable = authenticationAvailable,
                exportMessage = exportMessage,
                authorizeFresh = authorizeFresh,
                onExportMarkdown = onExportMarkdown,
                onExportText = onExportText,
                onBack = { surface = MainSurface.HOME },
                onEnableReturns = onEnableReturns,
                onOpenPendingReturn = onOpenPendingReturn,
                onSelectImport = onSelectImport,
            )
        }
    }
}

// ===========================================================================
// HOME — the product is writing. Everything else stays out of the way.
// ===========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: FioUiState,
    viewModel: FioViewModel,
    padding: PaddingValues,
    onOpenArchive: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var menuOpen by remember { mutableStateOf(false) }
    var timeSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf(false) }
    var policy by remember { mutableStateOf<ReturnPolicy>(ReturnPolicy.Someday) }

    // First-capsule copy (ADR-044): the single extended confirmation appears
    // only for the very first save of the life of this installation.
    val isFirstSave =
        state.settings.returnConsentState == ReturnConsentState.NOT_CONFIGURED &&
            state.entries.isEmpty() && state.savedNotice
    val saveCopy = if (isFirstSave) "Guardado. O tempo cuida do resto." else "Guardado."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = FioSpace.s6, vertical = FioSpace.s5),
    ) {
        // Top row: brand + overflow menu (ADR-004: archive/settings
        // discoverable, never equal to writing)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Fio",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.width(FioSpace.s2))
            Box {
                Text(
                    text = "⋯",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = { menuOpen = true })
                        .heightIn(min = 48.dp)
                        .widthIn(min = 48.dp)
                        .padding(12.dp)
                        .semantics { contentDescription = "Mais opções" },
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Arquivo") },
                        onClick = { menuOpen = false; onOpenArchive() },
                    )
                    DropdownMenuItem(
                        text = { Text("Configurações") },
                        onClick = { menuOpen = false; onOpenSettings() },
                    )
                }
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

        // The writing surface: no container, no visible border. The user's
        // words are the darkest, most present element on screen.
        val interactionSource = remember { MutableInteractionSource() }
        BasicTextField(
            value = state.draftText,
            onValueChange = viewModel::onDraftChanged,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics(mergeDescendants = true) {},
            decorationBox = { inner ->
                Box {
                    if (state.draftText.isEmpty()) {
                        Text(
                            "Escreva quando quiser.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    inner()
                }
            },
        )

        // Temporal policy chip (shows the chosen return policy)
        AnimatedVisibility(visible = state.draftText.isNotBlank()) {
            val policyLabel = when (val p = policy) {
                ReturnPolicy.Someday -> "Algum dia"
                is ReturnPolicy.InPeriod -> returnPolicyLabel(p.days)
                is ReturnPolicy.OnDate -> "Em ${p.date.format(Formatter.ptDate)}"
                ReturnPolicy.Never -> "Nunca"
            }
            Row(
                modifier = Modifier
                    .padding(top = FioSpace.s3)
                    .clickable(onClick = { timeSheet = true })
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(FioRadius.md),
                    )
                    .padding(horizontal = FioSpace.s3, vertical = FioSpace.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = null,
                    modifier = Modifier.height(16.dp).width(16.dp),
                )
                Spacer(Modifier.width(FioSpace.s1))
                Text(
                    text = "Quando isso pode voltar? · $policyLabel",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { contentDescription = "Quando isso pode voltar? Escolhido: $policyLabel" },
                )
            }
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
                .alpha(if (pressed) 0.92f else 1f),
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
                Text(
                    "O Fio decide o momento exato. Sua escolha apenas guia o que pode voltar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(FioSpace.s3))
                TimeOption("Algum dia", policy is ReturnPolicy.Someday) {
                    policy = ReturnPolicy.Someday; timeSheet = false
                }
                TimeOption("Em 7 dias", (policy as? ReturnPolicy.InPeriod)?.days == 7) {
                    policy = ReturnPolicy.InPeriod(7); timeSheet = false
                }
                TimeOption("Em 30 dias", (policy as? ReturnPolicy.InPeriod)?.days == 30) {
                    policy = ReturnPolicy.InPeriod(30); timeSheet = false
                }
                TimeOption("Em 90 dias", (policy as? ReturnPolicy.InPeriod)?.days == 90) {
                    policy = ReturnPolicy.InPeriod(90); timeSheet = false
                }
                TimeOption("Em 1 ano", (policy as? ReturnPolicy.InPeriod)?.days == 365) {
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
            initialSelectedDateMillis = (policy as? ReturnPolicy.OnDate)
                ?.date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { dateSheet = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            policy = ReturnPolicy.OnDate(
                                Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(),
                            )
                        }
                        dateSheet = false
                    },
                ) { Text("Escolher") }
            },
            dismissButton = { TextButton(onClick = { dateSheet = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = datePickerState)
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
    7 -> "Em 7 dias"
    30 -> "Em 30 dias"
    90 -> "Em 90 dias"
    365 -> "Em 1 ano"
    else -> "Em $days dias"
}

// ===========================================================================
// ARCHIVE — typographic grouping by month, temporal distance as secondary
// line, no cards (the system's rule: cards only for elevated objects).
// ===========================================================================

@Composable
private fun ArchiveScreen(
    state: FioUiState,
    viewModel: FioViewModel,
    padding: PaddingValues,
    onBack: () -> Unit,
) {
    var reading by remember { mutableStateOf<Entry?>(null) }
    var editing by remember { mutableStateOf<Entry?>(null) }
    var deleting by remember { mutableStateOf<Entry?>(null) }

    reading?.let { entry ->
        NoteScreen(
            entry = entry,
            onBack = { reading = null },
            onReedit = { text -> viewModel.onDraftChanged(text) },
        )
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
                Text("Arquivo", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            }
        }
        item { Spacer(Modifier.height(FioSpace.s3)) }
        if (state.loading) item { CircularProgressIndicator() }
        if (state.archiveError) {
            item { Text("O Arquivo não pôde ser aberto com segurança. Nada foi apagado.", color = MaterialTheme.colorScheme.error) }
        } else if (!state.loading && state.entries.isEmpty()) {
            item {
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
        val grouped = state.entries.groupBy { groupKey(it) }
        grouped.forEach { (label, entries) ->
            item {
                Column(modifier = Modifier.padding(top = FioSpace.s4, bottom = FioSpace.s2)) {
                    Text(label, style = MaterialTheme.typography.titleMedium)
                }
            }
            items(entries, key = { it.id }) { entry ->
                ArchiveRow(
                    entry = entry,
                    onOpen = { reading = entry },
                    onEdit = { editing = entry },
                    onDelete = { deleting = entry },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
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
private fun ArchiveRow(entry: Entry, onOpen: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(vertical = FioSpace.s3),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    displayDate(entry),
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
            Row(modifier = Modifier.padding(top = FioSpace.s2), horizontalArrangement = Arrangement.spacedBy(FioSpace.s2)) {
                TextButton(onClick = onEdit, modifier = Modifier.heightIn(min = 40.dp)) {
                    Text("Editar", fontSize = 13.sp)
                }
                TextButton(onClick = onDelete, modifier = Modifier.heightIn(min = 40.dp)) {
                    Text("Excluir", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ===========================================================================
// NOTE — reading an entry is a letter, not a row. Serif date, calm body.
// ===========================================================================

@Composable
private fun NoteScreen(
    entry: Entry,
    onBack: () -> Unit,
    onReedit: (String) -> Unit = {},
) {
    var returning by remember { mutableStateOf(false) }
    var returned by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
            Row {
                Text(
                    text = if (returned) "Reescrita com o olhar de hoje?" else "Devolver para agora",
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
                    text = "Suas palavras voltaram. Reescreva com o olhar de hoje, se quiser — ou apenas deixe seguir.",
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
        confirmButton = {
            TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) {
                Text("Guardar alterações")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

// ===========================================================================
// SETTINGS — same content, new system.
// ===========================================================================

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
    onBack: () -> Unit,
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
        contentPadding = PaddingValues(horizontal = FioSpace.s4, vertical = FioSpace.s5),
        verticalArrangement = Arrangement.spacedBy(FioSpace.s2),
    ) {
        item {
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
                Text("Configurações", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            }
        }
        item { Spacer(Modifier.height(FioSpace.s3)) }
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
        item { HorizontalDivider(modifier = Modifier.padding(vertical = FioSpace.s2)) }
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
            Row(horizontalArrangement = Arrangement.spacedBy(FioSpace.s2)) {
                OutlinedButton(onClick = onExportMarkdown) { Text("Markdown") }
                OutlinedButton(onClick = onExportText) { Text("Texto") }
            }
        }
        exportMessage?.let { message ->
            item { Text(message, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = FioSpace.s2)) }
        item { SectionTitle("Excluídos recentemente") }
        if (state.deletedEntries.isEmpty()) {
            item { Text("Nenhuma entrada excluída.") }
        }
        items(state.deletedEntries, key = { "deleted-${it.id}" }) { entry ->
            DeletedRow(
                entry = entry,
                onRecover = { viewModel.recoverEntry(entry.id) },
                onPurge = { permanentDelete = entry },
            )
        }
        item { Spacer(Modifier.height(FioSpace.s5)) }
        item {
            Text(
                "M1 local: sem conta, sincronização, analytics ou devoluções ativas.",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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


private fun quietHoursLabel(startMinute: Int, endMinute: Int): String {
    fun fmt(m: Int): String = "${(m / 60) % 24}h${(m % 60).let { if (it == 0) "00" else String.format(Locale.ROOT, "%02d", it) }}"
    return "${fmt(startMinute)}–${fmt(endMinute)}"
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
private fun ReturnScreen(entry: Entry, onClose: () -> Unit, onNeverReturn: () -> Unit) {
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
                onClick = onNeverReturn,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text("Não mostrar novamente") }
        }
    }
}

@Composable
private fun AppLockChoices(selected: AppLockMode, enabled: Boolean, onChoose: (AppLockMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(FioSpace.s1), modifier = Modifier.padding(top = FioSpace.s2)) {
        listOf(
            AppLockMode.OFF to "Desativado",
            AppLockMode.IMMEDIATE to "Imediato",
            AppLockMode.ONE_MINUTE to "Após 1 minuto",
            AppLockMode.FIVE_MINUTES to "Após 5 minutos",
        ).forEach { (mode, label) ->
            val isSelected = mode == selected
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled && !isSelected) { onChoose(mode) }
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(FioRadius.md),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
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

// ===========================================================================
// Botanical motif — the thread with two small leaves. Decorative only,
// TalkBack ignores it, never animated in a loop. ADR-045 reserves the
// maturing motif for a later gated slice.
// ===========================================================================

// Patina Temporal (ADR-045): deterministic botanical growth tied to the
// diary's age. Purely decorative, no metrics, no streak — the motif just
// quietly matures with the life of the installation. Removable via the
// PATINA_ENABLED flag.
private const val PATINA_ENABLED = true
@Composable
private fun BotanicalMotif(firstEntryAt: Instant? = null) {
    if (!PATINA_ENABLED) return
    val tertiary = MaterialTheme.colorScheme.tertiary
    val accent = MaterialTheme.colorScheme.secondary
    // Age in days since the first saved entry; deterministic seed from the
    // diary's own life, not from the calendar — never a streak, just patina.
    val ageDays = if (firstEntryAt != null) {
        ChronoUnit.DAYS.between(firstEntryAt, Instant.now()).coerceAtLeast(0).toInt()
    } else 0
    Canvas(
        modifier = Modifier.height(56.dp).width(32.dp).alpha(0.55f),
        onDraw = {
            drawLine(tertiary, start = Offset(16f, 56f), end = Offset(16f, 12f - ageDays.coerceAtMost(30) * 0.15f), strokeWidth = 2.2f)
            if (ageDays >= 7) {
                drawLine(tertiary, start = Offset(16f, 42f), end = Offset(6f, 34f), strokeWidth = 1.8f)
                drawCircle(accent, radius = 3.5f, center = Offset(6f, 34f))
            }
            if (ageDays >= 30) {
                drawLine(tertiary, start = Offset(16f, 30f), end = Offset(26f, 22f), strokeWidth = 1.8f)
                drawCircle(accent, radius = 3.5f, center = Offset(26f, 22f))
            }
            if (ageDays >= 180) {
                drawCircle(accent, radius = 4.5f, center = Offset(16f, 11f))
            }
        },
    )
}

// ===========================================================================
// Global screens
// ===========================================================================

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

// ---------------------------------------------------------------------------
// Date formatting shared by archive rows (medium date) and notes (long date).
// ---------------------------------------------------------------------------

private object Formatter {
    val ptDate: DateTimeFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
}

private fun displayDate(entry: Entry): String {
    val zone = runCatching { ZoneId.of(entry.originalTimeZone ?: "UTC") }.getOrDefault(ZoneId.of("UTC"))
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale.forLanguageTag("pt-BR"))
    return formatter.format(entry.originalCreatedAt.atZone(zone))
}
