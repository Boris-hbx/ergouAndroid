## Why

例行列表项的删除操作藏在详情页右上角，交互路径长（点击→进详情→点删除→确认），且后端是硬删除无 restore API，误删无法恢复。需要更直觉的删除入口和撤销保护。

## What Changes

- 列表项增加左滑删除手势（SwipeToDismissBox，endToStart 方向）
- 左滑触发 AlertDialog 确认，取消则重置滑动状态
- 确认后采用"客户端延迟删除"模式：UI 立即隐藏 + Snackbar 撤销 → Snackbar 消失后才真正调用后端删除
- 详情页删除按钮也走同样的延迟删除流程

## Capabilities

### New Capabilities
- `swipe-delete`: 左滑确认延迟删除交互，包含 SwipeToDismissBox + AlertDialog 确认 + pendingDelete 状态管理 + Snackbar 撤销

### Modified Capabilities
（无）

## Impact

- 仅修改 `ui/routine/RoutineScreen.kt`
- 不修改 ViewModel、不修改后端 API 调用方式
- 新增 Material 3 的 SwipeToDismissBox 相关 import
