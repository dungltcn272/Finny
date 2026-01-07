package com.ltcn272.finny.presentation.features.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.presentation.features.snackbar.SnackbarManager
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Initial : AuthUiState()
    data object Loading : AuthUiState()
    data class Authorized(val user: User) : AuthUiState()
    data object Error : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val authState: StateFlow<AuthUiState> = _authState

    fun loginWithGoogle(idToken: String, snackbarManager: SnackbarManager) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            when (val result = authRepository.loginWithGoogle(idToken)) {
                is AppResult.Success -> {
                    snackbarManager.showMessage(context.getString(R.string.welcome_user, result.data.displayName), TopSnackbarType.SUCCESS)
                    _authState.value = AuthUiState.Authorized(result.data)
                }
                is AppResult.Error -> {
                    snackbarManager.showMessage(context.getString(R.string.error_unknown), TopSnackbarType.ERROR)
                    _authState.value = AuthUiState.Error
                }
                is AppResult.Loading -> {}
            }
        }
    }

    fun loginWithFacebook(accessToken: String, snackbarManager: SnackbarManager) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            when (val result = authRepository.loginWithFacebook(accessToken)) {
                is AppResult.Success -> {
                    snackbarManager.showMessage(context.getString(R.string.welcome_user, result.data.displayName), TopSnackbarType.SUCCESS)
                    _authState.value = AuthUiState.Authorized(result.data)
                }
                is AppResult.Error -> {
                    snackbarManager.showMessage(context.getString(R.string.error_unknown), TopSnackbarType.ERROR)
                    _authState.value = AuthUiState.Error
                }
                is AppResult.Loading -> {}
            }
        }
    }

    fun cancelLoadingIfStuck(errorMessage: String, snackbarManager: SnackbarManager) {
        if (_authState.value is AuthUiState.Loading) {
            snackbarManager.showMessage(errorMessage, TopSnackbarType.ERROR)
            _authState.value = AuthUiState.Error
        }
    }
}
