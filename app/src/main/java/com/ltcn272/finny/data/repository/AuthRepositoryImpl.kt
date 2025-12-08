package com.ltcn272.finny.data.repository

import com.ltcn272.finny.core.TokenManager
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.data.local.dao.BudgetDao
import com.ltcn272.finny.data.local.dao.TransactionDao
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

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val firebaseAuth: FirebaseAuth,
    private val settingDataStore: SettingDataStore,
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
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
                _isLoggedIn.value = true

                // persist only username (displayName) if available
                val firebaseUser = firebaseAuth.currentUser
                val name = firebaseUser?.displayName
                if (!name.isNullOrBlank()) {
                    settingDataStore.saveUsername(name)
                }

                AppResult.Success(
                    Pair(response.data.user.toDomain(), newAuthToken)
                )
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error("Login failed: ${e.localizedMessage}", e)
        }
    }

    override suspend fun refreshTokens(refreshToken: String): AppResult<AuthToken> {
        return try {
            val refreshHeader = "Bearer $refreshToken"
            val body = mapOf("refresh_token" to refreshToken)

            val response = authApi.refreshToken(refreshHeader, body)

            if (response.status == 200 && response.data != null) {
                val newTokens = response.data.toAuthToken()
                tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                _isLoggedIn.value = true
                AppResult.Success(newTokens)
            } else {
                tokenManager.clearTokens()
                _isLoggedIn.value = false
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
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

            // Save username if available
            firebaseUser.displayName?.let {
                if (it.isNotBlank()) settingDataStore.saveUsername(it)
            }
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
            _isLoggedIn.value = true

            // persist username
            firebaseUser.displayName?.let {
                if (it.isNotBlank()) settingDataStore.saveUsername(it)
            }
        } else {
            throw IllegalStateException("Backend login failed: ${res.message}")
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
        tokenManager.clearTokens()
        _isLoggedIn.value = false
        // Clear persisted username on logout
        settingDataStore.clearUsername()
        // Clear local database
        budgetDao.deleteAll()
        transactionDao.deleteAll()
    }

    private fun firebaseUserToAuthUser(user: FirebaseUser?): AuthUser? {
        if (user == null) return null
        val providerId = user.providerData
            .firstOrNull { it.providerId != "firebase" }
            ?.providerId
            ?: user.providerId

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
