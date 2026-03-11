package com.ergou.app.ui.chat

// NOTE: 需要在 app/build.gradle.kts 中添加以下测试依赖：
// testImplementation("io.mockk:mockk:1.13.13")
// testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

import com.ergou.app.data.local.entity.SoulStateEntity
import com.ergou.app.data.local.entity.MessageEntity
import com.ergou.app.data.local.entity.SessionEntity
import com.ergou.app.data.remote.api.LLMService
import com.ergou.app.data.repository.ChatRepository
import com.ergou.app.data.repository.MemoryExtractor
import com.ergou.app.data.repository.MemoryRepository
import com.ergou.app.data.repository.ShortcutBarRepository
import com.ergou.app.data.repository.SoulRepository
import com.ergou.app.data.repository.SuggestionEngine
import com.ergou.app.data.tool.ToolExecutor
import com.ergou.app.util.ApiKeyProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var chatRepository: ChatRepository
    private lateinit var memoryRepository: MemoryRepository
    private lateinit var toolExecutor: ToolExecutor
    private lateinit var apiKeyProvider: ApiKeyProvider
    private lateinit var memoryExtractor: MemoryExtractor
    private lateinit var soulRepository: SoulRepository
    private lateinit var shortcutBarRepository: ShortcutBarRepository
    private lateinit var llmService: LLMService
    private lateinit var suggestionEngine: SuggestionEngine

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        chatRepository = mockk(relaxed = true)
        memoryRepository = mockk(relaxed = true)
        toolExecutor = mockk(relaxed = true)
        apiKeyProvider = mockk(relaxed = true)
        memoryExtractor = mockk(relaxed = true)
        soulRepository = mockk(relaxed = true)
        shortcutBarRepository = mockk(relaxed = true)
        llmService = mockk(relaxed = true)
        suggestionEngine = mockk(relaxed = true)

        every { apiKeyProvider.apiKey } returns flowOf("test-key")
        every { chatRepository.getAllSessions() } returns flowOf(emptyList())
        every { shortcutBarRepository.getShortcuts() } returns flowOf(emptyList())
        coEvery { suggestionEngine.generate() } returns listOf("建议1", "建议2")

        viewModel = ChatViewModel(
            chatRepository = chatRepository,
            memoryRepository = memoryRepository,
            toolExecutor = toolExecutor,
            apiKeyProvider = apiKeyProvider,
            memoryExtractor = memoryExtractor,
            soulRepository = soulRepository,
            shortcutBarRepository = shortcutBarRepository,
            llmService = llmService,
            suggestionEngine = suggestionEngine
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onSendMessage_validText_sendsAndUpdatesState() = runTest {
        // Arrange
        coEvery { chatRepository.createSession(any()) } returns 1L
        every { chatRepository.getMessages(1L) } returns flowOf(
            listOf(MessageEntity(id = 1, sessionId = 1, role = "user", content = "hello"))
        )
        coEvery { chatRepository.getMessagesOnce(1L) } returns listOf(
            MessageEntity(id = 1, sessionId = 1, role = "user", content = "hello")
        )
        coEvery { soulRepository.getSoulState() } returns SoulStateEntity()
        coEvery { memoryRepository.buildPeopleContext() } returns ""
        coEvery { memoryRepository.buildMemoryContext() } returns ""
        every { toolExecutor.chatWithTools(any()) } returns flowOf("你好！")
        coEvery { chatRepository.saveMessage(any(), any(), any()) } returns 1L

        advanceUntilIdle()

        // Act
        viewModel.onSendMessage("hello")
        advanceUntilIdle()

        // Assert
        coVerify { chatRepository.saveMessage(any(), "user", "hello") }
        coVerify { chatRepository.saveMessage(any(), "assistant", "你好！") }
        assertFalse(viewModel.uiState.value.isSending)
    }

    @Test
    fun onSendMessage_emptyText_doesNothing() = runTest {
        advanceUntilIdle()

        // Act
        viewModel.onSendMessage("")
        viewModel.onSendMessage("   ")
        advanceUntilIdle()

        // Assert - no message should be saved
        coVerify(exactly = 0) { chatRepository.saveMessage(any(), "user", any()) }
    }

    @Test
    fun cancelStreaming_whileSending_stopsAndSavesPartial() = runTest {
        // Arrange: set up state as if streaming is in progress
        coEvery { chatRepository.createSession(any()) } returns 1L
        every { chatRepository.getMessages(1L) } returns flowOf(emptyList())
        coEvery { chatRepository.saveMessage(any(), any(), any()) } returns 1L

        advanceUntilIdle()

        // Simulate streaming state by directly setting it (since we can't easily control timing)
        val field = ChatViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<ChatUiState>
        stateFlow.value = stateFlow.value.copy(
            isSending = true,
            streamingContent = "部分内容",
            currentSessionId = 1L
        )

        // Act
        viewModel.cancelStreaming()
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.uiState.value.isSending)
        assertEquals("", viewModel.uiState.value.streamingContent)
        coVerify { chatRepository.saveMessage(1L, "assistant", "部分内容") }
    }

    @Test
    fun regenerateLastMessage_deletesLastAndResends() = runTest {
        // Arrange
        val userMsg = MessageEntity(id = 1, sessionId = 1, role = "user", content = "hi")
        val assistantMsg = MessageEntity(id = 2, sessionId = 1, role = "assistant", content = "hello")

        coEvery { chatRepository.createSession(any()) } returns 1L
        every { chatRepository.getMessages(1L) } returns flowOf(listOf(userMsg, assistantMsg))
        coEvery { chatRepository.getMessagesOnce(1L) } returns listOf(userMsg)
        coEvery { chatRepository.saveMessage(any(), any(), any()) } returns 3L
        coEvery { soulRepository.getSoulState() } returns SoulStateEntity()
        coEvery { memoryRepository.buildPeopleContext() } returns ""
        coEvery { memoryRepository.buildMemoryContext() } returns ""
        every { toolExecutor.chatWithTools(any()) } returns flowOf("new reply")

        advanceUntilIdle()

        // Switch to session to populate messages
        val stateField = ChatViewModel::class.java.getDeclaredField("_uiState")
        stateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = stateField.get(viewModel) as MutableStateFlow<ChatUiState>
        stateFlow.value = stateFlow.value.copy(
            currentSessionId = 1L,
            messages = listOf(userMsg, assistantMsg)
        )

        // Act
        viewModel.regenerateLastMessage()
        advanceUntilIdle()

        // Assert
        coVerify { chatRepository.deleteMessage(2L) }
        // Should resend the user's last message (skipSaveUser=true so no new user message saved)
        coVerify { chatRepository.saveMessage(any(), "assistant", "new reply") }
    }

    @Test
    fun onMessageFeedback_updatesCorrectly() = runTest {
        advanceUntilIdle()

        // Act
        viewModel.onMessageFeedback(42L, 1)
        advanceUntilIdle()

        // Assert
        coVerify { chatRepository.updateMessageFeedback(42L, 1) }
    }

    @Test
    fun dismissError_clearsErrorState() = runTest {
        advanceUntilIdle()

        // Arrange - set error state
        val stateField = ChatViewModel::class.java.getDeclaredField("_uiState")
        stateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = stateField.get(viewModel) as MutableStateFlow<ChatUiState>
        stateFlow.value = stateFlow.value.copy(error = "some error")

        assertEquals("some error", viewModel.uiState.value.error)

        // Act
        viewModel.dismissError()

        // Assert
        assertNull(viewModel.uiState.value.error)
    }
}
