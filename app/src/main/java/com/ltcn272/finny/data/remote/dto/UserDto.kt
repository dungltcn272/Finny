package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("_id") val id: String,
    @SerializedName("display_name") val displayName: String,
    val email: String,
    val avatar: String,
    val plan: String,
    @SerializedName("created_at") val createdAt: String
)

data class ProfileResponse(val data: UserDto)
