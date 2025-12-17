package com.ltcn272.finny.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ltcn272.finny.data.local.entities.CategoryEntity
import com.ltcn272.finny.data.local.entities.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CategoryEntity)

    @Query("SELECT * FROM categories WHERE syncState = :status")
    suspend fun getBySyncStatus(status: SyncState): List<CategoryEntity>

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteLocal(id: String)

    @Query("SELECT * FROM categories WHERE serverId = :serverId LIMIT 1")
    suspend fun getCategoryByServerId(serverId: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Update
    suspend fun update(entity: CategoryEntity)
}
