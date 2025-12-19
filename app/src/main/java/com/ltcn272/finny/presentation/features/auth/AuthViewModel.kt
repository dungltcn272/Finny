package com.ltcn272.finny.presentation.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Initial : AuthUiState()
    data object Loading : AuthUiState()
    data class Authorized(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val authState: StateFlow<AuthUiState> = _authState

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            when (val result = authRepository.loginWithGoogle(idToken)) {
                is AppResult.Success -> {
                    _authState.value = AuthUiState.Authorized(result.data)
                }
                is AppResult.Error -> {
                    _authState.value = AuthUiState.Error(
                        result.errorType.toString()
                    )
                }
                is AppResult.Loading -> {
                }
            }
        }
    }

    fun loginWithFacebook(accessToken: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            when (val result = authRepository.loginWithFacebook(accessToken)) {
                is AppResult.Success -> {
                    _authState.value = AuthUiState.Authorized(result.data)
                }
                is AppResult.Error -> {
                    _authState.value = AuthUiState.Error(
                        result.errorType.toString()
                    )
                }
                is AppResult.Loading -> {
                }
            }
        }
    }

    fun cancelLoadingIfStuck(errorMessage: String) {
        if (_authState.value is AuthUiState.Loading) {
            _authState.value = AuthUiState.Error(errorMessage)
        }
    }
}
