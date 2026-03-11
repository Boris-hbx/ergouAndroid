## Why

记账功能的照片交互存在两个 bug：
1. **编辑模式"二狗分析"无法分析已上传照片**：`analyzeDialogPhotos` 只读取 `dialogPhotos`（本地未上传），编辑已有记账时照片在服务器上（`editingPhotos`），导致分析请求没有图片，自然无法回填金额、标题等字段
2. **照片加载可能失败但无反馈**：新建记账时照片上传失败仅 `Timber.w` 日志，用户完全不知情；需要增加 Snackbar 提示

## What Changes

- `ExpenseViewModel.analyzeDialogPhotos`：当 `editingPhotos` 非空时，从服务器下载这些照片转 base64，合并 `dialogPhotos` 一起发给 `ReceiptAnalyzer`
- `NextApiService`：新增 `downloadPhotoBytes(url)` 方法，用于下载已上传照片的原始字节
- `ExpenseViewModel.addExpenseWithPhotos`：照片上传失败时通过 error state 通知用户

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `expense-photo`: 编辑模式分析需支持从服务器下载已上传照片；照片上传失败需用户可感知

## Impact

- **ViewModel 层**：`ExpenseViewModel.analyzeDialogPhotos` 逻辑修改
- **API 层**：`NextApiService` 新增下载照片方法
- **无 UI 层变更**：分析结果回填逻辑已存在（`LaunchedEffect(preview)`）
- **无数据库变更**
