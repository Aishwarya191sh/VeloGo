package com.example.frontend.data.local.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

@Singleton
class SessionManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_ROLE = stringPreferencesKey("user_role") // "user", "vendor", "admin"
        private val USER_PHONE = stringPreferencesKey("user_phone")
        private val USER_ADRESS = stringPreferencesKey("user_adress")
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN]
    }

    val sessionDetailsFlow: Flow<UserSession?> = context.dataStore.data.map { preferences ->
        val id = preferences[USER_ID]
        val name = preferences[USER_NAME]
        val email = preferences[USER_EMAIL]
        val role = preferences[USER_ROLE]
        val phone = preferences[USER_PHONE]
        val address = preferences[USER_ADRESS]
        if (id != null && name != null && email != null && role != null) {
            UserSession(id, name, email, role, phone, address)
        } else {
            null
        }
    }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        username: String,
        email: String,
        role: String,
        phoneNumber: String? = null,
        adress: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[USER_ID] = userId
            preferences[USER_NAME] = username
            preferences[USER_EMAIL] = email
            preferences[USER_ROLE] = role
            if (phoneNumber != null) preferences[USER_PHONE] = phoneNumber else preferences.remove(USER_PHONE)
            if (adress != null) preferences[USER_ADRESS] = adress else preferences.remove(USER_ADRESS)
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun updateProfileDetails(username: String, email: String, phoneNumber: String?, adress: String?) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = username
            preferences[USER_EMAIL] = email
            if (phoneNumber != null) preferences[USER_PHONE] = phoneNumber else preferences.remove(USER_PHONE)
            if (adress != null) preferences[USER_ADRESS] = adress else preferences.remove(USER_ADRESS)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

data class UserSession(
    val userId: String,
    val username: String,
    val email: String,
    val role: String,
    val phoneNumber: String? = null,
    val adress: String? = null
)
