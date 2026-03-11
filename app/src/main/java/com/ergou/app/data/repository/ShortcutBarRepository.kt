package com.ergou.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val Context.shortcutDataStore: DataStore<Preferences> by preferencesDataStore(name = "shortcut_bar")

@Serializable
data class ShortcutItem(
    val route: String,
    val category: String,
    val label: String,
    val lastUsedAt: Long = 0L,
    val pinned: Boolean = false,
    val badge: String? = null
)

class ShortcutBarRepository(
    private val context: Context,
    private val nextApiService: com.ergou.app.data.remote.api.NextApiService,
    private val authProvider: com.ergou.app.util.NextAuthProvider
) {

    private val usageKey = stringPreferencesKey("shortcut_usage")
    private val pinnedKey = stringPreferencesKey("shortcut_pinned")

    private val defaultShortcuts = listOf(
        ShortcutItem(route = "task", category = "Todo", label = "任务"),
        ShortcutItem(route = "routine", category = "例行", label = "例行"),
        ShortcutItem(route = "expense", category = "记账", label = "记账"),
        ShortcutItem(route = "trip", category = "差旅", label = "出差"),
        ShortcutItem(route = "english", category = "学习", label = "英语"),
        ShortcutItem(route = "health", category = "养生", label = "养生")
    )

    // Default pinned routes (first 4 in order)
    private val defaultPinnedRoutes = listOf("task", "routine", "expense", "trip")

    private val mapSerializer = MapSerializer(String.serializer(), Long.serializer())
    private val listSerializer = ListSerializer(String.serializer())

    private val _todoBadge = MutableStateFlow<String?>(null)

    fun getShortcuts(): Flow<List<ShortcutItem>> {
        return context.shortcutDataStore.data.combine(_todoBadge) { prefs, badge ->
            val usageJson = prefs[usageKey]
            val pinnedJson = prefs[pinnedKey]
            val usageMap = if (usageJson != null) {
                Json.decodeFromString(mapSerializer, usageJson)
            } else {
                emptyMap()
            }
            val pinnedRoutes = if (pinnedJson != null) {
                Json.decodeFromString(listSerializer, pinnedJson)
            } else {
                defaultPinnedRoutes
            }

            val allItems = defaultShortcuts.map { item ->
                item.copy(
                    lastUsedAt = usageMap[item.route] ?: 0L,
                    pinned = item.route in pinnedRoutes,
                    badge = if (item.route == "task") badge else null
                )
            }

            // Pinned items first (in pinned order), then unpinned sorted by usage
            val pinned = pinnedRoutes.mapNotNull { route ->
                allItems.find { it.route == route }
            }
            val unpinned = allItems.filter { it.route !in pinnedRoutes }
                .sortedByDescending { it.lastUsedAt }

            pinned + unpinned
        }
    }

    suspend fun refreshTodoBadge() {
        val loggedIn = authProvider.isLoggedIn.first()
        if (!loggedIn) {
            _todoBadge.value = null
            return
        }
        nextApiService.getTodoCounts("today").fold(
            onSuccess = { response ->
                val pending = response.pending
                _todoBadge.value = if (pending > 0) "$pending 项" else null
            },
            onFailure = { e ->
                Timber.w(e, "[ShortcutBar] 获取待办计数失败")
                _todoBadge.value = null
            }
        )
    }

    suspend fun recordUsage(route: String) {
        context.shortcutDataStore.edit { prefs ->
            val json = prefs[usageKey]
            val usageMap: MutableMap<String, Long> = if (json != null) {
                Json.decodeFromString(mapSerializer, json).toMutableMap()
            } else {
                mutableMapOf()
            }
            usageMap[route] = System.currentTimeMillis()
            prefs[usageKey] = Json.encodeToString(mapSerializer, usageMap)
        }
    }

    suspend fun getPinnedRoutes(): List<String> {
        val prefs = context.shortcutDataStore.data.first()
        val json = prefs[pinnedKey]
        return if (json != null) {
            Json.decodeFromString(listSerializer, json)
        } else {
            defaultPinnedRoutes
        }
    }

    suspend fun setPinnedRoutes(routes: List<String>) {
        context.shortcutDataStore.edit { prefs ->
            prefs[pinnedKey] = Json.encodeToString(listSerializer, routes)
        }
    }
}
