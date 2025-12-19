package com.ltcn272.finny.core

class Gate(
    private val tokenManager: TokenManager,
    private val onboardingManager: OnboardingManager
) {
    fun isFirstLaunch(): Boolean {
        return !onboardingManager.isOnboardingSeen()
    }
    fun isLoggedIn(): Boolean {
        return !tokenManager.getAccessToken().isNullOrBlank()
    }
}
