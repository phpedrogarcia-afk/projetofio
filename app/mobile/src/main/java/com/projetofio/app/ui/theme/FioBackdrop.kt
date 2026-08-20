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
                        colors = listOf(mist.copy(alpha = .11f), Color.Transparent),
                        center = Offset(size.width * .79f, size.height * .18f),
                        radius = size.minDimension * .72f,
                    ),
                    radius = size.minDimension * .72f,
                    center = Offset(size.width * .79f, size.height * .18f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(gold.copy(alpha = .055f), Color.Transparent),
                        center = Offset(size.width * .12f, size.height * .78f),
                        radius = size.minDimension * .58f,
                    ),
                    radius = size.minDimension * .58f,
                    center = Offset(size.width * .12f, size.height * .78f),
                )
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
            }
        }
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}
