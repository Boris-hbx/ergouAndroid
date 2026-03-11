## ADDED Requirements

### Requirement: 标签使用 SuggestionChip 并支持点击
任务卡片上的标签 SHALL 使用 Material 3 的 SuggestionChip 组件（替代 AssistChip），点击后按该标签筛选任务列表。

#### Scenario: 点击标签激活筛选
- **WHEN** 用户点击任务卡片上的某个标签（如"工作"）
- **THEN** 列表仅显示包含"工作"标签的任务，被选中的标签高亮显示

#### Scenario: 再次点击取消筛选
- **WHEN** 用户点击已激活的筛选标签
- **THEN** 筛选取消，列表恢复显示当前 Tab 下的所有任务

#### Scenario: 切换到不同标签
- **WHEN** 已有标签"工作"被激活筛选时，用户点击另一个标签"学习"
- **THEN** 筛选切换为"学习"（单选模式），列表仅显示包含"学习"标签的任务

### Requirement: 筛选状态与 Tab 联动
标签筛选 SHALL 在当前 Tab 范围内生效，切换 Tab 时清除筛选。

#### Scenario: 筛选仅作用于当前 Tab
- **WHEN** 用户在"今天" Tab 下筛选标签"工作"
- **THEN** 仅显示"今天" Tab 中包含"工作"标签的任务

#### Scenario: 切换 Tab 清除筛选
- **WHEN** 用户在激活标签筛选状态下切换到"本周" Tab
- **THEN** 筛选自动清除，"本周" Tab 显示全部任务

### Requirement: 筛选结果空状态
当筛选结果为空时，系统 SHALL 显示友好的空状态提示。

#### Scenario: 筛选无匹配任务
- **WHEN** 筛选标签"学习"但当前 Tab 下没有带此标签的任务
- **THEN** 显示空状态提示"该标签下没有任务"

### Requirement: ViewModel 管理筛选状态
TaskViewModel SHALL 维护当前筛选标签状态，通过 StateFlow 暴露给 UI。

#### Scenario: 筛选为本地过滤
- **WHEN** 用户点击标签筛选
- **THEN** 系统在本地已加载的任务列表上过滤，不发起额外 API 请求
