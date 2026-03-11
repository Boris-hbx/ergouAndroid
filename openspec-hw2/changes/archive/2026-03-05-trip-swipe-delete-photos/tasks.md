## 1. NextModels 数据类

- [x] 1.1 在 NextModels.kt 添加 `NextTripItemPhoto` 数据类（id, itemId, filename, storagePath, fileSize, mimeType, createdAt）
- [x] 1.2 在 NextModels.kt 添加 `NextTripItemPhotoResponse` 数据类（success, photo, message）
- [x] 1.3 在 `NextTripItem` 中添加 `val photos: List<NextTripItemPhoto> = emptyList()` 字段

## 2. NextApiService 照片方法

- [x] 2.1 实现 `uploadTripItemPhoto(itemId, uri, context)` — multipart POST `/api/trips/items/{itemId}/photos`，10MB 大小检查，参考 `uploadExpensePhoto`
- [x] 2.2 实现 `deleteTripItemPhoto(photoId)` — DELETE `/api/trips/photos/{photoId}`

## 3. TripViewModel 延迟删除

- [x] 3.1 在 `TripUiState` 添加 `pendingDeleteTripIds: Set<String>`、`pendingDeleteItemIds: Set<String>`、`confirmDeleteTrip: NextTrip?`、`confirmDeleteItem: NextTripItem?`、`uploadingItemIds: Set<String>`
- [x] 3.2 实现 `confirmDeleteTrip(id)` / `undoDeleteTrip(id)` / `executeDeleteTrip(id)` — 加入 pending → Snackbar 撤销 → 超时调用 API
- [x] 3.3 实现 `confirmDeleteItem(id)` / `undoDeleteItem(id)` / `executeDeleteItem(id)` — 费用项同理
- [x] 3.4 修改 `TripUiState.sortedTrips` 过滤掉 `pendingDeleteTripIds` 中的项目
- [x] 3.5 详情页费用项列表过滤掉 `pendingDeleteItemIds` 中的项目

## 4. TripViewModel 照片方法

- [x] 4.1 实现 `uploadItemPhoto(itemId, uri)` — 调用 `nextApiService.uploadTripItemPhoto`，上传中设置 `uploadingItemIds`，完成后刷新详情
- [x] 4.2 实现 `deleteItemPhoto(photoId)` — 调用 `nextApiService.deleteTripItemPhoto`，完成后刷新详情

## 5. TripScreen 左滑删除 — Trip 列表

- [x] 5.1 移除 Trip 列表项的 Delete IconButton
- [x] 5.2 用 `SwipeToDismissBox` 包裹 Trip 列表项（参考 TaskScreen 模式：rememberSwipeToDismissBoxState + confirmValueChange 拦截）
- [x] 5.3 添加 AlertDialog 确认对话框（confirmDeleteTrip 状态驱动）
- [x] 5.4 添加 Snackbar 延迟删除逻辑（确认 → 乐观移除 → 撤销/超时真删）

## 6. TripScreen 左滑删除 — 费用项

- [x] 6.1 用 `SwipeToDismissBox` 包裹详情页费用项卡片
- [x] 6.2 添加 AlertDialog 确认对话框（confirmDeleteItem 状态驱动）
- [x] 6.3 编辑对话框的"删除此费用"按钮改为先弹确认对话框
- [x] 6.4 添加 Snackbar 延迟删除逻辑（与 Trip 列表一致）

## 7. TripScreen 照片 UI

- [x] 7.1 在费用项展开区域添加照片区域：LazyRow 显示 80x80dp 圆角缩略图（Coil AsyncImage）
- [x] 7.2 每张照片右上角添加 X 删除按钮（16dp 圆形，半透明黑底白 X）
- [x] 7.3 照片行末尾添加 + 按钮（80x80dp 虚线边框）
- [x] 7.4 实现相册选取：`rememberLauncherForActivityResult(PickVisualMedia())` → 选取后调用 `viewModel.uploadItemPhoto`
- [x] 7.5 实现拍照：`rememberLauncherForActivityResult(TakePicture())` + FileProvider 创建临时 Uri → 拍摄后调用 `viewModel.uploadItemPhoto`
- [x] 7.6 + 按钮点击弹出选择器（相册/拍照两个选项）
- [x] 7.7 上传中显示 CircularProgressIndicator 替代 + 按钮

## 8. 手动测试

- [ ] 8.1 验证 Trip 列表左滑删除：左滑 → 确认 → 乐观移除 → 撤销恢复 / 超时真删
- [ ] 8.2 验证费用项左滑删除：同上流程 + 编辑对话框删除按钮也弹确认
- [ ] 8.3 验证照片上传：相册选取 + 拍照，缩略图正确显示
- [ ] 8.4 验证照片删除：点击 X 后照片移除
- [ ] 8.5 验证错误处理：断网上传显示 Snackbar 错误，API 删除失败恢复项目
