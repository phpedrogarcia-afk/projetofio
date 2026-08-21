package com.projetofio.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projetofio.app.ui.theme.AppearanceThemeStore
import com.projetofio.app.ui.theme.FioVisualTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppearanceThemePersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun clearPreference() {
        context.getSharedPreferences(AppearanceThemeStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun selectedThemePersistsAcrossStoreInstances() {
        AppearanceThemeStore(context).save(FioVisualTheme.CEU_NOTURNO)

        assertEquals(FioVisualTheme.CEU_NOTURNO, AppearanceThemeStore(context).load())
    }
}
