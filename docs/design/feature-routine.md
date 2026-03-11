# 例行功能设计文档

> 遵循 [功能页面通用设计规范](feature-screen-conventions.md)

## 设计理念

"例行"是按频率重复的事项管理，统一使用 Next Review API。支持四种频率：每日、每周、每月、每年。不包含打卡、热力图、连续天数等游戏化功能。

## 数据源

全部使用 Review API (`/api/reviews`)，不再使用 Routine API (`/api/routines`)。

### API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/reviews` | 列表（含 due_status 计算字段） |
| POST | `/api/reviews` | 创建 `{ text, frequency, category? }` |
| PUT | `/api/reviews/:id` | 更新（部分字段） |
| DELETE | `/api/reviews/:id` | 删除（硬删） |
| POST | `/api/reviews/:id/complete` | 标记完成 |
| POST | `/api/reviews/:id/uncomplete` | 取消完成 |

### 频率

- `daily` — 每日
- `weekly` — 每周
- `monthly` — 每月
- `yearly` — 每年

（`quarterly` 不在 UI 中提供添加选项，但如已有数据会在"月" tab 下显示）

## UI

### 列表页

- Scaffold + TopAppBar("例行") + FAB
- TabRow 四个 tab：日 / 周 / 月 / 年
- 每 tab 下 LazyColumn 展示例行项卡片
- 卡片显示：名称 + 频率标签 + 到期状态（颜色编码）+ 分类标签（如有）+ 暂停标记
- 列表按到期紧急度排序：overdue > due_today > due_soon > upcoming > completed > paused
- 登录门控 + 空状态 + 错误 Snackbar

### 详情页

- 所有频率共用一个详情视图
- 显示：名称、频率、分类、到期状态、上次完成时间、创建时间
- 操作按钮：标记完成（未暂停时）、暂停/恢复、删除（带确认）

### 添加对话框

- 统一一个对话框，包含：
  - 名称输入框（必填）
  - 频率选择 FilterChip：每日 / 每周 / 每月 / 每年（默认跟随当前 tab）
  - 分类输入框（可选）

## 工具

- `add_review` — 创建例行项（支持 daily/weekly/monthly/yearly，默认 daily）
- `query_reviews` — 查询例行项列表（支持按 frequency 筛选）

## 快捷栏

- label: "例行"
- category: "例行"
- route: "routine"

## 文件清单

| 文件 | 说明 |
|------|------|
| `ui/routine/RoutineScreen.kt` | 列表页 + 详情页 + 添加对话框 |
| `ui/routine/RoutineViewModel.kt` | 状态管理，全部走 Review API |
| `data/tool/tools/AddReviewTool.kt` | 创建例行项工具 |
| `data/tool/tools/QueryReviewsTool.kt` | 查询例行项工具 |
