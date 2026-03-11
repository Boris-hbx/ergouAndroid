## ADDED Requirements

### Requirement: 长按拖拽启动排序
用户 SHALL 能通过长按任务卡片并移动手指来启动拖拽排序模式。系统 MUST 区分"长按不动"（触发编辑）和"长按+移动"（触发拖拽）。

#### Scenario: 长按后移动触发拖拽
- **WHEN** 用户长按任务卡片 500ms 后手指发生位移（> 8dp）
- **THEN** 系统进入拖拽排序模式，取消编辑对话框触发

#### Scenario: 长按不动触发编辑
- **WHEN** 用户长按任务卡片 500ms 后手指未发生位移并释放
- **THEN** 系统触发编辑对话框（由 todo-edit-dialog 处理）

#### Scenario: 仅未完成任务参与拖拽
- **WHEN** 用户尝试拖拽已完成折叠区中的任务
- **THEN** 不触发拖拽排序

### Requirement: 拖拽视觉反馈
拖拽过程中系统 SHALL 提供清晰的视觉反馈。

#### Scenario: 被拖拽项高亮
- **WHEN** 拖拽模式激活
- **THEN** 被拖拽的卡片 MUST 显示阴影提升效果（elevation 增大），并跟随手指移动

#### Scenario: 插入位置指示
- **WHEN** 被拖拽卡片经过其他任务卡片
- **THEN** 其他卡片 MUST 实时让出空间，显示目标插入位置

### Requirement: 释放后更新排序
用户释放拖拽的卡片后，系统 SHALL 计算新的 sort_order 并持久化。

#### Scenario: 拖到两个任务之间
- **WHEN** 用户将任务拖到任务 A（sort_order=1.0）和任务 B（sort_order=2.0）之间后释放
- **THEN** 系统设置该任务的 sort_order 为 1.5（取中间值），调用 PUT /api/todos/:id 更新

#### Scenario: 拖到列表顶部
- **WHEN** 用户将任务拖到列表第一个任务（sort_order=1.0）之前
- **THEN** 系统设置 sort_order 为 0.5（第一个任务的 sort_order - 0.5）

#### Scenario: 拖到列表底部
- **WHEN** 用户将任务拖到列表最后一个任务（sort_order=5.0）之后
- **THEN** 系统设置 sort_order 为 5.5（最后一个任务的 sort_order + 0.5）

#### Scenario: 拖回原位
- **WHEN** 用户将任务拖拽后释放在原始位置
- **THEN** 系统不发起 API 请求

### Requirement: 排序网络异常处理
系统 SHALL 处理排序更新的网络失败情况。

#### Scenario: 排序更新失败
- **WHEN** PUT /api/todos/:id 更新 sort_order 请求失败
- **THEN** 列表回退到拖拽前的顺序，显示错误 Snackbar "排序失败"

### Requirement: Activity 重建后保持排序
排序结果 SHALL 在 Activity 重建（如旋转屏幕）后保持不变。

#### Scenario: 屏幕旋转后
- **WHEN** 用户调整排序后旋转屏幕
- **THEN** 任务列表从 API 重新加载，显示已更新的 sort_order 顺序
