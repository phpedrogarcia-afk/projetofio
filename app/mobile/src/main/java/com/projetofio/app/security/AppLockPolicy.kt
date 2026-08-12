package com.projetofio.app.security

import com.projetofio.app.domain.AppLockMode

class AppLockPolicy {
    fun shouldLock(
        mode: AppLockMode,
        backgroundStartedAtMillis: Long?,
        nowMillis: Long,
    ): Boolean {
        if (mode == AppLockMode.OFF) return false
        val startedAt = backgroundStartedAtMillis ?: return true
        if (nowMillis < startedAt) return true
        val elapsed = nowMillis - startedAt
        return when (mode) {
            AppLockMode.OFF -> false
            AppLockMode.IMMEDIATE -> true
            AppLockMode.ONE_MINUTE -> elapsed >= ONE_MINUTE_MILLIS
            AppLockMode.FIVE_MINUTES -> elapsed >= FIVE_MINUTES_MILLIS
        }
    }

    private companion object {
        const val ONE_MINUTE_MILLIS = 60_000L
        const val FIVE_MINUTES_MILLIS = 300_000L
    }
}
