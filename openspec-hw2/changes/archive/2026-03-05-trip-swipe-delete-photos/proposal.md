## Why

Trip 模块的删除交互与其他模块（Todo、Expense 等已采用左滑删除）不一致，影响 UX 统一性。同时差旅费用项已有 photoCount 字段但没有照片管理功能，用户无法为报销凭证拍照附图。

## What Changes

- Trip 列表：去掉 Delete 按钮，改为 SwipeToDismissBox 左滑删除 + 确认对话框 + Snackbar 延迟删除（乐观移除 + 撤销）
- Trip 详情页费用项：同样支持左滑删除 + 确认 + Snackbar 延迟删除，编辑对话框的删除按钮保留但也加确认
- 延迟删除统一模式：pendingDeleteIds + confirmDeleteItem + AlertDialog + Snackbar 撤销 + 超时真删
- NextApiService 新增 `uploadTripItemPhoto` 和 `deleteTripItemPhoto` 方法
- NextModels 新增 `NextTripItemPhoto` 数据类，`NextTripItem` 添加 `photos` 字段
- TripViewModel 新增 `uploadItemPhoto` 和 `deleteItemPhoto` 方法
- TripScreen 详情页费用项区域添加照片展示（LazyRow 缩略图）、照片上传（相册/拍照）、照片删除

## Capabilities

### New Capabilities
- `trip-swipe-delete`: Trip 列表和费用项的左滑删除 + 确认 + Snackbar 延迟删除统一交互
- `trip-item-photos`: 差旅费用项照片上传、展示、删除功能

### Modified Capabilities
<!-- 无现有 spec 需要修改 -->

## Impact

- 修改文件：TripScreen.kt、TripViewModel.kt、NextApiService.kt、NextModels.kt
- 新增依赖：Coil AsyncImage（已有依赖）、ActivityResultContracts（Android 标准库）
- API：新增 POST `/api/trips/items/{itemId}/photos`、DELETE `/api/trips/photos/{photoId}`
- 不修改：AppModule.kt、ErgouPrompt.kt、其他模块文件
