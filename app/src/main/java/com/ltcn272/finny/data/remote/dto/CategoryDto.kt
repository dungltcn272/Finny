package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CategoryListDataDto(
    @SerializedName("data")
    val data: List<CategoryResponseDto>,

    @SerializedName("pagination")
    val pagination: PaginationDto
)

data class CategoryResponseDto(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("color")
    val color: String?,

    @SerializedName("is_default")
    val isDefault: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

data class CreateCategoryDto(
    @SerializedName("name")
    val name: String?
)