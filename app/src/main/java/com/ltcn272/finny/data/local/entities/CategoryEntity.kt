package com.ltcn272.finny.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey
    val id: String,

    val serverId: String? = null,

    val name: String,
    val description: String?,
    val color: String?,
    val isDefault: Boolean,

    // ===== OFFLINE-FIRST =====
    val syncState: SyncState = SyncState.CREATE,

    val updatedAt: String
)