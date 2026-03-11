## Why

用户无法对 AI 回复做好/坏反馈。点赞/点踩是主流 AI 助手（豆包、ChatGPT）标配，可用于未来偏好微调。操作栏已在 chat-actions-bar 中实现，本次只需在已有操作栏中添加两个按钮。

## What Changes

- MessageEntity 新增 `feedback` 字段（null=无反馈, 1=点赞, -1=点踩）
- Room 数据库 v7→v8 迁移，messages 表加 feedback 列
- AI 回复操作栏新增点赞/点踩图标按钮，点击后高亮已选状态

## Capabilities

### New Capabilities
- `message-feedback`: 消息点赞/点踩反馈

### Modified Capabilities

## Impact

- **数据层**: MessageEntity + MessageDao + ChatRepository + Room migration
- **ViewModel**: ChatViewModel 新增 feedback 方法
- **UI**: ChatScreen MessageBubble 操作栏扩展
