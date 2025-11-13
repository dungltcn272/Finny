package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.AuthDataDto
import com.ltcn272.finny.data.remote.dto.UpdateUserRequestDto // <-- Đã import DTO mới
import com.ltcn272.finny.data.remote.dto.UserDto
import com.ltcn272.finny.domain.model.AuthToken
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.model.Provider
import com.ltcn272.finny.domain.util.DateUtils
import com.ltcn272.finny.data.remote.dto.ProviderDto

// --- AuthToken Mappers ---
fun AuthDataDto.toAuthToken(): AuthToken {
    return AuthToken(
        accessToken = this.accessToken,
        refreshToken = this.refreshToken
    )
}

// --- User Mappers (DTO -> Domain) ---
fun UserDto.toDomain(): User {
    // Chuyển đổi Date Strings sang ZonedDateTime
    val lastLoginZoned = this.lastLogin?.let { DateUtils.parseIso8601(it) }
    val createdAtZoned = this.createdAt?.let { DateUtils.parseIso8601(it) }
    val updatedAtZoned = this.updatedAt?.let { DateUtils.parseIso8601(it) }

    return User(
        id = this.id,
        displayName = this.displayName, // <-- Đã dùng displayName
        email = this.email,
        avatar = this.avatar,           // <-- Đã dùng avatar
        plan = this.plan,
        currency = this.currency,
        lastLogin = lastLoginZoned,
        tags = this.tags,
        isAdmin = this.isAdmin,
        isActive = this.isActive,
        createdAt = createdAtZoned,
        updatedAt = updatedAtZoned,
        lang = this.lang,
        providers = this.providers?.map { it.toDomain() }
    )
}

fun ProviderDto.toDomain(): Provider {
    return Provider(
        provider = this.provider,
        providerId = this.providerId
    )
}

// --- User Mappers (Domain -> Request DTO) ---
fun User.toUpdateRequestDto(): UpdateUserRequestDto {
    return UpdateUserRequestDto(
        displayName = this.displayName,
        avatar = this.avatar,
        currency = this.currency,
        lang = this.lang
    )
}