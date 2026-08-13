package com.projetofio.app

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import com.projetofio.app.returns.AndroidReturnNotifications
import com.projetofio.app.returns.WorkManagerReturnScheduler
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M2AndroidContractTest {
    private val context = ApplicationProvider.getApplicationContext<FioApplication>()
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val notifications = AndroidReturnNotifications(context)

    @After
    fun cleanup() {
        manager.cancelAll()
        runCatching { WorkManager.getInstance(context).cancelUniqueWork(WorkManagerReturnScheduler.UNIQUE_WORK_NAME).result.get() }
    }

    @Test
    fun notificationChannelIsLowImportanceAndHasNoBadge() {
        notifications.createChannel()
        val channel = checkNotNull(manager.getNotificationChannel(AndroidReturnNotifications.CHANNEL_ID))
        assertEquals("Devoluções", channel.name.toString())
        assertEquals(NotificationManager.IMPORTANCE_LOW, channel.importance)
        assertFalse(channel.canShowBadge())
        // Channel visibility may remain VISIBILITY_NO_OVERRIDE so the user's
        // system preference wins. The notification itself is asserted private below.
    }

    @Test
    fun postedNotificationContainsOnlyCanonicalGenericCopy() {
        grantNotificationPermissionIfNeeded()
        val opaqueReturnId = "opaque-synthetic-return"
        notifications.post(opaqueReturnId)

        val deadline = SystemClock.elapsedRealtime() + 5_000
        var activeNotifications = manager.activeNotifications
        while (activeNotifications.isEmpty() && SystemClock.elapsedRealtime() < deadline) {
            SystemClock.sleep(50)
            activeNotifications = manager.activeNotifications
        }
        val active = activeNotifications.single()
        assertEquals("Algo seu voltou.", active.notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString())
        assertNull(active.notification.extras.getCharSequence(Notification.EXTRA_TEXT))
        assertFalse(active.notification.extras.keySet().any { key -> key.contains("entry", ignoreCase = true) })
        assertNotNull(active.notification.contentIntent)
        assertEquals(Notification.VISIBILITY_PRIVATE, active.notification.visibility)
        assertFalse(active.notification.extras.getBoolean("android.showWhen", false))
    }

    @Test
    fun uniqueWorkIsReplacedWithoutContentBearingInput() = runBlocking {
        val clock = Clock.fixed(Instant.parse("2026-08-13T15:00:00Z"), ZoneOffset.UTC)
        val scheduler = WorkManagerReturnScheduler(context, clock)
        val workManager = WorkManager.getInstance(context)

        scheduler.schedule(clock.instant().plusSeconds(3_600))
        scheduler.schedule(clock.instant().plusSeconds(7_200))

        val work = workManager.getWorkInfosForUniqueWork(WorkManagerReturnScheduler.UNIQUE_WORK_NAME).get()
        assertEquals(1, work.size)
        assertEquals(0, work.single().outputData.size())
        assertTrue(WorkManagerReturnScheduler.WORK_TAG in work.single().tags)
    }

    @Test
    fun debugTargetCannotActivateTimeReturns() {
        assertFalse(BuildConfig.TIME_RETURNS_ENGINEERING_ENABLED)
    }

    private fun grantNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
        assertTrue(notifications.canPostNotifications())
    }
}
