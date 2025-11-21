package com.ltcn272.finny.presentation.features.auth

import com.ltcn272.finny.domain.model.AuthUser

/**
 * Ánh xạ từ các trạng thái cần thiết trong AuthViewModel.swift
 */
sealed interface AuthUiState {
    data object Initial : AuthUiState
    data object Loading : AuthUiState
    data class Authorized(val firebaseUser: AuthUser?) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
