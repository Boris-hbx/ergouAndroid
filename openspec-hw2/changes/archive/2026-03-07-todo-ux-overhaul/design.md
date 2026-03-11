## Context

TaskScreen 当前的交互模式是"操作 → API 请求 → refreshTodos() 全量刷新"。每次操作（完成、删除、更新进度等）都会触发 `refreshTodos()`，导致整个列表被重新加载和重绘。进度条使用 Material 3 默认的 LinearProgressIndicator 和带刻度的 Slider，视觉和交互均不理想。详情页的编辑模式缺乏视觉提示，用户不知道可以点击编辑。对话框展开动画使用 AnimatedVisibility 但尺寸变化时 AlertDialog 不能平滑适应。

涉及文件：`TaskViewModel.kt`（状态管理）、`TaskScreen.kt`（UI 组件）。

## Goals / Non-Goals

**Goals:**
- 所有任务操作采用乐观更新，消除全量刷新导致的列表闪烁
- 重新设计进度指示器（列表圆弧环 + 详情页连续 Slider）
- 进度 100% 需用户确认才完成
- 详情页编辑区域增加铅笔图标提示
- 对话框展开收起动画平滑化

**Non-Goals:**
- 不做离线队列持久化（失败即回滚，不缓存待同步操作）
- 不改变 Next API 接口，仅客户端改动
- 不重构导航或新增页面

## Decisions

### 1. 乐观更新：保存快照 + 本地 mutation + 异步 API

**方案**：在 ViewModel 中，每个操作方法先保存当前 `_uiState.value` 作为快照，然后直接修改 StateFlow 中的列表数据，最后异步调 API。API 失败时恢复快照并显示错误。

**替代方案**：引入独立的本地缓存层（如 Room）作为 single source of truth，API 成功后同步。
→ 拒绝理由：Task 数据已由 Next 后端管理，引入 Room 缓存会增加复杂度且与现有架构不符。

**实现要点**：
```kotlin
fun completeTodo(id: String) {
    val snapshot = _uiState.value
    // 乐观更新：立即移动到已完成
    _uiState.value = snapshot.copy(
        pendingTodos = snapshot.pendingTodos.map {
            if (it.id == id) it.copy(completed = true) else it
        }.partition { !it.completed }.let { (pending, completed) ->
            _uiState.value = snapshot.copy(
                pendingTodos = pending,
                completedTodos = completed + snapshot.completedTodos
            )
            return@let pending
        }
    )
    // 异步 API
    viewModelScope.launch {
        val result = nextApiService.updateTodo(id, ...)
        result.onFailure {
            _uiState.value = snapshot  // 回滚
            _uiState.value = _uiState.value.copy(error = "操作失败，请重试")
        }
    }
}
```

实际实现中抽取公共的 `optimisticUpdate` 辅助函数，接收 `transform: (TaskUiState) -> TaskUiState` 和 `apiCall: suspend () -> Result<*>` 两个参数，统一处理快照/回滚/错误逻辑。

### 2. 列表进度指示器：Canvas 圆弧环

**方案**：用 Compose Canvas 绘制 24dp 圆弧进度环，放在任务卡片右侧（Checkbox 区域对面）。环内用 Text 显示百分比。

**替代方案**：使用 Material 3 的 CircularProgressIndicator。
→ 拒绝理由：需要在环内显示百分比文字，CircularProgressIndicator 不支持内嵌内容。自定义 Canvas 更灵活。

### 3. 详情页 Slider：去掉 steps，连续滑动

**方案**：移除 `steps = 9` 参数，Slider 变为连续模式。`onValueChangeFinished` 时取整提交。

### 4. 100% 完成确认：ViewModel 中管理确认状态

**方案**：在 ViewModel 中增加 `pendingCompleteId: String?` 状态。Slider 滑到 100% 松手时，设置此状态触发确认对话框。确认后调 `completeTodo()`，取消后回滚滑块值。

**替代方案**：纯 UI 层用 `remember` 管理确认状态。
→ 拒绝理由：需要在 Activity 重建时保持对话框状态，ViewModel 更可靠。但考虑到底部面板本身不在 SavedStateHandle 中持久化，实际用 `remember` 即可满足需求（面板关闭即重置）。最终采用 UI 层 `remember`。

### 5. 编辑提示：铅笔图标

**方案**：标题 Row 中在文本右侧加 `Icons.Default.Edit` 图标（16dp，onSurfaceVariant），点击整个 Row 进入编辑。描述区域同理。进入编辑模式后图标隐藏。

### 6. 对话框展开动画：animateContentSize

**方案**：将 `AnimatedVisibility(visible = showAdvanced)` 替换为 `Column(modifier = Modifier.animateContentSize())`，高级选项区域始终存在但通过 `if (showAdvanced)` 控制内容渲染。`animateContentSize()` 会平滑过渡容器高度变化。

**替代方案**：保留 AnimatedVisibility，自定义 EnterTransition/ExitTransition。
→ 拒绝理由：AlertDialog 对 AnimatedVisibility 的尺寸变化适应性差，animateContentSize 直接在父容器级别处理高度变化更可靠。

## Risks / Trade-offs

- **[乐观更新数据不一致]** → API 成功但服务端数据与本地乐观值有差异（如服务端修改了 updatedAt 等字段）。Mitigation：可接受，下次切换 Tab 或手动刷新时会同步。
- **[快速连续操作]** → 用户快速连续操作多个任务时，快照可能相互覆盖。Mitigation：每个操作保存各自的快照，回滚时仅恢复对应字段而非整个 state。实际实现中用 `optimisticUpdate` 的 transform 模式避免此问题。
- **[Canvas 圆弧环性能]** → 列表中每个 item 都绘制 Canvas。Mitigation：24dp 很小，绘制成本极低；用 `remember` 缓存 Paint 对象。
