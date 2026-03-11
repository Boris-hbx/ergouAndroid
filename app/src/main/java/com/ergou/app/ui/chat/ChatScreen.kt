package com.ergou.app.ui.chat

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ergou.app.data.repository.ShortcutItem
import com.ergou.app.ui.components.MarkdownText
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFeature: (String) -> Unit = {},
    onNavigateToSessionHistory: () -> Unit = {},
    onNavigateToFeatureHub: () -> Unit = {},
    viewModel: ChatViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shortcuts by viewModel.shortcuts.collectAsState()
    val isEditingShortcuts by viewModel.isEditingShortcuts.collectAsState()
    var showApiKeyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isApiKeySet) {
        if (uiState.isApiKeySet == false) {
            showApiKeyDialog = true
        }
    }

    // 刷新快捷栏待办计数
    LaunchedEffect(Unit) {
        viewModel.refreshShortcutBadges()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("二狗") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToFeatureHub) {
                        Icon(Icons.Default.Menu, contentDescription = "功能广场")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSessionHistory) {
                        Icon(Icons.Default.History, contentDescription = "历史会话")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { padding ->
        ChatContent(
            uiState = uiState,
            shortcuts = shortcuts,
            isEditingShortcuts = isEditingShortcuts,
            onSend = viewModel::onSendMessage,
            onStop = viewModel::cancelStreaming,
            onRegenerate = viewModel::regenerateLastMessage,
            onFeedback = viewModel::onMessageFeedback,
            onDismissError = viewModel::dismissError,
            onShortcutClick = { route ->
                viewModel.recordShortcutUsage(route)
                onNavigateToFeature(route)
            },
            onEnterShortcutEdit = viewModel::enterShortcutEditMode,
            onExitShortcutEdit = viewModel::exitShortcutEditMode,
            onTogglePin = viewModel::togglePin,
            modifier = Modifier.padding(padding)
        )
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            onDismiss = { showApiKeyDialog = false },
            onSave = { key ->
                viewModel.onSaveApiKey(key)
                showApiKeyDialog = false
            }
        )
    }
}

