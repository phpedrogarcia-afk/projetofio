package com.projetofio.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.projetofio.app.domain.Entry
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.ZoneId
import java.util.Locale

private const val PATINA_ENABLED = true

@Composable
internal fun BotanicalMotif(firstEntryAt: Instant? = null) {
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

internal object Formatter {
    val ptDate: DateTimeFormatter = DateTimeFormatter.ofPattern(
        "d 'de' MMMM 'de' yyyy",
        Locale.forLanguageTag("pt-BR"),
    )
}

internal fun displayDate(entry: Entry): String {
    val zone = runCatching { ZoneId.of(entry.originalTimeZone ?: "UTC") }.getOrDefault(ZoneId.of("UTC"))
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale.forLanguageTag("pt-BR"))
    return formatter.format(entry.originalCreatedAt.atZone(zone))
}
