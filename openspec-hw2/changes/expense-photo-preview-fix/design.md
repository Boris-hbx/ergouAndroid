## Design

### Approach

复用 `ExpenseDetailScreen` 中已有的全屏照片预览模式（`Dialog` + `AsyncImage`），在 `ExpenseEditDialog` 中添加相同的交互。

### Changes

#### ExpenseEditDialog.kt

1. **添加全屏预览状态**：在 dialog 内部新增 `var showFullScreenPhoto by remember { mutableStateOf<String?>(null) }` 状态变量，存储当前预览照片的 URL 或 URI 字符串。

2. **为已上传照片（existingPhotos）添加点击事件**：在 `AsyncImage` 的 `Modifier` 上添加 `.clickable { }` 修饰符，点击时构建 photoUrl 并设置 `showFullScreenPhoto`。

3. **为本地照片（dialogPhotos）添加点击事件**：在 `AsyncImage` 的 `Modifier` 上添加 `.clickable { }` 修饰符，点击时将 `Uri.toString()` 设置到 `showFullScreenPhoto`。

4. **添加全屏预览 Dialog**：在 dialog 底部添加 `showFullScreenPhoto?.let { }` 块，内容与 `ExpenseDetailScreen` 第 502-525 行的实现一致：
   - 使用 `Dialog(properties = DialogProperties(usePlatformDefaultWidth = false))`
   - 内部 `Box` 包裹 `AsyncImage`，点击关闭
   - 对于服务器照片，使用 `httpHeaders` 附带 session cookie
   - 对于本地 URI 照片，直接传入 URI 字符串（Coil 支持 `content://` URI）

### Key Decisions

- **不新建组件**：全屏预览逻辑简单（约 20 行），直接在 `ExpenseEditDialog` 内部实现，无需抽取公共组件
- **统一数据类型**：`showFullScreenPhoto` 使用 `String?` 类型，服务器照片存 URL，本地照片存 URI.toString()，Coil 均可处理
- **认证 header 判断**：服务器照片 URL 以 `https://` 开头时附带 Cookie header，本地 URI（`content://`）则不需要

### Not Changing

- ViewModel 层：无需修改，所有状态和交互逻辑均在 UI 层完成
- PhotoSection（DetailScreen）：已有完整预览功能，不受影响
- 照片上传/删除流程：不变
