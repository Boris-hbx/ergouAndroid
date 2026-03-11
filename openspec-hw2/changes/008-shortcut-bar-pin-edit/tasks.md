## 1. Repository 层补充

- [x] 1.1 `ShortcutBarRepository` 新增 `suspend fun getPinnedRoutes(): List<String>`，从 DataStore 读取当前 pinned 列表（无数据时返回 `defaultPinnedRoutes`）

## 2. ViewModel 层

- [x] 2.1 `ChatViewModel` 新增 `_isEditingShortcuts: MutableStateFlow<Boolean>`，对外暴露 `isEditingShortcuts: StateFlow<Boolean>`
- [x] 2.2 `ChatViewModel` 新增 `enterShortcutEditMode()` — 设为 true，记录 `pinnedSnapshot`
- [x] 2.3 `ChatViewModel` 新增 `exitShortcutEditMode()` — 比较当前 pinned 与 snapshot，有变化才写 DataStore，设为 false
- [x] 2.4 `ChatViewModel` 新增 `togglePin(route: String)` — 读取当前 pinned 列表，切换该 route 的 pin 状态，pin 时检查上限（MAX_PINNED = 4），立即写入 DataStore

## 3. ShortcutBar UI 改造

- [x] 3.1 `ShortcutBar` composable 新增参数：`isEditing: Boolean`、`onLongPress: () -> Unit`、`onTogglePin: (String) -> Unit`
- [x] 3.2 正常模式：保持现有 `AssistChip` + 点击跳转行为不变
- [x] 3.3 编辑模式：替换为 `FilterChip`，`selected = item.pinned`，`leadingIcon` 显示 PushPin 图标（pinned 0° / unpinned 45° 旋转），点击调用 `onTogglePin`；pinned 数已达 4 个时 unpinned chip 置灰（`enabled = false`）
- [x] 3.4 chip 长按事件：使用 `Modifier.combinedClickable(onLongClick = onLongPress)` 进入编辑模式
- [x] 3.5 PushPin 旋转动画：使用 `animateFloatAsState` + `graphicsLayer { rotationZ }` 实现平滑过渡

## 4. 外部点击退出

- [x] 4.1 `ChatScreen` 中编辑模式下，在消息列表区域上方覆盖透明 `Box(Modifier.fillMaxSize().clickable { exitEditMode() })`
- [x] 4.2 确保透明遮罩只拦截 tap，不影响消息列表滚动（使用 `clickable` 而非 `pointerInput`）

## 5. 触觉反馈

- [x] 5.1 进入编辑模式时调用 `view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)`

## 6. 手动测试

- [ ] 6.1 长按 chip → 进入编辑模式（振动反馈、图标出现）
- [ ] 6.2 点击 pinned chip → unpin（图标旋转、chip 位置变化）
- [ ] 6.3 点击 unpinned chip → pin（追加到 pinned 末尾）
- [ ] 6.4 点击栏外区域 → 退出编辑模式，重启 app 验证 pinned 持久化
- [ ] 6.5 编辑模式下不触发页面跳转
- [ ] 6.6 深色模式下 FilterChip + PushPin 图标显示正常
- [ ] 6.7 屏幕旋转后编辑模式状态保持
