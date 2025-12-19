package com.ltcn272.finny.data.repository

import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ltcn272.finny.core.TokenManager
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.data.mapper.toAuthToken
import com.ltcn272.finny.data.mapper.toUserDomain
import com.ltcn272.finny.data.remote.api.AuthApi
import com.ltcn272.finny.domain.model.AuthToken
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
import com.ltcn272.finny.domain.util.toErrorType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val firebaseAuth: FirebaseAuth,
    private val settingDataStore: SettingDataStore
) : AuthRepository {

    private val _isLoggedIn = MutableStateFlow(!tokenManager.getAccessToken().isNullOrBlank())
    override fun isLoggedIn(): Flow<Boolean> = _isLoggedIn.asStateFlow()

    override suspend fun backendLogin(idToken: String): AppResult<Pair<User, AuthToken>> {
        return try {
            val response = authApi.loginWithFirebaseToken("Bearer $idToken")
            val newAuthToken = response.data.toAuthToken()
            val user = response.data.user.toUserDomain()
            tokenManager.saveTokens(newAuthToken.accessToken, newAuthToken.refreshToken)
            _isLoggedIn.value = true
            user.displayName?.let { name ->
                if (name.isNotBlank()) {
                    settingDataStore.saveUsername(name)
                }
            }
            AppResult.Success(Pair(user, newAuthToken))
        } catch (e: Exception) {
            AppResult.Error(e.toErrorType())
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AppResult<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return AppResult.Error(ErrorType.UNKNOWN)

            val firebaseIdToken = firebaseUser.getIdToken(true).await().token
                ?: return AppResult.Error(ErrorType.UNKNOWN)

            when (val backendResult = backendLogin(firebaseIdToken)) {
                is AppResult.Success -> AppResult.Success(backendResult.data.first) // Trả về User
                is AppResult.Error -> backendResult
                is AppResult.Loading -> AppResult.Loading
            }
        } catch (e: Exception) {
            AppResult.Error(e.toErrorType())
        }
    }

    override suspend fun loginWithFacebook(accessToken: String): AppResult<User> {
        return try {
            val credential = FacebookAuthProvider.getCredential(accessToken)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return AppResult.Error(ErrorType.UNKNOWN)

            val firebaseIdToken = firebaseUser.getIdToken(true).await().token
                ?: return AppResult.Error(ErrorType.UNKNOWN)

            when (val backendResult = backendLogin(firebaseIdToken)) {
                is AppResult.Success -> AppResult.Success(backendResult.data.first)
                is AppResult.Error -> backendResult
                is AppResult.Loading -> AppResult.Loading
            }
        } catch (e: Exception) {
            AppResult.Error(e.toErrorType())
        }
    }

    override suspend fun refreshTokens(refreshToken: String): AppResult<AuthToken> {
        return try {
            val refreshHeader = "Bearer $refreshToken"
            val body = mapOf("refresh_token" to refreshToken)
            val response = authApi.refreshToken(refreshHeader, body)
            val newTokens = response.data.toAuthToken()
            tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
            _isLoggedIn.value = true
            AppResult.Success(newTokens)
        } catch (e: Exception) {
            tokenManager.clearTokens()
            _isLoggedIn.value = false
            AppResult.Error(e.toErrorType())
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
        tokenManager.clearTokens()
        _isLoggedIn.value = false
        settingDataStore.clearUsername()
    }
}
