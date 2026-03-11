## Context

记账模块当前使用 X 按钮 + AlertDialog 删除条目，与 Todo 等模块的左滑删除不一致。扫描功能只支持单张照片。需要统一删除交互并添加批量扫描能力。只修改 4 个文件：ExpenseScreen.kt、ExpenseDetailScreen.kt、ExpenseViewModel.kt、ExpenseDetailViewModel.kt。

## Goals / Non-Goals

**Goals:**
- 左滑删除 + Snackbar 撤销替代 X 按钮删除，与 Todo 模块一致
- 批量选择照片 → AI 逐张分析 → 结果编辑 → 批量保存
- 分析进度实时反馈

**Non-Goals:**
- 不修改 NextApiService.kt、NextModels.kt、AppModule.kt
- 不创建新文件
- 不做 OCR 本地识别，全部依赖 Next 后端 API
- 不修改 ExpenseDetailScreen 的已有照片管理（查看/删除已上传照片）

## Decisions

### D1: SwipeToDismissBox 左滑删除模式

**选择**: 在 ExpenseScreen 的 `items` 调用处用 `SwipeToDismissBox` 包裹 `ExpenseItem`，移除 `ExpenseItem` 内部的 X 按钮和 `showDeleteConfirm` 逻辑。

**删除流程**: 左滑 → AlertDialog 确认 → 乐观从列表移除（`pendingDeleteId`） → Snackbar "已删除"（含撤销按钮） → 超时后调用 `deleteExpense(id)` → 失败则恢复。

**状态管理**: 在 `ExpenseUiState` 中添加 `pendingDeleteId: String?`，UI 层通过 `filteredExpenses` 过滤掉 pending 条目。ViewModel 添加 `confirmDelete(id)` 和 `cancelDelete()` 方法。

**替代方案**: 直接删除不带撤销 → 与 Todo 模块不一致，且误删无法恢复。

### D2: 批量扫描 UI 作为内嵌 Composable

**选择**: 在 ExpenseScreen.kt 中新增 `BatchScanSection` composable 函数。通过 `ExpenseUiState.showBatchScan: Boolean` 控制显示/隐藏，替换主列表内容区域。

**入口**: 保留现有 SmallFloatingActionButton（相机图标），点击切换 `showBatchScan = true`，而非导航到新页面。

**替代方案**: 新建 BatchScanScreen.kt → 违反"不创建新文件"限制。

### D3: 照片选择使用 PickMultipleVisualMedia

**选择**: `ActivityResultContracts.PickMultipleVisualMedia()` 支持多选。返回 `List<Uri>`，追加到 `ExpenseUiState.selectedPhotos` 列表。

**照片存储**: 只在 UiState 中保存 `List<Uri>`（内存），不持久化。页面退出即清空。

### D4: AI 分析流程在 ViewModel 中逐张处理

**选择**: ViewModel 添加 `analyzePhotos(context: Context, uris: List<Uri>)` 方法：
1. 逐张：`contentResolver.openInputStream(uri)?.readBytes()` → 检查 size < 10MB → `Base64.encodeToString` → `nextApiService.parseReceiptPreview(base64)`
2. 进度：`ExpenseUiState.scanProgress: Pair<Int, Int>?`（current/total），null 表示未在分析
3. 结果：`ExpenseUiState.scanResults: List<ScanResultItem>`，每条包含 `NextParsePreview` + 对应 `Uri` + `isEditing: Boolean`

**ScanResultItem** 是 `ExpenseViewModel.kt` 中的 data class：
```kotlin
data class ScanResultItem(
    val id: String = UUID.randomUUID().toString(),
    val preview: NextParsePreview,
    val photoUri: Uri,
    val editedAmount: Double? = null,
    val editedNotes: String? = null,
    val editedTags: List<String>? = null,
    val editedCurrency: String? = null
)
```

### D5: 批量保存流程

逐条调用 `nextApiService.createExpense(request)` 创建条目，成功后调用 `nextApiService.uploadExpensePhoto(entryId, uri, context)` 上传照片。全部完成后清空扫描状态并 `refreshAll()`。

**错误处理**: 条目创建失败跳过该条，继续下一条。照片上传失败不影响条目（已创建）。最终汇报成功/失败数量。

### D6: ExpenseItem 参数简化

移除 `onDelete` 参数，`ExpenseItem` 不再处理删除逻辑。删除完全由外层 `SwipeToDismissBox` 控制。

## Risks / Trade-offs

- **大图片内存**: 多张照片的 base64 可能占用大量内存。通过逐张处理（不一次全部转换）缓解，且 10MB 限制控制了上限。
- **分析速度**: 逐张串行调用 API，3 张照片可能需要 30+ 秒。进度指示器缓解用户焦虑。未来可考虑并行调用。
- **Uri 权限**: PickMultipleVisualMedia 返回的 Uri 有临时读取权限，需在同一 Activity 生命周期内使用。由于分析和上传都在同一会话完成，问题不大。
