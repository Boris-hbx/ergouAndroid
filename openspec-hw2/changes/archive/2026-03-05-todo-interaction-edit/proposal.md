## Why

Todo 页面的核心 CRUD 和展示已完成，但交互体验仍停留在"能用"阶段。缺少左滑删除、长按编辑、拖拽排序等现代 Android 列表交互，用户只能通过删除按钮和添加对话框操作任务，无法直接编辑已有任务。这些交互增强是提升日常使用效率的关键一步。

## What Changes

- 左滑删除：用 `SwipeToDismiss` 替代右侧删除 IconButton，滑动露出红色删除背景
- 长按编辑：长按任务卡片弹出编辑对话框，复用添加对话框组件，支持修改标题/描述/标签/到期日/象限
- 拖拽排序：支持长按拖拽调整任务顺序，利用后端 `sort_order` 浮点数字段（取前后中间值）
- 标签点击筛选：将 `AssistChip` 改为 `SuggestionChip`，点击标签按该标签过滤任务列表
- 快捷栏动态待办数：ShortcutBarRepository 从 Next API 拉取实时未完成任务数量，显示"待办 N 项"

## Capabilities

### New Capabilities
- `todo-swipe-delete`: 左滑删除交互，SwipeToDismiss + 确认动画
- `todo-edit-dialog`: 长按编辑功能，复用添加对话框，调用 PUT /api/todos/:id 更新
- `todo-drag-reorder`: 拖拽排序，更新 sort_order 字段（浮点数中间值算法）
- `todo-tag-filter`: 标签点击筛选，SuggestionChip + ViewModel 筛选状态
- `todo-shortcut-count`: 快捷栏动态待办数，从 /api/todos/counts 获取实时数据

### Modified Capabilities

## Impact

- **UI 层**：`TaskScreen.kt` 大幅修改（卡片交互、对话框复用、筛选逻辑）
- **ViewModel 层**：`TaskViewModel.kt` 新增编辑状态、筛选状态、排序更新逻辑
- **Repository 层**：`ShortcutBarRepository.kt` 接入 NextApiService 获取待办计数
- **API 层**：`NextApiService.kt` 可能需新增 `getTodoCounts()` 和 `batchUpdateTodos()` 方法
- **依赖**：可能需要添加 `reorderable` 拖拽库（或用 Compose Foundation 的 `draggable`）
