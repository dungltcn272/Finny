package com.ltcn272.finny.domain.model

// Đây là Domain Model đại diện cho người dùng đã đăng nhập từ Backend
data class AuthUser(
    val userId: String,
    val email: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val isPremium: Boolean = false,
    val authProvider: String // e.g., "GOOGLE", "FACEBOOK", "EMAIL"
)