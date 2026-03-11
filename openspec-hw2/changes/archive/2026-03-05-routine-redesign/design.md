## Context

当前例行页面混用两套后端 API：
- **Routine API** (`/api/routines`): 每日打卡模型，有 `completed_today` + `toggle`，无频率概念
- **Review API** (`/api/reviews`): 定期审视模型，有 `frequency` + `due_status` + `paused` + `complete`

UI 通过 TabRow 将两者合并，但日 tab 和周/月/年 tab 的数据来源、操作方式、卡片样式完全不同。热力图/连续天数功能因缺少后端 history API 只能估算，实际价值低。

现有代码：
- `RoutineScreen.kt` (891行): 包含 DailyContent、PeriodicContent、HeatmapCard、RoutineDetailScreen、ReviewDetailScreen 等大量组件
- `RoutineViewModel.kt` (312行): 同时管理 Routine 和 Review 两套状态，含 streak/heatmap 估算逻辑
- 工具层: AddRoutineTool、CheckRoutineTool、RoutineStatsTool (Routine API) + AddReviewTool、QueryReviewsTool (Review API)
- 快捷栏: label="打卡"，route="routine"

## Goals / Non-Goals

**Goals:**
- 统一数据源: 全部使用 Review API，砍掉 Routine API 调用
- 简化 UI: 四个 tab 共用一套卡片和详情页组件
- 简化工具: 删除 Routine 系工具，复用已有 Review 系工具
- 减少代码量: 预计从 ~1200 行减至 ~500 行

**Non-Goals:**
- 不迁移用户已有 Routine 数据（后端脚本另行处理）
- 不支持 quarterly 频率（UI 不提供此选项，但 Review API 如返回 quarterly 数据在月 tab 下正常显示）
- 不新增 Review API 功能（如 frequency_config 的精细配置）
- 不改变导航路由（仍为 "routine"）

## Decisions

### D1: 全面切换到 Review API

**选择**: 全部使用 Review API，frequency="daily" 替代原 Routine。

**理由**: Review API 是 Routine 的超集 — 有 frequency、due_status、paused、category，而 Routine 只有 text + completed_today。统一到 Review 可以用一套模型/UI/工具覆盖所有频率。

**备选方案**: 保留 Routine API 用于每日项，Review 用于其他频率 → 拒绝，因为这正是当前的痛点（两套模型两套 UI）。

**影响**: NextApiService 中 Routine 相关方法（getRoutines、createRoutine、toggleRoutine、deleteRoutine）不再被调用，可保留但不引用。

### D2: 删除打卡游戏化组件

**选择**: 删除 HeatmapCard、RoutineSummaryCard、RoutineStreak、HeatmapDay、CheckCircleButton、estimateStreak、buildHeatmapData、computeMonthlyRate。

**理由**: 这些功能依赖后端 history API（不存在），当前只能估算"连续1天或0天"，信息量为零。Review 的 due_status 机制已足够表达"该做了"/"做完了"。

**备选方案**: 在客户端本地缓存打卡记录 → 拒绝，增加复杂度且与 Review 模型不一致。

### D3: 统一 UI 组件

**选择**: 所有 tab 共用同一套列表卡片 (`ReviewItemCard`) 和详情页 (`ReviewDetailScreen`)。删除 `DailyContent`、`RoutineDetailScreen`、`RoutineItemCard`、`AddRoutineDialog`。

**理由**: Review 数据模型统一，UI 自然统一。减少代码重复和维护成本。

**卡片显示**: 名称 + 频率标签 + 到期状态（颜色编码）+ 分类标签（如有）+ 暂停标记
**详情页**: 名称 + 频率 + 分类 + 到期状态 + 上次完成 + 创建时间 + 操作按钮（完成/暂停/恢复/删除）

### D4: 工具层精简

**选择**: 删除 `AddRoutineTool`、`CheckRoutineTool`、`RoutineStatsTool`。保留并增强 `AddReviewTool`、`QueryReviewsTool`。

**变更**:
- `AddReviewTool`: 在 description 和 frequency enum 中增加 "daily"，把描述从"定期回顾"改为"例行项"
- `QueryReviewsTool`: 增加 frequency 过滤参数，描述改为"查询例行项"
- Prompt: 更新工具说明，不再提及"习惯"/"打卡"，统一用"例行"

**理由**: 已有 Review 工具覆盖了 CRUD，只需微调描述和参数即可。

### D5: 快捷栏更新

**选择**:
- label 从 "打卡" 改为 "例行"
- category 从 "习惯" 改为 "例行"
- 删除独立的 "review" 快捷项（已合并到 routine）

**理由**: 概念统一后，"打卡"一词不再准确。

### D6: 月 tab 不合并 quarterly

**选择**: 月 tab 仅显示 frequency="monthly"，quarterly 单独归到月 tab 显示但不在添加对话框中提供 quarterly 选项。

**当前代码**: `FrequencyTab.MONTHLY -> state.reviews.filter { it.frequency == "monthly" || it.frequency == "quarterly" }`

**新行为**: `FrequencyTab.MONTHLY -> state.reviews.filter { it.frequency == "monthly" || it.frequency == "quarterly" }` — 保持不变，因为 quarterly 数据可能已存在于用户数据中，需要有地方展示。添加对话框不提供 quarterly 选项即可。

## Risks / Trade-offs

**[数据断裂]** 用户在 Routine API 中已有的每日习惯不会自动出现在 Review 列表中。
→ 缓解: App 首次更新后在例行页面显示一次性提示"请重新添加每日例行"；或后端提供迁移脚本。

**[每日频率的完成体验变化]** Routine 的 toggle 是即时切换 completed_today，Review 的 complete 是设置 last_completed 并重新计算 due_status。用户不能"取消完成"（Review 有 uncomplete API 但语义不同）。
→ 接受: Review 的 complete + due_status 机制更合理（"这件事到期了 → 做完了 → 下次再到期"），比 toggle 打卡更符合"例行"的本质。

**[快捷栏兼容]** 删除 "review" 路由后，已 pin "review" 的用户快捷栏会异常。
→ 缓解: ShortcutBarRepository 中 defaultShortcuts 移除 review 项，已保存的 pinnedRoutes 包含 "review" 时忽略（找不到对应 item 自动跳过）。

## Migration Plan

1. 修改 RoutineViewModel — 删除 Routine 相关状态和方法，全部走 Review API
2. 修改 RoutineScreen — 删除 Daily 专用组件，统一使用 Review 组件
3. 删除 AddRoutineTool、CheckRoutineTool、RoutineStatsTool
4. 修改 AddReviewTool、QueryReviewsTool — 增加 daily 频率，更新描述
5. 修改 AppModule — 移除已删工具注册
6. 修改 ErgouPrompt — 更新工具说明
7. 修改 ShortcutBarRepository — 更新 label/category，移除 review 项
8. 更新 feature-routine.md 设计文档

无 Room 迁移（Routine 数据在 Next 后端，本地无 routine 表）。
无破坏性 API 变更（NextApiService 中 Routine 方法保留但不调用）。
回滚: git revert 即可恢复。
