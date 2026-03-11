## 1. ViewModel 重写

- [x] 1.1 删除 RoutineStreak、HeatmapDay 数据类，从 RoutineUiState 中移除 streaks、heatmapDays、monthlyCompletionRate、selectedRoutine 字段
- [x] 1.2 删除 Routine 相关方法：addRoutine、toggleRoutine、deleteRoutine、selectRoutine、estimateStreak、buildHeatmapData、computeMonthlyRate
- [x] 1.3 修改 refreshAll()：只调用 getReviews()，不再调用 getRoutines()
- [x] 1.4 修改 filteredReviews()：DAILY tab 返回 frequency="daily" 的项

## 2. Screen 重写

- [x] 2.1 删除 DailyContent、HeatmapCard、heatmapColor、RoutineSummaryCard、RoutineItemCard、RoutineDetailScreen、CheckCircleButton、AddRoutineDialog 组件
- [x] 2.2 修改主 RoutineScreen：日 tab 也使用 PeriodicContent（所有 tab 统一走 ReviewItemCard 列表）
- [x] 2.3 修改添加对话框：所有 tab 统一使用 AddReviewDialog，frequency enum 增加 "daily"/"每日" 选项，默认频率跟随当前 tab
- [x] 2.4 修改列表中删除操作：从卡片移到详情页（通过 ReviewDetailScreen 的删除按钮触发）

## 3. 工具层精简

- [x] 3.1 删除 AddRoutineTool.kt、CheckRoutineTool.kt、RoutineStatsTool.kt
- [x] 3.2 修改 AddReviewTool：description 改为"创建例行项"，frequency enum 增加 "daily"，默认 frequency 改为 "daily"
- [x] 3.3 修改 QueryReviewsTool：description 改为"查询例行项列表"，增加 frequency 可选参数用于筛选
- [x] 3.4 修改 AppModule.kt：移除 AddRoutineTool、CheckRoutineTool 注册（RoutineStatsTool 如已注册也移除）

## 4. Prompt 和快捷栏

- [x] 4.1 修改 ErgouPrompt.kt：工具说明中移除 add_routine/check_routine/routine_stats，更新 add_review/query_reviews 的描述为"例行"相关
- [x] 4.2 修改 ShortcutBarRepository.kt：route="routine" 的 label 改为"例行"、category 改为"例行"；删除 route="review" 的 ShortcutItem

## 5. 文档更新

- [x] 5.1 更新 docs/design/feature-routine.md：反映新设计（统一 Review API、无打卡游戏化）
- [x] 5.2 更新 docs/design/next-gaps-routine.md：标记已解决的 GAP（通过统一到 Review 解决频率和打卡问题）

## 6. 手动测试

- [ ] 6.1 验证四个 tab（日/周/月/年）正确筛选显示 Review 数据
- [ ] 6.2 验证添加对话框：各 tab 下默认频率正确，创建成功后刷新列表
- [ ] 6.3 验证详情页：完成/暂停/恢复/删除操作正常
- [ ] 6.4 验证二狗对话：通过对话添加/查询例行项，工具调用正常
- [ ] 6.5 验证快捷栏显示"例行"，点击进入正确页面
- [ ] 6.6 验证未登录状态：显示登录引导
- [ ] 6.7 验证网络错误：Snackbar 正常弹出，不崩溃
