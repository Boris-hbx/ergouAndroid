## Context

健康模块目前是养生功法浏览器，HealthViewModel 使用 `health_prefs` DataStore 存储收藏（`stringSetPreferencesKey`），HealthScreen 展示分类 Tab + 功法卡片列表。需要在此基础上添加练习打卡和统计功能，只修改这两个文件。

## Goals / Non-Goals

**Goals:**
- 用户能在功法卡片上一键打卡/取消打卡
- 顶部统计条展示连续天数、今日进度、累计次数
- 练习记录持久化到 DataStore，最多保留 90 天
- Activity 重建后状态自动恢复

**Non-Goals:**
- 不添加提醒/通知功能
- 不创建新文件（不新建 Repository、Entity 等）
- 不修改 AppModule、ErgouPrompt、HealthData 等共享文件
- 不支持按时间段统计或图表展示

## Decisions

### D1: 使用 DataStore stringPreferencesKey 存储 JSON

**选择**: 在 `health_prefs` DataStore 中用 `stringPreferencesKey("practice_log")` 存储 JSON 字符串。

**替代方案**:
- Room 表：需要新建 Entity/Dao/Migration，违反"不创建新文件"的限制
- DataStore `stringSetPreferencesKey` 按天存多个 key：key 数量随天数增长，清理麻烦

**理由**: 最简单，复用现有 DataStore 实例，一个 key 存所有数据。90 天 × 每天几条记录，JSON 体积很小（< 10KB）。

### D2: JSON 格式 `{"yyyy-MM-dd": ["itemId1", ...], ...}`

使用 `kotlinx.serialization.json.Json` 手动解析为 `Map<String, Set<String>>`。不定义专门的 `@Serializable` 数据类，直接用 `Json.parseToJsonElement` + 手动转换，避免引入额外类型。

### D3: 连续天数计算逻辑

从今天开始往回逐日检查：
- 如果今天有记录，从今天开始往回算
- 如果今天没记录，从昨天开始往回算
- 逐日回查，有记录则 +1，遇到无记录的日期停止

这与 spec 中"今日未打卡但昨天有→连续天数为 3"的场景一致。宽松模式更友好——当天还没练不代表断链。

### D4: HealthUiState 扩展字段

在现有 `HealthUiState` data class 中添加 4 个字段，都带默认值，不影响现有代码：
```kotlin
val practiceLog: Map<String, Set<String>> = emptyMap()
val todayPracticed: Set<String> = emptySet()
val streakDays: Int = 0
val totalPracticeCount: Int = 0
```

`todayPracticed` 是 `practiceLog[today]` 的快捷引用，避免 UI 层反复计算。

### D5: HealthItemCard 新增 isPracticed + onTogglePractice 参数

给 `HealthItemCard` composable 添加两个参数：
- `isPracticed: Boolean` — 今日是否已打卡
- `onTogglePractice: () -> Unit` — 打卡回调

打卡按钮放在收藏星标左侧（header 行右侧区域），用 `IconButton` + `Icons.Default.CheckCircle` / `Icons.Outlined.Circle`。

### D6: 统计条位置

放在搜索框和 Tab 之间（搜索框下方、Tab 上方），使用 `Surface(color = surfaceVariant)` 包裹。这样统计条始终可见，不会被列表滚动带走。

## Risks / Trade-offs

- **DataStore 写入竞争**: `togglePractice` 和 `toggleFavorite` 共用同一个 DataStore，但使用不同的 key，DataStore 内部保证原子性，无风险。
- **JSON 解析性能**: 90 天数据量很小，不会有性能问题。每次打卡都全量序列化/反序列化，但 JSON < 10KB，可忽略。
- **今日进度分母**: "今日 Y/N" 中 N 使用当前分类下的功法总数（`filteredItems.size` 不含搜索过滤），而非全局总数。这样更直观——用户看的是当前分类的进度。实际使用所有分类的总 item 数会更合理，但需跨分类统计。采用所有分类总 item 数。
