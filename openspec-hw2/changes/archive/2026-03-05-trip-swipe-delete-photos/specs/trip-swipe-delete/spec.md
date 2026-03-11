## ADDED Requirements

### Requirement: Trip 列表左滑删除
Trip 列表中的每个项目 SHALL 支持 SwipeToDismissBox 左滑手势触发删除流程。列表项右侧的 Delete 按钮 MUST 被移除。

#### Scenario: 左滑触发确认
- **WHEN** 用户在 Trip 列表中左滑某个项目
- **THEN** 系统弹出 AlertDialog 确认对话框，显示"确定删除此差旅？"

#### Scenario: 确认后乐观移除
- **WHEN** 用户在确认对话框中点击"删除"
- **THEN** 系统立即从列表中移除该 Trip（乐观更新），并显示 Snackbar

#### Scenario: 取消删除
- **WHEN** 用户在确认对话框中点击"取消"
- **THEN** 列表恢复原状，SwipeToDismissBox 重置

### Requirement: 费用项左滑删除
Trip 详情页中的费用项列表 SHALL 支持 SwipeToDismissBox 左滑手势触发删除流程。编辑对话框中的"删除此费用"按钮保留，但 MUST 同样弹出确认对话框。

#### Scenario: 费用项左滑触发确认
- **WHEN** 用户在费用项列表中左滑某个费用项
- **THEN** 系统弹出 AlertDialog 确认对话框

#### Scenario: 编辑对话框内删除触发确认
- **WHEN** 用户在编辑对话框中点击"删除此费用"按钮
- **THEN** 系统弹出 AlertDialog 确认对话框（与左滑流程一致）

#### Scenario: 确认后乐观移除费用项
- **WHEN** 用户确认删除费用项
- **THEN** 系统乐观移除该费用项，显示 Snackbar

### Requirement: Snackbar 延迟删除与撤销
确认删除后，系统 SHALL 显示 Snackbar 并延迟约 5 秒再执行真正的 API 删除。Snackbar MUST 提供"撤销"操作按钮。

#### Scenario: 撤销恢复
- **WHEN** 用户在 Snackbar 消失前点击"撤销"
- **THEN** 系统将被删除的项目恢复到列表中，不调用删除 API

#### Scenario: 超时真删
- **WHEN** Snackbar 超时消失且用户未点击"撤销"
- **THEN** 系统调用 API 执行真正删除

#### Scenario: API 删除失败
- **WHEN** API 删除请求失败
- **THEN** 系统将项目恢复到列表中，显示错误 Snackbar

### Requirement: 延迟删除状态管理
ViewModel SHALL 维护 `pendingDeleteTripIds: Set<String>` 和 `pendingDeleteItemIds: Set<String>` 状态，用于跟踪待删除项目。UI 层 MUST 在列表渲染时过滤掉 pendingDelete 中的项目。

#### Scenario: 多个同时待删除
- **WHEN** 用户连续左滑删除多个项目
- **THEN** 每个项目独立进入 pending 状态，各自有独立的 Snackbar 计时

#### Scenario: Activity 重建恢复
- **WHEN** Activity 在 pending 期间被重建（如旋转屏幕）
- **THEN** pending 状态通过 ViewModel 存活保持，不会丢失
