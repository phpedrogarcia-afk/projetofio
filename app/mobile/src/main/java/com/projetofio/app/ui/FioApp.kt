package com.projetofio.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.platform.LocalContext
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
import com.projetofio.app.domain.ReturnedFilter
import com.projetofio.app.domain.SearchQuery
import com.projetofio.app.domain.SearchTimeFilter
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

private enum class MainSurface { SAVE, FIND, ARCHIVE, SETTINGS }

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
    var surface by remember { mutableStateOf(MainSurface.SAVE) }
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
        bottomBar = {
            if (surface != MainSurface.SETTINGS) {
                FioPrimaryNavigation(
                    selected = surface,
                    onSelect = { destination ->
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        surface = destination
                    },
                )
            }
        },
    ) { padding ->
        when (surface) {
            MainSurface.SAVE -> HomeScreen(
                state = state,
                viewModel = viewModel,
                padding = padding,
                onOpenSettings = { surface = MainSurface.SETTINGS },
            )
            MainSurface.FIND -> SearchScreen(
                state = state,
                viewModel = viewModel,
                padding = padding,
                onOpenSettings = { surface = MainSurface.SETTINGS },
            )
            MainSurface.ARCHIVE -> ArchiveScreen(
                state = state,
                viewModel = viewModel,
                padding = padding,
                onOpenSettings = { surface = MainSurface.SETTINGS },
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
                onBack = { surface = MainSurface.SAVE },
                onEnableReturns = onEnableReturns,
                onOpenPendingReturn = onOpenPendingReturn,
                onSelectImport = onSelectImport,
            )
        }
    }
}

@Composable
private fun FioPrimaryNavigation(
    selected: MainSurface,
    onSelect: (MainSurface) -> Unit,
) {
    val destinations = listOf(
        Triple(MainSurface.SAVE, "Guardar", R.drawable.ic_write),
        Triple(MainSurface.FIND, "Encontrar", R.drawable.ic_find),
        Triple(MainSurface.ARCHIVE, "Arquivo", R.drawable.ic_archive),
    )
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            destinations.forEach { (destination, label, icon) ->
                NavigationBarItem(
                    selected = selected == destination,
                    onClick = { onSelect(destination) },
                    icon = {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                        )
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier
                        .heightIn(min = 64.dp)
                        .semantics { contentDescription = "Abrir $label" },
                )
            }
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
    onOpenSettings: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
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
                    contentDescription = "Tela Guardar"
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
            // G1 fix: anchor the picker in the device's local zone — UTC
            // conversion shows the previous day for negative-offset zones
            // after 21:00 local time.
            initialSelectedDateMillis = (policy as? ReturnPolicy.OnDate)
                ?.date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
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
// FIND — deliberate retrieval has its own quiet surface. It never shares the
// chronological Archive list and never affects Returns.
// ===========================================================================

@Composable
private fun SearchScreen(
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

// ===========================================================================
// ARCHIVE — typographic grouping by month, temporal distance as secondary
// line, no cards (the system's rule: cards only for elevated objects).
// ===========================================================================

@Composable
private fun ArchiveScreen(
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
                text = when (state.entries.size) {
                    0 -> "Suas palavras, em ordem do tempo."
                    1 -> "1 lembrança guardada. Toque para ler, editar ou excluir."
                    else -> "${state.entries.size} lembranças guardadas. Toque em uma para ler, editar ou excluir."
                },
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
        } else {
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
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                }
            }
        }
        item { Spacer(Modifier.height(FioSpace.s5)) }
    }
}

// ===========================================================================
// FIND — search field and factual result panel (Encontrar).
// ===========================================================================

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
            // Label comes from the placeholder (read once by TalkBack);
            // avoid a duplicated contentDescription on the field itself.
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
                // Announce each hit factually for TalkBack: date + snippet, no
                // interpretation. The live-region updates flow through the
                // results container so results refresh without stealing focus.
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
    SelectionContainer {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(vertical = FioSpace.s3)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Abrir lembrança de ${displayDate(entry)} para ler ou editar. ${entry.content}"
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            }
            Text(
                "›",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = FioSpace.s3),
            )
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
// SETTINGS — a short map first, one understandable subject at a time.
// ===========================================================================

