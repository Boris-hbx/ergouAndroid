package com.ergou.app.ui.health

import android.app.Application
import android.net.Uri
import androidx.annotation.OptIn
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextHealthCategory
import com.ergou.app.data.remote.dto.NextHealthItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HealthCategory(
    val id: String,
    val name: String,
    val description: String,
    val intro: String,
    val items: List<HealthItem>
)

data class MeridianDetail(
    val name: String,
    val intensity: String = "primary",
    val note: String = ""
)

data class InsightItem(
    val label: String,
    val content: String
)

data class HealthItem(
    val id: String,
    val name: String,
    val description: String,
    val benefits: String,
    val meridians: String = "",
    val keyPoints: String = "",
    val benefitsList: List<String> = emptyList(),
    val meridianDetails: List<MeridianDetail> = emptyList(),
    val insights: List<InsightItem> = emptyList(),
    val videoUrl: String = ""
)

data class HealthUiState(
    val categories: List<HealthCategory> = emptyList(),
    val selectedCategoryId: String = "baduanjin",
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val favorites: Set<String> = emptySet(),
    val showFavoritesOnly: Boolean = false,
    val expandedItems: Set<String> = emptySet(),
    val playingVideoItemId: String? = null,
    val isVideoBuffering: Boolean = false,
    // Meridian diagram state
    val meridianViewSide: String = "front",
    val selectedMeridianIds: Set<String> = MeridianData.meridians.map { it.id }.toSet(),
    val selectedAcupoint: Acupoint? = null,
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val practiceLog: Map<String, Set<String>> = emptyMap(),
    val todayPracticed: Set<String> = emptySet(),
    val streakDays: Int = 0,
    val totalPracticeCount: Int = 0
) {
    val filteredItems: List<HealthItem>
        get() {
            val category = categories.find { it.id == selectedCategoryId } ?: return emptyList()
            var items = category.items

            if (showFavoritesOnly) {
                items = items.filter { it.id in favorites }
            }

            if (searchQuery.isNotBlank()) {
                val query = searchQuery.lowercase()
                items = items.filter { item ->
                    item.name.lowercase().contains(query) ||
                            item.benefits.lowercase().contains(query) ||
                            item.keyPoints.lowercase().contains(query) ||
                            item.meridians.lowercase().contains(query)
                }
            }

            return items
        }
}

