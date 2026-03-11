package com.ergou.app

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ergou.app.data.repository.ProactiveWorker
import com.ergou.app.data.repository.SoulRepository
import com.ergou.app.di.appModule
import com.ergou.app.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ErgouApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Timber logging (debug only)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // 通知渠道
        NotificationHelper.createChannel(this)
        ProactiveWorker.createNotificationChannel(this)

        // Koin DI
        startKoin {
            androidLogger()
            androidContext(this@ErgouApplication)
            modules(appModule)
        }

        // 启动时同步灵魂状态（后台，不阻塞启动）
        syncSoulState()

        // 注册主动通知定时任务
        scheduleProactiveWorker()

        Timber.d("二狗启动了")
    }

    private fun syncSoulState() {
        appScope.launch {
            try {
                val soulRepository: SoulRepository = get()
                soulRepository.syncFromBackend()
            } catch (e: Exception) {
                Timber.w(e, "[Soul] 启动时灵魂状态同步失败")
            }
        }
    }

    private fun scheduleProactiveWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ProactiveWorker>(
            4, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ProactiveWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        Timber.d("[Proactive] 定时任务已注册（每4小时）")
    }
}
