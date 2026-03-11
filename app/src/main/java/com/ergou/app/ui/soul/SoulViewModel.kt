package com.ergou.app.ui.soul

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ergou.app.data.local.entity.SoulEvolutionLogEntity
import com.ergou.app.data.local.entity.SoulStateEntity
import com.ergou.app.data.repository.SoulRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class SoulUiState(
    val soulState: SoulStateEntity = SoulStateEntity(),
    val recentLogs: List<SoulEvolutionLogEntity> = emptyList(),
    val selectedTab: Int = 0,
    val showResetDialog: Boolean = false,
    val isSyncing: Boolean = false
)

class SoulViewModel(
    private val soulRepository: SoulRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoulUiState())
    val uiState: StateFlow<SoulUiState> = _uiState

    init {
        viewModelScope.launch {
            soulRepository.observeSoulState().collect { state ->
                _uiState.value = _uiState.value.copy(soulState = state)
            }
        }

        viewModelScope.launch {
            soulRepository.observeRecentLogs(100).collect { logs ->
                _uiState.value = _uiState.value.copy(recentLogs = logs)
            }
        }

        // 进入页面时从后端刷新
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            try {
                soulRepository.syncFromBackend()
            } catch (e: Exception) {
                Timber.w(e, "[Soul] 刷新灵魂状态失败")
            } finally {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onShowResetDialog() {
        _uiState.value = _uiState.value.copy(showResetDialog = true)
    }

    fun onDismissResetDialog() {
        _uiState.value = _uiState.value.copy(showResetDialog = false)
    }

    fun onResetAll() {
        viewModelScope.launch {
            soulRepository.resetAllParameters()
            _uiState.value = _uiState.value.copy(showResetDialog = false)
        }
    }
}
