package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getLocalTransactions(filter: TransactionFilter): Flow<List<Transaction>>

    suspend fun syncTransactionsFromApi(): AppResult<Unit>

    suspend fun addTransactionLocally(transaction: Transaction): AppResult<Unit>

    suspend fun getTransactionById(transactionId: String): AppResult<Transaction?>

    suspend fun updateTransactionLocally(transaction: Transaction): AppResult<Unit>

    suspend fun deleteTransactionLocally(transactionId: String): AppResult<Unit>

    suspend fun pushLocalChangesToApi(): AppResult<Unit>

    suspend fun uploadImage(imagePath: String): AppResult<String>
}
