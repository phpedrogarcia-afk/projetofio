package com.projetofio.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.projetofio.app.R

// ---------------------------------------------------------------------------
// Fio typography. Two families, three roles (per the Verde-Sálvia system):
// - Fraunces (serif display): brand name and memory dates ONLY.
// - Inter (grotesk): everything else, especially the journal body.
// ADR-013: no cursive in the journal body, ever.
// All sizes use sp so they scale with the system font preference (test up
// to 200% without clipping).
// ---------------------------------------------------------------------------

internal val Fraunces = FontFamily(
    Font(R.font.fraunces_regular, FontWeight.Normal),
    Font(R.font.fraunces_medium, FontWeight.Medium),
)

private val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

val FioTypography = Typography(
    // displayBrand — "Fio" wordmark (Fraunces 500, 34sp/40sp)
    displayLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    ),
    // titleScreen — screen titles like "Arquivo" (Inter 600, 20sp/28sp)
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    // titleSection — group headers like "Privacidade" (Inter 600, 15sp/22sp)
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    // subtitle — prompts ("O que está passando pela sua cabeça hoje?")
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    // body — interface text (Inter 400, 16sp/24sp)
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // bodyNote — journal text, the most important text on screen
    // (Inter 400, 17sp/27sp — bigger, more air)
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 27.sp,
    ),
    // caption — metadata, dates, indicators (Inter 400, 13sp/18sp)
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    // button labels (Inter 500, 15sp/20sp)
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
)

// Display style for memory dates ("18 de março de 2025") — used explicitly,
// never by default, so the serif belongs to the memory system, not the UI.
val FioDisplayDate = TextStyle(
    fontFamily = Fraunces,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 28.sp,
)

// Céu Noturno keeps Inter for the user's words and controls, but lets screen
// titles carry the editorial warmth visible in the canonical reference.
val CosmicTypography = FioTypography.copy(
    headlineMedium = FioTypography.headlineMedium.copy(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 23.sp,
        lineHeight = 30.sp,
    ),
    titleMedium = FioTypography.titleMedium.copy(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
    ),
)
