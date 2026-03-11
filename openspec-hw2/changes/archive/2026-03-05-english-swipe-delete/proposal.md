## Why

EnglishScreen 列表中每个场景卡片右侧有一个删除按钮，点击后直接删除，无确认、无撤销。后端是硬删除，误删不可恢复。需要改为"左滑 → 确认 → 延迟删除 → 可撤销"的安全交互模式。

## What Changes

- 移除 EnglishItem 中的删除 IconButton
- 用 SwipeToDismissBox 包裹 EnglishItem，支持左滑显示红色删除背景
- 左滑后弹出 AlertDialog 确认删除
- 确认后乐观更新（本地 pendingDeleteIds 过滤列表），显示 Snackbar 带"撤销"按钮
- Snackbar 超时后才真正调用 viewModel.deleteScenario()
- 撤销则恢复到列表

## Capabilities

### New Capabilities
- `english-swipe-delete`: EnglishScreen 的左滑确认延迟删除交互

### Modified Capabilities

（无）

## Impact

- 仅修改 `EnglishScreen.kt`，不涉及 ViewModel、数据层或其他文件
- 新增 SwipeToDismissBox 相关 Material 3 API 使用
