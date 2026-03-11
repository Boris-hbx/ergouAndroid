## Context

当前记账模块只有列表页 (`ExpenseScreen`) 和新增对话框 (`AddExpenseDialog`)，没有详情/编辑页面。`NextApiService` 已有 `updateExpense` 和 `getExpenseById` 方法，但 UI 层未使用。照片相关 API（上传、删除、parse-preview）和 DTO 完全缺失。Coil 图片加载库也未引入。

导航使用 Jetpack Navigation Compose，当前所有路由都是简单字符串（无参数传递），需要为详情页引入带参数的路由。

## Goals / Non-Goals

**Goals:**
- 实现记账详情/编辑页面，支持编辑所有字段并保存
- 实现照片上传（相册/拍照）和管理（查看/删除）
- 实现 AI 收据解析流程（拍照 → 预览 → 确认保存）
- 添加必要的 API 方法和 DTO

**Non-Goals:**
- 不做明细行 (items) 的编辑功能（只展示 AI 解析结果）
- 不做离线缓存（照片不本地存储，始终从 Next 加载）
- 不做图片压缩/裁剪（直接上传原图，受 10MB 限制）
- 不做批量照片上传（一次只上传一张）

## Decisions

### 1. 详情页作为独立 Composable + 新 ViewModel

**决定**: 新建 `ExpenseDetailScreen` + `ExpenseDetailViewModel`，而非扩展现有 `ExpenseViewModel`。

**理由**: 详情页有独立的状态管理（加载详情、编辑状态、照片管理、AI 解析预览），职责明显不同于列表页。独立 ViewModel 更清晰，避免列表 ViewModel 膨胀。

**替代方案**: 扩展 `ExpenseViewModel` 增加详情状态 → 违反单一职责，列表刷新时可能干扰详情编辑。

### 2. 导航使用路由参数传递 expense ID

**决定**: 路由 `expense/{expenseId}`，使用 Navigation Compose 的 `arguments` 参数。

**实现**:
```kotlin
composable("expense/{expenseId}", arguments = listOf(navArgument("expenseId") { type = NavType.StringType })) {
    ExpenseDetailScreen(expenseId = it.arguments?.getString("expenseId")!!, ...)
}
```

**理由**: 项目已有 Navigation Compose，路由参数是标准做法。expense ID 是简单字符串，适合 URL 参数。

### 3. 照片选择使用 ActivityResult API

**决定**: 使用 `rememberLauncherForActivityResult` 配合 `PickVisualMedia`（相册）和 `TakePicture`（相机）。

**理由**: 这是现代 Android 推荐方式，不需要 `READ_EXTERNAL_STORAGE` 权限（相册），只需 `CAMERA` 权限（拍照）。兼容 API 21+。

**替代方案**: CameraX 库 → 太重，只需简单拍照不需要自定义相机界面。

### 4. 照片上传使用 Ktor Multipart

**决定**: 在 `NextApiService` 中新增 `uploadExpensePhoto` 方法，使用 Ktor 的 `submitFormWithBinaryData` 发送 multipart 请求。从 ContentResolver 读取图片字节。

**实现**:
```kotlin
suspend fun uploadExpensePhoto(entryId: String, uri: Uri, context: Context): Result<NextExpensePhoto>
```

需要传入 `Context` 来读取 URI 的 ContentResolver。通过 Koin 注入 Application context。

**替代方案**: 转 base64 发 JSON → 内存占用大，不符合后端 multipart 接口设计。

### 5. AI 解析预览使用 base64 传图

**决定**: `parse-preview` API 接收 base64 图片（参考 Next 后端文档），在客户端读取 URI → 转 base64 → POST JSON body。

**实现**: 单独的 `parseReceiptPreview(imageBase64: String)` 方法，120s 超时（与后端 vision_generate 超时一致）。

### 6. 图片加载使用 Coil 3

**决定**: 引入 Coil 3 (`io.coil-kt.coil3:coil-compose`) 加载照片缩略图和全屏图。需要自定义 ImageLoader 以注入认证 Cookie。

**实现**: 使用 Coil 的 `ImageRequest.Builder` 添加 `header("Cookie", "session=$token")`。照片 URL 格式: `$BASE_URL/api/uploads/{user_id}/{filename}`。

**替代方案**: Glide → Coil 更 Kotlin-native，与 Compose 集成更好，是项目 CLAUDE.md 推荐的方案。

### 7. AI 解析预览 UI 流程

**决定**: 解析预览作为详情页的一种模式状态（`ParsePreviewState`），而非独立页面。流程：

1. 用户在记账列表页/FAB 发起"拍照记账"
2. 选择图片后进入预览状态（在 `ExpenseDetailViewModel` 中管理）
3. 预览页复用详情页的编辑表单，预填 AI 解析结果
4. 确认后创建条目 + 上传照片

**理由**: 预览和编辑使用相同的表单字段，复用 UI 组件减少重复代码。

## Risks / Trade-offs

**[大图片上传慢]** → 展示上传进度 loading，10MB 限制已由 spec 定义。未来可加客户端压缩。

**[AI 解析超时 120s]** → 展示可取消的 loading 对话框，超时后提供手动输入入口。

**[照片 URL 需要认证]** → Coil ImageLoader 需注入 session cookie。如果 session 过期，照片加载失败，需要处理 401 场景。

**[Context 传递到 Service 层]** → `uploadExpensePhoto` 需要 Android Context 来读取 URI。通过 Koin 注入 `Application` context，不持有 Activity 引用，避免内存泄漏。

**[Navigation 参数类型]** → expense ID 为后端生成的 8 字符短 ID（字符串），直接作为路由参数安全。

## 文件清单

### 新增文件
| 文件 | 说明 |
|------|------|
| `ui/expense/ExpenseDetailScreen.kt` | 详情/编辑页面 Composable |
| `ui/expense/ExpenseDetailViewModel.kt` | 详情页 ViewModel |

### 修改文件
| 文件 | 变更 |
|------|------|
| `data/remote/dto/NextModels.kt` | 新增 `NextExpensePhoto`, `NextParsePreview`, 相关 Response 类 |
| `data/remote/api/NextApiService.kt` | 新增 `uploadExpensePhoto`, `deleteExpensePhoto`, `parseReceiptPreview` 方法 |
| `ui/expense/ExpenseScreen.kt` | 条目点击导航到详情页；FAB 增加拍照记账入口 |
| `ui/navigation/ErgouNavigation.kt` | 新增 `expense/{expenseId}` 路由 |
| `di/AppModule.kt` | 注册 `ExpenseDetailViewModel` |
| `gradle/libs.versions.toml` | 新增 Coil 3 依赖 |
| `app/build.gradle.kts` | 引用 Coil 依赖 |
| `AndroidManifest.xml` | 添加 `CAMERA` 权限声明 |
