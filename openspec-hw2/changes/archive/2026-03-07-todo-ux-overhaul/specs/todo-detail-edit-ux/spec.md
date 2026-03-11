## ADDED Requirements

### Requirement: 可编辑区域视觉提示
详情面板中可编辑的字段（标题、描述）SHALL 提供明确的视觉提示，让用户知道可以点击编辑。

#### Scenario: 标题区域提示
- **WHEN** 详情面板打开时
- **THEN** 标题右侧显示小型铅笔图标（16dp，onSurfaceVariant 色），提示用户可点击编辑

#### Scenario: 描述区域提示
- **WHEN** 描述为空时
- **THEN** 显示"点击添加描述..."占位文本，带铅笔图标

#### Scenario: 描述已有内容时
- **WHEN** 描述不为空时
- **THEN** 描述文本右上角显示小型铅笔图标

#### Scenario: 点击进入编辑
- **WHEN** 用户点击标题区域（包括铅笔图标）
- **THEN** 标题切换为 OutlinedTextField 并自动获取焦点，铅笔图标隐藏

### Requirement: 编辑模式自动聚焦
切换到编辑模式时，输入框 SHALL 自动获取焦点并弹出软键盘。

#### Scenario: 标题编辑聚焦
- **WHEN** 用户点击标题进入编辑模式
- **THEN** OutlinedTextField 自动聚焦，光标在文本末尾，软键盘弹出

#### Scenario: 描述编辑聚焦
- **WHEN** 用户点击描述进入编辑模式
- **THEN** OutlinedTextField 自动聚焦，光标在文本末尾

### Requirement: 对话框展开动画平滑化
添加/编辑任务对话框中"更多选项"的展开收起 SHALL 使用 animateContentSize 实现平滑的高度过渡动画。

#### Scenario: 展开更多选项
- **WHEN** 用户点击"更多选项"按钮
- **THEN** 高级选项区域以平滑动画从 0 高度渐变展开到完整高度，无跳跃或闪烁

#### Scenario: 收起更多选项
- **WHEN** 用户点击"收起选项"按钮
- **THEN** 高级选项区域以平滑动画从完整高度渐变收起到 0，无跳跃或闪烁

#### Scenario: 编辑模式默认展开
- **WHEN** 编辑模式且任务有高级字段值（描述/标签/截止日/象限）
- **THEN** 高级选项区域默认展开，不播放展开动画
