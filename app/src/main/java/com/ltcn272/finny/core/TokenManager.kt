package com.ltcn272.finny.core

import android.content.Context
import com.ltcn272.finny.domain.model.AuthToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

// Sử dụng Preference cho việc lưu trữ token.
@Singleton
class TokenManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }

    /**
     * Lưu trữ Access Token và Refresh Token nhận được từ API.
     */
    fun saveTokens(tokens: AuthToken) {
        prefs.edit {
            putString(ACCESS_TOKEN_KEY, tokens.accessToken)
                .putString(REFRESH_TOKEN_KEY, tokens.refreshToken)
        }
    }

    /**
     * Lấy Access Token để sử dụng trong các request.
     */
    fun getAccessToken(): String? {
        // Lưu ý: Có thể cần thêm logic kiểm tra token hết hạn ở lớp Interceptor
        return prefs.getString(ACCESS_TOKEN_KEY, null)
    }

    /**
     * Lấy Refresh Token.
     */
    fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN_KEY, null)
    }

    /**
     * Xóa tokens khi đăng xuất.
     */
    fun clearTokens() {
        prefs.edit {
            remove(ACCESS_TOKEN_KEY)
                .remove(REFRESH_TOKEN_KEY)
        }
    }
}