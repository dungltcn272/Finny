package com.ltcn272.finny.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider // <-- Cần import
import com.google.firebase.auth.FacebookAuthProvider // <-- Cần import
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client để giao tiếp trực tiếp với Firebase SDK, tách biệt khỏi Business Logic.
 */
@Singleton
class FirebaseAuthClient @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    // --- AUTHENTICATION METHODS ---

    /**
     * Dùng Google ID Token để đăng nhập vào Firebase.
     * Được gọi sau khi Google Sign-In SDK trả về ID Token.
     */
    suspend fun signInWithGoogle(googleIdToken: String) {
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        firebaseAuth.signInWithCredential(credential).await()
    }

    /**
     * Dùng Facebook Access Token để đăng nhập vào Firebase.
     * Được gọi sau khi Facebook Login Manager trả về Access Token.
     */
    suspend fun signInWithFacebook(facebookAccessToken: String) {
        val credential = FacebookAuthProvider.getCredential(facebookAccessToken)
        firebaseAuth.signInWithCredential(credential).await()
    }

    // --- TOKEN MANAGEMENT METHODS ---

    /**
     * Lấy Firebase ID Token hiện tại (được sử dụng bởi GetIdTokenUseCase).
     */
    suspend fun getIdToken(): String? {
        return try {
            val currentUser = firebaseAuth.currentUser ?: return null
            // forceRefresh = false để lấy token hiện tại
            val tokenResult = currentUser.getIdToken(false).await()
            tokenResult.token
        } catch (e: Exception) {
            // Xử lý lỗi Firebase SDK
            throw e
        }
    }

    /**
     * Thực hiện Logout khỏi Firebase.
     */
    fun firebaseLogout() {
        firebaseAuth.signOut()
    }
}