package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.CategoryResponseDto
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

