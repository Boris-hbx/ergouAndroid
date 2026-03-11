## 1. 修改 EnglishItem 组件

- [x] 1.1 移除 EnglishItem 的 onDelete 参数和删除 IconButton
- [x] 1.2 更新 EnglishItem 调用处，移除 onDelete 参数

## 2. 添加 SwipeToDismissBox 包裹

- [x] 2.1 在 EnglishScreen 中添加 pendingDeleteIds 和 confirmDeleteItem state
- [x] 2.2 displayList 过滤掉 pendingDeleteIds 中的项目
- [x] 2.3 用 SwipeToDismissBox 包裹 LazyColumn 中的每个 EnglishItem（endToStart 方向，红色背景 + 删除图标）
- [x] 2.4 confirmValueChange 检测 EndToStart 时设置 confirmDeleteItem，返回 false

## 3. 确认对话框 + Snackbar 延迟删除

- [x] 3.1 添加 AlertDialog（confirmDeleteItem != null 时显示）
- [x] 3.2 取消 → 清除 confirmDeleteItem，重置 SwipeToDismissBox
- [x] 3.3 确认 → 加入 pendingDeleteIds，显示 Snackbar "已删除" + "撤销"
- [x] 3.4 Snackbar 超时 → 调用 viewModel.deleteScenario(id)；撤销 → 从 pendingDeleteIds 移除

## 4. 验证

- [x] 4.1 添加必要的 import（SwipeToDismissBox, SwipeToDismissBoxValue, rememberSwipeToDismissBoxState 等）
- [x] 4.2 确认编译通过
