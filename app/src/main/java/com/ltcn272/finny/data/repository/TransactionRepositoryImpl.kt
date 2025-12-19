package com.ltcn272.finny.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ltcn272.finny.data.mapper.toCreateDto
import com.ltcn272.finny.data.mapper.toFilterDto
import com.ltcn272.finny.data.mapper.toTransactionDomain
import com.ltcn272.finny.data.mapper.toUpdateMap
import com.ltcn272.finny.data.paging.RecurringTransactionPagingSource
import com.ltcn272.finny.data.paging.TransactionPagingSource
import com.ltcn272.finny.data.remote.api.TransactionApi
import com.ltcn272.finny.data.remote.dto.TransactionListRequestDto
import com.ltcn272.finny.domain.model.RecurringTransaction
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.toErrorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionApi: TransactionApi,
) : TransactionRepository {

    override fun getTransactions(filter: TransactionFilter?): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { TransactionPagingSource(transactionApi, filter?.toFilterDto()) }
        ).flow
    }

    override suspend fun getRecentTransactions(): AppResult<List<Transaction>> {
        return try {
            val requestDto = TransactionListRequestDto(
                page = 1
            )
            val response = transactionApi.getTransactions(requestDto)
            val transactions = response.data.data.map { it.toTransactionDomain() }
            AppResult.Success(transactions)
        } catch (e: Exception) {
            AppResult.Error(e.toErrorType())
        }
    }


    override fun getRecurringTransactions(): Flow<PagingData<RecurringTransaction>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { RecurringTransactionPagingSource(transactionApi) }
        ).flow
    }

    override suspend fun createTransaction(transaction: Transaction): Flow<AppResult<Transaction>> = flow {
        emit(AppResult.Loading)
        try {
            val requestDto = transaction.toCreateDto()
            val response = transactionApi.createTransaction(requestDto)
            emit(AppResult.Success(response.data.toTransactionDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Flow<AppResult<Transaction>> = flow {
        emit(AppResult.Loading)
        try {
            val transactionId = transaction.serverId
            val requestBody = transaction.toUpdateMap()
            val response = transactionApi.updateTransaction(transactionId, requestBody)
            emit(AppResult.Success(response.data.toTransactionDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

    override suspend fun deleteTransaction(id: String): Flow<AppResult<Unit>> = flow {
        emit(AppResult.Loading)
        try {
            transactionApi.deleteTransaction(id)
            emit(AppResult.Success(Unit))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

    override suspend fun deleteRecurringTransaction(id: String): Flow<AppResult<Unit>> = flow {
        emit(AppResult.Loading)
        try {
            transactionApi.deleteRecurringTransaction(id)
            emit(AppResult.Success(Unit))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }
}
