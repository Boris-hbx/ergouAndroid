## ADDED Requirements

### Requirement: 列表项左滑手势
每个 ReviewItemCard SHALL 包裹在 SwipeToDismissBox 中，仅支持 endToStart 方向（右往左滑）。滑动背景 SHALL 显示红色 (error color) + 删除图标。

#### Scenario: 左滑触发确认
- **WHEN** 用户左滑列表项到阈值并松手
- **THEN** 系统弹出 AlertDialog 确认删除，SwipeToDismissBox 不完成 dismiss（confirmValueChange 返回 false）

#### Scenario: 滑动不足回弹
- **WHEN** 用户左滑未达阈值即松手
- **THEN** 列表项自动回弹到原位，不触发任何操作

#### Scenario: 禁止右滑
- **WHEN** 用户尝试从左往右滑（startToEnd）
- **THEN** 无响应（enableDismissFromStartToEnd = false）

### Requirement: 确认对话框
左滑触发后 SHALL 显示 AlertDialog，标题"确认删除"，正文"确定要删除「{item.text}」吗？"，按钮为"删除"（error 颜色）和"取消"。

#### Scenario: 用户确认删除
- **WHEN** 用户点击"删除"按钮
- **THEN** 对话框关闭，进入延迟删除流程

#### Scenario: 用户取消删除
- **WHEN** 用户点击"取消"或点击对话框外部
- **THEN** 对话框关闭，列表项保持不变

### Requirement: 客户端延迟删除
确认删除后，系统 SHALL 立即从 UI 列表中隐藏该项（通过 pendingDeleteIds 状态过滤），同时显示 Snackbar（message="已删除", actionLabel="撤销", duration=Short）。后端删除 API SHALL 仅在 Snackbar 自然消失后调用。

#### Scenario: 撤销删除
- **WHEN** 用户在 Snackbar 消失前点击"撤销"
- **THEN** 条目从 pendingDeleteIds 移除，重新出现在列表中，不调用后端删除

#### Scenario: 确认生效
- **WHEN** Snackbar 自然消失（Dismissed）
- **THEN** 系统调用 viewModel.deleteReview(id)，从 pendingDeleteIds 移除

#### Scenario: 多项同时 pending
- **WHEN** 用户在第一个 Snackbar 未消失时左滑删除第二项
- **THEN** 两项均从列表隐藏，各自独立的 Snackbar/撤销流程

### Requirement: 详情页删除走延迟流程
详情页的删除按钮 SHALL 触发相同的确认对话框。确认后 SHALL 返回列表页并进入延迟删除流程（pendingDeleteIds + Snackbar 撤销）。

#### Scenario: 详情页确认删除
- **WHEN** 用户在详情页点击删除并确认
- **THEN** 系统关闭详情页，返回列表页，该项隐藏，显示 Snackbar 可撤销
