## Context

RoutineScreen.kt 当前使用 AlertDialog 确认删除，确认后直接调用 viewModel.deleteReview()。后端是硬删除（DELETE /api/reviews/:id，无 restore）。需要增加左滑手势入口和 Snackbar 撤销保护。

限制：仅修改 RoutineScreen.kt，不改 ViewModel。

## Goals / Non-Goals

**Goals:**
- 列表项支持左滑触发删除
- 删除前有确认对话框
- 确认后 UI 立即响应 + Snackbar 撤销窗口
- 详情页删除也走延迟流程

**Non-Goals:**
- 不修改 ViewModel 或后端 API
- 不实现服务端软删除/恢复

## Decisions

### D1: 状态管理在 RoutineScreen 顶层

在 RoutineScreen composable 顶层维护：
- `pendingDeleteIds: Set<String>` — 已确认删除但未真正执行的 ID
- `confirmDeleteItem: NextReview?` — 当前待确认的项（触发 AlertDialog）

列表展示时过滤 `pendingDeleteIds`。这样不需要修改 ViewModel。

### D2: SwipeToDismissBox confirmValueChange 返回 false

左滑触发时 `confirmValueChange` 返回 false（不实际 dismiss），仅设置 `confirmDeleteItem` 触发对话框。这样取消时不需要手动重置滑动状态。

### D3: Snackbar 协程控制删除时机

确认后启动协程调用 `snackbarHostState.showSnackbar()`，根据返回值：
- `ActionPerformed` → 撤销，从 pendingDeleteIds 移除
- `Dismissed` → 调用 `viewModel.deleteReview(id)`，从 pendingDeleteIds 移除

### D4: 详情页删除返回列表再延迟

详情页确认删除后：先 `selectReview(null)` 返回列表，再加入 pendingDeleteIds + 显示 Snackbar。

## Risks / Trade-offs

**[Snackbar 被新 Snackbar 挤掉]** 快速连续删除多项时，新 Snackbar 会替换旧的，旧的回调收到 Dismissed，导致提前执行真删除。
→ 接受：这是 Material 3 Snackbar 的默认行为，用户快速连删说明有意删除。
