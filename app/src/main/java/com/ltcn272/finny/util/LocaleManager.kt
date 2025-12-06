package com.ltcn272.finny.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleManager {

    fun applyLocale(context: Context, languageTag: String?): Context {
        if (languageTag == null) {
            try {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            } catch (e: Throwable) {
                // ignore
            }
            return context
        }

        val locale = Locale.forLanguageTag(languageTag)

        return try {
            applyUsingAppCompat(languageTag)
            context
        } catch (ex: Throwable) {
            applyUsingContextWrapper(context, locale)
        }
    }

    private fun applyUsingAppCompat(languageTag: String) {
        val locales = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    private fun applyUsingContextWrapper(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLocales(android.os.LocaleList(locale))
        return context.createConfigurationContext(config)
    }
}

