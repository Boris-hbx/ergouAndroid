## ADDED Requirements

### Requirement: 任务卡片紧凑布局
任务卡片 SHALL 采用紧凑布局：左侧圆形勾选框 + 右侧标题和四象限标签，单行为主。

#### Scenario: 未完成任务卡片
- **WHEN** 渲染一个未完成任务
- **THEN** 显示空心圆形勾选框 + 任务标题 + 四象限颜色标签（如有）

#### Scenario: 已完成任务卡片
- **WHEN** 渲染一个已完成任务
- **THEN** 显示实心勾选框 + 删除线标题 + 灰色文字

#### Scenario: 有截止日期的任务
- **WHEN** 任务有 dueDate 字段
- **THEN** 标题下方显示截止日期（小字灰色）

### Requirement: 四象限颜色标签
四象限 SHALL 用颜色标签区分，显示在任务标题右侧或下方。

#### Scenario: 紧急重要任务
- **WHEN** 任务 quadrant 为 "urgent-important"
- **THEN** 显示红色标签

#### Scenario: 无四象限任务
- **WHEN** 任务没有 quadrant 字段
- **THEN** 不显示四象限标签

### Requirement: 任务操作简化
任务卡片 SHALL 支持点击勾选框完成任务，长按或左滑删除。

#### Scenario: 点击勾选框完成
- **WHEN** 用户点击未完成任务的勾选框
- **THEN** 系统将该任务标记为完成，任务移入已完成区域

#### Scenario: 删除任务
- **WHEN** 用户对任务卡片执行删除操作
- **THEN** 系统弹出确认对话框，确认后删除任务

### Requirement: 添加任务对话框简化
添加任务对话框 SHALL 默认仅显示标题输入框，高级选项（四象限、描述、标签、截止日期）可展开。

#### Scenario: 快速添加
- **WHEN** 用户点击 FAB 并输入标题后确认
- **THEN** 系统用标题创建任务，其他字段使用默认值

#### Scenario: 展开高级选项
- **WHEN** 用户点击"更多选项"
- **THEN** 显示四象限选择、描述、标签、截止日期输入区域
