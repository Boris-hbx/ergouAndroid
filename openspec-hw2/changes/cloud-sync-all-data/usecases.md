## Use Cases

### Use Case: Sync data on new device login

**Primary Actor:** 用户 (Boris)
**Scope:** 二狗 Android App
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 换手机后所有聊天记录、记忆、人格状态完整恢复

**Preconditions:**
- 用户已有 Next 账号且之前使用过二狗（后端已有数据）
- 新设备已安装二狗 App

**Success Guarantee (Postconditions):**
- 本地 Room 缓存包含后端全部数据
- App 状态与旧设备一致（会话列表、记忆、灵魂状态）

**Trigger:** 用户在新设备上登录 Next 账号

**Main Success Scenario:**
1. 用户输入账号密码登录
2. 系统验证登录成功，获取 session cookie
3. 系统启动全量同步，拉取所有 sessions 和 messages
4. 系统拉取所有 memories 和 people
5. 系统拉取 soul state 和 evolution logs
6. 系统拉取所有 reminders
7. 系统将数据写入本地 Room 缓存
8. 用户看到完整的会话列表和历史消息

**Extensions:**
- 3a. 网络中断：系统提示同步失败，允许用户重试，已拉取的数据保留
- 3b. 后端无数据（全新用户）：跳过同步，初始化默认灵魂状态

---

### Use Case: Send chat message with cloud persistence

**Primary Actor:** 用户
**Scope:** 二狗 Android App
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 聊天记录不会因换手机丢失

**Preconditions:**
- 用户已登录 Next 账号
- 存在活跃聊天会话

**Success Guarantee (Postconditions):**
- 消息存储在后端和本地缓存
- 对话在其他设备登录后可恢复

**Trigger:** 用户发送一条消息

**Main Success Scenario:**
1. 用户输入消息并发送
2. 系统将用户消息保存到后端
3. 系统同时写入本地 Room 缓存
4. 系统调用 LLM 获取回复（流式）
5. 系统将 AI 回复保存到后端
6. 系统同时写入本地 Room 缓存
7. 用户看到完整对话

**Extensions:**
- 2a. 后端写入失败：系统先写本地缓存保证用户体验，后台重试同步到后端
- 4a. LLM 调用失败：消息已保存，显示错误提示

---

### Use Case: Save memory to cloud

**Primary Actor:** 系统（MemoryExtractor 自动触发）/ 用户（手动保存）
**Scope:** 二狗 Android App
**Level:** Subfunction

**Stakeholders and Interests:**
- 用户 — 记忆系统跨设备可用

**Preconditions:**
- 用户已登录

**Success Guarantee (Postconditions):**
- Memory/Person 数据存在于后端和本地缓存

**Trigger:** 对话结束后 MemoryExtractor 提取记忆，或用户通过指令手动保存

**Main Success Scenario:**
1. 系统生成记忆内容（category, content, importance 等）
2. 系统调用后端 API 创建 memory
3. 系统将返回的 memory（含 remote ID）写入本地缓存
4. 记忆可在 recall 和 context building 中使用

**Extensions:**
- 2a. 后端不可用：写入本地缓存，标记为未同步，后续重试

---

### Use Case: Evolve soul personality with cloud sync

**Primary Actor:** 系统（SoulEvolver 自动触发）
**Scope:** 二狗 Android App
**Level:** Subfunction

**Stakeholders and Interests:**
- 用户 — 二狗人格在新设备上延续

**Preconditions:**
- 用户已登录
- 对话结束触发 soul evolution

**Success Guarantee (Postconditions):**
- Soul state 和 evolution log 同步到后端

**Trigger:** 对话结束后 SoulEvolver 分析并调整参数

**Main Success Scenario:**
1. SoulEvolver 分析对话，决定参数调整
2. 系统将新的 soul state 写入后端
3. 系统将 evolution log 追加到后端
4. 系统更新本地缓存

**Extensions:**
- 2a. 后端不可用：本地先更新，后续同步

---

### Use Case: Manage reminders with cloud sync

**Primary Actor:** 用户（通过对话指令）
**Scope:** 二狗 Android App
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 提醒在新设备上自动恢复

**Preconditions:**
- 用户已登录

**Success Guarantee (Postconditions):**
- Reminder 存储在后端和本地
- 新设备登录后未完成提醒自动恢复 AlarmManager

**Trigger:** 用户要求设置/完成/删除提醒

**Main Success Scenario:**
1. 用户通过对话设置提醒
2. 系统调用后端 API 创建 reminder
3. 系统写入本地缓存并注册 AlarmManager
4. 提醒到期时本地触发通知
5. 用户完成提醒，系统同步完成状态到后端

**Extensions:**
- 2a. 后端不可用：本地创建 + AlarmManager，标记未同步
- 新设备登录时：同步拉取未完成 reminders，重新注册 AlarmManager
