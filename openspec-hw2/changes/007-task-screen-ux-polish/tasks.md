## 1. 左滑删除确认

- [x] 1.1 SwipeTodoItem: confirmValueChange 返回 false，改为设置 pendingDeleteTodo state
- [x] 1.2 SwipeTodoItem 内部增加 AlertDialog（标题"确认删除"，正文"确定要删除「xxx」吗？"）
- [x] 1.3 确认删除：调用 onDelete lambda + Snackbar 撤销，重置 pendingDeleteTodo
- [x] 1.4 取消删除：LaunchedEffect 检测 pendingDeleteTodo=null 时调用 dismissState.reset()
- [x] 1.5 TaskScreen 中删除逻辑不变（onDelete 仍然是 deleteTodo + snackbar）

## 2. 任务详情 BottomSheet

- [x] 2.1 TaskScreen 顶层新增 detailTodo: NextTodo? state
- [x] 2.2 TodoItem onClick 设置 detailTodo（替换空 onClick）
- [x] 2.3 新增 TaskDetailBottomSheet composable，接收 todo + viewModel callbacks
- [x] 2.4 BottomSheet 标题区：默认大字显示，点击变 OutlinedTextField，失焦保存
- [x] 2.5 BottomSheet 状态行：进度百分比 + 完成状态 + 象限彩色 FilterChip
- [x] 2.6 BottomSheet 描述区：默认文本显示（空则占位符），点击变多行 OutlinedTextField，失焦保存
- [x] 2.7 BottomSheet 标签区：FilterChip 列表 + 输入框添加新标签 + 点击删除
- [x] 2.8 BottomSheet 截止日期：显示日期 + 点击弹 DatePicker + 清除按钮
- [x] 2.9 BottomSheet 底部操作栏：完成按钮 + 删除按钮（删除弹确认）
- [x] 2.10 关闭 BottomSheet 时刷新列表（refreshTodos）

## 3. 验证

- [x] 3.1 编译通过（只改 TaskScreen.kt）
- [ ] 3.2 手动测试：左滑 → 确认对话框 → 删除/取消
- [ ] 3.3 手动测试：单击 → BottomSheet → 编辑各字段 → 保存
