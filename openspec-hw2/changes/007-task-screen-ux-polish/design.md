## Context

TaskScreen.kt 当前左滑直接删除无确认，单击无响应。需要增加删除确认和详情 BottomSheet。约束：只改 TaskScreen.kt，不改 ViewModel 或其他文件。

## Goals / Non-Goals

**Goals:**
- 左滑删除增加确认步骤
- 单击弹出详情 BottomSheet 并支持内联编辑

**Non-Goals:**
- 修改 TaskViewModel、AppModule、NextApiService
- Markdown 渲染（描述用纯文本展示和编辑）

## Decisions

### D1: SwipeToDismiss 确认流程
- confirmValueChange 返回 false，用 state 记录 `pendingDeleteTodo: NextTodo?`
- AlertDialog 由 `pendingDeleteTodo` 驱动
- 确认后手动调用 onDelete lambda，取消时调用 `LaunchedEffect` 触发 `dismissState.reset()`
- 需要用 `key(todo.id)` 包裹 SwipeToDismissBox 以便重置状态

### D2: BottomSheet 实现
- 使用 M3 `ModalBottomSheet` + `rememberModalBottomSheetState`
- TaskScreen 顶层维护 `detailTodo: NextTodo?` state
- TodoItem 的 `onClick` 设置 `detailTodo`
- BottomSheet 内部维护编辑态：`isEditingTitle`, `isEditingContent`, `editTitle`, `editContent` 等
- 失焦时调用 `viewModel.updateTodo()` 保存（用已有 API，仅传被修改的字段）
- 标签编辑：本地维护 `editTags: List<String>`，变化时调用 updateTodo
- 不使用 TextFieldValue（中文 IME 问题），用 String state + OutlinedTextField

### D3: 状态管理
- `pendingDeleteTodo`：SwipeTodoItem 内部 state
- `detailTodo`：TaskScreen 顶层 state
- BottomSheet 内编辑态都是局部 remember state

## Risks / Trade-offs

**[SwipeToDismissBox reset]** → 用 `key(todo.id)` 和 LaunchedEffect reset，M3 API 可能需要 `snapTo(Default)`
**[BottomSheet 编辑频繁调 API]** → 只在失焦或关闭时保存，不逐字符调用
