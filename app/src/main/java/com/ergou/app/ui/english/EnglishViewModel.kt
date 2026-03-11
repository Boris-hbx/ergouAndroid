package com.ergou.app.ui.english

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextEnglishCreateRequest
import com.ergou.app.data.remote.dto.NextEnglishScenario
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class EnglishUiState(
    val scenarios: List<NextEnglishScenario> = emptyList(),
    val archivedScenarios: List<NextEnglishScenario> = emptyList(),
    val selectedCategory: String? = null,
    val showArchived: Boolean = false,
    val selectedScenario: NextEnglishScenario? = null,
    val generatingIds: Set<String> = emptySet(),
    val generateFailedIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false
)

class EnglishViewModel(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnglishUiState())
    val uiState: StateFlow<EnglishUiState> = _uiState

    init {
        viewModelScope.launch {
            authProvider.isLoggedIn.collect { loggedIn ->
                _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
                if (loggedIn) {
                    refreshScenarios()
                } else {
                    _uiState.value = _uiState.value.copy(scenarios = emptyList())
                }
            }
        }
    }

    fun refreshScenarios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val category = _uiState.value.selectedCategory

            val activeResult = nextApiService.getScenarios(archived = false, category = category)
            val archivedResult = nextApiService.getScenarios(archived = true, category = category)

            activeResult.fold(
                onSuccess = { scenarios ->
                    val archived = archivedResult.getOrDefault(emptyList())
                    _uiState.value = _uiState.value.copy(
                        scenarios = scenarios,
                        archivedScenarios = archived,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "[English] 刷新场景失败")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "加载失败")
                }
            )
        }
    }

    fun switchTab(showArchived: Boolean) {
        _uiState.value = _uiState.value.copy(showArchived = showArchived)
    }

    fun selectScenario(scenario: NextEnglishScenario?) {
        _uiState.value = _uiState.value.copy(selectedScenario = scenario)
    }

    fun filterByCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        refreshScenarios()
    }

    fun addScenario(title: String, category: String, description: String?) {
        viewModelScope.launch {
            val result = nextApiService.createScenario(
                NextEnglishCreateRequest(title = title, category = category, description = description)
            )
            result.fold(
                onSuccess = { refreshScenarios() },
                onFailure = { e ->
                    Timber.e(e, "[English] 添加场景失败")
                    _uiState.value = _uiState.value.copy(error = "添加失败：${e.message}")
                }
            )
        }
    }

    fun generateContent(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                generatingIds = _uiState.value.generatingIds + id,
                generateFailedIds = _uiState.value.generateFailedIds - id
            )
            val result = nextApiService.generateScenario(id)
            result.fold(
                onSuccess = { updated ->
                    _uiState.value = _uiState.value.copy(
                        generatingIds = _uiState.value.generatingIds - id,
                        selectedScenario = if (_uiState.value.selectedScenario?.id == id) updated else _uiState.value.selectedScenario
                    )
                    refreshScenarios()
                },
                onFailure = { e ->
                    Timber.e(e, "[English] AI生成失败")
                    _uiState.value = _uiState.value.copy(
                        generatingIds = _uiState.value.generatingIds - id,
                        generateFailedIds = _uiState.value.generateFailedIds + id,
                        error = "生成失败：${e.message}"
                    )
                }
            )
        }
    }

    fun archiveScenario(id: String) {
        viewModelScope.launch {
            val result = nextApiService.archiveScenario(id)
            result.fold(
                onSuccess = { refreshScenarios() },
                onFailure = { e ->
                    Timber.e(e, "[English] 归档失败")
                    _uiState.value = _uiState.value.copy(error = "归档失败：${e.message}")
                }
            )
        }
    }

    fun deleteScenario(id: String) {
        viewModelScope.launch {
            val result = nextApiService.deleteScenario(id)
            result.fold(
                onSuccess = { refreshScenarios() },
                onFailure = { e ->
                    Timber.e(e, "[English] 删除失败")
                    _uiState.value = _uiState.value.copy(error = "删除失败：${e.message}")
                }
            )
        }
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

    fun filteredScenarios(list: List<NextEnglishScenario>): List<NextEnglishScenario> {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return list
        val q = query.lowercase()
        return list.filter { s ->
            s.title.lowercase().contains(q) ||
                (s.titleEn?.lowercase()?.contains(q) == true) ||
                (s.description?.lowercase()?.contains(q) == true) ||
                s.category.lowercase().contains(q)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
