package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileUserDto(
    @SerializedName("_id")
    val id: String,

    @SerializedName("display_name")
    val displayName: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("avatar")
    val avatar: String?,

    @SerializedName("providers")
    val providers: List<ProviderDto>,

    @SerializedName("plan")
    val plan: String,

    @SerializedName("currency")
    val currency: String?,

    @SerializedName("last_login")
    val lastLogin: String?,

    @SerializedName("is_admin")
    val isAdmin: Boolean,

    @SerializedName("is_active")
    val isActive: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

data class ProviderDto(
    @SerializedName("provider")
    val provider: String,
    @SerializedName("provider_id")
    val providerId: String
)
