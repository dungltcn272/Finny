package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

interface BudgetRepository {
    fun getBudgets(): Flow<PagingData<Budget>>

    suspend fun getRecentBudgets(): AppResult<List<Budget>>


    suspend fun createBudget(budget: Budget): Flow<AppResult<Budget>>

    suspend fun updateBudget(budget: Budget): Flow<AppResult<Budget>>

    suspend fun deleteBudget(id: String): Flow<AppResult<Unit>>
}