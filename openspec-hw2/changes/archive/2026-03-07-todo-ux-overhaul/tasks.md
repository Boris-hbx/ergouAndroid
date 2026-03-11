## 1. 乐观更新机制 (TaskViewModel)

- [x] 1.1 在 TaskViewModel 中添加 `optimisticUpdate` 辅助函数：接收 transform 和 apiCall，保存快照、本地更新、异步 API、失败回滚+错误提示
- [x] 1.2 重构 `completeTodo()` 使用乐观更新：立即将任务从 pendingTodos 移至 completedTodos
- [x] 1.3 重构 `uncompleteTodo()` 使用乐观更新：立即将任务从 completedTodos 移回 pendingTodos
- [x] 1.4 重构 `deleteTodo()` 使用乐观更新：立即从列表移除任务
- [x] 1.5 重构 `updateProgress()` 使用乐观更新：立即更新列表中对应任务的 progress 字段
- [x] 1.6 重构 `updateTodo()` 使用乐观更新：立即更新列表中对应任务的 text/content/tags/dueDate/quadrant 字段
- [x] 1.7 重构 `addTodo()` 使用乐观更新：立即添加临时任务到 pendingTodos，API 成功后替换为服务端返回数据
- [x] 1.8 移除 completeTodo/uncompleteTodo/deleteTodo/updateProgress/updateTodo 中的 `refreshTodos()` 调用，仅保留 switchTab 和 init 中的全量刷新

## 2. 进度指示器重设计 (TaskScreen UI)

- [x] 2.1 创建 `CircularProgressRing` Composable：24dp Canvas 圆弧环 + 中心百分比文字，接收 progress: Int 参数
- [x] 2.2 在 TodoItem 中替换 LinearProgressIndicator 为 CircularProgressRing，放在卡片右侧
- [x] 2.3 在 TaskDetailBottomSheet 中移除 Slider 的 `steps = 9` 参数，改为连续滑动
- [x] 2.4 实现 100% 完成确认：滑块松手时若值 >= 100，弹出 AlertDialog 确认；确认→完成，取消→回退滑块值

## 3. 详情页编辑交互优化 (TaskDetailBottomSheet)

- [x] 3.1 标题区域：在 Text 右侧添加铅笔图标（Icons.Default.Edit, 16dp），点击整个 Row 进入编辑模式
- [x] 3.2 描述区域：添加铅笔图标提示，空描述时显示"点击添加描述..."带铅笔图标
- [x] 3.3 编辑模式自动聚焦：使用 FocusRequester 在 isEditingTitle/isEditingContent 切换时自动请求焦点

## 4. 对话框展开动画平滑化 (TaskDialog)

- [x] 4.1 将 AnimatedVisibility(visible = showAdvanced) 替换为 Column(Modifier.animateContentSize()) + if (showAdvanced) 内容渲染
- [x] 4.2 验证编辑模式有高级字段值时默认展开不播放动画

## 5. 手动测试验证

- [ ] 5.1 测试乐观更新：完成/取消完成/删除任务后列表不闪烁，断网时操作后回滚并提示
- [ ] 5.2 测试进度条：列表中圆弧环显示正确，详情页滑块连续滑动，100%弹出确认
- [ ] 5.3 测试编辑交互：点击标题/描述可进入编辑模式，铅笔图标可见
- [ ] 5.4 测试展开动画：更多选项展开/收起平滑无卡顿

> 注：5.1-5.4 为手动测试任务，需在设备上验证
