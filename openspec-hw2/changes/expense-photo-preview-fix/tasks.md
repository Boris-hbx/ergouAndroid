## Tasks

### ExpenseEditDialog — 添加照片全屏预览

- [x] 在 `ExpenseEditDialog` 函数内添加 `var showFullScreenPhoto by remember { mutableStateOf<String?>(null) }` 状态变量
- [x] 为 existingPhotos 的 `AsyncImage`（约第 387 行）添加 `.clickable` 修饰符，点击时构建 photoUrl 并赋值 `showFullScreenPhoto`
- [x] 为 dialogPhotos 的 `AsyncImage`（约第 424 行）添加 `.clickable` 修饰符，点击时将 `uri.toString()` 赋值 `showFullScreenPhoto`
- [x] 在 dialog 底部（DatePickerDialog 等之前）添加全屏预览 Dialog：
  - `showFullScreenPhoto?.let { url -> Dialog(...) { AsyncImage(...) } }`
  - 服务器照片（`https://` 开头）附带 `httpHeaders` Cookie
  - 本地 URI 照片直接传入
  - 点击 Box 关闭预览
- [x] 添加必要的 import：`DialogProperties`、`Dialog`（如果尚未导入）
- [ ] 手动验证：编辑已有记账 → 点击已上传照片缩略图 → 确认全屏预览正常显示 → 点击关闭后编辑状态不变
- [ ] 手动验证：新增记账 → 添加本地照片 → 点击缩略图 → 确认全屏预览正常显示
