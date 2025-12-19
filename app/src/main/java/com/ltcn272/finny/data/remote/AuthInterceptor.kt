package com.ltcn272.finny.data.remote

import com.ltcn272.finny.core.TokenManager
import com.ltcn272.finny.data.remote.api.AuthApi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val refreshClient: AuthApi
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = tokenManager.getAccessToken()

        val requestWithToken = if (accessToken != null) {
            addAuthHeader(originalRequest, accessToken)
        } else {
            return chain.proceed(originalRequest)
        }

        val response = chain.proceed(requestWithToken)

        if (response.code == 401 || response.code == 403) {
            response.close()

            synchronized(this) {
                val newAccessToken: String?

                val currentTokenAfterSync = tokenManager.getAccessToken()

                newAccessToken = if (currentTokenAfterSync != null && currentTokenAfterSync != accessToken) {
                    currentTokenAfterSync
                } else {
                    refreshToken()
                }

                if (newAccessToken != null) {
                    return chain.proceed(addAuthHeader(originalRequest, newAccessToken))
                } else {
                    tokenManager.clearTokens()
                }
            }
        }
        return response
    }

    private fun addAuthHeader(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun refreshToken(): String? {
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        return try {
            val refreshResponse = runBlocking {
                val oldRefreshTokenHeader = "Bearer $refreshToken"
                val body = mapOf("refresh_token" to refreshToken)
                refreshClient.refreshToken(oldRefreshTokenHeader, body)
            }

            val newAuthData = refreshResponse.data
            tokenManager.saveTokens(newAuthData.accessToken, newAuthData.refreshToken)

            newAuthData.accessToken
        } catch (e: Exception) {
            tokenManager.clearTokens()
            null
        }
    }
}
