package com.projetofio.app.security

import com.projetofio.app.domain.AppLockMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLockPolicyTest {
    private val policy = AppLockPolicy()

    @Test
    fun offNeverLocksAndImmediateAlwaysLocksAfterBackground() {
        assertFalse(policy.shouldLock(AppLockMode.OFF, null, 10_000L))
        assertTrue(policy.shouldLock(AppLockMode.IMMEDIATE, 9_999L, 10_000L))
    }

    @Test
    fun graceBoundariesAreExact() {
        assertFalse(policy.shouldLock(AppLockMode.ONE_MINUTE, 1_000L, 60_999L))
        assertTrue(policy.shouldLock(AppLockMode.ONE_MINUTE, 1_000L, 61_000L))
        assertFalse(policy.shouldLock(AppLockMode.FIVE_MINUTES, 1_000L, 300_999L))
        assertTrue(policy.shouldLock(AppLockMode.FIVE_MINUTES, 1_000L, 301_000L))
    }

    @Test
    fun missingOrInvalidElapsedTimeFailsClosed() {
        assertTrue(policy.shouldLock(AppLockMode.ONE_MINUTE, null, 10_000L))
        assertTrue(policy.shouldLock(AppLockMode.FIVE_MINUTES, 20_000L, 10_000L))
    }
}
