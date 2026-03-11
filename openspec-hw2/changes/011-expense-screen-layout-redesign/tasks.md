## 1. ViewModel 状态扩展

- [x] 1.1 新增 `periodOffset: MutableStateFlow<Int>` (默认 0)，暴露 `navigatePeriod(delta: Int)` 方法（+1/-1）
- [x] 1.2 切换 `selectedPeriod` 时自动重置 `periodOffset = 0` 和 `selectedTag = null`
- [x] 1.3 `refreshExpenses()` 中根据 `selectedPeriod + periodOffset` 计算 from/to 日期范围
- [x] 1.4 新增 `periodLabel: StateFlow<String>` derived state，根据粒度+offset 格式化导航标签（月: "2026年3月"，周: "3月3日 - 3月9日"，日: "3月8日 周六"）
- [x] 1.5 新增 `sortedTags: StateFlow<List<String>>` derived state，按当前 expenses 中 tag 出现频率降序排列
- [x] 1.6 新增 `filteredTotal: StateFlow<Double>` derived state，当前时间窗+分类筛选后的总额（从 filteredExpenses 求和）

## 2. TopAppBar 重构

- [x] 2.1 替换现有 TabRow 为 `SingleChoiceSegmentedButtonRow`（日/周/月），放入 TopAppBar actions 区域
- [x] 2.2 添加图表预留 `IconButton(BarChart)`，点击无操作
- [x] 2.3 移除现有 stats toggle 按钮和 StatsContent 饼图区域（被新布局替代）

## 3. 时间窗导航器

- [x] 3.1 新建 `PeriodNavigator` composable：◀ 按钮 + periodLabel 文本 + ▶ 按钮，居中排列
- [x] 3.2 ◀/▶ 点击调用 `viewModel.navigatePeriod(-1)` / `viewModel.navigatePeriod(+1)`

## 4. 总额摘要单行化

- [x] 4.1 移除现有 Summary Card（大卡片 + 环比），替换为单行 Row：左侧"总支出"灰色小字 + 右侧 filteredTotal 加粗金额

## 5. 分类筛选 Chips 改造

- [x] 5.1 将 chips 数据源从 `availableTags` 切换为 `sortedTags`（按频率排序）
- [x] 5.2 切换时间窗后分类重置为"全部"（已在 1.2 处理）

## 6. 列表布局调整

- [x] 6.1 重构 LazyColumn 结构：头部 items（PeriodNavigator + TotalSummary + TagChips）+ dayGroups 分组
- [x] 6.2 账单卡片增加分类标签 chip 显示（左下角小 chip）
- [x] 6.3 账单卡片右侧显示收据图标（仅有收据图片时显示 📷）
- [x] 6.4 空状态处理：无账单时显示提示文字

## 7. 手动测试验证

- [ ] 7.1 验证粒度切换（日→周→月）：时间窗重置、列表刷新、总额更新
- [ ] 7.2 验证时间窗导航（◀/▶）：标签格式正确、数据正确加载
- [ ] 7.3 验证分类筛选：chips 按频率排序、列表和总额联动过滤、切换时间窗后重置
- [ ] 7.4 验证现有功能不受影响：添加/编辑/删除/收据上传/批量扫描/Snackbar 撤销
