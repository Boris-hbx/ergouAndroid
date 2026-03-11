## ADDED Requirements

### Requirement: 列表进度指示器样式
列表中的任务进度 SHALL 使用圆弧进度指示器替代 LinearProgressIndicator，以更紧凑美观的方式展示进度。

#### Scenario: 有进度的未完成任务
- **WHEN** 任务 progress > 0 且 completed = false
- **THEN** 在任务卡片右侧显示小型圆弧进度环（24dp），环内显示百分比数字

#### Scenario: 无进度的任务
- **WHEN** 任务 progress = 0
- **THEN** 不显示进度指示器

#### Scenario: 已完成的任务
- **WHEN** 任务 completed = true
- **THEN** 不显示进度环（已有删除线和已完成区分）

### Requirement: 详情页进度滑块连续化
详情页的进度 Slider SHALL 移除 steps 刻度，改为连续滑动，以 1% 为最小步进。

#### Scenario: 拖动滑块
- **WHEN** 用户在详情页拖动进度滑块
- **THEN** 滑块连续移动，右侧实时显示百分比（如"73%"），无刻度卡顿

#### Scenario: 松手后提交
- **WHEN** 用户松手释放滑块
- **THEN** 系统将进度值取整为整数后提交更新

### Requirement: 进度 100% 完成确认
当用户将进度滑到 100% 时，系统 SHALL 弹出确认对话框，用户确认后才标记任务为已完成。

#### Scenario: 滑到 100% 弹出确认
- **WHEN** 用户将滑块滑到 100% 并松手
- **THEN** 系统弹出 AlertDialog："确认完成该任务？"，包含"确认"和"取消"按钮

#### Scenario: 确认完成
- **WHEN** 用户在确认对话框中点击"确认"
- **THEN** 系统将任务标记为 completed=true，更新进度为 100%，任务移至已完成区域

#### Scenario: 取消完成
- **WHEN** 用户在确认对话框中点击"取消"
- **THEN** 滑块回退到操作前的进度值，任务保持未完成状态

#### Scenario: Activity 重建时对话框状态
- **WHEN** 确认对话框显示期间发生 Activity 重建（如屏幕旋转）
- **THEN** 对话框状态 MUST 通过 ViewModel 或 rememberSaveable 保持，重建后仍显示
