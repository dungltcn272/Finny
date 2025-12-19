package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.AuthToken
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun isLoggedIn(): Flow<Boolean>

    suspend fun backendLogin(idToken: String): AppResult<Pair<User, AuthToken>>

    suspend fun refreshTokens(refreshToken: String): AppResult<AuthToken>

    suspend fun loginWithGoogle(idToken: String): AppResult<User>

    suspend fun loginWithFacebook(accessToken: String): AppResult<User>

    suspend fun logout()
}
