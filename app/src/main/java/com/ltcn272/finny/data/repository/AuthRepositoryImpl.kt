package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.mapper.toAuthToken
import com.ltcn272.finny.data.mapper.toDomain
import com.ltcn272.finny.data.mapper.toUpdateRequestDto
import com.ltcn272.finny.data.remote.api.AuthApi
import com.ltcn272.finny.domain.model.AuthToken
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.core.TokenManager
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    /**
     * LOGIN: Sau khi có Firebase ID Token, lưu nó vào Interceptor để API call.
     * @param idToken Firebase ID Token (hoặc ID Token từ Google/Facebook SDK)
     */
    override suspend fun backendLogin(idToken: String): AppResult<Pair<User, AuthToken>> {
        return try {
            // response là ApiResponseDto<AuthResponseDto>
            val response = authApi.login()

            if (response.status == 200 && response.data != null) {
                // response.data là AuthResponseDto. Gọi toAuthToken() đã hoạt động.
                val newAuthToken = response.data.toAuthToken()
                tokenManager.saveTokens(newAuthToken)

                AppResult.Success(
                    Pair(
                        // response.data.user là UserDto. Gọi toDomain() đã hoạt động.
                        response.data.user.toDomain(),
                        newAuthToken
                    )
                )
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error("Login failed: ${e.localizedMessage}", e)
        }
    }

    // Refresh Tokens
    override suspend fun refreshTokens(refreshToken: String): AppResult<AuthToken> {
        return try {
            // Refresh Token được truyền trong Body, và Access Token hiện tại được truyền trong Header
            // để bảo mật request.
            val currentAccessToken = tokenManager.getAccessToken()
                ?: return AppResult.Error("Access token not found for refresh request.")

            val requestDto = com.ltcn272.finny.data.remote.dto.RefreshRequestDto(refreshToken)
            val response = authApi.refreshToken("Bearer $currentAccessToken", requestDto)

            if (response.status == 200 && response.data != null) {
                val newTokens = response.data.toAuthToken()
                tokenManager.saveTokens(newTokens)
                AppResult.Success(newTokens)
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error("Token refresh failed: ${e.localizedMessage}", e)
        }
    }

    // ... (Các hàm khác như fetchProfile, updateProfile giữ nguyên logic sử dụng mapper)

    override suspend fun fetchProfile(): com.ltcn272.finny.domain.util.AppResult<User> {
        return try {
            val response = authApi.getProfile()
            if (response.status == 200 && response.data != null) {
                // SỬA LỖI 1: Gọi toDomain() trên UserDto từ API response data
                AppResult.Success(response.data.toDomain())
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error("Fetch profile failed: ${e.localizedMessage}", e)
        }
    }

    // Update Profile
    override suspend fun updateProfile(user: User): com.ltcn272.finny.domain.util.AppResult<User> {
        return try {
            // SỬA LỖI 2: Tạo Request DTO từ Domain Model
            val requestDto = user.toUpdateRequestDto()
            val response = authApi.updateProfile(requestDto)

            if (response.status == 200 && response.data != null) {
                // SỬA LỖI 3: Gọi toDomain() trên UserDto từ API response data
                AppResult.Success(response.data.toDomain())
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            com.ltcn272.finny.domain.util.AppResult.Error(
                "Update profile failed: ${e.localizedMessage}",
                e
            )
        }
    }
}