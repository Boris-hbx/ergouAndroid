package com.ergou.app.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import com.ergou.app.data.remote.dto.NextTodo
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

sealed class TaskDialogMode {
    data object Add : TaskDialogMode()
    data class Edit(val todo: NextTodo) : TaskDialogMode()
}

private val tabs = listOf("today" to "今天", "week" to "本周", "month" to "30天")

private val quadrantColors = mapOf(
    "urgent-important" to Color(0xFFE53935),        // 红
    "not-urgent-important" to Color(0xFFFB8C00),    // 橙
    "urgent-not-important" to Color(0xFF1E88E5),    // 蓝
    "not-urgent-not-important" to Color(0xFF9E9E9E) // 灰
)

private val quadrantLabels = listOf(
    "urgent-important" to "紧急重要",
    "not-urgent-important" to "重要不紧急",
    "urgent-not-important" to "紧急不重要",
    "not-urgent-not-important" to "不紧急不重要"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: TaskViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var dialogMode by remember { mutableStateOf<TaskDialogMode?>(null) }
    var detailTodo by remember { mutableStateOf<NextTodo?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("待办事项") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.isLoggedIn) {
                FloatingActionButton(onClick = { dialogMode = TaskDialogMode.Add }) {
                    Icon(Icons.Default.Add, contentDescription = "添加任务")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (!uiState.isLoggedIn) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("请先登录 Next 账号", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onNavigateToSettings) {
                        Text("前往设置")
                    }
                }
            } else {
                // Tab 栏（带计数）
                val selectedIndex = tabs.indexOfFirst { it.first == uiState.currentTab }.coerceAtLeast(0)
                TabRow(selectedTabIndex = selectedIndex) {
                    tabs.forEachIndexed { index, (key, label) ->
                        val count = if (index == selectedIndex) uiState.pendingTodos.size else null
                        Tab(
                            selected = index == selectedIndex,
                            onClick = { viewModel.switchTab(key) },
                            text = {
                                Text(
                                    if (count != null && count > 0) "$label（$count）" else label
                                )
                            }
                        )
                    }
                }

                // 内容
                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.pendingTodos.isEmpty() && uiState.completedTodos.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("还没有任务", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("点击 + 或告诉二狗来添加", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    else -> {
                        var showPending by remember { mutableStateOf(true) }
                        var showCompleted by remember { mutableStateOf(false) }
                        var showDeleted by remember { mutableStateOf(false) }

                        val displayPending = if (uiState.filterTag != null) {
                            uiState.pendingTodos.filter { it.tags?.contains(uiState.filterTag) == true }
                        } else uiState.pendingTodos
                        val displayCompleted = if (uiState.filterTag != null) {
                            uiState.completedTodos.filter { it.tags?.contains(uiState.filterTag) == true }
                        } else uiState.completedTodos
                        val displayDeleted = uiState.deletedTodos

                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 摘要卡片
                            item(key = "summary") {
                                TaskSummaryCard(summary = uiState.summary)
                            }

                            if (uiState.filterTag != null && displayPending.isEmpty() && displayCompleted.isEmpty()) {
                                item(key = "filter-empty") {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("该标签下没有任务", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            // ── 待办折叠区 ──
                            if (displayPending.isNotEmpty()) {
                                item(key = "pending-header") {
                                    SectionHeader(
                                        title = "待办 (${displayPending.size})",
                                        expanded = showPending,
                                        color = Color(0xFF1565C0),
                                        onClick = { showPending = !showPending }
                                    )
                                }

                                if (showPending) {
                                    items(displayPending, key = { it.id }) { todo ->
                                        SwipeTodoItem(
                                            todo = todo,
                                            filterTag = uiState.filterTag,
                                            onComplete = { viewModel.completeTodo(todo.id) },
                                            onUpdateProgress = { viewModel.updateProgress(todo.id, it) },
                                            onClick = { detailTodo = todo },
                                            onLongClick = { dialogMode = TaskDialogMode.Edit(todo) },
                                            onTagClick = { tag -> viewModel.setFilterTag(tag) },
                                            onDelete = {
                                                viewModel.deleteTodo(todo.id)
                                                scope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = "已删除",
                                                        actionLabel = "撤销",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        viewModel.restoreTodo(todo.id)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // ── 已完成折叠区 ──
                            if (displayCompleted.isNotEmpty()) {
                                item(key = "completed-header") {
                                    SectionHeader(
                                        title = "已完成 (${displayCompleted.size})",
                                        expanded = showCompleted,
                                        color = Color(0xFF9C27B0),
                                        onClick = { showCompleted = !showCompleted }
                                    )
                                }

                                if (showCompleted) {
                                    items(displayCompleted, key = { it.id }) { todo ->
                                        SwipeTodoItem(
                                            todo = todo,
                                            filterTag = uiState.filterTag,
                                            onComplete = { viewModel.uncompleteTodo(todo.id) },
                                            onUpdateProgress = { viewModel.updateProgress(todo.id, it) },
                                            onClick = { detailTodo = todo },
                                            onLongClick = { dialogMode = TaskDialogMode.Edit(todo) },
                                            onTagClick = { tag -> viewModel.setFilterTag(tag) },
                                            onDelete = {
                                                viewModel.deleteTodo(todo.id)
                                                scope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = "已删除",
                                                        actionLabel = "撤销",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        viewModel.restoreTodo(todo.id)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // ── 已删除折叠区 ──
                            if (displayDeleted.isNotEmpty()) {
                                item(key = "deleted-header") {
                                    SectionHeader(
                                        title = "已删除 (${displayDeleted.size})",
                                        expanded = showDeleted,
                                        color = MaterialTheme.colorScheme.error,
                                        onClick = { showDeleted = !showDeleted }
                                    )
                                }

                                if (showDeleted) {
                                    items(displayDeleted, key = { "deleted-${it.id}" }) { todo ->
                                        DeletedTodoItem(
                                            todo = todo,
                                            onRestore = { viewModel.restoreTodo(todo.id) }
                                        )
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    dialogMode?.let { mode ->
        TaskDialog(
            mode = mode,
            allTags = uiState.allTags,
            onDismiss = { dialogMode = null },
            onSubmit = { text, content, tags, dueDate, quadrant ->
                when (mode) {
                    is TaskDialogMode.Add -> {
                        viewModel.addTodo(text, content, tags, dueDate, quadrant)
                    }
                    is TaskDialogMode.Edit -> {
                        val orig = mode.todo
                        val newText = text.takeIf { it != orig.text }
                        val newContent = content.takeIf { it != (orig.content ?: "") }
                        val newTags = tags.takeIf { it != orig.tags }
                        val newDueDate = dueDate.takeIf { it != orig.dueDate }
                        val newQuadrant = quadrant.takeIf { it != orig.quadrant }
                        if (newText != null || newContent != null || newTags != null || newDueDate != null || newQuadrant != null) {
                            viewModel.updateTodo(orig.id, newText, newContent, newTags, newDueDate, newQuadrant)
                        }
                    }
                }
                dialogMode = null
            }
        )
    }

    detailTodo?.let { todo ->
        TaskDetailBottomSheet(
            todo = todo,
            snackbarHostState = snackbarHostState,
            onDismiss = {
                detailTodo = null
            },
            onUpdate = { text, content, tags, dueDate, quadrant ->
                viewModel.updateTodo(todo.id, text, content, tags, dueDate, quadrant)
            },
            onComplete = {
                viewModel.completeTodo(todo.id)
                detailTodo = null
            },
            onDelete = {
                viewModel.deleteTodo(todo.id)
                detailTodo = null
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "已删除",
                        actionLabel = "撤销",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreTodo(todo.id)
                    }
                }
            },
            onUpdateProgress = { progress ->
                viewModel.updateProgress(todo.id, progress)
            }
        )
    }
}

@Composable
fun TaskSummaryCard(summary: TaskSummary) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F9))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(label = "待办", count = summary.todayPending, color = Color(0xFF7B1FA2))
            SummaryItem(label = "已完成", count = summary.todayCompleted, color = Color(0xFF9C27B0))
            SummaryItem(label = "已删除", count = summary.deleted, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SummaryItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            color = Color(0xFF7B1FA2)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    expanded: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            ),
            color = color,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (expanded) "收起" else "展开",
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeTodoItem(
    todo: NextTodo,
    filterTag: String?,
    onComplete: () -> Unit,
    onUpdateProgress: (Int) -> Unit = {},
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onTagClick: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirm = true
            }
            false // never auto-dismiss, we handle it via dialog
        }
    )

    // Reset swipe state when dialog is dismissed
    LaunchedEffect(showDeleteConfirm) {
        if (!showDeleteConfirm && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(14.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        TodoItem(
            todo = todo,
            onCheckboxClick = {
                if (todo.completed) {
                    onComplete()
                } else {
                    showProgressDialog = true
                }
            },
            onClick = onClick,
            onLongClick = onLongClick
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${todo.text}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    if (showProgressDialog) {
        ProgressConfirmDialog(
            taskName = todo.text,
            initialProgress = todo.progress,
            onDismiss = { showProgressDialog = false },
            onConfirm = { progress ->
                showProgressDialog = false
                if (progress >= 100) {
                    onUpdateProgress(100)
                    onComplete()
                } else {
                    onUpdateProgress(progress)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoItem(
    todo: NextTodo,
    onCheckboxClick: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val cardColor = if (todo.completed) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        Color(0xFFE3F2FD) // 固定浅蓝色
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .padding(start = 4.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.completed,
                onCheckedChange = { onCheckboxClick() },
                modifier = Modifier.size(36.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.text,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (todo.completed) TextDecoration.LineThrough else null,
                    color = if (todo.completed) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                )

                val createdLabel = formatCreatedAt(todo.createdAt)
                if (createdLabel != null) {
                    Text(
                        text = "创建于 $createdLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (todo.progress > 0 && !todo.completed) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressRing(progress = todo.progress)
            }
        }
    }
}

@Composable
private fun formatCreatedAt(createdAt: String?): String? {
    if (createdAt == null) return null
    return remember(createdAt) {
        runCatching {
            val instant = Instant.parse(createdAt)
            val local = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
            "${local.monthValue}月${local.dayOfMonth}日 %02d:%02d".format(local.hour, local.minute)
        }.getOrNull()
    }
}

@Composable
private fun getDueDateColor(dueDate: String?, completed: Boolean): Color {
    val defaultColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    if (dueDate == null || completed) return defaultColor
    val due = runCatching { LocalDate.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE) }
        .getOrNull() ?: return defaultColor
    val today = LocalDate.now()
    return when {
        due.isBefore(today) -> errorColor                    // 逾期：红色
        due.isEqual(today) -> Color(0xFFE53935)              // 今天到期：红色
        due.isEqual(today.plusDays(1)) -> Color(0xFFFB8C00)  // 明天到期：橙色
        else -> defaultColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    mode: TaskDialogMode,
    allTags: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (String, String?, List<String>?, String?, String?) -> Unit
) {
    val editTodo = (mode as? TaskDialogMode.Edit)?.todo
    val isEdit = editTodo != null

    var text by remember { mutableStateOf(TextFieldValue(editTodo?.text ?: "")) }
    var content by remember { mutableStateOf(TextFieldValue(editTodo?.content ?: "")) }
    var tags by remember { mutableStateOf(TextFieldValue(editTodo?.tags?.joinToString(", ") ?: "")) }
    var dueDate by remember { mutableStateOf(editTodo?.dueDate ?: "") }
    var selectedQuadrant by remember { mutableStateOf(editTodo?.quadrant) }

    // 编辑模式：有高级字段值时默认展开
    val hasAdvancedValues = isEdit && (
        !editTodo?.content.isNullOrBlank() ||
        !editTodo?.tags.isNullOrEmpty() ||
        editTodo?.dueDate != null ||
        editTodo?.quadrant != null
    )
    var showAdvanced by remember { mutableStateOf(hasAdvancedValues) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTagSuggestions by remember { mutableStateOf(false) }

    // 标签自动补全：基于当前输入最后一个逗号后的文本过滤
    val currentTagInput = tags.text.substringAfterLast(",").trim()
    val tagSuggestions = if (currentTagInput.isNotBlank()) {
        allTags.filter { it.contains(currentTagInput, ignoreCase = true) && it != currentTagInput }
    } else {
        emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑任务" else "添加任务") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("任务标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { showAdvanced = !showAdvanced }) {
                    Text(if (showAdvanced) "收起选项" else "更多选项")
                }

                Column(modifier = Modifier.animateContentSize()) {
                  if (showAdvanced) {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("详细描述（可选）") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 四象限选择器
                        Text("优先级", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                        QuadrantSelector(
                            selected = selectedQuadrant,
                            onSelect = { selectedQuadrant = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 标签（带自动补全）
                        Box {
                            OutlinedTextField(
                                value = tags,
                                onValueChange = {
                                    tags = it
                                    showTagSuggestions = true
                                },
                                label = { Text("标签（逗号分隔，可选）") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = showTagSuggestions && tagSuggestions.isNotEmpty(),
                                onDismissRequest = { showTagSuggestions = false }
                            ) {
                                tagSuggestions.take(5).forEach { suggestion ->
                                    DropdownMenuItem(
                                        text = { Text(suggestion) },
                                        onClick = {
                                            val prefix = tags.text.substringBeforeLast(",", "")
                                            val newText = if (prefix.isBlank()) suggestion else "$prefix, $suggestion"
                                            tags = TextFieldValue(newText)
                                            showTagSuggestions = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 到期日（DatePicker）
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = {},
                            label = { Text("截止日期（可选）") },
                            readOnly = true,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            trailingIcon = {
                                if (dueDate.isNotBlank()) {
                                    IconButton(onClick = { dueDate = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "清除日期", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }.also {
                                LaunchedEffect(it) {
                                    it.interactions.collect { interaction ->
                                        if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                            showDatePicker = true
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val tagList = tags.text.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null }
                    onSubmit(
                        text.text,
                        content.text.ifBlank { null },
                        tagList,
                        dueDate.ifBlank { null },
                        selectedQuadrant
                    )
                },
                enabled = text.text.isNotBlank()
            ) { Text(if (isEdit) "保存" else "添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        dueDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskDetailBottomSheet(
    todo: NextTodo,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onUpdate: (text: String?, content: String?, tags: List<String>?, dueDate: String?, quadrant: String?) -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onUpdateProgress: (Int) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Editable states
    var editTitle by remember { mutableStateOf(todo.text) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var editContent by remember { mutableStateOf(todo.content ?: "") }
    var isEditingContent by remember { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }
    var editTags by remember { mutableStateOf(todo.tags ?: emptyList()) }
    var newTagInput by remember { mutableStateOf("") }
    var editDueDate by remember { mutableStateOf(todo.dueDate ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            // Save pending edits before closing
            val newText = editTitle.takeIf { it != todo.text && it.isNotBlank() }
            val newContent = editContent.takeIf { it != (todo.content ?: "") }
            val newTags = editTags.takeIf { it != (todo.tags ?: emptyList<String>()) }
            val newDueDate = editDueDate.takeIf { it != (todo.dueDate ?: "") }
            if (newText != null || newContent != null || newTags != null || newDueDate != null) {
                onUpdate(
                    newText,
                    newContent?.ifBlank { null },
                    newTags,
                    newDueDate?.ifBlank { null },
                    null
                )
            }
            onDismiss()
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // 1. 标题
            if (isEditingTitle) {
                LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }
                OutlinedTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocusRequester)
                        .onFocusChanged { if (!it.isFocused && isEditingTitle) {
                            isEditingTitle = false
                            if (editTitle != todo.text && editTitle.isNotBlank()) {
                                onUpdate(editTitle, null, null, null, null)
                            }
                        }},
                    textStyle = MaterialTheme.typography.headlineSmall,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        isEditingTitle = false
                        if (editTitle != todo.text && editTitle.isNotBlank()) {
                            onUpdate(editTitle, null, null, null, null)
                        }
                    })
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEditingTitle = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = editTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑标题",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. 状态行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (todo.progress > 0) {
                    Text(
                        text = "${todo.progress}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = if (todo.completed) "已完成" else "进行中",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (todo.completed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                val qLabel = quadrantLabels.find { it.first == todo.quadrant }
                val qColor = quadrantColors[todo.quadrant]
                if (qLabel != null && qColor != null) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text(qLabel.second, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(qColor))
                        }
                    )
                }
            }

            // 2.5 附加信息（负责人、创建时间）
            if (todo.assignee != null || todo.createdAt != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (todo.assignee != null) {
                        Text(
                            text = "负责人: ${todo.assignee}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (todo.createdAt != null) {
                        Text(
                            text = "创建: ${todo.createdAt.take(10)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // 3. 描述
            Text("描述", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            if (isEditingContent) {
                LaunchedEffect(Unit) { contentFocusRequester.requestFocus() }
                OutlinedTextField(
                    value = editContent,
                    onValueChange = { editContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(contentFocusRequester)
                        .onFocusChanged { if (!it.isFocused && isEditingContent) {
                            isEditingContent = false
                            if (editContent != (todo.content ?: "")) {
                                onUpdate(null, editContent.ifBlank { null }, null, null, null)
                            }
                        }},
                    minLines = 2,
                    maxLines = 6,
                    placeholder = { Text("添加描述...") }
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEditingContent = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = editContent.ifBlank { "点击添加描述..." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (editContent.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑描述",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // 4. 标签
            Text("标签", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                editTags.forEach { tag ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            editTags = editTags - tag
                            onUpdate(null, null, editTags, null, null)
                        },
                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "移除", modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("添加标签") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        val tag = newTagInput.trim()
                        if (tag.isNotBlank() && tag !in editTags) {
                            editTags = editTags + tag
                            onUpdate(null, null, editTags, null, null)
                        }
                        newTagInput = ""
                    })
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // 5. 截止日期
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "截止日期",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editDueDate.isNotBlank()) editDueDate else "设置截止日期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (editDueDate.isNotBlank()) {
                        getDueDateColor(editDueDate, todo.completed)
                    } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
                if (editDueDate.isNotBlank()) {
                    IconButton(onClick = {
                        editDueDate = ""
                        onUpdate(null, null, null, "", null)
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "清除日期", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // 6. 进度滑块
            if (!todo.completed) {
                var sliderProgress by remember { mutableStateOf(todo.progress.toFloat()) }
                var showCompleteConfirm by remember { mutableStateOf(false) }
                var preConfirmProgress by remember { mutableStateOf(0f) }
                Text("进度", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = sliderProgress,
                        onValueChange = { sliderProgress = it },
                        onValueChangeFinished = {
                            val newProgress = sliderProgress.toInt()
                            if (newProgress >= 100) {
                                preConfirmProgress = todo.progress.toFloat()
                                showCompleteConfirm = true
                            } else {
                                onUpdateProgress(newProgress)
                            }
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${sliderProgress.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (showCompleteConfirm) {
                    AlertDialog(
                        onDismissRequest = {
                            showCompleteConfirm = false
                            sliderProgress = preConfirmProgress
                        },
                        title = { Text("确认完成") },
                        text = { Text("确认完成该任务？") },
                        confirmButton = {
                            TextButton(onClick = {
                                showCompleteConfirm = false
                                onUpdateProgress(100)
                                onComplete()
                            }) { Text("确认") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showCompleteConfirm = false
                                sliderProgress = preConfirmProgress
                            }) { Text("取消") }
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("已完成", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }

    // DatePicker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        editDueDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        onUpdate(null, null, null, editDueDate, null)
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

}

@Composable
fun DeletedTodoItem(todo: NextTodo, onRestore: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.text,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (todo.deletedAt != null) {
                    Text(
                        text = "删除于 ${todo.deletedAt.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            IconButton(onClick = onRestore) {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = "恢复",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ProgressConfirmDialog(
    taskName: String,
    initialProgress: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(initialProgress.coerceIn(0, 100).toFloat()) }
    val currentProgress = sliderValue.roundToInt()
    val isComplete = currentProgress >= 100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("更新进度") },
        text = {
            Column {
                Text(
                    text = taskName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${currentProgress}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isComplete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..100f,
                    steps = 9, // 0,10,20,...,100
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(currentProgress) }) {
                Text(if (isComplete) "完成" else "确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun CircularProgressRing(progress: Int, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    Box(modifier = modifier.size(28.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 3.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress / 100f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$progress",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp)),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuadrantSelector(selected: String?, onSelect: (String?) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        quadrantLabels.forEach { (key, label) ->
            val color = quadrantColors[key] ?: Color.Gray
            FilterChip(
                selected = selected == key,
                onClick = { onSelect(if (selected == key) null else key) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            )
        }
    }
}
