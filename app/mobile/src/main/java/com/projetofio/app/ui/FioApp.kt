package com.projetofio.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projetofio.app.domain.Entry
import com.projetofio.app.R
import com.projetofio.app.ui.theme.fioScreenContainerColor
import kotlinx.coroutines.delay

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
        containerColor = fioScreenContainerColor(),
        contentColor = MaterialTheme.colorScheme.onBackground,
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
        Triple(MainSurface.SAVE, "Escrever", R.drawable.ic_write),
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
