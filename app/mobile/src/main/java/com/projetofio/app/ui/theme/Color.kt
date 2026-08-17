package com.projetofio.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Fio "Verde-Sálvia" design system tokens.
// The six legacy tokens remain as aliases so existing call sites keep
// compiling after this expansion.
// ---------------------------------------------------------------------------

// Light mode
internal val LightBackground = Color(0xFFF8F4EA) // marfim quente (legacy WarmPaper)
internal val LightSurface = Color(0xFFFBF8F0) // superfícies elevadas
internal val LightSurfaceVariant = Color(0xFFF0EBDE) // terciária, hover, fundo de chips
internal val LightPrimary = Color(0xFF667A66) // verde sálvia (legacy Sage)
internal val LightOnPrimary = Color(0xFFFAF7EE)
internal val LightSecondary = Color(0xFF8CA38A)
internal val LightOnSecondary = Color(0xFFFBF8F0)
internal val LightTertiary = Color(0xFFB0BFA6) // periféricos, botânicos, placeholders
internal val LightOutline = Color(0xFFDCD5C4)
internal val LightOutlineVariant = Color(0xFFEAE4D4) // dividers de baixa presença
internal val LightOnBackground = Color(0xFF252A25) // texto principal (legacy Charcoal)
internal val LightOnSurface = Color(0xFF252A25)
internal val LightOnSurfaceVariant = Color(0xFF5A6158) // captions, datas
internal val LightError = Color(0xFFA8543F) // terracota terroso, nunca vermelho puro
internal val LightOnError = Color(0xFFFBF7F2)
internal val LightSuccess = Color(0xFF5E7A5A) // estados positivos acessíveis

// Dark mode — never a pure inversion: deep charcoal-green with lighter sage.
internal val DarkBackground = Color(0xFF1E2320) // verde-carvão (legacy WarmPaperDark)
internal val DarkSurface = Color(0xFF262C28)
internal val DarkSurfaceVariant = Color(0xFF303732)
internal val DarkPrimary = Color(0xFFA8B9A0) // sálvia claro
internal val DarkOnPrimary = Color(0xFF1E2320)
internal val DarkSecondary = Color(0xFF859980)
internal val DarkOutline = Color(0xFF404A42)
internal val DarkOutlineVariant = Color(0xFF333B36)
internal val DarkOnBackground = Color(0xFFEFEFE9)
internal val DarkOnSurface = Color(0xFFEFEFE9)
internal val DarkOnSurfaceVariant = Color(0xFFB9BDB2)
internal val DarkError = Color(0xFFC87B63) // terracota claro
internal val DarkOnError = Color(0xFF2A2220)

// Legacy aliases (keep existing call sites working)
internal val Sage = LightPrimary
internal val SageDark = DarkPrimary
internal val WarmPaper = LightBackground
internal val WarmPaperDark = DarkBackground
internal val Charcoal = LightOnBackground
internal val SoftIvory = LightOnPrimary
