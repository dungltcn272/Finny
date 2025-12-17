package com.ltcn272.finny.domain.model

data class Category(
    val localId: String?,
    val serverId: String?,
    val name: String,
    val description: String?,
    val color: String?,
    val isDefault: Boolean
)

