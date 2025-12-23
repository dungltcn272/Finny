package com.ltcn272.finny.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ltcn272.finny.data.mapper.toCategoryDomain
import com.ltcn272.finny.data.mapper.toCreateDto
import com.ltcn272.finny.data.mapper.toUpdateMap
import com.ltcn272.finny.data.paging.CategoryPagingSource
import com.ltcn272.finny.data.remote.api.CategoryApi
import com.ltcn272.finny.domain.model.Category
import com.ltcn272.finny.domain.repository.CategoryRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.toErrorType
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CategoryRepositoryImpl @Inject constructor(
    private val categoryApi: CategoryApi
) : CategoryRepository {

    override fun getCategories(): Flow<PagingData<Category>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { CategoryPagingSource(categoryApi) }
        ).flow
    }

    override suspend fun createCategory(category: Category): Flow<AppResult<Category>> = flow {
        emit(AppResult.Loading)
        try {
            val requestDto = category.toCreateDto()
            val response = categoryApi.create(requestDto)
            emit(AppResult.Success(response.data.toCategoryDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

    override suspend fun updateCategory(category: Category): Flow<AppResult<Category>> = flow {
        emit(AppResult.Loading)
        try {
            val categoryId = category.serverId
                ?: throw IllegalArgumentException("Category serverId cannot be null for update")
            val requestBody = category.toUpdateMap()
            val response = categoryApi.update(categoryId, requestBody)
            emit(AppResult.Success(response.data.toCategoryDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

    override suspend fun deleteCategory(id: String): Flow<AppResult<Unit>> = flow {
        emit(AppResult.Loading)
        try {
            categoryApi.delete(id)
            emit(AppResult.Success(Unit))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }
}
