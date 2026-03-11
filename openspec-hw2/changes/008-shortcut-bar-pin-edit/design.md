## Context

快捷栏 `ShortcutBar` 是 `ChatScreen` 底部输入框上方的 `LazyRow`，展示 6 个功能入口 chip。当前 `ShortcutBarRepository` 已支持 pinned 列表的读写（DataStore），但 UI 层没有编辑入口。需要在 `ShortcutBar` composable 内部增加编辑模式。

相关文件：
- `ShortcutBarRepository.kt` — 数据层，已有 `setPinnedRoutes()`
- `ChatScreen.kt` — `ShortcutBar` composable
- `ChatViewModel.kt` — 持有 `shortcutBarRepository`

## Goals / Non-Goals

**Goals:**
- 用户可通过长按进入编辑模式，点击 chip 切换 pin/unpin
- 使用 PushPin 图标（旋转区分状态），视觉简洁
- 点击栏外区域退出并保存

**Non-Goals:**
- 拖拽排序（后续可加，当前 pin 顺序为追加到末尾）
- 快捷栏条目的增删（固定 6 个功能）
- 设置页中的快捷栏管理 UI

## Decisions

### 1. 编辑状态放在 ChatViewModel 而非 ShortcutBar 内部

**选择**: `ChatViewModel` 持有 `isEditingShortcuts: MutableStateFlow<Boolean>`

**理由**: 编辑模式需要在配置变更（屏幕旋转）后恢复。ViewModel 的 StateFlow 天然支持这一点，而 composable 内部的 `remember` 在 Activity 重建后会丢失。

**替代方案**: `rememberSaveable` — 可行但不如 ViewModel 统一管理状态。

### 2. 外部点击退出使用透明遮罩层

**选择**: 编辑模式下在 `ShortcutBar` 上方叠加一个全屏透明 `Box`（`Modifier.clickable`），点击即退出编辑模式。

**理由**: 比 `pointerInput` 全局拦截更简单可靠，与 DropdownMenu 的 dismiss 机制一致。遮罩层不会阻挡对话列表的滚动（因为点击后立即退出编辑模式）。

**实现**: 在 ChatScreen 的 Scaffold content 中，编辑模式下在消息列表上方覆盖透明 Box。

### 3. PushPin 图标使用 graphicsLayer 旋转

**选择**: `Icons.Filled.PushPin` + `Modifier.graphicsLayer { rotationZ = if (pinned) 0f else 45f }`

**理由**: Material Icons 内置 PushPin 图标，不需要额外依赖。旋转变换用 `graphicsLayer` 性能最优（不触发 recomposition）。可加 `animateFloatAsState` 做平滑过渡。

### 4. 编辑模式下 chip 样式区分

**选择**:
- pinned chip: `FilterChip(selected = true)` + 前置 PushPin 图标（0°）
- unpinned chip: `FilterChip(selected = false)` + 前置 PushPin 图标（45°）
- 正常模式: 保持现有 `AssistChip` 不变

**理由**: `FilterChip` 的 selected/unselected 样式自带 Material 3 的填充色/描边区分，语义上也契合"筛选/切换"操作。

### 5. togglePin 逻辑

**实现**:
```kotlin
companion object {
    const val MAX_PINNED = 4
}

fun togglePin(route: String) {
    viewModelScope.launch {
        val current = shortcutBarRepository.getPinnedRoutes()
        val newPinned = if (route in current) {
            current - route
        } else {
            if (current.size >= MAX_PINNED) return@launch // 已达上限，不操作
            current + route
        }
        shortcutBarRepository.setPinnedRoutes(newPinned)
    }
}
```

`ShortcutBarRepository` 需要新增 `getPinnedRoutes(): List<String>` 挂起函数（从 DataStore 读取当前 pinned 列表）。

达到上限时，unpinned chip 视觉置灰（`FilterChip(enabled = false)`），用户点击无响应，无需额外提示。

### 6. 脏检查避免无效写入

退出编辑模式时，比较当前 pinned 列表与进入时的快照。无变化则不写 DataStore。通过在进入编辑模式时记录 `pinnedSnapshot` 实现。

## Risks / Trade-offs

- [透明遮罩层可能拦截滚动手势] → 遮罩层只拦截 tap（`clickable`），不拦截 scroll。Compose 事件分发中 `clickable` 不消费滑动事件。
- [编辑模式下 LazyRow 布局可能跳动] → 使用 `AnimatedContent` 或 `Crossfade` 在 AssistChip 和 FilterChip 之间平滑切换，保持 chip 尺寸一致。
- [PushPin 图标在小尺寸下可能不够清晰] → 图标 16dp + FilterChip 的 selected 填充色双重暗示，即使图标不清晰也能通过颜色区分。
