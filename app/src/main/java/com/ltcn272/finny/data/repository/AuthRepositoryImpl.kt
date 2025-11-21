package com.ltcn272.finny.data.repository

import android.util.Log
import com.ltcn272.finny.core.TokenManager
import com.ltcn272.finny.data.mapper.toAuthToken
import com.ltcn272.finny.data.mapper.toDomain
import com.ltcn272.finny.data.remote.api.AuthApi
import com.ltcn272.finny.domain.model.AuthToken
import com.ltcn272.finny.domain.model.AuthUser
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.util.AppResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepositoryImpl"

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(firebaseUserToAuthUser(fa.currentUser))
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(firebaseUserToAuthUser(firebaseAuth.currentUser))
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    private val _isLoggedIn = MutableStateFlow(!tokenManager.getAccessToken().isNullOrBlank())
    override fun isLoggedIn(): Flow<Boolean> = _isLoggedIn.asStateFlow()

    override suspend fun backendLogin(idToken: String): AppResult<Pair<User, AuthToken>> {
        return try {
            val response = authApi.loginWithFirebaseToken("Bearer $idToken")
            if (response.status == 200 && response.data != null) {
                val newAuthToken = response.data.toAuthToken()
                tokenManager.saveTokens(newAuthToken.accessToken, newAuthToken.refreshToken)
                Log.d(TAG, "Backend Login Success. New Access Token: ${newAuthToken.accessToken}")
                Log.d(TAG, "Backend Login Success. New Refresh Token: ${newAuthToken.refreshToken}")
                _isLoggedIn.value = true
                AppResult.Success(
                    Pair(response.data.user.toDomain(), newAuthToken)
                )
            } else {
                Log.e(TAG, "Backend login failed: ${response.message}")
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backend login exception", e)
            AppResult.Error("Login failed: ${e.localizedMessage}", e)
        }
    }

    override suspend fun refreshTokens(refreshToken: String): AppResult<AuthToken> {
        Log.d(TAG, "Attempting to refresh token.")
        Log.d(TAG, "Using Refresh Token: $refreshToken")
        return try {
            val refreshHeader = "Bearer $refreshToken"
            val body = mapOf("refresh_token" to refreshToken)
            Log.d(TAG, "Refresh Token Request Body: $body")

            val response = authApi.refreshToken(refreshHeader, body)
            Log.d(TAG, "Refresh Token Response: $response")

            if (response.status == 200 && response.data != null) {
                val newTokens = response.data.toAuthToken()
                tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                Log.d(TAG, "Token refresh successful. New Access Token: ${newTokens.accessToken}")
                Log.d(TAG, "Token refresh successful. New Refresh Token: ${newTokens.refreshToken}")
                _isLoggedIn.value = true
                AppResult.Success(newTokens)
            } else {
                Log.e(TAG, "Token refresh failed with status ${response.status}: ${response.message}")
                tokenManager.clearTokens()
                _isLoggedIn.value = false
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh exception", e)
            tokenManager.clearTokens()
            _isLoggedIn.value = false
            AppResult.Error("Token refresh failed: ${e.localizedMessage}", e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user not found after Google login.")
        val firebaseIdToken = firebaseUser.getIdToken(true).await().token
            ?: throw IllegalStateException("Firebase ID token not found.")
        val res = authApi.loginWithFirebaseToken("Bearer $firebaseIdToken")
        if (res.status == 200 && res.data != null) {
            tokenManager.saveTokens(res.data.accessToken, res.data.refreshToken)
            _isLoggedIn.value = true
        } else {
            throw IllegalStateException("Backend login failed: ${res.message}")
        }
    }

    override suspend fun loginWithFacebook(accessToken: String) {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user not found after Facebook login.")
        val firebaseIdToken = firebaseUser.getIdToken(true).await().token
            ?: throw IllegalStateException("Firebase ID token not found.")

        val res = authApi.loginWithFirebaseToken("Bearer $firebaseIdToken")
        if (res.status == 200 && res.data != null) {
            tokenManager.saveTokens(res.data.accessToken, res.data.refreshToken)
            Log.d(TAG, "Facebook Login Success. New Access Token: ${res.data.accessToken}")
            Log.d(TAG, "Facebook Login Success. New Refresh Token: ${res.data.refreshToken}")
            _isLoggedIn.value = true
        } else {
            throw IllegalStateException("Backend login failed: ${res.message}")
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
        tokenManager.clearTokens()
        _isLoggedIn.value = false
    }

    private fun firebaseUserToAuthUser(user: FirebaseUser?): AuthUser? {
        if (user == null) return null
        val providerId = user.providerData
            .firstOrNull { it.providerId != "firebase" }
            ?.providerId
            ?: user.providerId
            ?: ""

        val provider = when (providerId.lowercase()) {
            "google.com" -> "GOOGLE"
            "facebook.com" -> "FACEBOOK"
            else -> providerId.uppercase()
        }

        return AuthUser(
            userId = user.uid,
            email = user.email ?: "",
            displayName = user.displayName,
            avatarUrl = user.photoUrl?.toString(),
            isPremium = false,
            authProvider = provider
        )
    }
}