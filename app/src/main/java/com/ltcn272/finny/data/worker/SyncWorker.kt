package com.ltcn272.finny.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker chịu trách nhiệm đồng bộ hóa các thay đổi cục bộ (pending POST/PUT/DELETE) lên API.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    // Inject Repositories đã triển khai logic pushLocalChangesToApi()
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Đảm bảo các tác vụ được chạy tuần tự và dừng lại ngay khi có lỗi

        // 1. Đồng bộ Budget
        val budgetResult = budgetRepository.pushLocalChangesToApi()

        if (budgetResult is AppResult.Error) {
            // Log lỗi: Budget sync thất bại. WorkManager sẽ thử lại theo policy.
            return@withContext Result.retry()
        }

        // 2. Đồng bộ Transaction
        val transactionResult = transactionRepository.pushLocalChangesToApi()

        if (transactionResult is AppResult.Error) {
            // Log lỗi: Transaction sync thất bại. WorkManager sẽ thử lại.
            return@withContext Result.retry()
        }

        // Cả hai đồng bộ đều thành công
        Result.success()
    }

    companion object {
        const val WORK_NAME = "OfflineDataSyncWork"
        // Key để truyền dữ liệu (nếu cần filter user ID, v.v.)
        const val INPUT_DATA_USER_ID = "userId"
    }
}