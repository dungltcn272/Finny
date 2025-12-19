package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthDataDto(
    @SerializedName("user")
    val user: AuthUserDto,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String
)

data class AuthUserDto(
    @SerializedName("_id")
    val id: String,

    @SerializedName("display_name")
    val displayName: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("avatar")
    val avatar: String?,

    @SerializedName("plan")
    val plan: String,

    @SerializedName("is_active")
    val isActive: Boolean
)