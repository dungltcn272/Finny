package com.ltcn272.finny.presentation.features.auth

/**
 * Ánh xạ từ các trạng thái cần thiết trong AuthViewModel.swift
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val isLoginSuccessful: Boolean = false, // Trạng thái chuyển màn hình
    val isGoogleSdkReady: Boolean = false,
    val isFacebookSdkReady: Boolean = false
)