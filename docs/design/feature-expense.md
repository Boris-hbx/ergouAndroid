# 记账（Expense）功能设计文档

> 遵循 [功能页面通用设计规范](feature-screen-conventions.md)

## 现状

### UI
- Scaffold + TopAppBar("记账") + FAB
- 顶部摘要卡片：本月总金额 + 条目数
- FlowRow FilterChip 按标签筛选（全部 + 动态标签）
- LazyColumn 展示记账卡片：备注 + 金额（红色）+ 货币 + 标签(#tag) + 日期
- 添加对话框：金额（必填）+ 备注 + 标签（逗号分隔）+ 货币选择（CAD/CNY/USD）
- 登录门控 + 空状态 + 错误 Snackbar

### 工具
- `add_expense` — 记账（支持 amount/notes/tags/currency/date）
- `expense_summary` — 统计汇总（支持 period: today/week/month）

### API
- NextApiService: getExpenses, createExpense, getExpenseById, updateExpense, deleteExpense, getExpenseSummary, getExpenseTags

---

## 改进计划

### UI 改进

#### 1. 摘要卡片增强
- 显示本月预算进度（如设有预算）
- 与上月同期对比："+15%" 或 "-8%"
- 按标签分类的饼图/条形图（简易实现，无需第三方图表库）

#### 2. 时间筛选
- 添加时间范围筛选：今日 / 本周 / 本月 / 自定义日期范围
- 顶部 TabRow 或下拉选择器

#### 3. 记账卡片优化
- 左侧显示标签对应的 emoji 图标（餐饮🍜、交通🚗、购物🛍️ 等）
- 金额根据货币格式化：¥1,234.56 / $56.78 / CA$12.34
- 按日期分组显示，日期头显示当日小计

#### 4. 添加对话框优化
- 标签改为可选芯片（基于历史标签），支持新增
- 金额输入键盘类型优化（数字键盘）
- 日期默认今天，使用 DatePickerDialog
- 收入/支出切换（预留，当前都按支出处理）

#### 5. 左滑删除
- `SwipeToDismiss` 替代卡片中的删除按钮

### 工具改进

#### 6. expense_summary 增强
- 新增参数：`tag`（按标签汇总）
- 返回值增加与上期对比数据
- 用途：二狗可以说 "这个月餐饮花了 ¥2,000，比上个月多了 30%"

#### 7. 新增工具：`delete_expense`
- 参数：expense_id（必填）
- 用途：二狗帮用户删除误记的账目

#### 8. 新增工具：`query_expenses`
- 参数：period（today/week/month），tag（可选），limit（可选，默认 10）
- 返回最近的记账条目列表
- 用途：二狗回答"我最近花了什么"

### 集成

#### 9. 快捷栏推送
- 快捷栏显示："本月 ¥X,XXX"（本月总支出）

#### 10. 通知预留
- `getPendingReminders()`: 返回本月超预算提醒（如设有预算）
- 二狗可主动分析："这个月已经花了 5,000 了，还有半个月，注意控制"

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/expense/ExpenseScreen.kt` | 修改 | 时间筛选、卡片优化、分组显示 |
| `ui/expense/ExpenseViewModel.kt` | 修改 | 时间筛选逻辑、分组数据 |
| `data/tool/tools/ExpenseSummaryTool.kt` | 修改 | 增加 tag 参数和对比数据 |
| `data/tool/tools/DeleteExpenseTool.kt` | 新增 | 删除记账工具 |
| `data/tool/tools/QueryExpensesTool.kt` | 新增 | 查询记账列表工具 |
| `di/AppModule.kt` | 修改 | 注册新 Tool |
| `data/remote/ErgouPrompt.kt` | 修改 | 更新工具使用说明 |
