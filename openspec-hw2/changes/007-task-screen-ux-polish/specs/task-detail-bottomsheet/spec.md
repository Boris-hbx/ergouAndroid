## ADDED Requirements

### Requirement: 单击弹出 ModalBottomSheet
用户单击任务卡片 SHALL 弹出 ModalBottomSheet 显示任务详情。长按编辑对话框保留不变。

#### Scenario: 单击打开详情
- **WHEN** 用户单击任务卡片
- **THEN** 弹出 ModalBottomSheet，显示任务完整详情

#### Scenario: 关闭 BottomSheet
- **WHEN** 用户下拉或点击 scrim 关闭 BottomSheet
- **THEN** BottomSheet 关闭，修改已自动保存

### Requirement: BottomSheet 标题可内联编辑
标题区域 SHALL 显示大字标题，点击后变为 OutlinedTextField 可编辑。

#### Scenario: 点击标题进入编辑态
- **WHEN** 用户点击标题文本
- **THEN** 标题变为 OutlinedTextField，显示当前文本，获得焦点

#### Scenario: 编辑完成保存
- **WHEN** 用户修改标题后点击其他区域（失焦）
- **THEN** 系统调用 viewModel.updateTodo(id, text=newTitle, ...) 保存，退出编辑态

### Requirement: BottomSheet 显示状态行
状态行 SHALL 显示进度百分比、完成状态和象限标签。

#### Scenario: 显示状态信息
- **WHEN** BottomSheet 打开时
- **THEN** 显示进度 N%（如有）、完成/未完成状态、象限彩色 Chip

### Requirement: BottomSheet 描述可内联编辑
描述区域 SHALL 显示 todo.content 文本，点击后变为 OutlinedTextField 可编辑。

#### Scenario: 点击描述进入编辑态
- **WHEN** 用户点击描述文本
- **THEN** 描述变为多行 OutlinedTextField，获得焦点

#### Scenario: 描述为空时
- **WHEN** todo.content 为空
- **THEN** 显示灰色占位文本"添加描述..."，点击后进入编辑态

### Requirement: BottomSheet 标签编辑
标签区域 SHALL 用 FilterChip 展示现有标签，支持删除和添加。

#### Scenario: 删除标签
- **WHEN** 用户点击已有标签的关闭图标
- **THEN** 移除该标签，调用 updateTodo 保存

#### Scenario: 添加标签
- **WHEN** 用户在标签输入框中输入新标签并回车
- **THEN** 新标签添加到列表，调用 updateTodo 保存

### Requirement: BottomSheet 截止日期编辑
截止日期区域 SHALL 显示当前 due_date，点击弹出 DatePicker。

#### Scenario: 修改截止日期
- **WHEN** 用户点击日期区域并在 DatePicker 中选择新日期
- **THEN** 日期更新，调用 updateTodo 保存

#### Scenario: 清除截止日期
- **WHEN** 用户点击日期旁的清除按钮
- **THEN** 截止日期清空，调用 updateTodo 保存

### Requirement: BottomSheet 底部操作栏
底部 SHALL 有"完成"和"删除"按钮。

#### Scenario: 点击完成按钮
- **WHEN** 用户点击"完成"按钮
- **THEN** 调用 viewModel.completeTodo(id)，关闭 BottomSheet

#### Scenario: 点击删除按钮
- **WHEN** 用户点击"删除"按钮
- **THEN** 弹出确认对话框，确认后删除并关闭 BottomSheet
