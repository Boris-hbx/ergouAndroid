## Why

快捷工具栏目前的固定(pin)列表硬编码为前 4 个（任务/例行/记账/出差），用户无法自行调整哪些功能固定、哪些释放。数据层 `ShortcutBarRepository` 已支持 `setPinnedRoutes()`，但 UI 没有任何入口让用户操作。需要一个轻量的编辑模式，让用户在快捷栏原地完成 pin/unpin 切换。

## What Changes

- 快捷栏新增"编辑模式"：长按任意 chip 进入，点击 chip 切换 pin/unpin 状态，点击栏外区域退出并保存
- 编辑模式下 chip 显示 PushPin 图标区分状态：竖直(pinned) / 旋转 45°(unpinned)
- 编辑模式下点击 chip 不再触发页面跳转，仅切换 pin 状态
- 新 pin 的条目追加到 pinned 列表末尾（不做拖拽排序）
- 退出编辑模式时自动保存到 DataStore

## Capabilities

### New Capabilities
- `shortcut-pin-edit`: 快捷栏编辑模式 — 长按进入、pin/unpin 切换、PushPin 图标状态、点击外部退出

### Modified Capabilities

（无现有 spec 需要修改）

## Impact

- **UI**: `ChatScreen.kt` — `ShortcutBar` composable 增加编辑模式状态和交互
- **ViewModel**: `ChatViewModel` — 新增 `togglePin(route)` 方法
- **Repository**: `ShortcutBarRepository` — 无接口变更，已有 `setPinnedRoutes()`
- **依赖**: 无新依赖，使用 Material Icons 内置的 `PushPin` 图标 + `Modifier.graphicsLayer` 旋转
