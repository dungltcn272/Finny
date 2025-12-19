package com.ltcn272.finny.domain.model

data class AuthToken(
    val accessToken: String,
    val refreshToken: String
)