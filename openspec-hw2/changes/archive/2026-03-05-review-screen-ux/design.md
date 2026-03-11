## Context

ReviewScreen.kt 需要两个 UI 交互优化，只修改这一个文件，不改 ViewModel。

## Goals / Non-Goals

**Goals:**
- SwipeToDismissBox + 确认对话框 + 乐观删除 + Snackbar 撤销
- 单击展开详情 (AnimatedVisibility)

**Non-Goals:**
- 不修改 ReviewViewModel 或任何其他文件

## Decisions

### 1. 删除流程：pendingDeleteIds + Snackbar 延迟

使用 `pendingDeleteIds: Set<String>` 本地状态过滤列表实现乐观删除。确认后加入 set，Snackbar 超时后调用 `viewModel.deleteReview()`，撤销则从 set 移除。

### 2. 展开状态：expandedIds: Set<String>

在 ReviewScreen composable 顶层维护 `expandedIds` state，传递 `isExpanded` 和 toggle 回调给 ReviewItem。

### 3. SwipeToDismissBox 重置

取消删除时需要 `reset()` SwipeToDismissBox。使用 `rememberSwipeToDismissBoxState` + `LaunchedEffect` 监听 `currentValue` 变化触发确认对话框，取消时调用 `snapTo(Default)`。

## Risks / Trade-offs

- SwipeToDismissBox 在 Material 3 中是 ExperimentalMaterial3Api，但项目已使用该注解。
