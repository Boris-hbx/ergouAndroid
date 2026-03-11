# todo-optimistic-update Specification

## Purpose
TBD - created by archiving change todo-ux-overhaul. Update Purpose after archive.
## Requirements
### Requirement: 操作后即时更新本地 UI 状态
用户对任务执行操作后，系统 SHALL 立即在本地 StateFlow 中更新对应任务的状态，不等待 API 响应。

#### Scenario: 勾选完成
- **WHEN** 用户勾选某个未完成任务
- **THEN** 系统立即将该任务从 pendingTodos 移至 completedTodos，UI 即时反映变化

#### Scenario: 取消完成
- **WHEN** 用户在已完成列表中取消勾选某个任务
- **THEN** 系统立即将该任务从 completedTodos 移回 pendingTodos

#### Scenario: 删除任务
- **WHEN** 用户删除某个任务
- **THEN** 系统立即将该任务从当前列表中移除，不等待 API 返回

#### Scenario: 更新进度
- **WHEN** 用户修改任务进度值
- **THEN** 系统立即更新该任务在列表中的 progress 字段值

#### Scenario: 更新标题或描述
- **WHEN** 用户在详情面板中编辑标题或描述并确认
- **THEN** 系统立即更新该任务在列表中的 text/content 字段值

### Requirement: API 请求后台执行
系统 SHALL 在本地状态更新后，异步发送 API 请求到 Next 后端。

#### Scenario: 正常操作流程
- **WHEN** 本地状态已更新
- **THEN** 系统在后台发送对应的 API 请求（PUT/DELETE），不阻塞 UI

### Requirement: 失败时回滚并提示
当 API 请求失败时，系统 SHALL 将本地状态回滚到操作前的快照，并显示错误提示。

#### Scenario: 完成操作 API 失败
- **WHEN** 勾选完成后 API 返回错误
- **THEN** 系统将该任务从 completedTodos 移回 pendingTodos，显示 Snackbar "操作失败，请重试"

#### Scenario: 删除操作 API 失败
- **WHEN** 删除任务后 API 返回错误
- **THEN** 系统将该任务重新插入原列表位置，显示 Snackbar "删除失败，请重试"

#### Scenario: 进度更新 API 失败
- **WHEN** 更新进度后 API 返回错误
- **THEN** 系统将该任务的 progress 字段回滚到操作前的值，显示 Snackbar "更新失败，请重试"

#### Scenario: 网络不可用
- **WHEN** 设备无网络连接时用户执行操作
- **THEN** 系统同样先乐观更新本地状态，API 失败后回滚并提示

### Requirement: 不再全量刷新列表
系统 SHALL 不在每次操作成功后调用 refreshTodos() 全量刷新。仅在以下场景全量刷新：初始加载、切换 Tab、手动下拉刷新。

#### Scenario: 完成任务后不刷新
- **WHEN** 用户完成一个任务且 API 成功返回
- **THEN** 系统不调用 refreshTodos()，列表保持当前状态

#### Scenario: 切换 Tab 时刷新
- **WHEN** 用户切换到"本周"Tab
- **THEN** 系统调用 refreshTodos() 从 API 获取该 Tab 数据

