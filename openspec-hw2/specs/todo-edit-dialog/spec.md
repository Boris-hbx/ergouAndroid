## ADDED Requirements

### Requirement: 长按触发编辑对话框
用户 SHALL 能通过长按任务卡片打开编辑对话框。系统 MUST 使用 combinedClickable 的 onLongClick 回调检测长按手势。

#### Scenario: 长按打开编辑
- **WHEN** 用户长按某个未完成任务卡片（持续 500ms 以上且无移动）
- **THEN** 系统弹出编辑对话框，预填该任务的所有现有字段

#### Scenario: 长按已完成任务
- **WHEN** 用户长按已完成折叠区中的任务卡片
- **THEN** 同样弹出编辑对话框

#### Scenario: 短按不触发编辑
- **WHEN** 用户快速点击任务卡片（不足 500ms）
- **THEN** 不触发任何操作（保持现有行为）

### Requirement: 编辑对话框复用添加对话框
编辑对话框 SHALL 复用 AddTaskDialog 组件，通过参数区分"添加"和"编辑"模式。

#### Scenario: 编辑模式预填数据
- **WHEN** 编辑对话框打开时
- **THEN** 标题、描述、标签、到期日、象限字段 MUST 全部预填为当前任务的值

#### Scenario: 编辑模式对话框标题
- **WHEN** 对话框以编辑模式打开
- **THEN** 对话框标题显示"编辑任务"（区别于添加模式的"添加任务"）

#### Scenario: 编辑模式确认按钮
- **WHEN** 对话框以编辑模式打开
- **THEN** 确认按钮文本显示"保存"（区别于添加模式的"添加"）

### Requirement: 编辑提交调用更新 API
用户确认编辑后，系统 SHALL 调用 PUT /api/todos/:id 更新任务，仅发送被修改的字段。

#### Scenario: 修改单个字段
- **WHEN** 用户仅修改了任务标题并点击"保存"
- **THEN** 系统调用 PUT /api/todos/:id，请求体仅包含 text 字段

#### Scenario: 修改多个字段
- **WHEN** 用户同时修改了标题、标签和到期日
- **THEN** 系统调用 PUT /api/todos/:id，请求体包含 text、tags、due_date 三个字段

#### Scenario: 未修改任何字段
- **WHEN** 用户打开编辑对话框后未做任何修改就点击"保存"
- **THEN** 系统不发起 API 请求，直接关闭对话框

### Requirement: 编辑网络异常处理
系统 SHALL 处理更新请求的网络失败情况。

#### Scenario: 更新请求失败
- **WHEN** PUT /api/todos/:id 请求返回错误或网络超时
- **THEN** 对话框保持打开，显示错误 Snackbar "保存失败，请重试"

### Requirement: 编辑后列表即时刷新
任务编辑成功后，系统 SHALL 立即刷新任务列表反映变更。

#### Scenario: 编辑成功后刷新
- **WHEN** PUT /api/todos/:id 请求成功返回
- **THEN** 对话框关闭，任务列表中该任务即时显示更新后的内容，无需手动刷新

#### Scenario: 象限变更后位置更新
- **WHEN** 用户编辑时修改了任务的象限
- **THEN** 任务在列表中的象限标记颜色 MUST 即时更新为新象限对应的颜色
