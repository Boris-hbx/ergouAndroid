## Why

TaskScreen 的两个交互需要优化：1) 左滑删除没有确认步骤，容易误删；2) 单击任务无响应，用户无法快速查看/编辑任务详情，只能长按。

## What Changes

- 删除确认：左滑松手后弹出 AlertDialog 确认，确认后才执行删除 + Snackbar 撤销
- 任务详情 BottomSheet：单击任务弹出 ModalBottomSheet，显示完整详情并支持内联编辑

## Capabilities

### New Capabilities
- `swipe-delete-confirm`: 左滑删除增加确认对话框
- `task-detail-bottomsheet`: 单击任务弹出详情 BottomSheet，支持内联编辑

### Modified Capabilities

## Impact

- **仅修改** `TaskScreen.kt`，不涉及 ViewModel/Repository/API 层
- 使用已有的 `viewModel.updateTodo()` 和 `viewModel.deleteTodo()` 方法
