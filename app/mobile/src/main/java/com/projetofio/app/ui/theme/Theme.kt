package com.projetofio.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Fio "Verde-Sálvia" color schemes.
// Light: warm ivory paper + sage green.
// Dark: deep charcoal-green + lighter sage (never a pure inversion).
// ---------------------------------------------------------------------------

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightBackground,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerHighest = LightElevatedSurface,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = DarkElevatedSurface,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
)

private val CosmicColors = darkColorScheme(
    primary = CosmicPrimary,
    onPrimary = CosmicOnPrimary,
    primaryContainer = CosmicPrimaryContainer,
    onPrimaryContainer = CosmicOnPrimaryContainer,
    secondary = CosmicSecondary,
    onSecondary = CosmicOnSecondary,
    tertiary = CosmicTertiary,
    onTertiary = CosmicOnTertiary,
    background = CosmicBackground,
    onBackground = CosmicOnBackground,
    surface = CosmicSurface,
    onSurface = CosmicOnSurface,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = CosmicOnSurfaceVariant,
    surfaceContainerLowest = CosmicBackgroundDeep,
    surfaceContainerLow = CosmicSurface,
    surfaceContainer = CosmicSurface,
    surfaceContainerHigh = CosmicSurfaceVariant,
    surfaceContainerHighest = CosmicElevatedSurface,
    outline = CosmicOutline,
    outlineVariant = CosmicOutlineVariant,
    error = CosmicError,
    onError = CosmicOnError,
)

enum class FioVisualTheme {
    SERENO,
    CEU_NOTURNO,
}

class FioThemeProfile internal constructor(
    val visualTheme: FioVisualTheme,
    val isCosmic: Boolean,
    val glassAlpha: Float,
    val ornamentAlpha: Float,
)

private val SerenoProfile = FioThemeProfile(
    visualTheme = FioVisualTheme.SERENO,
    isCosmic = false,
    glassAlpha = 1f,
    ornamentAlpha = 0f,
)

private val CosmicProfile = FioThemeProfile(
    visualTheme = FioVisualTheme.CEU_NOTURNO,
    isCosmic = true,
    glassAlpha = 0.88f,
    ornamentAlpha = 0.24f,
)

private val LocalFioThemeProfile = staticCompositionLocalOf { SerenoProfile }

object FioThemeContext {
    val current: FioThemeProfile
        @Composable
        @ReadOnlyComposable
        get() = LocalFioThemeProfile.current
}

@Composable
fun FioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    visualTheme: FioVisualTheme = FioVisualTheme.SERENO,
    content: @Composable () -> Unit,
) {
    val profile = if (visualTheme == FioVisualTheme.CEU_NOTURNO) CosmicProfile else SerenoProfile
    val colorScheme = when (visualTheme) {
        FioVisualTheme.CEU_NOTURNO -> CosmicColors
        FioVisualTheme.SERENO -> if (darkTheme) DarkColors else LightColors
    }
    val typography = if (profile.isCosmic) CosmicTypography else FioTypography
    CompositionLocalProvider(LocalFioThemeProfile provides profile) {
        MaterialTheme(colorScheme = colorScheme, typography = typography) {
            FioBackdrop(content = content)
        }
    }
}

@Composable
fun fioScreenContainerColor(): Color = if (FioThemeContext.current.isCosmic) {
    Color.Transparent
} else {
    MaterialTheme.colorScheme.background
}

// ---------------------------------------------------------------------------
// Non-Material constants reused by composables (spacing, radius, elevation).
// Base unit: 4 dp; content breathes in multiples of 8, groups in multiples
// of 24. The user's words remain the darkest, most saturated element.
// ---------------------------------------------------------------------------

object FioSpace {
    val s1 = 4.dp
    val s2 = 8.dp
    val s3 = 12.dp
    val s4 = 16.dp
    val s5 = 24.dp
    val s6 = 32.dp
    val s7 = 48.dp
    val s8 = 40.dp
}

object FioRadius {
    val full = 999.dp // pills, badges only
    val lg = 20.dp // primary buttons, bottom sheets
    val md = 12.dp // chips, focused fields
}
