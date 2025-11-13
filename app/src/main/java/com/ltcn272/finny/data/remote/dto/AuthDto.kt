package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

// Ánh xạ từ AuthData trong Auth.swift
data class AuthDataDto(
    @SerializedName("user") val user: UserDto,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

// Ánh xạ từ RefreshRequestDto/RefreshResponseDto (nếu API giống Android hiện tại)
data class RefreshRequestDto(
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshResponseDto(
    @SerializedName("data") val data: RefreshDataDto
)

data class RefreshDataDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class UpdateUserRequestDto(
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("lang") val lang: String?
)