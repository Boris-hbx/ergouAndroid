package com.ergou.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.repository.MemoryRepository
import com.ergou.app.data.repository.SoulRepository
import com.ergou.app.util.ApiKeyProvider
import com.ergou.app.util.NextAuthProvider
import com.ergou.app.util.ModelProvider
import com.ergou.app.util.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class SettingsUiState(
    val apiKeyMasked: String = "",
    val hasApiKey: Boolean = false,
    val claudeApiKeyMasked: String = "",
    val hasClaudeApiKey: Boolean = false,
    val modelProvider: ModelProvider = ModelProvider.DEEPSEEK,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val memoryCount: Int = 0,
    val peopleCount: Int = 0,
    val nextLoggedIn: Boolean = false,
    val nextUsername: String = "",
    val nextLoginLoading: Boolean = false,
    val nextLoginError: String? = null,
    val soulStageLabel: String = "初识",
    val nickname: String = "",
    val avatarResName: String = ""
)

class SettingsViewModel(
    private val apiKeyProvider: ApiKeyProvider,
    private val memoryRepository: MemoryRepository,
    private val nextAuthProvider: NextAuthProvider,
    private val nextApiService: NextApiService,
    private val soulRepository: SoulRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            apiKeyProvider.apiKey.collect { key ->
                _uiState.value = _uiState.value.copy(
                    hasApiKey = key.isNotBlank(),
                    apiKeyMasked = if (key.length > 8) "sk-***${key.takeLast(4)}" else ""
                )
            }
        }

        viewModelScope.launch {
            apiKeyProvider.claudeApiKey.collect { key ->
                _uiState.value = _uiState.value.copy(
                    hasClaudeApiKey = key.isNotBlank(),
                    claudeApiKeyMasked = if (key.length > 8) "sk-***${key.takeLast(4)}" else ""
                )
            }
        }

        viewModelScope.launch {
            apiKeyProvider.modelProvider.collect { provider ->
                _uiState.value = _uiState.value.copy(modelProvider = provider)
            }
        }

        viewModelScope.launch {
            apiKeyProvider.themeMode.collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }

        viewModelScope.launch {
            memoryRepository.getAllMemories().collect { memories ->
                _uiState.value = _uiState.value.copy(memoryCount = memories.size)
            }
        }

        viewModelScope.launch {
            memoryRepository.getAllPeople().collect { people ->
                _uiState.value = _uiState.value.copy(peopleCount = people.size)
            }
        }

        viewModelScope.launch {
            soulRepository.observeSoulState().collect { state ->
                val label = when (state.relationshipStage) {
                    "stranger" -> "初识"
                    "acquaintance" -> "相识"
                    "familiar" -> "熟悉"
                    "close" -> "亲近"
                    "intimate" -> "至交"
                    else -> state.relationshipStage
                }
                _uiState.value = _uiState.value.copy(
                    soulStageLabel = "$label · ${state.totalInteractions} 次对话"
                )
            }
        }

        viewModelScope.launch {
            nextAuthProvider.isLoggedIn.collect { loggedIn ->
                _uiState.value = _uiState.value.copy(nextLoggedIn = loggedIn)
            }
        }

        viewModelScope.launch {
            nextAuthProvider.username.collect { name ->
                _uiState.value = _uiState.value.copy(nextUsername = name)
            }
        }

        viewModelScope.launch {
            apiKeyProvider.nickname.collect { name ->
                _uiState.value = _uiState.value.copy(nickname = name)
            }
        }

        viewModelScope.launch {
            apiKeyProvider.avatar.collect { resName ->
                _uiState.value = _uiState.value.copy(avatarResName = resName)
            }
        }
    }

    fun onSaveApiKey(key: String) {
        viewModelScope.launch {
            apiKeyProvider.saveApiKey(key)
        }
    }

    fun onSaveClaudeApiKey(key: String) {
        viewModelScope.launch {
            apiKeyProvider.saveClaudeApiKey(key)
        }
    }

    fun onModelProviderChanged(provider: ModelProvider) {
        viewModelScope.launch {
            apiKeyProvider.saveModelProvider(provider)
        }
    }

    fun onThemeModeChanged(mode: ThemeMode) {
        viewModelScope.launch {
            apiKeyProvider.saveThemeMode(mode)
        }
    }

    fun onNextLogin(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(nextLoginLoading = true, nextLoginError = null)
            val result = nextApiService.login(username, password)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(nextLoginLoading = false)
                },
                onFailure = { e ->
                    Timber.e(e, "[Settings] Next 登录失败")
                    _uiState.value = _uiState.value.copy(
                        nextLoginLoading = false,
                        nextLoginError = e.message ?: "登录失败"
                    )
                }
            )
        }
    }

    fun onNextLogout() {
        viewModelScope.launch {
            nextApiService.logout()
        }
    }

    fun clearNextLoginError() {
        _uiState.value = _uiState.value.copy(nextLoginError = null)
    }

    fun onSaveNickname(name: String) {
        viewModelScope.launch {
            apiKeyProvider.saveNickname(name)
        }
    }

    fun onSaveAvatar(resName: String) {
        viewModelScope.launch {
            apiKeyProvider.saveAvatar(resName)
        }
    }
}
