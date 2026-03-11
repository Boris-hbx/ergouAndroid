## ADDED Requirements

### Requirement: 已完成任务折叠区
Todo 页面 SHALL 在未完成任务列表下方显示"已完成 (N)"折叠区域，默认收起。

#### Scenario: 显示折叠条
- **WHEN** 当前 Tab 下有已完成任务
- **THEN** 列表底部显示"已完成 (N)"可点击折叠条，N 为已完成任务数量

#### Scenario: 无已完成任务时隐藏
- **WHEN** 当前 Tab 下没有已完成任务
- **THEN** 不显示折叠条

### Requirement: 展开/收起已完成任务
用户点击折叠条时，系统 SHALL 切换已完成任务列表的展开/收起状态。

#### Scenario: 展开已完成列表
- **WHEN** 用户点击收起状态的"已完成 (N)"折叠条
- **THEN** 系统展开显示所有已完成任务，任务标题带删除线

#### Scenario: 收起已完成列表
- **WHEN** 用户点击展开状态的"已完成 (N)"折叠条
- **THEN** 系统收起已完成任务列表

### Requirement: 已完成任务与未完成任务分离
ViewModel SHALL 将当前 Tab 的任务分为未完成列表和已完成列表两组。

#### Scenario: 任务分组
- **WHEN** 系统获取到当前 Tab 的任务数据
- **THEN** 未完成任务（completed=false）显示在主列表，已完成任务（completed=true）显示在折叠区
