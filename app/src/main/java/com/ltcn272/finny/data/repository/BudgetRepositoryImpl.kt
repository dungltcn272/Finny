package com.ltcn272.finny.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ltcn272.finny.data.mapper.toBudgetDomain
import com.ltcn272.finny.data.mapper.toCreateRequestDto
import com.ltcn272.finny.data.mapper.toUpdateMap
import com.ltcn272.finny.data.paging.BudgetPagingSource
import com.ltcn272.finny.data.remote.api.BudgetApi
import com.ltcn272.finny.data.remote.dto.BudgetResponseDto
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.toErrorType
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BudgetRepositoryImpl @Inject constructor(
    private val budgetApi: BudgetApi
) : BudgetRepository {

    override fun getBudgets(): Flow<PagingData<Budget>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { BudgetPagingSource(budgetApi) }
        ).flow
    }

    override suspend fun getRecentBudgets(): AppResult<List<Budget>> {
        return try {
            val response = budgetApi.getBudgets(page = 1)
            val budgets = response.data.data.map { it.toBudgetDomain() }
            AppResult.Success(budgets)
        } catch (e: Exception) {
            AppResult.Error(e.toErrorType())
        }
    }

    override suspend fun createBudget(budget: Budget): Flow<AppResult<Budget>> = flow {
        emit(AppResult.Loading)
        try {
            val requestDto = budget.toCreateRequestDto()
            val response = budgetApi.createBudget(requestDto)
            val dto: BudgetResponseDto = response.data
            emit(AppResult.Success(dto.toBudgetDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

    override suspend fun updateBudget(budget: Budget): Flow<AppResult<Budget>> = flow {
        emit(AppResult.Loading)
        try {
            val budgetId = budget.serverId
                ?: throw IllegalArgumentException("Budget ID cannot be null for update")
            val requestBody = budget.toUpdateMap()
            val response = budgetApi.updateBudget(budgetId, requestBody)
            val dto: BudgetResponseDto = response.data
            emit(AppResult.Success(dto.toBudgetDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

    override suspend fun deleteBudget(id: String): Flow<AppResult<Unit>> = flow {
        emit(AppResult.Loading)
        try {
            budgetApi.deleteBudget(id)
            emit(AppResult.Success(Unit))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }
}
