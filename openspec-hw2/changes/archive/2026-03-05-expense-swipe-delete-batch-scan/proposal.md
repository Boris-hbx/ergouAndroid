## Why

记账模块的删除交互（X 按钮 + 确认对话框）与其他模块（如 Todo 的左滑删除）不一致，体验割裂。同时，当前只支持单张收据扫描，用户有多张小票时需要重复操作。统一删除模式 + 批量照片 AI 分析可以提升一致性和效率。

## What Changes

- **删除模式统一**：ExpenseScreen 列表条目去掉右侧 X 按钮，改为 SwipeToDismissBox 左滑删除 + 确认对话框 + Snackbar 撤销（延迟删除模式），与 Todo 等模块一致
- **批量照片扫描**：新增批量扫描入口，支持一次选择多张照片，LazyRow 预览 + 单张删除，统一调用 AI 分析后展示结果列表
- **分析结果管理**：AI 分析结果逐条展示（商家、金额、明细），支持编辑和删除，确认后批量保存为记账条目

## Capabilities

### New Capabilities
- `expense-swipe-delete`: 记账条目左滑删除 + Snackbar 撤销，替代原有 X 按钮删除
- `expense-batch-scan`: 批量选择照片 → AI 分析收据 → 结果编辑 → 批量保存

### Modified Capabilities
- `expense-photo`: 原有单张照片扫描行为被批量扫描替代，入口和流程发生变化

## Impact

- 修改文件：ExpenseScreen.kt、ExpenseDetailScreen.kt、ExpenseViewModel.kt、ExpenseDetailViewModel.kt
- 依赖：Coil AsyncImage（已有）、ActivityResultContracts.PickMultipleVisualMedia、NextApiService.parseReceiptPreview（已有）
- 不修改：NextApiService.kt、NextModels.kt、AppModule.kt
