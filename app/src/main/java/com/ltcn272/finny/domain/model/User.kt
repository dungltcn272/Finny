package com.ltcn272.finny.domain.model

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val avatar: String,
    val plan: String,
    val memberSince: String
)
data class Provider(
    val provider: String,
    val providerId: String
)

data class AuthToken(
    val accessToken: String,
    val refreshToken: String
)