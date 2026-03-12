package com.example.yearprogress.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class LanguageManager(base: Context) : ContextWrapper(base) {

    companion object {
        fun changeLanguage(context: Context, languageCode: String): ContextWrapper {
            val resources = context.resources
            val configuration = resources.configuration
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            } else {
                configuration.setLocale(locale)
            }
            val contextWithUpdatedConfig = context.createConfigurationContext(configuration)
            return ContextWrapper(contextWithUpdatedConfig)
        }
    }
}