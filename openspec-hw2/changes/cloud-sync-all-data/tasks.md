## 1. 后端 API（Next.js）

- [ ] 1.1 创建 Sessions API：`GET/POST /api/sessions`、`GET/PUT/DELETE /api/sessions/[id]`
- [ ] 1.2 创建 Messages API：`GET/POST /api/sessions/[id]/messages`、`PUT/DELETE /api/messages/[id]`
- [ ] 1.3 创建 Memories API：`GET/POST /api/memories`、`PUT/DELETE /api/memories/[id]`、`GET /api/memories/search`
- [ ] 1.4 创建 People API：`GET/POST /api/people`、`PUT/DELETE /api/people/[id]`
- [ ] 1.5 创建 Soul API：`GET/PUT /api/soul/state`、`GET/POST /api/soul/logs`
- [ ] 1.6 创建 Reminders API：`GET/POST /api/reminders`、`PUT/DELETE /api/reminders/[id]`

## 2. Room Schema 升级

- [ ] 2.1 SessionEntity 添加 `remoteId: String?` 和 `syncedAt: Long` 字段
- [ ] 2.2 MessageEntity 添加 `remoteId: String?` 和 `syncedAt: Long` 字段
- [ ] 2.3 MemoryEntity 添加 `remoteId: String?` 和 `syncedAt: Long` 字段
- [ ] 2.4 PersonEntity 添加 `remoteId: String?` 和 `syncedAt: Long` 字段
- [ ] 2.5 ReminderEntity 添加 `remoteId: String?` 和 `syncedAt: Long` 字段
- [ ] 2.6 SoulEvolutionLogEntity 添加 `remoteId: String?` 和 `syncedAt: Long` 字段
- [ ] 2.7 SoulStateEntity 添加 `syncedAt: Long` 字段
- [ ] 2.8 编写 Room Migration v8→v9（ALTER TABLE ADD COLUMN 所有新字段）
- [ ] 2.9 各 DAO 添加 `getByRemoteId()`、`getUnsynced()` 查询方法

## 3. NextApiService 扩展

- [ ] 3.1 添加 DTO 类：NextSession、NextMessage、NextMemory、NextPerson、NextSoulState、NextSoulLog、NextReminder
- [ ] 3.2 添加 Sessions 端点方法（getSessions, createSession, updateSession, deleteSession）
- [ ] 3.3 添加 Messages 端点方法（getMessages, createMessage, updateMessage, deleteMessage）
- [ ] 3.4 添加 Memories 端点方法（getMemories, createMemory, updateMemory, deleteMemory, searchMemories）
- [ ] 3.5 添加 People 端点方法（getPeople, createPerson, updatePerson, deletePerson）
- [ ] 3.6 添加 Soul 端点方法（getSoulState, updateSoulState, getSoulLogs, createSoulLog）
- [ ] 3.7 添加 Reminders 端点方法（getReminders, createReminder, updateReminder, deleteReminder）

## 4. SyncManager 实现

- [ ] 4.1 创建 `SyncManager` 类，注入 NextApiService + 所有 DAO
- [ ] 4.2 实现 `fullSync()`：按顺序拉取全部数据写入 Room（soul → sessions+messages → memories → people → reminders）
- [ ] 4.3 实现 `pushUnsynced()`：查询所有 `remoteId = null` 的本地记录推送到后端
- [ ] 4.4 实现 `clearAll()`：清空所有 Room 表（登出用）
- [ ] 4.5 在 Koin AppModule 注册 SyncManager

## 5. Repository 层重构

- [ ] 5.1 ChatRepositoryImpl：createSession 改为 write-through（先后端 → 再本地），失败时 fallback 本地
- [ ] 5.2 ChatRepositoryImpl：saveMessage 改为 write-through
- [ ] 5.3 ChatRepositoryImpl：deleteSession / deleteMessage 改为 write-through
- [ ] 5.4 ChatRepositoryImpl：updateSessionTitle / updateMessageFeedback 改为 write-through
- [ ] 5.5 MemoryRepositoryImpl：saveMemory / updateMemory / deleteMemory 改为 write-through
- [ ] 5.6 MemoryRepositoryImpl：savePerson / updatePerson / deletePerson 改为 write-through
- [ ] 5.7 SoulRepositoryImpl：adjustParameter / upsert 改为 write-through
- [ ] 5.8 SoulRepositoryImpl：insertLog 改为 write-through
- [ ] 5.9 ReminderDao 调用处改为 write-through（找到 set_reminder tool 中的直接 DAO 调用）

## 6. 登录/登出流程集成

- [ ] 6.1 登录成功后调用 `SyncManager.fullSync()`
- [ ] 6.2 登出时调用 `SyncManager.clearAll()` 清空本地数据
- [ ] 6.3 SettingsScreen 登出按钮添加确认对话框（提示本地数据将清空）
- [ ] 6.4 登录后同步期间显示进度提示

## 7. 测试验证

- [ ] 7.1 手动测试：登录 → 发送聊天 → 检查后端是否有 session 和 message
- [ ] 7.2 手动测试：保存记忆 → 检查后端是否有 memory
- [ ] 7.3 手动测试：登出 → 重新登录 → 验证数据完整恢复
- [ ] 7.4 手动测试：断网发消息 → 恢复网络 → 验证数据补同步
- [ ] 7.5 编译验证：`./gradlew assembleDebug` 通过
