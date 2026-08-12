package com.projetofio.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Sage,
    onPrimary = SoftIvory,
    background = WarmPaper,
    onBackground = Charcoal,
    surface = WarmPaper,
    onSurface = Charcoal,
)

private val DarkColors = darkColorScheme(
    primary = SageDark,
    background = WarmPaperDark,
    surface = WarmPaperDark,
)

@Composable
fun FioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
