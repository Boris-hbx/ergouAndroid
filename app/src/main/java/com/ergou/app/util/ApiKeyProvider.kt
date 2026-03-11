package com.ergou.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** 主题模式：跟随系统 / 亮色 / 暗色 */
enum class ThemeMode(val value: Int) {
    SYSTEM(0), LIGHT(1), DARK(2);
    companion object {
        fun fromValue(v: Int) = entries.firstOrNull { it.value == v } ?: SYSTEM
    }
}

/** 模型提供商 */
enum class ModelProvider(val value: Int, val displayName: String) {
    DEEPSEEK(0, "DeepSeek"),
    CLAUDE(1, "Claude");
    companion object {
        fun fromValue(v: Int) = entries.firstOrNull { it.value == v } ?: DEEPSEEK
    }
}

class ApiKeyProvider(private val context: Context) {

    companion object {
        private val API_KEY = stringPreferencesKey("deepseek_api_key")
        private val CLAUDE_API_KEY = stringPreferencesKey("claude_api_key")
        private val THEME_MODE = intPreferencesKey("theme_mode")
        private val MODEL_PROVIDER = intPreferencesKey("model_provider")
        private val NICKNAME = stringPreferencesKey("nickname")
        private val AVATAR = stringPreferencesKey("avatar")  // drawable resource name, e.g. "preset_boris"
    }

    val apiKey: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[API_KEY] ?: ""
    }

    val claudeApiKey: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[CLAUDE_API_KEY] ?: ""
    }

    val modelProvider: Flow<ModelProvider> = context.settingsDataStore.data.map { prefs ->
        ModelProvider.fromValue(prefs[MODEL_PROVIDER] ?: 0)
    }

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        ThemeMode.fromValue(prefs[THEME_MODE] ?: 0)
    }

    val nickname: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[NICKNAME] ?: ""
    }

    val avatar: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[AVATAR] ?: ""
    }

    /** 获取当前选中提供商的 API Key */
    suspend fun activeApiKey(): String {
        val provider = modelProvider.first()
        return when (provider) {
            ModelProvider.DEEPSEEK -> apiKey.first()
            ModelProvider.CLAUDE -> claudeApiKey.first()
        }
    }

    suspend fun saveApiKey(key: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[API_KEY] = key
        }
    }

    suspend fun saveClaudeApiKey(key: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[CLAUDE_API_KEY] = key
        }
    }

    suspend fun saveModelProvider(provider: ModelProvider) {
        context.settingsDataStore.edit { prefs ->
            prefs[MODEL_PROVIDER] = provider.value
        }
    }

    suspend fun saveThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs ->
            prefs[THEME_MODE] = mode.value
        }
    }

    suspend fun saveNickname(name: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[NICKNAME] = name
        }
    }

    suspend fun saveAvatar(resName: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[AVATAR] = resName
        }
    }
}
