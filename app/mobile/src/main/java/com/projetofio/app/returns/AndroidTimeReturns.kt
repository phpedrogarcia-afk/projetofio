package com.projetofio.app.returns

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.projetofio.app.FioApplication
import com.projetofio.app.MainActivity
import com.projetofio.app.R
import com.projetofio.app.application.ReturnNotificationGateway
import com.projetofio.app.application.ReturnOpportunityScheduler
import java.time.Clock
import java.time.Instant
import java.util.concurrent.TimeUnit

class WorkManagerReturnScheduler(
    context: Context,
    private val clock: Clock,
) : ReturnOpportunityScheduler {
    private val workManager = WorkManager.getInstance(context)

    override suspend fun schedule(at: Instant) {
        val delayMillis = (at.toEpochMilli() - clock.millis()).coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<TimeReturnWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()
        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    override suspend fun cancel() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "fio-time-return-opportunity-v1"
        const val WORK_TAG = "fio-time-return"
    }
}

class TimeReturnWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val graph = (applicationContext as FioApplication).graph
        return runCatching { graph.timeReturns.reconcile() }
            .fold(
                onSuccess = { Result.success() },
                onFailure = { if (runAttemptCount < 3) Result.retry() else Result.failure() },
            )
    }
}

class AndroidReturnNotifications(
    private val context: Context,
) : ReturnNotificationGateway {
    private val manager = context.getSystemService(NotificationManager::class.java)

    override fun canPostNotifications(): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return permissionGranted && manager.areNotificationsEnabled()
    }

    override fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lembranças do Fio",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Uma devolução silenciosa das suas próprias palavras."
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        manager.createNotificationChannel(channel)
    }

    override fun post(returnId: String) {
        if (!canPostNotifications()) return
        createChannel()
        val intent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_RETURN
            putExtra(MainActivity.EXTRA_RETURN_ID, returnId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            returnId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Algo seu voltou.")
            .setCategory(Notification.CATEGORY_REMINDER)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setShowWhen(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        manager.notify(notificationId(returnId), notification)
    }

    override fun cancel(returnId: String) {
        manager.cancel(notificationId(returnId))
    }

    private fun notificationId(returnId: String): Int = returnId.hashCode() and Int.MAX_VALUE

    companion object {
        const val CHANNEL_ID = "fio_returns_v1"
    }
}
