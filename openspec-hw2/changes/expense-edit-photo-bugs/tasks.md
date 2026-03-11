## Tasks

### Bug 1: 编辑模式分析已上传照片

- [x] `NextApiService` 新增 `downloadPhotoBytes(url: String): Result<ByteArray>` 方法，通过认证 GET 请求下载照片
- [x] `ExpenseViewModel.analyzeDialogPhotos` 修改：当 `editingPhotos` 非空时，下载服务器照片转 base64，与 dialogPhotos 合并后发给 ReceiptAnalyzer
- [ ] 手动验证：编辑已有记账（有已上传照片）→ 点"让二狗分析" → 确认金额/标题/日期自动回填

### Bug 2: 照片上传失败提示

- [x] `ExpenseViewModel.addExpenseWithPhotos` 修改：统计上传失败数量，失败时设置 `error` state 提示用户
- [ ] 手动验证：模拟上传失败场景 → 确认 Snackbar 提示
