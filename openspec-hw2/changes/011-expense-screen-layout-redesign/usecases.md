## Use Cases

### Use Case: 按时间粒度浏览账单

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 快速查看不同时间维度的消费情况

**Preconditions:**
- 用户已进入记账页面
- 已有至少一条账单记录

**Success Guarantee (Postconditions):**
- 页面显示所选时间粒度（日/周/月）对应时间窗内的账单列表、总额和分组

**Trigger:** 用户切换时间粒度或导航时间窗

**Main Success Scenario:**
1. 用户进入记账页面，默认按"月"粒度显示当月数据
2. 系统显示当月总支出和按日期分组的账单列表
3. 用户切换粒度为"周"
4. 系统切换为当前周的时间窗，刷新总额和列表
5. 用户点击 ◀ 箭头切换到上一周
6. 系统显示上一周的总额和账单列表

**Extensions:**
- 1a. 当前时间窗内无账单：系统显示空状态提示
- 3a. 切换粒度后，时间窗重置为包含"今天"的对应时段（本日/本周/本月）

---

### Use Case: 按分类筛选账单

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 查看某一分类的消费明细和总额

**Preconditions:**
- 用户已进入记账页面
- 当前时间窗内有账单数据

**Success Guarantee (Postconditions):**
- 列表仅显示所选分类的账单，总额为该分类在当前时间窗内的合计

**Trigger:** 用户点击分类筛选 chip

**Main Success Scenario:**
1. 用户看到分类筛选 chips 横向排列，默认选中"全部"
2. 用户点击"超市" chip
3. 系统过滤列表仅显示"超市"类别的账单
4. 系统更新总支出为当前时间窗内"超市"类别的合计金额
5. 用户点击"全部" chip
6. 系统恢复显示全部账单和总额

**Extensions:**
- 2a. 所选分类在当前时间窗内无账单：列表为空，总额显示 0
- 1a. 分类 chips 从当前时间窗内的账单动态提取，无账单则仅显示"全部"

---

### Use Case: 查看日期分组的账单明细

**Primary Actor:** 用户
**Scope:** 二狗记账模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 按日期快速定位和浏览消费记录

**Preconditions:**
- 当前时间窗+分类筛选下有账单数据

**Success Guarantee (Postconditions):**
- 账单按日期倒序分组显示，每组有日期标题和小计金额

**Trigger:** 页面加载或筛选条件变化

**Main Success Scenario:**
1. 系统将账单按日期倒序分组
2. 每组显示日期标题（今天/昨天/具体日期）和该日小计金额
3. 组内每条账单显示：商户名、分类标签、收据图标（如有）、金额
4. 用户上下滚动浏览各日期分组
5. 用户点击某条账单，进入编辑详情

**Extensions:**
- 3a. 商户名过长：单行显示，尾部省略号截断
- 3b. 无收据图片：不显示收据图标
- 5a. 用户左滑某条账单：触发删除确认（现有 SwipeToDismiss 行为，不变）

**Design Decision:**
- 分类 chips 按当前时间窗内出现频率降序排列，"全部"固定在最前
