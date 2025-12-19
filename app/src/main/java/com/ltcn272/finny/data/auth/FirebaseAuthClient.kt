package com.ltcn272.finny.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider // <-- Cần import
import com.google.firebase.auth.FacebookAuthProvider // <-- Cần import
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthClient @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun signInWithGoogle(googleIdToken: String) {
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        firebaseAuth.signInWithCredential(credential).await()
    }

    suspend fun signInWithFacebook(facebookAccessToken: String) {
        val credential = FacebookAuthProvider.getCredential(facebookAccessToken)
        firebaseAuth.signInWithCredential(credential).await()
    }

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

    fun firebaseLogout() {
        firebaseAuth.signOut()
    }
}