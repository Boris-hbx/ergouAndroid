package com.ergou.app.data.repository

// NOTE: 需要在 app/build.gradle.kts 中添加以下测试依赖：
// testImplementation("io.mockk:mockk:1.13.13")
// testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

import com.ergou.app.data.local.dao.MessageDao
import com.ergou.app.data.local.dao.SessionDao
import com.ergou.app.data.local.entity.MessageEntity
import com.ergou.app.data.local.entity.SessionEntity
import com.ergou.app.data.remote.api.LLMService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ChatRepositoryImplTest {

    private lateinit var sessionDao: SessionDao
    private lateinit var messageDao: MessageDao
    private lateinit var llmService: LLMService
    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setUp() {
        sessionDao = mockk(relaxed = true)
        messageDao = mockk(relaxed = true)
        llmService = mockk(relaxed = true)
        repository = ChatRepositoryImpl(sessionDao, messageDao, llmService)
    }

    @Test
    fun saveMessage_savesUserMessageAndCallsDao() = runTest {
        // Arrange
        val messageSlot = slot<MessageEntity>()
        coEvery { messageDao.insert(capture(messageSlot)) } returns 42L

        // Act
        val id = repository.saveMessage(1L, "user", "hello")

        // Assert
        assertEquals(42L, id)
        assertEquals(1L, messageSlot.captured.sessionId)
        assertEquals("user", messageSlot.captured.role)
        assertEquals("hello", messageSlot.captured.content)
        coVerify { sessionDao.incrementMessageCount(1L, any()) }
    }

    @Test
    fun deleteMessage_callsDao() = runTest {
        // Act
        repository.deleteMessage(99L)

        // Assert
        coVerify { messageDao.deleteById(99L) }
    }

    @Test
    fun updateMessageFeedback_callsDao() = runTest {
        // Act
        repository.updateMessageFeedback(42L, 1)

        // Assert
        coVerify { messageDao.updateFeedback(42L, 1) }
    }

    @Test
    fun createSession_insertsAndReturnsId() = runTest {
        // Arrange
        val sessionSlot = slot<SessionEntity>()
        coEvery { sessionDao.insert(capture(sessionSlot)) } returns 5L

        // Act
        val id = repository.createSession("测试对话")

        // Assert
        assertEquals(5L, id)
        assertEquals("测试对话", sessionSlot.captured.title)
    }

    @Test
    fun deleteSession_callsDao() = runTest {
        // Act
        repository.deleteSession(10L)

        // Assert
        coVerify { sessionDao.deleteById(10L) }
    }

    @Test
    fun updateSessionTitle_callsDao() = runTest {
        // Act
        repository.updateSessionTitle(3L, "新标题")

        // Assert
        coVerify { sessionDao.updateTitle(3L, "新标题", any()) }
    }

    @Test
    fun generateTitle_shortMessage_usesAsIs() = runTest {
        // Act
        repository.generateTitle(1L, "短标题")

        // Assert
        coVerify { sessionDao.updateTitle(1L, "短标题", any()) }
    }

    @Test
    fun generateTitle_longMessage_truncates() = runTest {
        // Act
        val longMsg = "这是一个非常非常非常非常长的消息标题用来测试截断"
        repository.generateTitle(1L, longMsg)

        // Assert
        coVerify { sessionDao.updateTitle(1L, match { it.endsWith("...") && it.length == 21 }, any()) }
    }
}
