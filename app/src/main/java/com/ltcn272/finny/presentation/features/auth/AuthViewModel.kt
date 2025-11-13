package com.ltcn272.finny.presentation.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.core.LoginStateManager
import com.ltcn272.finny.core.SyncScheduler
import com.ltcn272.finny.data.auth.FirebaseAuthClient
import com.ltcn272.finny.data.remote.AuthInterceptor // Dùng để set token tạm
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    // 1. Repository và Client
    private val authRepository: AuthRepository,
    private val firebaseAuthClient: FirebaseAuthClient,

    // 2. Core services
    private val authInterceptor: AuthInterceptor, // Cần cho setFirebaseIdToken
    private val loginStateManager: LoginStateManager,
    private val syncScheduler: SyncScheduler // Để bắt đầu sync sau khi login
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // --- Phương thức Khởi tạo Login (Gọi từ UI) ---

    fun onGoogleSignInClicked() {
        _uiState.update { it.copy(isLoading = true, loginError = null) }
        // Sau đó UI sẽ gọi Activity Result Launcher (ví dụ: handleActivityResult)
    }

    fun onFacebookSignInClicked() {
        _uiState.update { it.copy(isLoading = true, loginError = null) }
    }

    // --- Phương thức Xử lý Kết quả SDK (Gọi từ Activity/Composable) ---

    /**
     * Xử lý sau khi Google/Facebook SDK trả về chứng chỉ thành công.
     * @param credentialToken (Ví dụ: Google ID Token, Facebook Access Token)
     * @param loginMethod "google" hoặc "facebook"
     */
    fun handleSocialSignInSuccess(credentialToken: String, loginMethod: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }

            val firebaseResult = try {
                // 1. Đăng nhập Firebase bằng chứng chỉ SDK
                when (loginMethod) {
                    "google" -> firebaseAuthClient.signInWithGoogle(credentialToken)
                    "facebook" -> firebaseAuthClient.signInWithFacebook(credentialToken)
                    else -> throw IllegalArgumentException("Unknown login method")
                }
                // 2. Lấy Firebase ID Token sau khi đăng nhập thành công
                firebaseAuthClient.getIdToken()
            } catch (e: Exception) {
                // Lỗi Firebase SDK
                handleLoginFailure("SDK Login Failed: ${e.localizedMessage}")
                return@launch
            }

            if (firebaseResult == null) {
                handleLoginFailure("Failed to get Firebase ID Token.")
                return@launch
            }

            // 3. Login với Backend API
            when (val backendResult = backendLogin(firebaseResult)) {
                is AppResult.Success -> {
                    // 4. Thành công: Bắt đầu đồng bộ định kỳ
                    syncScheduler.schedulePeriodicSync()
                    _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
                }
                is AppResult.Error -> {
                    handleLoginFailure(backendResult.message)
                }

                AppResult.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    /**
     * Logic Login Backend (đã chuyển từ Use Case)
     */
    private suspend fun backendLogin(firebaseIdToken: String): AppResult<User> {
        // 1. Thiết lập Firebase ID Token vào Interceptor
        authInterceptor.setFirebaseIdToken(firebaseIdToken)

        // 2. Gọi Repository (Kiểu trả về: AppResult<Pair<User, AuthToken>>)
        // 3. Xử lý và chuyển đổi kiểu dữ liệu (TRUNG TÂM CỦA VIỆC KHẮC PHỤC LỖI)
        return when (val loginResult = authRepository.backendLogin(firebaseIdToken)) {
            is AppResult.Success -> {
                // Trích xuất User từ Pair và trả về AppResult<User>
                val user = loginResult.data.first
                AppResult.Success(user)
            }
            is AppResult.Error -> {
                // Xóa token và trả về lỗi
                authInterceptor.clearFirebaseIdToken()
                AppResult.Error(loginResult.message, loginResult.throwable)
            }
            // Trường hợp Loading (nếu có trong AppResult)
            AppResult.Loading -> AppResult.Loading
        }
    }


    /**
     * Xử lý khi quá trình SDK hoặc Backend Login bị hủy/thất bại.
     */
    fun handleLoginFailure(message: String) {
        viewModelScope.launch {
            // Đảm bảo logout khỏi Firebase
            firebaseAuthClient.firebaseLogout()
            loginStateManager.onLogout() // Xóa trạng thái đăng nhập cục bộ
            _uiState.update { it.copy(isLoading = false, loginError = message) }
        }
    }
}