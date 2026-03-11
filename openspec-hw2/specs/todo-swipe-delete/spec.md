## ADDED Requirements

### Requirement: 左滑手势触发删除
任务卡片 SHALL 支持从右向左滑动手势，使用 Material 3 的 SwipeToDismissBox 组件。滑动过程中 SHALL 露出红色背景和删除图标。

#### Scenario: 滑动超过阈值删除
- **WHEN** 用户将任务卡片向左滑动超过卡片宽度的 40%
- **THEN** 系统调用 DELETE /api/todos/:id 执行软删除，任务从列表中移除

#### Scenario: 滑动未超过阈值
- **WHEN** 用户将任务卡片向左滑动不足 40% 后释放
- **THEN** 卡片动画回弹到原始位置，不触发删除

#### Scenario: 已完成任务也支持滑动删除
- **WHEN** 用户对已完成折叠区中的任务向左滑动超过阈值
- **THEN** 系统同样执行软删除

### Requirement: 删除后支持撤销
任务被滑动删除后，系统 SHALL 显示 Snackbar 提供撤销操作。

#### Scenario: 撤销删除
- **WHEN** 任务被删除后，用户在 Snackbar 显示期间（5 秒内）点击"撤销"
- **THEN** 系统调用 POST /api/todos/:id/restore 恢复任务，任务重新出现在列表原位置

#### Scenario: Snackbar 超时
- **WHEN** Snackbar 显示 5 秒后用户未操作
- **THEN** Snackbar 自动消失，删除操作生效

### Requirement: 删除网络异常处理
系统 SHALL 处理删除请求的网络失败情况。

#### Scenario: 删除请求失败
- **WHEN** DELETE /api/todos/:id 请求返回错误或网络超时
- **THEN** 卡片回弹到原位，显示错误 Snackbar "删除失败，请重试"

### Requirement: 移除旧删除按钮
原有的右侧删除 IconButton 和确认 AlertDialog SHALL 被移除，统一使用滑动删除交互。

#### Scenario: 删除入口唯一性
- **WHEN** 任务卡片渲染时
- **THEN** 卡片右侧不再显示删除按钮，仅通过左滑手势触发删除
