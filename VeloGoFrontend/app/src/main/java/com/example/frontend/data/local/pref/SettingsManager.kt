package com.example.frontend.data.local.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme")
    }

    val themeFlow: Flow<AppTheme> = context.settingsDataStore.data.map { preferences ->
        val themeStr = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
        try {
            AppTheme.valueOf(themeStr)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}
