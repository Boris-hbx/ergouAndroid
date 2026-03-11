## Why

当前记账页面顶部饼图占空间大、列表无分组、缺少分类筛选，浏览效率低。参照竞品布局（见 `layout-mockup.md`），重新设计为"时间粒度切换 + 时间窗导航 + 分类筛选 + 日期分组列表"的紧凑布局，提升日常记账的查看体验。

## What Changes

- TopAppBar 改为：返回按钮 + "记账"标题 + 日/周/月 SegmentedButton + 图表预留按钮
- 新增时间窗导航器（◀ ▶），根据日/周/月粒度切换对应时间段
- 去掉饼图，改为单行总额摘要（"总支出 CA$xxx"），联动时间窗+分类筛选
- 新增横向 FilterChip 分类筛选（全部 + 动态分类），筛选后列表和总额同步更新
- 账单列表按日期分组，每组显示日期+小计 header
- 账单条目卡片增加分类标签 chip 和收据图标
- 保留现有：添加/编辑 Dialog、SwipeToDismiss 删除、Snackbar 撤销、数据层不变

## Capabilities

### New Capabilities
- `expense-list-layout`: 记账页面布局重设计 — 时间粒度切换、时间窗导航、单行总额、分类筛选、日期分组列表

### Modified Capabilities
（无，本次仅 UI 布局调整，不改变数据层行为）

## Impact

- **UI 层**: `ExpenseScreen.kt` 大幅重写（布局结构变化）
- **ViewModel**: `ExpenseViewModel` 新增状态：时间粒度(日/周/月)、当前时间窗、选中分类；新增分组/筛选/汇总的 derived state
- **无后端变更**: NextApiService 接口不变，仅前端对返回数据做分组和筛选
- **无新依赖**: 使用现有 Material 3 组件（SegmentedButton/FilterChip/LazyColumn）
