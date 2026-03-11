## Context

当前 ExpenseScreen 已具备：期间切换（TabRow: TODAY/WEEK/MONTH）、标签筛选（FilterChip）、按日期分组列表（dayGroups）、饼图统计（StatsContent）。本次重设计不新增功能，仅重新排列 UI 布局并补充时间窗导航能力。

**现有可复用的 ViewModel 状态：**
- `selectedPeriod: ExpensePeriod` — 已有日/周/月枚举
- `selectedTag: String?` — 已有标签筛选
- `dayGroups: List<ExpenseDayGroup>` — 已有日期分组 + 小计
- `summary: NextExpenseSummary?` — 已有期间总额
- `availableTags: List<String>` — 已有标签列表

**缺失的能力：**
- 时间窗导航（◀ ▶ 切换上/下一个时段）— 当前 period 只选类型，不能前后翻页
- 分类 chips 按频率排序 — 当前固定顺序
- TopAppBar 改为 SegmentedButton 样式

## Goals / Non-Goals

**Goals:**
- 重新排列 ExpenseScreen 布局，匹配目标 mockup（见 layout-mockup.md）
- 新增时间窗前后导航（◀ ▶）
- 分类 chips 改为按频率降序
- 去掉饼图区域，总额改为单行显示
- 保持所有现有功能不变（添加/编辑/删除/收据/批量扫描）

**Non-Goals:**
- 不实现图表按钮功能（仅占位）
- 不改动数据层（NextApiService 接口不变）
- 不改动 AddExpenseDialog
- 不新增依赖

## Decisions

### 1. 时间窗导航状态：在 ViewModel 中用日期偏移量

**方案**: 在 ExpenseViewModel 新增 `periodOffset: Int` 状态（默认 0 = 当前时段）。点击 ◀ 则 offset--，点击 ▶ 则 offset++。`refreshExpenses()` 根据 `selectedPeriod + periodOffset` 计算 from/to 日期范围。

**替代方案**: 存具体的起止日期对。但 offset 更简洁，切换粒度时重置为 0 即可。

**导航标签格式化**: 新增 `periodLabel: String` derived state，根据粒度+offset 计算：
- 月: `"2026年3月"` — `YearMonth.now().plusMonths(offset)`
- 周: `"3月3日 - 3月9日"` — 当前周 +/- offset 周
- 日: `"3月8日 周六"` — 今天 +/- offset 天

### 2. 粒度切换：SegmentedButton 替代 TabRow

**方案**: 使用 Material 3 `SingleChoiceSegmentedButtonRow` 替代现有 `TabRow`。放在 TopAppBar 的 actions 区域。

**理由**: SegmentedButton 更紧凑，适合放在 AppBar 里；TabRow 占整行宽度，不适合与标题并排。

### 3. 分类 chips 按频率排序

**方案**: 在 ViewModel 的 `dayGroups` 或 `expenses` 计算时，统计各 tag 出现次数，生成 `sortedTags: List<String>` derived state。UI 层直接用这个排好序的列表渲染 chips。

**实现**: `expenses.groupingBy { it.tags.firstOrNull() ?: "其他" }.eachCount().entries.sortedByDescending { it.value }.map { it.key }`

### 4. 总额显示：复用现有 summary

**方案**: 现有 `summary?.totalAmount` 已经是当前期间总额。筛选分类后，从 `filteredExpenses` 手动求和即可（`filteredExpenses.sumOf { it.amount }`），不需要额外 API 调用。

### 5. 切换粒度时重置状态

切换粒度时：`periodOffset = 0`，`selectedTag = null`。保证用户始终从"当前时段 + 全部分类"开始。

### 6. ExpenseScreen 布局结构

```
Scaffold(
  topBar = {
    TopAppBar(
      navigationIcon = { BackButton },
      title = { "记账" },
      actions = {
        SegmentedButtonRow(日/周/月),
        IconButton(BarChart) // 预留
      }
    )
  },
  floatingActionButton = { FAB(+) }
) {
  LazyColumn {
    item { PeriodNavigator(◀ label ▶) }
    item { TotalSummaryRow("总支出", amount) }
    item { TagFilterChips(sortedTags) }
    // 按日期分组
    dayGroups.forEach { group ->
      stickyHeader { DayGroupHeader(date, subtotal) }
      items(group.expenses) { ExpenseCard(it) }
    }
    // 空状态
    if (empty) item { EmptyState() }
  }
}
```

关键点：将 PeriodNavigator / TotalSummary / TagChips 作为 LazyColumn 的头部 items，这样整个页面可以统一滚动，不需要嵌套 scrollable。

## Risks / Trade-offs

- **[SegmentedButton in TopAppBar 空间紧张]** → 日/周/月只有三个短字，实测应该放得下。如果放不下，降级为 TopAppBar 下方独立行。
- **[切换粒度时 offset 重置丢失浏览位置]** → 可接受，用户切换维度通常期望看当前时段。
- **[分类筛选后总额客户端计算]** → 数据量小（单月账单通常 < 100 条），客户端求和无性能问题。
- **[stickyHeader 在 Material 3 LazyColumn 中需要 ExperimentalFoundationApi]** → 已在项目中使用，无额外风险。如果不想用 stickyHeader，可以退化为普通 item。
