package com.projetofio.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceThemeStoreTest {
    @Test
    fun onlyTheApprovedThemesCanBeDecoded() {
        assertEquals(FioVisualTheme.SERENO, AppearanceThemeStore.decode(null))
        assertEquals(FioVisualTheme.SERENO, AppearanceThemeStore.decode("unknown"))
        assertEquals(FioVisualTheme.SERENO, AppearanceThemeStore.decode(FioVisualTheme.SERENO.name))
        assertEquals(FioVisualTheme.CEU_NOTURNO, AppearanceThemeStore.decode(FioVisualTheme.CEU_NOTURNO.name))
    }
}
