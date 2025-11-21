package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName


data class UserDto(
    @SerializedName("_id") val id: String,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("plan") val plan: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("last_login") val lastLogin: String?, // ISO8601 String
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("is_admin") val isAdmin: Boolean?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("created_at") val createdAt: String?, // ISO8601 String
    @SerializedName("updated_at") val updatedAt: String?, // ISO8601 String
    @SerializedName("lang") val lang: String?,
    @SerializedName("providers") val providers: List<ProviderDto>?
)

// Ánh xạ từ Provider trong Common/Provider.swift
data class ProviderDto(
    @SerializedName("provider") val provider: String,
    @SerializedName("provider_id") val providerId: String
)

// DTO cho các request cập nhật (UpdateProfile Request Body)
data class UpdateProfileRequestDto(
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("lang") val lang: String? = null
)