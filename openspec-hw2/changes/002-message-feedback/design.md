## 背景

chat-actions-bar 已实现 AI 消息操作栏（复制 + 重新生成）。本次在同一操作栏中追加点赞/点踩按钮。

## 目标 / 非目标

**目标：**
- MessageEntity 加 feedback 字段，Room 迁移
- 操作栏加点赞/点踩，支持 toggle

**非目标：**
- 反馈数据上报/分析
- 基于反馈的模型微调

## 设计决策

### 1. 数据模型

MessageEntity 新增字段：
```kotlin
val feedback: Int? = null  // null=无反馈, 1=点赞, -1=点踩
```

Room migration v7→v8:
```sql
ALTER TABLE messages ADD COLUMN feedback INTEGER DEFAULT NULL
```

### 2. DAO

```kotlin
@Query("UPDATE messages SET feedback = :feedback WHERE id = :messageId")
suspend fun updateFeedback(messageId: Long, feedback: Int?)
```

### 3. ViewModel

```kotlin
fun onMessageFeedback(messageId: Long, feedback: Int?) {
    viewModelScope.launch {
        chatRepository.updateMessageFeedback(messageId, feedback)
    }
}
```

toggle 逻辑在 UI 层：点击已选按钮 → 传 null，点击新按钮 → 传 1/-1。

### 4. UI

在 MessageBubble 操作栏中，复制和重新生成之间加入点赞/点踩：
```
[复制] [👍] [👎] [重新生成(仅最后一条)]
```

- 图标用 `ThumbUp` / `ThumbDown`（outline 为默认，filled 为已选）
- 已选状态：`MaterialTheme.colorScheme.primary` 着色

MessageBubble 新增参数：
```kotlin
feedback: Int? = null,
onFeedback: ((Int?) -> Unit)? = null
```

## 风险 / 权衡

无显著风险。migration 仅 ALTER TABLE ADD COLUMN，向后兼容。
