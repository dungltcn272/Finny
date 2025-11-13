package com.ltcn272.finny

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory // Import mới
import androidx.work.Configuration // Import mới
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    // Inject WorkerFactory
    @Inject lateinit var workerFactory: HiltWorkerFactory

    /**
     * Khai báo cho WorkManager biết cách tạo các Worker có DI (HiltWorker)
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory) // Đưa factory của Hilt vào
            // .setMinimumLoggingLevel(android.util.Log.DEBUG) // Tùy chọn cho Debug
            .build()
}