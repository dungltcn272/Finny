package com.ltcn272.finny.domain.repository

import androidx.paging.PagingData
import com.ltcn272.finny.domain.model.Category
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<PagingData<Category>>

    suspend fun createCategory(category: Category): Flow<AppResult<Category>>

    suspend fun updateCategory(category: Category): Flow<AppResult<Category>>

    suspend fun deleteCategory(id: String): Flow<AppResult<Unit>>
}
