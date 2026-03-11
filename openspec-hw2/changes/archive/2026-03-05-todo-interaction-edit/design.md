## Context

Todo 页面已有完整的 CRUD 功能，通过 NextApiService 与后端交互。当前交互方式：
- 删除：右侧 IconButton + AlertDialog 确认
- 编辑：不支持（只有添加对话框）
- 排序：无（按后端默认顺序）
- 标签：AssistChip 展示，不可交互
- 快捷栏：静态标签，无动态计数

现有代码结构：
- `TaskScreen.kt`：UI 层（~600 行），含 TodoItem、AddTaskDialog、QuadrantSelector
- `TaskViewModel.kt`：状态管理（~175 行），TaskUiState + CRUD 操作
- `NextApiService.kt`：已有 getTodos, createTodo, updateTodo, deleteTodo, restoreTodo
- `ShortcutBarRepository.kt`：DataStore 管理快捷栏配置，无 API 调用

## Goals / Non-Goals

**Goals:**
- 左滑删除替代按钮删除，支持 Snackbar 撤销
- 长按编辑已有任务，复用添加对话框
- 拖拽排序任务，持久化 sort_order
- 标签点击筛选任务列表
- 快捷栏显示动态待办数

**Non-Goals:**
- 多选标签筛选（保持单选简单）
- 拖拽跨 Tab 移动任务
- 已完成任务的拖拽排序
- 离线缓存/乐观更新（保持现有的请求后刷新模式）

## Decisions

### D1: SwipeToDismiss 实现方案

**选择**: 使用 Material 3 的 `SwipeToDismissBox`（`SwipeToDismissBoxState`）

**替代方案**: 自定义 `Modifier.draggable` 手势 — 需要自行处理动画和阈值，工作量大。

**理由**: M3 原生组件，API 稳定，自动处理手势阈值和回弹动画。仅支持 EndToStart 方向（左滑），阈值用默认的 positionalThreshold（约 56dp）。

**实现要点**:
- 删除成功后先乐观移除列表项，Snackbar 显示"撤销"按钮
- 撤销调用 `nextApiService.restoreTodo(id)` 然后 `refreshTodos()`
- 失败时 `SwipeToDismissBoxState.reset()` 回弹

### D2: 长按编辑 vs 拖拽的手势区分

**选择**: 使用 `combinedClickable(onLongClick = ...)` 触发编辑，不实现拖拽排序

**替代方案 A**: 同时实现长按编辑和拖拽排序，用"长按不动=编辑，长按+移动=拖拽"区分。

**替代方案 B**: 添加专门的拖拽手柄图标（如 ≡）。

**理由**: 手势冲突问题复杂。长按不动 vs 长按+微移动的区分在触屏上体验差，用户容易误触。拖拽排序需要引入第三方库（如 `sh.calvin.reorderable`）增加依赖。考虑到：
1. 后端已支持 sort_order 字段，未来随时可加
2. 拖拽在设计文档中标注为"优先级低"
3. 先上长按编辑，拖拽作为后续独立 change

**结论**: 本次 change 去掉 `todo-drag-reorder` capability，集中做好左滑删除 + 长按编辑 + 标签筛选 + 快捷栏计数。

### D3: AddTaskDialog 复用为编辑对话框

**选择**: 给 AddTaskDialog 增加编辑模式参数

**新签名**:
```kotlin
@Composable
fun TaskDialog(
    mode: TaskDialogMode,  // sealed: Add / Edit(todo: NextTodo)
    allTags: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (text, content, tags, dueDate, quadrant) -> Unit
)

sealed class TaskDialogMode {
    object Add : TaskDialogMode()
    data class Edit(val todo: NextTodo) : TaskDialogMode()
}
```

**理由**: 添加和编辑共享完全相同的字段集。用 sealed class 区分模式，编辑模式预填字段值、改标题为"编辑任务"、改按钮为"保存"。

### D4: 标签筛选的状态管理

**选择**: ViewModel 维护 `filterTag: String?`，UI 层本地过滤

**实现**:
- TaskUiState 新增 `filterTag: String?`
- 用 `derivedStateOf` 或在 ViewModel 中计算 `filteredPendingTodos`
- 筛选在已加载的列表上执行，不额外调 API
- 切换 Tab 时自动清除 `filterTag`

**理由**: 任务数据已经全量加载（每个 Tab 通常 < 50 条），本地过滤效率高且无网络延迟。

### D5: 快捷栏待办计数

**选择**: ShortcutBarRepository 注入 NextApiService，新增 `getTodoCount()` 挂起函数

**API**: GET `/api/todos/counts?tab=today` — 返回各象限的计数

**NextApiService 改动**: 新增 `getTodoCounts(tab: String)` 方法，返回计数数据

**ShortcutItem 改动**: 新增 `badge: String?` 字段，显示如 "5 项"

**刷新时机**: 聊天页面 onResume 时调用一次（不做定时轮询，避免频繁网络请求）

## Risks / Trade-offs

**[SwipeToDismiss 误触删除]** → Snackbar 5 秒撤销窗口 + 后端软删除（随时可恢复）。风险可控。

**[编辑对话框字段过多]** → 复用现有的"更多选项"折叠设计，编辑模式下如果有高级字段值则默认展开。

**[标签筛选丢失上下文]** → 筛选后摘要卡片仍显示全量统计（不随筛选变化），避免用户困惑。

**[快捷栏计数网络失败]** → 降级显示无数字的"待办"标签，不影响核心功能。

**[去掉拖拽排序]** → 用户手动排序需求暂时不满足。可作为后续独立 change 添加，后端 sort_order 字段已就绪。

## Open Questions

- `/api/todos/counts` 的响应格式需确认（参考文档有这个 endpoint，但需验证返回结构）
- 编辑模式下是否允许修改 tab（today/week/month）？当前 AddTaskDialog 不包含 tab 选择
