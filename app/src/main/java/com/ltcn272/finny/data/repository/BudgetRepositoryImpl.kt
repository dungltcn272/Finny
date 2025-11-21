package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.local.dao.BudgetDao
import com.ltcn272.finny.data.local.dao.TransactionDao
import com.ltcn272.finny.data.mapper.toCreateRequestDto
import com.ltcn272.finny.data.mapper.toDomain
import com.ltcn272.finny.data.mapper.toEntity
import com.ltcn272.finny.data.remote.api.BudgetApi
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.BudgetDetails
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetRepositoryImpl @Inject constructor(
    private val budgetApi: BudgetApi,
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) : BudgetRepository {

    override fun getBudgetDetailsList(): Flow<List<BudgetDetails>> {
        return budgetDao.getAllBudgets().flatMapLatest { budgetEntities ->
            if (budgetEntities.isEmpty()) {
                return@flatMapLatest flowOf(emptyList())
            }
            val budgetDetailsFlows = budgetEntities.map { budgetEntity ->
                transactionDao.getTransactionsForBudget(budgetEntity.id).map { transactions ->
                    val spentAmount = transactions.sumOf { tx -> tx.amount }
                    BudgetDetails(budget = budgetEntity.toDomain(), spentAmount = spentAmount)
                }
            }
            combine(budgetDetailsFlows) { it.toList() }
        }
    }

    override fun getBudgetDetails(id: String): Flow<BudgetDetails?> {
        return budgetDao.getBudgetByIdFlow(id).flatMapLatest { budgetEntity ->
            if (budgetEntity == null) {
                return@flatMapLatest flowOf(null)
            }
            transactionDao.getTransactionsForBudget(budgetEntity.id).map { transactions ->
                val spentAmount = transactions.sumOf { tx -> tx.amount }
                BudgetDetails(budget = budgetEntity.toDomain(), spentAmount = spentAmount)
            }
        }
    }

    override fun getLocalBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllBudgets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncBudgetsFromApi(): AppResult<Unit> {
        return try {
            val response = budgetApi.getBudgets(page = 1)

            if (response.status == 200) {
                val entities = response.data?.data?.map { it.toEntity(isSynced = true) }
                if (entities != null) {
                    budgetDao.insertAll(entities)
                }
                AppResult.Success(Unit)
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error("API Sync Error: ${e.localizedMessage}")
        }
    }

    override suspend fun addBudgetLocally(budget: Budget): AppResult<Unit> {
        val entity = budget.copy(id = UUID.randomUUID().toString())
            .toEntity(isSynced = false, isDeleted = false)
        budgetDao.insert(entity)
        return AppResult.Success(Unit)
    }

    override suspend fun updateBudgetLocally(budget: Budget): AppResult<Unit> {
        val entity = budget.toEntity(isSynced = false, isDeleted = false)
        budgetDao.update(entity)
        return AppResult.Success(Unit)
    }

    override suspend fun deleteBudgetLocally(budgetId: String): AppResult<Unit> {
        val entity = budgetDao.getBudgetById(budgetId)
            ?: return AppResult.Error("Budget not found locally")

        transactionDao.deleteTransactionsByBudgetId(budgetId)

        // If the budget was created offline and never synced, delete it permanently.
        if (!entity.isSynced) {
            budgetDao.deleteById(budgetId)
        } else {
            // Otherwise, mark it for deletion on the server.
            val entityToUpdate = entity.copy(isDeleted = true, isSynced = false)
            budgetDao.update(entityToUpdate)
        }
        return AppResult.Success(Unit)
    }

    override suspend fun pushLocalChangesToApi(): AppResult<Unit> {
        val pendingEntities = budgetDao.getBudgetsToSync()

        pendingEntities.forEach { entity ->
            try {
                if (entity.isDeleted) {
                    val deleteResponse = budgetApi.deleteBudget(entity.id)
                    if (deleteResponse.status == 200) {
                        budgetDao.deleteById(entity.id)
                    }
                } else {
                    val budgetDomain = entity.toDomain()
                    val requestDto = budgetDomain.toCreateRequestDto()

                    val isNew = entity.id.contains("-") // UUIDs contain hyphens

                    val response = if (isNew) {
                        budgetApi.createBudget(requestDto)
                    } else {
                        budgetApi.updateBudget(entity.id, requestDto)
                    }

                    if (response.status == 200) {
                        val syncedEntity = if (isNew && response.data != null) {
                            // Update with the new ID from the server
                            response.data.toEntity(isSynced = true)
                        } else {
                            entity.copy(isSynced = true)
                        }
                        budgetDao.deleteById(entity.id) // Remove old temporary record
                        budgetDao.insert(syncedEntity) // Insert the synced record
                    }
                }
            } catch (e: Exception) {
                return AppResult.Error("Sync failed for ${entity.id}: ${e.localizedMessage}")
            }
        }

        return AppResult.Success(Unit)
    }
}