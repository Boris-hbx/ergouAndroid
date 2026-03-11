## 1. 数据层

- [x] 1.1 MessageDao 新增 `deleteById(messageId: Long)` 方法
- [x] 1.2 ChatRepository 接口新增 `deleteMessage(messageId: Long)`，ChatRepositoryImpl 实现

## 2. ViewModel

- [x] 2.1 ChatViewModel 新增 `sendJob` 字段，`sendMessageInternal` 启动协程时赋值
- [x] 2.2 新增 `cancelStreaming()` 方法：取消 sendJob，保存已生成的部分内容，重置 UI 状态
- [x] 2.3 catch 块中处理 `CancellationException`：不显示错误提示，直接返回
- [x] 2.4 `sendMessageInternal` 增加 `skipSaveUser` 参数（默认 false），重新生成时跳过保存用户消息
- [x] 2.5 新增 `regenerateLastMessage()` 方法：删除最后一条 AI 消息，重新发送上下文

## 3. UI — 操作栏

- [x] 3.1 重构 `MessageBubble` 签名：新增 `isLastAssistantMessage`、`isSending`、`onCopy`、`onRegenerate` 参数
- [x] 3.2 实现操作栏 `Row`（复制图标 + 重新生成图标），放在 AI 气泡下方
- [x] 3.3 实现复制功能：ClipboardManager + "已复制" Snackbar 反馈
- [x] 3.4 在 `LazyColumn` 消息列表中传入正确的参数（判断 isLastAssistantMessage、绑定回调）

## 4. UI — 停止按钮

- [x] 4.1 输入区域条件渲染：`isSending` 时发送按钮替换为停止按钮（方形图标）
- [x] 4.2 停止按钮点击绑定 `viewModel.cancelStreaming()`
- [x] 4.3 流式生成时输入框设为只读（`enabled = false`）

## 5. 验证

- [ ] 5.1 手动测试：复制 AI 回复 → 剪贴板内容正确，显示"已复制"
- [ ] 5.2 手动测试：点击重新生成 → 旧回复删除，新回复流式展示
- [ ] 5.3 手动测试：流式中点击停止 → 部分内容保存，UI 恢复
- [ ] 5.4 手动测试：空内容时停止 → 不保存空消息
