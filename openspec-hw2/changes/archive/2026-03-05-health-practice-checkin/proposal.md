## Why

健康模块目前只是养生功法浏览器（八段锦/易筋经/站桩/经络穴位），支持收藏和搜索，但缺少"练习记录"功能。用户无法追踪自己练了什么、练了几次、连续坚持了多少天。添加打卡 + 统计功能，让用户有动力持续练习。

## What Changes

- 在 `HealthUiState` 中添加练习打卡相关状态（practiceLog、todayPracticed、streakDays、totalPracticeCount）
- 使用现有 `health_prefs` DataStore 存储练习记录（JSON 格式，最多保留 90 天）
- ViewModel 添加 `togglePractice(itemId)` 和 `loadPracticeLog()` 方法
- HealthScreen 顶部添加统计条（连续天数、今日进度、累计次数）
- 功法卡片 header 行添加打卡按钮（绿色实心 ✓ / 空心圆形）
- 连续天数计算：从今天往回逐日检查，有记录 +1，断了停止

## Capabilities

### New Capabilities
- `practice-checkin`: 练习打卡与统计 — 用户可标记/取消今日已练功法，查看连续天数、今日进度、累计次数

### Modified Capabilities
<!-- 无现有 spec 需要修改 -->

## Impact

- 修改文件：`HealthViewModel.kt`、`HealthScreen.kt`（仅这两个文件）
- 数据存储：复用 `health_prefs` DataStore，新增 `practice_log` key
- 依赖：无新增依赖，使用现有 `kotlinx.serialization.json.Json` 和 `java.time.LocalDate`
- 不修改：AppModule.kt、ErgouPrompt.kt、HealthData.kt、QueryHealthTool.kt、RecommendHealthTool.kt
