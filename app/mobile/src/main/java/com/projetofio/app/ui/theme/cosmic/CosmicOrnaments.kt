package com.projetofio.app.ui.theme.cosmic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import com.projetofio.app.ui.theme.CosmicGoldSoft
import com.projetofio.app.ui.theme.FioThemeContext

/** Decorative, semantic-free line art used only when Céu Noturno is active. */
internal enum class CosmicOrnament { COMPASS, ASTROLABE, OBSERVATORY, ORBIT, MANDALA, STAR, CONSTELLATION_NODE }

@Composable
internal fun CosmicOrnament(
    ornament: CosmicOrnament,
    modifier: Modifier = Modifier,
    opacity: Float = 1f,
) {
    if (!FioThemeContext.current.isCosmic) return
    Canvas(modifier = modifier.fillMaxSize().alpha(opacity)) {
        val center = Offset(size.width * .5f, size.height * .5f)
        val radius = size.minDimension * .38f
        val line = CosmicGoldSoft.copy(alpha = .54f)
        val fine = Stroke(width = .8f * density)
        when (ornament) {
            CosmicOrnament.COMPASS -> {
                drawCircle(line, radius, center, style = fine)
                drawCircle(line.copy(alpha = .62f), radius * .66f, center, style = fine)
                repeat(8) { index ->
                    val angle = Math.toRadians((index * 45.0) - 90.0)
                    val end = Offset(
                        center.x + kotlin.math.cos(angle).toFloat() * radius,
                        center.y + kotlin.math.sin(angle).toFloat() * radius,
                    )
                    val start = Offset(
                        center.x + kotlin.math.cos(angle).toFloat() * radius * .34f,
                        center.y + kotlin.math.sin(angle).toFloat() * radius * .34f,
                    )
                    drawLine(line, start, end, fine.width)
                }
                drawCircle(CosmicGoldSoft.copy(alpha = .72f), radius * .06f, center)
            }
            CosmicOrnament.ASTROLABE -> {
                drawCircle(line, radius, center, style = fine)
                drawCircle(line.copy(alpha = .72f), radius * .68f, center, style = fine)
                drawCircle(line.copy(alpha = .55f), radius * .34f, center, style = fine)
                drawOval(line.copy(alpha = .78f), topLeft = Offset(center.x - radius, center.y - radius * .45f), size = androidx.compose.ui.geometry.Size(radius * 2f, radius * .90f), style = fine)
                drawLine(line, Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), fine.width)
            }
            CosmicOrnament.OBSERVATORY -> {
                val base = size.height * .83f
                val left = size.width * .12f
                val width = size.width * .76f
                repeat(4) { step ->
                    val inset = width * step * .09f
                    val y = base - step * size.height * .10f
                    drawLine(line, Offset(left + inset, y), Offset(left + width - inset, y), fine.width)
                }
                drawLine(line, Offset(left, base), Offset(left + width * .5f, size.height * .12f), fine.width)
                drawLine(line, Offset(left + width, base), Offset(left + width * .5f, size.height * .12f), fine.width)
                drawCircle(line, radius * .24f, Offset(size.width * .5f, size.height * .23f), style = fine)
            }
            CosmicOrnament.ORBIT -> {
                drawOval(line, topLeft = Offset(center.x - radius, center.y - radius * .42f), size = androidx.compose.ui.geometry.Size(radius * 2f, radius * .84f), style = fine)
                drawOval(line.copy(alpha = .72f), topLeft = Offset(center.x - radius * .72f, center.y - radius), size = androidx.compose.ui.geometry.Size(radius * 1.44f, radius * 2f), style = fine)
                drawCircle(CosmicGoldSoft.copy(alpha = .72f), radius * .07f, Offset(center.x + radius * .82f, center.y))
            }
            CosmicOrnament.MANDALA -> {
                // Outer celestial rings
                drawCircle(line, radius * 1.15f, center, style = fine)
                drawCircle(line.copy(alpha = .72f), radius * 0.95f, center, style = fine)
                drawCircle(line.copy(alpha = .55f), radius * 0.65f, center, style = fine)
                drawCircle(line.copy(alpha = .80f), radius * 0.35f, center, style = fine)
                // 12 Astrological ray spokes
                repeat(12) { index ->
                    val angle = Math.toRadians(index * 30.0)
                    val outer = Offset(
                        center.x + kotlin.math.cos(angle).toFloat() * radius * 1.15f,
                        center.y + kotlin.math.sin(angle).toFloat() * radius * 1.15f,
                    )
                    val inner = Offset(
                        center.x + kotlin.math.cos(angle).toFloat() * radius * 0.65f,
                        center.y + kotlin.math.sin(angle).toFloat() * radius * 0.65f,
                    )
                    drawLine(line.copy(alpha = .40f), inner, outer, fine.width)
                }
                // Central bright star core
                drawCircle(CosmicGoldSoft.copy(alpha = .90f), radius * .08f, center)
            }
            CosmicOrnament.STAR -> {
                val r = radius * 0.8f
                // 4-pointed radiant star
                drawLine(line, center - Offset(r, 0f), center + Offset(r, 0f), fine.width)
                drawLine(line, center - Offset(0f, r), center + Offset(0f, r), fine.width)
                val diagR = r * 0.45f
                drawLine(line.copy(alpha = .6f), center - Offset(diagR, diagR), center + Offset(diagR, diagR), fine.width)
                drawLine(line.copy(alpha = .6f), center - Offset(diagR, -diagR), center + Offset(diagR, -diagR), fine.width)
                drawCircle(CosmicGoldSoft.copy(alpha = .85f), r * 0.12f, center)
            }
            CosmicOrnament.CONSTELLATION_NODE -> {
                drawCircle(line.copy(alpha = .4f), radius * 0.8f, center, style = fine)
                drawCircle(CosmicGoldSoft.copy(alpha = .8f), radius * 0.3f, center)
            }
        }
    }
}