private enum class SettingsPage { OVERVIEW, PRIVACY, RETURNS, IMPORT, EXPORT, DELETED }

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
        item { SectionTitle("Privacidade") }
        item {
            SettingsNavigationRow(
                title = "Proteção ao abrir",
                summary = appLockSummary(state.settings.appLockMode),
                onClick = { onOpen(SettingsPage.PRIVACY) },
            )
        }
        if (state.m2EngineeringEnabled) {
            item { SectionTitle("Lembranças") }
            item {
                SettingsNavigationRow(
                    title = "Lembranças que voltam",
                    summary = returnsSummary(state.settings.returnConsentState),
                    onClick = { onOpen(SettingsPage.RETURNS) },
                )
            }
        }
        item { SectionTitle("Seus dados") }
        if (state.m3EngineeringEnabled) {
            item {
                SettingsNavigationRow(
                    title = "Importar notas",
                    summary = "Trazer textos e datas de um arquivo TXT ou Markdown.",
                    onClick = { onOpen(SettingsPage.IMPORT) },
                )
            }
        }
        item {
            SettingsNavigationRow(
                title = "Exportar uma cópia",
                summary = "Guardar todas as suas notas em um arquivo legível.",
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
                onClick = { onOpen(SettingsPage.DELETED) },
            )
        }
        item {
            Text(
                buildString {
                    append("Nesta versão, suas notas ficam neste aparelho. Não há conta, sincronização ou envio das suas palavras para análise de uso.")
                    if (!state.m2EngineeringEnabled) append(" O recurso de fazer lembranças antigas voltarem ainda não está ativo neste aplicativo instalado.")
                    if (!state.m3EngineeringEnabled) append(" A importação de arquivos ainda não está disponível aqui.")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = FioSpace.s5),
            )
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
                "Se estiver em dúvida, escolha Texto (.txt): é o formato mais simples e abre em quase qualquer aplicativo. " +
                    "Markdown (.md) preserva melhor títulos e datas para levar as notas a outro aplicativo compatível.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(FioSpace.s2)) {
                OutlinedButton(
                    onClick = onExportText,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Criar arquivo de texto (.txt)") }
                OutlinedButton(
                    onClick = onExportMarkdown,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Criar arquivo Markdown (.md)") }
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
private fun SettingsNavigationRow(title: String, summary: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 72.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$title. $summary" },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(FioRadius.md),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FioSpace.s3, vertical = FioSpace.s3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
private fun ReturnScreen(entry: Entry, onClose: () -> Unit, onNeverReturn: () -> Unit) {
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
    // ADR-045: the motif is purely decorative — Reduce Motion is respected,
    // and any user who asked for no motion sees no patina at all.
    // G9 fix: suppress the motif for reduced motion OR TalkBack.
    if (isMotionReduced(LocalContext.current)) return
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
// Accessibility helpers
// ===========================================================================

// G9 fix: returns true when motion should be suppressed — either the user
// asked for reduced motion (system animation scale disabled) or TalkBack is
// active (navigation is by order, not appearance). ADR-045 forbids the motif
// from drawing anything in either case. The old helper only read
// isTouchExplorationEnabled and was misnamed `reduceMotion`.
private fun isMotionReduced(context: android.content.Context): Boolean =
    runCatching {
        val settings = android.provider.Settings.Global.getFloat(
            context.contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        val reducedMotion = settings <= 0f
        val service = context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager
        val talkBack = service?.isEnabled == true && service.isTouchExplorationEnabled == true
        reducedMotion || talkBack
    }.getOrDefault(false)

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
