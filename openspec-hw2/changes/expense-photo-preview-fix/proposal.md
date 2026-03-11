## Why

记账功能的 ExpenseEditDialog（列表页弹窗编辑）中，已上传的照片缺少点击预览功能，且整体照片交互体验不完整。具体表现：
1. 编辑状态下，已上传的照片缩略图没有点击事件，无法全屏预览
2. 保存后重新打开编辑弹窗，虽然照片数据正确加载，但无法确认照片内容（因为无法预览）

ExpenseDetailScreen 的 PhotoSection 已经实现了完整的点击预览功能（全屏 Dialog），但 ExpenseEditDialog 中的照片展示缺少该交互。

## What Changes

- 为 ExpenseEditDialog 中已上传照片（existingPhotos）的缩略图添加点击全屏预览功能
- 为 ExpenseEditDialog 中待上传照片（dialogPhotos）的缩略图添加点击全屏预览功能
- 复用 ExpenseDetailScreen 中已有的全屏照片查看器 Dialog 模式

## Capabilities

### New Capabilities

（无新增能力）

### Modified Capabilities

- `expense-photo`: 补充 ExpenseEditDialog 中照片缩略图的点击预览交互，与 ExpenseDetailScreen 行为保持一致

## Impact

- **UI 层**：`ExpenseEditDialog.kt` — 添加 `clickable` 修饰符和全屏预览 Dialog
- **无后端/API 变更**：照片 URL 构建和认证 header 逻辑已存在，仅需在弹窗中复用
- **无数据层变更**：不涉及 ViewModel、Repository、DAO 修改
