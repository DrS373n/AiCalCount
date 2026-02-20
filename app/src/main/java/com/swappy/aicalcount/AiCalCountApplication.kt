package com.swappy.aicalcount

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.swappy.aicalcount.worker.ResetApiCounterWorker
import java.util.concurrent.TimeUnit

class AiCalCountApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
    }

    private fun setupRecurringWork() {
        val repeatingRequest = PeriodicWorkRequestBuilder<ResetApiCounterWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "reset-api-counter",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}
