package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    // 1. Lấy dữ liệu cho UI (từ Room)
    fun getLocalTransactions(filter: TransactionFilter): Flow<List<Transaction>>

    // 2. Sync dữ liệu ban đầu
    suspend fun syncTransactionsFromApi(filter: TransactionFilter): AppResult<Unit>

    // 3. Tạo/Cập nhật cục bộ (Offline-First)
    suspend fun saveTransactionLocally(transaction: Transaction, isNew: Boolean): AppResult<Unit>

    // 4. Xóa cục bộ (isDeleted = true)
    suspend fun deleteTransactionLocally(transactionId: String): AppResult<Unit>

    // 5. Đồng bộ hóa các thay đổi cục bộ lên API
    suspend fun pushLocalChangesToApi(): AppResult<Unit>

    // 6. Upload ảnh (cần gọi trước khi POST/PUT Transaction)
    suspend fun uploadImage(imagePath: String): AppResult<String> // Trả về Image URL
}