package com.ergou.app.ui.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextReview
import com.ergou.app.data.remote.dto.NextReviewCreateRequest
import com.ergou.app.data.remote.dto.NextReviewUpdateRequest
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

enum class FrequencyTab(val label: String, val apiValue: String) {
    DAILY("日", "daily"),
    WEEKLY("周", "weekly"),
    MONTHLY("月", "monthly"),
    YEARLY("年", "yearly")
}

data class RoutineUiState(
    val reviews: List<NextReview> = emptyList(),
    val selectedTab: FrequencyTab = FrequencyTab.DAILY,
    val selectedReview: NextReview? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val progressMap: Map<String, Float> = emptyMap()
)

class RoutineViewModel(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState

    init {
        viewModelScope.launch {
            authProvider.isLoggedIn.collect { loggedIn ->
                _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
                if (loggedIn) {
                    refreshAll()
                } else {
                    _uiState.value = _uiState.value.copy(reviews = emptyList())
                }
            }
        }
    }

    fun selectTab(tab: FrequencyTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val reviewResult = nextApiService.getReviews()
            reviewResult.fold(
                onSuccess = { reviews ->
                    val sorted = reviews.sortedWith(
                        compareBy<NextReview> { r -> r.paused }
                            .thenBy { r -> dueStatusOrder(r.dueStatus) }
                    )
                    val updatedSelectedReview = _uiState.value.selectedReview?.let { sel ->
                        sorted.find { it.id == sel.id }
                    }
                    _uiState.value = _uiState.value.copy(
                        reviews = sorted,
                        selectedReview = updatedSelectedReview,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "[Routine] 刷新例行失败")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    fun filteredReviews(): List<NextReview> {
        val state = _uiState.value
        return when (state.selectedTab) {
            FrequencyTab.DAILY -> state.reviews.filter { it.frequency == "daily" }
            FrequencyTab.WEEKLY -> state.reviews.filter { it.frequency == "weekly" }
            FrequencyTab.MONTHLY -> state.reviews.filter { it.frequency == "monthly" || it.frequency == "quarterly" }
            FrequencyTab.YEARLY -> state.reviews.filter { it.frequency == "yearly" }
        }
    }

    // ── Review actions ──

    fun selectReview(review: NextReview?) {
        _uiState.value = _uiState.value.copy(selectedReview = review)
    }

    fun updateProgress(id: String, progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(
            progressMap = _uiState.value.progressMap + (id to clamped)
        )
    }

    fun addReview(text: String, frequency: String, category: String?) {
        viewModelScope.launch {
            val result = nextApiService.createReview(
                NextReviewCreateRequest(text = text, frequency = frequency, category = category)
            )
            result.fold(
                onSuccess = { refreshAll() },
                onFailure = { e ->
                    Timber.e(e, "[Routine] 添加例行失败")
                    _uiState.value = _uiState.value.copy(error = "添加失败：${e.message}")
                }
            )
        }
    }

    fun completeReview(id: String) {
        viewModelScope.launch {
            val result = nextApiService.completeReview(id)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        progressMap = _uiState.value.progressMap + (id to 0f)
                    )
                    refreshAll()
                },
                onFailure = { e ->
                    Timber.e(e, "[Routine] 完成例行失败")
                    _uiState.value = _uiState.value.copy(error = "操作失败：${e.message}")
                }
            )
        }
    }

    fun togglePause(id: String, currentlyPaused: Boolean) {
        viewModelScope.launch {
            val result = nextApiService.updateReview(id, NextReviewUpdateRequest(paused = !currentlyPaused))
            result.fold(
                onSuccess = { refreshAll() },
                onFailure = { e ->
                    Timber.e(e, "[Routine] 切换暂停失败")
                    _uiState.value = _uiState.value.copy(error = "操作失败：${e.message}")
                }
            )
        }
    }

    fun deleteReview(id: String) {
        viewModelScope.launch {
            val result = nextApiService.deleteReview(id)
            result.fold(
                onSuccess = {
                    if (_uiState.value.selectedReview?.id == id) {
                        _uiState.value = _uiState.value.copy(selectedReview = null)
                    }
                    refreshAll()
                },
                onFailure = { e ->
                    Timber.e(e, "[Routine] 删除例行失败")
                    _uiState.value = _uiState.value.copy(error = "删除失败：${e.message}")
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun dueStatusOrder(status: String?): Int = when (status) {
        "overdue" -> 0
        "due_today" -> 1
        "due_soon" -> 2
        "upcoming" -> 3
        "completed" -> 4
        else -> 5
    }
}
