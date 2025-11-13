package com.ltcn272.finny.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ltcn272.finny.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    // Lấy tất cả Budgets (chưa bị đánh dấu xóa) và sắp xếp
    @Query("SELECT * FROM budgets WHERE isDeleted = 0 ORDER BY startDate DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    // Lấy các bản ghi cần sync (chưa synced HOẶC đã deleted)
    @Query("SELECT * FROM budgets WHERE isSynced = 0 OR isDeleted = 1")
    suspend fun getBudgetsToSync(): List<BudgetEntity>

    // Đánh dấu xóa cục bộ (Offline-First Delete)
    @Update
    suspend fun update(budget: BudgetEntity)

    // Xóa vĩnh viễn (sau khi sync thành công)
    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteById(budgetId: String)
}