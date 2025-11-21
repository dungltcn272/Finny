package com.ltcn272.finny.data.repository

import android.util.Log
import com.ltcn272.finny.data.local.dao.TransactionDao
import com.ltcn272.finny.data.mapper.*
import com.ltcn272.finny.data.remote.api.TransactionApi
import com.ltcn272.finny.data.remote.dto.TransactionFilterRequestDto
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.AppResult.Error
import com.ltcn272.finny.domain.util.AppResult.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject

private const val TAG = "TransactionRepository"

class TransactionRepositoryImpl @Inject constructor(
    private val transactionApi: TransactionApi,
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getLocalTransactions(filter: TransactionFilter): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
                .filter { txn ->
                    val budgetMatch = filter.budgetId == null || txn.budgetId == filter.budgetId
                    val dateMatch = if (filter.startDate != null && filter.endDate != null) {
                        val txnDate = txn.dateTime.toLocalDate()
                        txnDate != null && !txnDate.isBefore(filter.startDate) && !txnDate.isAfter(filter.endDate)
                    } else {
                        true
                    }
                    budgetMatch && dateMatch
                }
        }
    }

    override suspend fun syncTransactionsFromApi(): AppResult<Unit> {
        try {
            val filterBody = null

            var currentPage = 1
            var lastPage = 1

            while (currentPage <= lastPage) {
                val requestDto = TransactionFilterRequestDto(
                    page = currentPage,
                    filter = filterBody
                )

                val response = transactionApi.getTransactions(requestDto)

                if (response.status == 200 && response.data != null) {
                    lastPage = response.data.pagination.lastPage
                    val entities = response.data.data.map { it.toEntity(isSynced = true) }
                    transactionDao.insertAll(entities)
                    currentPage++
                } else {
                    return Error("API Sync failed on page $currentPage: ${response.message}")
                }
            }

            return Success(Unit)
        } catch (e: Exception) {
            return Error("Network Sync Error: ${e.localizedMessage}", e)
        }
    }

    override suspend fun addTransactionLocally(transaction: Transaction): AppResult<Unit> {
        val entity = transaction.copy(id = UUID.randomUUID().toString())
            .toEntity(isSynced = false, isDeleted = false)
        transactionDao.insert(entity)
        return Success(Unit)
    }

    override suspend fun getTransactionById(transactionId: String): AppResult<Transaction?> {
        val entity = transactionDao.getTransactionById(transactionId)
        return Success(entity?.toDomain())
    }

    override suspend fun updateTransactionLocally(transaction: Transaction): AppResult<Unit> {
        val entity = transaction.toEntity(isSynced = false, isDeleted = false)
        transactionDao.update(entity)
        return Success(Unit)
    }

    override suspend fun deleteTransactionLocally(transactionId: String): AppResult<Unit> {
        val entity = transactionDao.getTransactionById(transactionId)
            ?: return Error("Transaction not found locally for ID: $transactionId")

        if (!entity.isSynced) {
            transactionDao.deleteById(transactionId)
        } else {
            val entityToUpdate = entity.copy(isDeleted = true, isSynced = false)
            transactionDao.update(entityToUpdate)
        }
        return Success(Unit)
    }

    override suspend fun pushLocalChangesToApi(): AppResult<Unit> {
        val pendingEntities = transactionDao.getTransactionsToSync()
        val errors = mutableListOf<String>()

        pendingEntities.forEach { entity ->
            try {
                if (entity.isDeleted) {
                    val deleteResponse = transactionApi.deleteTransaction(entity.id)
                    if (deleteResponse.status == 200) {
                        transactionDao.deleteById(entity.id)
                    } else {
                        errors.add("Failed to delete ${entity.id}: ${deleteResponse.message}")
                    }
                } else {
                    var transactionToSync = entity.toDomain()

                    // Step 1: Upload image if a local path exists
                    if (!entity.localImagePath.isNullOrBlank()) {
                        when (val uploadResult = uploadImage(entity.localImagePath)) {
                            is Success -> {
                                // Update the domain object with the new server URL
                                transactionToSync = transactionToSync.copy(image = uploadResult.data)
                            }
                            is Error -> {
                                // If image upload fails, add error and skip this item
                                errors.add("Image upload failed for ${entity.id}: ${uploadResult.message}")
                                return@forEach // continue to next entity
                            }

                            AppResult.Loading -> {}
                        }
                    }

                    // Step 2: Create/Update the transaction with the (potentially new) image URL
                    val requestDto = transactionToSync.toCreateRequestDto()
                    val isNew = entity.id.contains("-")

                    val response = if (isNew) {
                        transactionApi.createTransaction(requestDto)
                    } else {
                        transactionApi.updateTransaction(entity.id, requestDto)
                    }

                    if (response.status == 200 && response.data != null) {
                        transactionDao.deleteById(entity.id)
                        val syncedEntity = response.data.toEntity(isSynced = true)
                        transactionDao.insert(syncedEntity)
                    } else if (response.status == 200) {
                        val updatedEntity = entity.copy(
                            isSynced = true,
                            image = transactionToSync.image // Persist the new image URL
                        )
                        transactionDao.update(updatedEntity)
                    } else {
                        errors.add("Failed to sync ${entity.id}: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed for entity ${entity.id}", e)
                errors.add("Sync failed for ${entity.id}: ${e.message}")
            }
        }

        return if (errors.isEmpty()) {
            Success(Unit)
        } else {
            Error(errors.joinToString("\n"))
        }
    }

    override suspend fun uploadImage(imagePath: String): AppResult<String> {
        try {
            val file = File(imagePath)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", file.name, requestBody)

            val response = transactionApi.uploadImage(part)

            return if (response.status == 200 && response.data != null) {
                Success(response.data.imageUrl)
            } else {
                Error(response.message)
            }
        } catch (e: Exception) {
            return Error("Image upload failed: ${e.localizedMessage}", e)
        }
    }
}
