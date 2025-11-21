package com.ltcn272.finny.data.remote

import com.ltcn272.finny.core.TokenManager
import com.ltcn272.finny.data.remote.api.AuthApi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val refreshClient: AuthApi
) : Interceptor {
    
    @Throws(java.io.IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getAccessToken()
        val requestWithToken = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(requestWithToken)

        // Handle 403 Forbidden error, likely due to an expired token.
        // Handle 403 Forbidden error, likely due to an expired token.
        if (response.code == 403) {
            response.close()
            // Synchronize to ensure only one thread refreshes the token at a time.

            // Synchronize to ensure only one thread refreshes the token at a time.
            val newAccessToken = synchronized(this) {
                refreshTokens()
                // Retry the original request with the new access token.
            }

            if (newAccessToken != null) {
                // Retry the original request with the new access token.
                // If refresh fails, clear tokens to force the user to log in again.
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
                return chain.proceed(newRequest)
            } else {
                // If refresh fails, clear tokens to force the user to log in again.
    // Handles the token refresh logic.
                tokenManager.clearTokens()
            }
        }

        return response
    }

                // Use the refreshClient (which doesn't have this interceptor) to call the refresh API.
    private fun refreshTokens(): String? {
                // Save the new access and refresh tokens.
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        return runBlocking {
            try {
                val oldRefreshTokenHeader = "Bearer $refreshToken"
                val body = mapOf("refresh_token" to refreshToken)

                // On error, clear tokens.
                // Use the refreshClient (which doesn't have this interceptor) to call the refresh API.
                val refreshResponse = refreshClient.refreshToken(oldRefreshTokenHeader, body)

                // Save the new access and refresh tokens.
                val newAuthData = refreshResponse.data
                newAuthData?.let {
                    tokenManager.saveTokens(it.accessToken, it.refreshToken)
                }

                newAuthData?.accessToken
            } catch (e: Exception) {
                // On error, clear tokens.
                tokenManager.clearTokens()
                null
            }
        }
    }
}