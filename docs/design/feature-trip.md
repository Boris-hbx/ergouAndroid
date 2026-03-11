# 差旅（Trip）功能设计文档

> 遵循 [功能页面通用设计规范](feature-screen-conventions.md)

## 现状

### UI
- Scaffold + TopAppBar（动态标题：列表="差旅" / 详情=行程名）+ FAB（上下文切换：添加行程/添加费用）
- 两级导航：行程列表 ↔ 行程详情
- 列表视图：Card 展示标题、目的地、日期范围、总金额
- 详情视图：头部卡片（目的地/日期/目的/总费用）+ 费用明细列表
- 费用明细：类型标签（交通/住宿/餐饮/会议/其他）+ 描述 + 日期 + 金额 + 报销状态
- 报销状态颜色：已报销=绿色，待报销=tertiary，已拒绝=红色
- 添加行程对话框：标题（必填）+ 目的地/起止日期/目的（可选）
- 添加费用对话框：类型选择器 + 描述（必填）+ 金额（必填）+ 日期（可选）
- 登录门控 + 空状态 + 错误 Snackbar

### 工具
- `create_trip` — 创建行程（title/destination/date_from/date_to/purpose）
- `add_trip_item` — 添加费用项（trip_id/type/description/amount/date）
- `query_trips` — 查询所有行程

### API
- NextApiService: getTrips, createTrip, getTripById, updateTrip, deleteTrip, createTripItem, updateTripItem, deleteTripItem

---

## 改进计划

### UI 改进

#### 1. 行程卡片优化
- 添加行程状态标签：进行中 / 已结束 / 计划中（根据日期自动判断）
- 显示费用项数量："5 笔费用"
- 进行中的行程高亮显示（置顶 + 边框强调）

#### 2. 详情页增强
- 费用按日期分组显示（每天一个分组头，显示当日小计）
- 费用类型统计：顶部横向统计条（交通 ¥500 / 住宿 ¥1200 / 餐饮 ¥300）
- 报销进度：已报销 ¥X / 总计 ¥Y（进度条）

#### 3. 费用明细操作
- 左滑删除费用项
- 点击费用项弹出编辑对话框
- 报销状态切换（长按或下拉菜单）

#### 4. 添加对话框优化
- 日期使用 DatePickerDialog
- 金额使用数字键盘
- 常用费用类型快速选择（一行 FilterChip）

#### 5. 行程时间线
- 详情页可切换为时间线视图（按日期纵向排列事件）
- 优先级低，可后续迭代

### 工具改进

#### 6. query_trips 增强
- 新增参数：`status`（ongoing/completed/planned）
- 返回值增加费用项数量和报销统计

#### 7. 新增工具：`trip_summary`
- 参数：trip_id（必填）
- 返回：行程详情 + 各类型费用汇总 + 报销状态统计
- 用途：二狗回答"上次出差花了多少"

#### 8. 新增工具：`update_trip_item`
- 参数：trip_id, item_id, reimburse_status
- 用途：二狗帮用户更新报销状态

### 集成

#### 9. 快捷栏推送
- 有进行中行程时显示："出差中·目的地"
- 无行程时显示默认文案

#### 10. 通知预留
- `getPendingReminders()`: 返回待报销的费用项
- 行程结束后提醒提交报销

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/trip/TripScreen.kt` | 修改 | 状态标签、分组显示、费用统计 |
| `ui/trip/TripViewModel.kt` | 修改 | 状态判断、分组逻辑、统计数据 |
| `data/tool/tools/QueryTripsTool.kt` | 修改 | 增加 status 筛选 |
| `data/tool/tools/TripSummaryTool.kt` | 新增 | 行程汇总工具 |
| `data/tool/tools/UpdateTripItemTool.kt` | 新增 | 更新费用项工具 |
| `di/AppModule.kt` | 修改 | 注册新 Tool |
| `data/remote/ErgouPrompt.kt` | 修改 | 更新工具使用说明 |
