package com.ltcn272.finny.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class AppStateManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("app_state_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val HAS_SEEN_INTRO_KEY = "has_seen_intro"
    }

    fun hasSeenIntro(): Boolean {
        // Mặc định là false (Chưa xem)
        return prefs.getBoolean(HAS_SEEN_INTRO_KEY, false)
    }

    fun setIntroSeen() {
        prefs.edit { putBoolean(HAS_SEEN_INTRO_KEY, true) }
    }
}