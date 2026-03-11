## 1. 对话框重构（编辑模式基础）

- [x] 1.1 创建 `TaskDialogMode` sealed class（Add / Edit(todo: NextTodo)），放在 TaskScreen.kt 中
- [x] 1.2 将 `AddTaskDialog` 重命名为 `TaskDialog`，增加 `mode: TaskDialogMode` 参数
- [x] 1.3 编辑模式：用 todo 字段预填标题、描述、标签、到期日、象限；标题改"编辑任务"，按钮改"保存"
- [x] 1.4 编辑模式：有高级字段值时（描述/标签/到期日/象限非空）默认展开"更多选项"
- [x] 1.5 编辑模式提交时仅发送被修改的字段（对比原值，未改的不传）

## 2. 长按编辑交互

- [x] 2.1 TodoItem 的 Card 添加 `combinedClickable(onLongClick = ...)` 手势
- [x] 2.2 TaskViewModel 新增 `editingTodo: NextTodo?` 状态字段
- [x] 2.3 TaskScreen 监听 `editingTodo`，非空时弹出 `TaskDialog(mode = Edit(todo))`
- [x] 2.4 TaskDialog 编辑模式提交时调用 `viewModel.updateTodo()`，成功后清除 editingTodo 并刷新列表
- [x] 2.5 编辑失败时对话框保持打开，显示错误 Snackbar

## 3. 左滑删除

- [x] 3.1 TodoItem 外层包裹 `SwipeToDismissBox`，方向设为 EndToStart
- [x] 3.2 实现红色背景 + 删除图标的 `background` 内容
- [x] 3.3 滑动确认后调用 `viewModel.deleteTodo(id)`，从列表乐观移除
- [x] 3.4 删除成功后显示 Snackbar（message="已删除"，action="撤销"）
- [x] 3.5 撤销操作：TaskViewModel 新增 `restoreTodo(id)` 方法，调用 `nextApiService.restoreTodo(id)` 后刷新
- [x] 3.6 删除失败时调用 `SwipeToDismissBoxState.reset()` 回弹卡片
- [x] 3.7 移除旧的删除 IconButton 和 AlertDialog 确认逻辑

## 4. 标签点击筛选

- [x] 4.1 TaskUiState 新增 `filterTag: String?` 字段
- [x] 4.2 TaskViewModel 新增 `setFilterTag(tag: String?)` 方法，点击已选中标签时传 null 取消
- [x] 4.3 TaskViewModel 在 `switchTab()` 中清除 filterTag
- [x] 4.4 TaskViewModel 新增 `filteredPendingTodos` / `filteredCompletedTodos` 计算属性，基于 filterTag 过滤
- [x] 4.5 TodoItem 中将 `AssistChip` 改为 `SuggestionChip`，onClick 调用 `viewModel.setFilterTag(tag)`
- [x] 4.6 选中的标签显示高亮样式（SuggestionChip 的 selected 参数）
- [x] 4.7 筛选后无结果时显示空状态"该标签下没有任务"

## 5. 快捷栏动态待办数

- [x] 5.1 NextApiService 新增 `getTodoCounts(tab: String): Result<TodoCountsResponse>` 方法
- [x] 5.2 创建 `NextTodoCountsResponse` DTO 类（解析 /api/todos/counts 响应）
- [x] 5.3 ShortcutItem 新增 `badge: String?` 字段
- [x] 5.4 ShortcutBarRepository 构造函数注入 NextApiService 和 NextAuthProvider
- [x] 5.5 ShortcutBarRepository 新增 `refreshTodoBadge()` 挂起函数，调用 API 后更新 badge
- [x] 5.6 Koin AppModule 更新 ShortcutBarRepository 注入（新增 NextApiService、NextAuthProvider 参数）
- [x] 5.7 聊天页面 onResume 时调用 `refreshTodoBadge()`
- [x] 5.8 网络失败或未登录时 badge 为 null（显示无数字的"待办"）

## 6. 测试验证

- [ ] 6.1 手动测试：左滑删除 → Snackbar 撤销 → 任务恢复
- [ ] 6.2 手动测试：长按任务 → 编辑对话框预填 → 修改保存 → 列表刷新
- [ ] 6.3 手动测试：点击标签筛选 → 再点取消 → 切 Tab 自动清除
- [ ] 6.4 手动测试：快捷栏显示待办数 → 完成任务后返回 → 数字更新
- [ ] 6.5 手动测试：网络断开时各操作的错误处理和回退
