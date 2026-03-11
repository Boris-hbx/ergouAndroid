## 背景

当前 `ChatScreen` 的 `MessageBubble` 是纯展示组件，无任何交互按钮。`ChatViewModel.sendMessageInternal()` 启动的协程没有保存 Job 引用，无法从外部取消。`MessageDao` 没有按 ID 删除单条消息的方法。

## 目标 / 非目标

**目标：**
- AI 回复下方显示操作栏（复制 + 重新生成）
- 流式生成时发送按钮变为停止按钮，支持打断
- 停止后保存已生成的部分内容

**非目标：**
- 点赞/点踩（B-053，后续单独做）
- 建议追问（B-052，后续单独做）
- 用户消息的编辑/删除

## 设计决策

### 1. MessageBubble 重构

将 `MessageBubble` 从纯展示升级为支持操作栏的组件：

```
MessageBubble(
    content: String,
    isFromUser: Boolean,
    isLastAssistantMessage: Boolean,  // 新增
    isSending: Boolean,               // 新增
    onCopy: (() -> Unit)?,            // 新增
    onRegenerate: (() -> Unit)?       // 新增
)
```

- AI 消息且非流式中：显示复制按钮
- AI 消息且是最后一条且非流式中：额外显示重新生成按钮
- 用户消息：无操作栏
- 操作栏为 `Row`，放在气泡下方，使用 `IconButton` + Material 3 图标

### 2. 停止按钮（发送按钮替换）

在 `ChatScreen` 输入区域：
- `isSending == false` → 显示发送按钮 (`Icons.AutoMirrored.Filled.Send`)
- `isSending == true` → 显示停止按钮 (`Icons.Filled.Stop` 或方形图标)

点击停止按钮调用 `viewModel.cancelStreaming()`。

### 3. ViewModel 变更

**新增 `sendJob` 字段**：保存 `sendMessageInternal` 启动的协程 Job。

```kotlin
private var sendJob: Job? = null

private fun sendMessageInternal(text: String) {
    sendJob = viewModelScope.launch {
        // ... 现有逻辑
    }
}
```

**新增 `cancelStreaming()` 方法**：
```kotlin
fun cancelStreaming() {
    sendJob?.cancel()
    val partial = _uiState.value.streamingContent
    viewModelScope.launch {
        if (partial.isNotBlank()) {
            val sessionId = _uiState.value.currentSessionId ?: return@launch
            chatRepository.saveMessage(sessionId, "assistant", partial)
        }
        _uiState.value = _uiState.value.copy(
            isSending = false,
            streamingContent = ""
        )
    }
}
```

**新增 `regenerateLastMessage()` 方法**：
```kotlin
fun regenerateLastMessage() {
    if (_uiState.value.isSending) return
    viewModelScope.launch {
        val sessionId = _uiState.value.currentSessionId ?: return@launch
        val messages = _uiState.value.messages
        val lastAssistant = messages.lastOrNull { it.role == "assistant" } ?: return@launch
        chatRepository.deleteMessage(lastAssistant.id)
        // 找到最后一条用户消息重新发送
        val lastUserMsg = messages.lastOrNull { it.role == "user" }?.content ?: return@launch
        sendMessageInternal(lastUserMsg, skipSaveUser = true)
    }
}
```

`sendMessageInternal` 需要增加 `skipSaveUser` 参数，重新生成时跳过保存用户消息（已经存在）。

### 4. 数据层变更

**MessageDao 新增**：
```kotlin
@Query("DELETE FROM messages WHERE id = :messageId")
suspend fun deleteById(messageId: Long)
```

**ChatRepository 新增**：
```kotlin
suspend fun deleteMessage(messageId: Long)
```

### 5. 剪贴板复制

使用 Android `ClipboardManager`，在 Composable 中通过 `LocalContext.current` 获取：
```kotlin
val context = LocalContext.current
val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
clipboardManager.setPrimaryClip(ClipData.newPlainText("ergou", content))
```

反馈使用 `SnackbarHostState`（ChatScreen 已有 Scaffold）。

## 风险 / 权衡

| 风险 | 应对 |
|------|------|
| `sendJob.cancel()` 后 try-catch 中的 `CancellationException` | 需要在 catch 中区分 CancellationException 和其他异常，不对 CancellationException 显示错误 |
| 重新生成删除消息后 Flow 自动更新 UI 可能闪烁 | 可接受，删除+重新流式几乎同时发生 |
| `sendMessageInternal` 增加 `skipSaveUser` 参数 | 保持向后兼容，默认值 false |
