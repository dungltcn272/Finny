package com.ltcn272.finny.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ltcn272.finny.data.local.entities.BudgetEntity
import com.ltcn272.finny.data.local.entities.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets ORDER BY name ASC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: String): BudgetEntity?

    // Upsert convenience (alias of insert with REPLACE) to align naming with Category/consistency
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :localId")
    suspend fun deleteBudget(localId: String)

    @Query("""
        SELECT * FROM budgets 
        WHERE syncState IN (:states) 
        ORDER BY 
            CASE syncState 
                WHEN 'CREATE' THEN 1 
                WHEN 'UPDATE' THEN 2 
                WHEN 'DELETE' THEN 3 
                ELSE 4 
            END ASC
    """)
    suspend fun getBudgetsByStates(states: List<SyncState>): List<BudgetEntity>

    suspend fun getAllUnsyncedBudgets(): List<BudgetEntity> {
        return getBudgetsByStates(listOf(SyncState.CREATE, SyncState.UPDATE, SyncState.DELETE))
    }

    @Query("""
        UPDATE budgets 
        SET serverId = :serverId, 
            syncState = 'SYNCED', 
            updatedAt = :updatedAt 
        WHERE id = :localId
    """)
    suspend fun updateBudgetAfterCreate(
        localId: String,
        serverId: String,
        updatedAt: String
    ): Int

    @Query("""
        UPDATE budgets 
        SET syncState = 'SYNCED', 
            updatedAt = :updatedAt 
        WHERE id = :localId
    """)
    suspend fun markAsSynced(localId: String, updatedAt: String): Int

    @Query("SELECT * FROM budgets WHERE serverId = :serverId LIMIT 1")
    suspend fun getBudgetByServerId(serverId: String): BudgetEntity?
}