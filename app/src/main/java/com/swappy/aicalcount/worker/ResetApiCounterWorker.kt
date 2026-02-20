package com.swappy.aicalcount.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.swappy.aicalcount.util.ApiUsageManager

class ResetApiCounterWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            val apiUsageManager = ApiUsageManager(applicationContext)
            apiUsageManager.resetApiCallCount()
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            ListenableWorker.Result.failure()
        }
    }
}
