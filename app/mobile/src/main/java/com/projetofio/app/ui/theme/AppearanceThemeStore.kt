package com.projetofio.app.ui.theme

import android.content.Context

/**
 * Stores only the visual preference of this installation.
 *
 * This deliberately stays outside Room, the autobiographical model and the
 * ViewModel: changing a theme must never touch a person's notes or flows.
 */
internal class AppearanceThemeStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    fun load(): FioVisualTheme = decode(preferences.getString(THEME_KEY, null))

    fun save(theme: FioVisualTheme) {
        preferences.edit().putString(THEME_KEY, theme.name).commit()
    }

    internal companion object {
        const val PREFERENCES_NAME = "fio_appearance"
        const val THEME_KEY = "visual_theme"

        fun decode(storedValue: String?): FioVisualTheme = when (storedValue) {
            FioVisualTheme.CEU_NOTURNO.name -> FioVisualTheme.CEU_NOTURNO
            else -> FioVisualTheme.SERENO
        }
    }
}
