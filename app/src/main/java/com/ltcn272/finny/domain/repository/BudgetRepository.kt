package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Pagination
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    // 1. Lấy dữ liệu cho UI (luôn từ Room, sử dụng Flow)
    fun getLocalBudgets(): Flow<List<Budget>>

    // 2. Tác vụ sync dữ liệu ban đầu từ API về Room
    suspend fun syncBudgetsFromApi(): AppResult<Unit>

    // 3. Tạo/Cập nhật cục bộ (Offline-First)
    suspend fun saveBudgetLocally(budget: Budget, isNew: Boolean): AppResult<Unit>

    // 4. Đồng bộ hóa các thay đổi cục bộ lên API (Background Task)
    suspend fun pushLocalChangesToApi(): AppResult<Unit>
}