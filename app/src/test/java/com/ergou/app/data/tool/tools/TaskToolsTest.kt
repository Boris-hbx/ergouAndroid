package com.ergou.app.data.tool.tools

// NOTE: 需要在 app/build.gradle.kts 中添加以下测试依赖：
// testImplementation("io.mockk:mockk:1.13.13")
// testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTodo
import com.ergou.app.util.NextAuthProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TaskToolsTest {

    private lateinit var nextApiService: NextApiService
    private lateinit var authProvider: NextAuthProvider

    @Before
    fun setUp() {
        nextApiService = mockk(relaxed = true)
        authProvider = mockk(relaxed = true)
        every { authProvider.isLoggedIn } returns flowOf(true)
    }

    // ── AddTaskTool ──

    @Test
    fun addTask_execute_validArgs_returnsSuccess() = runTest {
        // Arrange
        val todo = NextTodo(id = "1", text = "买菜", tab = "today", quadrant = "not-urgent-important")
        coEvery { nextApiService.createTodo(any()) } returns Result.success(todo)

        val tool = AddTaskTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(mapOf("text" to "买菜"))

        // Assert
        assertTrue(result.contains("买菜"))
        assertTrue(result.contains("已添加"))
    }

    @Test
    fun addTask_execute_notLoggedIn_returnsError() = runTest {
        // Arrange
        every { authProvider.isLoggedIn } returns flowOf(false)
        val tool = AddTaskTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(mapOf("text" to "买菜"))

        // Assert
        assertTrue(result.contains("登录"))
    }

    @Test
    fun addTask_execute_missingRequired_returnsError() = runTest {
        // Arrange
        val tool = AddTaskTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(emptyMap())

        // Assert
        assertTrue(result.contains("缺少"))
    }

    // ── ListTasksTool ──

    @Test
    fun listTasks_execute_validArgs_returnsSuccess() = runTest {
        // Arrange
        val todos = listOf(
            NextTodo(id = "1", text = "买菜", tab = "today", quadrant = "not-urgent-important"),
            NextTodo(id = "2", text = "开会", tab = "today", quadrant = "urgent-important")
        )
        coEvery { nextApiService.getTodos(any()) } returns Result.success(todos)

        val tool = ListTasksTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(mapOf("tab" to "today"))

        // Assert
        assertTrue(result.contains("买菜"))
        assertTrue(result.contains("开会"))
        assertTrue(result.contains("2项"))
    }

    @Test
    fun listTasks_execute_notLoggedIn_returnsError() = runTest {
        // Arrange
        every { authProvider.isLoggedIn } returns flowOf(false)
        val tool = ListTasksTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(emptyMap())

        // Assert
        assertTrue(result.contains("登录"))
    }

    @Test
    fun listTasks_execute_emptyList_returnsNoTasks() = runTest {
        // Arrange
        coEvery { nextApiService.getTodos(any()) } returns Result.success(emptyList())
        val tool = ListTasksTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(emptyMap())

        // Assert
        assertTrue(result.contains("没有"))
    }

    @Test
    fun listTasks_execute_apiError_returnsError() = runTest {
        // Arrange
        coEvery { nextApiService.getTodos(any()) } returns Result.failure(Exception("网络超时"))
        val tool = ListTasksTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(emptyMap())

        // Assert
        assertTrue(result.contains("失败"))
        assertTrue(result.contains("网络超时"))
    }

    // ── CompleteTaskTool ──

    @Test
    fun completeTask_execute_validArgs_returnsSuccess() = runTest {
        // Arrange
        val completedTodo = NextTodo(id = "1", text = "买菜", completed = true)
        coEvery { nextApiService.updateTodo("1", any()) } returns Result.success(completedTodo)

        val tool = CompleteTaskTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(mapOf("task_id" to "1"))

        // Assert
        assertTrue(result.contains("买菜"))
        assertTrue(result.contains("完成"))
    }

    @Test
    fun completeTask_execute_notLoggedIn_returnsError() = runTest {
        // Arrange
        every { authProvider.isLoggedIn } returns flowOf(false)
        val tool = CompleteTaskTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(mapOf("task_id" to "1"))

        // Assert
        assertTrue(result.contains("登录"))
    }

    @Test
    fun completeTask_execute_missingRequired_returnsError() = runTest {
        // Arrange
        val tool = CompleteTaskTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(emptyMap())

        // Assert
        assertTrue(result.contains("缺少"))
    }

    @Test
    fun completeTask_execute_apiError_returnsError() = runTest {
        // Arrange
        coEvery { nextApiService.updateTodo(any(), any()) } returns Result.failure(Exception("not found"))
        val tool = CompleteTaskTool(nextApiService, authProvider)

        // Act
        val result = tool.execute(mapOf("task_id" to "999"))

        // Assert
        assertTrue(result.contains("失败"))
    }
}
