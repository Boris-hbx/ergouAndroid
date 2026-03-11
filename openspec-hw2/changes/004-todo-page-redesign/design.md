## Context

当前 `TaskScreen.kt` 使用简单 Tab + LazyColumn + Card 布局，所有任务（完成/未完成）混在一起展示。API 层（NextApiService）支持 today/week/month 三个 Tab 过滤，数据模型包含 progress、tags、dueDate 等字段。

用户明确：**不需要四象限**，但**保留进度条**。

设计稿要求：Tab 带计数、已完成折叠区、紧凑卡片、简化添加。

## Goals / Non-Goals

**Goals:**
- Tab 栏带未完成任务计数，标签改为"今天/本周/30天"
- 任务分组：未完成在上，已完成折叠在下
- 任务卡片：勾选框 + 标题 + 进度条（有进度时） + 截止日期
- 添加对话框简化：默认只输标题，高级选项可展开
- 去掉四象限相关 UI

**Non-Goals:**
- 不改 API 接口和 DTO
- 不做拖拽排序
- 不做搜索/过滤功能

## Decisions

### D1: ViewModel 拆分已完成/未完成列表

**决策**：在 `TaskUiState` 中将 `todos` 拆为 `pendingTodos` 和 `completedTodos` 两个列表，刷新时在客户端分组。同时维护各 Tab 的 `pendingCount` 用于 Tab 计数。

**理由**：API 返回的数据已包含 completed 字段，客户端分组零成本。各 Tab 计数需要额外请求（或一次性拉取三个 Tab 的数据），第一版先只显示当前 Tab 的计数。

### D2: 去掉四象限，保留进度条

**决策**：移除四象限选择器（FilterChip）和卡片中的四象限颜色标签。保留进度条显示（当 progress > 0 时）。

**理由**：用户明确不需要四象限。进度条对任务跟踪有价值。

### D3: 已完成折叠区 — AnimatedVisibility

**决策**：用 `AnimatedVisibility` 实现已完成区域的展开/收起动画。折叠状态用 `remember { mutableStateOf(false) }` 管理。

### D4: 添加对话框简化 — 默认折叠高级选项

**决策**：保留现有 `AddTaskDialog`，但调整布局：
- 标题输入框始终可见
- "更多选项"按钮控制高级区域展开（描述、标签、截止日期）
- 去掉四象限选择器
- 默认收起高级选项

### D5: Tab 计数 — 当前 Tab 显示实际计数

**决策**：当前选中 Tab 显示 `pendingTodos.size` 作为计数，其他 Tab 不显示计数（避免额外 API 请求）。

**备选**：初始加载时请求三个 Tab 的数据 → 增加 3 倍网络请求，第一版不做。

## Risks / Trade-offs

- **[只显示当前 Tab 计数]** → 其他 Tab 没有计数，体验稍差。后续可优化为并行加载三个 Tab 数据。
- **[进度条占垂直空间]** → 仅在 progress > 0 时显示，大多数任务不会有进度，影响小。
