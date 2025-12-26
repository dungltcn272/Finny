package com.ltcn272.finny.domain.repository

import androidx.paging.PagingData
import com.ltcn272.finny.domain.model.Category
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<PagingData<Category>>

    fun createCategory(name: String): Flow<AppResult<Category>>

    fun updateCategory(id: String, data: Map<String, Any>): Flow<AppResult<Category>>

    fun deleteCategory(id: String): Flow<AppResult<Unit>>
}
