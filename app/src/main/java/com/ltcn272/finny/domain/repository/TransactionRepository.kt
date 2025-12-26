package com.ltcn272.finny.domain.repository

import androidx.paging.PagingData
import com.ltcn272.finny.domain.model.RecurringTransaction
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TransactionRepository {
    fun getTransactions(filter: TransactionFilter?): Flow<PagingData<Transaction>>

    suspend fun getRecentTransactions(): AppResult<List<Transaction>>

    suspend fun getAllTransactionsByBudget(budgetId: String): AppResult<List<Transaction>>

    fun getRecurringTransactions(): Flow<PagingData<RecurringTransaction>>

    suspend fun createTransaction(transaction: Transaction): Flow<AppResult<Transaction>>

    suspend fun updateTransaction(
        transactionId: String,
        updateMap: Map<String, Any?>
    ): Flow<AppResult<Transaction>>

    suspend fun deleteTransaction(id: String): Flow<AppResult<Unit>>

    suspend fun deleteRecurringTransaction(id: String): Flow<AppResult<Unit>>

    suspend fun uploadImage(imageFile: File): Flow<AppResult<String>>
}
