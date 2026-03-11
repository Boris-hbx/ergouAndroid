## Context

EnglishScreen.kt 当前有直接删除按钮，需改为左滑确认延迟删除模式。此模式已在项目中（RoutineScreen）验证过，直接复用。

## Goals / Non-Goals

**Goals:**
- 实现 SwipeToDismissBox + AlertDialog + Snackbar 延迟删除
- 仅修改 EnglishScreen.kt

**Non-Goals:**
- 不修改 EnglishViewModel.kt 或其他文件
- 不改变后端删除逻辑

## Decisions

### 1. 客户端延迟删除模式
使用 pendingDeleteIds: Set<String> 本地 state 过滤列表实现乐观更新，Snackbar 超时后才调用 viewModel.deleteScenario()。

**理由**：后端硬删除无法撤销，客户端延迟是唯一方案。与 RoutineScreen 模式一致。

### 2. confirmValueChange 返回 false
SwipeToDismissBox 的 confirmValueChange 在检测到 EndToStart 时记录 confirmDeleteItem 但返回 false，阻止自动 dismiss。由 AlertDialog 结果驱动后续流程。

**理由**：需要先弹确认对话框，不能直接 dismiss。

### 3. 移除 onDelete 参数
EnglishItem 的 onDelete 参数移除，删除功能完全由外层 SwipeToDismissBox 承载。

## Risks / Trade-offs

- [风险] SwipeToDismissBox state 与 LazyColumn key 不匹配导致动画异常 → items 已使用 key = { it.id }
- [权衡] 用户快速连续删除多个时 Snackbar 会被覆盖，前一个直接执行删除 → 可接受行为
