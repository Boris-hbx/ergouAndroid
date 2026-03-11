## 1. ViewModel

- [x] 1.1 ChatUiState 新增 `suggestions: List<String> = emptyList()`
- [x] 1.2 ChatViewModel 注入 LLMService（构造函数 + Koin 模块）
- [x] 1.3 新增 `generateSuggestions(userMessage, aiReply): List<String>` 私有方法
- [x] 1.4 sendMessageInternal 完成后后台调用 generateSuggestions 并更新状态
- [x] 1.5 onSendMessage 开头清空 suggestions

## 2. UI

- [x] 2.1 ChatContent 中 LazyColumn 插入 suggestion chips（LazyRow + SuggestionChip）
- [x] 2.2 点击 chip 调用 onSend，与手动输入发送行为一致
- [x] 2.3 ChatScreen 绑定完成（onSend 已有，无需额外回调）

## 3. 验证

- [ ] 3.1 手动测试：AI 回复后出现 2-3 个建议 chip
- [ ] 3.2 手动测试：点击 chip 发送消息，chip 消失
- [ ] 3.3 手动测试：LLM 调用失败时无 chip，无报错
