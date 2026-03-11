## 1. HealthViewModel 数据层

- [x] 1.1 在 `HealthUiState` 添加 `practiceLog: Map<String, Set<String>>`、`todayPracticed: Set<String>`、`streakDays: Int`、`totalPracticeCount: Int` 字段（均带默认值）
- [x] 1.2 添加 `PRACTICE_LOG_KEY = stringPreferencesKey("practice_log")` 常量
- [x] 1.3 实现 `loadPracticeLog()` — 从 DataStore 读取 JSON，解析为 Map，清理 >90 天数据，更新 UiState（含 todayPracticed、streakDays、totalPracticeCount）
- [x] 1.4 实现 `togglePractice(itemId: String)` — 切换今日打卡状态，更新内存状态并持久化到 DataStore
- [x] 1.5 实现 `calculateStreakDays(practiceLog)` — 从今天/昨天往回逐日检查，返回连续天数（宽松模式：今天没练从昨天开始算）
- [x] 1.6 在 `init` 块中调用 `loadPracticeLog()`

## 2. HealthScreen 统计条

- [x] 2.1 在搜索框和 Tab 之间添加统计条 `Surface`，使用 `surfaceVariant` 背景色
- [x] 2.2 统计条 Row 展示三项：🔥连续 X 天、今日 Y/N、累计 Z 次，使用 `SpaceEvenly` 排列
- [x] 2.3 连续天数 > 7 时数字使用 `MaterialTheme.colorScheme.primary` 高亮

## 3. HealthScreen 打卡按钮

- [x] 3.1 `HealthItemCard` 添加 `isPracticed: Boolean` 和 `onTogglePractice: () -> Unit` 参数
- [x] 3.2 在 header 行收藏星标左侧添加打卡 `IconButton`（绿色实心 ✓ / 空心圆形），最小点击区域 40dp
- [x] 3.3 调用处传入 `isPracticed = item.id in uiState.todayPracticed` 和 `onTogglePractice = { viewModel.togglePractice(item.id) }`

## 4. 手动测试

- [ ] 4.1 验证打卡/取消打卡：点击后按钮状态切换，统计条实时更新
- [ ] 4.2 验证持久化：打卡后杀掉 App 重启，打卡状态恢复
- [ ] 4.3 验证连续天数：连续多天打卡后显示正确天数，中断后归零或从昨天算
- [ ] 4.4 验证统计条样式：连续 >7 天时数字高亮，统计条布局正确
