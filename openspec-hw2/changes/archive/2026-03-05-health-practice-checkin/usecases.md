## Use Cases

### Use Case: 标记今日练习打卡

**Primary Actor:** 用户
**Scope:** 二狗健康模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 记录自己练了哪些功法，获得坚持练习的成就感

**Preconditions:**
- 用户已进入健康页面，功法列表已加载

**Success Guarantee (Postconditions):**
- 练习记录已持久化到 DataStore
- 统计数据（今日进度、连续天数、累计次数）已更新
- UI 反映最新打卡状态

**Trigger:** 用户点击功法卡片上的打卡按钮

**Main Success Scenario:**
1. 用户浏览功法列表，找到今日练习的功法
2. 用户点击该功法卡片上的打卡按钮
3. 系统将该功法标记为今日已练，按钮变为绿色实心 ✓
4. 系统更新顶部统计条（今日进度 +1，连续天数和累计次数相应更新）
5. 系统将练习记录持久化到 DataStore

**Extensions:**
- 3a. 该功法今日已打卡：系统取消打卡标记，按钮恢复为空心圆形，统计数据相应减少
- 5a. DataStore 写入失败：系统记录错误日志，UI 状态保持内存中的值（下次启动时丢失）

---

### Use Case: 查看练习统计

**Primary Actor:** 用户
**Scope:** 二狗健康模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 了解自己的练习情况，保持坚持的动力

**Preconditions:**
- 用户已进入健康页面

**Success Guarantee (Postconditions):**
- 统计数据正确显示（连续天数、今日进度、累计次数）

**Trigger:** 用户打开健康页面

**Main Success Scenario:**
1. 用户进入健康页面
2. 系统从 DataStore 加载练习记录
3. 系统计算连续天数（从今天往回逐日检查）
4. 系统计算今日已练数量和累计练习次数
5. 顶部统计条显示：🔥 连续 X 天 | 今日 Y/N | 累计 Z 次

**Extensions:**
- 2a. 无历史练习记录：统计条显示连续 0 天、今日 0/N、累计 0 次
- 2b. DataStore 读取失败：使用空记录作为降级，记录错误日志
- 3a. 练习记录超过 90 天：系统自动清理过期数据，只保留最近 90 天

**Open Questions:**
- 无