class HealthViewModel(
    private val dataStore: DataStore<Preferences>,
    private val nextApiService: NextApiService? = null,
    private val appContext: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage

    private var playerInitialized = false

    @OptIn(UnstableApi::class)
    val exoPlayer: ExoPlayer by lazy {
        playerInitialized = true
        val cache = getVideoCache(appContext)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15_000,
                /* maxBufferMs = */ 30_000,
                /* bufferForPlaybackMs = */ 500,
                /* bufferForPlaybackAfterRebufferMs = */ 1_000
            )
            .build()
        ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setLoadControl(loadControl)
            .build().also { player ->
                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _uiState.value = _uiState.value.copy(
                            isVideoBuffering = playbackState == Player.STATE_BUFFERING
                        )
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Timber.w(error, "[Health] 视频播放错误")
                        _uiState.value = _uiState.value.copy(
                            playingVideoItemId = null,
                            isVideoBuffering = false
                        )
                        viewModelScope.launch {
                            _snackbarMessage.emit("无法加载视频，请检查网络连接")
                        }
                    }
                })
            }
    }

    init {
        loadFavorites()
        loadPracticeLog()
        loadFromApi()
    }

    override fun onCleared() {
        super.onCleared()
        if (playerInitialized) {
            exoPlayer.release()
            Timber.d("[Health] ExoPlayer 已释放")
        }
    }

    private fun loadFromApi() {
        viewModelScope.launch {
            if (nextApiService == null) {
                _uiState.value = _uiState.value.copy(
                    categories = HealthData.categories,
                    isLoading = false,
                    isOffline = false
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val categoriesResult = nextApiService.getHealthCategories()
                val apiCategories = categoriesResult.getOrNull()

                if (apiCategories != null && apiCategories.isNotEmpty()) {
                    val fullCategories = apiCategories.map { apiCat ->
                        val itemsResult = nextApiService.getHealthItems(apiCat.id)
                        val apiItems = itemsResult.getOrNull() ?: emptyList()
                        apiCat.toHealthCategory(apiItems)
                    }
                    _uiState.value = _uiState.value.copy(
                        categories = fullCategories,
                        isLoading = false,
                        isOffline = false
                    )
                    Timber.d("[Health] API 加载成功 categories=%d", fullCategories.size)
                } else {
                    fallbackToLocal()
                }
            } catch (e: Exception) {
                Timber.w(e, "[Health] API 加载失败，降级到本地数据")
                fallbackToLocal()
            }
        }
    }

    private suspend fun fallbackToLocal() {
        _uiState.value = _uiState.value.copy(
            categories = HealthData.categories,
            isLoading = false,
            isOffline = true
        )
        _snackbarMessage.emit("使用本地数据")
    }

    fun selectCategory(id: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = id)
    }

    fun setSearchActive(active: Boolean) {
        _uiState.value = _uiState.value.copy(
            isSearchActive = active,
            searchQuery = if (!active) "" else _uiState.value.searchQuery
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleFavorite(itemId: String) {
        val current = _uiState.value.favorites
        val updated = if (itemId in current) current - itemId else current + itemId
        _uiState.value = _uiState.value.copy(favorites = updated)
        saveFavorites(updated)
        Timber.d("[Health] 切换收藏 itemId=%s isFavorite=%b", itemId, itemId in updated)
    }

    fun toggleShowFavoritesOnly() {
        _uiState.value = _uiState.value.copy(showFavoritesOnly = !_uiState.value.showFavoritesOnly)
    }

    fun toggleExpanded(itemId: String) {
        val current = _uiState.value.expandedItems
        val updated = if (itemId in current) current - itemId else current + itemId
        // Stop video when collapsing
        if (itemId !in updated && _uiState.value.playingVideoItemId == itemId) {
            stopVideo()
        }
        _uiState.value = _uiState.value.copy(expandedItems = updated)
    }

    fun toggleVideo(itemId: String, videoUrl: String) {
        val current = _uiState.value.playingVideoItemId
        if (current == itemId) {
            stopVideo()
        } else {
            playVideo(itemId, videoUrl)
        }
    }

    private fun playVideo(itemId: String, videoUrl: String) {
        _uiState.value = _uiState.value.copy(
            playingVideoItemId = itemId,
            isVideoBuffering = true
        )
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        Timber.d("[Health] 播放视频 itemId=%s", itemId)
    }

    private fun stopVideo() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _uiState.value = _uiState.value.copy(
            playingVideoItemId = null,
            isVideoBuffering = false
        )
    }

    // ── Meridian diagram controls ──

    fun toggleMeridianViewSide() {
        val newSide = if (_uiState.value.meridianViewSide == "front") "back" else "front"
        _uiState.value = _uiState.value.copy(meridianViewSide = newSide, selectedAcupoint = null)
    }

    fun toggleMeridianSelected(meridianId: String) {
        val current = _uiState.value.selectedMeridianIds
        val updated = if (meridianId in current) current - meridianId else current + meridianId
        _uiState.value = _uiState.value.copy(selectedMeridianIds = updated)
    }

    fun selectAllMeridians() {
        _uiState.value = _uiState.value.copy(
            selectedMeridianIds = MeridianData.meridians.map { it.id }.toSet()
        )
    }

    fun deselectAllMeridians() {
        _uiState.value = _uiState.value.copy(selectedMeridianIds = emptySet())
    }

    fun selectAcupoint(acupoint: Acupoint?) {
        _uiState.value = _uiState.value.copy(selectedAcupoint = acupoint)
    }

    fun togglePractice(itemId: String) {
        val today = LocalDate.now().format(DATE_FORMAT)
        val currentLog = _uiState.value.practiceLog.toMutableMap()
        val todaySet = (currentLog[today] ?: emptySet()).toMutableSet()

        if (itemId in todaySet) {
            todaySet.remove(itemId)
            Timber.d("[Health] 取消打卡 itemId=%s", itemId)
        } else {
            todaySet.add(itemId)
            Timber.d("[Health] 打卡 itemId=%s", itemId)
        }

        if (todaySet.isEmpty()) {
            currentLog.remove(today)
        } else {
            currentLog[today] = todaySet
        }

        updatePracticeState(currentLog)
        savePracticeLog(currentLog)
    }

    private fun loadPracticeLog() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = dataStore.data.first()
                val json = prefs[PRACTICE_LOG_KEY] ?: return@launch
                val parsed = parsePracticeLog(json)
                val cleaned = cleanExpiredEntries(parsed)
                if (cleaned.size < parsed.size) {
                    savePracticeLog(cleaned)
                }
                updatePracticeState(cleaned)
            } catch (e: Exception) {
                Timber.e(e, "[Health] 加载练习记录失败")
            }
        }
    }

    private fun updatePracticeState(log: Map<String, Set<String>>) {
        val today = LocalDate.now().format(DATE_FORMAT)
        val todayPracticed = log[today] ?: emptySet()
        val streak = calculateStreakDays(log)
        val total = log.values.sumOf { it.size }
        _uiState.value = _uiState.value.copy(
            practiceLog = log,
            todayPracticed = todayPracticed,
            streakDays = streak,
            totalPracticeCount = total
        )
    }

    private fun calculateStreakDays(log: Map<String, Set<String>>): Int {
        if (log.isEmpty()) return 0
        val today = LocalDate.now()
        // 宽松模式：今天有记录从今天开始，否则从昨天开始
        var date = if (log.containsKey(today.format(DATE_FORMAT))) today else today.minusDays(1)
        var streak = 0
        while (log.containsKey(date.format(DATE_FORMAT))) {
            streak++
            date = date.minusDays(1)
        }
        return streak
    }

    private fun savePracticeLog(log: Map<String, Set<String>>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonStr = serializePracticeLog(log)
                dataStore.edit { prefs ->
                    prefs[PRACTICE_LOG_KEY] = jsonStr
                }
            } catch (e: Exception) {
                Timber.e(e, "[Health] 保存练习记录失败")
            }
        }
    }

    private fun parsePracticeLog(json: String): Map<String, Set<String>> {
        return try {
            val obj = Json.parseToJsonElement(json).jsonObject
            obj.mapValues { (_, value) ->
                value.jsonArray.map { it.jsonPrimitive.content }.toSet()
            }
        } catch (e: Exception) {
            Timber.w(e, "[Health] 解析练习记录 JSON 失败")
            emptyMap()
        }
    }

    private fun serializePracticeLog(log: Map<String, Set<String>>): String {
        val obj = JsonObject(log.mapValues { (_, ids) ->
            JsonArray(ids.map { JsonPrimitive(it) })
        })
        return obj.toString()
    }

    private fun cleanExpiredEntries(log: Map<String, Set<String>>): Map<String, Set<String>> {
        val cutoff = LocalDate.now().minusDays(90)
        return log.filter { (dateStr, _) ->
            try {
                LocalDate.parse(dateStr, DATE_FORMAT) >= cutoff
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                val prefs = dataStore.data.first()
                val saved = prefs[FAVORITES_KEY] ?: emptySet()
                _uiState.value = _uiState.value.copy(favorites = saved)
            } catch (e: Exception) {
                Timber.w(e, "[Health] 加载收藏失败")
            }
        }
    }

    private fun saveFavorites(favorites: Set<String>) {
        viewModelScope.launch {
            try {
                dataStore.edit { prefs ->
                    prefs[FAVORITES_KEY] = favorites
                }
            } catch (e: Exception) {
                Timber.w(e, "[Health] 保存收藏失败")
            }
        }
    }

    companion object {
        private val FAVORITES_KEY = stringSetPreferencesKey("health_favorites")
        private val PRACTICE_LOG_KEY = stringPreferencesKey("practice_log")
        private val DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE

        private const val VIDEO_CACHE_SIZE = 200L * 1024 * 1024 // 200MB
        private var videoCache: SimpleCache? = null

        @OptIn(UnstableApi::class)
        @Synchronized
        fun getVideoCache(context: Application): SimpleCache {
            return videoCache ?: SimpleCache(
                File(context.cacheDir, "video_cache"),
                LeastRecentlyUsedCacheEvictor(VIDEO_CACHE_SIZE),
                StandaloneDatabaseProvider(context)
            ).also { videoCache = it }
        }
    }
}

private fun NextHealthCategory.toHealthCategory(items: List<NextHealthItem>): HealthCategory =
    HealthCategory(
        id = id,
        name = name,
        description = description,
        intro = intro,
        items = items.map { it.toHealthItem() }
    )

private fun NextHealthItem.toHealthItem(): HealthItem =
    HealthItem(
        id = id,
        name = name,
        description = description,
        benefits = benefits,
        meridians = meridians.joinToString("、"),
        keyPoints = keyPoints.joinToString("、"),
        benefitsList = benefitsList,
        meridianDetails = meridianDetails.map {
            MeridianDetail(name = it.name, intensity = it.intensity, note = it.note)
        },
        insights = insights.map {
            InsightItem(label = it.label, content = it.content)
        },
        videoUrl = videoUrl
    )
