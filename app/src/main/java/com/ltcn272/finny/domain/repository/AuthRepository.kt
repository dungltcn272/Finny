package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.AuthToken
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Login
    suspend fun backendLogin(idToken: String): AppResult<Pair<User, AuthToken>>
    // Refresh Token
    suspend fun refreshTokens(refreshToken: String): AppResult<AuthToken>

    // New API expected by UI/ViewModel
    val currentUser: Flow<AuthUser?>
    fun isLoggedIn(): Flow<Boolean>

    suspend fun loginWithGoogle(idToken: String)
    suspend fun loginWithFacebook(accessToken: String)

    suspend fun logout()
}