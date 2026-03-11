## ADDED Requirements

### Requirement: Tab 显示未完成任务计数
每个时间维度 Tab SHALL 显示该维度下未完成任务的数量，格式为 `标签 N`。

#### Scenario: Tab 计数正确显示
- **WHEN** 用户进入 Todo 页面，"今天"有 4 个未完成任务
- **THEN** "今天"Tab 显示为"今天 4"

#### Scenario: 切换 Tab 后计数更新
- **WHEN** 用户切换到"本周"Tab
- **THEN** "本周"Tab 显示该时间段的未完成任务计数

#### Scenario: 完成任务后计数递减
- **WHEN** 用户完成一个任务
- **THEN** 当前 Tab 的计数减少 1

#### Scenario: 计数为零
- **WHEN** 某个 Tab 下没有未完成任务
- **THEN** 该 Tab 显示为"标签 0"或仅显示标签名（无数字）

### Requirement: Tab 标签文案对齐设计稿
Tab 标签 SHALL 显示为"今天"、"本周"、"30天"，对应 API 的 today/week/month 参数。

#### Scenario: Tab 标签文案
- **WHEN** Todo 页面渲染 Tab 栏
- **THEN** 三个 Tab 依次显示"今天"、"本周"、"30天"
