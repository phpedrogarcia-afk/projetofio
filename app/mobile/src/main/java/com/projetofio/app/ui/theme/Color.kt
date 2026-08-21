package com.projetofio.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Fio "Verde-Sálvia" design system tokens.
// The six legacy tokens remain as aliases so existing call sites keep
// compiling after this expansion.
// ---------------------------------------------------------------------------

// Light mode — natural paper, mineral ink and a quieter botanical sage.
internal val LightBackground = Color(0xFFF6F1E7) // papel quente (legacy WarmPaper)
internal val LightSurface = Color(0xFFFBF8F1) // papel interno
internal val LightElevatedSurface = Color(0xFFFFFCF5) // diálogo / folha elevada
internal val LightSurfaceVariant = Color(0xFFEDE9DE) // campos e grupos discretos
internal val LightPrimary = Color(0xFF596C5A) // sálvia mineral (legacy Sage)
internal val LightOnPrimary = Color(0xFFFFFBF3)
internal val LightPrimaryContainer = Color(0xFFDFE7DA) // seleção delicada
internal val LightOnPrimaryContainer = Color(0xFF263329)
internal val LightSecondary = Color(0xFF7D9079)
internal val LightOnSecondary = Color(0xFFFFFBF3)
internal val LightTertiary = Color(0xFF9AAA91) // botânicos e sinais periféricos
internal val LightOnTertiary = Color(0xFF1F2B21)
internal val LightOutline = Color(0xFFD2CCBE)
internal val LightOutlineVariant = Color(0xFFE5DED1) // divisores de baixa presença
internal val LightOnBackground = Color(0xFF242823) // tinta principal (legacy Charcoal)
internal val LightOnSurface = Color(0xFF242823)
internal val LightOnSurfaceVariant = Color(0xFF5B6259) // captions e datas
internal val LightError = Color(0xFF9A5947) // terracota, nunca vermelho puro
internal val LightOnError = Color(0xFFFFF8F3)
internal val LightSuccess = Color(0xFF526D53)

// Dark mode — warm charcoal, never hard black and never a literal inversion.
internal val DarkBackground = Color(0xFF1C211D) // carvão quente (legacy WarmPaperDark)
internal val DarkSurface = Color(0xFF242A25)
internal val DarkElevatedSurface = Color(0xFF2A302B)
internal val DarkSurfaceVariant = Color(0xFF303731)
internal val DarkPrimary = Color(0xFFB2C0A9) // sálvia de baixa luminosidade
internal val DarkOnPrimary = Color(0xFF1F281F)
internal val DarkPrimaryContainer = Color(0xFF39463A)
internal val DarkOnPrimaryContainer = Color(0xFFDCE7D7)
internal val DarkSecondary = Color(0xFF93A58E)
internal val DarkOnSecondary = Color(0xFF182019)
internal val DarkTertiary = Color(0xFF879A82)
internal val DarkOnTertiary = Color(0xFF172018)
internal val DarkOutline = Color(0xFF4B554C)
internal val DarkOutlineVariant = Color(0xFF363E37)
internal val DarkOnBackground = Color(0xFFECEDE6)
internal val DarkOnSurface = Color(0xFFECEDE6)
internal val DarkOnSurfaceVariant = Color(0xFFBDC2B8)
internal val DarkError = Color(0xFFD08A72)
internal val DarkOnError = Color(0xFF2D201C)

// Céu Noturno — nocturnal teal glass, warm parchment and aged gold.
// These are separate tokens: no Sereno value above is changed or aliased.
internal val CosmicBackground = Color(0xFF071D20)
internal val CosmicBackgroundDeep = Color(0xFF041417)
internal val CosmicMistBlue = Color(0xFF2D5961)
internal val CosmicMistWarm = Color(0xFF8B7153)
internal val CosmicSurface = Color(0xC70D282B)
internal val CosmicElevatedSurface = Color(0xD6123033)
internal val CosmicSurfaceVariant = Color(0xC2163538)
internal val CosmicPrimary = Color(0xFFD5B773)
internal val CosmicGoldSoft = Color(0xFFE8D49B)
internal val CosmicGoldMuted = Color(0xFF9C8657)
internal val CosmicGoldGlow = Color(0x66E8D49B)
internal val CosmicGoldBorder = Color(0xA8B89A5F)
internal val CosmicOnPrimary = Color(0xFF172124)
internal val CosmicPrimaryContainer = Color(0xD91C3838)
internal val CosmicOnPrimaryContainer = Color(0xFFF3E6C7)
internal val CosmicSecondary = Color(0xFFE8D49B)
internal val CosmicOnSecondary = Color(0xFF1C2425)
internal val CosmicTertiary = Color(0xFF9AB7B2)
internal val CosmicOnTertiary = Color(0xFF102325)
internal val CosmicOutline = Color(0xB88F7A4D)
internal val CosmicOutlineVariant = Color(0xB8426063)
internal val CosmicOnBackground = Color(0xFFF0E6D2)
internal val CosmicOnSurface = Color(0xFFF0E6D2)
internal val CosmicOnSurfaceVariant = Color(0xFFB9AE99)
internal val CosmicError = Color(0xFFD69A86)
internal val CosmicOnError = Color(0xFF2D1F1B)

// Legacy aliases (keep existing call sites working)
internal val Sage = LightPrimary
internal val SageDark = DarkPrimary
internal val WarmPaper = LightBackground
internal val WarmPaperDark = DarkBackground
internal val Charcoal = LightOnBackground
internal val SoftIvory = LightOnPrimary
