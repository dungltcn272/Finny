package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.Category
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAllCategories(): Flow<List<Category>>

    suspend fun getCategoryById(categoryId: String): Category?

    suspend fun createNewCategory(category: Category): AppResult<String>

    suspend fun updateCategory(category: Category): AppResult<Unit>

    suspend fun deleteCategory(categoryId: String): AppResult<Unit>

    fun syncCategories(): Flow<AppResult<Unit>>

    suspend fun fetchAndSaveRemoteCategories(): AppResult<Unit>
}

