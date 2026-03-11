# Capability: practice-checkin (练习打卡与统计)

## Purpose
让用户在健康页面对功法进行每日打卡，并展示连续天数、今日进度、累计次数等统计数据，帮助用户养成练习习惯。

## Requirements

### Requirement: 练习打卡切换
用户 SHALL 能在功法卡片上点击打卡按钮，标记/取消今日已练习该功法。打卡状态以当日（`LocalDate`）为粒度，同一功法同一天只有"已练/未练"两种状态。

#### Scenario: 首次打卡
- **WHEN** 用户点击某功法卡片上的空心打卡按钮
- **THEN** 按钮变为绿色实心 ✓，该功法被记录为今日已练

#### Scenario: 取消打卡
- **WHEN** 用户点击已打卡（绿色实心 ✓）的按钮
- **THEN** 按钮恢复为空心圆形，该功法的今日练习记录被移除

#### Scenario: 跨天重置
- **WHEN** 用户在新的一天打开健康页面
- **THEN** 所有功法的今日打卡状态为未练（历史记录保留）

### Requirement: 练习记录持久化
系统 SHALL 使用现有 `health_prefs` DataStore 的 `practice_log` key 持久化练习记录。记录格式为 JSON，按日期索引，每天存储已练功法 ID 列表。系统 SHALL 只保留最近 90 天的记录，超出部分在加载时自动清理。

#### Scenario: 打卡后持久化
- **WHEN** 用户完成打卡操作（标记或取消）
- **THEN** 系统立即将更新后的练习记录写入 DataStore

#### Scenario: 启动时加载
- **WHEN** 用户打开健康页面
- **THEN** 系统从 DataStore 读取 `practice_log`，解析为内存数据结构

#### Scenario: DataStore 读取失败降级
- **WHEN** DataStore 读取 `practice_log` 抛出异常
- **THEN** 系统使用空记录作为降级，并通过 `Timber.e` 记录错误日志

#### Scenario: DataStore 写入失败降级
- **WHEN** DataStore 写入失败
- **THEN** 系统保留内存中的状态（下次启动时丢失），并通过 `Timber.e` 记录错误日志

#### Scenario: 自动清理过期记录
- **WHEN** 系统加载练习记录且存在超过 90 天的条目
- **THEN** 系统自动删除过期条目并写回 DataStore

### Requirement: 统计数据展示
系统 SHALL 在健康页面顶部（功法列表上方）显示统计条，包含连续天数、今日进度、累计次数三项指标。

#### Scenario: 正常显示统计
- **WHEN** 用户打开健康页面且有练习记录
- **THEN** 顶部统计条显示：🔥 连续 X 天 | 今日 Y/N | 累计 Z 次（N 为功法总数）

#### Scenario: 无练习记录
- **WHEN** 用户从未打卡
- **THEN** 统计条显示：连续 0 天 | 今日 0/N | 累计 0 次

#### Scenario: 统计实时更新
- **WHEN** 用户完成打卡/取消打卡操作
- **THEN** 统计条立即更新，无需刷新页面

### Requirement: 连续天数计算
系统 SHALL 从今天开始往回逐日检查练习记录，连续有记录则天数 +1，遇到无记录的日期则停止计算。今天有打卡则包含今天，否则从昨天开始算。

#### Scenario: 连续打卡
- **WHEN** 用户过去 5 天每天都有至少 1 次打卡，今天也已打卡
- **THEN** 连续天数显示 6

#### Scenario: 今日未打卡但昨天有
- **WHEN** 用户今天未打卡，但昨天及之前连续 3 天有打卡
- **THEN** 连续天数显示 3

#### Scenario: 中间断了一天
- **WHEN** 用户今天和前天有打卡，但昨天没有
- **THEN** 连续天数显示 1（仅算今天）

### Requirement: 打卡按钮 UI
功法卡片 header 行 SHALL 在右侧显示打卡按钮。未打卡时显示空心圆形图标，已打卡时显示绿色实心 ✓ 图标。按钮点击区域 MUST 不小于 40dp。

#### Scenario: 未打卡视觉状态
- **WHEN** 功法今日未打卡
- **THEN** 显示空心圆形图标，颜色为 `onSurfaceVariant`

#### Scenario: 已打卡视觉状态
- **WHEN** 功法今日已打卡
- **THEN** 显示绿色实心 ✓ 图标（`Color(0xFF43A047)`）
