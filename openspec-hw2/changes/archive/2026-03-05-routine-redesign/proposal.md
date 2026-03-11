## Why

当前"例行"页面把后端两套独立概念（Routine = 每日打卡, Review = 定期审视）硬塞进同一个 TabRow，导致：
1. **概念割裂**: 日 tab 用 Routine API（toggle 打卡），周/月/年 tab 用 Review API（complete 标记），数据模型、操作方式完全不同
2. **功能冗余**: 热力图、连续天数、完成率等打卡游戏化功能缺少后端 history API 支持，实际只能估算显示，价值低
3. **用户认知混乱**: "例行"和"习惯"两个词混用，添加对话框在不同 tab 下弹不同表单（AddRoutineDialog vs AddReviewDialog），用户无法建立一致心智模型

**目标**: 将"例行"重新定义为**按频率重复的事项提醒**（每日/每周/每月/每年），统一使用 Review API，砍掉打卡游戏化，回归简洁实用。

## What Changes

- **BREAKING** 砍掉 Routine API 的使用 — 不再调用 `/api/routines` 系列接口，全部迁移到 Review API (`/api/reviews`)
- **BREAKING** 删除热力图 (HeatmapCard)、连续天数 (RoutineStreak)、完成率 (monthlyCompletionRate)、每日汇总卡片 (RoutineSummaryCard)
- **BREAKING** 删除 RoutineDetailScreen — 不再有独立的"每日例行"详情视图
- 统一数据模型: 所有例行项使用 NextReview（含 frequency, due_status, paused, category 等字段）
- "每日"频率 = Review frequency="daily"，与周/月/年一致
- UI 简化: 列表页按频率 tab 分组，每项显示名称 + 到期状态 + 分类标签
- 添加对话框统一为一个（文本 + 频率选择 + 可选分类），默认频率跟随当前 tab
- 详情页统一: 所有频率共用一个详情视图，支持完成/暂停/删除
- 工具层: add_routine / check_routine / routine_stats → 替换为调用 Review 相关工具
- 快捷栏: "例行 M/N" → 改为 "例行: N 项到期"（显示 overdue + due_today + due_soon 数量）

## Capabilities

### New Capabilities
- `routine-unified`: 统一的例行功能 — 基于 Review API 的四频率（日/周/月/年）例行管理，包含列表/详情/添加/完成/暂停/删除

### Modified Capabilities
（无现有 spec 需要修改）

## Impact

### 代码变更
- `ui/routine/RoutineScreen.kt` — 大幅重写，删除 DailyContent/HeatmapCard/RoutineSummaryCard/RoutineItemCard/RoutineDetailScreen/AddRoutineDialog/CheckCircleButton，保留并统一 PeriodicContent/ReviewItemCard/ReviewDetailScreen/AddReviewDialog
- `ui/routine/RoutineViewModel.kt` — 大幅简化，删除 Routine 相关状态和方法（toggleRoutine/addRoutine/estimateStreak/buildHeatmapData 等），RoutineStreak/HeatmapDay 数据类移除
- `data/tool/tools/AddRoutineTool.kt` — 删除或改为调用 Review API
- `data/tool/tools/CheckRoutineTool.kt` — 删除或改为查询 Review
- `data/tool/tools/RoutineStatsTool.kt` — 删除（Review 自带 due_status）
- `di/AppModule.kt` — 更新工具注册
- `data/remote/ErgouPrompt.kt` — 更新工具说明
- `data/repository/ShortcutBarRepository.kt` — 更新快捷栏显示逻辑

### API 依赖
- 不再依赖: `GET/POST/DELETE /api/routines`, `POST /api/routines/:id/toggle`
- 继续使用: `GET/POST/PUT/DELETE /api/reviews`, `POST /api/reviews/:id/complete`, `POST /api/reviews/:id/uncomplete`

### 数据迁移
- 用户现有 Routine 数据需要手动迁移到 Review（通过后端脚本或引导用户重新创建）
- App 端无需 Room 迁移（Routine 数据已在 Next 后端）
