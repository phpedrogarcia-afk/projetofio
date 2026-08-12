package com.projetofio.app.testing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.projetofio.app.FioApplication
import com.projetofio.app.domain.AppLockMode
import kotlinx.coroutines.runBlocking

class DebugFixtureReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val application = context.applicationContext as FioApplication
        runBlocking {
            when (intent.action) {
                ACTION_SEED_DRAFT -> {
                    val content = checkNotNull(intent.getStringExtra(EXTRA_CONTENT))
                    require(content.startsWith(SYNTHETIC_PREFIX))
                    application.graph.service.autosaveDraft(content)
                }
                ACTION_CLEAR_DRAFT -> application.graph.service.autosaveDraft(" ")
                ACTION_SET_APP_LOCK -> {
                    val mode = AppLockMode.valueOf(checkNotNull(intent.getStringExtra(EXTRA_MODE)))
                    application.graph.service.setAppLockMode(mode)
                }
                else -> error("Unsupported debug fixture action")
            }
        }
    }

    companion object {
        const val ACTION_SEED_DRAFT = "com.projetofio.app.testing.SEED_DRAFT"
        const val ACTION_CLEAR_DRAFT = "com.projetofio.app.testing.CLEAR_DRAFT"
        const val ACTION_SET_APP_LOCK = "com.projetofio.app.testing.SET_APP_LOCK"
        const val EXTRA_CONTENT = "content"
        const val EXTRA_MODE = "mode"
        const val SYNTHETIC_PREFIX = "FIO_SYNTHETIC_"
    }
}
