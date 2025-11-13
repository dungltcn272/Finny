package com.ltcn272.finny.core

import com.ltcn272.finny.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginStateManager @Inject constructor(
    private val tokenManager: TokenManager // Dùng để kiểm tra trạng thái token
) {
    // Trạng thái Flow để UI lắng nghe
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    // Trạng thái tổng quát: đã đăng nhập chưa (dựa trên sự tồn tại của Access Token)
    private val _isLoggedIn = MutableStateFlow(tokenManager.getAccessToken() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /**
     * Cần gọi sau khi BackendLoginUseCase thành công.
     * @param user Thông tin người dùng (Domain Model)
     */
    fun onSuccessfulLogin(user: User) {
        _userState.value = user
        _isLoggedIn.value = true
        // Lưu ý: AuthRepository đã lưu tokens
    }

    /**
     * Cần gọi khi người dùng Logout hoặc khi TokenAuthenticator thất bại.
     */
    fun onLogout() {
        tokenManager.clearTokens() // Xóa tokens khỏi SharedPreferences
        _userState.value = null
        _isLoggedIn.value = false
        // TODO: Cần xóa sạch Room DB nếu user không muốn giữ data cục bộ
    }

    /**
     * Cập nhật thông tin User sau khi fetchProfile hoặc updateProfile thành công.
     */
    fun onProfileUpdate(user: User) {
        _userState.value = user
    }
}