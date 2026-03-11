## Context

Trip 模块当前使用按钮式删除（列表项有 Delete IconButton，费用项在编辑对话框底部有删除按钮）。其他模块（Task、Routine、Review）已统一使用 SwipeToDismissBox + 确认对话框 + Snackbar 延迟删除模式。

费用项有 `photoCount` 字段但没有照片管理功能。Expense 模块已有完整的照片上传/删除实现（`uploadExpensePhoto`/`deleteExpensePhoto`），可作为参考。

## Goals / Non-Goals

**Goals:**
- Trip 列表和费用项删除统一为左滑 + 确认 + Snackbar 延迟删除模式
- 费用项支持照片上传（相册/拍照）、展示（缩略图）、删除
- 复用现有 SwipeToDismiss 和 photo upload 模式，保持代码一致性

**Non-Goals:**
- 不做照片编辑/裁剪
- 不做离线照片缓存
- 不做照片批量管理
- 不修改 AppModule.kt、ErgouPrompt.kt

## Decisions

### D1: SwipeToDismissBox 模式复用 TaskScreen 实现

**选择**: 直接复用 TaskScreen 中已有的 SwipeToDismiss 模式：`rememberSwipeToDismissBoxState` + `confirmValueChange` 拦截 + `showDeleteConfirm` 状态 + AlertDialog + `LaunchedEffect` reset。

**备选方案**: 抽取通用 SwipeToDeleteItem composable → 过度抽象，目前只有 4-5 个模块用到，每个有微小差异。

**理由**: 模式已在 Task/Routine/Review 中验证，直接拷贝模式最简单可靠。

### D2: 延迟删除使用 pendingDeleteIds + Snackbar

**选择**: TripUiState 新增 `pendingDeleteTripIds: Set<String>` 和 `pendingDeleteItemIds: Set<String>`。确认后将 id 加入 pending set，UI 过滤掉 pending 项目。Snackbar 显示"撤销"按钮，超时后调用真实删除 API。

ViewModel 方法：
- `confirmDeleteTrip(id)` — 加入 pending，启动 Snackbar 协程
- `undoDeleteTrip(id)` — 从 pending 移除
- `executeDeleteTrip(id)` — 调用 API，成功后从列表移除，失败恢复
- 费用项同理：`confirmDeleteItem`/`undoDeleteItem`/`executeDeleteItem`

**理由**: 与 Task/Routine 模块一致的延迟删除模式。

### D3: 照片上传参考 uploadExpensePhoto

**选择**: `uploadTripItemPhoto` 方法完全复制 `uploadExpensePhoto` 的实现模式：读取 ContentResolver bytes → 10MB 检查 → submitFormWithBinaryData multipart → 解析 response。

API 端点：
- POST `/api/trips/items/{itemId}/photos` — multipart 上传
- DELETE `/api/trips/photos/{photoId}` — 删除

Response 数据类：
```kotlin
@Serializable
data class NextTripItemPhoto(
    val id: String = "",
    @SerialName("item_id") val itemId: String = "",
    val filename: String = "",
    @SerialName("storage_path") val storagePath: String? = null,
    @SerialName("file_size") val fileSize: Long = 0,
    @SerialName("mime_type") val mimeType: String = "",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class NextTripItemPhotoResponse(
    val success: Boolean = false,
    val photo: NextTripItemPhoto? = null,
    val message: String? = null
)
```

`NextTripItem` 添加 `val photos: List<NextTripItemPhoto> = emptyList()`。

### D4: 照片选取使用 PickVisualMedia + TakePicture

**选择**:
- 相册：`ActivityResultContracts.PickVisualMedia()` — 现代 Photo Picker API
- 拍照：`ActivityResultContracts.TakePicture()` + `FileProvider` 创建临时 Uri

两个 launcher 在 Composable 中用 `rememberLauncherForActivityResult` 注册。拍照前通过 `FileProvider.getUriForFile` 创建临时文件 Uri。

**备选方案**: 只用 `GetContent("image/*")` — 不支持拍照，且 UX 较差。

### D5: 照片缩略图展示

照片区域放在费用项卡片展开区域内（日期和金额下方），使用 `LazyRow` 水平展示 80x80dp 圆角缩略图。每张图右上角叠加 X 删除按钮（16dp 圆形，半透明黑底白 X）。行末尾为 + 添加按钮（同 80x80dp 虚线边框）。

图片加载使用 Coil `AsyncImage`，URL 为 `${BASE_URL}/api/uploads/${photo.storagePath}`。

### D6: TripUiState 新增字段

```kotlin
val pendingDeleteTripIds: Set<String> = emptySet()
val pendingDeleteItemIds: Set<String> = emptySet()
val confirmDeleteTrip: NextTrip? = null
val confirmDeleteItem: NextTripItem? = null
val uploadingItemIds: Set<String> = emptySet()  // 正在上传照片的费用项 ID
```

## Risks / Trade-offs

**[多 Snackbar 并发]** → 多个延迟删除同时进行时，Snackbar 会互相覆盖（Material 3 SnackbarHost 只显示最新一个）。可接受——最新的操作优先显示，其他操作仍按超时执行。

**[大照片上传耗时]** → 10MB 限制 + 移动网络可能较慢。通过 uploadingItemIds 显示 loading 状态，用户可感知进度。

**[FileProvider 配置]** → 拍照需要 `FileProvider` 在 AndroidManifest 中配置。项目已有 FileProvider 配置（用于其他功能），需确认 `file_paths.xml` 包含临时目录。

**[照片 URL 依赖 storagePath]** → 如果服务端返回的 storagePath 为 null，缩略图将无法加载。Coil 的 error placeholder 可处理此情况。
