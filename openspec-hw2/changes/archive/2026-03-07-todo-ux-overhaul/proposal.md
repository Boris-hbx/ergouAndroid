## Why

待办事项（TaskScreen）存在多个交互体验问题，严重影响日常使用：
1. 详情页点击标题/内容进入编辑模式的交互不直观，用户不知道可以点击编辑
2. 列表中的 LinearProgressIndicator 视觉效果差，详情页的 Slider 带 steps 刻度不美观且操作精度低
3. 进度滑到 100% 时直接自动完成，没有确认步骤，容易误操作
4. 添加/编辑对话框中"更多选项"展开使用 AnimatedVisibility 但过渡不平滑，有卡顿感
5. 每次操作（完成、删除、更新进度等）都调用 `refreshTodos()` 全量刷新列表，导致整个列表闪烁重建，体验极差

## What Changes

- **进度条重新设计**：列表中的 LinearProgressIndicator 替换为更美观的自定义圆形或分段进度指示器；详情页的 Slider 去掉 steps 刻度，改为连续滑动 + 百分比显示
- **100% 完成确认**：进度滑到 100% 时弹出确认对话框，用户确认后才标记完成
- **详情页编辑交互优化**：标题和描述区域增加明确的编辑提示（如铅笔图标或"点击编辑"提示），让用户知道可以点击进入编辑模式
- **展开动画优化**：对话框中"更多选项"的展开/收起使用 `animateContentSize()` 替代 `AnimatedVisibility`，实现平滑过渡
- **乐观更新（Optimistic Update）**：操作后立即在本地更新 UI 状态，API 请求在后台执行；失败时回滚并提示错误。消除全量刷新导致的列表闪烁

## Capabilities

### New Capabilities
- `todo-optimistic-update`: 乐观更新机制 — 操作后即时更新本地 UI 状态，API 后台执行，失败回滚
- `todo-progress-redesign`: 进度条视觉与交互重设计 — 新进度指示器样式 + 100% 完成确认流程
- `todo-detail-edit-ux`: 详情页编辑交互优化 — 可编辑区域视觉提示 + 展开动画平滑化

### Modified Capabilities
_(无现有 spec 需要修改)_

## Impact

- **UI 层**：`TaskScreen.kt` 大幅修改（TodoItem 进度展示、TaskDetailBottomSheet 编辑交互与进度滑块、TaskDialog 动画）
- **ViewModel 层**：`TaskViewModel.kt` 重构 — 所有操作方法改为乐观更新模式，不再每次调 `refreshTodos()`
- **依赖**：无新依赖，使用 Compose 内置动画 API（`animateContentSize`、`Animatable`）
