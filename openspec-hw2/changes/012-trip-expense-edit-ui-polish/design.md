## Context

`ItemFormDialog` 是差旅费用编辑的核心表单（`TripScreen.kt` lines 1006-1456），当前以 Dialog + Card 方式实现，高度占屏幕 90%。表单字段较多（类型、描述、金额/币种、日期、备注、照片），默认字体和间距偏大，需要滚动才能看到全部内容。

现有问题：
1. 输入框使用默认高度和字体，占空间过大
2. `PhotoThumbnail` 删除按钮使用 `offset(x=4.dp, y=(-4).dp)` 导致超出父 Box 边界被裁剪
3. 照片缩略图无点击预览功能
4. 备注输入框固定 100.dp 高度，始终展示
5. 删除按钮直接调用 `onDelete()` 无确认对话框，且调用方在 confirm 后同时关闭了表单

## Goals / Non-Goals

**Goals:**
- 所有字段在备注折叠状态下一屏可见（无需滚动）
- 照片可点击预览大图
- 删除按钮完整可见、可点击
- 删除操作有确认流程，取消后留在编辑页

**Non-Goals:**
- 不改变表单功能逻辑（类型选择、AI 分析等）
- 不改变 ViewModel / Repository 层
- 不改变照片上传/拍照流程
- 不做多图预览滑动（仅单图预览）

## Decisions

### D1: 紧凑布局 — 统一使用 `bodySmall` + 减少间距

**选择**: 描述、金额、币种输入框的 `textStyle` 设为 `MaterialTheme.typography.bodySmall`（对应约 12sp），字段间 Spacer 从 16.dp 减至 8.dp，日期 Surface 内部 padding 从 16.dp 减至 10.dp。

**替代方案**: 使用自定义 14sp — 但 bodySmall 是 M3 标准 token，语义更清晰且与设计系统一致。

**理由**: 用户要求字体"和日期里面一样大"，日期当前用 `bodyMedium`，但整体要缩小，统一用 `bodySmall` 最合理。

### D2: 照片删除按钮 — 去除 offset，使用 padding 方案

**选择**: 移除 `offset(x=4.dp, y=(-4).dp)`，改为在父 Box 上增加 `padding(top=4.dp, end=4.dp)`，让删除按钮 `align(TopEnd)` 自然位于角落内侧。同时增大 Box 尺寸或增加 `clipToBounds = false` 的 padding wrapper。

**替代方案**: 设置 `clipToBounds = false` — 但 Compose 的 clip(RoundedCornerShape) 会覆盖，不够可靠。

**理由**: offset 导致按钮超出 parent 裁剪区域。padding 方案让按钮完全在 bounds 内，同时保持视觉位置。

### D3: 照片预览 — Dialog + AsyncImage 全屏

**选择**: 新增 `var previewPhotoUrl by remember { mutableStateOf<String?>(null) }`，点击缩略图设置 URL，显示一个全屏 Dialog 包含 AsyncImage。支持远程 URL（已上传照片）和本地 Uri（pending 照片）。

**替代方案**: 使用新 Activity/Navigation — 过重，一个 Dialog 即可满足需求。

**理由**: 保持在同一 Composable 树内，状态简单，不需要 Navigation。

### D4: 备注折叠 — AnimatedVisibility + 可点击标题行

**选择**: 新增 `var notesExpanded by remember { mutableStateOf(false) }`。备注标题行改为可点击 Row，包含"备注"文本 + 展开/折叠箭头图标。点击切换 `notesExpanded`，TextField 包裹在 `AnimatedVisibility(notesExpanded)` 中。已有备注内容时，折叠状态标题旁显示截断摘要。

**替代方案**: 使用 `ExpandableCard` 自定义组件 — 过度封装，这里只需简单状态切换。

### D5: 删除确认 — 表单内 AlertDialog + 状态控制

**选择**: 新增 `var showDeleteConfirm by remember { mutableStateOf(false) }`。删除 IconButton 的 onClick 改为 `{ showDeleteConfirm = true }`。确认对话框：确定 → 调用 `onDelete()`；取消 → `showDeleteConfirm = false`，表单保持打开。

**关键点**: 当前 `onDelete` 回调在调用方（TripDetailContent）中同时执行删除 API 调用和关闭表单。需要确保取消时不触发 `onDelete`，而确认时才触发。

## Risks / Trade-offs

- **[风险] 一屏适配**: 小屏手机（<360dp 宽度）即使压缩后可能仍需滚动 → 表单已有 verticalScroll，降级体验可接受
- **[风险] 照片预览内存**: 大图加载可能 OOM → Coil 默认有内存缓存和采样，无需额外处理
- **[权衡] bodySmall 可读性**: 12sp 对部分用户可能偏小 → 用户明确要求缩小，且 M3 bodySmall 是设计系统内的合理最小值
- **[权衡] 删除按钮位置调整**: 去除 offset 后视觉上按钮稍微内缩 → 功能优先，确保可点击比视觉微调更重要
