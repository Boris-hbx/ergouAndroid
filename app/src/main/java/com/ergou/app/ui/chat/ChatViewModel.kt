package com.ergou.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ergou.app.data.local.entity.MessageEntity
import com.ergou.app.data.local.entity.SessionEntity
import com.ergou.app.data.remote.ErgouPrompt
import com.ergou.app.data.remote.api.LLMService
import com.ergou.app.data.remote.dto.ChatRequest
import com.ergou.app.data.remote.dto.Message
import com.ergou.app.data.repository.ChatRepository
import com.ergou.app.data.repository.ChatRepositoryImpl
import com.ergou.app.data.repository.MemoryExtractor
import com.ergou.app.data.repository.MemoryRepository
import com.ergou.app.data.repository.ShortcutBarRepository
import com.ergou.app.data.repository.ShortcutItem
import com.ergou.app.data.repository.SoulRepository
import com.ergou.app.data.repository.SuggestionEngine
import com.ergou.app.data.tool.ToolExecutor
import com.ergou.app.util.ApiKeyProvider
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

data class ChatUiState(
    val sessions: List<SessionEntity> = emptyList(),
    val currentSessionId: Long? = null,
    val messages: List<MessageEntity> = emptyList(),
    val streamingContent: String = "",
    val isApiKeySet: Boolean? = null,  // null=加载中, false=未设置, true=已设置
    val isSending: Boolean = false,
    val error: String? = null,
    val suggestions: List<String> = emptyList(),
    val welcomeSuggestions: List<String> = emptyList()
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val memoryRepository: MemoryRepository,
    private val toolExecutor: ToolExecutor,
    private val apiKeyProvider: ApiKeyProvider,
    private val memoryExtractor: MemoryExtractor,
    private val soulRepository: SoulRepository,
    private val shortcutBarRepository: ShortcutBarRepository,
    private val llmService: LLMService,
    private val suggestionEngine: SuggestionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    val shortcuts: StateFlow<List<ShortcutItem>> = shortcutBarRepository.getShortcuts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isEditingShortcuts = MutableStateFlow(false)
    val isEditingShortcuts: StateFlow<Boolean> = _isEditingShortcuts

    private var pinnedSnapshot: List<String> = emptyList()

    fun enterShortcutEditMode() {
        viewModelScope.launch {
            pinnedSnapshot = shortcutBarRepository.getPinnedRoutes()
            _isEditingShortcuts.value = true
        }
    }

    fun exitShortcutEditMode() {
        viewModelScope.launch {
            val current = shortcutBarRepository.getPinnedRoutes()
            if (current != pinnedSnapshot) {
                shortcutBarRepository.setPinnedRoutes(current)
            }
            _isEditingShortcuts.value = false
        }
    }

    fun togglePin(route: String) {
        viewModelScope.launch {
            val current = shortcutBarRepository.getPinnedRoutes()
            val newPinned = if (route in current) {
                current - route
            } else {
                if (current.size >= MAX_PINNED) return@launch
                current + route
            }
            shortcutBarRepository.setPinnedRoutes(newPinned)
        }
    }

    fun recordShortcutUsage(route: String) {
        viewModelScope.launch {
            shortcutBarRepository.recordUsage(route)
        }
    }

    fun refreshShortcutBadges() {
        viewModelScope.launch {
            shortcutBarRepository.refreshTodoBadge()
        }
    }

    private var messageCollectionJob: Job? = null
    private var sendJob: Job? = null

    // 记忆指令正则
    private val memoryPattern = Regex("""\[SAVE_MEMORY:(\w+):(.+?)]""")
    private val personPattern = Regex("""\[SAVE_PERSON:(.+?):(.+?):(.+?)]""")

    init {
        viewModelScope.launch {
            val key = apiKeyProvider.activeApiKey()
            _uiState.value = _uiState.value.copy(isApiKeySet = key.isNotBlank())
        }

        viewModelScope.launch {
            chatRepository.getAllSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(sessions = sessions)
            }
        }

        viewModelScope.launch {
            try {
                val suggestions = suggestionEngine.generate()
                _uiState.value = _uiState.value.copy(welcomeSuggestions = suggestions)
            } catch (e: Exception) {
                Timber.d("[Chat] 欢迎建议生成失败: %s", e.message)
            }
        }
    }

    fun onSendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _uiState.value.isSending) return
        _uiState.value = _uiState.value.copy(suggestions = emptyList())
        sendMessageInternal(trimmed)
    }

    fun onSaveApiKey(key: String) {
        viewModelScope.launch {
            apiKeyProvider.saveApiKey(key)
            // 重新检查当前提供商的 key 是否已设置
            val activeKey = apiKeyProvider.activeApiKey()
            _uiState.value = _uiState.value.copy(isApiKeySet = activeKey.isNotBlank())
        }
    }

    fun onNewSession() {
        viewModelScope.launch {
            val sessionId = chatRepository.createSession()
            switchToSession(sessionId)
        }
    }

    fun onSwitchSession(sessionId: Long) {
        viewModelScope.launch {
            switchToSession(sessionId)
        }
    }

    fun onDeleteSession(sessionId: Long) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            if (_uiState.value.currentSessionId == sessionId) {
                messageCollectionJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    currentSessionId = null,
                    messages = emptyList()
                )
            }
        }
    }

    private fun switchToSession(sessionId: Long) {
        messageCollectionJob?.cancel()
        _uiState.value = _uiState.value.copy(currentSessionId = sessionId)
        messageCollectionJob = viewModelScope.launch {
            chatRepository.getMessages(sessionId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    private fun sendMessageInternal(text: String, skipSaveUser: Boolean = false) {
        sendJob = viewModelScope.launch {
            val sessionId = _uiState.value.currentSessionId ?: run {
                val id = chatRepository.createSession()
                switchToSession(id)
                id
            }

            _uiState.value = _uiState.value.copy(
                isSending = true,
                error = null,
                streamingContent = ""
            )

            try {
                // 保存用户消息（重新生成时跳过，用户消息已存在）
                if (!skipSaveUser) {
                    chatRepository.saveMessage(sessionId, "user", text)

                    // 第一条消息时自动设置标题
                    if (_uiState.value.messages.size <= 1) {
                        (chatRepository as? ChatRepositoryImpl)?.generateTitle(sessionId, text)
                    }
                }

                // 构建动态上下文
                val soulState = soulRepository.getSoulState()
                val peopleContext = memoryRepository.buildPeopleContext()
                val memoryContext = memoryRepository.buildMemoryContext()

                // 构建消息列表（system + 最近历史，历史已包含刚保存的用户消息）
                val systemPrompt = ErgouPrompt.buildSystemPrompt(
                    soulState = soulState,
                    peopleContext = peopleContext,
                    memoryContext = memoryContext
                )

                val history = chatRepository.getMessagesOnce(sessionId)
                    .takeLast(MAX_HISTORY_MESSAGES)
                    .map { Message(role = it.role, content = it.content) }

                val messages = buildList {
                    add(Message(role = "system", content = systemPrompt))
                    addAll(history)
                }

                // 通过 ToolExecutor 发送（支持工具调用循环）
                val responseBuilder = StringBuilder()

                toolExecutor.chatWithTools(messages).collect { chunk ->
                    responseBuilder.append(chunk)
                    _uiState.value = _uiState.value.copy(
                        streamingContent = responseBuilder.toString()
                    )
                }

                val fullResponse = responseBuilder.toString()

                if (fullResponse.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        streamingContent = "",
                        error = "二狗没回复，检查一下网络或 API Key"
                    )
                    return@launch
                }

                // 解析并执行记忆指令（文本标记作为备用机制）
                processMemoryCommands(fullResponse, sessionId)

                // 保存AI回复（去掉指令标记）
                val cleanResponse = cleanMemoryCommands(fullResponse)
                chatRepository.saveMessage(sessionId, "assistant", cleanResponse)

                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    streamingContent = ""
                )

                // 后台生成建议追问（不阻塞UI）
                viewModelScope.launch {
                    try {
                        val suggestions = generateSuggestions(text, cleanResponse)
                        _uiState.value = _uiState.value.copy(suggestions = suggestions)
                    } catch (e: Exception) {
                        Timber.d("[Chat] 建议追问生成失败: %s", e.message)
                    }
                }

                // 后台自动提取记忆（不阻塞UI）
                viewModelScope.launch {
                    memoryExtractor.extractFromConversation(text, cleanResponse, sessionId)
                }

                // 人格进化和互动计数已由后端在对话后自动处理

                Timber.d("[Chat] 收到回复 len=%d", cleanResponse.length)
            } catch (e: CancellationException) {
                // 用户主动取消（停止按钮），不显示错误
                Timber.d("[Chat] 流式生成被用户取消")
            } catch (e: Exception) {
                Timber.e(e, "[Chat] 发送消息失败")
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    streamingContent = "",
                    error = "发送失败: ${e.message}"
                )
            }
        }
    }

    fun cancelStreaming() {
        sendJob?.cancel()
        val partial = _uiState.value.streamingContent
        viewModelScope.launch {
            if (partial.isNotBlank()) {
                val sessionId = _uiState.value.currentSessionId ?: return@launch
                val cleanPartial = cleanMemoryCommands(partial)
                chatRepository.saveMessage(sessionId, "assistant", cleanPartial)
            }
            _uiState.value = _uiState.value.copy(
                isSending = false,
                streamingContent = ""
            )
        }
    }

    fun regenerateLastMessage() {
        if (_uiState.value.isSending) return
        viewModelScope.launch {
            val sessionId = _uiState.value.currentSessionId ?: return@launch
            val messages = _uiState.value.messages
            val lastAssistant = messages.lastOrNull { it.role == "assistant" } ?: return@launch
            chatRepository.deleteMessage(lastAssistant.id)
            val lastUserMsg = messages.lastOrNull { it.role == "user" }?.content ?: return@launch
            sendMessageInternal(lastUserMsg, skipSaveUser = true)
        }
    }

    companion object {
        private const val MAX_HISTORY_MESSAGES = 20
        const val MAX_PINNED = 4
    }

    private suspend fun generateSuggestions(userMessage: String, aiReply: String): List<String> {
        val prompt = """基于以下对话，生成2-3个用户可能想继续追问的短问题。
每个问题一行，不要编号，不要引号，直接输出问题文本。

用户: $userMessage
二狗: ${aiReply.take(500)}"""

        val messages = listOf(
            Message(role = "system", content = "你是问题生成器。只输出问题，每行一个，不超过3个。"),
            Message(role = "user", content = prompt)
        )
        val request = ChatRequest(messages = messages, maxTokens = 200, temperature = 0.8)
        val response = llmService.chat(request)
        val content = response.choices.firstOrNull()?.message?.content ?: return emptyList()
        return content.lines().filter { it.isNotBlank() }.take(3)
    }

    /**
     * 解析AI回复中的记忆指令并执行
     */
    private suspend fun processMemoryCommands(response: String, sessionId: Long) {
        // 解析记忆保存指令
        memoryPattern.findAll(response).forEach { match ->
            val category = match.groupValues[1]
            val content = match.groupValues[2]
            if (category in listOf("fact", "habit", "personality", "intent") && content.isNotBlank()) {
                memoryRepository.saveMemory(category, content, importance = 3, sessionId = sessionId)
                Timber.d("二狗记住了: [$category] $content")
            }
        }

        // 解析人物保存指令
        personPattern.findAll(response).forEach { match ->
            val name = match.groupValues[1]
            val relationship = match.groupValues[2]
            val notes = match.groupValues[3]
            if (name.isNotBlank()) {
                memoryRepository.savePerson(name, relationship, notes = notes)
                Timber.d("二狗认识了: $name ($relationship)")
            }
        }
    }

    /**
     * 清除回复中的指令标记，返回干净文本
     */
    private fun cleanMemoryCommands(response: String): String {
        return response
            .replace(memoryPattern, "")
            .replace(personPattern, "")
            .trim()
    }

    fun onMessageFeedback(messageId: Long, feedback: Int?) {
        viewModelScope.launch {
            chatRepository.updateMessageFeedback(messageId, feedback)
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
