package com.ergou.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.nextAuthDataStore by preferencesDataStore(name = "next_auth")

class NextAuthProvider(private val context: Context) {

    companion object {
        private val SESSION_TOKEN = stringPreferencesKey("session_token")
        private val USERNAME = stringPreferencesKey("username")
    }

    val sessionToken: Flow<String> = context.nextAuthDataStore.data.map { prefs ->
        prefs[SESSION_TOKEN] ?: ""
    }

    val username: Flow<String> = context.nextAuthDataStore.data.map { prefs ->
        prefs[USERNAME] ?: ""
    }

    val isLoggedIn: Flow<Boolean> = sessionToken.map { it.isNotBlank() }

    suspend fun saveSession(token: String, username: String) {
        context.nextAuthDataStore.edit { prefs ->
            prefs[SESSION_TOKEN] = token
            prefs[USERNAME] = username
        }
    }

    suspend fun clearSession() {
        context.nextAuthDataStore.edit { prefs ->
            prefs.remove(SESSION_TOKEN)
            prefs.remove(USERNAME)
        }
    }
}
