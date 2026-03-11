package com.ergou.app.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ergou.app.MainActivity
import com.ergou.app.R
import com.ergou.app.data.remote.api.LLMService
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.util.NextAuthProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import timber.log.Timber

/**
 * 定时执行的主动通知 Worker — 每 4 小时触发一次
 */
class ProactiveWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    companion object {
        const val WORK_NAME = "ergou_proactive_notification"
        const val CHANNEL_ID = "ergou_proactive"
        private const val CHANNEL_NAME = "二狗主动提醒"
        private const val NOTIFICATION_ID = 9001

        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "二狗的主动关心和提醒"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private val proactiveNotifier: ProactiveNotifier by lazy {
        ProactiveNotifier(
            nextApiService = get<NextApiService>(),
            authProvider = get<NextAuthProvider>(),
            soulRepository = get<SoulRepository>(),
            llmService = get<LLMService>()
        )
    }

    override suspend fun doWork(): Result {
        Timber.d("[Proactive] Worker 开始执行")

        // 安静时段不推送
        if (proactiveNotifier.isQuietHour()) {
            Timber.d("[Proactive] 安静时段，跳过")
            return Result.success()
        }

        try {
            val reminders = proactiveNotifier.collectReminders()
            val notification = proactiveNotifier.generateNotification(reminders)

            if (notification != null) {
                showNotification(notification)
                Timber.d("[Proactive] 通知已推送")
            } else {
                Timber.d("[Proactive] 无需推送通知")
            }
        } catch (e: Exception) {
            Timber.w(e, "[Proactive] Worker 执行失败")
            return Result.retry()
        }

        return Result.success()
    }

    private fun showNotification(content: String) {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext, NOTIFICATION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("二狗说")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = appContext.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
}
