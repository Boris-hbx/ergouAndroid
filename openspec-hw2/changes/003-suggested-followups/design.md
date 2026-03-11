## 背景

当前 AI 回复后对话停止，用户需要主动想下一个问题。豆包等竞品有"猜你想问"功能。本功能在 AI 回复完成后，后台调用 LLM 生成 2-3 个建议追问，展示为可点击的 Chip。

## 目标 / 非目标

**目标：**
- AI 回复后自动生成 2-3 个建议追问
- 以 SuggestionChip 展示，点击直接发送
- 不阻塞主对话流程

**非目标：**
- 建议不持久化到数据库
- 不做个性化推荐（基于历史偏好）

## 设计决策

### 1. UiState 新增 suggestions

```kotlin
data class ChatUiState(
    // ... 现有字段
    val suggestions: List<String> = emptyList()
)
```

### 2. 生成逻辑

在 `sendMessageInternal` 中，AI 回复保存后，后台发起一次**非流式** LLM 调用生成建议：

```kotlin
// AI 回复完成后
viewModelScope.launch {
    try {
        val suggestions = generateSuggestions(text, cleanResponse)
        _uiState.value = _uiState.value.copy(suggestions = suggestions)
    } catch (e: Exception) {
        Timber.d("[Chat] 建议追问生成失败: ${e.message}")
    }
}
```

`generateSuggestions` 方法：
```kotlin
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
```

需要在 ChatViewModel 中注入 `LLMService`。

### 3. 发送时清空

`onSendMessage` 和发送建议时清空 suggestions：
```kotlin
_uiState.value = _uiState.value.copy(suggestions = emptyList())
```

### 4. UI 展示

在 LazyColumn 中，最后一条 AI 消息之后、流式消息之前，插入建议 Chip 行：

```kotlin
if (uiState.suggestions.isNotEmpty() && !uiState.isSending) {
    item {
        LazyRow {
            items(uiState.suggestions) { suggestion ->
                SuggestionChip(
                    onClick = { onSendSuggestion(suggestion) },
                    label = { Text(suggestion) }
                )
            }
        }
    }
}
```

### 5. LLMService 注入

ChatViewModel 当前没有直接持有 LLMService。有两个选择：
- **方案 A**: 通过 ToolExecutor（已注入）间接访问 → 但 ToolExecutor 不暴露非流式 chat
- **方案 B**: 直接注入 LLMService 到 ChatViewModel ✅

选择方案 B，在 Koin 中添加 LLMService 参数。

## 风险 / 权衡

| 风险 | 应对 |
|------|------|
| 额外 LLM 调用增加延迟和成本 | maxTokens=200，温度 0.8，轻量调用；失败静默忽略 |
| 建议质量不稳定 | 限制输入长度(aiReply.take(500))，明确 prompt 格式 |
| 与 cancelStreaming 的交互 | 取消时不生成建议（只有正常完成才生成） |
