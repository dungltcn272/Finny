package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAllBudgets(): Flow<List<Budget>>
    suspend fun getBudgetById(budgetId: String): Budget?

    suspend fun createNewBudget(
        budget: Budget
    ): AppResult<String>

    suspend fun updateBudget(
        budget: Budget
    ): AppResult<Unit>

    suspend fun deleteBudget(
        budgetId: String
    ): AppResult<Unit>

    fun syncBudgets(): Flow<AppResult<Unit>>

    suspend fun fetchAndSaveRemoteBudgets(): AppResult<Unit>
}

