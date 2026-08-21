package com.projetofio.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope

private data class SkyPoint(val x: Float, val y: Float, val size: Float)

private val stars = listOf(
    SkyPoint(.08f, .08f, 1.2f), SkyPoint(.21f, .13f, .7f), SkyPoint(.36f, .07f, .9f),
    SkyPoint(.52f, .16f, 1.3f), SkyPoint(.69f, .09f, .7f), SkyPoint(.88f, .15f, 1.0f),
    SkyPoint(.14f, .25f, .7f), SkyPoint(.31f, .31f, 1.1f), SkyPoint(.77f, .28f, .8f),
    SkyPoint(.94f, .36f, 1.2f), SkyPoint(.07f, .47f, .8f), SkyPoint(.43f, .43f, .7f),
    SkyPoint(.63f, .51f, 1.0f), SkyPoint(.84f, .58f, .7f), SkyPoint(.18f, .63f, 1.1f),
    SkyPoint(.39f, .70f, .8f), SkyPoint(.57f, .66f, .7f), SkyPoint(.92f, .73f, 1.0f),
    SkyPoint(.09f, .82f, .8f), SkyPoint(.28f, .89f, 1.2f), SkyPoint(.71f, .87f, .9f),
    SkyPoint(.86f, .95f, .7f),
)

private val distantStars = List(72) { index ->
    SkyPoint(
        x = ((index * 37 + 11) % 97 + 1) / 99f,
        y = ((index * 53 + 17) % 89 + 3) / 94f,
        size = if (index % 13 == 0) .85f else if (index % 5 == 0) .55f else .34f,
    )
}

@Composable
internal fun FioBackdrop(content: @Composable () -> Unit) {
    val profile = FioThemeContext.current
    val background = MaterialTheme.colorScheme.background
    val gold = MaterialTheme.colorScheme.primary
    val mist = MaterialTheme.colorScheme.tertiary
    Box(modifier = Modifier.fillMaxSize().background(background)) {
        if (profile.isCosmic) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(CosmicBackgroundDeep, CosmicBackground, Color(0xFF0B282B)),
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CosmicMistBlue.copy(alpha = .19f), mist.copy(alpha = .07f), Color.Transparent),
                        center = Offset(size.width * .82f, size.height * .17f),
                        radius = size.minDimension * .82f,
                    ),
                    radius = size.minDimension * .82f,
                    center = Offset(size.width * .82f, size.height * .17f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CosmicMistWarm.copy(alpha = .12f), gold.copy(alpha = .07f), Color.Transparent),
                        center = Offset(size.width * .10f, size.height * .78f),
                        radius = size.minDimension * .72f,
                    ),
                    radius = size.minDimension * .72f,
                    center = Offset(size.width * .12f, size.height * .78f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CosmicMistBlue.copy(alpha = .10f), Color.Transparent),
                        center = Offset(size.width * .50f, size.height * .55f),
                        radius = size.minDimension * .55f,
                    ),
                    radius = size.minDimension * .55f,
                    center = Offset(size.width * .50f, size.height * .55f),
                )
                distantStars.forEach { star ->
                    drawCircle(
                        CosmicSecondary.copy(alpha = if (star.size > .7f) .34f else .18f),
                        radius = star.size * density,
                        center = Offset(size.width * star.x, size.height * star.y),
                    )
                }
                stars.forEach { star ->
                    val center = Offset(size.width * star.x, size.height * star.y)
                    val radius = star.size * density
                    drawCircle(CosmicSecondary.copy(alpha = .56f), radius, center)
                    if (star.size > 1f) {
                        drawLine(
                            CosmicSecondary.copy(alpha = .26f),
                            center - Offset(radius * 3f, 0f),
                            center + Offset(radius * 3f, 0f),
                            strokeWidth = .5f * density,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            CosmicSecondary.copy(alpha = .26f),
                            center - Offset(0f, radius * 3f),
                            center + Offset(0f, radius * 3f),
                            strokeWidth = .5f * density,
                            cap = StrokeCap.Round,
                        )
                    }
                }
                val constellation = listOf(
                    Offset(.61f, .24f), Offset(.68f, .20f), Offset(.74f, .27f),
                    Offset(.81f, .23f), Offset(.87f, .31f),
                ).map { Offset(it.x * size.width, it.y * size.height) }
                constellation.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        CosmicPrimary.copy(alpha = .15f),
                        start,
                        end,
                        strokeWidth = .7f * density,
                    )
                }
                val orbitCenter = Offset(size.width * .08f, size.height * .72f)
                drawCircle(
                    CosmicPrimary.copy(alpha = .11f),
                    radius = size.width * .12f,
                    center = orbitCenter,
                    style = Stroke(width = .7f * density),
                )
                drawCircle(
                    CosmicPrimary.copy(alpha = .08f),
                    radius = size.width * .085f,
                    center = orbitCenter,
                    style = Stroke(width = .5f * density),
                )
                drawVignette()
                drawBackdropObservatory()
            }
        }
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}

private fun DrawScope.drawVignette() {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, CosmicBackgroundDeep.copy(alpha = .58f)),
            center = Offset(size.width * .52f, size.height * .46f),
            radius = size.maxDimension * .78f,
        ),
    )
}

private fun DrawScope.drawBackdropObservatory() {
    val baseY = size.height * .88f
    val left = size.width * .66f
    val width = size.width * .34f
    val line = CosmicPrimary.copy(alpha = .075f)
    val stroke = Stroke(width = .7f * density)
    for (step in 0..4) {
        val inset = width * step * .09f
        val y = baseY - step * size.height * .035f
        drawLine(line, Offset(left + inset, y), Offset(left + width - inset, y), stroke.width)
    }
    drawLine(line, Offset(left, baseY), Offset(left + width * .5f, baseY - size.height * .20f), stroke.width)
    drawLine(line, Offset(left + width, baseY), Offset(left + width * .5f, baseY - size.height * .20f), stroke.width)
    val center = Offset(left + width * .5f, baseY - size.height * .23f)
    drawCircle(line, radius = width * .11f, center = center, style = stroke)
    drawLine(line, Offset(center.x - width * .16f, center.y), Offset(center.x + width * .16f, center.y), stroke.width)
}
