## 1. 数据层

- [x] 1.1 MessageEntity 新增 `feedback: Int? = null` 字段
- [x] 1.2 MessageDao 新增 `updateFeedback(messageId, feedback)` 方法
- [x] 1.3 ChatRepository 接口新增 `updateMessageFeedback`，ChatRepositoryImpl 实现
- [x] 1.4 ErgouDatabase v7→v8 迁移：ALTER TABLE messages ADD COLUMN feedback

## 2. ViewModel

- [x] 2.1 ChatViewModel 新增 `onMessageFeedback(messageId, feedback)` 方法

## 3. UI

- [x] 3.1 MessageBubble 新增 `feedback` 和 `onFeedback` 参数
- [x] 3.2 操作栏中加入点赞/点踩图标，支持 toggle 和高亮
- [x] 3.3 LazyColumn 中传入 message.feedback 和回调

## 4. 验证

- [ ] 4.1 手动测试：点赞 → 图标高亮，数据库写入
- [ ] 4.2 手动测试：再次点赞 → 取消高亮
- [ ] 4.3 手动测试：切换点踩 → 高亮切换
