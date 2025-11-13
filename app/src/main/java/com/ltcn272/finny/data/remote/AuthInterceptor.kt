package com.ltcn272.finny.data.remote

import com.ltcn272.finny.core.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        private const val LOGIN_PATH = "auth/login"
        private const val BEARER = "Bearer "
    }

    // Biến tạm để giữ Firebase ID Token cho request Login duy nhất
    private var firebaseIdToken: String? = null

    /**
     * Dùng để thiết lập Firebase ID Token (từ Google/FB SDK) cho request POST /auth/login.
     */
    fun setFirebaseIdToken(idToken: String) {
        this.firebaseIdToken = idToken
    }

    /**
     * Xóa Firebase ID Token sau khi request Login hoàn tất.
     */
    fun clearFirebaseIdToken() {
        this.firebaseIdToken = null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        val requestBuilder = originalRequest.newBuilder()

        val token: String? = when {
            // 1. Nếu đang là request Login VÀ đã có Firebase ID Token
            path.endsWith(LOGIN_PATH) && firebaseIdToken != null -> {
                firebaseIdToken
            }
            // 2. Các request khác (Protected Requests)
            else -> {
                tokenManager.getAccessToken() // Lấy Backend Access Token đã lưu
            }
        }

        // Thêm Header Authorization
        if (token != null) {
            requestBuilder.header("Authorization", BEARER + token)
        }

        // Tiến hành request
        val response = chain.proceed(requestBuilder.build())

        // Dọn dẹp Firebase ID Token ngay sau khi request Login hoàn tất (ngay cả khi lỗi)
        if (path.endsWith(LOGIN_PATH)) {
            clearFirebaseIdToken()
        }

        // LƯU Ý: Logic xử lý lỗi 401 (Refresh Token) KHÔNG NÊN ĐẶT Ở ĐÂY.
        // Nó sẽ được đặt ở OkHttp Authenticator (TokenAuthenticator.kt) để xử lý gọn gàng hơn.

        return response
    }
}