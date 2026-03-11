# 任务（Task）功能设计文档

> 遵循 [功能页面通用设计规范](feature-screen-conventions.md)

## 现状

### UI
- Scaffold + TopAppBar("待办事项") + FAB
- TabRow 三个时间筛选：今天/本周/30天，显示待办数量
- LazyColumn 展示待办卡片：勾选框 + 标题 + 到期日 + 进度条 + 标签(FlowRow) + 删除
- 已完成任务可折叠显示（完成后标题加删除线）
- 添加对话框：标题（必填）+ 描述/标签/到期日（可选）
- 登录门控 + 空状态 + 错误 Snackbar

### 工具
- `add_task` — 添加任务（支持 quadrant/tab/content/tags/due_date）
- `list_tasks` — 列表查询（支持 tab 筛选）
- `complete_task` — 完成任务

### API
- NextApiService: getTodos, createTodo, getTodoById, updateTodo, deleteTodo, restoreTodo

---

## 改进计划

### UI 改进

#### 1. 任务卡片优化
- 紧急/重要四象限标记：用颜色小圆点区分（红=紧急重要，橙=重要不紧急，蓝=紧急不重要，灰=不紧急不重要）
- 到期日临近高亮：今天到期红色，明天到期橙色
- 标签样式统一为 `SuggestionChip`，可点击筛选

#### 2. 交互增强
- 左滑删除（`SwipeToDismiss`）替代右侧删除按钮
- 长按任务弹出编辑对话框（复用添加对话框）
- 拖拽排序（可选，优先级低）

#### 3. 统计摘要
- 页面顶部摘要卡片：今日待办 N 项 / 已完成 M 项 / 逾期 K 项
- 进度环形图展示完成率

#### 4. 添加对话框优化
- 四象限选择器：2×2 网格按钮
- 到期日使用 `DatePickerDialog` 替代手动输入
- 标签支持自动补全（基于历史标签）

### 工具改进

#### 5. 新增工具：`update_task`
- 参数：task_id（必填），text/content/tags/due_date/quadrant（可选）
- 用途：二狗帮用户修改任务内容

#### 6. 新增工具：`delete_task`
- 参数：task_id（必填）
- 用途：二狗帮用户删除任务（需谨慎，tool 描述中强调需用户确认）

#### 7. list_tasks 增强
- 新增参数：`include_completed`（布尔，默认 false）
- 新增参数：`quadrant`（按四象限筛选）
- 返回值增加到期日和进度信息

### 集成

#### 8. 快捷栏推送
- 快捷栏显示："待办 N 项"（仅今日未完成数）
- 点击快捷栏直接进入任务页面

#### 9. 通知预留
- `getPendingReminders()`: 返回今日到期但未完成的任务
- 二狗可据此主动提醒："你今天还有 3 个待办没完成哦"

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/task/TaskScreen.kt` | 修改 | UI 改进 |
| `ui/task/TaskViewModel.kt` | 修改 | 新增摘要数据、编辑功能 |
| `data/tool/tools/UpdateTaskTool.kt` | 新增 | 修改任务工具 |
| `data/tool/tools/DeleteTaskTool.kt` | 新增 | 删除任务工具 |
| `data/tool/tools/ListTasksTool.kt` | 修改 | 增加筛选参数 |
| `di/AppModule.kt` | 修改 | 注册新 Tool |
| `data/remote/ErgouPrompt.kt` | 修改 | 更新工具使用说明 |
