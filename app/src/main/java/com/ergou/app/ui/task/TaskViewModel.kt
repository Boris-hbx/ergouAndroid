package com.ergou.app.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.data.remote.dto.NextTodo
import com.ergou.app.data.remote.dto.NextTodoCreateRequest
import com.ergou.app.data.remote.dto.NextTodoUpdateRequest
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class TaskSummary(
    val todayPending: Int = 0,
    val todayCompleted: Int = 0,
    val deleted: Int = 0
)

data class TaskUiState(
    val pendingTodos: List<NextTodo> = emptyList(),
    val completedTodos: List<NextTodo> = emptyList(),
    val deletedTodos: List<NextTodo> = emptyList(),
    val currentTab: String = "today",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val summary: TaskSummary = TaskSummary(),
    val allTags: List<String> = emptyList(),
    val filterTag: String? = null
)

class TaskViewModel(
    private val nextApiService: NextApiService,
    private val authProvider: NextAuthProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState

    init {
        viewModelScope.launch {
            authProvider.isLoggedIn.collect { loggedIn ->
                _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
                if (loggedIn) {
                    refreshTodos()
                } else {
                    _uiState.value = _uiState.value.copy(
                        pendingTodos = emptyList(),
                        completedTodos = emptyList()
                    )
                }
            }
        }
    }

    private fun optimisticUpdate(
        transform: (TaskUiState) -> TaskUiState,
        errorMessage: String,
        apiCall: suspend () -> Result<*>
    ) {
        val snapshot = _uiState.value
        _uiState.value = transform(snapshot)
        recomputeSummary()
        viewModelScope.launch {
            apiCall().onFailure { e ->
                Timber.e(e, "[Task] $errorMessage")
                _uiState.value = snapshot.copy(error = "$errorMessage：${e.message}")
            }
        }
    }

    private fun recomputeSummary() {
        val state = _uiState.value
        val allActive = state.pendingTodos + state.completedTodos
        val allTags = allActive.flatMap { it.tags ?: emptyList() }.distinct().sorted()
        _uiState.value = state.copy(
            summary = TaskSummary(
                todayPending = state.pendingTodos.size,
                todayCompleted = state.completedTodos.size,
                deleted = state.deletedTodos.size
            ),
            allTags = allTags
        )
    }

    fun switchTab(tab: String) {
        _uiState.value = _uiState.value.copy(
            currentTab = tab,
            filterTag = null,
            pendingTodos = emptyList(),
            completedTodos = emptyList(),
            deletedTodos = emptyList(),
            summary = TaskSummary()
        )
        refreshTodos()
    }

    fun setFilterTag(tag: String?) {
        val current = _uiState.value.filterTag
        _uiState.value = _uiState.value.copy(
            filterTag = if (tag.isNullOrBlank() || tag == current) null else tag
        )
    }

    fun refreshTodos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = nextApiService.getTodos(_uiState.value.currentTab)
            result.fold(
                onSuccess = { todos ->
                    val active = todos.filter { !it.deleted }
                    val deleted = todos.filter { it.deleted }
                    val pending = active.filter { !it.completed }
                    val completed = active.filter { it.completed }
                    val allTags = active.flatMap { it.tags ?: emptyList() }.distinct().sorted()
                    val summary = TaskSummary(
                        todayPending = pending.size,
                        todayCompleted = completed.size,
                        deleted = deleted.size
                    )
                    _uiState.value = _uiState.value.copy(
                        pendingTodos = pending,
                        completedTodos = completed,
                        deletedTodos = deleted,
                        isLoading = false,
                        summary = summary,
                        allTags = allTags
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "[Task] 刷新任务失败")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            )
        }
    }

    fun updateTodo(id: String, text: String?, content: String?, tags: List<String>?, dueDate: String?, quadrant: String?) {
        optimisticUpdate(
            transform = { state ->
                fun updateInList(list: List<NextTodo>) = list.map { todo ->
                    if (todo.id == id) todo.copy(
                        text = text ?: todo.text,
                        content = content ?: todo.content,
                        tags = tags ?: todo.tags,
                        dueDate = dueDate ?: todo.dueDate,
                        quadrant = quadrant ?: todo.quadrant
                    ) else todo
                }
                state.copy(
                    pendingTodos = updateInList(state.pendingTodos),
                    completedTodos = updateInList(state.completedTodos)
                )
            },
            errorMessage = "修改失败"
        ) {
            nextApiService.updateTodo(
                id,
                NextTodoUpdateRequest(text = text, content = content, tags = tags, dueDate = dueDate, quadrant = quadrant)
            )
        }
    }

    fun addTodo(text: String, content: String?, tags: List<String>?, dueDate: String?, quadrant: String? = null) {
        val tempId = "temp-${System.currentTimeMillis()}"
        val tempTodo = NextTodo(
            id = tempId,
            text = text,
            content = content,
            tab = _uiState.value.currentTab,
            tags = tags,
            dueDate = dueDate,
            quadrant = quadrant
        )
        val snapshot = _uiState.value
        _uiState.value = snapshot.copy(pendingTodos = snapshot.pendingTodos + tempTodo)
        recomputeSummary()
        viewModelScope.launch {
            val result = nextApiService.createTodo(
                NextTodoCreateRequest(
                    text = text,
                    tab = _uiState.value.currentTab,
                    content = content,
                    tags = tags,
                    dueDate = dueDate,
                    quadrant = quadrant
                )
            )
            result.fold(
                onSuccess = { serverTodo ->
                    _uiState.value = _uiState.value.copy(
                        pendingTodos = _uiState.value.pendingTodos.map {
                            if (it.id == tempId) serverTodo else it
                        }
                    )
                    recomputeSummary()
                },
                onFailure = { e ->
                    Timber.e(e, "[Task] 添加任务失败")
                    _uiState.value = _uiState.value.copy(
                        pendingTodos = _uiState.value.pendingTodos.filter { it.id != tempId },
                        error = "添加失败：${e.message}"
                    )
                    recomputeSummary()
                }
            )
        }
    }

    fun completeTodo(id: String) {
        optimisticUpdate(
            transform = { state ->
                val todo = state.pendingTodos.find { it.id == id }
                    ?: return@optimisticUpdate state
                state.copy(
                    pendingTodos = state.pendingTodos.filter { it.id != id },
                    completedTodos = listOf(todo.copy(completed = true, progress = 100)) + state.completedTodos
                )
            },
            errorMessage = "操作失败"
        ) {
            nextApiService.updateTodo(id, NextTodoUpdateRequest(completed = true))
        }
    }

    fun uncompleteTodo(id: String) {
        optimisticUpdate(
            transform = { state ->
                val todo = state.completedTodos.find { it.id == id }
                    ?: return@optimisticUpdate state
                state.copy(
                    completedTodos = state.completedTodos.filter { it.id != id },
                    pendingTodos = state.pendingTodos + todo.copy(completed = false, progress = 0)
                )
            },
            errorMessage = "操作失败"
        ) {
            nextApiService.updateTodo(id, NextTodoUpdateRequest(completed = false, progress = 0))
        }
    }

    fun updateProgress(id: String, progress: Int) {
        optimisticUpdate(
            transform = { state ->
                state.copy(
                    pendingTodos = state.pendingTodos.map {
                        if (it.id == id) it.copy(progress = progress) else it
                    }
                )
            },
            errorMessage = "更新进度失败"
        ) {
            nextApiService.updateTodo(id, NextTodoUpdateRequest(progress = progress))
        }
    }

    fun deleteTodo(id: String) {
        optimisticUpdate(
            transform = { state ->
                val todo = (state.pendingTodos + state.completedTodos).find { it.id == id }
                state.copy(
                    pendingTodos = state.pendingTodos.filter { it.id != id },
                    completedTodos = state.completedTodos.filter { it.id != id },
                    deletedTodos = if (todo != null) state.deletedTodos + todo.copy(deleted = true) else state.deletedTodos
                )
            },
            errorMessage = "删除失败"
        ) {
            nextApiService.deleteTodo(id)
        }
    }

    fun restoreTodo(id: String) {
        optimisticUpdate(
            transform = { state ->
                val todo = state.deletedTodos.find { it.id == id }
                    ?: return@optimisticUpdate state
                val restored = todo.copy(deleted = false)
                if (restored.completed) {
                    state.copy(
                        deletedTodos = state.deletedTodos.filter { it.id != id },
                        completedTodos = state.completedTodos + restored
                    )
                } else {
                    state.copy(
                        deletedTodos = state.deletedTodos.filter { it.id != id },
                        pendingTodos = state.pendingTodos + restored
                    )
                }
            },
            errorMessage = "恢复失败"
        ) {
            nextApiService.restoreTodo(id)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
