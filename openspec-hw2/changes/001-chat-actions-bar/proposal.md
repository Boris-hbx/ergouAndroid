## Why

当前 AI 回复是纯展示，用户无法复制内容、重新生成不满意的回复、或在生成过程中打断。这些是主流 AI 助手（豆包、ChatGPT）的标配交互，缺失会让用户感到功能不完整。B-050 + B-051 一起做，因为操作栏和停止按钮共享同一个 UI 区域（消息气泡下方）。

## What Changes

- AI 回复气泡下方新增**操作栏**：复制、重新生成两个按钮
- 流式生成过程中，输入框区域的发送按钮替换为**停止按钮**，点击可打断生成
- ViewModel 新增 `cancelStreaming()` 和 `regenerateLastMessage()` 方法
- `MessageBubble` 组件重构，AI 消息支持操作栏插槽

## Capabilities

### New Capabilities
- `message-actions`: AI 回复操作栏（复制到剪贴板、重新生成）
- `stream-control`: 流式输出控制（停止按钮、取消协程）

### Modified Capabilities
<!-- 无现有 spec 需要修改 -->

## Impact

- **UI 层**: `ChatScreen.kt` — `MessageBubble` 重构，新增操作栏组件，输入框区域条件渲染停止按钮
- **ViewModel**: `ChatViewModel.kt` — 新增 cancel/regenerate 方法，`sendJob` 暴露以支持取消
- **依赖**: 无新依赖，复制功能使用 Android `ClipboardManager`
- **数据层**: 重新生成需要删除最后一条 AI 消息再重发，涉及 `ChatRepository`
