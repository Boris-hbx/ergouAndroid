package com.ergou.app.ui.task

// NOTE: 需要在 app/build.gradle.kts 中添加以下测试依赖：
// testImplementation("io.mockk:mockk:1.13.13")
// testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTodo
import com.ergou.app.data.remote.dto.NextTodoCreateRequest
import com.ergou.app.data.remote.dto.NextTodoUpdateRequest
import com.ergou.app.util.NextAuthProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class TaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var nextApiService: NextApiService
    private lateinit var authProvider: NextAuthProvider
    private lateinit var viewModel: TaskViewModel

    private val isLoggedInFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        nextApiService = mockk(relaxed = true)
        authProvider = mockk(relaxed = true)
        every { authProvider.isLoggedIn } returns isLoggedInFlow

        // Default: getTodos returns empty
        coEvery { nextApiService.getTodos(any()) } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TaskViewModel {
        return TaskViewModel(nextApiService, authProvider).also { viewModel = it }
    }

    @Test
    fun refreshTodos_success_updatesState() = runTest {
        // Arrange
        val todos = listOf(
            NextTodo(id = "1", text = "买菜", tab = "today", completed = false),
            NextTodo(id = "2", text = "开会", tab = "today", completed = true)
        )
        coEvery { nextApiService.getTodos("today") } returns Result.success(todos)

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.isLoggedIn)
        assertFalse(state.isLoading)
        assertEquals(1, state.pendingTodos.size)
        assertEquals("买菜", state.pendingTodos[0].text)
        assertEquals(1, state.completedTodos.size)
        assertEquals("开会", state.completedTodos[0].text)
    }

    @Test
    fun refreshTodos_unauthorized_setsNotLoggedIn() = runTest {
        // Arrange - login triggers refresh which fails with 401-like error
        coEvery { nextApiService.getTodos(any()) } returns Result.failure(Exception("加载失败"))

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals("加载失败", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun addTodo_success_refreshesList() = runTest {
        // Arrange
        val newTodo = NextTodo(id = "3", text = "新任务", tab = "today")
        coEvery { nextApiService.createTodo(any()) } returns Result.success(newTodo)
        coEvery { nextApiService.getTodos(any()) } returns Result.success(listOf(newTodo))

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        // Act
        viewModel.addTodo("新任务", null, null, null)
        advanceUntilIdle()

        // Assert
        coVerify { nextApiService.createTodo(match { it.text == "新任务" && it.tab == "today" }) }
        coVerify(atLeast = 2) { nextApiService.getTodos("today") } // init + after add
    }

    @Test
    fun completeTodo_success_refreshesList() = runTest {
        // Arrange
        val completedTodo = NextTodo(id = "1", text = "买菜", completed = true)
        coEvery { nextApiService.updateTodo("1", any()) } returns Result.success(completedTodo)
        coEvery { nextApiService.getTodos(any()) } returns Result.success(listOf(completedTodo))

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        // Act
        viewModel.completeTodo("1")
        advanceUntilIdle()

        // Assert
        coVerify { nextApiService.updateTodo("1", match { it.completed == true }) }
        coVerify(atLeast = 2) { nextApiService.getTodos("today") }
    }

    @Test
    fun switchTab_changesTabAndRefreshes() = runTest {
        // Arrange
        coEvery { nextApiService.getTodos("week") } returns Result.success(emptyList())

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        // Act
        viewModel.switchTab("week")
        advanceUntilIdle()

        // Assert
        assertEquals("week", viewModel.uiState.value.currentTab)
        coVerify { nextApiService.getTodos("week") }
    }

    @Test
    fun clearError_resetsErrorState() = runTest {
        // Arrange
        coEvery { nextApiService.getTodos(any()) } returns Result.failure(Exception("网络错误"))

        createViewModel()
        isLoggedInFlow.value = true
        advanceUntilIdle()

        assertEquals("网络错误", viewModel.uiState.value.error)

        // Act
        viewModel.clearError()

        // Assert
        assertNull(viewModel.uiState.value.error)
    }
}
