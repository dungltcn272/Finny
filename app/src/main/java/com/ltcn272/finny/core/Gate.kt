package com.ltcn272.finny.core

import android.content.Context
import androidx.core.content.edit

object Gate {
    private const val PREFS_APP = "app_prefs"
    private const val KEY_ONBOARD = "onboarding_seen"
    private const val PREFS_AUTH = "AUTH_PREFS_FINNY"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    fun isFirstLaunch(ctx: Context): Boolean {
        val p = ctx.getSharedPreferences(PREFS_APP, Context.MODE_PRIVATE)
        return !p.getBoolean(KEY_ONBOARD, false)
    }

    fun markOnboardingSeen(ctx: Context) {
        val p = ctx.getSharedPreferences(PREFS_APP, Context.MODE_PRIVATE)
        p.edit { putBoolean(KEY_ONBOARD, true) }
    }

    fun isLoggedIn(ctx: Context): Boolean {
        val p = ctx.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        return !p.getString(KEY_ACCESS, null).isNullOrEmpty() &&
                !p.getString(KEY_REFRESH, null).isNullOrEmpty()
    }
}