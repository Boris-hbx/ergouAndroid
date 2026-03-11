## ADDED Requirements

### Requirement: 费用项照片上传 API
NextApiService SHALL 提供 `uploadTripItemPhoto(itemId, uri, context)` 方法，通过 multipart POST 上传照片到 `/api/trips/items/{itemId}/photos`。照片大小 MUST 不超过 10MB。

#### Scenario: 成功上传
- **WHEN** 调用 uploadTripItemPhoto 且照片 ≤ 10MB
- **THEN** 返回 `Result.success(NextTripItemPhoto)` 包含照片元数据

#### Scenario: 照片超过大小限制
- **WHEN** 照片大小超过 10MB
- **THEN** 返回 `Result.failure` 并提示"照片大小不能超过10MB"，不发起网络请求

#### Scenario: 网络上传失败
- **WHEN** multipart 上传请求失败
- **THEN** 返回 `Result.failure` 包含错误信息

### Requirement: 费用项照片删除 API
NextApiService SHALL 提供 `deleteTripItemPhoto(photoId)` 方法，调用 DELETE `/api/trips/photos/{photoId}`。

#### Scenario: 成功删除
- **WHEN** 调用 deleteTripItemPhoto 且请求成功
- **THEN** 返回 `Result.success(Unit)`

#### Scenario: 删除失败
- **WHEN** API 请求失败
- **THEN** 返回 `Result.failure` 包含错误信息

### Requirement: 照片数据模型
NextModels SHALL 包含 `NextTripItemPhoto` 数据类（id, itemId, filename, storagePath, fileSize, mimeType, createdAt）。`NextTripItem` SHALL 包含可选的 `photos: List<NextTripItemPhoto>` 字段。

#### Scenario: 反序列化照片列表
- **WHEN** API 返回带照片列表的费用项 JSON
- **THEN** `NextTripItem.photos` 正确解析为 `List<NextTripItemPhoto>`

#### Scenario: 无照片时默认空列表
- **WHEN** API 返回的费用项不包含 photos 字段
- **THEN** `NextTripItem.photos` 为空列表

### Requirement: 照片展示 UI
Trip 详情页的每个费用项下方 SHALL 显示照片区域。已有照片以 LazyRow 展示 80x80dp 圆角缩略图，使用 Coil AsyncImage 从 `${BASE_URL}/api/uploads/${storagePath}` 加载。

#### Scenario: 展示已有照片
- **WHEN** 费用项有照片
- **THEN** 照片以水平滚动的缩略图行展示

#### Scenario: 无照片时仅显示添加按钮
- **WHEN** 费用项无照片
- **THEN** 照片区域仅显示添加照片按钮

### Requirement: 照片添加交互
照片行末尾 SHALL 有一个"+"添加按钮。点击后弹出选择器支持相册选取（PickVisualMedia）和拍照（TakePicture + FileProvider）两种来源。

#### Scenario: 从相册选取
- **WHEN** 用户选择从相册添加照片
- **THEN** 系统启动 PickVisualMedia，选取后自动上传

#### Scenario: 拍照添加
- **WHEN** 用户选择拍照
- **THEN** 系统通过 FileProvider 创建临时 Uri，启动相机，拍摄后自动上传

#### Scenario: 取消选取
- **WHEN** 用户取消照片选取或拍照
- **THEN** 不做任何操作

### Requirement: 照片删除交互
每张照片缩略图右上角 SHALL 有一个 X 删除按钮。点击后调用 API 删除照片。

#### Scenario: 点击删除照片
- **WHEN** 用户点击照片缩略图右上角的 X
- **THEN** 系统调用 deleteTripItemPhoto 并刷新详情

#### Scenario: 删除失败
- **WHEN** 照片删除 API 失败
- **THEN** 系统显示 Snackbar 错误信息，照片保留

### Requirement: 上传失败错误处理
照片上传失败时系统 SHALL 显示 Snackbar 错误信息。上传过程中 MUST 有 loading 状态指示。

#### Scenario: 上传中显示 loading
- **WHEN** 照片正在上传
- **THEN** 添加按钮位置显示 CircularProgressIndicator

#### Scenario: 上传失败提示
- **WHEN** 照片上传失败
- **THEN** 显示 Snackbar 包含错误信息（如"上传失败：网络错误"）
