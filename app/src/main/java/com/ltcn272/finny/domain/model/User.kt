package com.ltcn272.finny.domain.model

import java.time.ZonedDateTime

data class User(
    val id: String,
    val displayName: String?,
    val email: String?,
    val avatar: String?,
    val plan: String?,
    val currency: String?,
    val lastLogin: ZonedDateTime?,
    val tags: List<String>?,
    val isAdmin: Boolean?,
    val isActive: Boolean?,
    val createdAt: ZonedDateTime?,
    val updatedAt: ZonedDateTime?,
    val lang: String?,
    val providers: List<Provider>?
)

data class Provider(
    val provider: String,
    val providerId: String
)

data class AuthToken(
    val accessToken: String,
    val refreshToken: String
)