package com.ltcn272.finny.core

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_SEEN = "onboarding_seen"
    }


    fun isOnboardingSeen(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_SEEN, false)
    }

    fun markOnboardingSeen() {
        prefs.edit {
            putBoolean(KEY_ONBOARDING_SEEN, true)
        }
    }
}
