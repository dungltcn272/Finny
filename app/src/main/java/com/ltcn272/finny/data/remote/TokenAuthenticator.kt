package com.ltcn272.finny.data.remote

import com.ltcn272.finny.core.TokenManager
import com.ltcn272.finny.domain.repository.AuthRepository // Cần truy cập Repository
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider // <-- QUAN TRỌNG: Phá vỡ vòng lặp
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    // THAY ĐỔI: Sử dụng Provider<AuthRepository> để phá vỡ vòng lặp
    private val authRepositoryProvider: Provider<AuthRepository>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Kiểm tra vòng lặp vô hạn (không cần thay đổi)
        if (response.request.header("Authorization")?.contains(tokenManager.getAccessToken() ?: "") == false) {
            return null
        }

        // 1. Lấy Refresh Token cục bộ
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        // 2. Thực hiện Refresh Token
        val newTokensResult = runBlocking {
            // GỌI REPOSITORY: Truy cập AuthRepository thông qua .get() để yêu cầu token mới
            authRepositoryProvider.get().refreshTokens(refreshToken)
        }

        return when (newTokensResult) {
            is AppResult.Success -> {
                // Thành công: Lấy Access Token mới
                val newAccessToken = newTokensResult.data.accessToken

                // Tạo lại request ban đầu với Access Token mới
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }
            is AppResult.Error -> {
                // Thất bại: Xóa tokens và yêu cầu đăng nhập lại
                tokenManager.clearTokens()
                null
            }

            AppResult.Loading -> TODO()
        }
    }
}