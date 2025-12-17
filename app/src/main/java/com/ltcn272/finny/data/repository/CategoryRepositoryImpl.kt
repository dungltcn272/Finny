package com.ltcn272.finny.data.repository

import android.util.Log
import com.ltcn272.finny.data.local.dao.CategoryDao
import com.ltcn272.finny.data.local.entities.SyncState
import com.ltcn272.finny.data.mapper.toEntity
import com.ltcn272.finny.data.mapper.toDomain
import com.ltcn272.finny.data.remote.api.CategoryApi
import com.ltcn272.finny.data.remote.dto.BusinessException
import com.ltcn272.finny.data.remote.dto.CreateCategoryDto
import com.ltcn272.finny.data.remote.dto.ensureSuccess
import com.ltcn272.finny.domain.model.Category
import com.ltcn272.finny.domain.repository.CategoryRepository
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.ZonedDateTime
import java.util.UUID

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val categoryApi: CategoryApi
) : CategoryRepository {

    companion object {
        private const val TAG = "CategoryRepository"
    }

    override fun observeAllCategories(): Flow<List<Category>> {
        return categoryDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(categoryId: String): Category? {
        return categoryDao.getCategoryById(categoryId)?.toDomain()
    }

    override suspend fun createNewCategory(category: Category): AppResult<String> {
        return try {
            val localId = category.localId ?: UUID.randomUUID().toString()
            val entity = category.toEntity(syncState = SyncState.CREATE)
            categoryDao.upsert(entity)
            AppResult.Success(localId)
        } catch (e: Exception) {
            AppResult.Error(e.message.toString())
        }
    }

    override suspend fun updateCategory(category: Category): AppResult<Unit> {
        return try {
            val existingEntity = categoryDao.getCategoryById(
                category.localId ?: return AppResult.Error("Local ID required for update")
            )

            if (existingEntity == null) {
                return AppResult.Error("Category not found locally.")
            }

            val newState =
                if (existingEntity.syncState == SyncState.CREATE) SyncState.CREATE else SyncState.UPDATE

            val updatedEntity = category.toEntity(syncState = newState)
                .copy(id = existingEntity.id, updatedAt = ZonedDateTime.now().toString())

            categoryDao.upsert(updatedEntity)

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message.toString())
        }
    }

    override suspend fun deleteCategory(categoryId: String): AppResult<Unit> {
        return try {
            val existing = categoryDao.getCategoryById(categoryId)
                ?: return AppResult.Success(Unit)

            if (existing.serverId == null) {
                categoryDao.deleteLocal(categoryId)
            } else {
                categoryDao.update(
                    existing.copy(syncState = SyncState.DELETE)
                )
            }

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message.toString())
        }
    }

    override fun syncCategories(): Flow<AppResult<Unit>> = flow {
        emit(AppResult.Loading)
        val currentTime = ZonedDateTime.now().toString()
        try {
            val toSync = categoryDao.getBySyncStatus(SyncState.CREATE) +
                    categoryDao.getBySyncStatus(SyncState.UPDATE) +
                    categoryDao.getBySyncStatus(SyncState.DELETE)

            for (cat in toSync) {
                try {
                    when (cat.syncState) {
                        SyncState.CREATE -> {
                            val response = categoryApi.create(CreateCategoryDto(name = cat.name))
                                .ensureSuccess()
                            // replace insert with upsert to consolidate
                            categoryDao.upsert(
                                cat.copy(
                                    serverId = response.id,
                                    syncState = SyncState.SYNCED,
                                    updatedAt = currentTime
                                )
                            )
                            Log.d(
                                TAG,
                                "Synced CREATE category localId=${cat.id} -> serverId=${response.id}"
                            )
                        }

                        SyncState.UPDATE -> {
                            cat.serverId?.let { serverId ->
                                val body = mutableMapOf<String, Any>("name" to cat.name)
                                categoryApi.update(serverId, body).ensureSuccess()
                                categoryDao.upsert(cat.copy(syncState = SyncState.SYNCED))
                                Log.d(
                                    TAG,
                                    "Synced UPDATE category localId=${cat.id} (serverId=$serverId)"
                                )
                            }
                        }

                        SyncState.DELETE -> {
                            cat.serverId?.let { serverId ->
                                categoryApi.delete(serverId).ensureSuccess()
                                Log.d(
                                    TAG,
                                    "Synced DELETE category localId=${cat.id} (serverId=$serverId)"
                                )
                            }
                            categoryDao.deleteLocal(cat.id)
                        }

                        else -> Unit
                    }
                } catch (e: BusinessException) {
                    Log.e(TAG, "API Error syncing category ${cat.id}: ${e.message}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing category ${cat.id}: ${e.message}")
                }
            }

            emit(AppResult.Success(Unit))
        } catch (e: Exception) {
            emit(AppResult.Error(e.message.toString()))
        }
    }

    override suspend fun fetchAndSaveRemoteCategories(): AppResult<Unit> {
        return try {
            val limit = 100
            var page = 1
            while (true) {
                val resp = categoryApi.getList(page = page, limit = limit).ensureSuccess()
                for (remote in resp.data) {
                    try {
                        val exist = categoryDao.getCategoryByServerId(remote.id)
                        if (exist != null) {
                            val preserve =
                                if (exist.syncState != SyncState.SYNCED) exist.syncState else SyncState.SYNCED
                            val updatedEntity = remote.toEntity().copy(syncState = preserve)
                            categoryDao.upsert(updatedEntity)
                        } else {
                            categoryDao.upsert(remote.toEntity())
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error merging category ${remote.id}: ${e.message}")
                    }
                }
                if (resp.pagination.totalPage <= page) break
                page += 1
            }
            AppResult.Success(Unit)
        } catch (e: BusinessException) {
            AppResult.Error(e.message)
        } catch (e: Exception) {
            AppResult.Error(e.message.toString())
        }
    }
}
