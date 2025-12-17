package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.local.entities.CategoryEntity
import com.ltcn272.finny.data.local.entities.SyncState
import com.ltcn272.finny.data.remote.dto.CategoryResponseDto
import com.ltcn272.finny.domain.model.Category
import java.util.UUID

fun CategoryResponseDto.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = UUID.randomUUID().toString(),
        serverId = this.id,
        name = this.name,
        description = this.description,
        color = this.color,
        isDefault = this.isDefault,
        syncState = SyncState.SYNCED,
        updatedAt = this.updatedAt
    )
}

fun CategoryEntity.toDomain(): Category {
    return Category(
        localId = id,
        serverId = serverId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault
    )
}

fun Category.toEntity(syncState: SyncState): CategoryEntity {
    return CategoryEntity(
        id = localId ?: UUID.randomUUID().toString(),
        serverId = serverId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        syncState = syncState,
        updatedAt = java.time.ZonedDateTime.now().toString()
    )
}



