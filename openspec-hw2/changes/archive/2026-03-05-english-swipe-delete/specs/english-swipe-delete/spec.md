## ADDED Requirements

### Requirement: 移除删除按钮
EnglishItem 组件 SHALL 移除删除 IconButton（onDelete 参数），保留归档、AI生成、重试按钮不变。

#### Scenario: EnglishItem 无删除按钮
- **WHEN** EnglishItem 渲染
- **THEN** 卡片右侧不显示删除图标按钮

---

### Requirement: SwipeToDismissBox 左滑交互
每个 EnglishItem SHALL 被 SwipeToDismissBox 包裹，支持 endToStart 方向左滑，背景为红色 + 删除图标。confirmValueChange SHALL 返回 false 阻止自动 dismiss，改为手动触发确认流程。

#### Scenario: 左滑显示删除背景
- **WHEN** 用户左滑场景卡片
- **THEN** 卡片滑动露出红色背景和白色删除图标

#### Scenario: 松手触发确认
- **WHEN** 用户左滑到 EndToStart 位置松手
- **THEN** SwipeToDismissBox 不自动 dismiss，弹出确认对话框

---

### Requirement: 确认删除对话框
左滑松手后 SHALL 弹出 AlertDialog，标题"确认删除"，正文"确定要删除「${title}」吗？此操作不可恢复。"，按钮为"删除"（红色）和"取消"。

#### Scenario: 点击取消
- **WHEN** 用户点击"取消"
- **THEN** 对话框关闭，SwipeToDismissBox 重置到 Settled 状态

#### Scenario: 点击删除
- **WHEN** 用户点击"删除"
- **THEN** 场景 ID 加入 pendingDeleteIds，列表乐观更新移除该项

---

### Requirement: Snackbar 延迟删除与撤销
确认删除后 SHALL 显示 Snackbar "已删除" + "撤销"按钮。超时后调用 viewModel.deleteScenario(id) 真正删除；点击"撤销"则从 pendingDeleteIds 移除，恢复到列表。

#### Scenario: Snackbar 超时执行删除
- **WHEN** Snackbar 显示后超时（默认 duration）
- **THEN** 调用 viewModel.deleteScenario(id) 执行真正删除

#### Scenario: 用户点击撤销
- **WHEN** 用户在 Snackbar 超时前点击"撤销"
- **THEN** 场景 ID 从 pendingDeleteIds 移除，场景恢复显示在列表中，不调用后端删除
