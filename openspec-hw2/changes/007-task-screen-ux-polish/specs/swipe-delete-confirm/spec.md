## ADDED Requirements

### Requirement: 左滑松手后弹出确认对话框
SwipeToDismissBox 的 confirmValueChange SHALL 返回 false（阻止自动 dismiss），同时记录待删除的 todo，弹出 AlertDialog。

#### Scenario: 滑动超过阈值松手
- **WHEN** 用户左滑超过阈值后松手
- **THEN** 系统弹出 AlertDialog（标题"确认删除"，正文"确定要删除「{todo.text}」吗？"），SwipeToDismissBox 保持滑开状态

#### Scenario: 用户确认删除
- **WHEN** 用户在确认对话框中点击"删除"
- **THEN** 系统调用 viewModel.deleteTodo(id)，关闭对话框，SwipeToDismissBox 重置，显示 Snackbar "已删除" + "撤销"按钮

#### Scenario: 用户取消删除
- **WHEN** 用户在确认对话框中点击"取消"或点击对话框外部
- **THEN** 对话框关闭，SwipeToDismissBox 通过 reset() 动画回弹到原位

#### Scenario: 撤销删除
- **WHEN** 用户点击 Snackbar 的"撤销"按钮
- **THEN** 系统调用 viewModel.restoreTodo(id) 恢复任务
