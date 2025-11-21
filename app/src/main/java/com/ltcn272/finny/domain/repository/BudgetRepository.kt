package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.BudgetDetails
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetDetailsList(): Flow<List<BudgetDetails>>

    fun getBudgetDetails(id: String): Flow<BudgetDetails?>

    fun getLocalBudgets(): Flow<List<Budget>>

    suspend fun syncBudgetsFromApi(): AppResult<Unit>

    suspend fun addBudgetLocally(budget: Budget): AppResult<Unit>

    suspend fun updateBudgetLocally(budget: Budget): AppResult<Unit>

    suspend fun deleteBudgetLocally(budgetId: String): AppResult<Unit>

    suspend fun pushLocalChangesToApi(): AppResult<Unit>
}
