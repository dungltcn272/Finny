package com.ltcn272.finny.data.repository

import android.util.Log
import com.ltcn272.finny.data.local.dao.BudgetDao
import com.ltcn272.finny.data.local.entities.SyncState
import com.ltcn272.finny.data.mapper.toCreateRequestDto
import com.ltcn272.finny.data.mapper.toDomain
import com.ltcn272.finny.data.mapper.toEntity
import com.ltcn272.finny.data.mapper.toUpdateDto
import com.ltcn272.finny.data.remote.api.BudgetApi
import com.ltcn272.finny.data.remote.dto.BusinessException
import com.ltcn272.finny.data.remote.dto.ensureSuccess
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.ZonedDateTime
import java.util.UUID


class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao,
    private val budgetApi: BudgetApi
) : BudgetRepository {

    companion object {
        private const val TAG = "BudgetRepository"
    }

    override fun getAllBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllBudgets().map { entities ->
            entities
                .filter { it.syncState != SyncState.DELETE }
                .map { it.toDomain() }
        }
    }

    override suspend fun getBudgetById(budgetId: String): Budget? {
        return budgetDao.getBudgetById(budgetId)?.toDomain()
    }

    override suspend fun createNewBudget(budget: Budget): AppResult<String> {
        return try {
            val localId = budget.localId ?: UUID.randomUUID().toString()
            val entity = budget.copy(localId = localId)
                .toEntity(syncState = SyncState.CREATE)
            budgetDao.upsertBudget(entity)
            AppResult.Success(localId)
        } catch (e: Exception) {
            AppResult.Error(e.message.toString())
        }
    }

    override suspend fun updateBudget(budget: Budget): AppResult<Unit> {
        return try {
            val existingEntity = budgetDao.getBudgetById(
                budget.localId ?: return AppResult.Error("Local ID required for update")
            )

            if (existingEntity == null) {
                return AppResult.Error("Budget not found locally.")
            }

            val newState =
                if (existingEntity.syncState == SyncState.CREATE) SyncState.CREATE else SyncState.UPDATE

            val updatedEntity = budget.toEntity(
                syncState = newState
            ).copy(updatedAt = ZonedDateTime.now().toString())

            budgetDao.upsertBudget(updatedEntity)

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message.toString())
        }
    }

    override suspend fun deleteBudget(budgetId: String): AppResult<Unit> {
        return try {
            val existingEntity = budgetDao.getBudgetById(budgetId)
                ?: return AppResult.Success(Unit)

            if (existingEntity.serverId == null) {
                budgetDao.deleteBudget(budgetId)
            } else {
                val deletedEntity = existingEntity.copy(
                    syncState = SyncState.DELETE,
                    updatedAt = ZonedDateTime.now().toString()
                )
                budgetDao.updateBudget(deletedEntity)
            }

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message.toString())
        }
    }

    override fun syncBudgets(): Flow<AppResult<Unit>> = flow {
        emit(AppResult.Loading)
        val currentTime = ZonedDateTime.now().toString()

        try {
            val budgetsToSync = budgetDao.getAllUnsyncedBudgets()

            val failures = mutableListOf<String>()

            for (budget in budgetsToSync) {
                try {
                    when (budget.syncState) {

                        SyncState.CREATE -> {
                            val requestDto = budget.toCreateRequestDto()
                            val responseDto = budgetApi.createBudget(requestDto).ensureSuccess()

                            budgetDao.updateBudgetAfterCreate(
                                localId = budget.id,
                                serverId = responseDto.id,
                                updatedAt = currentTime
                            )
                            Log.d(
                                TAG,
                                "Synced CREATE for localId=${budget.id} -> serverId=${responseDto.id}"
                            )
                        }

                        SyncState.UPDATE -> {
                            budget.serverId?.let { serverId ->
                                val requestDto = budget.toUpdateDto()
                                budgetApi.updateBudget(serverId, requestDto).ensureSuccess()
                                budgetDao.markAsSynced(budget.id, currentTime)
                                Log.d(
                                    TAG,
                                    "Synced UPDATE for localId=${budget.id} (serverId=$serverId)"
                                )
                            } ?: run {
                                // If serverId is null, treat as create
                                val requestDto = budget.toCreateRequestDto()
                                val responseDto = budgetApi.createBudget(requestDto).ensureSuccess()
                                budgetDao.updateBudgetAfterCreate(
                                    localId = budget.id,
                                    serverId = responseDto.id,
                                    updatedAt = currentTime
                                )
                                Log.d(
                                    TAG,
                                    "Converted UPDATE->CREATE for localId=${budget.id} -> serverId=${responseDto.id}"
                                )
                            }
                        }

                        SyncState.DELETE -> {
                            budget.serverId?.let { serverId ->
                                budgetApi.deleteBudget(serverId).ensureSuccess()
                                Log.d(
                                    TAG,
                                    "Synced DELETE for localId=${budget.id} (serverId=$serverId)"
                                )
                            }
                            budgetDao.deleteBudget(budget.id)
                        }

                        SyncState.SYNCED -> Unit

                    }
                } catch (e: BusinessException) {
                    val msg = "API Error for localId=${budget.id} (${e.statusCode}): ${e.message}"
                    Log.e(TAG, msg)
                    // collect for logging but do not abort overall sync
                    failures.add(msg)
                } catch (e: Exception) {
                    val msg = "System Error for localId=${budget.id}: ${e.message}"
                    Log.e(TAG, msg)
                    // collect for logging but do not abort overall sync
                    failures.add(msg)
                }
            }
            if (failures.isNotEmpty()) {
                Log.w(
                    TAG,
                    "syncBudgets completed with ${failures.size} item failures; see logs for details"
                )
            }

            emit(AppResult.Success(Unit))

        } catch (e: Exception) {
            emit(AppResult.Error("System Error: ${e.message}"))
        }
    }

    override suspend fun fetchAndSaveRemoteBudgets(): AppResult<Unit> {
        return try {
            val limit = 100
            var page = 1

            while (true) {
                val listDto = budgetApi.getBudgets(page = page, limit = limit).ensureSuccess()
                for (remote in listDto.data) {
                    try {
                        val existing = if (remote.id.isNotEmpty()) {
                            budgetDao.getBudgetByServerId(remote.id)
                        } else null

                        if (existing != null) {
                            val preserveSyncState = if (existing.syncState != SyncState.SYNCED) existing.syncState else SyncState.SYNCED
                            val updatedEntity = remote.toEntity().copy(syncState = preserveSyncState)
                            budgetDao.upsertBudget(updatedEntity)
                        } else {
                            val newEntity = remote.toEntity()
                            budgetDao.upsertBudget(newEntity)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error merging remote budget ${remote.id}: ${e.message}")
                    }
                }
                Log.d(TAG, "Fetched and merged page=$page size=${listDto.data.size} from server")

                if (listDto.pagination.totalPage <= page) break
                page += 1
            }

            Log.d(TAG, "Fetched and merged budgets from server (all pages)")
            AppResult.Success(Unit)
        } catch (e: BusinessException) {
            val msg = "API Error while fetching budgets: ${e.message}"
            Log.e(TAG, msg)
            AppResult.Error(msg)
        } catch (e: Exception) {
            val msg = "System Error while fetching budgets: ${e.message}"
            Log.e(TAG, msg)
            AppResult.Error(msg)
        }
    }

}