@Composable
fun ChatContent(
    uiState: ChatUiState,
    shortcuts: List<ShortcutItem>,
    isEditingShortcuts: Boolean,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onRegenerate: () -> Unit,
    onFeedback: (Long, Int?) -> Unit,
    onDismissError: () -> Unit,
    onShortcutClick: (String) -> Unit,
    onEnterShortcutEdit: () -> Unit,
    onExitShortcutEdit: () -> Unit,
    onTogglePin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Calculate actual LazyColumn item count for correct scroll position
    val hasWelcome = uiState.messages.isEmpty() && uiState.streamingContent.isEmpty()
    val hasSuggestions = uiState.suggestions.isNotEmpty() && !uiState.isSending
    val hasStreaming = uiState.streamingContent.isNotEmpty()
    val hasLoading = uiState.isSending && uiState.streamingContent.isEmpty()
    val lazyItemCount = 1 + // top spacer
        (if (hasWelcome) 1 else 0) +
        uiState.messages.size +
        (if (hasSuggestions) 1 else 0) +
        (if (hasStreaming) 1 else 0) +
        (if (hasLoading) 1 else 0) +
        1 // bottom spacer
    LaunchedEffect(lazyItemCount, uiState.streamingContent) {
        if (lazyItemCount > 2) {
            listState.animateScrollToItem(lazyItemCount - 1)
        }
    }

    val lastAssistantId = remember(uiState.messages) {
        uiState.messages.lastOrNull { it.role == "assistant" }?.id
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 消息列表（用 Box 包裹以支持编辑模式下的透明遮罩）
            Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // 空状态
                if (uiState.messages.isEmpty() && uiState.streamingContent.isEmpty()) {
                    item {
                        WelcomeMessage(
                            suggestions = uiState.welcomeSuggestions,
                            onSend = onSend
                        )
                    }
                }

                // 历史消息
                items(uiState.messages, key = { it.id }) { message ->
                    if (message.role != "system") {
                        MessageBubble(
                            content = message.content,
                            isFromUser = message.role == "user",
                            isLastAssistantMessage = message.id == lastAssistantId,
                            isSending = uiState.isSending,
                            feedback = message.feedback,
                            onCopy = if (message.role == "assistant") {
                                {
                                    clipboardManager.setText(AnnotatedString(message.content))
                                    scope.launch { snackbarHostState.showSnackbar("已复制") }
                                }
                            } else null,
                            onFeedback = if (message.role == "assistant") {
                                { value -> onFeedback(message.id, value) }
                            } else null,
                            onRegenerate = if (message.id == lastAssistantId && !uiState.isSending) {
                                onRegenerate
                            } else null
                        )
                    }
                }

                // 建议追问
                if (uiState.suggestions.isNotEmpty() && !uiState.isSending) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.suggestions.size) { index ->
                                SuggestionChip(
                                    onClick = { onSend(uiState.suggestions[index]) },
                                    label = { Text(uiState.suggestions[index]) }
                                )
                            }
                        }
                    }
                }

                // 流式输出中的消息
                if (uiState.streamingContent.isNotEmpty()) {
                    item {
                        val displayContent = uiState.streamingContent
                            .replace(Regex("""\[SAVE_MEMORY:\w+:.+?]"""), "")
                            .replace(Regex("""\[SAVE_PERSON:.+?:.+?:.+?]"""), "")
                            .trim()
                        if (displayContent.isNotEmpty()) {
                            MessageBubble(
                                content = displayContent,
                                isFromUser = false,
                                isLastAssistantMessage = false,
                                isSending = true,
                                onCopy = null,
                                onRegenerate = null
                            )
                        }
                    }
                }

                // 加载中
                if (uiState.isSending && uiState.streamingContent.isEmpty()) {
                    item { LoadingIndicator() }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // 编辑模式下的透明遮罩：点击退出编辑
            if (isEditingShortcuts) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onExitShortcutEdit() }
                )
            }
            } // Box wrapper end

            // 错误提示
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = onDismissError) {
                            Text("关闭")
                        }
                    }
                ) {
                    Text(error)
                }
            }

            // 快捷条
            ShortcutBar(
                shortcuts = shortcuts,
                isEditing = isEditingShortcuts,
                pinnedCount = shortcuts.count { it.pinned },
                onClick = onShortcutClick,
                onLongPress = onEnterShortcutEdit,
                onTogglePin = onTogglePin
            )

            // 输入框
            var textFieldValue by remember { mutableStateOf(TextFieldValue()) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("说点什么...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    enabled = !uiState.isSending
                )
                if (uiState.isSending) {
                    IconButton(onClick = onStop) {
                        Icon(
                            Icons.Filled.Stop,
                            contentDescription = "停止",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            onSend(textFieldValue.text)
                            textFieldValue = TextFieldValue()
                        },
                        enabled = textFieldValue.text.isNotBlank()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送"
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortcutBar(
    shortcuts: List<ShortcutItem>,
    isEditing: Boolean,
    pinnedCount: Int,
    onClick: (String) -> Unit,
    onLongPress: () -> Unit,
    onTogglePin: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(shortcuts, key = { it.route }) { item ->
            val displayLabel = if (!isEditing && item.badge != null) {
                "${item.label} ${item.badge}"
            } else {
                item.label
            }

            if (isEditing) {
                val canPin = item.pinned || pinnedCount < ChatViewModel.MAX_PINNED
                val rotation by animateFloatAsState(
                    targetValue = if (item.pinned) 0f else 45f,
                    label = "pinRotation"
                )
                FilterChip(
                    selected = item.pinned,
                    onClick = { onTogglePin(item.route) },
                    enabled = canPin,
                    label = { Text(displayLabel) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = if (item.pinned) "已固定" else "未固定",
                            modifier = Modifier
                                .size(16.dp)
                                .graphicsLayer { rotationZ = rotation }
                        )
                    }
                )
            } else {
                Surface(
                    modifier = Modifier.combinedClickable(
                        onClick = { onClick(item.route) },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Text(
                        displayLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeMessage(
    suggestions: List<String> = emptyList(),
    onSend: (String) -> Unit = {}
) {
    val greeting = "善谋者胜，善行者成。"

    val displaySuggestions = suggestions.ifEmpty {
        listOf("查看今日待办", "帮我记一笔账", "聊聊最近怎么样")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "二狗",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = greeting,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            displaySuggestions.forEach { topic ->
                SuggestionChip(
                    onClick = { onSend(topic) },
                    label = { Text(topic) }
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                )
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "  二狗正在想...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    content: String,
    isFromUser: Boolean,
    isLastAssistantMessage: Boolean = false,
    isSending: Boolean = false,
    feedback: Int? = null,
    onCopy: (() -> Unit)? = null,
    onFeedback: ((Int?) -> Unit)? = null,
    onRegenerate: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = if (isFromUser)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (isFromUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            val textColor = if (isFromUser)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant

            if (isFromUser) {
                Text(text = content, color = textColor)
            } else {
                MarkdownText(text = content, color = textColor)
            }
        }

        // AI 消息操作栏
        if (!isFromUser && !isSending) {
            Row(
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (onCopy != null) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "复制",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (onFeedback != null) {
                    IconButton(
                        onClick = { onFeedback(if (feedback == 1) null else 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (feedback == 1) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "点赞",
                            modifier = Modifier.size(16.dp),
                            tint = if (feedback == 1) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onFeedback(if (feedback == -1) null else -1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (feedback == -1) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                            contentDescription = "点踩",
                            modifier = Modifier.size(16.dp),
                            tint = if (feedback == -1) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (onRegenerate != null) {
                    IconButton(onClick = onRegenerate, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "重新生成",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApiKeyDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var keyInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置 API Key") },
        text = {
            Column {
                Text("请输入 DeepSeek API Key")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    placeholder = { Text("sk-...") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(keyInput.trim()) },
                enabled = keyInput.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
