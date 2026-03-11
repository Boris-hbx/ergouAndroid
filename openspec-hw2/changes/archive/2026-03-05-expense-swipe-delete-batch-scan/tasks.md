## 1. ExpenseViewModel 左滑删除支持

- [x] 1.1 在 `ExpenseUiState` 添加 `pendingDeleteId: String? = null`，添加计算属性 `filteredExpenses` 过滤掉 pending 条目
- [x] 1.2 添加 `confirmDelete(id: String)` 方法 — 设置 `pendingDeleteId`，不立即调用 API
- [x] 1.3 添加 `executeDelete()` 方法 — 调用 `nextApiService.deleteExpense(pendingDeleteId)`，失败则恢复条目并提示错误
- [x] 1.4 添加 `cancelDelete()` 方法 — 清空 `pendingDeleteId`，条目恢复显示
- [x] 1.5 修改 `dayGroups` 计算属性，基于 `filteredExpenses` 而非 `expenses`

## 2. ExpenseScreen 左滑删除 UI

- [x] 2.1 添加 SwipeToDismissBox 相关 import（SwipeToDismissBox、SwipeToDismissBoxValue、rememberSwipeToDismissBoxState、Background + Icon）
- [x] 2.2 用 SwipeToDismissBox 包裹 `ExpenseItem`，左滑显示红色背景 + 删除图标
- [x] 2.3 左滑到位后弹出 AlertDialog 确认，确认调用 `viewModel.confirmDelete(id)`，取消重置 swipe 状态
- [x] 2.4 确认删除后显示 Snackbar "已删除" + 撤销按钮，超时调用 `viewModel.executeDelete()`，撤销调用 `viewModel.cancelDelete()`
- [x] 2.5 从 `ExpenseItem` 中移除 `onDelete` 参数、X 按钮 `IconButton` 和 `showDeleteConfirm` 对话框逻辑
- [x] 2.6 列表使用 `uiState.filteredExpenses` 代替 `uiState.expenses`（dayGroups 已基于 filtered）

## 3. ExpenseViewModel 批量扫描支持

- [x] 3.1 添加 `ScanResultItem` data class（id, preview, photoUri, editedAmount, editedNotes, editedTags, editedCurrency）
- [x] 3.2 在 `ExpenseUiState` 添加批量扫描状态字段：`showBatchScan: Boolean`、`selectedPhotos: List<Uri>`、`scanProgress: Pair<Int, Int>?`、`scanResults: List<ScanResultItem>`、`isSaving: Boolean`
- [x] 3.3 添加 `toggleBatchScan()` 方法 — 切换 showBatchScan，进入时清空照片和结果
- [x] 3.4 添加 `addPhotos(uris: List<Uri>)` 和 `removePhoto(index: Int)` 方法
- [x] 3.5 添加 `analyzePhotos(context: Context)` 方法 — 逐张 uri→base64→parseReceiptPreview，更新 scanProgress，10MB 检查跳过，失败继续
- [x] 3.6 添加 `updateScanResult(id: String, ...)` 方法 — 更新编辑后的金额/备注/标签/币种
- [x] 3.7 添加 `removeScanResult(id: String)` 方法
- [x] 3.8 添加 `saveAllResults(context: Context)` 方法 — 逐条 createExpense + uploadExpensePhoto，完成后清空扫描状态并 refreshAll()

## 4. ExpenseScreen 批量扫描 UI

- [x] 4.1 添加 `BatchScanSection` composable 函数框架（接收 viewModel、uiState、context）
- [x] 4.2 实现照片选择器：`rememberLauncherForActivityResult(PickMultipleVisualMedia())` + 入口按钮
- [x] 4.3 实现照片预览区：LazyRow 展示缩略图（Coil AsyncImage 80dp×80dp），每张右上角 X 删除，末尾 + 按钮追加
- [x] 4.4 实现"二狗分析"按钮：照片为空 disabled，分析中显示 CircularProgressIndicator + "分析中 1/3..."
- [x] 4.5 实现分析结果列表：每条 Card 显示商家 + ¥金额 + 明细行，编辑/删除按钮
- [x] 4.6 实现编辑弹窗：点击编辑弹出对话框，可修改金额、商家(notes)、标签、币种
- [x] 4.7 实现"确认保存全部"按钮：有结果时显示，点击调用 viewModel.saveAllResults()，saving 时显示 loading
- [x] 4.8 修改 FAB 相机按钮：点击切换 `viewModel.toggleBatchScan()` 而非导航
- [x] 4.9 在主内容区根据 `uiState.showBatchScan` 切换显示 BatchScanSection 或列表

## 5. 手动测试

- [ ] 5.1 验证左滑删除：滑动→确认→Snackbar→撤销恢复 / 超时真删
- [ ] 5.2 验证批量扫描：选多张照片→预览→分析进度→结果展示→编辑→保存
- [ ] 5.3 验证边界：10MB 照片跳过、分析失败继续、部分保存失败提示
- [ ] 5.4 验证删除失败恢复：网络断开时删除，条目应恢复
