package com.ltcn272.finny.core

import android.content.Context
import androidx.work.*
import com.ltcn272.finny.data.worker.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    private val context: Context // Sử dụng Application Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Lập lịch tác vụ đồng bộ hóa định kỳ (ví dụ: mỗi 15 phút).
     * Sử dụng ExistingPeriodicWorkPolicy.KEEP để đảm bảo chỉ có 1 tác vụ chạy.
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Yêu cầu có mạng
            .build()

        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME, // Tên định danh
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }

    /**
     * Lập lịch tác vụ đồng bộ hóa chạy một lần (cho Manual Retry hoặc sau khi User Add Transaction).
     */
    fun scheduleOneTimeSync() {
        val oneTimeSyncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueue(oneTimeSyncRequest)
    }
}