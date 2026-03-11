## Why

差旅费用编辑表单（`ItemFormDialog`）当前输入框尺寸偏大、字体偏大，导致表单内容在一屏内无法全部展示，用户需要滚动才能看到所有字段。同时存在几个功能缺陷：票据照片无法预览、删除按钮被遮盖、点击删除取消后意外退出编辑页面。

## What Changes

- 缩小描述/内容输入框的文字字体
- 金额+币种行：输入框更窄更扁，字体缩小至与日期行一致
- 日期输入框高度缩小（更扁）
- 票据照片支持点击预览大图，修复删除按钮（灰色×）被遮盖的问题
- 备注输入框改为默认折叠，点击展开/收起，折叠状态下用户可一屏看完所有内容
- 删除确认对话框点击"取消"后留在编辑页面，不跳出

## Capabilities

### New Capabilities
- `expense-form-compact-layout`: 费用编辑表单紧凑布局 — 输入框尺寸、字体、间距的统一调整
- `expense-photo-preview`: 票据照片预览与删除按钮修复
- `expense-notes-collapse`: 备注输入框折叠/展开交互
- `expense-delete-cancel-fix`: 删除取消后不退出编辑页面的行为修复

### Modified Capabilities

（无现有 spec 需要修改）

## Impact

- 主要影响文件：`TripScreen.kt` 中的 `ItemFormDialog` composable（约 lines 1006-1450）
- 可能新增一个全屏图片预览 composable（或复用现有图片查看组件）
- 无 API 变更、无数据库变更、无依赖变更
- 纯 UI 层修改，不影响 ViewModel 或 Repository
