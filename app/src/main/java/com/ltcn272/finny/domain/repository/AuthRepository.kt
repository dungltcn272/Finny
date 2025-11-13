package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.AuthToken
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.util.AppResult

interface AuthRepository {
    // Login
    suspend fun backendLogin(idToken: String): AppResult<Pair<User, AuthToken>>
    // Refresh Token
    suspend fun refreshTokens(refreshToken: String): AppResult<AuthToken>
    // Profile
    suspend fun fetchProfile(): AppResult<User>
    suspend fun updateProfile(user: User): AppResult<User>
}