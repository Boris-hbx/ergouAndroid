## 1. 紧凑布局 — 字体与间距调整

- [x] 1.1 描述 OutlinedTextField 添加 `textStyle = MaterialTheme.typography.bodySmall`
- [x] 1.2 金额 OutlinedTextField 添加 `textStyle = MaterialTheme.typography.bodySmall`，label 字体同步缩小
- [x] 1.3 币种 OutlinedTextField 添加 `textStyle = MaterialTheme.typography.bodySmall`，label 字体同步缩小
- [x] 1.4 日期 Surface 内部 padding 从 16.dp 减至 10.dp，Text 的 style 改为 `bodySmall`
- [x] 1.5 所有字段间 Spacer 从 16.dp 减至 8.dp（类型→描述、描述→金额、金额→日期、日期→备注、备注→照片）

## 2. 照片删除按钮修复

- [x] 2.1 `PhotoThumbnail`: 移除 `offset(x=4.dp, y=(-4).dp)`，改为父 Box 添加 padding 让删除按钮在 bounds 内
- [x] 2.2 pending 照片的删除按钮（line 1277-1290）: 同样移除 offset，采用与 PhotoThumbnail 一致的 padding 方案

## 3. 照片预览功能

- [x] 3.1 在 `ItemFormDialog` 中新增 `var previewImageModel by remember { mutableStateOf<Any?>(null) }` 状态（支持 String URL 和 Uri）
- [x] 3.2 `PhotoThumbnail` 添加 `onClick` 参数，点击时设置 previewImageModel 为远程照片 URL
- [x] 3.3 pending 照片的 AsyncImage 添加点击事件，设置 previewImageModel 为本地 Uri
- [x] 3.4 新增全屏预览 Dialog：当 `previewImageModel != null` 时显示，包含 AsyncImage + 关闭按钮，点击背景或关闭按钮 dismiss

## 4. 备注折叠/展开

- [x] 4.1 新增 `var notesExpanded by remember { mutableStateOf(false) }` 状态
- [x] 4.2 备注标题行改为可点击 Row：左侧"备注"文本 + 右侧展开/折叠箭头图标（ExpandMore / ExpandLess）
- [x] 4.3 已有备注内容时，折叠状态标题旁显示截断摘要（maxLength 20 字符 + "..."）
- [x] 4.4 OutlinedTextField 包裹在 `AnimatedVisibility(notesExpanded)` 中
- [x] 4.5 确认折叠/展开切换不会丢失已输入的备注内容

## 5. 删除确认对话框

- [x] 5.1 新增 `var showDeleteConfirm by remember { mutableStateOf(false) }` 状态
- [x] 5.2 删除 IconButton 的 onClick 改为 `{ showDeleteConfirm = true }`
- [x] 5.3 新增 AlertDialog：title "确认删除"，text "确定删除此费用项？"，确定按钮调用 `onDelete()`，取消按钮仅关闭对话框
- [x] 5.4 验证取消后编辑表单保持打开，所有字段值不变

## 6. 手动测试验证

- [ ] 6.1 验证编辑表单在备注折叠时所有字段一屏可见（无需滚动）
- [ ] 6.2 验证照片缩略图删除按钮（×）完整可见，不被裁剪
- [ ] 6.3 验证点击照片缩略图弹出大图预览，关闭后回到编辑表单
- [ ] 6.4 验证备注展开/折叠正常，内容在切换后保留
- [ ] 6.5 验证删除 → 取消后留在编辑页面，字段值不变
- [ ] 6.6 验证删除 → 确定后费用项被删除，表单关闭
