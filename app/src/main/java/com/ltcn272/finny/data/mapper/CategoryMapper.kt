package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.CategoryResponseDto
import com.ltcn272.finny.data.remote.dto.CreateCategoryDto
import com.ltcn272.finny.domain.model.Category

fun CategoryResponseDto.toCategoryDomain(): Category {
    return Category(
        serverId = id,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault
    )
}

fun Category.toCreateDto(): CreateCategoryDto {
    return CreateCategoryDto(
        name = name
    )
}

fun Category.toUpdateMap(): Map<String, Any?> {
    return mapOf(
        "name" to name,
        "description" to description,
        "color" to color
    )
}
