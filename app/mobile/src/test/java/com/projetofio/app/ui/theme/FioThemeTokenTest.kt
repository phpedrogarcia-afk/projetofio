package com.projetofio.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class FioThemeTokenTest {
    @Test
    fun sereneLightTokensRemainFrozenAtPv02Values() {
        assertEquals(Color(0xFFF6F1E7), LightBackground)
        assertEquals(Color(0xFFFBF8F1), LightSurface)
        assertEquals(Color(0xFFFFFCF5), LightElevatedSurface)
        assertEquals(Color(0xFF596C5A), LightPrimary)
        assertEquals(Color(0xFFDFE7DA), LightPrimaryContainer)
        assertEquals(Color(0xFF242823), LightOnBackground)
        assertEquals(Color(0xFFD2CCBE), LightOutline)
    }

    @Test
    fun sereneDarkTokensRemainFrozenAtPv02Values() {
        assertEquals(Color(0xFF1C211D), DarkBackground)
        assertEquals(Color(0xFF242A25), DarkSurface)
        assertEquals(Color(0xFF2A302B), DarkElevatedSurface)
        assertEquals(Color(0xFFB2C0A9), DarkPrimary)
        assertEquals(Color(0xFF39463A), DarkPrimaryContainer)
        assertEquals(Color(0xFFECEDE6), DarkOnBackground)
        assertEquals(Color(0xFF4B554C), DarkOutline)
    }

    @Test
    fun cosmicTokensMatchCanonicalSpec() {
        assertEquals(Color(0xFF071D20), CosmicBackground)
        assertEquals(Color(0xFF041417), CosmicBackgroundDeep)
        assertEquals(Color(0xFFD5B773), CosmicPrimary)
        assertEquals(Color(0xFFE8D49B), CosmicSecondary)
        assertEquals(Color(0xFFF0E6D2), CosmicOnBackground)
        assertEquals(Color(0xFFB9AE99), CosmicOnSurfaceVariant)
    }
}
