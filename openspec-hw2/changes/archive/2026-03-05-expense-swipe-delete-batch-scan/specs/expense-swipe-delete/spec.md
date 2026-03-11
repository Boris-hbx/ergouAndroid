## ADDED Requirements

### Requirement: 左滑删除记账条目
系统 SHALL 使用 SwipeToDismissBox 包裹记账列表条目，支持向左滑动触发删除流程。原有的 X 按钮删除入口 SHALL 被移除。

#### Scenario: 左滑触发删除确认
- **WHEN** 用户在记账条目上向左滑动超过阈值
- **THEN** 系统显示红色背景和删除图标
- **THEN** 系统弹出确认对话框："确认删除此记录？"

#### Scenario: 用户确认删除
- **WHEN** 用户在确认对话框中点击"确认"
- **THEN** 系统乐观移除该条目（立即从列表隐藏）
- **THEN** 系统显示 Snackbar "已删除" 并附带"撤销"按钮

#### Scenario: 用户取消删除
- **WHEN** 用户在确认对话框中点击"取消"或返回
- **THEN** 条目恢复原位，SwipeToDismissBox 重置状态

#### Scenario: Snackbar 超时后真删
- **WHEN** Snackbar 超时（约 5 秒）且用户未点击撤销
- **THEN** 系统调用后端 DELETE API 永久删除该条目

#### Scenario: 用户点击撤销
- **WHEN** 用户在 Snackbar 超时前点击"撤销"
- **THEN** 系统将条目恢复到列表原位，不调用删除 API

#### Scenario: 后端删除失败
- **WHEN** 后端 DELETE API 调用失败
- **THEN** 系统将条目恢复到列表中
- **THEN** 系统显示错误 Snackbar
