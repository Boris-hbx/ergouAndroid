## 1. RoutineScreen 删除交互重写

- [x] 1.1 在 RoutineScreen 顶层添加 pendingDeleteIds 和 confirmDeleteItem 状态，添加 coroutineScope
- [x] 1.2 将 ReviewItemCard 包裹在 SwipeToDismissBox 中（endToStart，红色背景+删除图标，confirmValueChange 返回 false 并设置 confirmDeleteItem）
- [x] 1.3 在列表展示时用 pendingDeleteIds 过滤（displayList = reviews.filter { it.id !in pendingDeleteIds }）
- [x] 1.4 实现确认对话框（confirmDeleteItem 触发），确认后加入 pendingDeleteIds + 启动 Snackbar 协程
- [x] 1.5 Snackbar 协程：ActionPerformed 撤销，Dismissed 调用 viewModel.deleteReview()
- [x] 1.6 详情页删除按钮走同样流程：确认后 selectReview(null) + pendingDeleteIds + Snackbar
- [x] 1.7 移除旧的 itemToDelete 状态和对应的 AlertDialog（已被新流程替代）
