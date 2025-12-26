package com.ltcn272.finny.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ltcn272.finny.data.mapper.toCategoryDomain
import com.ltcn272.finny.data.paging.CategoryPagingSource
import com.ltcn272.finny.data.remote.api.CategoryApi
import com.ltcn272.finny.data.remote.dto.CreateCategoryDto
import com.ltcn272.finny.domain.model.Category
import com.ltcn272.finny.domain.repository.CategoryRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.toErrorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

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

    override fun createCategory(name: String): Flow<AppResult<Category>> = flow {
        emit(AppResult.Loading)
        try {
            val requestDto = CreateCategoryDto(name = name)
            val response = categoryApi.create(requestDto)
            emit(AppResult.Success(response.data.toCategoryDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

    override fun updateCategory(id: String, data: Map<String, Any>): Flow<AppResult<Category>> = flow {
        emit(AppResult.Loading)
        try {
            val response = categoryApi.update(id, data)
            emit(AppResult.Success(response.data.toCategoryDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

    override fun deleteCategory(id: String): Flow<AppResult<Unit>> = flow {
        emit(AppResult.Loading)
        try {
            categoryApi.delete(id)
            emit(AppResult.Success(Unit))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }
}
