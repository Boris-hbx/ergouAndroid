## Design

### Bug 1: 编辑模式分析已上传照片

**根因**：`ExpenseViewModel.analyzeDialogPhotos` 只读取 `dialogPhotos`（本地 URI），编辑模式下照片在 `editingPhotos`（服务器端），导致分析请求无图片。

**方案**：
1. `NextApiService` 新增 `downloadPhotoBytes(photoUrl: String): Result<ByteArray>` — 通过认证 GET 请求下载照片原始字节
2. `analyzeDialogPhotos` 修改逻辑：
   - 先将 `dialogPhotos`（本地）压缩转 base64
   - 再将 `editingPhotos`（服务器）下载转 base64
   - 合并两组 base64 发给 `ReceiptAnalyzer`

### Bug 2: 照片上传失败无反馈

**根因**：`addExpenseWithPhotos` 上传失败只 `Timber.w`，用户无感知。

**方案**：上传失败时更新 `error` state，用户可在列表页看到 Snackbar 提示。

### Not Changing

- `ExpenseEditDialog` UI 层不变（`LaunchedEffect(preview)` 自动回填已正确）
- `ReceiptAnalyzer` 接口不变（已接受 `List<String>` base64）
- 照片上传/删除 API 不变
