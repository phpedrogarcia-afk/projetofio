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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
        modifier = Modifier.height(56.dp).width(32.dp).alpha(0.68f),
        onDraw = {
            val scaleX = size.width / 32f
            val scaleY = size.height / 56f
            fun point(x: Float, y: Float) = Offset(x * scaleX, y * scaleY)
            val stem = Path().apply {
                moveTo(point(16f, 56f).x, point(16f, 56f).y)
                cubicTo(
                    point(13f, 44f).x,
                    point(13f, 44f).y,
                    point(19f, 30f).x,
                    point(19f, 30f).y,
                    point(16f, 12f - ageDays.coerceAtMost(30) * 0.08f).x,
                    point(16f, 12f - ageDays.coerceAtMost(30) * 0.08f).y,
                )
            }
            drawPath(stem, color = tertiary, style = Stroke(width = 1.dp.toPx()))
            drawLeaf(
                color = accent,
                stem = tertiary,
                base = point(15.5f, 43f),
                tip = point(6f, 34f),
                strokeWidth = 0.75.dp.toPx(),
            )
            if (ageDays >= 7) {
                drawLeaf(
                    color = accent,
                    stem = tertiary,
                    base = point(16.5f, 34f),
                    tip = point(26f, 25f),
                    strokeWidth = 0.75.dp.toPx(),
                )
            }
            if (ageDays >= 30) {
                drawLeaf(
                    color = accent,
                    stem = tertiary,
                    base = point(16f, 25f),
                    tip = point(7f, 17f),
                    strokeWidth = 0.75.dp.toPx(),
                )
            }
            if (ageDays >= 180) {
                drawLeaf(
                    color = accent,
                    stem = tertiary,
                    base = point(16f, 15f),
                    tip = point(21f, 7f),
                    strokeWidth = 0.75.dp.toPx(),
                )
            }
        },
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeaf(
    color: androidx.compose.ui.graphics.Color,
    stem: androidx.compose.ui.graphics.Color,
    base: Offset,
    tip: Offset,
    strokeWidth: Float,
) {
    val direction = tip - base
    val normal = Offset(-direction.y, direction.x)
    val normalLength = kotlin.math.sqrt(normal.x * normal.x + normal.y * normal.y).coerceAtLeast(1f)
    val unitNormal = Offset(normal.x / normalLength, normal.y / normalLength)
    val middle = base + direction * 0.55f
    val leaf = Path().apply {
        moveTo(base.x, base.y)
        quadraticTo(
            middle.x + unitNormal.x * 3.2f,
            middle.y + unitNormal.y * 3.2f,
            tip.x,
            tip.y,
        )
        quadraticTo(
            middle.x - unitNormal.x * 2.4f,
            middle.y - unitNormal.y * 2.4f,
            base.x,
            base.y,
        )
        close()
    }
    drawPath(leaf, color = color.copy(alpha = 0.58f))
    drawLine(stem, start = base, end = tip, strokeWidth = strokeWidth)
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

internal fun displayDay(entry: Entry): String {
    val zone = runCatching { ZoneId.of(entry.originalTimeZone ?: "UTC") }.getOrDefault(ZoneId.of("UTC"))
    return Formatter.ptDate.format(entry.originalCreatedAt.atZone(zone).toLocalDate())
}
