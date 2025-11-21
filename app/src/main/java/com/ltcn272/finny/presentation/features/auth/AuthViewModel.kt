package com.ltcn272.finny.presentation.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val authState: StateFlow<AuthUiState> = _authState

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                authRepository.loginWithGoogle(idToken)
                val user = authRepository.currentUser.first()
                _authState.value = AuthUiState.Authorized(firebaseUser = user)
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Google login failed")
            }
        }
    }

    fun loginWithFacebook(accessToken: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                authRepository.loginWithFacebook(accessToken)
                val user = authRepository.currentUser.first()
                _authState.value = AuthUiState.Authorized(firebaseUser = user)
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Facebook login failed")
            }
        }
    }

    fun cancelLoadingIfStuck(errorMessage: String) {
        if (_authState.value is AuthUiState.Loading) {
            _authState.value = AuthUiState.Error(errorMessage)
        }
    }

}